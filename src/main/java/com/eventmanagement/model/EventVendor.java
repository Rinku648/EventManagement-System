package com.eventmanagement.model;

public class EventVendor {
    private int eventVendorId, eventId, vendorId;
    private String paymentStatus;

    public int getEventVendorId() { return eventVendorId; }
    public void setEventVendorId(int eventVendorId) { this.eventVendorId = eventVendorId; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public int getVendorId() { return vendorId; }
    public void setVendorId(int vendorId) { this.vendorId = vendorId; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
