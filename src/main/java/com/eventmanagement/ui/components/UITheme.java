package com.eventmanagement.ui.components;

import java.awt.*;

/**
 * Central design-token file for SEMS.
 * Theme name: "Aurora" — indigo/violet primary with soft pastel tints
 * for status/badge surfaces instead of harsh saturated fills.
 *
 * Every panel pulls colors/fonts/spacing from here so the whole app
 * stays visually consistent across all 14 modules.
 */
public class UITheme {

    // ===== Core brand colors =====
    public static final Color PRIMARY        = new Color(0x4F46E5); // Indigo
    public static final Color PRIMARY_DARK    = new Color(0x4338CA);
    public static final Color PRIMARY_LIGHT   = new Color(0xEEF2FF);
    public static final Color ACCENT          = new Color(0x7C3AED); // Violet
    public static final Color ACCENT_TEAL     = new Color(0x0D9488);

    // ===== Semantic colors (solid + light tint pair) =====
    public static final Color SUCCESS         = new Color(0x10B981);
    public static final Color SUCCESS_LIGHT    = new Color(0xECFDF5);
    public static final Color WARNING         = new Color(0xF59E0B);
    public static final Color WARNING_LIGHT    = new Color(0xFFFBEB);
    public static final Color DANGER          = new Color(0xEF4444);
    public static final Color DANGER_LIGHT     = new Color(0xFEF2F2);
    public static final Color INFO            = new Color(0x0EA5E9);
    public static final Color INFO_LIGHT       = new Color(0xF0F9FF);
    public static final Color NEUTRAL         = new Color(0x64748B);
    public static final Color NEUTRAL_LIGHT    = new Color(0xF1F5F9);

    // ===== Surfaces =====
    public static final Color BACKGROUND      = new Color(0xF3F4F8);
    public static final Color SURFACE         = Color.WHITE;
    public static final Color BORDER          = new Color(0xE2E8F0);
    public static final Color BORDER_STRONG    = new Color(0xCBD5E1);

    // ===== Text =====
    public static final Color TEXT_DARK       = new Color(0x0F172A);
    public static final Color TEXT_MUTED      = new Color(0x64748B);
    public static final Color TEXT_ON_PRIMARY  = Color.WHITE;

    // ===== Dark sidebar palette =====
    public static final Color SIDEBAR_BG      = new Color(0x111827);
    public static final Color SIDEBAR_HOVER    = new Color(0x1F2937);
    public static final Color SIDEBAR_TEXT     = new Color(0xCBD5E1);
    public static final Color SIDEBAR_ACTIVE   = new Color(0x4F46E5);

    // ===== Background blob accents (used behind the app canvas) =====
    public static final Color BLOB_INDIGO     = new Color(0x4F46E5);
    public static final Color BLOB_VIOLET     = new Color(0x7C3AED);

    // ===== Typography =====
    private static final String FONT_FAMILY = "Segoe UI";
    public static final Font TITLE_FONT       = new Font(FONT_FAMILY, Font.BOLD, 26);
    public static final Font SUBTITLE_FONT     = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font SECTION_FONT      = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font LABEL_FONT        = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font LABEL_BOLD_FONT    = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font BODY_FONT         = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font STAT_VALUE_FONT    = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font BUTTON_FONT       = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font BADGE_FONT        = new Font(FONT_FAMILY, Font.BOLD, 11);
    public static final Font MONO_FONT         = new Font("Consolas", Font.PLAIN, 13);

    // ===== Spacing & radius scale =====
    public static final int RADIUS_SM = 8;
    public static final int RADIUS_MD = 12;
    public static final int RADIUS_LG = 18;
    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 16;
    public static final int SPACING_LG = 24;

    /** Solid accent color for a given status string (badge text/dot). */
    public static Color statusColor(String status) {
        if (status == null) return NEUTRAL;
        switch (status.toUpperCase()) {
            case "UPCOMING":
            case "PENDING":        return PRIMARY;
            case "ONGOING":         return INFO;
            case "PUBLISHED":
            case "CONFIRMED":
            case "PAID":
            case "AVAILABLE":
            case "REFUNDED":
            case "COMPLETED":
            case "SUCCESS":        return SUCCESS;
            case "CANCELLED":
            case "FAILED":
            case "BOOKED":         return DANGER;
            case "DRAFT":
            case "UNPAID":
            case "REFUND_PENDING": return WARNING;
            default:                return NEUTRAL;
        }
    }

    /** Soft tint background for a given status string (badge fill). */
    public static Color statusLightColor(String status) {
        if (status == null) return NEUTRAL_LIGHT;
        switch (status.toUpperCase()) {
            case "UPCOMING":
            case "PENDING":        return PRIMARY_LIGHT;
            case "ONGOING":         return INFO_LIGHT;
            case "PUBLISHED":
            case "CONFIRMED":
            case "PAID":
            case "AVAILABLE":
            case "REFUNDED":
            case "COMPLETED":
            case "SUCCESS":        return SUCCESS_LIGHT;
            case "CANCELLED":
            case "FAILED":
            case "BOOKED":         return DANGER_LIGHT;
            case "DRAFT":
            case "UNPAID":
            case "REFUND_PENDING": return WARNING_LIGHT;
            default:                return NEUTRAL_LIGHT;
        }
    }
}