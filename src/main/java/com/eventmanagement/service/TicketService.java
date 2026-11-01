package com.eventmanagement.service;

import com.eventmanagement.dao.EventDAO;
import com.eventmanagement.dao.HallDAO;
import com.eventmanagement.dao.TicketDAO;
import com.eventmanagement.dao.TicketTypeDAO;
import com.eventmanagement.dao.VenueDAO;
import com.eventmanagement.model.Booking;
import com.eventmanagement.model.Event;
import com.eventmanagement.model.Hall;
import com.eventmanagement.model.Ticket;
import com.eventmanagement.model.TicketType;
import com.eventmanagement.model.Venue;
import com.eventmanagement.util.PdfGenerator;
import com.eventmanagement.util.QRCodeGenerator;

import java.util.List;

public class TicketService {
    private final TicketDAO ticketDAO = new TicketDAO();
    private final BookingService bookingService = new BookingService();
    private final EventDAO eventDAO = new EventDAO();
    private final TicketTypeDAO ticketTypeDAO = new TicketTypeDAO();
    private final VenueDAO venueDAO = new VenueDAO();
    private final HallDAO hallDAO = new HallDAO();

    /**
     * Issues a ticket for a booking, pulling the real attendee name, event name,
     * ticket type name, and venue/hall (shown as separate rows) from the database.
     * Idempotent: if a ticket already exists for this booking, it is reused instead
     * of creating a duplicate row.
     */
    public String issueTicket(int bookingId) throws Exception {
        Ticket existing = ticketDAO.findByBookingId(bookingId);
        if (existing != null) {
            return existing.getPdfPath();
        }

        Booking booking = bookingService.getById(bookingId);
        if (booking == null) throw new IllegalArgumentException("Booking not found: " + bookingId);

        String attendeeName = booking.getAttendeeName() != null ? booking.getAttendeeName() : "Guest";

        Event event = eventDAO.getById(booking.getEventId());
        String eventName = event != null ? event.getEventName() : "Event #" + booking.getEventId();

        TicketType ticketType = ticketTypeDAO.getById(booking.getTicketTypeId());
        String typeName = ticketType != null ? ticketType.getTypeName() : "General";

        String venueName = null;
        String hallName = null;
        if (event != null) {
            if (event.getVenueId() != null) {
                Venue venue = venueDAO.getById(event.getVenueId());
                venueName = venue != null ? venue.getVenueName() : null;
            }
            if (event.getHallId() != null) {
                Hall hall = hallDAO.getById(event.getHallId());
                hallName = hall != null ? hall.getHallName() : null;
            }
        }
        if (venueName == null) venueName = "Venue TBA";

        String payload = "BOOKING-" + bookingId;
        String fileName = "ticket_" + bookingId;
        String qrImagePath = QRCodeGenerator.generateQrImage(payload, fileName);
        String pdfPath = PdfGenerator.generateTicketPdf(fileName, bookingId, eventName, attendeeName,
                typeName, venueName, hallName, qrImagePath);

        Ticket ticket = new Ticket();
        ticket.setBookingId(bookingId);
        ticket.setQrPayload(payload);
        ticket.setPdfPath(pdfPath);
        ticket.setCheckedIn(false);
        ticketDAO.createTicket(ticket);
        return pdfPath;
    }

    /**
     * Legacy overload kept so any other existing caller with hardcoded values
     * still compiles — but prefer issueTicket(bookingId) which uses real data.
     * Also idempotent, for the same reason as above.
     */
    public String issueTicket(int bookingId, String eventName, String attendeeName, String ticketType) throws Exception {
        Ticket existing = ticketDAO.findByBookingId(bookingId);
        if (existing != null) {
            return existing.getPdfPath();
        }

        String payload = "BOOKING-" + bookingId;
        String fileName = "ticket_" + bookingId;
        String qrImagePath = QRCodeGenerator.generateQrImage(payload, fileName);
        String pdfPath = PdfGenerator.generateTicketPdf(fileName, bookingId, eventName, attendeeName,
                ticketType, "Venue TBA", null, qrImagePath);

        Ticket ticket = new Ticket();
        ticket.setBookingId(bookingId);
        ticket.setQrPayload(payload);
        ticket.setPdfPath(pdfPath);
        ticket.setCheckedIn(false);
        ticketDAO.createTicket(ticket);
        return pdfPath;
    }

    /** Blocks duplicate check-ins — checks is_checked_in FIRST, exactly as required by the report. */
    public String checkIn(String payload) {
        Ticket ticket = ticketDAO.findByPayload(payload);
        if (ticket == null) return "Ticket not found.";
        if (ticket.isCheckedIn()) return "Already checked in — duplicate entry blocked.";
        ticketDAO.markCheckedIn(ticket.getTicketId());
        return "Check-in successful.";
    }

    /** Looks up a single ticket by its QR payload — used by the check-in panel. */
    public Ticket findByPayload(String payload) {
        return ticketDAO.findByPayload(payload);
    }

    /** Returns every ticket — used to populate the check-in panel list/table. */
    public List<Ticket> getAllTickets() {
        return ticketDAO.getAllTickets();
    }

    /** Reverts a checked-in ticket back to not-checked-in — used by the check-in panel's "undo" action. */
    public boolean undoCheckIn(int ticketId) {
        return ticketDAO.resetCheckIn(ticketId);
    }
}