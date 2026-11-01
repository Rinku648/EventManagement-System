package com.eventmanagement.service;

import com.eventmanagement.dao.BudgetDAO;
import com.eventmanagement.dao.EventDAO;
import com.eventmanagement.dao.ExpenseCategoryDAO;
import com.eventmanagement.dao.PaymentDAO;
import com.eventmanagement.model.Budget;
import com.eventmanagement.model.Event;
import com.eventmanagement.model.Expense;
import com.eventmanagement.model.ExpenseCategory;
import java.util.ArrayList;
import java.util.List;

public class BudgetService {
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final ExpenseCategoryDAO categoryDAO = new ExpenseCategoryDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    /** One row of the main Budget dashboard table. */
    public static class BudgetOverviewRow {
        public int eventId;
        public String eventName;
        public int budgetId;
        public double plannedAmount;
        public double ticketRevenue;
        public double totalExpenses;
        public double profitLoss;
    }

    public boolean setBudget(Budget b) { return budgetDAO.setBudget(b); }
    public boolean updateBudget(Budget b) { return budgetDAO.updateBudget(b); }
    public boolean deleteBudget(int budgetId) { return budgetDAO.deleteBudget(budgetId); }
    public Budget getBudgetByEvent(int eventId) { return budgetDAO.getBudgetByEvent(eventId); }

    public boolean logExpense(Expense e) { return budgetDAO.logExpense(e); }
    public boolean updateExpense(Expense e) { return budgetDAO.updateExpense(e); }
    public boolean deleteExpense(int expenseId) { return budgetDAO.deleteExpense(expenseId); }
    public List<Expense> getExpenses(int eventId) { return budgetDAO.getExpensesByEvent(eventId); }

    public List<ExpenseCategory> getAllCategories() { return categoryDAO.getAllCategories(); }

    public String getEventName(int eventId) {
        Event e = eventDAO.getById(eventId);
        return e != null ? e.getEventName() : null;
    }

    /**
     * Real-time profit/loss for any event: confirmed ticket revenue minus total expenses.
     * The planned budget amount is a separate reference figure, not part of this calculation —
     * it tells you what you INTENDED to spend, while this tells you actual financial outcome.
     */
    public double getProfitLoss(int eventId) {
        double revenue = paymentDAO.getTicketRevenueByEvent(eventId);
        double spent = budgetDAO.getTotalExpenses(eventId);
        return revenue - spent;
    }

    public double getTicketRevenue(int eventId) {
        return paymentDAO.getTicketRevenueByEvent(eventId);
    }

    /** One row per event that currently has a planned budget set — the main dashboard data. */
    public List<BudgetOverviewRow> getBudgetOverview() {
        List<BudgetOverviewRow> rows = new ArrayList<>();
        for (Event ev : eventDAO.getAllEvents()) {
            Budget b = budgetDAO.getBudgetByEvent(ev.getEventId());
            if (b == null) continue;

            double revenue = paymentDAO.getTicketRevenueByEvent(ev.getEventId());
            double spent = budgetDAO.getTotalExpenses(ev.getEventId());

            BudgetOverviewRow row = new BudgetOverviewRow();
            row.eventId = ev.getEventId();
            row.eventName = ev.getEventName();
            row.budgetId = b.getBudgetId();
            row.plannedAmount = b.getPlannedAmount();
            row.ticketRevenue = revenue;
            row.totalExpenses = spent;
            row.profitLoss = revenue - spent;
            rows.add(row);
        }
        return rows;
    }
}