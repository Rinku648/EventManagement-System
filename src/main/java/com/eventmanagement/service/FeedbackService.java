package com.eventmanagement.service;

import com.eventmanagement.dao.FeedbackDAO;
import com.eventmanagement.model.Feedback;
import java.util.List;

public class FeedbackService {
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    public boolean submitFeedback(Feedback f) { return feedbackDAO.submitFeedback(f); }
    public List<Feedback> getByEvent(int eventId) { return feedbackDAO.getByEvent(eventId); }
    public List<Feedback> getAllFeedback() { return feedbackDAO.getAllFeedback(); }
    public double getAverageRating(int eventId) { return feedbackDAO.getAverageRating(eventId); }
    public int getFeedbackCount(int eventId) { return feedbackDAO.getFeedbackCount(eventId); }
}