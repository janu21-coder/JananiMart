package com.janumart.controller;

import com.janumart.dao.ProductDao;
import com.janumart.dao.UserDao;
import com.janumart.model.Category;
import com.janumart.model.Product;
import com.janumart.model.User;
import com.janumart.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Serves the marketplace homepage with trending/best-seller/new sections. */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final UserDao userDao = new UserDao();
    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        req.setAttribute("trending", productService.trending(8));
        req.setAttribute("bestSellers", productService.bestSellers(8));
        req.setAttribute("newArrivals", productService.newest(8));
        req.setAttribute("categories", Category.all());

        Map<String, List<Product>> categoryRows = new LinkedHashMap<>();
        categoryRows.put("Watches", productService.byCategory("Watches", 6));
        categoryRows.put("Bags", productService.byCategory("Bags", 6));
        categoryRows.put("Jewellery", productService.byCategory("Jewellery", 6));
        categoryRows.put("Sunglasses", productService.byCategory("Sunglasses", 6));
        categoryRows.put("Travel Accessories", productService.byCategory("Travel Accessories", 6));
        categoryRows.put("Gifts", productService.byCategory("Gifts", 6));
        req.setAttribute("categoryRows", categoryRows);

        List<Map<String, Object>> sellers = new ArrayList<>();
        int productCount = 0;
        for (User s : userDao.list(null, "SELLER")) {
            List<Product> pl = productDao.bySeller(s.getId());
            productCount += pl.size();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", s.getName());
            m.put("count", pl.size());
            m.put("image", pl.isEmpty() ? null : pl.get(0).getImageUrl());
            sellers.add(m);
        }
        req.setAttribute("sellers", sellers);
        req.setAttribute("sellerCount", sellers.size());
        req.setAttribute("productCount", productCount);

        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}