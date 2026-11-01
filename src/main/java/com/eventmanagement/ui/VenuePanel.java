package com.eventmanagement.ui;

import com.eventmanagement.model.Venue;
import com.eventmanagement.model.Hall;
import com.eventmanagement.model.Event;
import com.eventmanagement.service.VenueService;
import com.eventmanagement.util.ValidationUtil;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VenuePanel extends JPanel {
    private final VenueService venueService = new VenueService();
    private final DefaultTableModel venueTableModel;
    private final DefaultTableModel hallTableModel;
    private final JTable venueTable;
    private final JTable hallTable;

    private JButton editVenueButton;
    private JButton manageVenueAssignmentButton;
    private JButton deleteVenueButton;
    private JButton editHallButton;
    private JButton manageHallAssignmentButton;
    private JButton deleteHallButton;

    public VenuePanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JButton addVenueButton = UIComponents.pillButton("+  Add Venue", Color.WHITE, UITheme.PRIMARY, true);
        addVenueButton.addActionListener(e -> openAddVenueDialog());
        JButton addHallButton = UIComponents.pillButton("+  Add Hall", new Color(255, 255, 255, 40), Color.WHITE, true);
        addHallButton.addActionListener(e -> openAddHallDialog());
        JButton refreshButton = UIComponents.pillButton("Refresh", new Color(255, 255, 255, 40), Color.WHITE, true);
        refreshButton.addActionListener(e -> { loadVenues(); loadHalls(); });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(refreshButton);
        actions.add(addHallButton);
        actions.add(addVenueButton);

        add(UIComponents.panelBanner("Venues", "Manage venues and their halls", actions), BorderLayout.NORTH);

        venueTableModel = new DefaultTableModel(new Object[]{"Venue ID", "Venue Name", "City", "Address", "Assigned Events"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        venueTable = new JTable(venueTableModel);
        UIComponents.styleTable(venueTable);
        venueTable.getSelectionModel().addListSelectionListener(e -> updateVenueButtonStates());

        hallTableModel = new DefaultTableModel(
                new Object[]{"Hall ID", "Venue ID", "Hall Number", "Hall Name", "Capacity", "Cost", "Assigned Events"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        hallTable = new JTable(hallTableModel);
        UIComponents.styleTable(hallTable);
        hallTable.getSelectionModel().addListSelectionListener(e -> updateHallButtonStates());
        hallTable.getColumnModel().removeColumn(hallTable.getColumnModel().getColumn(0));

        RoundedPanel venueCard = UIComponents.tableCard(venueTable);
        RoundedPanel hallCard = UIComponents.tableCard(hallTable);

        editVenueButton = UIComponents.secondaryButton("Edit");
        editVenueButton.addActionListener(e -> openEditVenueDialog());
        manageVenueAssignmentButton = UIComponents.pillButton("Manage Assignment", UITheme.INFO, Color.WHITE, true);
        manageVenueAssignmentButton.addActionListener(e -> openManageVenueAssignmentDialog());
        deleteVenueButton = UIComponents.dangerButton("Delete Venue");
        deleteVenueButton.addActionListener(e -> deleteSelectedVenue());

        JPanel venueToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        venueToolbar.setOpaque(false);
        venueToolbar.add(editVenueButton);
        venueToolbar.add(manageVenueAssignmentButton);
        venueToolbar.add(deleteVenueButton);

        editHallButton = UIComponents.secondaryButton("Edit");
        editHallButton.addActionListener(e -> openEditHallDialog());
        manageHallAssignmentButton = UIComponents.pillButton("Manage Assignment", UITheme.INFO, Color.WHITE, true);
        manageHallAssignmentButton.addActionListener(e -> openManageHallAssignmentDialog());
        deleteHallButton = UIComponents.dangerButton("Delete Hall");
        deleteHallButton.addActionListener(e -> deleteSelectedHall());

        JPanel hallToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        hallToolbar.setOpaque(false);
        hallToolbar.add(editHallButton);
        hallToolbar.add(manageHallAssignmentButton);
        hallToolbar.add(deleteHallButton);

        updateVenueButtonStates();
        updateHallButtonStates();

        JPanel venueSectionHeader = new JPanel(new BorderLayout());
        venueSectionHeader.setOpaque(false);
        venueSectionHeader.add(sectionLabel("Venues"), BorderLayout.WEST);
        venueSectionHeader.add(venueToolbar, BorderLayout.EAST);

        JPanel hallSectionHeader = new JPanel(new BorderLayout());
        hallSectionHeader.setOpaque(false);
        hallSectionHeader.add(sectionLabel("Halls"), BorderLayout.WEST);
        hallSectionHeader.add(hallToolbar, BorderLayout.EAST);

        JPanel venueBlock = new JPanel(new BorderLayout(0, 8));
        venueBlock.setOpaque(false);
        venueBlock.add(venueSectionHeader, BorderLayout.NORTH);
        venueBlock.add(venueCard, BorderLayout.CENTER);

        JPanel hallBlock = new JPanel(new BorderLayout(0, 8));
        hallBlock.setOpaque(false);
        hallBlock.add(hallSectionHeader, BorderLayout.NORTH);
        hallBlock.add(hallCard, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, venueBlock, hallBlock);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        centerWrap.add(splitPane, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        loadVenues();
        loadHalls();
    }

    private void updateVenueButtonStates() {
        boolean hasSelection = venueTable.getSelectedRow() != -1;
        editVenueButton.setEnabled(hasSelection);
        manageVenueAssignmentButton.setEnabled(hasSelection);
        deleteVenueButton.setEnabled(hasSelection);
    }

    private void updateHallButtonStates() {
        boolean hasSelection = hallTable.getSelectedRow() != -1;
        editHallButton.setEnabled(hasSelection);
        manageHallAssignmentButton.setEnabled(hasSelection);
        deleteHallButton.setEnabled(hasSelection);
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.SECTION_FONT);
        l.setForeground(UITheme.TEXT_DARK);
        return l;
    }

    private void loadVenues() {
        venueTableModel.setRowCount(0);
        List<Venue> venues = venueService.getAllVenues();
        for (Venue v : venues) {
            List<String> assigned = venueService.getAssignedEventNamesForVenue(v.getVenueId());
            String assignedText = assigned.isEmpty() ? "—" : String.join(", ", assigned);
            venueTableModel.addRow(new Object[]{v.getVenueId(), v.getVenueName(), v.getCity(), v.getAddress(), assignedText});
        }
        updateVenueButtonStates();
    }

    private void loadHalls() {
        hallTableModel.setRowCount(0);
        List<Venue> venues = venueService.getAllVenues();
        for (Venue venue : venues) {
            List<Hall> halls = venueService.getHallsByVenue(venue.getVenueId());
            for (Hall h : halls) {
                String numberDisplay = (h.getHallNumber() == null || h.getHallNumber().isBlank()) ? "—" : h.getHallNumber();
                List<String> assigned = venueService.getAssignedEventNamesForHall(h.getHallId());
                String assignedText = assigned.isEmpty() ? "—" : String.join(", ", assigned);
                hallTableModel.addRow(new Object[]{
                        h.getHallId(), h.getVenueId(), numberDisplay, h.getHallName(), h.getCapacity(), h.getCost(), assignedText
                });
            }
        }
        updateHallButtonStates();
    }

    private Venue getSelectedVenue() {
        int row = venueTable.getSelectedRow();
        if (row == -1) return null;
        int venueId = (int) venueTableModel.getValueAt(row, 0);
        for (Venue v : venueService.getAllVenues()) {
            if (v.getVenueId() == venueId) return v;
        }
        return null;
    }

    private Hall getSelectedHall() {
        int row = hallTable.getSelectedRow();
        if (row == -1) return null;
        int hallId = (int) hallTableModel.getValueAt(row, 0);
        int venueId = (int) hallTableModel.getValueAt(row, 1);
        for (Hall h : venueService.getHallsByVenue(venueId)) {
            if (h.getHallId() == hallId) return h;
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Shared dialog chrome — a polished rounded card in a borderless
    // JDialog, used by every popup on this screen for a consistent,
    // professional look instead of raw JOptionPane layouts.
    // ---------------------------------------------------------------
    private JDialog buildStyledDialog(String title, String subtitle, int width, int height) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this) instanceof Frame f ? f : null, true);
        dialog.setUndecorated(false);
        dialog.setTitle(title);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.BACKGROUND);
        dialog.setLayout(new BorderLayout());
        return dialog;
    }

    private JPanel buildDialogHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(20, 24, 8, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.SECTION_FONT);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(titleLabel);

        if (subtitle != null && !subtitle.isBlank()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(UITheme.SUBTITLE_FONT);
            subtitleLabel.setForeground(UITheme.TEXT_MUTED);
            subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(Box.createRigidArea(new Dimension(0, 3)));
            header.add(subtitleLabel);
        }
        return header;
    }

    private JPanel buildFieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = UIComponents.formLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (field instanceof JTextField tf) {
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            tf.setPreferredSize(new Dimension(100, 36));
        } else if (field instanceof JComboBox<?> cb) {
            cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            cb.setPreferredSize(new Dimension(100, 36));
        }
        row.add(label);
        row.add(Box.createRigidArea(new Dimension(0, 5)));
        row.add(field);
        row.add(Box.createRigidArea(new Dimension(0, 14)));
        return row;
    }

    // ---------------------------------------------------------------
    // Add / Edit / Delete Venue
    // ---------------------------------------------------------------
    private void openAddVenueDialog() {
        openVenueFormDialog(null);
    }

    private void openEditVenueDialog() {
        Venue venue = getSelectedVenue();
        if (venue == null) return;
        openVenueFormDialog(venue);
    }

    private void openVenueFormDialog(Venue existing) {
        boolean isEdit = existing != null;
        JDialog dialog = buildStyledDialog(isEdit ? "Edit Venue" : "Add Venue", null, 420, 400);

        JTextField nameField = new JTextField(isEdit ? existing.getVenueName() : "");
        JTextField cityField = new JTextField(isEdit ? existing.getCity() : "");
        JTextField addressField = new JTextField(isEdit ? existing.getAddress() : "");
        UIComponents.styleTextField(nameField);
        UIComponents.styleTextField(cityField);
        UIComponents.styleTextField(addressField);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(4, 24, 4, 24));
        body.add(buildFieldRow("Venue Name", nameField));
        body.add(buildFieldRow("City", cityField));
        body.add(buildFieldRow("Address", addressField));

        JButton saveButton = UIComponents.primaryButton(isEdit ? "Save Changes" : "Add Venue");
        JButton cancelButton = UIComponents.secondaryButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        saveButton.addActionListener(e -> {
            if (!ValidationUtil.isNotEmpty(nameField.getText())) {
                JOptionPane.showMessageDialog(dialog, "Venue name is required.");
                return;
            }
            if (isEdit) {
                existing.setVenueName(nameField.getText().trim());
                existing.setCity(cityField.getText().trim());
                existing.setAddress(addressField.getText().trim());
                if (venueService.updateVenue(existing)) {
                    loadVenues();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update venue.");
                }
            } else {
                Venue v = new Venue();
                v.setVenueName(nameField.getText().trim());
                v.setCity(cityField.getText().trim());
                v.setAddress(addressField.getText().trim());
                if (venueService.createVenue(v)) {
                    loadVenues();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add venue.");
                }
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 24, 20, 24));
        footer.add(cancelButton);
        footer.add(saveButton);

        dialog.add(buildDialogHeader(isEdit ? "Edit Venue" : "Add Venue", isEdit ? existing.getVenueName() : "Create a new venue"), BorderLayout.NORTH);
        dialog.add(body, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedVenue() {
        Venue venue = getSelectedVenue();
        if (venue == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + venue.getVenueName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (venueService.deleteVenue(venue.getVenueId())) {
                loadVenues();
                loadHalls();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not delete this venue. It may still have halls or assigned events linked to it. " +
                                "Remove its halls and unassign it from events first.",
                        "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------------------------------------------------------
    // Add / Edit / Delete Hall
    // ---------------------------------------------------------------
    private void openAddHallDialog() {
        openHallFormDialog(null);
    }

    private void openEditHallDialog() {
        Hall hall = getSelectedHall();
        if (hall == null) return;
        openHallFormDialog(hall);
    }

    private void openHallFormDialog(Hall existing) {
        boolean isEdit = existing != null;
        JDialog dialog = buildStyledDialog(isEdit ? "Edit Hall" : "Add Hall", null, 420, 480);

        JTextField venueIdField = new JTextField(isEdit ? String.valueOf(existing.getVenueId()) : "");
        JTextField hallNumberField = new JTextField(isEdit && existing.getHallNumber() != null ? existing.getHallNumber() : "");
        JTextField hallNameField = new JTextField(isEdit ? existing.getHallName() : "");
        JTextField capacityField = new JTextField(isEdit ? String.valueOf(existing.getCapacity()) : "");
        JTextField costField = new JTextField(isEdit ? String.valueOf(existing.getCost()) : "");
        UIComponents.styleTextField(venueIdField);
        UIComponents.styleTextField(hallNumberField);
        UIComponents.styleTextField(hallNameField);
        UIComponents.styleTextField(capacityField);
        UIComponents.styleTextField(costField);
        venueIdField.setEditable(!isEdit); // venue can't be changed after creation, only via reassignment elsewhere
        if (isEdit) venueIdField.setBackground(new Color(0xF3F4F6));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(4, 24, 4, 24));
        body.add(buildFieldRow("Venue ID", venueIdField));
        body.add(buildFieldRow("Hall Number (e.g. \"1\" or \"A\")", hallNumberField));
        body.add(buildFieldRow("Hall Name", hallNameField));
        body.add(buildFieldRow("Capacity", capacityField));
        body.add(buildFieldRow("Cost", costField));

        JButton saveButton = UIComponents.primaryButton(isEdit ? "Save Changes" : "Add Hall");
        JButton cancelButton = UIComponents.secondaryButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        saveButton.addActionListener(e -> {
            try {
                int capacity = Integer.parseInt(capacityField.getText().trim());
                double cost = Double.parseDouble(costField.getText().trim());

                if (isEdit) {
                    existing.setHallNumber(hallNumberField.getText().trim());
                    existing.setHallName(hallNameField.getText().trim());
                    existing.setCapacity(capacity);
                    existing.setCost(cost);
                    if (venueService.updateHall(existing)) {
                        loadHalls();
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to update hall.");
                    }
                } else {
                    int venueId = Integer.parseInt(venueIdField.getText().trim());
                    Hall h = new Hall();
                    h.setVenueId(venueId);
                    h.setHallNumber(hallNumberField.getText().trim());
                    h.setHallName(hallNameField.getText().trim());
                    h.setCapacity(capacity);
                    h.setCost(cost);
                    if (venueService.createHall(h)) {
                        loadHalls();
                        dialog.dispose();
                        JOptionPane.showMessageDialog(this, "Hall added successfully.");
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to add hall.");
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Venue ID, Capacity, and Cost must be numbers.");
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 24, 20, 24));
        footer.add(cancelButton);
        footer.add(saveButton);

        dialog.add(buildDialogHeader(isEdit ? "Edit Hall" : "Add Hall", isEdit ? existing.getHallName() : "Add a new hall to a venue"), BorderLayout.NORTH);
        dialog.add(body, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedHall() {
        Hall hall = getSelectedHall();
        if (hall == null) return;
        String label = (hall.getHallNumber() == null || hall.getHallNumber().isBlank()) ? hall.getHallName() : hall.getHallNumber() + " — " + hall.getHallName();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete hall \"" + label + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (venueService.deleteHall(hall.getHallId())) {
                loadHalls();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not delete this hall. It may still have seats or assigned events linked to it.",
                        "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------------------------------------------------------
    // Manage Assignment — Venue (polished, properly spaced dialog)
    // ---------------------------------------------------------------
    private void openManageVenueAssignmentDialog() {
        Venue venue = getSelectedVenue();
        if (venue == null) return;

        JDialog dialog = buildStyledDialog("Manage Assignment", null, 480, 560);
        dialog.add(buildDialogHeader("Manage Assignment", venue.getVenueName()), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(4, 24, 4, 24));

        JLabel currentLabel = UIComponents.formLabel("CURRENTLY ASSIGNED EVENTS");
        currentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(currentLabel);
        body.add(Box.createRigidArea(new Dimension(0, 8)));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> eventList = new JList<>(listModel);
        eventList.setFont(UITheme.BODY_FONT);
        eventList.setFixedCellHeight(32);
        eventList.setBorder(new EmptyBorder(4, 10, 4, 10));
        JScrollPane listScroll = new JScrollPane(eventList);
        listScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        listScroll.setPreferredSize(new Dimension(420, 130));
        listScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        listScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        body.add(listScroll);

        java.util.List<Event>[] assignedHolder = new java.util.List[]{ venueService.getEventsForVenue(venue.getVenueId()) };
        Runnable refreshList = () -> {
            assignedHolder[0] = venueService.getEventsForVenue(venue.getVenueId());
            listModel.clear();
            if (assignedHolder[0].isEmpty()) {
                listModel.addElement("No events currently assigned.");
            } else {
                for (Event ev : assignedHolder[0]) {
                    listModel.addElement("#" + ev.getEventId() + "   " + ev.getEventName());
                }
            }
        };
        refreshList.run();

        JButton unassignButton = UIComponents.dangerButton("Unassign Selected");
        unassignButton.addActionListener(e -> {
            int idx = eventList.getSelectedIndex();
            if (idx == -1 || assignedHolder[0].isEmpty()) return;
            Event ev = assignedHolder[0].get(idx);
            venueService.unassignVenueFromEvent(ev.getEventId());
            refreshList.run();
            loadVenues();
        });
        unassignButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(unassignButton);

        body.add(Box.createRigidArea(new Dimension(0, 22)));
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        body.add(sep);
        body.add(Box.createRigidArea(new Dimension(0, 18)));

        JLabel newLabel = UIComponents.formLabel("ASSIGN TO A NEW EVENT");
        newLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(newLabel);
        body.add(Box.createRigidArea(new Dimension(0, 10)));

        JTextField eventIdField = new JTextField();
        UIComponents.styleTextField(eventIdField);
        body.add(buildFieldRow("Event ID", eventIdField));

        List<Hall> halls = venueService.getHallsByVenue(venue.getVenueId());
        String[] hallOptions = new String[halls.size() + 1];
        hallOptions[0] = "None";
        for (int i = 0; i < halls.size(); i++) {
            Hall h = halls.get(i);
            String number = (h.getHallNumber() == null || h.getHallNumber().isBlank()) ? h.getHallName() : h.getHallNumber();
            hallOptions[i + 1] = number + " — " + h.getHallName();
        }
        JComboBox<String> hallBox = new JComboBox<>(hallOptions);
        hallBox.setFont(UITheme.BODY_FONT);
        body.add(buildFieldRow("Hall (optional)", hallBox));

        JButton assignButton = UIComponents.primaryButton("Assign to Event");
        assignButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        assignButton.addActionListener(e -> {
            int eventId;
            try {
                eventId = Integer.parseInt(eventIdField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Event ID must be a number.");
                return;
            }
            Integer hallId = null;
            int selectedIndex = hallBox.getSelectedIndex();
            if (selectedIndex > 0) hallId = halls.get(selectedIndex - 1).getHallId();

            String eventName = venueService.assignVenueToEvent(eventId, venue.getVenueId(), hallId);
            if (eventName != null) {
                JOptionPane.showMessageDialog(dialog, "Assigned to \"" + eventName + "\".");
                eventIdField.setText("");
                refreshList.run();
                loadVenues();
            } else {
                JOptionPane.showMessageDialog(dialog, "No event found with ID " + eventId + ".", "Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        body.add(assignButton);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.getViewport().setOpaque(false);
        bodyScroll.setOpaque(false);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(14);

        JButton closeButton = UIComponents.secondaryButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 24, 20, 24));
        footer.add(closeButton);

        dialog.add(bodyScroll, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ---------------------------------------------------------------
    // Manage Assignment — Hall (same polish, scoped to one hall)
    // ---------------------------------------------------------------
    private void openManageHallAssignmentDialog() {
        Hall hall = getSelectedHall();
        if (hall == null) return;
        String hallLabel = (hall.getHallNumber() == null || hall.getHallNumber().isBlank()) ? hall.getHallName() : hall.getHallNumber() + " — " + hall.getHallName();

        JDialog dialog = buildStyledDialog("Manage Assignment", null, 480, 500);
        dialog.add(buildDialogHeader("Manage Assignment", hallLabel), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(4, 24, 4, 24));

        JLabel currentLabel = UIComponents.formLabel("CURRENTLY ASSIGNED EVENTS");
        currentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(currentLabel);
        body.add(Box.createRigidArea(new Dimension(0, 8)));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> eventList = new JList<>(listModel);
        eventList.setFont(UITheme.BODY_FONT);
        eventList.setFixedCellHeight(32);
        eventList.setBorder(new EmptyBorder(4, 10, 4, 10));
        JScrollPane listScroll = new JScrollPane(eventList);
        listScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        listScroll.setPreferredSize(new Dimension(420, 130));
        listScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        listScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        body.add(listScroll);

        java.util.List<Event>[] assignedHolder = new java.util.List[]{ venueService.getEventsForHall(hall.getHallId()) };
        Runnable refreshList = () -> {
            assignedHolder[0] = venueService.getEventsForHall(hall.getHallId());
            listModel.clear();
            if (assignedHolder[0].isEmpty()) {
                listModel.addElement("No events currently assigned.");
            } else {
                for (Event ev : assignedHolder[0]) {
                    listModel.addElement("#" + ev.getEventId() + "   " + ev.getEventName());
                }
            }
        };
        refreshList.run();

        JButton unassignButton = UIComponents.dangerButton("Unassign Selected");
        unassignButton.addActionListener(e -> {
            int idx = eventList.getSelectedIndex();
            if (idx == -1 || assignedHolder[0].isEmpty()) return;
            Event ev = assignedHolder[0].get(idx);
            venueService.unassignHallFromEvent(ev.getEventId());
            refreshList.run();
            loadHalls();
        });
        unassignButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(unassignButton);

        body.add(Box.createRigidArea(new Dimension(0, 22)));
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        body.add(sep);
        body.add(Box.createRigidArea(new Dimension(0, 18)));

        JLabel newLabel = UIComponents.formLabel("ASSIGN TO A NEW EVENT");
        newLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(newLabel);
        body.add(Box.createRigidArea(new Dimension(0, 10)));

        JTextField eventIdField = new JTextField();
        UIComponents.styleTextField(eventIdField);
        body.add(buildFieldRow("Event ID", eventIdField));

        JButton assignButton = UIComponents.primaryButton("Assign to Event");
        assignButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        assignButton.addActionListener(e -> {
            int eventId;
            try {
                eventId = Integer.parseInt(eventIdField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Event ID must be a number.");
                return;
            }
            String eventName = venueService.assignHallToEvent(eventId, hall);
            if (eventName != null) {
                JOptionPane.showMessageDialog(dialog, "Assigned to \"" + eventName + "\".");
                eventIdField.setText("");
                refreshList.run();
                loadHalls();
            } else {
                JOptionPane.showMessageDialog(dialog, "No event found with ID " + eventId + ".", "Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        body.add(assignButton);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.getViewport().setOpaque(false);
        bodyScroll.setOpaque(false);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(14);

        JButton closeButton = UIComponents.secondaryButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 24, 20, 24));
        footer.add(closeButton);

        dialog.add(bodyScroll, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}