package com.eventmanagement.ui;

import com.eventmanagement.model.Budget;
import com.eventmanagement.model.Expense;
import com.eventmanagement.model.ExpenseCategory;
import com.eventmanagement.service.BudgetService;
import com.eventmanagement.service.BudgetService.BudgetOverviewRow;
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
import java.text.NumberFormat;
import java.util.List;
import java.util.regex.Pattern;

public class BudgetPanel extends JPanel {
    private static final int COL_EVENT_ID = 0;
    private static final int COL_EVENT_NAME = 1;
    private static final int COL_PLANNED = 2;
    private static final int COL_REVENUE = 3;
    private static final int COL_SPENT = 4;
    private static final int COL_PROFIT_LOSS = 5;
    private static final int COL_STATUS = 6;

    private final BudgetService budgetService = new BudgetService();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> rowSorter;
    private List<BudgetOverviewRow> currentRows;

    private JTextField searchField;
    private JLabel searchResultLabel;

    private JButton editButton;
    private JButton logExpenseButton;
    private JButton viewExpensesButton;
    private JButton deleteButton;

    public BudgetPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JButton setBudgetButton = UIComponents.pillButton("+  Set Budget", Color.WHITE, UITheme.PRIMARY, true);
        setBudgetButton.addActionListener(e -> openSetBudgetDialog());
        JButton lookupButton = UIComponents.pillButton("Quick Lookup", new Color(255, 255, 255, 40), Color.WHITE, true);
        lookupButton.addActionListener(e -> openQuickLookupDialog());
        JButton refreshButton = UIComponents.pillButton("Refresh", new Color(255, 255, 255, 40), Color.WHITE, true);
        refreshButton.addActionListener(e -> { loadBudgets(); clearSearch(); });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(refreshButton);
        actions.add(lookupButton);
        actions.add(setBudgetButton);

        add(UIComponents.panelBanner("Budget", "Set planned budgets, log expenses, and track profit/loss", actions), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Event ID", "Event Name", "Planned Amount", "Ticket Revenue", "Total Expenses", "Profit / Loss", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(new StatusRenderer());
        table.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        // ALL_COLUMNS resize mode: columns proportionally fill the full table width,
        // so no empty gap appears after the last column. Horizontal scroll only kicks
        // in if the window becomes too narrow for the minimum widths below.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(COL_EVENT_ID).setMinWidth(70);
        table.getColumnModel().getColumn(COL_EVENT_NAME).setMinWidth(160);
        table.getColumnModel().getColumn(COL_PLANNED).setMinWidth(120);
        table.getColumnModel().getColumn(COL_REVENUE).setMinWidth(120);
        table.getColumnModel().getColumn(COL_SPENT).setMinWidth(120);
        table.getColumnModel().getColumn(COL_PROFIT_LOSS).setMinWidth(120);
        table.getColumnModel().getColumn(COL_STATUS).setMinWidth(130);

        RoundedPanel tableCard = new RoundedPanel(UITheme.RADIUS_MD, UITheme.SURFACE, UITheme.BORDER);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(6, 6, 6, 6));
        JScrollPane tableScroll = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(UITheme.SURFACE);
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        editButton = UIComponents.pillButton("Edit Budget", UITheme.ACCENT, Color.WHITE, true);
        editButton.addActionListener(e -> openEditBudgetDialog());
        logExpenseButton = UIComponents.pillButton("Log Expense", UITheme.INFO, Color.WHITE, true);
        logExpenseButton.addActionListener(e -> openLogExpenseDialog());
        viewExpensesButton = UIComponents.pillButton("View Expenses", UITheme.ACCENT_TEAL, Color.WHITE, true);
        viewExpensesButton.addActionListener(e -> openViewExpensesDialog());
        deleteButton = UIComponents.dangerButton("Delete Budget");
        deleteButton.addActionListener(e -> deleteSelectedBudget());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        toolbar.add(editButton);
        toolbar.add(logExpenseButton);
        toolbar.add(viewExpensesButton);
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
        tableWrap.setPreferredSize(new Dimension(100, 380));
        tableWrap.add(tableCard, BorderLayout.CENTER);
        centerWrap.add(tableWrap, BorderLayout.CENTER);

        add(centerWrap, BorderLayout.CENTER);

        loadBudgets();
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
        searchField.putClientProperty("JTextField.placeholderText", "Search by event name");
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
        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(query), COL_EVENT_NAME));
        int matches = table.getRowCount();
        if (matches == 0) {
            searchResultLabel.setText("No budgets found for an event matching \"" + query + "\".");
            searchResultLabel.setForeground(UITheme.DANGER);
        } else {
            searchResultLabel.setText(matches + " budget(s) matching \"" + query + "\".");
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
        logExpenseButton.setEnabled(hasSelection);
        viewExpensesButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }

    private BudgetOverviewRow getSelectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (currentRows == null || modelRow >= currentRows.size()) return null;
        return currentRows.get(modelRow);
    }

    private void loadBudgets() {
        tableModel.setRowCount(0);
        currentRows = budgetService.getBudgetOverview();
        NumberFormat fmt = NumberFormat.getNumberInstance();
        for (BudgetOverviewRow r : currentRows) {
            String status = r.profitLoss >= 0 ? "SURPLUS" : "OVER BUDGET";
            tableModel.addRow(new Object[]{
                    r.eventId, r.eventName, fmt.format(r.plannedAmount), fmt.format(r.ticketRevenue),
                    fmt.format(r.totalExpenses), (r.profitLoss >= 0 ? "+" : "") + fmt.format(r.profitLoss), status
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
    // Set Budget
    // ---------------------------------------------------------------
    private void openSetBudgetDialog() {
        JDialog dialog = buildStyledDialog("Set Budget", 420, 420);

        JTextField eventIdField = new JTextField();
        UIComponents.styleTextField(eventIdField);
        JLabel eventNameLabel = new JLabel(" ");
        eventNameLabel.setFont(UITheme.LABEL_FONT);
        eventNameLabel.setForeground(UITheme.TEXT_MUTED);
        eventNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        eventIdField.addActionListener(e -> updateEventNamePreview(eventIdField, eventNameLabel));
        eventIdField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { updateEventNamePreview(eventIdField, eventNameLabel); }
        });

        JTextField amountField = new JTextField();
        UIComponents.styleTextField(amountField);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(4, 24, 4, 24));
        body.add(buildFieldRow("Event ID", eventIdField));
        body.add(eventNameLabel);
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(buildFieldRow("Planned Amount", amountField));

        JButton saveButton = UIComponents.primaryButton("Set Budget");
        JButton cancelButton = UIComponents.secondaryButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        saveButton.addActionListener(e -> {
            int eventId;
            double amount;
            try {
                eventId = Integer.parseInt(eventIdField.getText().trim());
                amount = Double.parseDouble(amountField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Event ID and Planned Amount must be valid numbers.");
                return;
            }
            String eventName = budgetService.getEventName(eventId);
            if (eventName == null) {
                JOptionPane.showMessageDialog(dialog, "No event found with ID " + eventId + ".");
                return;
            }
            Budget b = new Budget();
            b.setEventId(eventId);
            b.setPlannedAmount(amount);
            if (budgetService.setBudget(b)) {
                loadBudgets();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to set budget.");
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 24, 20, 24));
        footer.add(cancelButton);
        footer.add(saveButton);

        dialog.add(buildDialogHeader("Set Budget", "Assign a planned amount to an event"), BorderLayout.NORTH);
        dialog.add(body, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updateEventNamePreview(JTextField eventIdField, JLabel eventNameLabel) {
        try {
            int id = Integer.parseInt(eventIdField.getText().trim());
            String name = budgetService.getEventName(id);
            eventNameLabel.setText(name != null ? "Event: " + name : "No event found with this ID.");
            eventNameLabel.setForeground(name != null ? UITheme.SUCCESS.darker() : UITheme.DANGER);
        } catch (NumberFormatException ignored) {
            eventNameLabel.setText(" ");
        }
    }

    // ---------------------------------------------------------------
    // Edit / Delete selected budget row
    // ---------------------------------------------------------------
    private void openEditBudgetDialog() {
        BudgetOverviewRow row = getSelectedRow();
        if (row == null) return;

        JDialog dialog = buildStyledDialog("Edit Budget", 400, 320);

        JTextField amountField = new JTextField(String.valueOf(row.plannedAmount));
        UIComponents.styleTextField(amountField);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(4, 24, 4, 24));
        body.add(buildFieldRow("Planned Amount", amountField));

        JButton saveButton = UIComponents.primaryButton("Save Changes");
        JButton cancelButton = UIComponents.secondaryButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        saveButton.addActionListener(e -> {
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Planned Amount must be a number.");
                return;
            }
            Budget b = new Budget();
            b.setBudgetId(row.budgetId);
            b.setEventId(row.eventId);
            b.setPlannedAmount(amount);
            if (budgetService.updateBudget(b)) {
                loadBudgets();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update budget.");
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 24, 20, 24));
        footer.add(cancelButton);
        footer.add(saveButton);

        dialog.add(buildDialogHeader("Edit Budget", row.eventName), BorderLayout.NORTH);
        dialog.add(body, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedBudget() {
        BudgetOverviewRow row = getSelectedRow();
        if (row == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete the budget entry for \"" + row.eventName + "\"?\nLogged expenses for this event will NOT be deleted.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (budgetService.deleteBudget(row.budgetId)) {
                loadBudgets();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete budget.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Log Expense
    // ---------------------------------------------------------------
    private void openLogExpenseDialog() {
        BudgetOverviewRow row = getSelectedRow();
        if (row == null) return;

        JDialog dialog = buildStyledDialog("Log Expense", 420, 460);

        List<ExpenseCategory> categories = budgetService.getAllCategories();
        JComboBox<String> categoryBox = new JComboBox<>();
        for (ExpenseCategory c : categories) categoryBox.addItem(c.getCategoryName());
        if (categories.isEmpty()) categoryBox.addItem("Miscellaneous");
        categoryBox.setFont(UITheme.BODY_FONT);

        JTextField amountField = new JTextField();
        UIComponents.styleTextField(amountField);
        JTextField descField = new JTextField();
        UIComponents.styleTextField(descField);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(4, 24, 4, 24));
        body.add(buildFieldRow("Category", categoryBox));
        body.add(buildFieldRow("Amount", amountField));
        body.add(buildFieldRow("Description", descField));

        JButton saveButton = UIComponents.primaryButton("Log Expense");
        JButton cancelButton = UIComponents.secondaryButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        saveButton.addActionListener(e -> {
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Amount must be a number.");
                return;
            }
            Expense expense = new Expense();
            expense.setEventId(row.eventId);
            int selectedIndex = categoryBox.getSelectedIndex();
            if (!categories.isEmpty() && selectedIndex >= 0) {
                expense.setExpenseCategoryId(categories.get(selectedIndex).getExpenseCategoryId());
            }
            expense.setAmount(amount);
            expense.setDescription(descField.getText().trim());

            if (budgetService.logExpense(expense)) {
                loadBudgets();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to log expense.");
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 24, 20, 24));
        footer.add(cancelButton);
        footer.add(saveButton);

        dialog.add(buildDialogHeader("Log Expense", row.eventName), BorderLayout.NORTH);
        dialog.add(body, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ---------------------------------------------------------------
    // View Expenses
    // ---------------------------------------------------------------
    private void openViewExpensesDialog() {
        BudgetOverviewRow row = getSelectedRow();
        if (row == null) return;

        JDialog dialog = buildStyledDialog("Expenses", 560, 540);
        dialog.add(buildDialogHeader("Expenses", row.eventName), BorderLayout.NORTH);

        List<ExpenseCategory> categories = budgetService.getAllCategories();

        DefaultTableModel expenseModel = new DefaultTableModel(new Object[]{"ID", "Category", "Amount", "Description"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable expenseTable = new JTable(expenseModel);
        UIComponents.styleTable(expenseTable);
        expenseTable.getColumnModel().getColumn(0).setMinWidth(0);
        expenseTable.getColumnModel().getColumn(0).setMaxWidth(0);

        Runnable refreshExpenses = () -> {
            expenseModel.setRowCount(0);
            for (Expense ex : budgetService.getExpenses(row.eventId)) {
                String catName = "—";
                if (ex.getExpenseCategoryId() != null) {
                    for (ExpenseCategory c : categories) {
                        if (c.getExpenseCategoryId() == ex.getExpenseCategoryId()) { catName = c.getCategoryName(); break; }
                    }
                }
                expenseModel.addRow(new Object[]{ex.getExpenseId(), catName, ex.getAmount(), ex.getDescription()});
            }
        };
        refreshExpenses.run();

        JScrollPane expenseScroll = new JScrollPane(expenseTable,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        expenseScroll.setPreferredSize(new Dimension(500, 320));

        JButton deleteExpenseButton = UIComponents.dangerButton("Delete Selected");
        deleteExpenseButton.addActionListener(e -> {
            int r = expenseTable.getSelectedRow();
            if (r == -1) return;
            int expenseId = (int) expenseModel.getValueAt(r, 0);
            int confirm = JOptionPane.showConfirmDialog(dialog, "Delete this expense entry?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                budgetService.deleteExpense(expenseId);
                refreshExpenses.run();
                loadBudgets();
            }
        });

        JPanel innerToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        innerToolbar.setOpaque(false);
        innerToolbar.add(deleteExpenseButton);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(4, 24, 4, 24));
        body.add(expenseScroll, BorderLayout.CENTER);
        body.add(innerToolbar, BorderLayout.SOUTH);

        JButton closeButton = UIComponents.secondaryButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 24, 20, 24));
        footer.add(closeButton);

        dialog.add(body, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ---------------------------------------------------------------
    // Quick Lookup
    // ---------------------------------------------------------------
    private void openQuickLookupDialog() {
        JDialog dialog = buildStyledDialog("Quick Profit/Loss Lookup", 420, 340);

        JTextField eventIdField = new JTextField();
        UIComponents.styleTextField(eventIdField);
        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(UITheme.SECTION_FONT);
        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel detailLabel = new JLabel(" ");
        detailLabel.setFont(UITheme.LABEL_FONT);
        detailLabel.setForeground(UITheme.TEXT_MUTED);
        detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(4, 24, 4, 24));
        body.add(buildFieldRow("Event ID", eventIdField));
        body.add(resultLabel);
        body.add(Box.createRigidArea(new Dimension(0, 6)));
        body.add(detailLabel);

        JButton lookupButton = UIComponents.primaryButton("Look Up");
        lookupButton.addActionListener(e -> {
            try {
                int eventId = Integer.parseInt(eventIdField.getText().trim());
                String eventName = budgetService.getEventName(eventId);
                if (eventName == null) {
                    resultLabel.setText("No event found with ID " + eventId + ".");
                    resultLabel.setForeground(UITheme.DANGER);
                    detailLabel.setText(" ");
                    return;
                }
                double revenue = budgetService.getTicketRevenue(eventId);
                double pl = budgetService.getProfitLoss(eventId);
                resultLabel.setText(eventName + ": " + (pl >= 0 ? "+" : "") + String.format("%.2f", pl));
                resultLabel.setForeground(pl >= 0 ? UITheme.SUCCESS.darker() : UITheme.DANGER);
                detailLabel.setText("Ticket revenue: " + String.format("%.2f", revenue));
            } catch (NumberFormatException ex) {
                resultLabel.setText("Enter a valid numeric Event ID.");
                resultLabel.setForeground(UITheme.DANGER);
                detailLabel.setText(" ");
            }
        });

        JButton closeButton = UIComponents.secondaryButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 24, 20, 24));
        footer.add(closeButton);
        footer.add(lookupButton);

        dialog.add(buildDialogHeader("Quick Profit/Loss Lookup", "Ticket revenue minus expenses, real-time"), BorderLayout.NORTH);
        dialog.add(body, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private class StatusRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String status = value == null ? "" : value.toString();
            Color dot = status.equals("SURPLUS") ? UITheme.SUCCESS : UITheme.DANGER;
            Color tint = status.equals("SURPLUS") ? UITheme.SUCCESS_LIGHT : UITheme.DANGER_LIGHT;

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            wrapper.setOpaque(true);
            wrapper.setBackground(isSelected ? UITheme.PRIMARY_LIGHT : (row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC)));
            wrapper.add(new SoftBadge(status, dot, tint));
            return wrapper;
        }
    }
}