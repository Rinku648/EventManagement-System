package com.eventmanagement.ui;

import com.eventmanagement.dao.HallDAO;
import com.eventmanagement.dao.VenueDAO;
import com.eventmanagement.model.Booking;
import com.eventmanagement.model.Hall;
import com.eventmanagement.model.Seat;
import com.eventmanagement.model.Venue;
import com.eventmanagement.service.SeatService;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatMapPanel extends JPanel {
    private final SeatService seatService = new SeatService();
    private final HallDAO hallDAO = new HallDAO();
    private final VenueDAO venueDAO = new VenueDAO();

    private final JPanel gridPanel = new JPanel();
    private final JComboBox<Hall> hallDropdown = new JComboBox<>();
    private final JTextField searchField = new JTextField();
    private final Map<Integer, String> venueNameCache = new HashMap<>();
    private final Map<Integer, JButton> seatButtons = new HashMap<>();
    private final Map<Integer, Border> defaultBorders = new HashMap<>();

    private Map<Integer, Seat> currentSeats;
    private Integer selectedSeatId = null;
    private Hall currentHall = null;

    private static final int FORM_WIDTH = 320;
    private static final Border SELECT_BORDER = BorderFactory.createLineBorder(UITheme.PRIMARY, 3);
    private static final Border MATCH_BORDER = BorderFactory.createLineBorder(UITheme.WARNING, 3);

    public SeatMapPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        hallDropdown.setPreferredSize(new Dimension(280, 34));
        hallDropdown.setMaximumSize(new Dimension(280, 34));
        hallDropdown.setRenderer(new HallCellRenderer());
        loadHalls();

        JLabel hallLabel = new JLabel("Hall:");
        hallLabel.setForeground(Color.WHITE);
        hallLabel.setFont(UITheme.LABEL_FONT);

        JButton refreshButton = UIComponents.pillButton("Refresh", new Color(255, 255, 255, 40), Color.WHITE, true);
        refreshButton.addActionListener(e -> refreshHalls());

        JButton loadButton = UIComponents.pillButton("Load Seat Map", Color.WHITE, UITheme.PRIMARY, true);
        loadButton.addActionListener(e -> renderSeatMap());

        JButton editButton = UIComponents.pillButton("Edit", new Color(255, 255, 255, 40), Color.WHITE, true);
        editButton.addActionListener(e -> editSelectedSeat());

        JButton deleteButton = UIComponents.pillButton("Delete", new Color(255, 255, 255, 40), Color.WHITE, true);
        deleteButton.addActionListener(e -> deleteSelectedSeat());

        JButton resetButton = UIComponents.pillButton("Reset Seats", new Color(255, 255, 255, 40), Color.WHITE, true);
        resetButton.addActionListener(e -> resetSeats());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(hallLabel);
        actions.add(hallDropdown);
        actions.add(refreshButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(resetButton);
        actions.add(loadButton);

        add(UIComponents.panelBanner("Seat Map", "View seat availability for a hall", actions), BorderLayout.NORTH);

        // ---- Search row ----
        UIComponents.styleTextField(searchField);
        searchField.putClientProperty("JTextField.placeholderText", "Search by attendee name...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applySearchHighlight(); }
            public void removeUpdate(DocumentEvent e) { applySearchHighlight(); }
            public void changedUpdate(DocumentEvent e) { applySearchHighlight(); }
        });

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        searchWrap.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(UITheme.LABEL_BOLD_FONT);
        searchLabel.setForeground(UITheme.TEXT_MUTED);
        searchLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        searchWrap.add(searchLabel, BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);

        // ---- Legend ----
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        legend.setOpaque(false);
        legend.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        legend.add(legendChip("Available", UITheme.INFO));
        legend.add(legendChip("Booked", UITheme.DANGER));

        JPanel topStack = new JPanel();
        topStack.setOpaque(false);
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.add(searchWrap);
        topStack.add(legend);

        gridPanel.setOpaque(false);
        RoundedPanel gridCard = new RoundedPanel(UITheme.RADIUS_MD, UITheme.SURFACE, UITheme.BORDER);
        gridCard.setLayout(new BorderLayout());
        gridCard.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        gridCard.add(new JScrollPane(gridPanel), BorderLayout.CENTER);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(topStack, BorderLayout.NORTH);
        centerWrap.add(gridCard, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    // Hall dropdown
    // ---------------------------------------------------------------

    private void loadHalls() {
        hallDropdown.removeAllItems();
        List<Hall> halls = hallDAO.getAllHalls();
        for (Hall h : halls) {
            hallDropdown.addItem(h);
            venueNameCache.computeIfAbsent(h.getVenueId(), id -> {
                Venue v = venueDAO.getById(id);
                return v != null ? v.getVenueName() : "Unknown Venue";
            });
        }
    }

    /** Reloads the hall list live (picks up newly added venues/halls) without restarting the app. */
    private void refreshHalls() {
        Integer previousHallId = currentHall != null ? currentHall.getHallId() : null;
        venueNameCache.clear();
        loadHalls();

        if (previousHallId != null) {
            for (int i = 0; i < hallDropdown.getItemCount(); i++) {
                Hall h = hallDropdown.getItemAt(i);
                if (h.getHallId() == previousHallId) {
                    hallDropdown.setSelectedIndex(i);
                    break;
                }
            }
        }
        JOptionPane.showMessageDialog(this, "Hall list refreshed.");
    }

    private String venueNameFor(Hall h) {
        return venueNameCache.getOrDefault(h.getVenueId(), "Unknown Venue");
    }

    /** Shows "Hall Name · Venue Name (Hall Number)" so identical hall numbers across venues never collide. */
    private class HallCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Hall h) {
                setText(h.getHallName() + " · " + venueNameFor(h) + " (Hall " + h.getHallNumber() + ")");
            }
            return this;
        }
    }

    private JPanel legendChip(String label, Color color) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chip.setOpaque(false);
        JLabel dot = new JLabel("\u25CF");
        dot.setForeground(color);
        JLabel text = new JLabel(label);
        text.setFont(UITheme.LABEL_FONT);
        text.setForeground(UITheme.TEXT_MUTED);
        chip.add(dot);
        chip.add(text);
        return chip;
    }

    // ---------------------------------------------------------------
    // Grid rendering
    // ---------------------------------------------------------------

    private void renderSeatMap() {
        Hall hall = (Hall) hallDropdown.getSelectedItem();
        if (hall == null) { JOptionPane.showMessageDialog(this, "No hall selected."); return; }

        currentHall = hall;
        selectedSeatId = null;
        currentSeats = seatService.getSeatMap(hall.getHallId(), hall.getCapacity());

        gridPanel.removeAll();
        seatButtons.clear();
        defaultBorders.clear();
        gridPanel.setLayout(new GridLayout(0, 10, 8, 8));

        for (Seat seat : currentSeats.values()) {
            boolean available = "AVAILABLE".equals(seat.getStatus());
            JButton seatButton = UIComponents.pillButton(seat.getLabel(),
                    available ? UITheme.INFO_LIGHT : UITheme.DANGER_LIGHT,
                    available ? UITheme.INFO.darker() : UITheme.DANGER.darker(), true);
            seatButton.addActionListener(e -> selectSeat(seat.getSeatId()));
            gridPanel.add(seatButton);
            seatButtons.put(seat.getSeatId(), seatButton);
            defaultBorders.put(seat.getSeatId(), seatButton.getBorder());
        }
        gridPanel.revalidate();
        gridPanel.repaint();
        searchField.setText("");
    }

    private void selectSeat(int seatId) {
        // Clear previous selection border
        if (selectedSeatId != null && seatButtons.containsKey(selectedSeatId)) {
            seatButtons.get(selectedSeatId).setBorder(defaultBorders.get(selectedSeatId));
        }
        selectedSeatId = seatId;
        JButton button = seatButtons.get(seatId);
        if (button != null) button.setBorder(SELECT_BORDER);
    }

    // ---------------------------------------------------------------
    // Search — highlights the seat(s) booked by a matching attendee name
    // ---------------------------------------------------------------

    private void applySearchHighlight() {
        if (currentSeats == null) return;
        String query = searchField.getText().trim().toLowerCase();

        boolean scrolled = false;
        for (Seat seat : currentSeats.values()) {
            JButton button = seatButtons.get(seat.getSeatId());
            if (button == null) continue;

            boolean isSelected = seat.getSeatId() == (selectedSeatId != null ? selectedSeatId : -1);
            if (isSelected) continue; // don't override the selection border

            boolean matches = !query.isEmpty() && seat.getAttendeeName() != null
                    && seat.getAttendeeName().toLowerCase().contains(query);

            button.setBorder(matches ? MATCH_BORDER : defaultBorders.get(seat.getSeatId()));

            if (matches && !scrolled) {
                button.scrollRectToVisible(button.getBounds());
                scrolled = true;
            }
        }
    }

    // ---------------------------------------------------------------
    // Edit dialog
    // ---------------------------------------------------------------

    private JLabel buildLabel(String text) {
        JLabel label = UIComponents.formLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension pref = label.getPreferredSize();
        label.setMaximumSize(new Dimension(FORM_WIDTH, pref.height));
        return label;
    }

    private void stretchField(JComponent field) {
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension pref = field.getPreferredSize();
        Dimension fixed = new Dimension(FORM_WIDTH, Math.max(pref.height, 34));
        field.setPreferredSize(fixed);
        field.setMaximumSize(fixed);
        field.setMinimumSize(fixed);
    }

    private void editSelectedSeat() {
        if (selectedSeatId == null) { JOptionPane.showMessageDialog(this, "Click a seat first to select it."); return; }
        Seat seat = currentSeats.get(selectedSeatId);
        if (seat == null || currentHall == null) return;

        List<Booking> bookings = seatService.getBookingsForHall(currentHall.getHallId());

        JComboBox<String> statusBox = new JComboBox<>(new String[]{"AVAILABLE", "BOOKED"});
        statusBox.setSelectedItem(seat.getStatus());
        stretchField(statusBox);

        JComboBox<String> zoneBox = new JComboBox<>(new String[]{"REGULAR", "PREMIUM", "VIP"});
        zoneBox.setSelectedItem(seat.getZone());
        stretchField(zoneBox);

        JComboBox<Booking> bookingBox = new JComboBox<>(bookings.toArray(new Booking[0]));
        bookingBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Booking b) {
                    String name = b.getAttendeeName() != null ? b.getAttendeeName() : "Guest";
                    setText(name + " — Booking #" + b.getBookingId());
                }
                return this;
            }
        });
        stretchField(bookingBox);
        if (seat.getBookingId() != null) {
            for (int i = 0; i < bookingBox.getItemCount(); i++) {
                if (bookingBox.getItemAt(i).getBookingId() == seat.getBookingId()) {
                    bookingBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        bookingBox.setEnabled("BOOKED".equals(seat.getStatus()));
        statusBox.addActionListener(e -> bookingBox.setEnabled("BOOKED".equals(statusBox.getSelectedItem())));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        form.add(buildLabel("Seat: " + seat.getLabel()));
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Status"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(statusBox);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Zone"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(zoneBox);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Assign Attendee (if Booked)"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(bookingBox);

        int result = JOptionPane.showConfirmDialog(this, form, "Edit Seat " + seat.getLabel(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newStatus = (String) statusBox.getSelectedItem();
            String newZone = (String) zoneBox.getSelectedItem();

            seatService.updateZone(seat.getSeatId(), newZone);

            if ("BOOKED".equals(newStatus)) {
                Booking chosen = (Booking) bookingBox.getSelectedItem();
                if (chosen == null) {
                    JOptionPane.showMessageDialog(this, "No booking available to assign — this hall has no bookings yet.");
                } else {
                    seatService.bookSeatWithBooking(seat.getSeatId(), chosen.getBookingId());
                }
            } else {
                seatService.markAvailable(seat.getSeatId());
            }
            renderSeatMap();
        }
    }

    private void deleteSelectedSeat() {
        if (selectedSeatId == null) { JOptionPane.showMessageDialog(this, "Click a seat first to select it."); return; }
        Seat seat = currentSeats.get(selectedSeatId);
        if (seat == null) return;

        if (!"BOOKED".equals(seat.getStatus())) {
            JOptionPane.showMessageDialog(this, "Seat " + seat.getLabel() + " has no active booking to cancel.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel the booking for seat " + seat.getLabel() +
                        (seat.getAttendeeName() != null ? " (" + seat.getAttendeeName() + ")" : "") +
                        "?\nThe booking will be marked CANCELLED and the seat freed.",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = seatService.cancelBookingForSeat(seat.getSeatId());
            if (success) {
                renderSeatMap();
            } else {
                JOptionPane.showMessageDialog(this, "Could not cancel this booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetSeats() {
        if (currentHall == null) { JOptionPane.showMessageDialog(this, "Load a hall's seat map first."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Reset ALL seats for " + currentHall.getHallName() + "?\n" +
                        "This deletes every seat and booking-history record for this hall, then regenerates a clean grid.\n" +
                        "Use this only if the seat layout looks duplicated or corrupted.",
                "Confirm Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            seatService.resetAndRegenerateHall(currentHall.getHallId(), currentHall.getCapacity());
            renderSeatMap();
            JOptionPane.showMessageDialog(this, "Seats regenerated for " + currentHall.getHallName() + ".");
        }
    }
}