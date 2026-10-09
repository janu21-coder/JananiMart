package com.janumart.dao;

import com.janumart.model.Order;
import com.janumart.model.OrderItem;
import com.janumart.model.Product;
import com.janumart.util.DBUtil;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** JDBC access for orders and their line items. */
public class OrderDao {

    private final DataSource ds;

    public OrderDao() {
        this.ds = DBUtil.getDataSource();
    }

    private static final String ORDER_SELECT =
            "SELECT o.id, o.buyer_id, o.status, o.total_amount, o.customer_name, o.address, " +
            "o.city, o.state, o.pincode, o.phone, o.payment_method, o.created_at, u.name AS buyer_name " +
            "FROM orders o JOIN users u ON u.id = o.buyer_id ";

    private static final String ITEM_SELECT =
            "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, " +
            "p.name AS product_name, p.image_url, p.seller_id AS product_seller " +
            "FROM order_items oi LEFT JOIN products p ON p.id = oi.product_id ";

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setBuyerId(rs.getInt("buyer_id"));
        o.setStatus(rs.getString("status"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setAddress(rs.getString("address"));
        o.setCity(rs.getString("city"));
        o.setState(rs.getString("state"));
        o.setPincode(rs.getString("pincode"));
        o.setPhone(rs.getString("phone"));
        o.setPaymentMethod(rs.getString("payment_method"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            o.setCreatedAt(ts.toLocalDateTime());
        }
        try {
            o.setBuyerName(rs.getString("buyer_name"));
        } catch (SQLException ignored) {
            // buyer_name is not present in some projections
        }
        return o;
    }

    private OrderItem mapItem(ResultSet rs) throws SQLException {
        OrderItem i = new OrderItem();
        i.setId(rs.getInt("id"));
        i.setOrderId(rs.getInt("order_id"));
        int pid = rs.getInt("product_id");
        i.setProductId(rs.wasNull() ? null : pid);
        i.setQuantity(rs.getInt("quantity"));
        i.setUnitPrice(rs.getBigDecimal("unit_price"));
        i.setProductName(rs.getString("product_name"));
        i.setImageUrl(rs.getString("image_url"));
        i.setSellerId(rs.getInt("product_seller"));
        return i;
    }

    private List<OrderItem> itemsForOrder(Connection c, int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = ITEM_SELECT + " WHERE oi.order_id = ? ORDER BY oi.id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
        }
        return items;
    }

    /** Inserts the order on the given connection and returns the generated id. */
    public int insert(Connection c, Order o) throws SQLException {
        String sql = "INSERT INTO orders (buyer_id, status, total_amount, customer_name, address, " +
                "city, state, pincode, phone, payment_method, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime now = LocalDateTime.now();
            ps.setInt(1, o.getBuyerId());
            ps.setString(2, o.getStatus());
            ps.setBigDecimal(3, o.getTotalAmount());
            ps.setString(4, o.getCustomerName());
            ps.setString(5, o.getAddress());
            ps.setString(6, o.getCity());
            ps.setString(7, o.getState());
            ps.setString(8, o.getPincode());
            ps.setString(9, o.getPhone());
            ps.setString(10, o.getPaymentMethod());
            ps.setTimestamp(11, Timestamp.valueOf(now));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    o.setId(id);
                    return id;
                }
            }
            throw new SQLException("No generated order id");
        }
    }

    /** Convenience overload with its own connection. */
    public int insert(Order o) {
        try (Connection c = ds.getConnection()) {
            return insert(c, o);
        } catch (SQLException e) {
            throw new RuntimeException("DB error while creating order", e);
        }
    }

    public void insertItem(Connection c, OrderItem item) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getInt(1));
                }
            }
        }
    }

    public Order findById(int id) {
        String sql = ORDER_SELECT + " WHERE o.id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Order o = mapOrder(rs);
                o.setItems(itemsForOrder(c, id));
                return o;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while finding order", e);
        }
    }

    public List<Order> findByBuyer(int buyerId) {
        String sql = ORDER_SELECT + " WHERE o.buyer_id = ? ORDER BY o.created_at DESC, o.id DESC";
        List<Order> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapOrder(rs));
                }
            }
            for (Order o : out) {
                o.setItems(itemsForOrder(c, o.getId()));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("DB error while listing buyer orders", e);
        }
    }

    public List<Order> listAll(String status) {
        StringBuilder sql = new StringBuilder(ORDER_SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY o.created_at DESC, o.id DESC");
        List<Order> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapOrder(rs));
                }
            }
            for (Order o : out) {
                o.setItems(itemsForOrder(c, o.getId()));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("DB error while listing orders", e);
        }
    }

    /** Orders that contain at least one product belonging to the seller (items pre-filtered). */
    public List<Order> listForSeller(int sellerId) {
        String sql = "SELECT DISTINCT o.id, o.created_at FROM orders o " +
                "JOIN order_items oi ON oi.order_id = o.id " +
                "JOIN products p ON p.id = oi.product_id " +
                "WHERE p.seller_id = ? ORDER BY o.created_at DESC, o.id DESC";
        List<Order> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = findById(rs.getInt(1));
                    List<OrderItem> own = new ArrayList<>();
                    for (OrderItem item : o.getItems()) {
                        if (item.getSellerId() == sellerId) {
                            own.add(item);
                        }
                    }
                    o.setItems(own);
                    out.add(o);
                }
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("DB error while listing seller orders", e);
        }
    }

    public void updateStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new com.janumart.exception.NotFoundException("Order not found.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while updating order status", e);
        }
    }

    /** Transaction-scoped variant (used by checkout to record the computed total). */
    public void updateTotal(Connection c, int orderId, BigDecimal total) throws SQLException {
        String sql = "UPDATE orders SET total_amount = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, total);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    public boolean containsSellerProduct(int orderId, int sellerId) {
        String sql = "SELECT COUNT(*) FROM order_items oi JOIN products p ON p.id = oi.product_id " +
                "WHERE oi.order_id = ? AND p.seller_id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error checking seller order", e);
        }
    }

    /* ---------- dashboard statistics ---------- */

    public long countAll() {
        return scalarLong("SELECT COUNT(*) FROM orders");
    }

    public long countByStatus(String status) {
        return scalarLong("SELECT COUNT(*) FROM orders WHERE status = ?", status);
    }

    public long countByBuyer(int buyerId) {
        return scalarLong("SELECT COUNT(*) FROM orders WHERE buyer_id = ?", buyerId);
    }

    public long countForSeller(int sellerId) {
        return scalarLong("SELECT COUNT(DISTINCT oi.order_id) FROM order_items oi " +
                "JOIN products p ON p.id = oi.product_id WHERE p.seller_id = ?", sellerId);
    }

    public BigDecimal revenueTotal() {
        return scalarDecimal("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status <> 'CANCELLED'");
    }

    public BigDecimal spentByBuyer(int buyerId) {
        return scalarDecimal("SELECT COALESCE(SUM(total_amount), 0) FROM orders " +
                "WHERE buyer_id = ? AND status <> 'CANCELLED'", buyerId);
    }

    public BigDecimal revenueBySeller(int sellerId) {
        return scalarDecimal("SELECT COALESCE(SUM(oi.quantity * oi.unit_price), 0) FROM order_items oi " +
                "JOIN products p ON p.id = oi.product_id JOIN orders o ON o.id = oi.order_id " +
                "WHERE p.seller_id = ? AND o.status <> 'CANCELLED'", sellerId);
    }

    public List<Order> recentOrders(int limit) {
        String sql = ORDER_SELECT + " ORDER BY o.created_at DESC, o.id DESC LIMIT ?";
        List<Order> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapOrder(rs));
                }
            }
            for (Order o : out) {
                o.setItems(findById(o.getId()).getItems());
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("DB error while loading recent orders", e);
        }
    }

    public List<Order> recentForBuyer(int buyerId, int limit) {
        String sql = ORDER_SELECT + " WHERE o.buyer_id = ? ORDER BY o.created_at DESC, o.id DESC LIMIT ?";
        List<Order> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapOrder(rs));
                }
            }
            for (Order o : out) {
                o.setItems(findById(o.getId()).getItems());
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("DB error while loading recent buyer orders", e);
        }
    }

    public List<Order> recentForSeller(int sellerId, int limit) {
        String sql = "SELECT DISTINCT o.id, o.created_at FROM orders o " +
                "JOIN order_items oi ON oi.order_id = o.id " +
                "JOIN products p ON p.id = oi.product_id " +
                "WHERE p.seller_id = ? ORDER BY o.created_at DESC, o.id DESC LIMIT ?";
        List<Order> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = findById(rs.getInt(1));
                    List<OrderItem> own = new ArrayList<>();
                    for (OrderItem item : o.getItems()) {
                        if (item.getSellerId() == sellerId) {
                            own.add(item);
                        }
                    }
                    o.setItems(own);
                    out.add(o);
                }
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("DB error while loading recent seller orders", e);
        }
    }

    public List<Product> topProducts(int sellerId, int limit) {
        String sql = "SELECT p.id, p.name, p.price, p.image_url, SUM(oi.quantity) AS sold " +
                "FROM order_items oi JOIN products p ON p.id = oi.product_id " +
                "JOIN orders o ON o.id = oi.order_id " +
                "WHERE p.seller_id = ? AND o.status <> 'CANCELLED' " +
                "GROUP BY p.id, p.name, p.price, p.image_url ORDER BY sold DESC LIMIT ?";
        List<Product> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getBigDecimal("price"));
                    p.setImageUrl(rs.getString("image_url"));
                    p.setSoldQty(rs.getLong("sold"));
                    out.add(p);
                }
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("DB error while loading top products", e);
        }
    }

    private long scalarLong(String sql, Object... params) {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error in scalar query", e);
        }
    }

    private BigDecimal scalarDecimal(String sql, Object... params) {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                BigDecimal v = rs.getBigDecimal(1);
                return v == null ? BigDecimal.ZERO : v;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error in scalar query", e);
        }
    }
}