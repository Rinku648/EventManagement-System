package com.eventmanagement.ui;

import com.eventmanagement.model.Attendee;
import com.eventmanagement.model.Booking;
import com.eventmanagement.dao.AttendeeDAO;
import com.eventmanagement.service.BookingService;
import com.eventmanagement.service.TicketService;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class BookingPanel extends JPanel {
    private final BookingService bookingService = new BookingService();
    private final AttendeeDAO attendeeDAO = new AttendeeDAO();
    private final TicketService ticketService = new TicketService();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;

    public BookingPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JButton newBookingButton = UIComponents.pillButton("+  New Booking", Color.WHITE, UITheme.PRIMARY, true);
        newBookingButton.addActionListener(e -> openNewBookingDialog());
        JButton confirmButton = UIComponents.pillButton("Confirm Selected", new Color(255, 255, 255, 40), Color.WHITE, true);
        confirmButton.addActionListener(e -> confirmSelected());
        JButton issueTicketButton = UIComponents.pillButton("Issue Ticket", new Color(255, 255, 255, 40), Color.WHITE, true);
        issueTicketButton.addActionListener(e -> issueTicketForSelected());
        JButton editButton = UIComponents.pillButton("Edit", new Color(255, 255, 255, 40), Color.WHITE, true);
        editButton.addActionListener(e -> editSelected());
        JButton deleteButton = UIComponents.pillButton("Delete", new Color(255, 255, 255, 40), Color.WHITE, true);
        deleteButton.addActionListener(e -> deleteSelected());
        JButton refreshButton = UIComponents.pillButton("Refresh", new Color(255, 255, 255, 40), Color.WHITE, true);
        refreshButton.addActionListener(e -> loadBookings());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(refreshButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(issueTicketButton);
        actions.add(confirmButton);
        actions.add(newBookingButton);

        add(UIComponents.panelBanner("Bookings", "Create bookings, confirm them, and issue tickets", actions), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Booking ID", "Attendee Name", "Event ID", "Ticket Type ID", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        RoundedPanel tableCard = UIComponents.tableCard(table);
        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        searchField = new JTextField();
        UIComponents.styleTextField(searchField);
        searchField.putClientProperty("JTextField.placeholderText", "Search by attendee name...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        searchWrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JLabel searchIcon = new JLabel("Search:");
        searchIcon.setFont(UITheme.LABEL_BOLD_FONT);
        searchIcon.setForeground(UITheme.TEXT_MUTED);
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        searchWrap.add(searchIcon, BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);

        centerWrap.add(searchWrap, BorderLayout.NORTH);
        centerWrap.add(tableCard, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        loadBookings();
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1));
        }
    }

    private void loadBookings() {
        tableModel.setRowCount(0);
        List<Booking> bookings = bookingService.getAllBookings();
        for (Booking b : bookings) {
            tableModel.addRow(new Object[]{b.getBookingId(), b.getAttendeeName(), b.getEventId(), b.getTicketTypeId(), b.getStatus()});
        }
    }

    private int getSelectedBookingId() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return -1;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (int) tableModel.getValueAt(modelRow, 0);
    }

    private void confirmSelected() {
        int bookingId = getSelectedBookingId();
        if (bookingId == -1) { JOptionPane.showMessageDialog(this, "Select a booking first."); return; }
        bookingService.confirmBooking(bookingId);
        loadBookings();
    }

    private void issueTicketForSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Select a booking first."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);

        int bookingId = (int) tableModel.getValueAt(modelRow, 0);
        String status = (String) tableModel.getValueAt(modelRow, 4);
        if (!"CONFIRMED".equals(status)) {
            JOptionPane.showMessageDialog(this, "Booking must be CONFIRMED before issuing a ticket.");
            return;
        }

        try {
            String pdfPath = ticketService.issueTicket(bookingId);
            JOptionPane.showMessageDialog(this,
                    "Ticket issued.\nQR payload to use for Check-In: BOOKING-" + bookingId +
                            "\nPDF saved at: " + pdfPath);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to issue ticket: " + ex.getMessage());
        }
    }

    private void editSelected() {
        int bookingId = getSelectedBookingId();
        if (bookingId == -1) { JOptionPane.showMessageDialog(this, "Select a booking first."); return; }

        Booking existing = bookingService.getById(bookingId);
        if (existing == null) { JOptionPane.showMessageDialog(this, "Booking not found."); return; }

        JTextField attendeeNameField = new JTextField(existing.getAttendeeName());
        JTextField eventIdField = new JTextField(String.valueOf(existing.getEventId()));
        JTextField ticketTypeIdField = new JTextField(String.valueOf(existing.getTicketTypeId()));
        UIComponents.styleTextField(attendeeNameField);
        UIComponents.styleTextField(eventIdField);
        UIComponents.styleTextField(ticketTypeIdField);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(UIComponents.formLabel("Attendee Full Name")); form.add(attendeeNameField);
        form.add(Box.createRigidArea(new Dimension(0, 12)));
        form.add(UIComponents.formLabel("Event ID")); form.add(eventIdField);
        form.add(Box.createRigidArea(new Dimension(0, 12)));
        form.add(UIComponents.formLabel("Ticket Type ID")); form.add(ticketTypeIdField);

        int result = JOptionPane.showConfirmDialog(this, form, "Edit Booking", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String newName = attendeeNameField.getText().trim();
                int newEventId = Integer.parseInt(eventIdField.getText().trim());
                int newTicketTypeId = Integer.parseInt(ticketTypeIdField.getText().trim());
                bookingService.updateBooking(bookingId, existing.getAttendeeId(), newName, newEventId, newTicketTypeId);
                loadBookings();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Event ID and Ticket Type ID must be numbers.");
            }
        }
    }

    private void deleteSelected() {
        int bookingId = getSelectedBookingId();
        if (bookingId == -1) { JOptionPane.showMessageDialog(this, "Select a booking first."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete booking #" + bookingId + "? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = bookingService.deleteBooking(bookingId);
            if (success) {
                loadBookings();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not delete booking #" + bookingId + ".",
                        "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openNewBookingDialog() {
        JTextField attendeeNameField = new JTextField();
        JTextField eventIdField = new JTextField();
        JTextField ticketTypeIdField = new JTextField();
        UIComponents.styleTextField(attendeeNameField);
        UIComponents.styleTextField(eventIdField);
        UIComponents.styleTextField(ticketTypeIdField);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(UIComponents.formLabel("Attendee Full Name")); form.add(attendeeNameField);
        form.add(Box.createRigidArea(new Dimension(0, 12)));
        form.add(UIComponents.formLabel("Event ID")); form.add(eventIdField);
        form.add(Box.createRigidArea(new Dimension(0, 12)));
        form.add(UIComponents.formLabel("Ticket Type ID")); form.add(ticketTypeIdField);

        int result = JOptionPane.showConfirmDialog(this, form, "New Booking", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Attendee attendee = new Attendee();
            attendee.setFullName(attendeeNameField.getText().trim());
            int attendeeId = attendeeDAO.createAttendee(attendee);

            Booking booking = new Booking();
            booking.setAttendeeId(attendeeId);
            booking.setEventId(Integer.parseInt(eventIdField.getText().trim()));
            booking.setTicketTypeId(Integer.parseInt(ticketTypeIdField.getText().trim()));
            bookingService.createBooking(booking);
            loadBookings();
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