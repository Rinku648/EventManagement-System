package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Hall;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HallDAO {
    public boolean createHall(Hall hall) {
        String sql = "INSERT INTO halls (venue_id, hall_number, hall_name, capacity, cost) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hall.getVenueId());
            ps.setString(2, hall.getHallNumber());
            ps.setString(3, hall.getHallName());
            ps.setInt(4, hall.getCapacity());
            ps.setDouble(5, hall.getCost());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateHall(Hall hall) {
        String sql = "UPDATE halls SET hall_number = ?, hall_name = ?, capacity = ?, cost = ? WHERE hall_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hall.getHallNumber());
            ps.setString(2, hall.getHallName());
            ps.setInt(3, hall.getCapacity());
            ps.setDouble(4, hall.getCost());
            ps.setInt(5, hall.getHallId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteHall(int hallId) {
        String sql = "DELETE FROM halls WHERE hall_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Hall> getHallsByVenue(int venueId) {
        List<Hall> halls = new ArrayList<>();
        String sql = "SELECT * FROM halls WHERE venue_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, venueId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) halls.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return halls;
    }

    /** All halls across every venue — used to populate a hall-name picker. */
    public List<Hall> getAllHalls() {
        List<Hall> halls = new ArrayList<>();
        String sql = "SELECT * FROM halls ORDER BY venue_id, hall_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) halls.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return halls;
    }

    public Hall getById(int hallId) {
        String sql = "SELECT * FROM halls WHERE hall_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean isHallAvailable(int hallId, java.time.LocalDate date) {
        String sql = "SELECT COUNT(*) FROM events WHERE hall_id = ? AND start_date = ? AND status != 'CANCELLED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) == 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Hall mapRow(ResultSet rs) throws SQLException {
        Hall h = new Hall();
        h.setHallId(rs.getInt("hall_id"));
        h.setVenueId(rs.getInt("venue_id"));
        h.setHallNumber(rs.getString("hall_number"));
        h.setHallName(rs.getString("hall_name"));
        h.setCapacity(rs.getInt("capacity"));
        h.setCost(rs.getDouble("cost"));
        return h;
    }
}