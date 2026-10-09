package com.janumart.filter;

import com.janumart.exception.AppException;
import com.janumart.exception.ForbiddenException;
import com.janumart.exception.UnauthorizedException;
import com.janumart.util.JsonUtil;
import com.janumart.util.SessionUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Enforces role-based access before controllers run:
 * SELLER only -> /seller/* and /api/v1/seller/*,
 * ADMIN only  -> /admin/* and /api/v1/admin/*,
 * BUYER only  -> cart/checkout/orders pages and cart APIs,
 * product writes require SELLER or ADMIN.
 */
public class RoleAuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        String method = req.getMethod();

        try {
            if (prefix(path, "/seller") || prefix(path, "/api/v1/seller")) {
                SessionUtil.requireRole(req, "SELLER");
            } else if (prefix(path, "/admin") || prefix(path, "/api/v1/admin")) {
                SessionUtil.requireRole(req, "ADMIN");
            } else if (prefix(path, "/cart") || prefix(path, "/checkout")
                    || prefix(path, "/orders") || path.startsWith("/order")
                    || prefix(path, "/api/v1/cart") || prefix(path, "/api/v1/reviews")
                    || path.equals("/api/v1/orders") && method.equals("POST")) {
                SessionUtil.requireRole(req, "BUYER");
            } else if (path.startsWith("/api/v1/products") && !method.equals("GET")) {
                SessionUtil.requireRole(req, "SELLER", "ADMIN");
            }
            chain.doFilter(request, response);
        } catch (UnauthorizedException | ForbiddenException e) {
            handleDenied(req, resp, e);
        }
    }

    private boolean prefix(String path, String p) {
        return path.equals(p) || path.startsWith(p + "/");
    }

    private void handleDenied(HttpServletRequest req, HttpServletResponse resp, AppException e)
            throws IOException, ServletException {
        if (req.getRequestURI().contains("/api/")) {
            JsonUtil.error(resp, e.getStatus(), e.getMessage());
        } else {
            req.setAttribute("errorStatus", e.getStatus());
            req.setAttribute("errorMessage", e.getMessage());
            resp.setStatus(e.getStatus());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp")
                    .forward(req, resp);
        }
    }
}