package com.eventmanagement.model;

public class Vendor {
    private int vendorId;
    private String vendorName, category, contactPhone;
    private double quotedCost;

    public int getVendorId() { return vendorId; }
    public void setVendorId(int vendorId) { this.vendorId = vendorId; }
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public double getQuotedCost() { return quotedCost; }
    public void setQuotedCost(double quotedCost) { this.quotedCost = quotedCost; }
}