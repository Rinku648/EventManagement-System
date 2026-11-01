package com.eventmanagement.ui;

import com.eventmanagement.service.CertificateService;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;

import javax.swing.*;
import java.awt.*;

public class CertificatePanel extends JPanel {
    private final CertificateService certificateService = new CertificateService();

    public CertificatePanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        add(UIComponents.panelBanner("Certificates", "Generate participation certificates for attendees", null), BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        RoundedPanel card = UIComponents.formCard();
        card.setPreferredSize(new Dimension(420, 420));

        JTextField attendeeIdField = new JTextField();
        JTextField eventIdField = new JTextField();
        JTextField attendeeNameField = new JTextField();
        JTextField eventNameField = new JTextField();
        UIComponents.styleTextField(attendeeIdField);
        UIComponents.styleTextField(eventIdField);
        UIComponents.styleTextField(attendeeNameField);
        UIComponents.styleTextField(eventNameField);
        attendeeIdField.setMaximumSize(new Dimension(320, 36));
        eventIdField.setMaximumSize(new Dimension(320, 36));
        attendeeNameField.setMaximumSize(new Dimension(320, 36));
        eventNameField.setMaximumSize(new Dimension(320, 36));

        JButton generateButton = UIComponents.primaryButton("\uD83C\uDFC6  Generate Certificate");
        generateButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(UITheme.LABEL_FONT);
        resultLabel.setForeground(UITheme.TEXT_MUTED);
        resultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        generateButton.addActionListener(e -> {
            try {
                int attendeeId = Integer.parseInt(attendeeIdField.getText().trim());
                int eventId = Integer.parseInt(eventIdField.getText().trim());
                String path = certificateService.issueCertificate(
                        attendeeId, eventId, attendeeNameField.getText().trim(), eventNameField.getText().trim());
                resultLabel.setForeground(UITheme.SUCCESS.darker());
                resultLabel.setText("Certificate saved to: " + path);
            } catch (Exception ex) {
                resultLabel.setForeground(UITheme.DANGER);
                resultLabel.setText("Failed: " + ex.getMessage());
            }
        });

        card.add(UIComponents.formLabel("Attendee ID")); card.add(attendeeIdField);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(UIComponents.formLabel("Event ID")); card.add(eventIdField);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(UIComponents.formLabel("Attendee Name")); card.add(attendeeNameField);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(UIComponents.formLabel("Event Name")); card.add(eventNameField);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(generateButton);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(resultLabel);

        centerWrap.add(card);
        add(centerWrap, BorderLayout.CENTER);
    }
}