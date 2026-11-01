package com.eventmanagement.ui;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.Session;
import com.eventmanagement.service.EventService;
import com.eventmanagement.service.ScheduleService;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SchedulePanel extends JPanel {
    private final ScheduleService scheduleService = new ScheduleService();
    private final EventService eventService = new EventService();

    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JComboBox<Event> eventCombo = new JComboBox<>();
    private List<Session> currentSessions = new ArrayList<>();

    private JButton editButton;
    private JButton deleteButton;

    private static final int FORM_WIDTH = 340;
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public SchedulePanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel eventLabel = new JLabel("Event:");
        eventLabel.setForeground(Color.WHITE);
        eventLabel.setFont(UITheme.LABEL_FONT);

        eventCombo.setPreferredSize(new Dimension(300, 34));
        eventCombo.setMaximumSize(new Dimension(300, 34));
        eventCombo.setRenderer(new EventCellRenderer());
        eventCombo.setBackground(Color.WHITE);
        loadEvents();

        JButton refreshButton = UIComponents.pillButton("Refresh", new Color(255, 255, 255, 40), Color.WHITE, true);
        refreshButton.addActionListener(e -> { loadEvents(); loadAgenda(); });

        JButton loadButton = UIComponents.pillButton("Load Agenda", new Color(255, 255, 255, 40), Color.WHITE, true);
        loadButton.addActionListener(e -> loadAgenda());

        JButton addButton = UIComponents.pillButton("+  Add Session", Color.WHITE, UITheme.PRIMARY, true);
        addButton.addActionListener(e -> openAddSessionDialog());

        editButton = UIComponents.pillButton("Edit", new Color(255, 255, 255, 40), Color.WHITE, true);
        editButton.addActionListener(e -> openEditSessionDialog());

        deleteButton = UIComponents.pillButton("Delete", new Color(255, 255, 255, 40), Color.WHITE, true);
        deleteButton.addActionListener(e -> deleteSelectedSession());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(eventLabel);
        actions.add(eventCombo);
        actions.add(refreshButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(loadButton);
        actions.add(addButton);

        add(UIComponents.panelBanner("Schedule", "Build the session agenda for an event", actions), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Title", "Speaker", "Event Name", "Venue Name", "Start", "End"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);

        RoundedPanel tableCard = UIComponents.tableCard(table);
        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        centerWrap.add(tableCard, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        updateButtonStates();
    }

    // ---------------------------------------------------------------
    // Event dropdown
    // ---------------------------------------------------------------

    private void loadEvents() {
        Event previouslySelected = getSelectedEvent();
        List<Event> allEvents = eventService.getAllEvents();

        eventCombo.removeAllItems();
        for (Event ev : allEvents) eventCombo.addItem(ev);

        if (previouslySelected != null) {
            for (int i = 0; i < eventCombo.getItemCount(); i++) {
                if (eventCombo.getItemAt(i).getEventId() == previouslySelected.getEventId()) {
                    eventCombo.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (eventCombo.getItemCount() > 0) {
            eventCombo.setSelectedIndex(0);
        }
    }

    /**
     * Shows a clean, human-readable label like:
     *   "SBI Meeting  ·  Event 6  ·  12 Jul 2026"
     * instead of raw IDs with "#" or ISO-style dates — both the closed
     * box and the popup list use this same renderer.
     */
    private class EventCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
            setFont(UITheme.BODY_FONT);
            if (value instanceof Event ev) {
                String dateText = ev.getStartDate() != null
                        ? ev.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        : "date TBA";
                setText(ev.getEventName() + "   ·   Event " + ev.getEventId() + "   ·   " + dateText);
            } else {
                setText("No events available");
            }
            return this;
        }
    }

    private Event getSelectedEvent() {
        Object selected = eventCombo.getSelectedItem();
        return selected instanceof Event ? (Event) selected : null;
    }

    // ---------------------------------------------------------------
    // Agenda loading
    // ---------------------------------------------------------------

    private void loadAgenda() {
        Event event = getSelectedEvent();
        if (event == null) {
            JOptionPane.showMessageDialog(this, "Select an event first.");
            return;
        }
        tableModel.setRowCount(0);
        Map<LocalDateTime, Session> agenda = scheduleService.getSortedAgenda(event.getEventId());
        currentSessions = new ArrayList<>(agenda.values());
        for (Session s : currentSessions) {
            tableModel.addRow(new Object[]{
                    s.getSessionTitle(),
                    s.getSpeakerName(),
                    s.getEventName() != null ? s.getEventName() : "—",
                    s.getVenueName() != null ? s.getVenueName() : "—",
                    s.getStartTime().format(DISPLAY_FMT),
                    s.getEndTime().format(DISPLAY_FMT)
            });
        }
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = table.getSelectedRow() != -1;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }

    private Session getSelectedSession() {
        int row = table.getSelectedRow();
        if (row == -1 || row >= currentSessions.size()) return null;
        return currentSessions.get(row);
    }

    // ---------------------------------------------------------------
    // Form-building helpers (aligned labels/fields, consistent width)
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

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return form;
    }

    // ---------------------------------------------------------------
    // Add / Edit / Delete
    // ---------------------------------------------------------------

    private void openAddSessionDialog() {
        Event event = getSelectedEvent();
        if (event == null) {
            JOptionPane.showMessageDialog(this, "Select an event first.");
            return;
        }

        JTextField titleField = new JTextField();
        JTextField speakerField = new JTextField();
        JTextField startField = new JTextField(LocalDateTime.now().withSecond(0).withNano(0).toString());
        JTextField endField = new JTextField(LocalDateTime.now().plusHours(1).withSecond(0).withNano(0).toString());
        UIComponents.styleTextField(titleField);
        UIComponents.styleTextField(speakerField);
        UIComponents.styleTextField(startField);
        UIComponents.styleTextField(endField);
        stretchField(titleField);
        stretchField(speakerField);
        stretchField(startField);
        stretchField(endField);

        JPanel form = buildForm();
        form.add(buildLabel("Event: " + event.getEventName()));
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Title"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(titleField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Speaker Name"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(speakerField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Start (yyyy-MM-ddTHH:mm)"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(startField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("End (yyyy-MM-ddTHH:mm)"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(endField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Session", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Session s = new Session();
                s.setScheduleId(event.getEventId());
                s.setSessionTitle(titleField.getText().trim());
                s.setSpeakerName(speakerField.getText().trim());
                s.setStartTime(LocalDateTime.parse(startField.getText().trim()));
                s.setEndTime(LocalDateTime.parse(endField.getText().trim()));

                boolean created = scheduleService.createSession(s);
                if (created) loadAgenda();
                else JOptionPane.showMessageDialog(this, "Conflict: overlapping session time for this event.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date/time format. Use yyyy-MM-ddTHH:mm.");
            }
        }
    }

    private void openEditSessionDialog() {
        Session session = getSelectedSession();
        if (session == null) { JOptionPane.showMessageDialog(this, "Select a session first."); return; }

        JTextField titleField = new JTextField(session.getSessionTitle());
        JTextField speakerField = new JTextField(session.getSpeakerName());
        JTextField startField = new JTextField(session.getStartTime().toString());
        JTextField endField = new JTextField(session.getEndTime().toString());
        UIComponents.styleTextField(titleField);
        UIComponents.styleTextField(speakerField);
        UIComponents.styleTextField(startField);
        UIComponents.styleTextField(endField);
        stretchField(titleField);
        stretchField(speakerField);
        stretchField(startField);
        stretchField(endField);

        JPanel form = buildForm();
        form.add(buildLabel("Event: " + (session.getEventName() != null ? session.getEventName() : "#" + session.getScheduleId())));
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Title"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(titleField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Speaker Name"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(speakerField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Start (yyyy-MM-ddTHH:mm)"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(startField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("End (yyyy-MM-ddTHH:mm)"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(endField);

        int result = JOptionPane.showConfirmDialog(this, form, "Edit Session", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                session.setSessionTitle(titleField.getText().trim());
                session.setSpeakerName(speakerField.getText().trim());
                session.setStartTime(LocalDateTime.parse(startField.getText().trim()));
                session.setEndTime(LocalDateTime.parse(endField.getText().trim()));

                boolean updated = scheduleService.updateSession(session);
                if (updated) loadAgenda();
                else JOptionPane.showMessageDialog(this, "Conflict: overlapping session time for this event.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date/time format. Use yyyy-MM-ddTHH:mm.");
            }
        }
    }

    private void deleteSelectedSession() {
        Session session = getSelectedSession();
        if (session == null) { JOptionPane.showMessageDialog(this, "Select a session first."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete session \"" + session.getSessionTitle() + "\" (" + session.getSpeakerName() + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = scheduleService.deleteSession(session.getSessionId());
            if (success) {
                loadAgenda();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete this session.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}