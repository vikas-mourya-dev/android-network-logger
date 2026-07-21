package com.vbm.logger.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Formats logged epoch-millis timestamps in the host device's local timezone.
 * Request/response timestamps are captured via {@code System.currentTimeMillis()}, which is
 * timezone-agnostic UTC millis; this only controls how they're rendered for display, so logs
 * always read in the time of the app that's actually using this library, not a fixed zone.
 */
public final class TimeFormatter {

    private TimeFormatter() {
    }

    public static String format(long epochMillis) {
        if (epochMillis <= 0) {
            return "-";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        format.setTimeZone(TimeZone.getDefault());
        return format.format(new Date(epochMillis));
    }
}
