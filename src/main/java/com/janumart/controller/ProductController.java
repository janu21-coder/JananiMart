package com.janumart.controller;

import com.janumart.dto.ProductQuery;
import com.janumart.dto.ProductRequest;
import com.janumart.model.User;
import com.janumart.service.ReviewService;
import com.janumart.service.ProductService;
import com.janumart.util.JsonUtil;
import com.janumart.util.SessionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/** GET/POST/PUT/DELETE /api/v1/products/* and GET /api/v1/meta/filters */
public class ProductController {

    private final ProductService productService = new ProductService();
    private final ReviewService reviewService = new ReviewService();

    public void handle(HttpServletRequest req, HttpServletResponse resp, String method, List<String> seg)
            throws IOException {
        if (seg.isEmpty()) {
            if (method.equals("GET")) {
                JsonUtil.ok(resp, "", productService.list(query(req)));
            } else if (method.equals("POST")) {
                User actor = SessionUtil.requireRole(req, "SELLER", "ADMIN");
                ProductRequest pr = JsonUtil.parse(req, ProductRequest.class);
                JsonUtil.created(resp, "Product listed successfully.", productService.create(actor, pr));
            } else {
                JsonUtil.error(resp, 405, "Method not allowed.");
            }
            return;
        }

        int id;
        try {
            id = Integer.parseInt(seg.get(0));
        } catch (NumberFormatException e) {
            JsonUtil.error(resp, 400, "Invalid product id.");
            return;
        }
        if (seg.size() == 1) {
            switch (method) {
                case "GET" -> JsonUtil.ok(resp, "", productService.get(id));
                case "PUT" -> {
                    User actor = SessionUtil.requireRole(req, "SELLER", "ADMIN");
                    ProductRequest pr = JsonUtil.parse(req, ProductRequest.class);
                    JsonUtil.ok(resp, "Product updated successfully.",
                            productService.update(actor, id, pr));
                }
                case "DELETE" -> {
                    User actor = SessionUtil.requireRole(req, "SELLER", "ADMIN");
                    productService.delete(actor, id);
                    JsonUtil.ok(resp, "Product deleted successfully.", null);
                }
                default -> JsonUtil.error(resp, 405, "Method not allowed.");
            }
            return;
        }

        if (seg.size() == 2 && seg.get(1).equals("reviews") && method.equals("GET")) {
            JsonUtil.ok(resp, "", reviewService.forProduct(id));
            return;
        }
        JsonUtil.error(resp, 404, "Product endpoint not found.");
    }

    /** GET /api/v1/meta/filters -> distinct brands/colors/sizes/subcategories. */
    public void meta(HttpServletRequest req, HttpServletResponse resp, String method, List<String> seg)
            throws IOException {
        if (!method.equals("GET")) {
            JsonUtil.error(resp, 404, "Meta endpoint not found.");
            return;
        }
        JsonUtil.ok(resp, "", productService.filterMeta());
    }

    private ProductQuery query(HttpServletRequest req) {
        ProductQuery q = new ProductQuery();
        q.setQ(blank(req.getParameter("q")));
        q.setCategory(blank(req.getParameter("category")));
        q.setSubcategory(blank(req.getParameter("subcategory")));
        q.setGender(blank(req.getParameter("gender")));
        q.setBrand(blank(req.getParameter("brand")));
        q.setColor(blank(req.getParameter("color")));
        q.setSize(blank(req.getParameter("size")));
        q.setSortOrder(blank(req.getParameter("sort")));
        q.setMinPrice(decimal(req.getParameter("minPrice")));
        q.setMaxPrice(decimal(req.getParameter("maxPrice")));
        Double rating = doubleOf(req.getParameter("rating"));
        q.setMinRating(rating);
        String inStock = req.getParameter("inStock");
        if (inStock != null) {
            q.setInStockOnly(inStock.equalsIgnoreCase("true") || inStock.equals("1"));
        }
        String sellerParam = req.getParameter("sellerId");
        if (sellerParam != null && !sellerParam.isBlank()) {
            try {
                q.setSellerId(Integer.parseInt(sellerParam));
            } catch (NumberFormatException ignored) {
                // ignore malformed seller filter
            }
        }
        q.setPage(intOf(req.getParameter("page"), 1));
        q.setPageSize(intOf(req.getParameter("pageSize"), 12));
        return q;
    }

    private static String blank(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    private static int intOf(String v, int def) {
        try {
            return v == null ? def : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static BigDecimal decimal(String v) {
        try {
            return v == null || v.isBlank() ? null : new BigDecimal(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double doubleOf(String v) {
        try {
            return v == null || v.isBlank() ? null : Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}