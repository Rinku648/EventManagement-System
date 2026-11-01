package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    public int createBooking(Booking b) {
        String sql = "INSERT INTO bookings (attendee_id, event_id, ticket_type_id, seat_id, total_amount, status) VALUES (?,?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, b.getAttendeeId());
            ps.setInt(2, b.getEventId());
            ps.setInt(3, b.getTicketTypeId());
            if (b.getSeatId() != null) ps.setInt(4, b.getSeatId()); else ps.setNull(4, Types.INTEGER);
            ps.setDouble(5, 0.0);
            ps.setString(6, b.getStatus() != null ? b.getStatus() : "PENDING");

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, a.full_name AS attendee_name " +
                "FROM bookings b LEFT JOIN attendees a ON b.attendee_id = a.attendee_id " +
                "ORDER BY b.booking_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** All bookings whose event is assigned to the given hall — used to pick an attendee when booking a seat. */
    public List<Booking> getBookingsForHall(int hallId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, a.full_name AS attendee_name " +
                "FROM bookings b " +
                "JOIN events e ON b.event_id = e.event_id " +
                "LEFT JOIN attendees a ON b.attendee_id = a.attendee_id " +
                "WHERE e.hall_id = ? " +
                "ORDER BY b.booking_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Booking getById(int bookingId) {
        String sql = "SELECT b.*, a.full_name AS attendee_name " +
                "FROM bookings b LEFT JOIN attendees a ON b.attendee_id = a.attendee_id " +
                "WHERE b.booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean updateStatus(int bookingId, String status) {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Assigns (or reassigns) which physical seat a booking occupies. */
    public boolean assignSeat(int bookingId, int seatId) {
        String sql = "UPDATE bookings SET seat_id = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateDetails(int bookingId, int eventId, int ticketTypeId) {
        String sql = "UPDATE bookings SET event_id = ?, ticket_type_id = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, ticketTypeId);
            ps.setInt(3, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteBooking(int bookingId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM tickets WHERE booking_id = ?")) {
                ps1.setInt(1, bookingId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM payments WHERE booking_id = ?")) {
                ps2.setInt(1, bookingId);
                ps2.executeUpdate();
            }

            boolean deleted;
            try (PreparedStatement ps3 = conn.prepareStatement("DELETE FROM bookings WHERE booking_id = ?")) {
                ps3.setInt(1, bookingId);
                deleted = ps3.executeUpdate() > 0;
            }

            conn.commit();
            return deleted;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setAttendeeId(rs.getInt("attendee_id"));
        b.setEventId(rs.getInt("event_id"));
        b.setTicketTypeId(rs.getInt("ticket_type_id"));
        int seatId = rs.getInt("seat_id");
        b.setSeatId(rs.wasNull() ? null : seatId);
        b.setStatus(rs.getString("status"));
        b.setAttendeeName(rs.getString("attendee_name"));
        return b;
    }
}