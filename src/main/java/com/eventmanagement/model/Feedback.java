package com.eventmanagement.model;

public class Feedback {
    private int feedbackId, eventId, attendeeId, rating;
    private String comments;

    // Display-only fields, populated via join — not stored on the feedback table itself.
    private String attendeeName;
    private String eventName;

    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public int getAttendeeId() { return attendeeId; }
    public void setAttendeeId(int attendeeId) { this.attendeeId = attendeeId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getAttendeeName() { return attendeeName; }
    public void setAttendeeName(String attendeeName) { this.attendeeName = attendeeName; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
}