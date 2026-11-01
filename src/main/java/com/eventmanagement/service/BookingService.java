package com.eventmanagement.service;

import com.eventmanagement.dao.BookingDAO;
import com.eventmanagement.dao.TicketTypeDAO;
import com.eventmanagement.model.Booking;
import java.util.List;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final TicketTypeDAO ticketTypeDAO = new TicketTypeDAO();
    private final SeatService seatService = new SeatService();

    public int createBooking(Booking booking) {
        int booked = ticketTypeDAO.getBookedCount(booking.getTicketTypeId());
        booking.setStatus("PENDING");
        return bookingDAO.createBooking(booking);
    }

    public boolean confirmBooking(int bookingId) {
        return bookingDAO.updateStatus(bookingId, "CONFIRMED");
    }

    public boolean cancelBooking(int bookingId) {
        Booking booking = bookingDAO.getById(bookingId);
        if (booking == null) return false;
        if (booking.getSeatId() != null) {
            seatService.releaseSeat(booking.getSeatId(), bookingId);
        }
        return bookingDAO.updateStatus(bookingId, "REFUND_PENDING");
    }

    public boolean completeRefund(int bookingId) {
        return bookingDAO.updateStatus(bookingId, "REFUNDED");
    }

    public boolean updateBooking(int bookingId, int attendeeId, String attendeeName, int eventId, int ticketTypeId) {
        boolean nameOk = new com.eventmanagement.dao.AttendeeDAO().updateFullName(attendeeId, attendeeName);
        boolean detailsOk = bookingDAO.updateDetails(bookingId, eventId, ticketTypeId);
        return nameOk && detailsOk;
    }

    public boolean deleteBooking(int bookingId) {
        return bookingDAO.deleteBooking(bookingId);
    }

    public boolean markCancelled(int bookingId) {
        return bookingDAO.updateStatus(bookingId, "CANCELLED");
    }

    public boolean assignSeatToBooking(int bookingId, int seatId) {
        return bookingDAO.assignSeat(bookingId, seatId);
    }

    public List<Booking> getBookingsForHall(int hallId) {
        return bookingDAO.getBookingsForHall(hallId);
    }

    public List<Booking> getAllBookings() { return bookingDAO.getAllBookings(); }
    public Booking getById(int id) { return bookingDAO.getById(id); }
}