package com.vbm.logger.sample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vbm.logger.data.entity.NetworkLogEntity;

import java.util.Locale;

class SampleLogAdapter extends ListAdapter<NetworkLogEntity, SampleLogAdapter.ViewHolder> {

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

    SampleLogAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView line1;
        private final TextView line2;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            line1 = itemView.findViewById(android.R.id.text1);
            line2 = itemView.findViewById(android.R.id.text2);
        }

        void bind(NetworkLogEntity entity) {
            line1.setText(String.format(Locale.US, "%s  %s", entity.getMethod(), entity.getUrl()));

            String status = entity.getErrorMessage() != null
                    ? "ERROR: " + entity.getErrorMessage()
                    : String.valueOf(entity.getStatusCode());
            line2.setText(String.format(Locale.US, "%s · %dms", status, entity.getLatencyMs()));
        }
    }
}
