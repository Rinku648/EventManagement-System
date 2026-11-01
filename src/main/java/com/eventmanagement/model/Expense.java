package com.eventmanagement.model;

public class Expense {
    private int expenseId, eventId;
    private Integer expenseCategoryId;
    private double amount;
    private String description;

    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public Integer getExpenseCategoryId() { return expenseCategoryId; }
    public void setExpenseCategoryId(Integer expenseCategoryId) { this.expenseCategoryId = expenseCategoryId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
