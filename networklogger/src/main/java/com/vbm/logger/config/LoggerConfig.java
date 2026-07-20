package com.vbm.logger.config;

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

    private LoggerConfig(Builder builder) {
        this.loggingEnabled = builder.loggingEnabled;
        this.databaseStorageEnabled = builder.databaseStorageEnabled;
        this.maxLogCount = builder.maxLogCount;
        this.logBodyMaxLength = builder.logBodyMaxLength;
        this.redactedHeaders = Collections.unmodifiableSet(new HashSet<>(builder.redactedHeaders));
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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean loggingEnabled = true;
        private boolean databaseStorageEnabled = true;
        private int maxLogCount = 500;
        private int logBodyMaxLength = 10_000;
        private final Set<String> redactedHeaders = new HashSet<>();

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

        public LoggerConfig build() {
            return new LoggerConfig(this);
        }
    }
}
