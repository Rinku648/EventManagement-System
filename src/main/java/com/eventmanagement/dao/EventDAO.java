package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Event;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    public boolean createEvent(Event event) {
        String sql = "INSERT INTO events (event_name, organizer_id, description, start_date, start_time, end_date, " +
                "day_number, function_type, status, venue_id, hall_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getEventName());
            ps.setInt(2, event.getOrganizerId());
            ps.setString(3, event.getDescription());
            ps.setDate(4, Date.valueOf(event.getStartDate()));
            setTimeParam(ps, 5, event.getStartTime());
            ps.setDate(6, event.getEndDate() != null ? Date.valueOf(event.getEndDate()) : null);
            ps.setInt(7, event.getDayNumber());
            ps.setString(8, event.getFunctionType());
            ps.setString(9, event.getStatus());
            if (event.getVenueId() != null) ps.setInt(10, event.getVenueId()); else ps.setNull(10, Types.INTEGER);
            if (event.getHallId() != null) ps.setInt(11, event.getHallId()); else ps.setNull(11, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateEvent(Event event) {
        String sql = "UPDATE events SET event_name = ?, start_date = ?, start_time = ?, function_type = ? WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getEventName());
            ps.setDate(2, Date.valueOf(event.getStartDate()));
            setTimeParam(ps, 3, event.getStartTime());
            ps.setString(4, event.getFunctionType());
            ps.setInt(5, event.getEventId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Sets (or clears) the venue and hall assigned to an event. */
    public boolean assignVenue(int eventId, Integer venueId, Integer hallId) {
        String sql = "UPDATE events SET venue_id = ?, hall_id = ? WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (venueId != null) ps.setInt(1, venueId); else ps.setNull(1, Types.INTEGER);
            if (hallId != null) ps.setInt(2, hallId); else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Clears venue_id AND hall_id for an event — full unassignment. */
    public boolean unassignVenue(int eventId) {
        String sql = "UPDATE events SET venue_id = NULL, hall_id = NULL WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Clears only hall_id for an event, keeping the venue assigned. */
    public boolean unassignHall(int eventId) {
        String sql = "UPDATE events SET hall_id = NULL WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Event> getEventsByVenue(int venueId) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE venue_id = ? ORDER BY start_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, venueId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Event> getEventsByHall(int hallId) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE hall_id = ? ORDER BY start_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean deleteEvent(int eventId) {
        String sql = "DELETE FROM events WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY start_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) events.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return events;
    }

    public Event getById(int eventId) {
        String sql = "SELECT * FROM events WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean updateStatus(int eventId, String status) {
        String sql = "UPDATE events SET status = ? WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private void setTimeParam(PreparedStatement ps, int index, String hhmm) throws SQLException {
        if (hhmm == null || hhmm.isBlank()) {
            ps.setNull(index, Types.TIME);
            return;
        }
        try {
            LocalTime lt = LocalTime.parse(hhmm);
            ps.setTime(index, Time.valueOf(lt));
        } catch (Exception ex) {
            ps.setNull(index, Types.TIME);
        }
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setEventId(rs.getInt("event_id"));
        e.setEventName(rs.getString("event_name"));
        e.setOrganizerId(rs.getInt("organizer_id"));
        e.setDescription(rs.getString("description"));
        e.setStartDate(rs.getDate("start_date").toLocalDate());

        Time t = rs.getTime("start_time");
        e.setStartTime(t != null ? t.toLocalTime().toString().substring(0, 5) : null);

        Date end = rs.getDate("end_date");
        e.setEndDate(end != null ? end.toLocalDate() : null);
        e.setDayNumber(rs.getInt("day_number"));
        e.setFunctionType(rs.getString("function_type"));
        e.setStatus(rs.getString("status"));
        int venueId = rs.getInt("venue_id");
        e.setVenueId(rs.wasNull() ? null : venueId);
        int hallId = rs.getInt("hall_id");
        e.setHallId(rs.wasNull() ? null : hallId);
        return e;
    }
}