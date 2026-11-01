package com.eventmanagement.dao;

import com.eventmanagement.db.DBConnection;
import com.eventmanagement.model.ExpenseCategory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseCategoryDAO {
    public List<ExpenseCategory> getAllCategories() {
        List<ExpenseCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM expense_categories ORDER BY category_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ExpenseCategory c = new ExpenseCategory();
                c.setExpenseCategoryId(rs.getInt("expense_category_id"));
                c.setCategoryName(rs.getString("category_name"));
                list.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}