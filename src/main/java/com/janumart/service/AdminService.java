package com.janumart.service;

import com.janumart.dao.OrderDao;
import com.janumart.dao.UserDao;
import com.janumart.dto.ProductQuery;
import com.janumart.exception.ValidationException;
import com.janumart.model.Order;
import com.janumart.model.User;

import java.util.List;
import java.util.Map;

/** Admin-only back office operations (protected by filters + role checks). */
public class AdminService {

    private final UserDao userDao = new UserDao();
    private final OrderDao orderDao = new OrderDao();
    private final ProductService productService = new ProductService();

    public List<User> listUsers(String role, String q) {
        return userDao.list(q, role);
    }

    public void deleteUser(User admin, int userId) {
        if (admin.getId() == userId) {
            throw new ValidationException("You cannot delete your own account.");
        }
        userDao.delete(userId);
    }

    public List<Order> listOrders(String status) {
        return orderDao.listAll(status);
    }

    public Map<String, Object> listProducts(String q, int page, int pageSize) {
        ProductQuery query = new ProductQuery();
        query.setQ(q);
        query.setPage(page);
        query.setPageSize(pageSize);
        return productService.list(query);
    }

    public void deleteProduct(User admin, int productId) {
        productService.delete(admin, productId);
    }
}