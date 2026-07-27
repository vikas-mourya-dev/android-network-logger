package com.vbm.logger.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable configuration for NetworkLogger, built via {@link Builder} so new
 * options can be added later without breaking existing integrations.
 */
public final class LoggerConfig {

    private final boolean loggingEnabled;
    private final boolean databaseStorageEnabled;
    private final int maxLogCount;
    private final int logBodyMaxLength;
    private final Set<String> redactedHeaders;
    private final boolean floatingBubbleEnabled;
    private final Set<Class<?>> excludedActivityClasses;

    private LoggerConfig(Builder builder) {
        this.loggingEnabled = builder.loggingEnabled;
        this.databaseStorageEnabled = builder.databaseStorageEnabled;
        this.maxLogCount = builder.maxLogCount;
        this.logBodyMaxLength = builder.logBodyMaxLength;
        this.redactedHeaders = Collections.unmodifiableSet(new HashSet<>(builder.redactedHeaders));
        this.floatingBubbleEnabled = builder.floatingBubbleEnabled;
        this.excludedActivityClasses = Collections.unmodifiableSet(new HashSet<>(builder.excludedActivityClasses));
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public boolean isDatabaseStorageEnabled() {
        return databaseStorageEnabled;
    }

    public int getMaxLogCount() {
        return maxLogCount;
    }

    public int getLogBodyMaxLength() {
        return logBodyMaxLength;
    }

    public Set<String> getRedactedHeaders() {
        return redactedHeaders;
    }

    /** Whether the draggable in-app debug bubble should be shown automatically after {@code init()}. */
    public boolean isFloatingBubbleEnabled() {
        return floatingBubbleEnabled;
    }

    /** Activity classes the debug bubble is never attached to (e.g. a third-party SDK screen). */
    public Set<Class<?>> getExcludedActivityClasses() {
        return excludedActivityClasses;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean loggingEnabled = true;
        private boolean databaseStorageEnabled = true;
        private int maxLogCount = 500;
        private int logBodyMaxLength = 10_000;
        private final Set<String> redactedHeaders = new HashSet<>();
        private boolean floatingBubbleEnabled = false;
        private Set<Class<?>> excludedActivityClasses = new HashSet<>();

        public Builder setLoggingEnabled(boolean loggingEnabled) {
            this.loggingEnabled = loggingEnabled;
            return this;
        }

        public Builder setDatabaseStorageEnabled(boolean databaseStorageEnabled) {
            this.databaseStorageEnabled = databaseStorageEnabled;
            return this;
        }

        public Builder setMaxLogCount(int maxLogCount) {
            this.maxLogCount = maxLogCount;
            return this;
        }

        public Builder setLogBodyMaxLength(int logBodyMaxLength) {
            this.logBodyMaxLength = logBodyMaxLength;
            return this;
        }

        public Builder setRedactedHeaders(String... headers) {
            this.redactedHeaders.clear();
            for (String header : headers) {
                this.redactedHeaders.add(header.toLowerCase());
            }
            return this;
        }

        /** Shows a draggable in-app debug bubble (Start/Stop Logging, View Logs, Clear Logs) as soon as init() runs. */
        public Builder setFloatingBubbleEnabled(boolean floatingBubbleEnabled) {
            this.floatingBubbleEnabled = floatingBubbleEnabled;
            return this;
        }

        /**
         * Activities the debug bubble should never attach to — e.g. a third-party SDK screen
         * whose theme doesn't extend AppCompat/MaterialComponents.
         */
        public Builder setExcludedActivityClasses(Class<?>... activityClasses) {
            this.excludedActivityClasses = new HashSet<>(Arrays.asList(activityClasses));
            return this;
        }

        public LoggerConfig build() {
            return new LoggerConfig(this);
        }
    }
}
