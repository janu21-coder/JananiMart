package com.janumart.filter;

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
 * Blocks unauthenticated access to protected pages and API endpoints.
 * HTML requests are redirected to the login page; API requests get JSON 401.
 */
public class AuthFilter implements Filter {

    private static final String[] API_PREFIXES = {
            "/api/v1/cart", "/api/v1/orders", "/api/v1/admin", "/api/v1/seller",
            "/api/v1/reviews", "/api/v1/profile"
    };

    private static final String[] PAGE_PREFIXES = {
            "/cart", "/checkout", "/orders", "/order", "/order-success",
            "/profile", "/dashboard", "/seller", "/admin"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (isProtected(path) && !SessionUtil.isLoggedIn(req)) {
            if (path.startsWith("/api/")) {
                JsonUtil.error(resp, HttpServletResponse.SC_UNAUTHORIZED,
                        "Please log in to continue.");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isProtected(String path) {
        for (String p : PAGE_PREFIXES) {
            if (path.equals(p) || path.startsWith(p + "/")) {
                return true;
            }
        }
        for (String p : API_PREFIXES) {
            if (path.equals(p) || path.startsWith(p + "/")) {
                return true;
            }
        }
        return false;
    }
}