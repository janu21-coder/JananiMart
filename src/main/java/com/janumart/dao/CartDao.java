package com.janumart.dao;

import com.janumart.model.CartItem;
import com.janumart.util.DBUtil;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** JDBC access for the cart ({@code cart_items} joined with products). */
public class CartDao {

    private final DataSource ds;

    public CartDao() {
        this.ds = DBUtil.getDataSource();
    }

    private static final String SELECT =
            "SELECT ci.id, ci.user_id, ci.product_id, ci.quantity, " +
            "p.name AS product_name, p.price, p.image_url, p.stock_qty, u.name AS seller_name " +
            "FROM cart_items ci " +
            "JOIN products p ON p.id = ci.product_id " +
            "JOIN users u ON u.id = p.seller_id ";

    private CartItem map(ResultSet rs) throws SQLException {
        CartItem ci = new CartItem();
        ci.setId(rs.getInt("id"));
        ci.setUserId(rs.getInt("user_id"));
        ci.setProductId(rs.getInt("product_id"));
        ci.setQuantity(rs.getInt("quantity"));
        ci.setProductName(rs.getString("product_name"));
        BigDecimal price = rs.getBigDecimal("price");
        ci.setPrice(price);
        ci.setImageUrl(rs.getString("image_url"));
        ci.setStock(rs.getInt("stock_qty"));
        ci.setSellerName(rs.getString("seller_name"));
        return ci;
    }

    public List<CartItem> findByUser(int userId) {
        String sql = SELECT + " WHERE ci.user_id = ? ORDER BY ci.id DESC";
        List<CartItem> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while reading cart", e);
        }
        return out;
    }

    public CartItem findItem(int userId, int productId) {
        String sql = SELECT + " WHERE ci.user_id = ? AND ci.product_id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while reading cart item", e);
        }
    }

    public void insert(int userId, int productId, int quantity) {
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error while adding cart item", e);
        }
    }

    public void updateQuantity(int id, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error while updating cart item", e);
        }
    }

    public void deleteItem(int userId, int productId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error while removing cart item", e);
        }
    }

    public void clear(int userId) {
        try (Connection c = ds.getConnection()) {
            clear(c, userId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error while clearing cart", e);
        }
    }

    /** Transaction-scoped variant. */
    public void clear(Connection c, int userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public long countItems(int userId) {
        String sql = "SELECT COUNT(*) FROM cart_items WHERE user_id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while counting cart items", e);
        }
    }
}