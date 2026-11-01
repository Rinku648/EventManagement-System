package com.eventmanagement.service;

import com.eventmanagement.dao.ReportDAO;

public class ReportService {
    private final ReportDAO reportDAO = new ReportDAO();

    public String buildSummary(int eventId) {
        int bookings = reportDAO.getTotalBookings(eventId);
        double revenue = reportDAO.getTotalRevenue(eventId);
        int vendors = reportDAO.getVendorCount(eventId);
        return String.format("Bookings: %d | Revenue: %.2f | Vendors: %d", bookings, revenue, vendors);
    }
}
