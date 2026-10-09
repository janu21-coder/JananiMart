package com.janumart.service;

import com.janumart.dao.CartDao;
import com.janumart.dao.OrderDao;
import com.janumart.dao.ProductDao;
import com.janumart.dao.UserDao;
import com.janumart.model.Order;
import com.janumart.model.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Aggregated statistics for the buyer / seller / admin dashboards. */
public class DashboardService {

    private final UserDao userDao = new UserDao();
    private final ProductDao productDao = new ProductDao();
    private final OrderDao orderDao = new OrderDao();
    private final CartDao cartDao = new CartDao();

    public Map<String, Object> buyer(int userId) {
        Map<String, Object> m = new HashMap<>();
        m.put("orderCount", orderDao.countByBuyer(userId));
        m.put("totalSpent", orderDao.spentByBuyer(userId));
        m.put("cartCount", cartDao.countItems(userId));
        m.put("recentOrders", orderDao.recentForBuyer(userId, 5));
        return m;
    }

    public Map<String, Object> seller(int sellerId) {
        List<Product> products = productDao.bySeller(sellerId);
        long active = products.stream().filter(Product::isInStock).count();

        Map<String, Object> m = new HashMap<>();
        m.put("productCount", products.size());
        m.put("activeProducts", active);
        m.put("lowStockProducts", productDao.lowStock(sellerId, 5));
        m.put("orderCount", orderDao.countForSeller(sellerId));
        m.put("revenue", orderDao.revenueBySeller(sellerId));
        m.put("topProducts", orderDao.topProducts(sellerId, 5));
        m.put("recentOrders", orderDao.recentForSeller(sellerId, 6));
        m.put("newArrivals", products.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(4).toList());
        return m;
    }

    public Map<String, Object> admin() {
        Map<String, Object> m = new HashMap<>();
        m.put("userCount", userDao.countAll());
        m.put("buyerCount", userDao.countByRole("BUYER"));
        m.put("sellerCount", userDao.countByRole("SELLER"));
        m.put("productCount", productDao.countAll());
        m.put("orderCount", orderDao.countAll());
        m.put("revenue", orderDao.revenueTotal());
        m.put("delivered", orderDao.countByStatus("DELIVERED"));
        m.put("cancelled", orderDao.countByStatus("CANCELLED"));
        m.put("pending", orderDao.countByStatus("PENDING"));
        m.put("confirmed", orderDao.countByStatus("CONFIRMED"));
        m.put("recentOrders", orderDao.recentOrders(8));
        m.put("recentProducts", productDao.newest(6));
        return m;
    }
}