package com.eventmanagement.model;

public class Attendee {
    private int attendeeId;
    private Integer userId;
    private String fullName, email, phone, guestOf;
    private boolean rsvp;

    public int getAttendeeId() { return attendeeId; }
    public void setAttendeeId(int attendeeId) { this.attendeeId = attendeeId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getGuestOf() { return guestOf; }
    public void setGuestOf(String guestOf) { this.guestOf = guestOf; }
    public boolean isRsvp() { return rsvp; }
    public void setRsvp(boolean rsvp) { this.rsvp = rsvp; }
}