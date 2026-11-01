package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    public boolean createPayment(Payment p) {
        String sql = "INSERT INTO payments (booking_id, transaction_id, amount, status) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getBookingId());
            ps.setString(2, p.getTransactionId());
            ps.setDouble(3, p.getAmount());
            ps.setString(4, p.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT b.booking_id AS b_booking_id, p.payment_id, p.transaction_id, p.amount, p.status " +
                "FROM bookings b LEFT JOIN payments p ON b.booking_id = p.booking_id " +
                "ORDER BY b.booking_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Payment p = new Payment();
                p.setBookingId(rs.getInt("b_booking_id"));

                int paymentId = rs.getInt("payment_id");
                p.setPaymentId(rs.wasNull() ? null : paymentId);

                p.setTransactionId(rs.getString("transaction_id"));

                double amount = rs.getDouble("amount");
                p.setAmount(rs.wasNull() ? null : amount);

                String status = rs.getString("status");
                p.setStatus(status != null ? status : "PENDING");

                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updatePayment(int paymentId, String transactionId, double amount, String status) {
        String sql = "UPDATE payments SET transaction_id = ?, amount = ?, status = ? WHERE payment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionId);
            ps.setDouble(2, amount);
            ps.setString(3, status);
            ps.setInt(4, paymentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deletePayment(int paymentId) {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Total confirmed ticket revenue for one event — only payments with status SUCCESS count toward profit/loss. */
    public double getTicketRevenueByEvent(int eventId) {
        String sql = "SELECT COALESCE(SUM(p.amount), 0) FROM payments p " +
                "JOIN bookings b ON p.booking_id = b.booking_id " +
                "WHERE b.event_id = ? AND p.status = 'SUCCESS'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}