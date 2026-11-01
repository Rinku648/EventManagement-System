package com.eventmanagement.ui.components;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class UIComponents {

    // ---------------------------------------------------------------
    // Rounded card panel
    // ---------------------------------------------------------------
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;
        private final Color border;

        public RoundedPanel(int radius, Color bg, Color border) {
            this.radius = radius;
            this.bg = bg;
            this.border = border;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------------------------------------------------------------
    // Gradient header banner
    // ---------------------------------------------------------------
    public static class GradientHeader extends JPanel {
        private final Color from;
        private final Color to;
        private final int radius;

        public GradientHeader(Color from, Color to, int radius) {
            this.from = from;
            this.to = to;
            this.radius = radius;
            setOpaque(false);
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, from, getWidth(), getHeight(), to);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static GradientHeader panelBanner(String title, String subtitle, JComponent actions) {
        GradientHeader banner = new GradientHeader(UITheme.PRIMARY, UITheme.ACCENT, UITheme.RADIUS_LG);
        banner.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);

        titleBlock.add(titleLabel);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(UITheme.SUBTITLE_FONT);
            subtitleLabel.setForeground(new Color(255, 255, 255, 210));
            titleBlock.add(Box.createRigidArea(new Dimension(0, 4)));
            titleBlock.add(subtitleLabel);
        }

        banner.add(titleBlock, BorderLayout.WEST);
        if (actions != null) {
            actions.setOpaque(false);
            banner.add(actions, BorderLayout.EAST);
        }
        return banner;
    }

    public static class AppBackground extends JPanel {
        public AppBackground() {
            setLayout(new BorderLayout());
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = Math.max(getWidth(), 1);
            int h = Math.max(getHeight(), 1);

            g2.setColor(UITheme.BACKGROUND);
            g2.fillRect(0, 0, w, h);

            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Float(w * 0.9f, h * -0.05f), Math.max(w, h) * 0.55f,
                    new float[]{0f, 1f},
                    new Color[]{withAlpha(UITheme.BLOB_INDIGO, 26), withAlpha(UITheme.BLOB_INDIGO, 0)}
            ));
            g2.fillRect(0, 0, w, h);

            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Float(w * 0.05f, h * 1.05f), Math.max(w, h) * 0.5f,
                    new float[]{0f, 1f},
                    new Color[]{withAlpha(UITheme.BLOB_VIOLET, 20), withAlpha(UITheme.BLOB_VIOLET, 0)}
            ));
            g2.fillRect(0, 0, w, h);

            g2.dispose();
            super.paintComponent(g);
        }

        private Color withAlpha(Color c, int alpha) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
        }
    }

    // ---------------------------------------------------------------
    // Sidebar navigation button — icon and label are hand-drawn in
    // paintComponent (no HTML), so there is zero risk of text wrapping
    // onto a second line regardless of label length or button width.
    // ---------------------------------------------------------------
    public static JButton sidebarNavButton(String icon, String label) {
        JButton button = new JButton() {
            private boolean hover = false;

            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setForeground(UITheme.SIDEBAR_TEXT);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                setAlignmentX(Component.LEFT_ALIGNMENT);
                setPreferredSize(new Dimension(206, 44));
                setMaximumSize(new Dimension(206, 44));
                setMinimumSize(new Dimension(206, 44));
                setHorizontalAlignment(SwingConstants.LEFT);
                putClientProperty("active", Boolean.FALSE);

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (active) {
                    g2.setColor(UITheme.SIDEBAR_ACTIVE);
                    g2.fillRoundRect(8, 0, getWidth() - 16, getHeight(), UITheme.RADIUS_SM, UITheme.RADIUS_SM);
                } else if (hover) {
                    g2.setColor(UITheme.SIDEBAR_HOVER);
                    g2.fillRoundRect(8, 0, getWidth() - 16, getHeight(), UITheme.RADIUS_SM, UITheme.RADIUS_SM);
                }

                Color textColor = (active || hover) ? Color.WHITE : UITheme.SIDEBAR_TEXT;

                // Draw icon
                Font iconFont = new Font("Segoe UI Emoji", Font.PLAIN, 16);
                g2.setFont(iconFont);
                g2.setColor(textColor);
                FontMetrics iconFm = g2.getFontMetrics();
                int iconY = (getHeight() - iconFm.getHeight()) / 2 + iconFm.getAscent();
                g2.drawString(icon, 18, iconY);

                // Draw label
                Font labelFont = UITheme.LABEL_FONT;
                g2.setFont(labelFont);
                FontMetrics labelFm = g2.getFontMetrics();
                int labelY = (getHeight() - labelFm.getHeight()) / 2 + labelFm.getAscent();
                g2.drawString(label, 48, labelY);

                g2.dispose();
            }
        };
        return button;
    }

    // ---------------------------------------------------------------
    // Pill-shaped button with hover state (filled or outline)
    // ---------------------------------------------------------------
    public static JButton pillButton(String text, Color bg, Color fg, boolean filled) {
        JButton button = new JButton(text) {
            private Color currentBg = filled ? bg : UITheme.SURFACE;

            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setForeground(fg);
                setFont(UITheme.BUTTON_FONT);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        currentBg = filled ? darker(bg, 0.12f) : UITheme.PRIMARY_LIGHT;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        currentBg = filled ? bg : UITheme.SURFACE;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color paintColor = isEnabled() ? currentBg : new Color(0xE2E8F0);
                g2.setColor(paintColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS_LG, UITheme.RADIUS_LG);
                if (!filled) {
                    g2.setColor(UITheme.BORDER_STRONG);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, UITheme.RADIUS_LG, UITheme.RADIUS_LG);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        return button;
    }

    public static JButton primaryButton(String text) {
        return pillButton(text, UITheme.PRIMARY, Color.WHITE, true);
    }

    public static JButton secondaryButton(String text) {
        return pillButton(text, UITheme.SURFACE, UITheme.TEXT_DARK, false);
    }

    public static JButton dangerButton(String text) {
        return pillButton(text, UITheme.DANGER, Color.WHITE, true);
    }

    private static Color darker(Color c, float factor) {
        return new Color(
                Math.max((int) (c.getRed() * (1 - factor)), 0),
                Math.max((int) (c.getGreen() * (1 - factor)), 0),
                Math.max((int) (c.getBlue() * (1 - factor)), 0)
        );
    }

    // ---------------------------------------------------------------
    // Soft pill badge: light tint background + solid-colored text/dot
    // ---------------------------------------------------------------
    public static class SoftBadge extends JLabel {
        private final Color dot;
        private final Color bgTint;

        public SoftBadge(String text, Color dot, Color bgTint) {
            super("  " + text);
            this.dot = dot;
            this.bgTint = bgTint;
            setForeground(dot.darker());
            setFont(UITheme.BADGE_FONT);
            setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 12));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgTint);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(dot);
            int d = 7;
            g2.fillOval(10, (getHeight() - d) / 2, d, d);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JPanel badgeCell(String status, boolean selected, boolean evenRow) {
        Color dot = UITheme.statusColor(status);
        Color tint = UITheme.statusLightColor(status);
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(true);
        wrapper.setBackground(selected ? UITheme.PRIMARY_LIGHT : (evenRow ? Color.WHITE : new Color(0xF8FAFC)));
        wrapper.add(new SoftBadge(status == null ? "" : status, dot, tint));
        return wrapper;
    }

    // ---------------------------------------------------------------
    // Stat card with an icon chip
    // ---------------------------------------------------------------
    public static RoundedPanel statCard(String icon, String label, JLabel valueLabel, Color accent, Color accentTint) {
        RoundedPanel card = new RoundedPanel(UITheme.RADIUS_MD, UITheme.SURFACE, UITheme.BORDER);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel iconChip = new JLabel(icon, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentTint);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS_SM, UITheme.RADIUS_SM);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconChip.setPreferredSize(new Dimension(44, 44));
        iconChip.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        valueLabel.setFont(UITheme.STAT_VALUE_FONT);
        valueLabel.setForeground(UITheme.TEXT_DARK);

        JLabel captionLabel = new JLabel(label);
        captionLabel.setFont(UITheme.LABEL_FONT);
        captionLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.add(valueLabel);
        textBlock.add(captionLabel);

        card.add(iconChip, BorderLayout.WEST);
        card.add(textBlock, BorderLayout.CENTER);
        return card;
    }

    // ---------------------------------------------------------------
    // Table styling helpers
    // ---------------------------------------------------------------
    public static void styleTable(JTable table) {
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(UITheme.PRIMARY_LIGHT);
        table.setSelectionForeground(UITheme.TEXT_DARK);
        table.setFont(UITheme.BODY_FONT);
        table.setFillsViewportHeight(true);

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                } else {
                    c.setBackground(UITheme.PRIMARY_LIGHT);
                }
                if (c instanceof JLabel jl) {
                    jl.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                    jl.setForeground(UITheme.TEXT_DARK);
                }
                return c;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setFont(UITheme.LABEL_BOLD_FONT);
        header.setBackground(new Color(0xF8FAFC));
        header.setForeground(UITheme.TEXT_MUTED);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));
        header.setReorderingAllowed(false);
    }

    public static RoundedPanel tableCard(JTable table) {
        RoundedPanel card = new RoundedPanel(UITheme.RADIUS_MD, UITheme.SURFACE, UITheme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.SURFACE);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    // ---------------------------------------------------------------
    // Form field + label styling
    // ---------------------------------------------------------------
    public static void styleTextField(JTextField field) {
        field.setFont(UITheme.BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_STRONG, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(340, 36));
    }

    public static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.LABEL_BOLD_FONT);
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static RoundedPanel formCard() {
        RoundedPanel card = new RoundedPanel(UITheme.RADIUS_MD, UITheme.SURFACE, UITheme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        return card;
    }
}