package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Venue;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VenueDAO {
    public boolean createVenue(Venue venue) {
        String sql = "INSERT INTO venues (venue_name, address, city) VALUES (?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, venue.getVenueName());
            ps.setString(2, venue.getAddress());
            ps.setString(3, venue.getCity());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateVenue(Venue venue) {
        String sql = "UPDATE venues SET venue_name = ?, city = ?, address = ? WHERE venue_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, venue.getVenueName());
            ps.setString(2, venue.getCity());
            ps.setString(3, venue.getAddress());
            ps.setInt(4, venue.getVenueId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteVenue(int venueId) {
        String sql = "DELETE FROM venues WHERE venue_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, venueId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Venue> getAllVenues() {
        List<Venue> venues = new ArrayList<>();
        String sql = "SELECT * FROM venues";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Venue v = new Venue();
                v.setVenueId(rs.getInt("venue_id"));
                v.setVenueName(rs.getString("venue_name"));
                v.setAddress(rs.getString("address"));
                v.setCity(rs.getString("city"));
                venues.add(v);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return venues;
    }

    /** Fetches a single venue by ID — used when issuing a ticket to show the real venue name. */
    public Venue getById(int venueId) {
        String sql = "SELECT * FROM venues WHERE venue_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, venueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Venue v = new Venue();
                    v.setVenueId(rs.getInt("venue_id"));
                    v.setVenueName(rs.getString("venue_name"));
                    v.setAddress(rs.getString("address"));
                    v.setCity(rs.getString("city"));
                    return v;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}