package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {
    public boolean createSession(Session s) {
        String sql = "INSERT INTO sessions (event_id, session_title, speaker_name, start_time, end_time) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getScheduleId()); // reused field to hold event_id
            ps.setString(2, s.getSessionTitle());
            ps.setString(3, s.getSpeakerName());
            ps.setTimestamp(4, Timestamp.valueOf(s.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(s.getEndTime()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Sessions for one event, with event name and venue name joined in for display. */
    public List<Session> getSessionsByEvent(int eventId) {
        List<Session> list = new ArrayList<>();
        String sql = "SELECT s.*, e.event_name, v.venue_name " +
                "FROM sessions s " +
                "LEFT JOIN events e ON s.event_id = e.event_id " +
                "LEFT JOIN venues v ON e.venue_id = v.venue_id " +
                "WHERE s.event_id = ? ORDER BY s.start_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Session getById(int sessionId) {
        String sql = "SELECT s.*, e.event_name, v.venue_name " +
                "FROM sessions s " +
                "LEFT JOIN events e ON s.event_id = e.event_id " +
                "LEFT JOIN venues v ON e.venue_id = v.venue_id " +
                "WHERE s.session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean updateSession(Session s) {
        String sql = "UPDATE sessions SET session_title = ?, speaker_name = ?, start_time = ?, end_time = ? WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSessionTitle());
            ps.setString(2, s.getSpeakerName());
            ps.setTimestamp(3, Timestamp.valueOf(s.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(s.getEndTime()));
            ps.setInt(5, s.getSessionId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteSession(int sessionId) {
        String sql = "DELETE FROM sessions WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Session mapRow(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setSessionId(rs.getInt("session_id"));
        s.setScheduleId(rs.getInt("event_id")); // reused field
        s.setSessionTitle(rs.getString("session_title"));
        s.setSpeakerName(rs.getString("speaker_name"));
        s.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        s.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        s.setEventName(rs.getString("event_name"));
        s.setVenueName(rs.getString("venue_name"));
        return s;
    }
}