package com.eventmanagement.model;

public class Ticket {
    private int ticketId, bookingId;
    private String qrPayload, pdfPath, checkedInAt;
    private boolean checkedIn;

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public String getQrPayload() { return qrPayload; }
    public void setQrPayload(String qrPayload) { this.qrPayload = qrPayload; }
    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }
    public String getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(String checkedInAt) { this.checkedInAt = checkedInAt; }
}