package com.janumart.dao;

import com.janumart.model.Review;
import com.janumart.util.DBUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** JDBC access for product reviews. */
public class ReviewDao {

    private final DataSource ds;

    public ReviewDao() {
        this.ds = DBUtil.getDataSource();
    }

    private Review map(ResultSet rs, boolean withName) throws SQLException {
        Review r = new Review();
        r.setId(rs.getInt("id"));
        r.setProductId(rs.getInt("product_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            r.setCreatedAt(ts.toLocalDateTime());
        }
        if (withName) {
            r.setUserName(rs.getString("user_name"));
        }
        return r;
    }

    public List<Review> findByProduct(int productId) {
        String sql = "SELECT r.id, r.product_id, r.user_id, r.rating, r.comment, r.created_at, u.name AS user_name " +
                "FROM reviews r JOIN users u ON u.id = r.user_id " +
                "WHERE r.product_id = ? ORDER BY r.created_at DESC, r.id DESC";
        List<Review> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs, true));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while listing reviews", e);
        }
        return out;
    }

    public boolean exists(int userId, int productId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND product_id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while checking review", e);
        }
    }

    /** A buyer may review a product only when a DELIVERED order contained it. */
    public boolean hasDeliveredPurchase(int userId, int productId) {
        String sql = "SELECT COUNT(*) FROM orders o JOIN order_items oi ON oi.order_id = o.id " +
                "WHERE o.buyer_id = ? AND oi.product_id = ? AND o.status = 'DELIVERED'";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while checking purchase eligibility", e);
        }
    }

    public void insert(Review review) {
        String sql = "INSERT INTO reviews (product_id, user_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, review.getProductId());
            ps.setInt(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    review.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while inserting review", e);
        }
    }
}