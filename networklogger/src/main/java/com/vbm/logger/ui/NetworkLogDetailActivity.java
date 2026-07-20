package com.vbm.logger.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vbm.logger.NetworkLogger;
import com.vbm.logger.R;
import com.vbm.logger.data.entity.NetworkLogEntity;

import java.util.Locale;

public class NetworkLogDetailActivity extends AppCompatActivity {

    private static final String EXTRA_LOG_ID = "extra_log_id";

    public static Intent newIntent(Context context, long logId) {
        Intent intent = new Intent(context, NetworkLogDetailActivity.class);
        intent.putExtra(EXTRA_LOG_ID, logId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_log_detail);

        long logId = getIntent().getLongExtra(EXTRA_LOG_ID, -1);
        TextView detailText = findViewById(R.id.text_log_detail);

        NetworkLogger.getInstance().getLogById(logId, entity -> {
            if (entity == null) {
                detailText.setText(R.string.network_logger_log_not_found);
                return;
            }
            detailText.setText(formatDetail(entity));
        });
    }

    private String formatDetail(NetworkLogEntity entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getMethod()).append(' ').append(entity.getUrl()).append("\n\n");
        sb.append(String.format(Locale.US, "Status: %d    Latency: %dms\n\n",
                entity.getStatusCode(), entity.getLatencyMs()));

        if (entity.getErrorMessage() != null) {
            sb.append("Error: ").append(entity.getErrorMessage()).append("\n\n");
        }

        sb.append("--- Request Headers ---\n").append(safe(entity.getRequestHeaders())).append('\n');
        sb.append("--- Request Body ---\n").append(safe(entity.getRequestBody())).append("\n\n");
        sb.append("--- Response Headers ---\n").append(safe(entity.getResponseHeaders())).append('\n');
        sb.append("--- Response Body ---\n").append(safe(entity.getResponseBody()));
        return sb.toString();
    }

    private String safe(String value) {
        return value != null ? value : "(none)";
    }
}
