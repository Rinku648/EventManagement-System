package com.eventmanagement.model;

public class Seat {
    private int seatId, hallId, seatNumber;
    private String seatRow, zone, status;
    private Integer bookingId;
    private String attendeeName;

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }
    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }
    public String getSeatRow() { return seatRow; }
    public void setSeatRow(String seatRow) { this.seatRow = seatRow; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    public String getAttendeeName() { return attendeeName; }
    public void setAttendeeName(String attendeeName) { this.attendeeName = attendeeName; }

    public String getLabel() { return seatRow + seatNumber; }
}