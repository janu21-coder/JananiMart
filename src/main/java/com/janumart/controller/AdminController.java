package com.janumart.controller;

import com.janumart.model.User;
import com.janumart.service.AdminService;
import com.janumart.service.DashboardService;
import com.janumart.util.JsonUtil;
import com.janumart.util.SessionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** /api/v1/admin/* (ADMIN only - enforced by filter and here). */
public class AdminController {

    private final AdminService adminService = new AdminService();
    private final DashboardService dashboardService = new DashboardService();

    public void handle(HttpServletRequest req, HttpServletResponse resp, String method, List<String> seg)
            throws IOException {
        User admin = SessionUtil.requireRole(req, "ADMIN");
        String root = seg.isEmpty() ? "" : seg.get(0);

        switch (root) {
            case "stats" -> {
                if (!method.equals("GET")) {
                    JsonUtil.error(resp, 405, "Method not allowed.");
                    return;
                }
                JsonUtil.ok(resp, "", dashboardService.admin());
            }
            case "users" -> {
                if (method.equals("GET")) {
                    JsonUtil.ok(resp, "", adminService.listUsers(
                            req.getParameter("role"), req.getParameter("q")));
                } else if (method.equals("DELETE") && seg.size() == 2) {
                    adminService.deleteUser(admin, idOf(seg.get(1), resp));
                    if (resp.isCommitted()) {
                        return;
                    }
                    JsonUtil.ok(resp, "User deleted successfully.", null);
                } else {
                    JsonUtil.error(resp, 405, "Method not allowed.");
                }
            }
            case "products" -> {
                if (method.equals("GET")) {
                    JsonUtil.ok(resp, "", adminService.listProducts(
                            req.getParameter("q"),
                            intOf(req.getParameter("page"), 1),
                            intOf(req.getParameter("pageSize"), 20)));
                } else if (method.equals("DELETE") && seg.size() == 2) {
                    adminService.deleteProduct(admin, idOf(seg.get(1), resp));
                    if (resp.isCommitted()) {
                        return;
                    }
                    JsonUtil.ok(resp, "Product removed successfully.", null);
                } else {
                    JsonUtil.error(resp, 405, "Method not allowed.");
                }
            }
            case "orders" -> {
                if (method.equals("GET")) {
                    JsonUtil.ok(resp, "", adminService.listOrders(req.getParameter("status")));
                } else {
                    JsonUtil.error(resp, 405, "Method not allowed.");
                }
            }
            default -> JsonUtil.error(resp, 404, "Admin endpoint not found.");
        }
    }

    private int idOf(String raw, HttpServletResponse resp) throws IOException {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            JsonUtil.error(resp, 400, "Invalid id.");
            return 0;
        }
    }

    private static int intOf(String v, int def) {
        try {
            return v == null ? def : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}