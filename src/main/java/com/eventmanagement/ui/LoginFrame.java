package com.eventmanagement.ui;

import com.eventmanagement.model.User;
import com.eventmanagement.service.AuthService;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.GradientHeader;
import com.eventmanagement.ui.components.UIComponents.RoundedPanel;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("Smart Event Management System — Login");
        setSize(920, 560);
        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildBrandPanel(), BorderLayout.WEST);
        add(buildFormPanel(), BorderLayout.CENTER);
    }

    // Left brand panel: gradient background with logo mark and tagline
    private JPanel buildBrandPanel() {
        GradientHeader brand = new GradientHeader(UITheme.PRIMARY, UITheme.ACCENT, 0);
        brand.setPreferredSize(new Dimension(360, 0));
        brand.setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        // Same sparkle glyph + emoji font as the sidebar brand mark, so both screens match
        JLabel mark = new JLabel("\u2728");
        mark.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        mark.setForeground(Color.WHITE);
        mark.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("SEMS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("<html>Smart Event<br>Management System</html>");
        subtitle.setFont(UITheme.SECTION_FONT);
        subtitle.setForeground(new Color(255, 255, 255, 220));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("<html>Plan, host, and track every<br>event from one dashboard.</html>");
        tagline.setFont(UITheme.LABEL_FONT);
        tagline.setForeground(new Color(255, 255, 255, 190));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(mark);
        inner.add(Box.createRigidArea(new Dimension(0, 8)));
        inner.add(title);
        inner.add(Box.createRigidArea(new Dimension(0, 10)));
        inner.add(subtitle);
        inner.add(Box.createRigidArea(new Dimension(0, 16)));
        inner.add(tagline);

        brand.add(inner);
        return brand;
    }

    // Right side: white canvas holding the centered rounded login card
    private JPanel buildFormPanel() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(UITheme.BACKGROUND);

        RoundedPanel card = UIComponents.formCard();
        card.setPreferredSize(new Dimension(360, 380));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Welcome back");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to continue to your dashboard");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel emailLabel = UIComponents.formLabel("Email");
        JTextField emailField = new JTextField();
        UIComponents.styleTextField(emailField);
        emailField.setMaximumSize(new Dimension(500, 38));

        JLabel passwordLabel = UIComponents.formLabel("Password");
        JPasswordField passwordField = new JPasswordField();
        UIComponents.styleTextField(passwordField);
        passwordField.setMaximumSize(new Dimension(500, 38));

        JButton loginButton = UIComponents.primaryButton("Login");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(500, 42));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.LABEL_FONT);
        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            User user = authService.login(email, password);
            if (user != null) {
                new DashboardFrame(user).setVisible(true);
                dispose();
            } else {
                statusLabel.setText("Invalid email or password");
            }
        });

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 26)));
        card.add(emailLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(emailField);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(passwordLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(passwordField);
        card.add(Box.createRigidArea(new Dimension(0, 22)));
        card.add(loginButton);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(statusLabel);

        wrap.add(card);
        return wrap;
    }
}