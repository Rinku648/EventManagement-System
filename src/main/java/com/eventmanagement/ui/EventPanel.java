package com.eventmanagement.ui;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.User;
import com.eventmanagement.dao.EventDAO;
import com.eventmanagement.util.DateUtil;
import com.eventmanagement.util.ValidationUtil;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class EventPanel extends JPanel {
    private static final String[] FUNCTION_TYPES = {
            "Conference", "Wedding", "Corporate Event", "Concert",
            "Exhibition", "Birthday Party", "Workshop", "Seminar", "Other"
    };

    private final EventDAO eventDAO = new EventDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final User currentUser;

    private final JLabel totalCountLabel = new JLabel("0");
    private final JLabel upcomingCountLabel = new JLabel("0");
    private final JLabel ongoingCountLabel = new JLabel("0");
    private final JLabel completedCountLabel = new JLabel("0");
    private final JLabel cancelledCountLabel = new JLabel("0");

    private JButton editButton;
    private JButton completeButton;
    private JButton cancelButton;
    private JButton deleteButton;

    public EventPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setOpaque(false);

        JButton refreshButton = UIComponents.pillButton("Refresh", new Color(255, 255, 255, 40), Color.WHITE, true);
        refreshButton.addActionListener(e -> loadEvents());
        JButton createButton = UIComponents.pillButton("+  Create Event", Color.WHITE, UITheme.PRIMARY, true);
        createButton.addActionListener(e -> openCreateEventDialog());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(refreshButton);
        actions.add(createButton);

        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.setOpaque(false);
        headerWrap.add(UIComponents.panelBanner("Events", "Manage and track every event in one place", actions), BorderLayout.NORTH);
        headerWrap.add(buildStatsRow(), BorderLayout.SOUTH);
        add(headerWrap, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Event Name", "Start Date", "Start Time", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = buildTable();
        table.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        RoundedPanel tableCard = UIComponents.tableCard(table);

        editButton = UIComponents.secondaryButton("Edit");
        editButton.addActionListener(e -> openEditEventDialog());

        completeButton = UIComponents.pillButton("Mark Completed", UITheme.SUCCESS, Color.WHITE, true);
        completeButton.addActionListener(e -> markStatus("COMPLETED", "Mark this event as Completed?"));

        cancelButton = UIComponents.pillButton("Mark Cancelled", UITheme.WARNING, Color.WHITE, true);
        cancelButton.addActionListener(e -> markStatus("CANCELLED", "Mark this event as Cancelled?"));

        deleteButton = UIComponents.dangerButton("Delete");
        deleteButton.addActionListener(e -> deleteSelected());

        updateButtonStates();

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        toolbar.add(editButton);
        toolbar.add(completeButton);
        toolbar.add(cancelButton);
        toolbar.add(deleteButton);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        centerWrap.add(toolbar, BorderLayout.NORTH);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        tableWrap.add(tableCard, BorderLayout.CENTER);
        centerWrap.add(tableWrap, BorderLayout.CENTER);

        add(centerWrap, BorderLayout.CENTER);

        loadEvents();
    }

    private void updateButtonStates() {
        boolean hasSelection = table.getSelectedRow() != -1;
        editButton.setEnabled(hasSelection);
        completeButton.setEnabled(hasSelection);
        cancelButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }

    private Event getSelectedEvent() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        int eventId = (int) tableModel.getValueAt(row, 0);
        return eventDAO.getById(eventId);
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 16, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        row.add(UIComponents.statCard("\uD83D\uDCC5", "Total Events", totalCountLabel, UITheme.PRIMARY, UITheme.PRIMARY_LIGHT));
        row.add(UIComponents.statCard("\u23F3", "Upcoming", upcomingCountLabel, UITheme.WARNING, UITheme.WARNING_LIGHT));
        row.add(UIComponents.statCard("\uD83D\uDD34", "Ongoing", ongoingCountLabel, UITheme.INFO, UITheme.INFO_LIGHT));
        row.add(UIComponents.statCard("\u2705", "Completed", completedCountLabel, UITheme.SUCCESS, UITheme.SUCCESS_LIGHT));
        row.add(UIComponents.statCard("\u274C", "Cancelled", cancelledCountLabel, UITheme.DANGER, UITheme.DANGER_LIGHT));
        return row;
    }

    private JTable buildTable() {
        JTable t = new JTable(tableModel);
        UIComponents.styleTable(t);
        t.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        t.getColumnModel().getColumn(0).setPreferredWidth(50);
        t.getColumnModel().getColumn(1).setPreferredWidth(260);
        t.getColumnModel().getColumn(2).setPreferredWidth(120);
        t.getColumnModel().getColumn(3).setPreferredWidth(90);
        t.getColumnModel().getColumn(4).setPreferredWidth(130);
        return t;
    }

    /**
     * Computes what this event's status SHOULD be right now, based on its
     * date/time versus the current moment — unless it's already in a
     * manually-set terminal state (COMPLETED or CANCELLED), which is never
     * auto-overridden.
     *
     * - Past date (before today)              -> COMPLETED (auto)
     * - Today, and start time has arrived      -> ONGOING
     * - Today, but start time hasn't arrived   -> UPCOMING
     * - Future date                            -> UPCOMING
     */
    private String computeEffectiveStatus(Event ev, LocalDate today, LocalDateTime now) {
        String stored = ev.getStatus() == null ? "" : ev.getStatus().toUpperCase();
        if (stored.equals("CANCELLED") || stored.equals("COMPLETED")) {
            return stored;
        }

        LocalDate startDate = ev.getStartDate();
        if (startDate == null) {
            return stored.isEmpty() ? "UPCOMING" : stored;
        }

        if (startDate.isBefore(today)) {
            return "COMPLETED";
        }

        if (startDate.isEqual(today)) {
            LocalTime startTime;
            try {
                startTime = (ev.getStartTime() != null && !ev.getStartTime().isBlank())
                        ? LocalTime.parse(ev.getStartTime())
                        : LocalTime.MIDNIGHT;
            } catch (Exception ex) {
                startTime = LocalTime.MIDNIGHT;
            }
            LocalDateTime eventStart = LocalDateTime.of(startDate, startTime);
            return now.isBefore(eventStart) ? "UPCOMING" : "ONGOING";
        }

        return "UPCOMING";
    }

    private void loadEvents() {
        tableModel.setRowCount(0);
        List<Event> events = eventDAO.getAllEvents();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        int upcoming = 0, ongoing = 0, completed = 0, cancelled = 0;
        for (Event ev : events) {
            String effectiveStatus = computeEffectiveStatus(ev, today, now);

            // Persist the auto-calculated status if it actually changed,
            // so the database and every other module stay in sync too.
            String storedStatus = ev.getStatus() == null ? "" : ev.getStatus().toUpperCase();
            if (!effectiveStatus.equals(storedStatus)) {
                eventDAO.updateStatus(ev.getEventId(), effectiveStatus);
            }

            String timeDisplay = (ev.getStartTime() == null || ev.getStartTime().isEmpty()) ? "--:--" : ev.getStartTime();
            tableModel.addRow(new Object[]{ev.getEventId(), ev.getEventName(), DateUtil.format(ev.getStartDate()), timeDisplay, effectiveStatus});

            switch (effectiveStatus) {
                case "UPCOMING" -> upcoming++;
                case "ONGOING" -> ongoing++;
                case "COMPLETED" -> completed++;
                case "CANCELLED" -> cancelled++;
            }
        }
        totalCountLabel.setText(String.valueOf(events.size()));
        upcomingCountLabel.setText(String.valueOf(upcoming));
        ongoingCountLabel.setText(String.valueOf(ongoing));
        completedCountLabel.setText(String.valueOf(completed));
        cancelledCountLabel.setText(String.valueOf(cancelled));
        updateButtonStates();
    }

    private void markStatus(String newStatus, String confirmMessage) {
        Event event = getSelectedEvent();
        if (event == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "\"" + event.getEventName() + "\" — " + confirmMessage,
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            eventDAO.updateStatus(event.getEventId(), newStatus);
            loadEvents();
        }
    }

    private void deleteSelected() {
        Event event = getSelectedEvent();
        if (event == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + event.getEventName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (eventDAO.deleteEvent(event.getEventId())) {
                loadEvents();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not delete this event. It may still have bookings, budgets, or other linked records.",
                        "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openCreateEventDialog() {
        openEventDialog(null);
    }

    private void openEditEventDialog() {
        Event event = getSelectedEvent();
        if (event == null) return;
        openEventDialog(event);
    }

    private void openEventDialog(Event existingEvent) {
        boolean isEdit = existingEvent != null;

        JTextField nameField = new JTextField(isEdit ? existingEvent.getEventName() : "");
        JTextField dateField = new JTextField(isEdit ? existingEvent.getStartDate().toString() : LocalDate.now().toString());
        JTextField timeField = new JTextField(isEdit && existingEvent.getStartTime() != null ? existingEvent.getStartTime() : "18:00");
        UIComponents.styleTextField(nameField);
        UIComponents.styleTextField(dateField);
        UIComponents.styleTextField(timeField);

        JComboBox<String> functionTypeBox = new JComboBox<>(FUNCTION_TYPES);
        functionTypeBox.setFont(UITheme.BODY_FONT);
        functionTypeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        functionTypeBox.setMaximumSize(new Dimension(340, 36));
        if (isEdit && existingEvent.getFunctionType() != null) {
            functionTypeBox.setSelectedItem(existingEvent.getFunctionType());
        }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));

        form.add(UIComponents.formLabel("Event Name"));
        form.add(nameField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(UIComponents.formLabel("Start Date (yyyy-mm-dd)"));
        form.add(dateField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(UIComponents.formLabel("Start Time (HH:mm, 24-hour)"));
        form.add(timeField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(UIComponents.formLabel("Function Type"));
        form.add(functionTypeBox);

        int result = JOptionPane.showConfirmDialog(this, form,
                isEdit ? "Edit Event" : "Create New Event",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        if (!ValidationUtil.isNotEmpty(nameField.getText())) {
            JOptionPane.showMessageDialog(this, "Event name is required.");
            return;
        }
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(dateField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Start date must be in yyyy-mm-dd format.");
            return;
        }
        String timeText = timeField.getText().trim();
        if (!timeText.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            JOptionPane.showMessageDialog(this, "Start time must be in HH:mm 24-hour format, e.g. 18:30.");
            return;
        }

        if (isEdit) {
            existingEvent.setEventName(nameField.getText().trim());
            existingEvent.setStartDate(parsedDate);
            existingEvent.setStartTime(timeText);
            existingEvent.setFunctionType((String) functionTypeBox.getSelectedItem());
            if (eventDAO.updateEvent(existingEvent)) loadEvents();
            else JOptionPane.showMessageDialog(this, "Failed to update event.");
        } else {
            Event event = new Event();
            event.setEventName(nameField.getText().trim());
            event.setOrganizerId(currentUser.getUserId());
            event.setStartDate(parsedDate);
            event.setStartTime(timeText);
            event.setDayNumber(1);
            event.setFunctionType((String) functionTypeBox.getSelectedItem());
            event.setStatus("UPCOMING");

            if (eventDAO.createEvent(event)) loadEvents();
            else JOptionPane.showMessageDialog(this, "Failed to create event.");
        }
    }

    private class StatusCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String status = value == null ? "" : value.toString();
            return UIComponents.badgeCell(status, isSelected, row % 2 == 0);
        }
    }
}