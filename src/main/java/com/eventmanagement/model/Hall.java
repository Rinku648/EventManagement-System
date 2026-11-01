package com.eventmanagement.model;

public class Hall {
    private int hallId, venueId, capacity;
    private String hallNumber, hallName;
    private double cost;

    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }
    public int getVenueId() { return venueId; }
    public void setVenueId(int venueId) { this.venueId = venueId; }
    public String getHallNumber() { return hallNumber; }
    public void setHallNumber(String hallNumber) { this.hallNumber = hallNumber; }
    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
}