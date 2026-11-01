package com.eventmanagement.model;

public class Budget {
    private int budgetId, eventId;
    private double plannedAmount;

    public int getBudgetId() { return budgetId; }
    public void setBudgetId(int budgetId) { this.budgetId = budgetId; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public double getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(double plannedAmount) { this.plannedAmount = plannedAmount; }
}
