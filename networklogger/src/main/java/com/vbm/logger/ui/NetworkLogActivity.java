package com.vbm.logger.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import com.vbm.logger.R;
import com.vbm.logger.data.NetworkLogSortOrder;
import com.vbm.logger.viewmodel.NetworkLogViewModel;

/**
 * Ready-made log viewer screen. Consumers can launch it with
 * {@code startActivity(NetworkLogActivity.newIntent(context))}.
 * Tap a row for details, use the delete icon to remove a single entry, filter
 * chips to sort, and the toolbar delete icon to clear everything.
 */
public class NetworkLogActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, NetworkLogActivity.class);
    }

    private NetworkLogAdapter adapter;
    private NetworkLogViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_log);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Network Logs");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(NetworkLogViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recycler_network_logs);
        TextView emptyState = findViewById(R.id.text_empty_state);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NetworkLogAdapter(
                entity -> startActivity(NetworkLogDetailActivity.newIntent(this, entity.getId())),
                entity -> viewModel.deleteLogById(entity.getId()));
        recyclerView.setAdapter(adapter);

        viewModel.getLogs().observe(this, logs -> {
            adapter.submitList(logs);
            boolean empty = logs == null || logs.isEmpty();
            emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        ChipGroup sortChips = findViewById(R.id.chip_group_sort);
        sortChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_sort_latency) {
                viewModel.setSortOrder(NetworkLogSortOrder.LATENCY_DESC);
            } else if (checkedId == R.id.chip_sort_status) {
                viewModel.setSortOrder(NetworkLogSortOrder.STATUS_CODE);
            } else {
                viewModel.setSortOrder(NetworkLogSortOrder.TIME_DESC);
            }
        });

        findViewById(R.id.button_delete_all).setOnClickListener(v -> confirmDeleteAll());
    }

    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
                .setTitle("Delete all logs?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.clearLogs())
                .setNegativeButton("Cancel", null)
                .show();
    }
}
