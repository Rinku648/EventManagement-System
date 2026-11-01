package com.eventmanagement.ui;

import com.eventmanagement.model.Payment;
import com.eventmanagement.service.PaymentService;
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
import java.util.ArrayList;
import java.util.List;

public class PaymentPanel extends JPanel {
    private final PaymentService paymentService = new PaymentService();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;
    private List<Payment> currentPayments = new ArrayList<>();

    private static final String[] STATUS_OPTIONS = {"PENDING", "SUCCESS", "FAILED"};
    private static final int FORM_WIDTH = 320;

    public PaymentPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JButton payButton = UIComponents.pillButton("+  Simulate Payment", Color.WHITE, UITheme.PRIMARY, true);
        payButton.addActionListener(e -> openPaymentDialog());
        JButton editButton = UIComponents.pillButton("Edit", new Color(255, 255, 255, 40), Color.WHITE, true);
        editButton.addActionListener(e -> editSelected());
        JButton deleteButton = UIComponents.pillButton("Delete", new Color(255, 255, 255, 40), Color.WHITE, true);
        deleteButton.addActionListener(e -> deleteSelected());
        JButton refreshButton = UIComponents.pillButton("Refresh", new Color(255, 255, 255, 40), Color.WHITE, true);
        refreshButton.addActionListener(e -> loadPayments());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(refreshButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(payButton);

        add(UIComponents.panelBanner("Payments", "Track and simulate transactions per booking", actions), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Payment ID", "Booking ID", "Transaction ID", "Amount", "Status"}, 0) {
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
        searchField.putClientProperty("JTextField.placeholderText", "Search by Booking ID...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        searchWrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(UITheme.LABEL_BOLD_FONT);
        searchLabel.setForeground(UITheme.TEXT_MUTED);
        searchLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        searchWrap.add(searchLabel, BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);

        centerWrap.add(searchWrap, BorderLayout.NORTH);
        centerWrap.add(tableCard, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        loadPayments();
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1));
        }
    }

    private void loadPayments() {
        tableModel.setRowCount(0);
        currentPayments = paymentService.getAllPayments();
        for (Payment p : currentPayments) {
            Object paymentIdCell = p.getPaymentId() == null ? "-" : p.getPaymentId();
            Object amountCell = p.getAmount() == null ? "-" : p.getAmount();
            Object txnCell = p.getTransactionId() == null ? "-" : p.getTransactionId();
            tableModel.addRow(new Object[]{paymentIdCell, p.getBookingId(), txnCell, amountCell, p.getStatus()});
        }
    }

    private Payment getSelectedPayment() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return currentPayments.get(modelRow);
    }

    // ---------------------------------------------------------------
    // Form-building helpers: force left alignment + a fixed stretch
    // width so labels/fields/combo boxes line up instead of each
    // centering at its own tiny preferred size.
    // ---------------------------------------------------------------
    private JLabel buildLabel(String text) {
        JLabel label = UIComponents.formLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
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

    /** IMPORTANT: do not call setPreferredSize on this panel itself —
     *  it has no children yet at creation time, so that would lock it
     *  to a near-zero height and clip everything added afterward.
     *  Let BoxLayout compute the size naturally from its children. */
    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return form;
    }

    private void openPaymentDialog() {
        JTextField bookingIdField = new JTextField();
        JTextField amountField = new JTextField();
        JComboBox<String> statusBox = new JComboBox<>(STATUS_OPTIONS);
        statusBox.setSelectedItem("SUCCESS");
        UIComponents.styleTextField(bookingIdField);
        UIComponents.styleTextField(amountField);
        stretchField(bookingIdField);
        stretchField(amountField);
        stretchField(statusBox);

        JPanel form = buildForm();
        form.add(buildLabel("Booking ID"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(bookingIdField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Amount"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(amountField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Status"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(statusBox);

        int result = JOptionPane.showConfirmDialog(this, form, "Simulate Payment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int bookingId = Integer.parseInt(bookingIdField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());
                String status = (String) statusBox.getSelectedItem();
                paymentService.recordPayment(bookingId, amount, status);
                loadPayments();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Booking ID and Amount must be numbers.");
            }
        }
    }

    private void editSelected() {
        Payment selected = getSelectedPayment();
        if (selected == null) { JOptionPane.showMessageDialog(this, "Select a payment row first."); return; }

        JTextField amountField = new JTextField(selected.getAmount() == null ? "" : String.valueOf(selected.getAmount()));
        JComboBox<String> statusBox = new JComboBox<>(STATUS_OPTIONS);
        statusBox.setSelectedItem(selected.getStatus() != null ? selected.getStatus() : "PENDING");
        UIComponents.styleTextField(amountField);
        stretchField(amountField);
        stretchField(statusBox);

        JLabel bookingIdInfo = buildLabel("Booking ID: " + selected.getBookingId());

        JPanel form = buildForm();
        form.add(bookingIdInfo);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Amount"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(amountField);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(buildLabel("Status"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(statusBox);

        String title = selected.getPaymentId() == null ? "Record Payment" : "Edit Payment";
        int result = JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                String status = (String) statusBox.getSelectedItem();

                if (selected.getPaymentId() == null) {
                    paymentService.recordPayment(selected.getBookingId(), amount, status);
                } else {
                    paymentService.updatePayment(selected.getPaymentId(), selected.getTransactionId(), amount, status);
                }
                loadPayments();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Amount must be a number.");
            }
        }
    }

    private void deleteSelected() {
        Payment selected = getSelectedPayment();
        if (selected == null) { JOptionPane.showMessageDialog(this, "Select a payment row first."); return; }

        if (selected.getPaymentId() == null) {
            JOptionPane.showMessageDialog(this, "This booking has no payment recorded yet — nothing to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete payment #" + selected.getPaymentId() + " for booking #" + selected.getBookingId() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = paymentService.deletePayment(selected.getPaymentId());
            if (success) {
                loadPayments();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete this payment.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
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