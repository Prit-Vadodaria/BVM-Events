package com.bvm.events.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtils {
    private DateUtils() {}

    private static final String SERVER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DISPLAY_DATE_FORMAT = "dd MMM yyyy, hh:mm a";
    private static final String TIME_ONLY_FORMAT = "hh:mm a";
    private static final String DATE_ONLY_FORMAT = "dd MMM yyyy";

    private static final TimeZone IST_TIMEZONE = TimeZone.getTimeZone("Asia/Kolkata");

    private static SimpleDateFormat getServerFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.US);
        sdf.setTimeZone(IST_TIMEZONE);
        return sdf;
    }

    public static Date parseServerDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return getServerFormat().parse(dateString.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    public static String formatServerDate(Date date) {
        if (date == null) return "";
        return getServerFormat().format(date);
    }

    public static String formatDisplayDate(String serverDateString) {
        Date date = parseServerDate(serverDateString);
        if (date == null) return serverDateString != null ? serverDateString : "";
        SimpleDateFormat sdf = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.US);
        sdf.setTimeZone(IST_TIMEZONE);
        return sdf.format(date);
    }

    public static String formatTimeOnly(String serverDateString) {
        Date date = parseServerDate(serverDateString);
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_ONLY_FORMAT, Locale.US);
        sdf.setTimeZone(IST_TIMEZONE);
        return sdf.format(date);
    }

    public static String formatDateOnly(String serverDateString) {
        Date date = parseServerDate(serverDateString);
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_ONLY_FORMAT, Locale.US);
        sdf.setTimeZone(IST_TIMEZONE);
        return sdf.format(date);
    }
}

