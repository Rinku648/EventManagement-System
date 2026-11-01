package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.TicketType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketTypeDAO {
    public boolean createTicketType(TicketType t) {
        String sql = "INSERT INTO ticket_types (event_id, type_name, price, total_quantity) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getEventId());
            ps.setString(2, t.getTypeName());
            ps.setDouble(3, t.getPrice());
            ps.setInt(4, t.getTotalQuantity());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<TicketType> getByEvent(int eventId) {
        List<TicketType> list = new ArrayList<>();
        String sql = "SELECT * FROM ticket_types WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Fetches a single ticket type by its ID — used when issuing a ticket to get the real type name. */
    public TicketType getById(int ticketTypeId) {
        String sql = "SELECT * FROM ticket_types WHERE ticket_type_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int getBookedCount(int ticketTypeId) {
        return 0;
    }

    private TicketType mapRow(ResultSet rs) throws SQLException {
        TicketType t = new TicketType();
        t.setTicketTypeId(rs.getInt("ticket_type_id"));
        t.setEventId(rs.getInt("event_id"));
        t.setTypeName(rs.getString("type_name"));
        t.setPrice(rs.getDouble("price"));
        t.setTotalQuantity(rs.getInt("total_quantity"));
        return t;
    }
}