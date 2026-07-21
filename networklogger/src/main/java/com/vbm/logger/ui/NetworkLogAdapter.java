package com.vbm.logger.ui;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vbm.logger.R;
import com.vbm.logger.data.entity.NetworkLogEntity;

import java.util.Locale;

class NetworkLogAdapter extends ListAdapter<NetworkLogEntity, NetworkLogAdapter.LogViewHolder> {

    interface OnLogClickListener {
        void onLogClick(NetworkLogEntity entity);
    }

    interface OnLogDeleteListener {
        void onLogDelete(NetworkLogEntity entity);
    }

    private final OnLogClickListener listener;
    private final OnLogDeleteListener deleteListener;

    NetworkLogAdapter(OnLogClickListener listener, OnLogDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    private static final DiffUtil.ItemCallback<NetworkLogEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<NetworkLogEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull NetworkLogEntity oldItem, @NonNull NetworkLogEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull NetworkLogEntity oldItem, @NonNull NetworkLogEntity newItem) {
                    return oldItem.getStatusCode() == newItem.getStatusCode()
                            && oldItem.getLatencyMs() == newItem.getLatencyMs();
                }
            };

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_network_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.bind(getItem(position), position + 1, listener, deleteListener);
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView srNo;
        private final TextView methodBadge;
        private final TextView statusBadge;
        private final TextView latency;
        private final TextView url;
        private final TextView time;
        private final ImageButton deleteButton;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            srNo = itemView.findViewById(R.id.text_sr_no);
            methodBadge = itemView.findViewById(R.id.badge_method);
            statusBadge = itemView.findViewById(R.id.badge_status);
            latency = itemView.findViewById(R.id.text_latency);
            url = itemView.findViewById(R.id.text_url);
            time = itemView.findViewById(R.id.text_time);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }

        void bind(NetworkLogEntity entity, int position,
                  OnLogClickListener listener, OnLogDeleteListener deleteListener) {
            srNo.setText(String.format(Locale.US, "#%d", position));
            methodBadge.setText(entity.getMethod());
            setBadgeColor(methodBadge, methodColor(entity.getMethod()));

            boolean isError = entity.getErrorMessage() != null;
            statusBadge.setText(isError ? "ERR" : String.valueOf(entity.getStatusCode()));
            setBadgeColor(statusBadge, statusColor(entity.getStatusCode(), isError));

            latency.setText(String.format(Locale.US, "%dms", entity.getLatencyMs()));
            url.setText(entity.getUrl());
            time.setText(entity.getFormattedRequestTime());

            itemView.setOnClickListener(v -> listener.onLogClick(entity));
            deleteButton.setOnClickListener(v -> deleteListener.onLogDelete(entity));
        }

        private void setBadgeColor(TextView badge, @ColorRes int colorRes) {
            int color = ContextCompat.getColor(badge.getContext(), colorRes);
            badge.setBackgroundTintList(ColorStateList.valueOf(color));
        }

        @ColorRes
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

        @ColorRes
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
    }
}
