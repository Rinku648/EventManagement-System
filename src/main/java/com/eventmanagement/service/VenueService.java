package com.eventmanagement.service;

import com.eventmanagement.dao.VenueDAO;
import com.eventmanagement.dao.HallDAO;
import com.eventmanagement.dao.EventDAO;
import com.eventmanagement.model.Venue;
import com.eventmanagement.model.Hall;
import com.eventmanagement.model.Event;
import java.time.LocalDate;
import java.util.List;

public class VenueService {
    private final VenueDAO venueDAO = new VenueDAO();
    private final HallDAO hallDAO = new HallDAO();
    private final EventDAO eventDAO = new EventDAO();

    public boolean createVenue(Venue v) { return venueDAO.createVenue(v); }
    public boolean updateVenue(Venue v) { return venueDAO.updateVenue(v); }
    public boolean deleteVenue(int venueId) { return venueDAO.deleteVenue(venueId); }
    public List<Venue> getAllVenues() { return venueDAO.getAllVenues(); }

    public boolean createHall(Hall h) { return hallDAO.createHall(h); }
    public boolean updateHall(Hall h) { return hallDAO.updateHall(h); }
    public boolean deleteHall(int hallId) { return hallDAO.deleteHall(hallId); }
    public List<Hall> getHallsByVenue(int venueId) { return hallDAO.getHallsByVenue(venueId); }

    public boolean checkHallAvailable(int hallId, LocalDate date) {
        return hallDAO.isHallAvailable(hallId, date);
    }

    public List<Event> getEventsForVenue(int venueId) { return eventDAO.getEventsByVenue(venueId); }
    public List<Event> getEventsForHall(int hallId) { return eventDAO.getEventsByHall(hallId); }

    public List<String> getAssignedEventNamesForVenue(int venueId) {
        return eventDAO.getEventsByVenue(venueId).stream().map(Event::getEventName).toList();
    }

    public List<String> getAssignedEventNamesForHall(int hallId) {
        return eventDAO.getEventsByHall(hallId).stream().map(Event::getEventName).toList();
    }

    public String assignVenueToEvent(int eventId, int venueId, Integer hallId) {
        Event event = eventDAO.getById(eventId);
        if (event == null) return null;
        boolean ok = eventDAO.assignVenue(eventId, venueId, hallId);
        return ok ? event.getEventName() : null;
    }

    public String assignHallToEvent(int eventId, Hall hall) {
        Event event = eventDAO.getById(eventId);
        if (event == null) return null;
        boolean ok = eventDAO.assignVenue(eventId, hall.getVenueId(), hall.getHallId());
        return ok ? event.getEventName() : null;
    }

    public boolean unassignVenueFromEvent(int eventId) { return eventDAO.unassignVenue(eventId); }
    public boolean unassignHallFromEvent(int eventId) { return eventDAO.unassignHall(eventId); }
}