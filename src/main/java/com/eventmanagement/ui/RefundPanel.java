package com.eventmanagement.ui;

import com.eventmanagement.model.Booking;
import com.eventmanagement.service.BookingService;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class RefundPanel extends JPanel {
    private final BookingService bookingService = new BookingService();
    private final DefaultTableModel tableModel;
    private final JTable table;

    public RefundPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JButton cancelButton = UIComponents.dangerButton("Cancel & Request Refund");
        cancelButton.addActionListener(e -> cancelSelected());
        JButton completeButton = UIComponents.pillButton("Mark Refunded", Color.WHITE, UITheme.PRIMARY, true);
        completeButton.addActionListener(e -> completeSelected());
        JButton refreshButton = UIComponents.pillButton("Refresh", new Color(255, 255, 255, 40), Color.WHITE, true);
        refreshButton.addActionListener(e -> loadBookings());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(refreshButton);
        actions.add(completeButton);
        actions.add(cancelButton);

        add(UIComponents.panelBanner("Refunds", "Cancel bookings and process refunds", actions), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Booking ID", "Event ID", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());

        RoundedPanel tableCard = UIComponents.tableCard(table);
        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        centerWrap.add(tableCard, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        loadBookings();
    }

    private void loadBookings() {
        tableModel.setRowCount(0);
        List<Booking> bookings = bookingService.getAllBookings();
        for (Booking b : bookings) {
            tableModel.addRow(new Object[]{b.getBookingId(), b.getEventId(), b.getStatus()});
        }
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a booking first."); return; }
        int bookingId = (int) tableModel.getValueAt(row, 0);
        bookingService.cancelBooking(bookingId);
        loadBookings();
    }

    private void completeSelected() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a booking first."); return; }
        int bookingId = (int) tableModel.getValueAt(row, 0);
        bookingService.completeRefund(bookingId);
        loadBookings();
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