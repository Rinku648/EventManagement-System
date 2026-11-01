package com.eventmanagement.ui;

import com.eventmanagement.dao.AttendeeDAO;
import com.eventmanagement.model.Attendee;
import com.eventmanagement.model.Event;
import com.eventmanagement.model.Feedback;
import com.eventmanagement.service.EventService;
import com.eventmanagement.service.FeedbackService;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class FeedbackPanel extends JPanel {
    private final FeedbackService feedbackService = new FeedbackService();
    private final EventService eventService = new EventService();
    private final AttendeeDAO attendeeDAO = new AttendeeDAO();

    private final DefaultTableModel tableModel;
    private final JTable table;

    private static final int FORM_WIDTH = 340;

    public FeedbackPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JButton submitButton = UIComponents.pillButton("+  Submit Feedback", Color.WHITE, UITheme.PRIMARY, true);
        submitButton.addActionListener(e -> openSubmitDialog());
        JButton avgButton = UIComponents.pillButton("View Average Rating", new Color(255, 255, 255, 40), Color.WHITE, true);
        avgButton.addActionListener(e -> openAverageRatingDialog());
        JButton refreshButton = UIComponents.pillButton("Refresh", new Color(255, 255, 255, 40), Color.WHITE, true);
        refreshButton.addActionListener(e -> loadFeedback());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(refreshButton);
        actions.add(avgButton);
        actions.add(submitButton);

        add(UIComponents.panelBanner("Feedback", "Collect and review attendee feedback", actions), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Attendee", "Event", "Rating", "Comments"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIComponents.styleTable(table);
        table.getColumnModel().getColumn(2).setCellRenderer(new StarRatingCellRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(400);

        RoundedPanel tableCard = UIComponents.tableCard(table);
        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        centerWrap.add(tableCard, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        loadFeedback();
    }

    private void loadFeedback() {
        tableModel.setRowCount(0);
        List<Feedback> all = feedbackService.getAllFeedback();
        for (Feedback f : all) {
            tableModel.addRow(new Object[]{
                    f.getAttendeeName() != null ? f.getAttendeeName() : "Attendee #" + f.getAttendeeId(),
                    f.getEventName() != null ? f.getEventName() : "Event #" + f.getEventId(),
                    f.getRating(),
                    f.getComments() != null && !f.getComments().isBlank() ? f.getComments() : "—"
            });
        }
    }

    /** Renders an integer 1-5 as filled/empty star glyphs, e.g. "★★★★☆". */
    private class StarRatingCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            int rating = value instanceof Integer ? (Integer) value : 0;
            JLabel label = new JLabel(starString(rating));
            label.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 15));
            label.setForeground(new Color(0xF59E0B)); // warm gold, matches WARNING accent
            label.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            label.setOpaque(true);
            label.setBackground(isSelected ? UITheme.PRIMARY_LIGHT : (row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC)));
            return label;
        }
    }

    private static String starString(int rating) {
        int clamped = Math.max(0, Math.min(5, rating));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < clamped ? "\u2605" : "\u2606");
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // Interactive star-rating widget (click to set 1-5)
    // ---------------------------------------------------------------
    private static class StarPicker extends JPanel {
        private int rating = 5;
        private final JLabel[] stars = new JLabel[5];

        StarPicker() {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
            for (int i = 0; i < 5; i++) {
                final int starValue = i + 1;
                JLabel star = new JLabel();
                star.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 26));
                star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                star.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        rating = starValue;
                        render();
                    }
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        renderPreview(starValue);
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        render();
                    }
                });
                stars[i] = star;
                add(star);
            }
            render();
        }

        private void renderPreview(int hoverValue) {
            for (int i = 0; i < 5; i++) {
                stars[i].setText(i < hoverValue ? "\u2605" : "\u2606");
                stars[i].setForeground(new Color(0xF59E0B));
            }
        }

        private void render() {
            for (int i = 0; i < 5; i++) {
                stars[i].setText(i < rating ? "\u2605" : "\u2606");
                stars[i].setForeground(i < rating ? new Color(0xF59E0B) : UITheme.BORDER_STRONG);
            }
        }

        int getRating() { return rating; }
        void setRating(int r) { rating = Math.max(1, Math.min(5, r)); render(); }
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

    private JComboBox<Event> buildEventCombo() {
        JComboBox<Event> combo = new JComboBox<>();
        combo.setRenderer(new DefaultListCellRenderer() {
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
        });
        List<Event> events = eventService.getAllEvents();
        for (Event ev : events) combo.addItem(ev);
        if (combo.getItemCount() > 0) combo.setSelectedIndex(0);
        stretchField(combo);
        return combo;
    }

    // ---------------------------------------------------------------
    // Submit Feedback dialog
    // ---------------------------------------------------------------

    private void openSubmitDialog() {
        JTextField attendeeIdField = new JTextField();
        JLabel attendeeNameLabel = new JLabel("Enter an Attendee ID and click \"Find\"");
        attendeeNameLabel.setFont(UITheme.LABEL_FONT);
        attendeeNameLabel.setForeground(UITheme.TEXT_MUTED);
        attendeeNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton findButton = UIComponents.secondaryButton("Find");
        UIComponents.styleTextField(attendeeIdField);

        JPanel attendeeRow = new JPanel(new BorderLayout(8, 0));
        attendeeRow.setOpaque(false);
        attendeeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        attendeeRow.setMaximumSize(new Dimension(FORM_WIDTH, 36));
        attendeeIdField.setPreferredSize(new Dimension(FORM_WIDTH - 80, 34));
        attendeeRow.add(attendeeIdField, BorderLayout.CENTER);
        attendeeRow.add(findButton, BorderLayout.EAST);

        int[] resolvedAttendeeId = {-1};
        findButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(attendeeIdField.getText().trim());
                Attendee a = attendeeDAO.getById(id);
                if (a == null) {
                    resolvedAttendeeId[0] = -1;
                    attendeeNameLabel.setText("No attendee found with ID " + id);
                    attendeeNameLabel.setForeground(UITheme.DANGER);
                } else {
                    resolvedAttendeeId[0] = id;
                    attendeeNameLabel.setText("Attendee: " + a.getFullName());
                    attendeeNameLabel.setForeground(UITheme.SUCCESS.darker());
                }
            } catch (NumberFormatException ex) {
                resolvedAttendeeId[0] = -1;
                attendeeNameLabel.setText("Attendee ID must be a number.");
                attendeeNameLabel.setForeground(UITheme.DANGER);
            }
        });

        JComboBox<Event> eventCombo = buildEventCombo();

        StarPicker starPicker = new StarPicker();
        starPicker.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea commentsArea = new JTextArea(4, 20);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setFont(UITheme.BODY_FONT);
        commentsArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_STRONG, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JScrollPane commentsScroll = new JScrollPane(commentsArea);
        commentsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentsScroll.setPreferredSize(new Dimension(FORM_WIDTH, 90));
        commentsScroll.setMaximumSize(new Dimension(FORM_WIDTH, 90));

        JPanel form = buildForm();
        form.add(buildLabel("Attendee ID"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(attendeeRow);
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(attendeeNameLabel);
        form.add(Box.createRigidArea(new Dimension(0, 16)));
        form.add(buildLabel("Event"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(eventCombo);
        form.add(Box.createRigidArea(new Dimension(0, 16)));
        form.add(buildLabel("Rating"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(starPicker);
        form.add(Box.createRigidArea(new Dimension(0, 16)));
        form.add(buildLabel("Comments"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(commentsScroll);

        int result = JOptionPane.showConfirmDialog(this, form, "Submit Feedback", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if (resolvedAttendeeId[0] == -1) {
                JOptionPane.showMessageDialog(this, "Please look up a valid Attendee ID using \"Find\" before submitting.");
                return;
            }
            Event selectedEvent = (Event) eventCombo.getSelectedItem();
            if (selectedEvent == null) {
                JOptionPane.showMessageDialog(this, "Please select an event.");
                return;
            }

            Feedback f = new Feedback();
            f.setEventId(selectedEvent.getEventId());
            f.setAttendeeId(resolvedAttendeeId[0]);
            f.setRating(starPicker.getRating());
            f.setComments(commentsArea.getText().trim());

            boolean success = feedbackService.submitFeedback(f);
            if (success) {
                loadFeedback();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit feedback.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------------------------------------------------------
    // View Average Rating dialog
    // ---------------------------------------------------------------

    private void openAverageRatingDialog() {
        JComboBox<Event> eventCombo = buildEventCombo();

        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(UITheme.SECTION_FONT);
        resultLabel.setForeground(UITheme.TEXT_DARK);
        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel starsLabel = new JLabel(" ");
        starsLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 26));
        starsLabel.setForeground(new Color(0xF59E0B));
        starsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel countLabel = new JLabel(" ");
        countLabel.setFont(UITheme.LABEL_FONT);
        countLabel.setForeground(UITheme.TEXT_MUTED);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Runnable refresh = () -> {
            Event ev = (Event) eventCombo.getSelectedItem();
            if (ev == null) return;
            int count = feedbackService.getFeedbackCount(ev.getEventId());
            if (count == 0) {
                resultLabel.setText("No feedback yet for this event.");
                starsLabel.setText(starString(0));
                countLabel.setText(" ");
            } else {
                double avg = feedbackService.getAverageRating(ev.getEventId());
                resultLabel.setText(String.format("%.1f out of 5", avg));
                starsLabel.setText(starString((int) Math.round(avg)));
                countLabel.setText(count + " review" + (count == 1 ? "" : "s"));
            }
        };
        eventCombo.addActionListener(e -> refresh.run());
        refresh.run();

        JPanel form = buildForm();
        form.add(buildLabel("Select Event"));
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(eventCombo);
        form.add(Box.createRigidArea(new Dimension(0, 20)));
        form.add(resultLabel);
        form.add(Box.createRigidArea(new Dimension(0, 6)));
        form.add(starsLabel);
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(countLabel);

        JOptionPane.showMessageDialog(this, form, "Average Rating", JOptionPane.PLAIN_MESSAGE);
    }
}