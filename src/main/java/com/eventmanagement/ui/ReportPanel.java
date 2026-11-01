package com.eventmanagement.ui;

import com.eventmanagement.service.ReportService;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;

import javax.swing.*;
import java.awt.*;

public class ReportPanel extends JPanel {
    private final ReportService reportService = new ReportService();

    public ReportPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        add(UIComponents.panelBanner("Reports", "Generate a summary report for any event", null), BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        JTextField eventIdField = new JTextField();
        UIComponents.styleTextField(eventIdField);
        eventIdField.setMaximumSize(new Dimension(140, 36));

        JButton generateButton = UIComponents.primaryButton("Generate Report");

        JPanel topForm = new JPanel();
        topForm.setOpaque(false);
        topForm.setLayout(new BoxLayout(topForm, BoxLayout.Y_AXIS));
        topForm.add(UIComponents.formLabel("Event ID"));
        topForm.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        row.add(eventIdField);
        row.add(generateButton);
        topForm.add(row);

        JTextArea resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(UITheme.MONO_FONT);
        resultArea.setForeground(UITheme.TEXT_DARK);
        resultArea.setBackground(new Color(0xF8FAFC));
        resultArea.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));

        generateButton.addActionListener(e -> {
            int eventId = Integer.parseInt(eventIdField.getText().trim());
            resultArea.setText(reportService.buildSummary(eventId));
        });

        RoundedPanel resultCard = new RoundedPanel(UITheme.RADIUS_MD, UITheme.SURFACE, UITheme.BORDER);
        resultCard.setLayout(new BorderLayout());
        resultCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        resultCard.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JPanel stacked = new JPanel();
        stacked.setOpaque(false);
        stacked.setLayout(new BoxLayout(stacked, BoxLayout.Y_AXIS));

        RoundedPanel topCard = new RoundedPanel(UITheme.RADIUS_MD, UITheme.SURFACE, UITheme.BORDER);
        topCard.setLayout(new BorderLayout());
        topCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        topCard.add(topForm, BorderLayout.CENTER);
        topCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        topCard.setMaximumSize(new Dimension(3000, 100));

        stacked.add(topCard);
        stacked.add(Box.createRigidArea(new Dimension(0, 16)));
        stacked.add(resultCard);

        centerWrap.add(stacked, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);
    }
}