package com.eventmanagement.ui;

import com.eventmanagement.model.Ticket;
import com.eventmanagement.service.TicketService;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;
import com.eventmanagement.ui.components.UIComponents.SoftBadge;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class CheckInPanel extends JPanel {
    private static final int CARD_WIDTH = 520;

    private final TicketService ticketService = new TicketService();

    private JTextField payloadField;
    private RoundedPanel resultBanner;
    private JLabel resultLabel;

    private JTextField searchField;
    private JLabel searchResultLabel;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton undoButton;

    public CheckInPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        add(UIComponents.panelBanner("Check-In", "Scan or enter a ticket QR payload to check an attendee in", null), BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel checkInCard = buildCheckInCard();
        checkInCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        stack.add(checkInCard);
        stack.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel allCheckInsCard = buildAllCheckInsCard();
        allCheckInsCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        stack.add(allCheckInsCard);

        centerWrap.add(stack);

        JScrollPane outerScroll = new JScrollPane(centerWrap);
        outerScroll.setBorder(BorderFactory.createEmptyBorder());
        outerScroll.getViewport().setOpaque(false);
        outerScroll.setOpaque(false);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(outerScroll, BorderLayout.CENTER);

        loadAllTickets();
    }

    // ---------------------------------------------------------------
    // Card 1: perform a check-in
    // ---------------------------------------------------------------
    private RoundedPanel buildCheckInCard() {
        RoundedPanel card = UIComponents.formCard();
        card.setPreferredSize(new Dimension(CARD_WIDTH, 320));
        card.setMaximumSize(new Dimension(CARD_WIDTH, 320));

        JLabel iconChip = new JLabel("\u2705", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SUCCESS_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconChip.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        iconChip.setPreferredSize(new Dimension(60, 60));
        iconChip.setMaximumSize(new Dimension(60, 60));
        iconChip.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Attendee Check-In");
        title.setFont(UITheme.SECTION_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Ticket QR payload (e.g. BOOKING-12)");
        hint.setFont(UITheme.SUBTITLE_FONT);
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        payloadField = new JTextField();
        UIComponents.styleTextField(payloadField);
        payloadField.setAlignmentX(Component.CENTER_ALIGNMENT);
        payloadField.setMaximumSize(new Dimension(320, 38));
        payloadField.setHorizontalAlignment(SwingConstants.CENTER);
        payloadField.addActionListener(e -> performCheckIn());

        JButton checkInButton = UIComponents.primaryButton("Check In");
        JButton refreshButton = UIComponents.secondaryButton("Refresh");
        refreshButton.addActionListener(e -> resetForm());
        checkInButton.addActionListener(e -> performCheckIn());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(checkInButton);
        buttonRow.add(refreshButton);
        buttonRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultBanner = new RoundedPanel(UITheme.RADIUS_SM, UITheme.NEUTRAL_LIGHT, null);
        resultBanner.setLayout(new BorderLayout());
        resultBanner.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        resultBanner.setMaximumSize(new Dimension(380, 46));
        resultBanner.setPreferredSize(new Dimension(380, 46));
        resultBanner.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultBanner.setVisible(false);

        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        resultLabel.setFont(UITheme.LABEL_BOLD_FONT);
        resultBanner.add(resultLabel, BorderLayout.CENTER);

        card.add(iconChip);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(hint);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(payloadField);
        card.add(Box.createRigidArea(new Dimension(0, 18)));
        card.add(buttonRow);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(resultBanner);

        return card;
    }

    // ---------------------------------------------------------------
    // Card 2: search + full check-in history, with undo
    // ---------------------------------------------------------------
    private RoundedPanel buildAllCheckInsCard() {
        RoundedPanel card = UIComponents.formCard();
        card.setPreferredSize(new Dimension(CARD_WIDTH, 460));
        card.setMaximumSize(new Dimension(CARD_WIDTH, 460));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(CARD_WIDTH - 64, 50));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("All Check-Ins");
        title.setFont(UITheme.SECTION_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Every ticket issued, checked-in first");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(title);
        titleBlock.add(Box.createRigidArea(new Dimension(0, 2)));
        titleBlock.add(subtitle);

        JButton listRefreshButton = UIComponents.secondaryButton("Refresh");
        listRefreshButton.addActionListener(e -> {
            loadAllTickets();
            searchResultLabel.setText(" ");
        });

        headerRow.add(titleBlock, BorderLayout.WEST);
        headerRow.add(listRefreshButton, BorderLayout.EAST);

        // Search row
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.setMaximumSize(new Dimension(CARD_WIDTH - 64, 40));

        searchField = new JTextField();
        UIComponents.styleTextField(searchField);
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        searchField.putClientProperty("JTextField.placeholderText", "Search booking (e.g. BOOKING-14)");
        searchField.addActionListener(e -> performSearch());

        JButton searchButton = UIComponents.primaryButton("Search");
        searchButton.addActionListener(e -> performSearch());

        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(searchButton, BorderLayout.EAST);

        searchResultLabel = new JLabel(" ");
        searchResultLabel.setFont(UITheme.LABEL_FONT);
        searchResultLabel.setForeground(UITheme.TEXT_MUTED);
        searchResultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Table
        tableModel = new DefaultTableModel(new Object[]{"Ticket ID", "Booking Payload", "Status", "Checked-In At"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);

        // Center-align plain text columns (Booking Payload, Checked-In At) to match the centered headers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(2).setCellRenderer(new CheckInStatusRenderer());

        // Hide Ticket ID (kept in the model for lookups) but give visible columns
        // explicit widths so "Checked-In At" has room and nothing gets cut off with "..."
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(140); // Booking Payload
        table.getColumnModel().getColumn(2).setPreferredWidth(140); // Status
        table.getColumnModel().getColumn(3).setPreferredWidth(176); // Checked-In At

        table.getSelectionModel().addListSelectionListener(e -> updateUndoButtonState());

        RoundedPanel tableCard = UIComponents.tableCard(table);
        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableCard.setPreferredSize(new Dimension(CARD_WIDTH - 64, 220));
        tableCard.setMaximumSize(new Dimension(CARD_WIDTH - 64, 220));

        undoButton = UIComponents.dangerButton("Undo Check-In");
        undoButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        undoButton.addActionListener(e -> performUndo());
        undoButton.setEnabled(false);

        card.add(headerRow);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(searchRow);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        searchResultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(searchResultLabel);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(tableCard);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(undoButton);

        return card;
    }

    // ---------------------------------------------------------------
    // Behavior
    // ---------------------------------------------------------------
    private void performCheckIn() {
        String payload = payloadField.getText().trim();
        if (payload.isEmpty()) {
            showResult("Enter a ticket QR payload first.", UITheme.WARNING, UITheme.WARNING_LIGHT);
            return;
        }

        String result = ticketService.checkIn(payload);
        String lower = result.toLowerCase();

        if (lower.contains("successful")) {
            showResult(result, UITheme.SUCCESS, UITheme.SUCCESS_LIGHT);
        } else if (lower.contains("already checked in")) {
            showResult(result, UITheme.WARNING, UITheme.WARNING_LIGHT);
        } else {
            showResult(result, UITheme.DANGER, UITheme.DANGER_LIGHT);
        }

        loadAllTickets();
    }

    private void resetForm() {
        payloadField.setText("");
        resultBanner.setVisible(false);
        resultLabel.setText(" ");
        payloadField.requestFocusInWindow();
        revalidate();
        repaint();
    }

    private void showResult(String message, Color textColor, Color bgColor) {
        resultLabel.setText(message);
        resultLabel.setForeground(textColor.darker());
        resultBanner.setBackground(bgColor);
        resultBanner.setVisible(true);
        revalidate();
        repaint();
    }

    private void loadAllTickets() {
        tableModel.setRowCount(0);
        List<Ticket> tickets = ticketService.getAllTickets();
        for (Ticket t : tickets) {
            String status = t.isCheckedIn() ? "CHECKED IN" : "NOT CHECKED IN";
            String time = t.getCheckedInAt() != null ? t.getCheckedInAt() : "—";
            tableModel.addRow(new Object[]{t.getTicketId(), t.getQrPayload(), status, time});
        }
        updateUndoButtonState();
    }

    private void performSearch() {
        String payload = searchField.getText().trim();
        if (payload.isEmpty()) {
            searchResultLabel.setText("Type a booking payload to search.");
            searchResultLabel.setForeground(UITheme.TEXT_MUTED);
            return;
        }

        Ticket ticket = ticketService.findByPayload(payload);
        if (ticket == null) {
            searchResultLabel.setText("No ticket found for \"" + payload + "\".");
            searchResultLabel.setForeground(UITheme.DANGER);
            return;
        }

        // Find and select the matching row so it's visibly highlighted in the table
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (payload.equalsIgnoreCase(tableModel.getValueAt(row, 1).toString())) {
                table.setRowSelectionInterval(row, row);
                table.scrollRectToVisible(table.getCellRect(row, 0, true));
                break;
            }
        }

        if (ticket.isCheckedIn()) {
            searchResultLabel.setText("\"" + payload + "\" is checked in — at " + ticket.getCheckedInAt() + ".");
            searchResultLabel.setForeground(UITheme.SUCCESS.darker());
        } else {
            searchResultLabel.setText("\"" + payload + "\" has NOT been checked in yet.");
            searchResultLabel.setForeground(UITheme.WARNING.darker());
        }
    }

    private void updateUndoButtonState() {
        int row = table.getSelectedRow();
        if (row == -1) {
            undoButton.setEnabled(false);
            return;
        }
        String status = tableModel.getValueAt(row, 2).toString();
        undoButton.setEnabled(status.equals("CHECKED IN"));
    }

    private void performUndo() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int ticketId = (int) tableModel.getValueAt(row, 0);
        String payload = tableModel.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Undo the check-in for \"" + payload + "\"? They will need to be scanned again to re-enter.",
                "Confirm Undo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (ticketService.undoCheckIn(ticketId)) {
                loadAllTickets();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to undo check-in.");
            }
        }
    }

    private class CheckInStatusRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String status = value == null ? "" : value.toString();
            Color dot = status.equals("CHECKED IN") ? UITheme.SUCCESS : UITheme.WARNING;
            Color tint = status.equals("CHECKED IN") ? UITheme.SUCCESS_LIGHT : UITheme.WARNING_LIGHT;

            // GridBagLayout with no constraints stretches its one child to fill the
            // wrapper and keeps it centered regardless of the cell's actual width —
            // FlowLayout only centers reliably when the container's width happens to
            // match the content's preferred width, which is why badges drifted off-center.
            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(true);
            wrapper.setBackground(isSelected ? UITheme.PRIMARY_LIGHT : (row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC)));
            wrapper.add(new SoftBadge(status, dot, tint), new GridBagConstraints());
            return wrapper;
        }
    }
}