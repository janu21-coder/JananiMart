package com.janumart.service;

import com.janumart.dao.CartDao;
import com.janumart.dao.ProductDao;
import com.janumart.dto.CartItemRequest;
import com.janumart.exception.NotFoundException;
import com.janumart.exception.ValidationException;
import com.janumart.model.CartItem;
import com.janumart.model.Product;
import com.janumart.util.Validate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Buyer cart operations (BUYER role only). */
public class CartService {

    private final CartDao cartDao = new CartDao();
    private final ProductDao productDao = new ProductDao();

    public Map<String, Object> view(int userId) {
        List<CartItem> items = cartDao.findByUser(userId);
        BigDecimal subtotal = BigDecimal.ZERO;
        int count = 0;
        for (CartItem item : items) {
            subtotal = subtotal.add(item.getLineTotal());
            count += item.getQuantity();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("subtotal", subtotal);
        data.put("count", count);
        return data;
    }

    public void add(int userId, CartItemRequest req) {
        if (req.getProductId() == null) {
            throw new ValidationException("Product id is required.");
        }
        int qty = req.getQuantity() == null ? 1 : req.getQuantity();
        Validate.positiveQuantity(qty);

        Product p = productDao.findById(req.getProductId());
        if (p == null) {
            throw new NotFoundException("Product not found.");
        }
        if (p.getStockQty() <= 0) {
            throw new ValidationException("Sorry, " + p.getName() + " is out of stock.");
        }
        if (qty > p.getStockQty()) {
            throw new ValidationException("Only " + p.getStockQty() + " units of " + p.getName() + " are available.");
        }

        CartItem existing = cartDao.findItem(userId, req.getProductId());
        if (existing == null) {
            cartDao.insert(userId, req.getProductId(), qty);
        } else {
            int newQty = existing.getQuantity() + qty;
            if (newQty > p.getStockQty()) {
                throw new ValidationException("Only " + p.getStockQty() + " units of " + p.getName() + " are available in stock.");
            }
            cartDao.updateQuantity(existing.getId(), newQty);
        }
    }

    public void updateQuantity(int userId, int productId, Integer quantity) {
        Validate.positiveId(productId, "Product id");
        if (quantity == null) {
            throw new ValidationException("Quantity is required.");
        }
        Validate.positiveQuantity(quantity);

        CartItem existing = cartDao.findItem(userId, productId);
        if (existing == null) {
            throw new NotFoundException("Item is not in your cart.");
        }
        if (quantity > existing.getStock()) {
            throw new ValidationException(
                    "Only " + existing.getStock() + " units of this item are available in stock.");
        }
        cartDao.updateQuantity(existing.getId(), quantity);
    }

    public void remove(int userId, int productId) {
        Validate.positiveId(productId, "Product id");
        cartDao.deleteItem(userId, productId);
    }

    public void clear(int userId) {
        cartDao.clear(userId);
    }

    public long count(int userId) {
        return cartDao.countItems(userId);
    }
}