package com.janumart.controller;

import com.janumart.dto.CartItemRequest;
import com.janumart.model.User;
import com.janumart.service.CartService;
import com.janumart.util.JsonUtil;
import com.janumart.util.SessionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** /api/v1/cart/* (BUYER authenticated). */
public class CartController {

    private final CartService cartService = new CartService();

    public void handle(HttpServletRequest req, HttpServletResponse resp, String method, List<String> seg)
            throws IOException {
        User user = SessionUtil.requireRole(req, "BUYER");

        if (seg.isEmpty()) {
            switch (method) {
                case "GET" -> JsonUtil.ok(resp, "", cartService.view(user.getId()));
                case "POST" -> {
                    CartItemRequest cir = JsonUtil.parse(req, CartItemRequest.class);
                    cartService.add(user.getId(), cir);
                    JsonUtil.ok(resp, "Added to cart.", cartService.view(user.getId()));
                }
                default -> JsonUtil.error(resp, 405, "Method not allowed.");
            }
            return;
        }

        int productId;
        try {
            productId = Integer.parseInt(seg.get(0));
        } catch (NumberFormatException e) {
            JsonUtil.error(resp, 400, "Invalid product id.");
            return;
        }

        switch (method) {
            case "PUT" -> {
                CartItemRequest cir = JsonUtil.parse(req, CartItemRequest.class);
                cartService.updateQuantity(user.getId(), productId, cir == null ? null : cir.getQuantity());
                JsonUtil.ok(resp, "Cart updated.", cartService.view(user.getId()));
            }
            case "DELETE" -> {
                cartService.remove(user.getId(), productId);
                JsonUtil.ok(resp, "Item removed from cart.", cartService.view(user.getId()));
            }
            default -> JsonUtil.error(resp, 405, "Method not allowed.");
        }
    }
}