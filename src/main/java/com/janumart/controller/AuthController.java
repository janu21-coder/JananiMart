package com.janumart.controller;

import com.janumart.dto.LoginRequest;
import com.janumart.dto.RegisterRequest;
import com.janumart.model.User;
import com.janumart.service.AuthService;
import com.janumart.util.JsonUtil;
import com.janumart.util.SessionUtil;
import com.janumart.util.Validate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/** POST /api/v1/auth/* and PUT /api/v1/profile* */
public class AuthController {

    private final AuthService authService = new AuthService();

    public void handle(HttpServletRequest req, HttpServletResponse resp, String method, List<String> seg)
            throws IOException {
        String action = seg.isEmpty() ? "" : seg.get(0);
        switch (action) {
            case "register" -> {
                if (!method.equals("POST")) {
                    JsonUtil.error(resp, 405, "Method not allowed.");
                    return;
                }
                RegisterRequest rr = JsonUtil.parse(req, RegisterRequest.class);
                User user = authService.register(rr);
                SessionUtil.login(req, user);
                JsonUtil.created(resp, "Registration successful. Welcome to JanuMart!", user.safeMap());
            }
            case "login" -> {
                if (!method.equals("POST")) {
                    JsonUtil.error(resp, 405, "Method not allowed.");
                    return;
                }
                LoginRequest lr = JsonUtil.parse(req, LoginRequest.class);
                User user = authService.login(lr);
                SessionUtil.login(req, user);
                JsonUtil.ok(resp, "Welcome back, " + user.getName() + "!", user.safeMap());
            }
            case "logout" -> {
                SessionUtil.logout(req);
                JsonUtil.ok(resp, "You have been logged out.", null);
            }
            case "me" -> {
                if (!method.equals("GET")) {
                    JsonUtil.error(resp, 405, "Method not allowed.");
                    return;
                }
                User user = SessionUtil.getUser(req);
                JsonUtil.ok(resp, "", user == null ? null : user.safeMap());
            }
            case "profile" -> profile(req, resp, method, seg.size() > 1 ? seg.get(1) : "");
            default -> JsonUtil.error(resp, 404, "Auth endpoint not found.");
        }
    }

    private void profile(HttpServletRequest req, HttpServletResponse resp, String method, String sub)
            throws IOException {
        User user = SessionUtil.requireUser(req);
        if (!method.equals("PUT")) {
            JsonUtil.error(resp, 405, "Method not allowed.");
            return;
        }
        if (sub.equals("password")) {
            Map<String, Object> body = JsonUtil.body(req);
            String current = JsonUtil.str(body, "currentPassword");
            String next = JsonUtil.str(body, "newPassword");
            String confirm = JsonUtil.str(body, "confirmPassword");
            authService.changePassword(user.getId(), current, next, confirm);
            JsonUtil.ok(resp, "Password updated successfully.", null);
            return;
        }
        if (!sub.isEmpty()) {
            JsonUtil.error(resp, 404, "Profile endpoint not found.");
            return;
        }
        Map<String, Object> body = JsonUtil.body(req);
        String name = JsonUtil.str(body, "name");
        Validate.required(name, "Name");
        authService.updateProfile(user.getId(), name);
        User fresh = new User();
        fresh.setId(user.getId());
        fresh.setName(name.trim());
        fresh.setEmail(user.getEmail());
        fresh.setRole(user.getRole());
        fresh.setCreatedAt(user.getCreatedAt());
        SessionUtil.login(req, fresh);
        JsonUtil.ok(resp, "Profile updated successfully.", fresh.safeMap());
    }
}