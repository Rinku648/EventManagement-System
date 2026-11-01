package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import java.sql.*;

public class ReportDAO {
    public int getTotalBookings(int eventId) {
        return runCount("SELECT COUNT(*) FROM bookings WHERE event_id = ?", eventId);
    }
    public double getTotalRevenue(int eventId) {
        String sql = "SELECT COALESCE(SUM(p.amount),0) FROM payments p " +
                "JOIN bookings b ON p.booking_id = b.booking_id " +
                "WHERE b.event_id = ? AND p.status = 'SUCCESS'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
    public int getVendorCount(int eventId) {
        return runCount("SELECT COUNT(*) FROM event_vendors WHERE event_id = ?", eventId);
    }
    private int runCount(String sql, int eventId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}