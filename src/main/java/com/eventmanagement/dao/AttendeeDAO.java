package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Attendee;
import java.sql.*;

public class AttendeeDAO {
    public int createAttendee(Attendee a) {

        String sql = "INSERT INTO attendees (full_name, email, phone, gender) VALUES (?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getFullName());
            ps.setString(2, a.getEmail());
            ps.setString(3, a.getPhone());

            // Default gender
            ps.setString(4, "OTHER");

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();

            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
    public Attendee getById(int attendeeId) {
        String sql = "SELECT * FROM attendees WHERE attendee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attendeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Attendee a = new Attendee();
                    a.setAttendeeId(rs.getInt("attendee_id"));
                    a.setFullName(rs.getString("full_name"));
                    a.setEmail(rs.getString("email"));
                    a.setPhone(rs.getString("phone"));
//                    a.setGuestOf(rs.getString("guest_of"));
//                    a.setRsvp(rs.getBoolean("is_rsvp"));
                    return a;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /** Updates just the attendee's name (used when editing a booking). */
    public boolean updateFullName(int attendeeId, String fullName) {
        String sql = "UPDATE attendees SET full_name = ? WHERE attendee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setInt(2, attendeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}