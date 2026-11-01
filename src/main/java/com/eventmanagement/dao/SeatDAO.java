package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Seat;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class SeatDAO {

    public int countSeatsForHall(int hallId) {
        String sql = "SELECT COUNT(*) FROM seats WHERE hall_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private void insertSeat(Connection conn, int hallId, String rowLabel, int seatNumber) throws SQLException {
        String sql = "INSERT INTO seats (hall_id, seat_row, seat_number, zone) VALUES (?,?,?, 'REGULAR')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            ps.setString(2, rowLabel);
            ps.setInt(3, seatNumber);
            ps.executeUpdate();
        }
    }

    /**
     * Ensures the hall has exactly as many seat rows as its real capacity.
     * If fewer exist, generates the missing ones — 20 seats per row, labelled
     * A, B, C ... Z, AA, AB ... Safe to call every time the seat map is loaded.
     */
    public void ensureSeatsForHall(int hallId, int capacity) {
        int existing = countSeatsForHall(hallId);
        if (existing >= capacity) return;

        int rowSize = 20;
        try (Connection conn = DBConnection.getConnection()) {
            for (int i = existing; i < capacity; i++) {
                int rowIndex = i / rowSize;
                int seatNumber = (i % rowSize) + 1;
                String rowLabel = toRowLabel(rowIndex);
                insertSeat(conn, hallId, rowLabel, seatNumber);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Deletes ALL seats and their booking history for a hall, so it can be
     * cleanly regenerated from scratch. Use this if a hall's seat grid looks
     * corrupted or has duplicate labels from earlier manual test data.
     */
    public void resetSeatsForHall(int hallId) {
        String deleteBookingsSql = "DELETE sb FROM seat_bookings sb " +
                "JOIN seats s ON sb.seat_id = s.seat_id WHERE s.hall_id = ?";
        String deleteSeatsSql = "DELETE FROM seats WHERE hall_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(deleteBookingsSql)) {
                ps1.setInt(1, hallId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(deleteSeatsSql)) {
                ps2.setInt(1, hallId);
                ps2.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Converts 0,1,2... to A,B,C...Z,AA,AB... (spreadsheet-style column labels). */
    private String toRowLabel(int index) {
        StringBuilder sb = new StringBuilder();
        int n = index;
        do {
            sb.insert(0, (char) ('A' + (n % 26)));
            n = n / 26 - 1;
        } while (n >= 0);
        return sb.toString();
    }

    /**
     * Loads every seat for a hall with LIVE status, plus the attendee name and
     * booking id if it's currently booked — derived from each seat's most
     * recent seat_bookings row.
     */
    public Map<Integer, Seat> loadSeatMap(int hallId) {
        Map<Integer, Seat> seats = new LinkedHashMap<>();

        String sql = "SELECT s.seat_id, s.seat_row, s.seat_number, s.zone, " +
                "latest.status AS latest_status, latest.booking_id AS latest_booking_id, " +
                "a.full_name AS attendee_name " +
                "FROM seats s " +
                "LEFT JOIN (" +
                "  SELECT sb1.seat_id, sb1.status, sb1.booking_id " +
                "  FROM seat_bookings sb1 " +
                "  INNER JOIN (SELECT seat_id, MAX(seat_booking_id) AS max_id FROM seat_bookings GROUP BY seat_id) sb2 " +
                "    ON sb1.seat_id = sb2.seat_id AND sb1.seat_booking_id = sb2.max_id" +
                ") latest ON s.seat_id = latest.seat_id " +
                "LEFT JOIN bookings b ON latest.booking_id = b.booking_id " +
                "LEFT JOIN attendees a ON b.attendee_id = a.attendee_id " +
                "WHERE s.hall_id = ? " +
                "ORDER BY s.seat_row, s.seat_number";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat();
                    seat.setSeatId(rs.getInt("seat_id"));
                    seat.setHallId(hallId);
                    seat.setSeatRow(rs.getString("seat_row"));
                    seat.setSeatNumber(rs.getInt("seat_number"));
                    seat.setZone(rs.getString("zone"));

                    String latestStatus = rs.getString("latest_status");
                    boolean booked = "BOOKED".equals(latestStatus);
                    seat.setStatus(booked ? "BOOKED" : "AVAILABLE");

                    if (booked) {
                        int bookingId = rs.getInt("latest_booking_id");
                        seat.setBookingId(rs.wasNull() ? null : bookingId);
                        seat.setAttendeeName(rs.getString("attendee_name"));
                    }

                    seats.put(seat.getSeatId(), seat);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return seats;
    }

    public boolean bookSeat(int seatId, int bookingId) {
        String sql = "INSERT INTO seat_bookings (seat_id, booking_id, status) VALUES (?, ?, 'BOOKED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean releaseSeat(int seatId, int bookingId) {
        String sql = "INSERT INTO seat_bookings (seat_id, booking_id, status) VALUES (?, ?, 'RELEASED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Finds the booking_id tied to a seat's most recent BOOKED entry, or -1 if none. */
    public int findLatestBookingId(int seatId) {
        String sql = "SELECT booking_id FROM seat_bookings WHERE seat_id = ? AND status = 'BOOKED' " +
                "ORDER BY seat_booking_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public boolean updateZone(int seatId, String zone) {
        String sql = "UPDATE seats SET zone = ? WHERE seat_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, zone);
            ps.setInt(2, seatId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}