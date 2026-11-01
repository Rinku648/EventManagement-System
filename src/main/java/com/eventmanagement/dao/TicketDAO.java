package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Ticket;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");

    public boolean createTicket(Ticket t) {
        String sql = "INSERT INTO tickets (booking_id, qr_payload, pdf_path, is_checked_in) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getBookingId());
            ps.setString(2, t.getQrPayload());
            ps.setString(3, t.getPdfPath());
            ps.setBoolean(4, t.isCheckedIn());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Returns the ticket already issued for this booking, if any — used to prevent duplicate issuing. */
    public Ticket findByBookingId(int bookingId) {
        String sql = "SELECT * FROM tickets WHERE booking_id = ? ORDER BY ticket_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Ticket findByPayload(String payload) {
        String sql = "SELECT * FROM tickets WHERE qr_payload = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, payload);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean markCheckedIn(int ticketId) {
        String sql = "UPDATE tickets SET is_checked_in = TRUE, checked_in_at = NOW() WHERE ticket_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Reverts a check-in — the safe equivalent of "deleting" one, without breaking the ticket's link to its booking. */
    public boolean resetCheckIn(int ticketId) {
        String sql = "UPDATE tickets SET is_checked_in = FALSE, checked_in_at = NULL WHERE ticket_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Every ticket ever issued, most recently checked-in first, then most recently created. */
    public List<Ticket> getAllTickets() {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM tickets ORDER BY checked_in_at IS NULL, checked_in_at DESC, ticket_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setTicketId(rs.getInt("ticket_id"));
        t.setBookingId(rs.getInt("booking_id"));
        t.setQrPayload(rs.getString("qr_payload"));
        t.setPdfPath(rs.getString("pdf_path"));
        t.setCheckedIn(rs.getBoolean("is_checked_in"));
        Timestamp ts = rs.getTimestamp("checked_in_at");
        t.setCheckedInAt(ts != null ? ts.toLocalDateTime().format(DISPLAY_FMT) : null);
        return t;
    }
}