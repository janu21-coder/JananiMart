package com.janumart.controller;

import com.janumart.model.User;
import com.janumart.service.DashboardService;
import com.janumart.service.OrderService;
import com.janumart.service.ProductService;
import com.janumart.util.JsonUtil;
import com.janumart.util.SessionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** /api/v1/seller/* (SELLER only - enforced by filter and here). */
public class SellerController {

    private final DashboardService dashboardService = new DashboardService();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();

    public void handle(HttpServletRequest req, HttpServletResponse resp, String method, List<String> seg)
            throws IOException {
        User seller = SessionUtil.requireRole(req, "SELLER");
        String root = seg.isEmpty() ? "" : seg.get(0);

        switch (root) {
            case "stats" -> {
                if (!method.equals("GET")) {
                    JsonUtil.error(resp, 405, "Method not allowed.");
                    return;
                }
                JsonUtil.ok(resp, "", dashboardService.seller(seller.getId()));
            }
            case "products" -> {
                if (!method.equals("GET")) {
                    JsonUtil.error(resp, 405, "Method not allowed.");
                    return;
                }
                JsonUtil.ok(resp, "", productService.bySeller(seller.getId()));
            }
            case "orders" -> {
                if (!method.equals("GET")) {
                    JsonUtil.error(resp, 405, "Method not allowed.");
                    return;
                }
                JsonUtil.ok(resp, "", orderService.forSeller(seller.getId()));
            }
            default -> JsonUtil.error(resp, 404, "Seller endpoint not found.");
        }
    }
}