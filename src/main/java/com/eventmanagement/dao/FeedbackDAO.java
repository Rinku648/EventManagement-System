package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Feedback;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {
    public boolean submitFeedback(Feedback f) {
        String sql = "INSERT INTO feedback (event_id, attendee_id, rating, comments) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, f.getEventId());
            ps.setInt(2, f.getAttendeeId());
            ps.setInt(3, f.getRating());
            ps.setString(4, f.getComments());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Feedback> getByEvent(int eventId) {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT f.*, a.full_name AS attendee_name, e.event_name " +
                "FROM feedback f " +
                "LEFT JOIN attendees a ON f.attendee_id = a.attendee_id " +
                "LEFT JOIN events e ON f.event_id = e.event_id " +
                "WHERE f.event_id = ? ORDER BY f.feedback_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** All feedback across every event, with attendee/event names joined in — used by the dashboard table. */
    public List<Feedback> getAllFeedback() {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT f.*, a.full_name AS attendee_name, e.event_name " +
                "FROM feedback f " +
                "LEFT JOIN attendees a ON f.attendee_id = a.attendee_id " +
                "LEFT JOIN events e ON f.event_id = e.event_id " +
                "ORDER BY f.feedback_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public double getAverageRating(int eventId) {
        String sql = "SELECT AVG(rating) AS avg_rating FROM feedback WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /** Count of feedback rows for an event — used to distinguish "0 reviews" from "average is genuinely 0". */
    public int getFeedbackCount(int eventId) {
        String sql = "SELECT COUNT(*) FROM feedback WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Feedback mapRow(ResultSet rs) throws SQLException {
        Feedback f = new Feedback();
        f.setFeedbackId(rs.getInt("feedback_id"));
        f.setEventId(rs.getInt("event_id"));
        f.setAttendeeId(rs.getInt("attendee_id"));
        f.setRating(rs.getInt("rating"));
        f.setComments(rs.getString("comments"));
        f.setAttendeeName(rs.getString("attendee_name"));
        f.setEventName(rs.getString("event_name"));
        return f;
    }
}