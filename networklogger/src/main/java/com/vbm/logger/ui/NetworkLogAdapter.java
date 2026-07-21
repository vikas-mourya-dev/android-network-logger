package com.vbm.logger.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    interface OnLogLongClickListener {
        void onLogLongClick(NetworkLogEntity entity);
    }

    private final OnLogClickListener listener;
    private final OnLogLongClickListener longClickListener;

    NetworkLogAdapter(OnLogClickListener listener, OnLogLongClickListener longClickListener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.longClickListener = longClickListener;
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
        holder.bind(getItem(position), listener, longClickListener);
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView methodAndUrl;
        private final TextView statusAndLatency;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            methodAndUrl = itemView.findViewById(R.id.text_method_url);
            statusAndLatency = itemView.findViewById(R.id.text_status_latency);
        }

        void bind(NetworkLogEntity entity, OnLogClickListener listener, OnLogLongClickListener longClickListener) {
            methodAndUrl.setText(String.format(Locale.US, "%s  %s", entity.getMethod(), entity.getUrl()));

            String status = entity.getErrorMessage() != null
                    ? "ERROR: " + entity.getErrorMessage()
                    : String.valueOf(entity.getStatusCode());
            statusAndLatency.setText(String.format(Locale.US, "%s · %dms · %s",
                    status, entity.getLatencyMs(), entity.getFormattedRequestTime()));

            itemView.setOnClickListener(v -> listener.onLogClick(entity));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onLogLongClick(entity);
                return true;
            });
        }
    }
}
