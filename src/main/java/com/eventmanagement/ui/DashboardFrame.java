package com.eventmanagement.ui;

import com.eventmanagement.model.User;
import com.eventmanagement.ui.components.UITheme;
import com.eventmanagement.ui.components.UIComponents;
import com.eventmanagement.ui.components.UIComponents.AppBackground;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public DashboardFrame(User user) {
        setTitle("SEMS Dashboard — " + user.getFullName());
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(buildSidebar(user), BorderLayout.WEST);

        AppBackground background = new AppBackground();
        background.setLayout(new BorderLayout());
        background.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        addModule("events",       "📅", "Events",       new EventPanel(user));
        addModule("venues",       "🏛",  "Venues",       new VenuePanel());
        addModule("bookings",     "🎟",  "Bookings",     new BookingPanel());
        addModule("payments",     "💳", "Payments",     new PaymentPanel());
        addModule("seatmap",      "🪑",  "Seat Map",     new SeatMapPanel());
        addModule("checkin",      "✅", "Check-In",     new CheckInPanel());
        addModule("vendors",      "🧾",  "Vendors",      new VendorPanel());
        addModule("budget",       "💰", "Budget",       new BudgetPanel());
        addModule("schedule",     "🗓",  "Schedule",     new SchedulePanel());
        addModule("feedback",     "⭐", "Feedback",     new FeedbackPanel());
        addModule("refunds",      "↩",  "Refunds",      new RefundPanel());
        addModule("certificates", "🏆", "Certificates", new CertificatePanel());
        addModule("reports",      "📊", "Reports",      new ReportPanel());

        background.add(contentPanel, BorderLayout.CENTER);
        add(background, BorderLayout.CENTER);

        setActive("events");
    }

    private void addModule(String key, String icon, String label, JPanel panel) {
        contentPanel.add(panel, key);
    }

    private JPanel buildSidebar(User user) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel brand = new JLabel(
                "<html><span style='font-family:Segoe UI Emoji;font-size:20px;color:#FFFFFF;'>✨</span>" +
                        "&nbsp;&nbsp;<span style='font-family:Segoe UI;font-size:20px;font-weight:bold;color:#FFFFFF;'>SEMS</span></html>"
        );
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 0));
        sidebar.add(brand);

        String[][] items = {
                {"events", "📅", "Events"}, {"venues", "🏛", "Venues"},
                {"bookings", "🎟", "Bookings"}, {"payments", "💳", "Payments"},
                {"seatmap", "🪑", "Seat Map"}, {"checkin", "✅", "Check-In"},
                {"vendors", "🧾", "Vendors"}, {"budget", "💰", "Budget"},
                {"schedule", "🗓", "Schedule"}, {"feedback", "⭐", "Feedback"},
                {"refunds", "↩", "Refunds"}, {"certificates", "🏆", "Certificates"},
                {"reports", "📊", "Reports"}
        };

        for (String[] item : items) {
            JButton nav = UIComponents.sidebarNavButton(item[1], item[2]);
            nav.setAlignmentX(Component.LEFT_ALIGNMENT);
            nav.putClientProperty("active", item[0].equals("events"));
            nav.addActionListener(e -> setActive(item[0]));
            navButtons.put(item[0], nav);
            sidebar.add(nav);
            sidebar.add(Box.createRigidArea(new Dimension(0, 2)));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel userChip = new JLabel(
                "<html><span style='font-family:Segoe UI Emoji;font-size:14px;color:#CBD5E1;'>👤</span>" +
                        "&nbsp;&nbsp;<span style='font-family:Segoe UI;font-size:13px;color:#CBD5E1;'>" + user.getFullName() + "</span></html>"
        );
        userChip.setAlignmentX(Component.LEFT_ALIGNMENT);
        userChip.setBorder(BorderFactory.createEmptyBorder(10, 16, 0, 0));
        sidebar.add(userChip);

        return sidebar;
    }

    private void setActive(String key) {
        cardLayout.show(contentPanel, key);
        navButtons.forEach((k, btn) -> {
            btn.putClientProperty("active", k.equals(key));
            btn.repaint();
        });
    }
}