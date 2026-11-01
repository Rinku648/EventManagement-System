package com.eventmanagement.model;

import java.time.LocalDate;

public class Event {
    private int eventId, organizerId, dayNumber;
    private String eventName, description, functionType, status, startTime;
    private LocalDate startDate, endDate;
    private Integer venueId, hallId;

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public int getOrganizerId() { return organizerId; }
    public void setOrganizerId(int organizerId) { this.organizerId = organizerId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }
    public String getFunctionType() { return functionType; }
    public void setFunctionType(String functionType) { this.functionType = functionType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getVenueId() { return venueId; }
    public void setVenueId(Integer venueId) { this.venueId = venueId; }
    public Integer getHallId() { return hallId; }
    public void setHallId(Integer hallId) { this.hallId = hallId; }
}