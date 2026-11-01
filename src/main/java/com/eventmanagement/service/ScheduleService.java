package com.eventmanagement.service;

import com.eventmanagement.dao.SessionDAO;
import com.eventmanagement.model.Session;
import java.util.List;
import java.util.TreeMap;

public class ScheduleService {
    private final SessionDAO sessionDAO = new SessionDAO();

    public boolean createSession(Session s) {
        if (hasOverlap(s, -1)) return false;
        return sessionDAO.createSession(s);
    }

    /** Updates a session, re-checking for overlaps against every OTHER session on the same event. */
    public boolean updateSession(Session s) {
        if (hasOverlap(s, s.getSessionId())) return false;
        return sessionDAO.updateSession(s);
    }

    private boolean hasOverlap(Session s, int excludeSessionId) {
        for (Session existing : sessionDAO.getSessionsByEvent(s.getScheduleId())) {
            if (existing.getSessionId() == excludeSessionId) continue;
            boolean overlaps = s.getStartTime().isBefore(existing.getEndTime()) &&
                    existing.getStartTime().isBefore(s.getEndTime());
            if (overlaps) return true;
        }
        return false;
    }

    public boolean deleteSession(int sessionId) {
        return sessionDAO.deleteSession(sessionId);
    }

    public Session getById(int sessionId) {
        return sessionDAO.getById(sessionId);
    }

    public TreeMap<java.time.LocalDateTime, Session> getSortedAgenda(int eventId) {
        TreeMap<java.time.LocalDateTime, Session> agenda = new TreeMap<>();
        List<Session> sessions = sessionDAO.getSessionsByEvent(eventId);
        for (Session s : sessions) agenda.put(s.getStartTime(), s);
        return agenda;
    }
}