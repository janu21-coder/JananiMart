package com.janumart.service;

import com.janumart.dao.ProductDao;
import com.janumart.dto.ProductQuery;
import com.janumart.dto.ProductRequest;
import com.janumart.exception.ForbiddenException;
import com.janumart.exception.NotFoundException;
import com.janumart.exception.ValidationException;
import com.janumart.model.Category;
import com.janumart.model.Product;
import com.janumart.model.User;
import com.janumart.util.Validate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Catalog operations: search, filters, sorting, seller product management. */
public class ProductService {

    private final ProductDao productDao = new ProductDao();

    public Map<String, Object> list(ProductQuery q) {
        int page = Math.max(1, q.getPage());
        int pageSize = Math.min(60, Math.max(1, q.getPageSize()));
        q.setPage(page);
        q.setPageSize(pageSize);

        List<Product> items = productDao.search(q);
        long total = productDao.count(q);
        int pages = (int) Math.max(1, Math.ceil((double) total / pageSize));

        Map<String, Object> data = new HashMap<>();
        data.put("products", items);
        data.put("total", total);
        data.put("page", page);
        data.put("pages", pages);
        data.put("pageSize", pageSize);
        return data;
    }

    public Product get(int id) {
        Validate.positiveId(id, "Product id");
        Product p = productDao.findById(id);
        if (p == null) {
            throw new NotFoundException("Product not found.");
        }
        return p;
    }

    /** Creates a product. Sellers always own what they create; admin may pass a sellerId. */
    public Product create(User actor, ProductRequest req) {
        Product p = new Product();
        int sellerId;
        if (actor.is("ADMIN")) {
            if (req.getSellerId() == null) {
                throw new ValidationException("sellerId is required when an admin lists a product.");
            }
            sellerId = req.getSellerId();
        } else {
            sellerId = actor.getId();
        }
        p.setSellerId(sellerId);
        applyValidated(p, req);
        productDao.insert(p);
        return get(p.getId());
    }

    public Product update(User actor, int productId, ProductRequest req) {
        Product existing = get(productId);
        if (actor.is("SELLER") && existing.getSellerId() != actor.getId()) {
            throw new ForbiddenException("You can only edit your own products.");
        }
        applyValidated(existing, req);
        productDao.update(existing);
        return get(productId);
    }

    public void delete(User actor, int productId) {
        Product existing = get(productId);
        if (actor.is("SELLER") && existing.getSellerId() != actor.getId()) {
            throw new ForbiddenException("You can only delete your own products.");
        }
        productDao.delete(productId);
    }

    private void applyValidated(Product p, ProductRequest req) {
        p.setName(Validate.requireText(req.getName(), "Product name", 200));
        p.setDescription(Validate.option(req.getDescription(), "Description", 2000));
        p.setPrice(requirePrice(req.getPrice()));
        Validate.nonNegativeStock(req.getStockQty() == null ? 0 : req.getStockQty());
        p.setStockQty(req.getStockQty() == null ? 0 : req.getStockQty());

        String category = Validate.requireText(req.getCategory(), "Category", 60);
        if (Category.fromName(category) == null) {
            throw new ValidationException("Please choose a valid category.");
        }
        p.setCategory(Category.fromName(category).getName());
        p.setSubcategory(Validate.option(req.getSubcategory(), "Subcategory", 60));
        p.setGender(validateGender(Validate.option(req.getGender(), "Gender", 20)));
        p.setBrand(Validate.option(req.getBrand(), "Brand", 80));
        p.setColor(Validate.option(req.getColor(), "Color", 40));
        p.setSize(Validate.option(req.getSize(), "Size", 40));
        p.setMaterial(Validate.option(req.getMaterial(), "Material", 80));
        p.setImageUrl(validateImageUrl(req.getImageUrl()));
    }

    private static BigDecimal requirePrice(BigDecimal price) {
        if (price == null) {
            throw new ValidationException("Price is required.");
        }
        Validate.price(price);
        return price;
    }

    private static String validateGender(String gender) {
        if (gender == null) {
            return null;
        }
        if (!gender.equals("Men") && !gender.equals("Women") && !gender.equals("Unisex")) {
            throw new ValidationException("Gender must be Men, Women or Unisex.");
        }
        return gender;
    }

    private static String validateImageUrl(String url) {
        String clean = Validate.option(url, "Image URL", 500);
        if (clean == null) {
            return null;
        }
        String lower = clean.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            throw new ValidationException("Image URL must start with http:// or https://.");
        }
        return clean;
    }

    /* ---------- homepage sections ---------- */

    public List<Product> trending(int limit) {
        return productDao.trending(limit);
    }

    public List<Product> bestSellers(int limit) {
        return productDao.bestSellers(limit);
    }

    public List<Product> newest(int limit) {
        return productDao.newest(limit);
    }

    public List<Product> byCategory(String category, int limit) {
        return productDao.byCategory(category, limit);
    }

    /* ---------- seller tooling ---------- */

    public List<Product> bySeller(int sellerId) {
        return productDao.bySeller(sellerId);
    }

    public List<Product> recentByIds(List<Integer> ids) {
        return productDao.recentByIds(ids);
    }

    public List<Product> lowStock(int sellerId) {
        return productDao.lowStock(sellerId, 5);
    }

    /* ---------- shop filter options ---------- */

    public Map<String, Object> filterMeta() {
        Map<String, Object> m = new HashMap<>();
        m.put("brands", productDao.distinct("brand"));
        m.put("colors", productDao.distinct("color"));
        m.put("sizes", productDao.distinct("size"));
        m.put("subcategories", productDao.distinct("subcategory"));
        return m;
    }
}