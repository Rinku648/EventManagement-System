package com.eventmanagement.ui;

import com.eventmanagement.model.Vendor;
import com.eventmanagement.model.EventVendor;
import com.eventmanagement.model.Event;
import com.eventmanagement.service.VendorService;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;
import com.eventmanagement.ui.components.UIComponents.SoftBadge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class VendorPanel extends JPanel {
    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    private static final int COL_CATEGORY = 2;
    private static final int COL_PHONE = 3;
    private static final int COL_COST = 4;
    private static final int COL_ASSIGNED = 5;
    private static final int COL_STATUS = 6;

    private final VendorService vendorService = new VendorService();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> rowSorter;

    private JTextField searchField;
    private JLabel searchResultLabel;

    private JButton editButton;
    private JButton manageAssignmentButton;
    private JButton deleteButton;

    public VendorPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JButton addButton = UIComponents.pillButton("+  Add Vendor", Color.WHITE, UITheme.PRIMARY, true);
        addButton.addActionListener(e -> openVendorFormDialog(null));
        JButton refreshButton = UIComponents.pillButton("Refresh", new Color(255, 255, 255, 40), Color.WHITE, true);
        refreshButton.addActionListener(e -> { loadVendors(); clearSearch(); });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(refreshButton);
        actions.add(addButton);

        add(UIComponents.panelBanner("Vendors", "Manage vendors and assign them to events", actions), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Vendor Name", "Category", "Contact Phone", "Quoted Cost", "Assigned Events", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(new AssignmentStatusRenderer());
        table.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        // Default auto-resize (AUTO_RESIZE_SUBSEQUENT_COLUMNS) so columns
        // stretch to fill the full table card width — no leftover empty
        // space on the right.
        table.getColumnModel().getColumn(COL_ID).setPreferredWidth(60);
        table.getColumnModel().getColumn(COL_NAME).setPreferredWidth(200);
        table.getColumnModel().getColumn(COL_CATEGORY).setPreferredWidth(170);
        table.getColumnModel().getColumn(COL_PHONE).setPreferredWidth(170);
        table.getColumnModel().getColumn(COL_COST).setPreferredWidth(130);
        table.getColumnModel().getColumn(COL_ASSIGNED).setPreferredWidth(340);
        table.getColumnModel().getColumn(COL_STATUS).setPreferredWidth(150);

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        // Table card with scrollbars forced ALWAYS visible (not just
        // on-demand), so the dashboard consistently looks scrollable
        // even when the current row count happens to fit on screen.
        RoundedPanel tableCard = new RoundedPanel(UITheme.RADIUS_MD, UITheme.SURFACE, UITheme.BORDER);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane tableScroll = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(UITheme.SURFACE);
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);
        tableScroll.getHorizontalScrollBar().setUnitIncrement(16);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        editButton = UIComponents.secondaryButton("Edit");
        editButton.addActionListener(e -> openEditSelectedVendor());
        manageAssignmentButton = UIComponents.pillButton("Manage Assignment", UITheme.INFO, Color.WHITE, true);
        manageAssignmentButton.addActionListener(e -> openManageAssignmentDialog());
        deleteButton = UIComponents.dangerButton("Delete Vendor");
        deleteButton.addActionListener(e -> deleteSelectedVendor());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        toolbar.add(editButton);
        toolbar.add(manageAssignmentButton);
        toolbar.add(deleteButton);
        updateButtonStates();

        JPanel searchBar = buildSearchBar();

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        JPanel topStack = new JPanel();
        topStack.setOpaque(false);
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        topStack.add(toolbar);
        topStack.add(Box.createRigidArea(new Dimension(0, 12)));
        topStack.add(searchBar);

        centerWrap.add(topStack, BorderLayout.NORTH);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        tableWrap.add(tableCard, BorderLayout.CENTER);
        centerWrap.add(tableWrap, BorderLayout.CENTER);

        add(centerWrap, BorderLayout.CENTER);

        loadVendors();
    }

    // ---------------------------------------------------------------
    // Search bar
    // ---------------------------------------------------------------
    private JPanel buildSearchBar() {
        RoundedPanel bar = new RoundedPanel(UITheme.RADIUS_MD, UITheme.SURFACE, UITheme.BORDER);
        bar.setLayout(new BorderLayout(10, 0));
        bar.setBorder(new EmptyBorder(14, 16, 14, 16));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        bar.setPreferredSize(new Dimension(100, 68));

        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setOpaque(false);

        searchField = new JTextField();
        UIComponents.styleTextField(searchField);
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        searchField.putClientProperty("JTextField.placeholderText", "Search by event name (e.g. Diwali Nights)");
        searchField.addActionListener(e -> performSearch());

        JButton searchButton = UIComponents.primaryButton("Search");
        searchButton.addActionListener(e -> performSearch());
        JButton clearButton = UIComponents.secondaryButton("Clear");
        clearButton.addActionListener(e -> clearSearch());

        JPanel buttonPair = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPair.setOpaque(false);
        buttonPair.add(searchButton);
        buttonPair.add(clearButton);

        inputRow.add(searchField, BorderLayout.CENTER);
        inputRow.add(buttonPair, BorderLayout.EAST);

        searchResultLabel = new JLabel(" ");
        searchResultLabel.setFont(UITheme.LABEL_FONT);
        searchResultLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        inputRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchResultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(inputRow);
        stack.add(Box.createRigidArea(new Dimension(0, 4)));
        stack.add(searchResultLabel);

        bar.add(stack, BorderLayout.CENTER);
        return bar;
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            clearSearch();
            return;
        }
        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(query), COL_ASSIGNED));

        int matches = table.getRowCount();
        if (matches == 0) {
            searchResultLabel.setText("No vendors are assigned to an event matching \"" + query + "\".");
            searchResultLabel.setForeground(UITheme.DANGER);
        } else {
            searchResultLabel.setText(matches + " vendor(s) assigned to an event matching \"" + query + "\".");
            searchResultLabel.setForeground(UITheme.SUCCESS.darker());
        }
    }

    private void clearSearch() {
        searchField.setText("");
        rowSorter.setRowFilter(null);
        searchResultLabel.setText(" ");
    }

    // ---------------------------------------------------------------
    // Data loading / selection helpers
    // ---------------------------------------------------------------
    private void updateButtonStates() {
        boolean hasSelection = table.getSelectedRow() != -1;
        editButton.setEnabled(hasSelection);
        manageAssignmentButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }

    private Vendor getSelectedVendor() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        int vendorId = (int) tableModel.getValueAt(modelRow, COL_ID);
        for (Vendor v : vendorService.getAllVendors()) {
            if (v.getVendorId() == vendorId) return v;
        }
        return null;
    }

    private void loadVendors() {
        tableModel.setRowCount(0);
        List<Vendor> vendors = vendorService.getAllVendors();
        for (Vendor v : vendors) {
            List<String> assigned = vendorService.getAssignedEventNamesForVendor(v.getVendorId());
            String assignedText = assigned.isEmpty() ? "—" : String.join(", ", assigned);
            String status = assigned.isEmpty() ? "UNASSIGNED" : "ASSIGNED";
            String phone = (v.getContactPhone() == null || v.getContactPhone().isBlank()) ? "—" : v.getContactPhone();
            tableModel.addRow(new Object[]{
                    v.getVendorId(), v.getVendorName(), v.getCategory(), phone,
                    v.getQuotedCost(), assignedText, status
            });
        }
        updateButtonStates();
    }

    // ---------------------------------------------------------------
    // Shared dialog chrome
    // ---------------------------------------------------------------
    private JDialog buildStyledDialog(String title, int width, int height) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this) instanceof Frame f ? f : null, true);
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
        }
        row.add(label);
        row.add(Box.createRigidArea(new Dimension(0, 5)));
        row.add(field);
        row.add(Box.createRigidArea(new Dimension(0, 14)));
        return row;
    }

    // ---------------------------------------------------------------
    // Add / Edit / Delete Vendor
    // ---------------------------------------------------------------
    private void openEditSelectedVendor() {
        Vendor vendor = getSelectedVendor();
        if (vendor == null) return;
        openVendorFormDialog(vendor);
    }

    private void openVendorFormDialog(Vendor existing) {
        boolean isEdit = existing != null;
        JDialog dialog = buildStyledDialog(isEdit ? "Edit Vendor" : "Add Vendor", 420, 460);

        JTextField nameField = new JTextField(isEdit ? existing.getVendorName() : "");
        JTextField categoryField = new JTextField(isEdit ? existing.getCategory() : "");
        JTextField phoneField = new JTextField(isEdit ? existing.getContactPhone() : "");
        JTextField costField = new JTextField(isEdit ? String.valueOf(existing.getQuotedCost()) : "");
        UIComponents.styleTextField(nameField);
        UIComponents.styleTextField(categoryField);
        UIComponents.styleTextField(phoneField);
        UIComponents.styleTextField(costField);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(4, 24, 4, 24));
        body.add(buildFieldRow("Vendor Name", nameField));
        body.add(buildFieldRow("Category", categoryField));
        body.add(buildFieldRow("Contact Phone", phoneField));
        body.add(buildFieldRow("Quoted Cost", costField));

        JButton saveButton = UIComponents.primaryButton(isEdit ? "Save Changes" : "Add Vendor");
        JButton cancelButton = UIComponents.secondaryButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        saveButton.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vendor name is required.");
                return;
            }
            double cost;
            try {
                cost = Double.parseDouble(costField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Quoted Cost must be a number.");
                return;
            }

            if (isEdit) {
                existing.setVendorName(nameField.getText().trim());
                existing.setCategory(categoryField.getText().trim());
                existing.setContactPhone(phoneField.getText().trim());
                existing.setQuotedCost(cost);
                if (vendorService.updateVendor(existing)) {
                    loadVendors();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update vendor.");
                }
            } else {
                Vendor v = new Vendor();
                v.setVendorName(nameField.getText().trim());
                v.setCategory(categoryField.getText().trim());
                v.setContactPhone(phoneField.getText().trim());
                v.setQuotedCost(cost);
                if (vendorService.createVendor(v)) {
                    loadVendors();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add vendor.");
                }
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 24, 20, 24));
        footer.add(cancelButton);
        footer.add(saveButton);

        dialog.add(buildDialogHeader(isEdit ? "Edit Vendor" : "Add Vendor", isEdit ? existing.getVendorName() : "Add a new vendor"), BorderLayout.NORTH);
        dialog.add(body, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedVendor() {
        Vendor vendor = getSelectedVendor();
        if (vendor == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + vendor.getVendorName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (vendorService.deleteVendor(vendor.getVendorId())) {
                loadVendors();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not delete this vendor. It may still be assigned to events. " +
                                "Unassign it from all events first (use Manage Assignment).",
                        "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------------------------------------------------------
    // Manage Assignment
    // ---------------------------------------------------------------
    private void openManageAssignmentDialog() {
        Vendor vendor = getSelectedVendor();
        if (vendor == null) return;

        JDialog dialog = buildStyledDialog("Manage Assignment", 480, 500);
        dialog.add(buildDialogHeader("Manage Assignment", vendor.getVendorName()), BorderLayout.NORTH);

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

        java.util.List<Event>[] assignedHolder = new java.util.List[]{ vendorService.getEventsForVendor(vendor.getVendorId()) };
        Runnable refreshList = () -> {
            assignedHolder[0] = vendorService.getEventsForVendor(vendor.getVendorId());
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
            vendorService.unassignVendorFromEvent(ev.getEventId(), vendor.getVendorId());
            refreshList.run();
            loadVendors();
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
            EventVendor ev = new EventVendor();
            ev.setEventId(eventId);
            ev.setVendorId(vendor.getVendorId());
            ev.setPaymentStatus("UNPAID");
            boolean ok = vendorService.assignVendorToEvent(ev);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Vendor assigned to event #" + eventId + ".");
                eventIdField.setText("");
                refreshList.run();
                loadVendors();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Could not assign — check the Event ID exists and isn't already assigned to this vendor.",
                        "Failed", JOptionPane.ERROR_MESSAGE);
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

    private class AssignmentStatusRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String status = value == null ? "" : value.toString();
            Color dot = status.equals("ASSIGNED") ? UITheme.SUCCESS : UITheme.NEUTRAL;
            Color tint = status.equals("ASSIGNED") ? UITheme.SUCCESS_LIGHT : UITheme.NEUTRAL_LIGHT;

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            wrapper.setOpaque(true);
            wrapper.setBackground(isSelected ? UITheme.PRIMARY_LIGHT : (row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC)));
            wrapper.add(new SoftBadge(status, dot, tint));
            return wrapper;
        }
    }
}