package com.janumart.controller;

import com.janumart.exception.AppException;
import com.janumart.exception.ForbiddenException;
import com.janumart.exception.NotFoundException;
import com.janumart.model.Product;
import com.janumart.model.User;
import com.janumart.service.AdminService;
import com.janumart.service.DashboardService;
import com.janumart.service.OrderService;
import com.janumart.service.ProductService;
import com.janumart.util.SessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Routes servlet URLs to JSP views under /WEB-INF/views and prepares the
 * server-side data each page needs (layered MVC).
 */
@WebServlet(urlPatterns = {
        "/login", "/register", "/shop", "/product", "/cart", "/checkout",
        "/orders", "/order", "/order-success", "/dashboard", "/profile",
        "/seller/*", "/admin/*"
})
public class ViewController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ViewController.class);

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final DashboardService dashboardService = new DashboardService();
    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        User user = SessionUtil.getUser(req);
        String path = req.getRequestURI().substring(req.getContextPath().length());
        try {
            switch (path) {
                case "/login" -> {
                    if (user != null) {
                        resp.sendRedirect(req.getContextPath() + "/dashboard");
                        return;
                    }
                    view(req, resp, "login");
                }
                case "/register" -> {
                    if (user != null) {
                        resp.sendRedirect(req.getContextPath() + "/dashboard");
                        return;
                    }
                    view(req, resp, "register");
                }
                case "/shop" -> view(req, resp, "shop");
                case "/product" -> productPage(req, resp);
                case "/cart" -> view(req, resp, "cart");
                case "/checkout" -> view(req, resp, "checkout");
                case "/orders" -> {
                    req.setAttribute("orders", orderService.forBuyer(user.getId()));
                    view(req, resp, "orders");
                }
                case "/order" -> orderPage(req, resp, user);
                case "/order-success" -> orderSuccessPage(req, resp, user);
                case "/profile" -> view(req, resp, "profile");
                case "/dashboard" -> dashboard(req, resp, user);
                default -> {
                    if (path.startsWith("/seller")) {
                        sellerPage(req, resp, user, path);
                    } else if (path.startsWith("/admin")) {
                        adminPage(req, resp, user, path);
                    } else {
                        throw new NotFoundException("Page not found.");
                    }
                }
            }
        } catch (AppException e) {
            forwardError(req, resp, e.getStatus(), e.getMessage());
        } catch (Exception e) {
            log.error("Error rendering page {}", path, e);
            forwardError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong");
        }
    }

    private void productPage(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (Exception e) {
            throw new NotFoundException("Product not found.");
        }
        Product product = productService.get(id);

        HttpSession session = req.getSession();
        @SuppressWarnings("unchecked")
        List<Integer> recent = (List<Integer>) session.getAttribute("recentlyViewed");
        if (recent == null) {
            recent = new ArrayList<>();
        }
        recent.remove(Integer.valueOf(product.getId()));
        recent.add(0, product.getId());
        if (recent.size() > 6) {
            recent = new ArrayList<>(recent.subList(0, 6));
        }
        session.setAttribute("recentlyViewed", recent);

        req.setAttribute("product", product);
        view(req, resp, "product");
    }

    private void orderPage(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (Exception e) {
            throw new NotFoundException("Order not found.");
        }
        req.setAttribute("order", orderService.forBuyerDetail(user.getId(), id));
        view(req, resp, "order-detail");
    }

    private void orderSuccessPage(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (Exception e) {
            throw new NotFoundException("Order not found.");
        }
        req.setAttribute("order", orderService.forBuyerDetail(user.getId(), id));
        view(req, resp, "order-success");
    }

    private void dashboard(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        switch (user.getRole()) {
            case "BUYER" -> {
                req.setAttribute("stats", dashboardService.buyer(user.getId()));
                List<Integer> recent = ((List<Integer>) req.getSession().getAttribute("recentlyViewed"));
                req.setAttribute("recentlyViewed",
                        recent == null ? List.of() : productService.recentByIds(new ArrayList<>(recent)));
                view(req, resp, "dashboard-buyer");
            }
            case "SELLER" -> {
                req.setAttribute("stats", dashboardService.seller(user.getId()));
                view(req, resp, "dashboard-seller");
            }
            case "ADMIN" -> {
                req.setAttribute("stats", dashboardService.admin());
                view(req, resp, "dashboard-admin");
            }
            default -> forwardError(req, resp, HttpServletResponse.SC_FORBIDDEN, "Unknown role.");
        }
    }

    private void sellerPage(HttpServletRequest req, HttpServletResponse resp, User user, String path)
            throws IOException, ServletException {
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String rest = path.substring("/seller".length());
        switch (rest) {
            case "", "/", "/dashboard" -> {
                req.setAttribute("stats", dashboardService.seller(user.getId()));
                view(req, resp, "dashboard-seller");
            }
            case "/products" -> {
                req.setAttribute("products", productService.bySeller(user.getId()));
                view(req, resp, "seller-products");
            }
            case "/product-form" -> {
                String id = req.getParameter("id");
                if (id != null && !id.isBlank()) {
                    Product p = productService.get(Integer.parseInt(id));
                    if (p.getSellerId() != user.getId() && !user.is("ADMIN")) {
                        throw new ForbiddenException("You can only edit your own products.");
                    }
                    req.setAttribute("product", p);
                }
                view(req, resp, "seller-product-form");
            }
            case "/orders" -> {
                req.setAttribute("orders", orderService.forSeller(user.getId()));
                view(req, resp, "seller-orders");
            }
            default -> throw new NotFoundException("Seller page not found.");
        }
    }

    private void adminPage(HttpServletRequest req, HttpServletResponse resp, User user, String path)
            throws IOException, ServletException {
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String rest = path.substring("/admin".length());
        switch (rest) {
            case "", "/", "/dashboard" -> {
                req.setAttribute("stats", dashboardService.admin());
                view(req, resp, "dashboard-admin");
            }
            case "/users" -> {
                req.setAttribute("users", adminService.listUsers(
                        req.getParameter("role"), req.getParameter("q")));
                view(req, resp, "admin-users");
            }
            case "/products" -> {
                req.setAttribute("productData", adminService.listProducts(
                        req.getParameter("q"), intParam(req, "page", 1), 50));
                view(req, resp, "admin-products");
            }
            case "/orders" -> {
                req.setAttribute("orders", adminService.listOrders(req.getParameter("status")));
                view(req, resp, "admin-orders");
            }
            default -> throw new NotFoundException("Admin page not found.");
        }
    }

    private static int intParam(HttpServletRequest req, String name, int def) {
        try {
            String v = req.getParameter(name);
            return v == null ? def : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void view(HttpServletRequest req, HttpServletResponse resp, String name)
            throws IOException, ServletException {
        req.getRequestDispatcher("/WEB-INF/views/" + name + ".jsp").forward(req, resp);
    }

    private void forwardError(HttpServletRequest req, HttpServletResponse resp, int status, String message)
            throws IOException, ServletException {
        req.setAttribute("errorStatus", status);
        req.setAttribute("errorMessage", message);
        resp.setStatus(status);
        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }
}