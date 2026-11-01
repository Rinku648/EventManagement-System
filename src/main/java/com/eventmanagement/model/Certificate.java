package com.eventmanagement.model;

public class Certificate {
    private int certificateId, attendeeId, eventId;
    private String certificateNumber, pdfPath;

    public int getCertificateId() { return certificateId; }
    public void setCertificateId(int certificateId) { this.certificateId = certificateId; }
    public int getAttendeeId() { return attendeeId; }
    public void setAttendeeId(int attendeeId) { this.attendeeId = attendeeId; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }
    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
}