package com.eventmanagement.model;

public class Booking {
    private int bookingId, attendeeId, eventId, ticketTypeId;
    private Integer seatId;
    private String status;
    private String attendeeName;

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public int getAttendeeId() { return attendeeId; }
    public void setAttendeeId(int attendeeId) { this.attendeeId = attendeeId; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public int getTicketTypeId() { return ticketTypeId; }
    public void setTicketTypeId(int ticketTypeId) { this.ticketTypeId = ticketTypeId; }
    public Integer getSeatId() { return seatId; }
    public void setSeatId(Integer seatId) { this.seatId = seatId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAttendeeName() { return attendeeName; }
    public void setAttendeeName(String attendeeName) { this.attendeeName = attendeeName; }
}