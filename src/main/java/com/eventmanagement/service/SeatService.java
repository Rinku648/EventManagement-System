package com.eventmanagement.service;

import com.eventmanagement.dao.BookingDAO;
import com.eventmanagement.dao.SeatDAO;
import com.eventmanagement.model.Booking;
import com.eventmanagement.model.Seat;
import java.util.List;
import java.util.Map;

public class SeatService {
    private final SeatDAO seatDAO = new SeatDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    /** Ensures seats exist for the hall (matching its real capacity), then returns live status. */
    public Map<Integer, Seat> getSeatMap(int hallId, int capacity) {
        seatDAO.ensureSeatsForHall(hallId, capacity);
        return seatDAO.loadSeatMap(hallId);
    }

    /** Wipes and cleanly regenerates every seat for a hall — fixes corrupted/duplicate seat data. */
    public void resetAndRegenerateHall(int hallId, int capacity) {
        seatDAO.resetSeatsForHall(hallId);
        seatDAO.ensureSeatsForHall(hallId, capacity);
    }

    /** Bookings tied to events in this hall — used to populate the "assign attendee" picker. */
    public List<Booking> getBookingsForHall(int hallId) {
        return bookingDAO.getBookingsForHall(hallId);
    }

    /** Books a seat to an existing booking/attendee. */
    public boolean bookSeatWithBooking(int seatId, int bookingId) {
        bookingDAO.assignSeat(bookingId, seatId);
        return seatDAO.bookSeat(seatId, bookingId);
    }

    /** Releases a specific seat for a specific booking. */
    public boolean releaseSeat(int seatId, int bookingId) {
        return seatDAO.releaseSeat(seatId, bookingId);
    }

    /** Marks a seat available — looks up the latest booking on that seat automatically. */
    public boolean markAvailable(int seatId) {
        int bookingId = seatDAO.findLatestBookingId(seatId);
        if (bookingId == -1) return true; // already available, nothing to release
        return seatDAO.releaseSeat(seatId, bookingId);
    }

    /** Cancels the booking tied to this seat AND frees the seat — used by the Delete button. */
    public boolean cancelBookingForSeat(int seatId) {
        int bookingId = seatDAO.findLatestBookingId(seatId);
        if (bookingId == -1) return false;
        boolean cancelled = bookingDAO.updateStatus(bookingId, "CANCELLED");
        boolean released = seatDAO.releaseSeat(seatId, bookingId);
        return cancelled && released;
    }

    public boolean updateZone(int seatId, String zone) {
        return seatDAO.updateZone(seatId, zone);
    }
}