package com.vbm.logger.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.vbm.logger.NetworkLogger;
import com.vbm.logger.R;
import com.vbm.logger.data.entity.NetworkLogEntity;
import com.vbm.logger.util.JsonFormatter;

import java.util.Locale;

public class NetworkLogDetailActivity extends AppCompatActivity {

    private static final String EXTRA_LOG_ID = "extra_log_id";

    public static Intent newIntent(Context context, long logId) {
        Intent intent = new Intent(context, NetworkLogDetailActivity.class);
        intent.putExtra(EXTRA_LOG_ID, logId);
        return intent;
    }

    private String requestBodyRaw;
    private String responseBodyRaw;
    private boolean requestBeautified = true;
    private boolean responseBeautified = true;

    private TextView requestBodyView;
    private TextView responseBodyView;
    private TextView requestFormatButton;
    private TextView responseFormatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_log_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Log Detail");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        setupHeadersToggle();

        requestBodyView = findViewById(R.id.text_request_body);
        responseBodyView = findViewById(R.id.text_response_body);
        requestFormatButton = findViewById(R.id.button_request_format);
        responseFormatButton = findViewById(R.id.button_response_format);

        long logId = getIntent().getLongExtra(EXTRA_LOG_ID, -1);
        NetworkLogger.getInstance().getLogById(logId, this::onLogLoaded);
    }

    private void setupHeadersToggle() {
        View toggleRow = findViewById(R.id.layout_headers_toggle);
        View content = findViewById(R.id.layout_headers_content);
        ImageView chevron = findViewById(R.id.icon_headers_chevron);

        toggleRow.setOnClickListener(v -> {
            boolean expanding = content.getVisibility() != View.VISIBLE;
            content.setVisibility(expanding ? View.VISIBLE : View.GONE);
            chevron.setRotation(0f);
            chevron.animate().rotation(expanding ? 180f : 0f).setDuration(150).start();
        });
    }

    private void onLogLoaded(NetworkLogEntity entity) {
        if (entity == null) {
            findViewById(R.id.text_url).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.text_times)).setText(R.string.network_logger_log_not_found);
            return;
        }

        bindOverview(entity);
        bindHeaders(entity);

        requestBodyRaw = entity.getRequestBody();
        responseBodyRaw = entity.getResponseBody();
        setupBodySection(requestBodyRaw, requestBodyView, requestFormatButton, true);
        setupBodySection(responseBodyRaw, responseBodyView, responseFormatButton, false);

        findViewById(R.id.button_copy_request).setOnClickListener(
                v -> copyToClipboard("Request body", requestBodyView.getText().toString()));
        findViewById(R.id.button_copy_response).setOnClickListener(
                v -> copyToClipboard("Response body", responseBodyView.getText().toString()));
    }

    private void bindOverview(NetworkLogEntity entity) {
        TextView methodBadge = findViewById(R.id.badge_method);
        TextView statusBadge = findViewById(R.id.badge_status);
        TextView url = findViewById(R.id.text_url);
        TextView latency = findViewById(R.id.text_latency);
        TextView times = findViewById(R.id.text_times);
        TextView error = findViewById(R.id.text_error);

        methodBadge.setText(entity.getMethod());
        setBadgeColor(methodBadge, methodColor(entity.getMethod()));

        boolean isError = entity.getErrorMessage() != null;
        statusBadge.setText(isError ? "ERR" : String.valueOf(entity.getStatusCode()));
        setBadgeColor(statusBadge, statusColor(entity.getStatusCode(), isError));

        url.setText(entity.getUrl());
        latency.setText(String.format(Locale.US, "%dms", entity.getLatencyMs()));
        times.setText(String.format(Locale.US, "Requested: %s\nResponded: %s",
                entity.getFormattedRequestTime(), entity.getFormattedResponseTime()));

        if (isError) {
            error.setVisibility(View.VISIBLE);
            error.setText(String.format(Locale.US, "Error: %s", entity.getErrorMessage()));
        } else {
            error.setVisibility(View.GONE);
        }
    }

    private void bindHeaders(NetworkLogEntity entity) {
        ((TextView) findViewById(R.id.text_request_headers)).setText(safe(entity.getRequestHeaders()));
        ((TextView) findViewById(R.id.text_response_headers)).setText(safe(entity.getResponseHeaders()));
    }

    private void setupBodySection(String raw, TextView bodyView, TextView formatButton, boolean isRequest) {
        if (!JsonFormatter.isJson(raw)) {
            formatButton.setVisibility(View.GONE);
            bodyView.setText(safe(raw));
            return;
        }

        formatButton.setVisibility(View.VISIBLE);
        renderBody(raw, bodyView, formatButton, isRequest);
        formatButton.setOnClickListener(v -> {
            if (isRequest) {
                requestBeautified = !requestBeautified;
            } else {
                responseBeautified = !responseBeautified;
            }
            renderBody(raw, bodyView, formatButton, isRequest);
        });
    }

    private void renderBody(String raw, TextView bodyView, TextView formatButton, boolean isRequest) {
        boolean beautified = isRequest ? requestBeautified : responseBeautified;
        bodyView.setText(beautified ? JsonFormatter.beautify(raw) : JsonFormatter.minify(raw));
        formatButton.setText(beautified ? "Minify" : "Beautify");
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void setBadgeColor(TextView badge, int colorRes) {
        int color = ContextCompat.getColor(badge.getContext(), colorRes);
        badge.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private int methodColor(String method) {
        if (method == null) {
            return R.color.nl_method_default;
        }
        switch (method.toUpperCase(Locale.US)) {
            case "GET":
                return R.color.nl_method_get;
            case "POST":
                return R.color.nl_method_post;
            case "PUT":
                return R.color.nl_method_put;
            case "PATCH":
                return R.color.nl_method_patch;
            case "DELETE":
                return R.color.nl_method_delete;
            default:
                return R.color.nl_method_default;
        }
    }

    private int statusColor(int statusCode, boolean isError) {
        if (isError) {
            return R.color.nl_status_error;
        }
        if (statusCode >= 200 && statusCode < 300) {
            return R.color.nl_status_2xx;
        }
        if (statusCode >= 300 && statusCode < 400) {
            return R.color.nl_status_3xx;
        }
        if (statusCode >= 400 && statusCode < 500) {
            return R.color.nl_status_4xx;
        }
        if (statusCode >= 500 && statusCode < 600) {
            return R.color.nl_status_5xx;
        }
        return R.color.nl_status_default;
    }

    private String safe(String value) {
        return value != null && !value.isEmpty() ? value : "(none)";
    }
}
