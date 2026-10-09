package com.janumart.dao;

import com.janumart.model.User;
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

/** JDBC access for users. PreparedStatements only. */
public class UserDao {

    private final DataSource ds;

    public UserDao() {
        this.ds = DBUtil.getDataSource();
    }

    private static final String COLS =
            "id, name, email, password_hash, role, created_at";

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            u.setCreatedAt(ts.toLocalDateTime());
        }
        return u;
    }

    public User findByEmail(String email) {
        String sql = "SELECT " + COLS + " FROM users WHERE email = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while finding user by email", e);
        }
    }

    public User findById(int id) {
        String sql = "SELECT " + COLS + " FROM users WHERE id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while finding user by id", e);
        }
    }

    public int insert(User u) {
        String sql = "INSERT INTO users (name, email, password_hash, role, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRole());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    u.setId(keys.getInt(1));
                }
            }
            return u.getId();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("uq_users_email")) {
                throw new com.janumart.exception.ValidationException("An account with this email already exists.");
            }
            throw new RuntimeException("DB error while inserting user", e);
        }
    }

    public void updateName(int id, String name) {
        String sql = "UPDATE users SET name = ? WHERE id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error while updating user name", e);
        }
    }

    public void updatePassword(int id, String hash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error while updating password", e);
        }
    }

    public List<User> list(String q, String role) {
        StringBuilder sql = new StringBuilder("SELECT " + COLS + " FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (role != null && !role.isBlank()) {
            sql.append(" AND role = ?");
            params.add(role);
        }
        if (q != null && !q.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?))");
            params.add("%" + q.trim() + "%");
            params.add("%" + q.trim() + "%");
        }
        sql.append(" ORDER BY created_at DESC, id DESC");
        List<User> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while listing users", e);
        }
        return out;
    }

    public long countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error while counting users", e);
        }
    }

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("DB error while counting users", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new com.janumart.exception.NotFoundException("User not found.");
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().contains("referential")
                    || e.getMessage().contains("FK") || e.getMessage().contains("constraint"))) {
                throw new com.janumart.exception.ValidationException(
                        "This user cannot be deleted because they have linked orders or other data.");
            }
            throw new RuntimeException("DB error while deleting user", e);
        }
    }
}