package com.eventmanagement.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    public static boolean isPositiveNumber(String value) {
        try { return Double.parseDouble(value) > 0; } catch (NumberFormatException e) { return false; }
    }
}
