package com.eventmanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.User;

public class UserDAO {
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean createUser(User user) {
        String sql = "INSERT INTO users (full_name, email, password_hash, phone, role) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(getOptionalColumnValue(rs, "password", "password_hash"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        u.setStaffDuty("");
        return u;
    }

    private String getOptionalColumnValue(ResultSet rs, String... columnNames) throws SQLException {
        for (String columnName : columnNames) {
            try {
                return rs.getString(columnName);
            } catch (SQLException ignored) {
                // Continue to next possible column name if this one is missing.
            }
        }
        return null;
    }
}
