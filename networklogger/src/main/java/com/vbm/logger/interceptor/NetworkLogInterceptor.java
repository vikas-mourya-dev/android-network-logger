package com.vbm.logger.interceptor;

import androidx.annotation.NonNull;

import com.vbm.logger.config.LoggerConfig;
import com.vbm.logger.data.entity.NetworkLogEntity;
import com.vbm.logger.data.repository.NetworkLogRepository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Captures request/response metadata and bodies for every OkHttp call and hands
 * the result to the repository asynchronously. Never blocks or breaks the host
 * app's network call: any internal failure is swallowed, and response bodies
 * are peeked rather than consumed.
 */
public class NetworkLogInterceptor implements okhttp3.Interceptor {

    private final NetworkLogRepository repository;
    private final AtomicReference<LoggerConfig> configRef;

    public NetworkLogInterceptor(NetworkLogRepository repository, AtomicReference<LoggerConfig> configRef) {
        this.repository = repository;
        this.configRef = configRef;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        LoggerConfig config = configRef.get();

        if (!config.isLoggingEnabled()) {
            return chain.proceed(request);
        }

        NetworkLogEntity entity = new NetworkLogEntity();
        entity.setUrl(request.url().toString());
        entity.setMethod(request.method());
        entity.setRequestTimestamp(System.currentTimeMillis());

        try {
            entity.setRequestHeaders(formatHeaders(request.headers(), config));
            entity.setRequestBody(readRequestBody(request, config));
        } catch (Exception e) {
            entity.setRequestBody("[failed to read request body: " + e.getMessage() + "]");
        }

        Response response;
        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            entity.setResponseTimestamp(System.currentTimeMillis());
            entity.setLatencyMs(entity.getResponseTimestamp() - entity.getRequestTimestamp());
            entity.setErrorMessage(e.getMessage());
            persist(entity, config);
            throw e;
        }

        entity.setResponseTimestamp(System.currentTimeMillis());
        entity.setLatencyMs(entity.getResponseTimestamp() - entity.getRequestTimestamp());
        entity.setStatusCode(response.code());

        try {
            entity.setResponseHeaders(formatHeaders(response.headers(), config));
            entity.setResponseBody(peekResponseBody(response, config));
        } catch (Exception e) {
            entity.setResponseBody("[failed to read response body: " + e.getMessage() + "]");
        }

        persist(entity, config);
        return response;
    }

    private void persist(NetworkLogEntity entity, LoggerConfig config) {
        if (!config.isDatabaseStorageEnabled()) {
            return;
        }
        try {
            repository.insert(entity);
        } catch (Exception ignored) {
            // Logging must never take down the host app.
        }
    }

    private String formatHeaders(Headers headers, LoggerConfig config) {
        Set<String> redacted = config.getRedactedHeaders();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            String name = headers.name(i);
            String value = redacted.contains(name.toLowerCase()) ? "[redacted]" : headers.value(i);
            sb.append(name).append(": ").append(value).append('\n');
        }
        return sb.toString();
    }

    private String readRequestBody(Request request, LoggerConfig config) throws IOException {
        RequestBody body = request.body();
        if (body == null) {
            return null;
        }
        if (isBinary(body.contentType())) {
            return "[binary body, " + humanReadableBytes(body.contentLength()) + "]";
        }

        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        byte[] bytes = buffer.readByteArray();

        if (!isProbablyText(bytes)) {
            return "[binary body, " + humanReadableBytes(bytes.length) + "]";
        }
        Charset charset = charsetOf(body.contentType());
        return truncate(new String(bytes, charset), config);
    }

    private String peekResponseBody(Response response, LoggerConfig config) throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
            return null;
        }

        if (isBinary(body.contentType())) {
            return "[binary body, " + humanReadableBytes(body.contentLength()) + "]";
        }

        // peekBody copies bytes without consuming the real stream, so the host
        // app's own Retrofit/OkHttp call still receives an untouched body.
        long peekBytes = Math.max(config.getLogBodyMaxLength(), 1) * 4L;
        byte[] bytes = response.peekBody(peekBytes).bytes();

        if (!isProbablyText(bytes)) {
            return "[binary body, " + humanReadableBytes(body.contentLength()) + "]";
        }

        Charset charset = charsetOf(body.contentType());
        return truncate(new String(bytes, charset), config);
    }

    private boolean isBinary(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        String type = mediaType.type();
        String subtype = mediaType.subtype();
        if ("multipart".equals(type) || "image".equals(type) || "video".equals(type) || "audio".equals(type)) {
            return true;
        }
        return "octet-stream".equals(subtype);
    }

    private boolean isProbablyText(byte[] bytes) {
        int limit = Math.min(bytes.length, 64);
        for (int i = 0; i < limit; i++) {
            int b = bytes[i] & 0xFF;
            boolean isWhitespace = b == '\n' || b == '\r' || b == '\t';
            if (b < 0x20 && !isWhitespace) {
                return false;
            }
        }
        return true;
    }

    private Charset charsetOf(MediaType mediaType) {
        Charset charset = mediaType != null ? mediaType.charset(StandardCharsets.UTF_8) : null;
        return charset != null ? charset : StandardCharsets.UTF_8;
    }

    private String truncate(String body, LoggerConfig config) {
        int max = config.getLogBodyMaxLength();
        if (max <= 0 || body.length() <= max) {
            return body;
        }
        return body.substring(0, max) + "... [truncated, " + body.length() + " chars total]";
    }

    private String humanReadableBytes(long bytes) {
        if (bytes < 0) {
            return "unknown size";
        }
        if (bytes < 1024) {
            return bytes + "B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f%cB", bytes / Math.pow(1024, exp), unit);
    }
}
