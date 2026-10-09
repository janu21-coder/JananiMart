package com.janumart.controller;

import com.janumart.dto.CheckoutRequest;
import com.janumart.dto.StatusUpdateRequest;
import com.janumart.model.User;
import com.janumart.service.OrderService;
import com.janumart.util.JsonUtil;
import com.janumart.util.SessionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** /api/v1/orders/*  - role-aware access (BUYER own, SELLER own-products, ADMIN all). */
public class OrderController {

    private final OrderService orderService = new OrderService();

    public void handle(HttpServletRequest req, HttpServletResponse resp, String method, List<String> seg)
            throws IOException {
        User user = SessionUtil.requireUser(req);

        if (seg.isEmpty()) {
            if (method.equals("POST")) {
                if (!user.is("BUYER")) {
                    JsonUtil.error(resp, 403, "Only buyers can place orders.");
                    return;
                }
                CheckoutRequest cr = JsonUtil.parse(req, CheckoutRequest.class);
                JsonUtil.created(resp, "Order placed successfully!",
                        orderService.placeOrder(user, cr));
            } else if (method.equals("GET")) {
                Object list;
                if (user.is("BUYER")) {
                    list = orderService.forBuyer(user.getId());
                } else if (user.is("SELLER")) {
                    list = orderService.forSeller(user.getId());
                } else if (user.is("ADMIN")) {
                    list = orderService.all(req.getParameter("status"));
                } else {
                    JsonUtil.error(resp, 403, "Access denied.");
                    return;
                }
                JsonUtil.ok(resp, "", list);
            } else {
                JsonUtil.error(resp, 405, "Method not allowed.");
            }
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(seg.get(0));
        } catch (NumberFormatException e) {
            JsonUtil.error(resp, 400, "Invalid order id.");
            return;
        }

        if (seg.size() == 1) {
            if (method.equals("GET")) {
                Object order;
                if (user.is("BUYER")) {
                    order = orderService.forBuyerDetail(user.getId(), orderId);
                } else if (user.is("SELLER")) {
                    order = orderService.forSellerDetail(user.getId(), orderId);
                } else if (user.is("ADMIN")) {
                    order = orderService.getForAdmin(orderId);
                } else {
                    JsonUtil.error(resp, 403, "Access denied.");
                    return;
                }
                JsonUtil.ok(resp, "", order);
            } else {
                JsonUtil.error(resp, 405, "Method not allowed.");
            }
            return;
        }

        if (seg.size() == 2 && seg.get(1).equals("status") && method.equals("PUT")) {
            StatusUpdateRequest sr = JsonUtil.parse(req, StatusUpdateRequest.class);
            JsonUtil.ok(resp, "Order status updated to " + sr.getStatus() + ".",
                    orderService.updateStatus(user, orderId, sr));
            return;
        }

        JsonUtil.error(resp, 404, "Order endpoint not found.");
    }
}