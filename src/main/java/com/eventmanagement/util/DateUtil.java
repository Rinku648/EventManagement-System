package com.eventmanagement.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter DISPLAY_DATETIME = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    public static String format(LocalDate date) { return date == null ? "" : date.format(DISPLAY_FORMAT); }
    public static String format(LocalDateTime dt) { return dt == null ? "" : dt.format(DISPLAY_DATETIME); }
    public static LocalDate today() { return LocalDate.now(); }
}