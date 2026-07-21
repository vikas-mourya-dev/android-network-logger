package com.vbm.logger.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Detects and reformats JSON request/response bodies for display; a no-op for non-JSON text. */
public final class JsonFormatter {

    private static final int INDENT_SPACES = 2;

    private JsonFormatter() {
    }

    public static boolean isJson(String raw) {
        if (raw == null) {
            return false;
        }
        String trimmed = raw.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    public static String beautify(String raw) {
        if (!isJson(raw)) {
            return raw;
        }
        String trimmed = raw.trim();
        try {
            if (trimmed.startsWith("{")) {
                return new JSONObject(trimmed).toString(INDENT_SPACES);
            }
            return new JSONArray(trimmed).toString(INDENT_SPACES);
        } catch (JSONException e) {
            return raw;
        }
    }

    public static String minify(String raw) {
        if (!isJson(raw)) {
            return raw;
        }
        String trimmed = raw.trim();
        try {
            if (trimmed.startsWith("{")) {
                return new JSONObject(trimmed).toString();
            }
            return new JSONArray(trimmed).toString();
        } catch (JSONException e) {
            return raw;
        }
    }
}
