package com.janumart.controller;

import com.google.gson.JsonParseException;
import com.janumart.exception.AppException;
import com.janumart.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Front controller for all /api/v1/* endpoints. Routes to fluent controllers,
 * centralizes JSON errors, and never leaks stack traces to the client.
 */
@WebServlet("/api/v1/*")
public class ApiServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ApiServlet.class);

    private final AuthController authController = new AuthController();
    private final ProductController productController = new ProductController();
    private final CartController cartController = new CartController();
    private final OrderController orderController = new OrderController();
    private final ReviewController reviewController = new ReviewController();
    private final AdminController adminController = new AdminController();
    private final SellerController sellerController = new SellerController();
    private final ChatbotController chatbotController = new ChatbotController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        dispatch(req, resp, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        dispatch(req, resp, "POST");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        dispatch(req, resp, "PUT");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        dispatch(req, resp, "DELETE");
    }

    private void dispatch(HttpServletRequest req, HttpServletResponse resp, String method) throws IOException {
        List<String> seg = segments(req.getPathInfo());
        try {
            if (seg.isEmpty()) {
                JsonUtil.error(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
                return;
            }
            String root = seg.get(0);
            List<String> rest = seg.subList(1, seg.size());
            switch (root) {
                case "auth" -> authController.handle(req, resp, method, rest);
                case "products" -> productController.handle(req, resp, method, rest);
                case "cart" -> cartController.handle(req, resp, method, rest);
                case "orders" -> orderController.handle(req, resp, method, rest);
                case "reviews" -> reviewController.handle(req, resp, method, rest);
                case "admin" -> adminController.handle(req, resp, method, rest);
                case "seller" -> sellerController.handle(req, resp, method, rest);
                case "chatbot" -> chatbotController.handle(req, resp, method, rest);
                case "meta" -> productController.meta(req, resp, method, rest);
                default -> JsonUtil.error(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (AppException e) {
            log.debug("Request failed ({} {}): {}", method, req.getPathInfo(), e.getMessage());
            JsonUtil.error(resp, e.getStatus(), e.getMessage());
        } catch (JsonParseException e) {
            JsonUtil.error(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
        } catch (Exception e) {
            log.error("Unexpected error on {} {}", method, req.getPathInfo(), e);
            JsonUtil.error(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong");
        }
    }

    private static List<String> segments(String pathInfo) {
        List<String> seg = new ArrayList<>();
        if (pathInfo == null) {
            return seg;
        }
        for (String part : pathInfo.split("/")) {
            if (!part.isBlank()) {
                seg.add(part);
            }
        }
        return seg;
    }
}