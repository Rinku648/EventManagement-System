package com.eventmanagement.service;

import com.eventmanagement.dao.EventDAO;
import com.eventmanagement.model.Event;
import java.util.List;

public class EventService {
    private final EventDAO eventDAO = new EventDAO();
    public boolean createEvent(Event e) { return eventDAO.createEvent(e); }
    public boolean updateEvent(Event e) { return eventDAO.updateEvent(e); }
    public boolean deleteEvent(int id) { return eventDAO.deleteEvent(id); }
    public List<Event> getAllEvents() { return eventDAO.getAllEvents(); }
    public Event getById(int id) { return eventDAO.getById(id); }
    public boolean updateStatus(int id, String status) { return eventDAO.updateStatus(id, status); }
}