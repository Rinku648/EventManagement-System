package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Vendor;
import com.eventmanagement.model.EventVendor;
import com.eventmanagement.model.Event;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VendorDAO {
    public boolean createVendor(Vendor v) {
        String sql = "INSERT INTO vendors (vendor_name, category, contact_phone, quoted_cost) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getVendorName());
            ps.setString(2, v.getCategory());
            ps.setString(3, v.getContactPhone());
            ps.setDouble(4, v.getQuotedCost());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateVendor(Vendor v) {
        String sql = "UPDATE vendors SET vendor_name = ?, category = ?, contact_phone = ?, quoted_cost = ? WHERE vendor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getVendorName());
            ps.setString(2, v.getCategory());
            ps.setString(3, v.getContactPhone());
            ps.setDouble(4, v.getQuotedCost());
            ps.setInt(5, v.getVendorId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteVendor(int vendorId) {
        String sql = "DELETE FROM vendors WHERE vendor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vendorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Vendor> getAllVendors() {
        List<Vendor> list = new ArrayList<>();
        String sql = "SELECT * FROM vendors";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Vendor v = new Vendor();
                v.setVendorId(rs.getInt("vendor_id"));
                v.setVendorName(rs.getString("vendor_name"));
                v.setCategory(rs.getString("category"));
                v.setContactPhone(rs.getString("contact_phone"));
                v.setQuotedCost(rs.getDouble("quoted_cost"));
                list.add(v);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean assignVendorToEvent(EventVendor ev) {
        String sql = "INSERT INTO event_vendors (event_id, vendor_id, payment_status) VALUES (?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ev.getEventId());
            ps.setInt(2, ev.getVendorId());
            ps.setString(3, ev.getPaymentStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Removes a specific vendor-event assignment link. */
    public boolean unassignVendorFromEvent(int eventId, int vendorId) {
        String sql = "DELETE FROM event_vendors WHERE event_id = ? AND vendor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, vendorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Every event this vendor is currently assigned to (full Event objects, for the manage-assignment dialog). */
    public List<Event> getEventsForVendor(int vendorId) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT e.* FROM events e " +
                "JOIN event_vendors ev ON e.event_id = ev.event_id " +
                "WHERE ev.vendor_id = ? ORDER BY e.start_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vendorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapEventRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Event mapEventRow(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setEventId(rs.getInt("event_id"));
        e.setEventName(rs.getString("event_name"));
        return e;
    }
}