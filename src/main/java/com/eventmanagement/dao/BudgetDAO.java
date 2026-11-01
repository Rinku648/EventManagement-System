package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.Budget;
import com.eventmanagement.model.Expense;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {

    public boolean setBudget(Budget b) {
        String sql = "INSERT INTO budgets (event_id, planned_amount) VALUES (?,?) " +
                "ON DUPLICATE KEY UPDATE planned_amount = VALUES(planned_amount)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, b.getEventId());
            ps.setDouble(2, b.getPlannedAmount());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateBudget(Budget b) {
        String sql = "UPDATE budgets SET planned_amount = ? WHERE budget_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, b.getPlannedAmount());
            ps.setInt(2, b.getBudgetId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteBudget(int budgetId) {
        String sql = "DELETE FROM budgets WHERE budget_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, budgetId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Budget getBudgetByEvent(int eventId) {
        String sql = "SELECT * FROM budgets WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Budget b = new Budget();
                    b.setBudgetId(rs.getInt("budget_id"));
                    b.setEventId(rs.getInt("event_id"));
                    b.setPlannedAmount(rs.getDouble("planned_amount"));
                    return b;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public double getPlannedAmount(int eventId) {
        Budget b = getBudgetByEvent(eventId);
        return b != null ? b.getPlannedAmount() : 0;
    }

    public boolean logExpense(Expense e) {
        String sql = "INSERT INTO expenses (event_id, expense_category_id, amount, description) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, e.getEventId());
            if (e.getExpenseCategoryId() != null) ps.setInt(2, e.getExpenseCategoryId()); else ps.setNull(2, Types.INTEGER);
            ps.setDouble(3, e.getAmount());
            ps.setString(4, e.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public boolean updateExpense(Expense e) {
        String sql = "UPDATE expenses SET expense_category_id = ?, amount = ?, description = ? WHERE expense_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (e.getExpenseCategoryId() != null) ps.setInt(1, e.getExpenseCategoryId()); else ps.setNull(1, Types.INTEGER);
            ps.setDouble(2, e.getAmount());
            ps.setString(3, e.getDescription());
            ps.setInt(4, e.getExpenseId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public boolean deleteExpense(int expenseId) {
        String sql = "DELETE FROM expenses WHERE expense_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Expense> getExpensesByEvent(int eventId) {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE event_id = ? ORDER BY expense_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Expense e = new Expense();
                    e.setExpenseId(rs.getInt("expense_id"));
                    e.setEventId(rs.getInt("event_id"));
                    int catId = rs.getInt("expense_category_id");
                    e.setExpenseCategoryId(rs.wasNull() ? null : catId);
                    e.setAmount(rs.getDouble("amount"));
                    e.setDescription(rs.getString("description"));
                    list.add(e);
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    public double getTotalExpenses(int eventId) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM expenses WHERE event_id = ?";
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