package com.janumart.service;

import com.janumart.dao.CartDao;
import com.janumart.dao.OrderDao;
import com.janumart.dao.ProductDao;
import com.janumart.dto.CheckoutRequest;
import com.janumart.dto.StatusUpdateRequest;
import com.janumart.exception.ForbiddenException;
import com.janumart.exception.NotFoundException;
import com.janumart.exception.ValidationException;
import com.janumart.model.CartItem;
import com.janumart.model.Order;
import com.janumart.model.OrderItem;
import com.janumart.model.Product;
import com.janumart.model.User;
import com.janumart.util.DBUtil;
import com.janumart.util.Validate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Order placement (database transaction), order history, tracking and
 * status transitions with strict validation.
 */
public class OrderService {

    private final OrderDao orderDao = new OrderDao();
    private final CartDao cartDao = new CartDao();
    private final ProductDao productDao = new ProductDao();

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_SHIPPED = "SHIPPED";
    private static final String STATUS_DELIVERED = "DELIVERED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            STATUS_PENDING, Set.of(STATUS_CONFIRMED, STATUS_CANCELLED),
            STATUS_CONFIRMED, Set.of(STATUS_SHIPPED, STATUS_CANCELLED),
            STATUS_SHIPPED, Set.of(STATUS_DELIVERED),
            STATUS_DELIVERED, Set.of(),
            STATUS_CANCELLED, Set.of()
    );

    /**
     * Checkout: BEGIN -> validate cart -> validate stock -> create order ->
     * create items -> reduce stock -> clear cart -> COMMIT (ROLLBACK on any error).
     */
    public Order placeOrder(User buyer, CheckoutRequest req) {
        String name = Validate.requireText(req.getCustomerName(), "Name", 100);
        String address = Validate.requireText(req.getAddress(), "Address", 300);
        String city = Validate.requireText(req.getCity(), "City", 80);
        String state = Validate.requireText(req.getState(), "State", 80);
        String pincode = Validate.requireText(req.getPincode(), "Pincode", 10);
        Validate.pincode(pincode);
        String phone = Validate.requireText(req.getPhone(), "Phone number", 15);
        Validate.phone(phone);
        String payment = Validate.requireText(req.getPaymentMethod(), "Payment method", 10);
        Validate.inOneOf(payment, "Payment method", "UPI", "CARD", "COD");

        List<CartItem> cart = cartDao.findByUser(buyer.getId());
        if (cart.isEmpty()) {
            throw new ValidationException("Your cart is empty. Add products before checking out.");
        }

        Connection c = DBUtil.getConnection();
        try {
            c.setAutoCommit(false);
            try {
                // Pass 1: validate stock and compute the total (transaction-scoped reads).
                BigDecimal total = BigDecimal.ZERO;
                for (CartItem item : cart) {
                    Product p = productDao.findById(item.getProductId());
                    if (p == null) {
                        throw new ValidationException("A product in your cart is no longer available.");
                    }
                    if (p.getStockQty() < item.getQuantity()) {
                        throw new ValidationException(
                                "Only " + p.getStockQty() + " units of " + p.getName() + " are in stock right now.");
                    }
                    total = total.add(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }

                Order order = new Order();
                order.setBuyerId(buyer.getId());
                order.setStatus(STATUS_CONFIRMED); // mock payment succeeded
                order.setCustomerName(name);
                order.setAddress(address);
                order.setCity(city);
                order.setState(state);
                order.setPincode(pincode);
                order.setPhone(phone);
                order.setPaymentMethod(payment);
                order.setTotalAmount(total);
                orderDao.insert(c, order);

                // Pass 2: create items and decrement stock.
                for (CartItem item : cart) {
                    Product p = productDao.findById(item.getProductId());
                    OrderItem oi = new OrderItem();
                    oi.setOrderId(order.getId());
                    oi.setProductId(p.getId());
                    oi.setQuantity(item.getQuantity());
                    oi.setUnitPrice(p.getPrice());
                    orderDao.insertItem(c, oi);

                    int updated = productDao.decrementStock(c, p.getId(), item.getQuantity());
                    if (updated == 0) {
                        throw new ValidationException(p.getName() + " is out of stock.");
                    }
                }

                cartDao.clear(c, buyer.getId());
                c.commit();

                Order placed = orderDao.findById(order.getId());
                if (placed == null) {
                    throw new NotFoundException("Order could not be loaded after checkout.");
                }
                return placed;
            } catch (Exception e) {
                try {
                    c.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Checkout transaction failed", e);
        } finally {
            try {
                c.setAutoCommit(true);
            } catch (SQLException ignored) {
                // pool will reset state on close
            }
            try {
                c.close();
            } catch (SQLException ignored) {
                // nothing to do
            }
        }
    }

    public List<Order> forBuyer(int buyerId) {
        return orderDao.findByBuyer(buyerId);
    }

    public Order forBuyerDetail(int buyerId, int orderId) {
        Order o = requireOrder(orderId);
        if (o.getBuyerId() != buyerId) {
            throw new ForbiddenException("You cannot view another buyer's order.");
        }
        return o;
    }

    public List<Order> forSeller(int sellerId) {
        return orderDao.listForSeller(sellerId);
    }

    public Order forSellerDetail(int sellerId, int orderId) {
        if (!orderDao.containsSellerProduct(orderId, sellerId)) {
            throw new ForbiddenException("This order does not contain your products.");
        }
        Order o = requireOrder(orderId);
        o.setItems(o.getItems().stream()
                .filter(i -> i.getSellerId() == sellerId)
                .toList());
        return o;
    }

    public List<Order> all(String status) {
        return orderDao.listAll(status);
    }

    public Order getForAdmin(int orderId) {
        return requireOrder(orderId);
    }

    public Order updateStatus(User actor, int orderId, StatusUpdateRequest req) {
        String to = Validate.requireText(req.getStatus(), "Status", 12);
        Validate.inOneOf(to, "Status", STATUS_PENDING, STATUS_CONFIRMED, STATUS_SHIPPED,
                STATUS_DELIVERED, STATUS_CANCELLED);

        Order o = requireOrder(orderId);
        String from = o.getStatus();

        if (actor.is("BUYER")) {
            if (o.getBuyerId() != actor.getId()) {
                throw new ForbiddenException("You can only manage your own orders.");
            }
            if (!to.equals(STATUS_CANCELLED)) {
                throw new ForbiddenException("Buyers can only cancel an order.");
            }
        } else if (actor.is("SELLER")) {
            if (!orderDao.containsSellerProduct(orderId, actor.getId())) {
                throw new ForbiddenException("This order does not contain your products.");
            }
            if (to.equals(STATUS_CANCELLED)) {
                throw new ForbiddenException("Only the buyer or an admin can cancel an order.");
            }
        }

        if (!TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw new ValidationException(
                    "Order status cannot move from " + from + " to " + to + ".");
        }

        orderDao.updateStatus(orderId, to);
        return requireOrder(orderId);
    }

    private Order requireOrder(int orderId) {
        Validate.positiveId(orderId, "Order id");
        Order o = orderDao.findById(orderId);
        if (o == null) {
            throw new NotFoundException("Order not found.");
        }
        return o;
    }
}