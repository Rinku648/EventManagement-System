package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Certificate;
import java.sql.*;

public class CertificateDAO {
    public boolean createCertificate(Certificate c) {
        String sql = "INSERT INTO certificates (attendee_id, event_id, certificate_number, pdf_path) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getAttendeeId());
            ps.setInt(2, c.getEventId());
            ps.setString(3, c.getCertificateNumber());
            ps.setString(4, c.getPdfPath());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
