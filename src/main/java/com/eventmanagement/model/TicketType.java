package com.eventmanagement.model;

public class TicketType {
    private int ticketTypeId, eventId, totalQuantity;
    private String typeName;
    private double price;

    public int getTicketTypeId() { return ticketTypeId; }
    public void setTicketTypeId(int ticketTypeId) { this.ticketTypeId = ticketTypeId; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
}

