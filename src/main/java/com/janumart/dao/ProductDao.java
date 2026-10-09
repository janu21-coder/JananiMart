package com.janumart.dao;

import com.janumart.dto.ProductQuery;
import com.janumart.model.Product;
import com.janumart.util.DBUtil;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC access for products with search/filter/sort/pagination.
 * All SQL is built from whitelisted fragments and bound params.
 */
public class ProductDao {

    private final DataSource ds;

    public ProductDao() {
        this.ds = DBUtil.getDataSource();
    }

    private static final String BASE_SELECT =
            "SELECT p.id, p.seller_id, p.name, p.description, p.price, p.stock_qty, " +
            "p.category, p.subcategory, p.gender, p.brand, p.color, p.size, p.material, " +
            "p.image_url, p.created_at, p.updated_at, u.name AS seller_name, " +
            "r.avg_rating, r.rating_count " +
            "FROM products p " +
            "JOIN users u ON u.id = p.seller_id " +
            "LEFT JOIN (SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS rating_count " +
            "FROM reviews GROUP BY product_id) r ON r.product_id = p.id ";

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setSellerId(rs.getInt("seller_id"));
        p.setSellerName(rs.getString("seller_name"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setCategory(rs.getString("category"));
        p.setSubcategory(rs.getString("subcategory"));
        p.setGender(rs.getString("gender"));
        p.setBrand(rs.getString("brand"));
        p.setColor(rs.getString("color"));
        p.setSize(rs.getString("size"));
        p.setMaterial(rs.getString("material"));
        p.setImageUrl(rs.getString("image_url"));
        Timestamp ca = rs.getTimestamp("created_at");
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ca != null) {
            p.setCreatedAt(ca.toLocalDateTime());
        }
        if (ua != null) {
            p.setUpdatedAt(ua.toLocalDateTime());
        }
        Double avg = rs.getObject("avg_rating") != null ? rs.getDouble("avg_rating") : null;
        p.setAvgRating(avg);
        p.setRatingCount(rs.getLong("rating_count"));
        return p;
    }

    public Product findById(int id) {
        String sql = BASE_SELECT + " WHERE p.id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while finding product", e);
        }
    }

    /** Builds WHERE (params list) from the query. */
    private String where(ProductQuery q, List<Object> params) {
        StringBuilder w = new StringBuilder(" WHERE 1=1");
        if (q.getQ() != null && !q.getQ().isBlank()) {
            String like = "%" + q.getQ().trim() + "%";
            w.append(" AND (LOWER(p.name) LIKE LOWER(?) OR COALESCE(p.description,'') LIKE LOWER(?)")
              .append(" OR LOWER(p.category) LIKE LOWER(?) OR COALESCE(p.subcategory,'') LIKE LOWER(?)")
              .append(" OR COALESCE(p.brand,'') LIKE LOWER(?) OR COALESCE(p.color,'') LIKE LOWER(?)")
              .append(" OR LOWER(u.name) LIKE LOWER(?))");
            for (int i = 0; i < 7; i++) {
                params.add(like);
            }
        }
        if (notBlank(q.getCategory())) {
            w.append(" AND p.category = ?");
            params.add(q.getCategory());
        }
        if (notBlank(q.getSubcategory())) {
            w.append(" AND p.subcategory = ?");
            params.add(q.getSubcategory());
        }
        if (notBlank(q.getGender())) {
            w.append(" AND p.gender = ?");
            params.add(q.getGender());
        }
        if (notBlank(q.getBrand())) {
            w.append(" AND p.brand = ?");
            params.add(q.getBrand());
        }
        if (notBlank(q.getColor())) {
            w.append(" AND p.color = ?");
            params.add(q.getColor());
        }
        if (notBlank(q.getSize())) {
            w.append(" AND p.size = ?");
            params.add(q.getSize());
        }
        if (q.getSellerId() != null) {
            w.append(" AND p.seller_id = ?");
            params.add(q.getSellerId());
        }
        if (q.getMinPrice() != null) {
            w.append(" AND p.price >= ?");
            params.add(q.getMinPrice());
        }
        if (q.getMaxPrice() != null) {
            w.append(" AND p.price <= ?");
            params.add(q.getMaxPrice());
        }
        if (q.getMinRating() != null) {
            w.append(" AND (r.avg_rating IS NOT NULL AND r.avg_rating >= ?)");
            params.add(q.getMinRating());
        }
        if (Boolean.TRUE.equals(q.getInStockOnly())) {
            w.append(" AND p.stock_qty > 0");
        }
        return w.toString();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String orderBy(String sortOrder) {
        return switch (sortOrder == null ? "" : sortOrder) {
            case "price_asc" -> " ORDER BY p.price ASC, p.id DESC";
            case "price_desc" -> " ORDER BY p.price DESC, p.id DESC";
            case "rating" -> " ORDER BY r.avg_rating DESC NULLS LAST, r.rating_count DESC, p.id DESC";
            case "name_asc" -> " ORDER BY p.name ASC, p.id DESC";
            default -> " ORDER BY p.created_at DESC, p.id DESC";
        };
    }

    public List<Product> search(ProductQuery q) {
        List<Object> params = new ArrayList<>();
        String sql = BASE_SELECT + where(q, params) + orderBy(q.getSortOrder()) + " LIMIT ? OFFSET ?";
        List<Product> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Object p : params) {
                ps.setObject(i++, p);
            }
            ps.setInt(i++, q.getPageSize());
            ps.setInt(i, (q.getPage() - 1) * q.getPageSize());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while searching products", e);
        }
        return out;
    }

    public long count(ProductQuery q) {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM products p " +
                "JOIN users u ON u.id = p.seller_id " +
                "LEFT JOIN (SELECT product_id FROM reviews GROUP BY product_id) r ON r.product_id = p.id"
                + where(q, params);
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Object p : params) {
                ps.setObject(i++, p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while counting products", e);
        }
    }

    /** Atomic stock decrement used inside the checkout transaction. Returns rows updated. */
    public int decrementStock(Connection c, int productId, int qty) throws SQLException {
        String sql = "UPDATE products SET stock_qty = stock_qty - ?, updated_at = ? WHERE id = ? AND stock_qty >= ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, productId);
            ps.setInt(4, qty);
            return ps.executeUpdate();
        }
    }

    public int insert(Product p) {
        String sql = "INSERT INTO products (seller_id, name, description, price, stock_qty, category, " +
                "subcategory, gender, brand, color, size, material, image_url, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime now = LocalDateTime.now();
            ps.setInt(1, p.getSellerId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrice());
            ps.setInt(5, p.getStockQty());
            ps.setString(6, p.getCategory());
            ps.setString(7, p.getSubcategory());
            ps.setString(8, p.getGender());
            ps.setString(9, p.getBrand());
            ps.setString(10, p.getColor());
            ps.setString(11, p.getSize());
            ps.setString(12, p.getMaterial());
            ps.setString(13, p.getImageUrl());
            ps.setTimestamp(14, Timestamp.valueOf(now));
            ps.setTimestamp(15, Timestamp.valueOf(now));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
            return p.getId();
        } catch (SQLException e) {
            throw new RuntimeException("DB error while inserting product", e);
        }
    }

    public void update(Product p) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock_qty = ?, " +
                "category = ?, subcategory = ?, gender = ?, brand = ?, color = ?, size = ?, material = ?, " +
                "image_url = ?, updated_at = ? WHERE id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setBigDecimal(3, p.getPrice());
            ps.setInt(4, p.getStockQty());
            ps.setString(5, p.getCategory());
            ps.setString(6, p.getSubcategory());
            ps.setString(7, p.getGender());
            ps.setString(8, p.getBrand());
            ps.setString(9, p.getColor());
            ps.setString(10, p.getSize());
            ps.setString(11, p.getMaterial());
            ps.setString(12, p.getImageUrl());
            ps.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(14, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error while updating product", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new com.janumart.exception.NotFoundException("Product not found.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while deleting product", e);
        }
    }

    public List<Product> bySeller(int sellerId) {
        String sql = BASE_SELECT + " WHERE p.seller_id = ? ORDER BY p.created_at DESC, p.id DESC";
        List<Product> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while listing seller products", e);
        }
        return out;
    }

    public List<Product> lowStock(int sellerId, int threshold) {
        String sql = BASE_SELECT + " WHERE p.seller_id = ? AND p.stock_qty <= ? ORDER BY p.stock_qty ASC";
        List<Product> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            ps.setInt(2, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while listing low stock", e);
        }
        return out;
    }

    private List<Product> simpleQuery(String orderSql, int limit) {
        String sql = BASE_SELECT + orderSql + " LIMIT ?";
        List<Product> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error in product query", e);
        }
        return out;
    }

    public List<Product> trending(int limit) {
        return simpleQuery(" ORDER BY r.rating_count DESC NULLS LAST, r.avg_rating DESC NULLS LAST, p.id DESC ", limit);
    }

    public List<Product> bestSellers(int limit) {
        String sql = BASE_SELECT +
                " LEFT JOIN (SELECT oi.product_id, SUM(oi.quantity) AS sold FROM order_items oi GROUP BY oi.product_id) s ON s.product_id = p.id " +
                " WHERE p.stock_qty > 0 ORDER BY s.sold DESC NULLS LAST, r.avg_rating DESC NULLS LAST, p.id DESC LIMIT ?";
        List<Product> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while listing best sellers", e);
        }
        return out;
    }

    public List<Product> newest(int limit) {
        return simpleQuery(" ORDER BY p.created_at DESC, p.id DESC ", limit);
    }

    public List<Product> byCategory(String category, int limit) {
        ProductQuery q = new ProductQuery();
        q.setCategory(category);
        List<Object> params = new ArrayList<>();
        String sql = BASE_SELECT + where(q, params) + " ORDER BY p.created_at DESC, p.id DESC LIMIT ?";
        List<Product> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Object p : params) {
                ps.setObject(i++, p);
            }
            ps.setInt(i, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while listing category products", e);
        }
        return out;
    }

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("DB error while counting products", e);
        }
    }

    /** Distinct facet values for the shop filters. */
    public List<String> distinct(String column) {
        String sql = "SELECT DISTINCT " + column + " FROM products WHERE " + column + " IS NOT NULL ORDER BY 1";
        List<String> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while reading filter options", e);
        }
        return out;
    }

    public List<Product> recentByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append(i > 0 ? ",?" : "?");
        }
        String sql = BASE_SELECT + " WHERE p.id IN (" + placeholders + ")";
        List<Product> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (Integer id : ids) {
                ps.setInt(i++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while loading recently viewed", e);
        }
        return out;
    }
}