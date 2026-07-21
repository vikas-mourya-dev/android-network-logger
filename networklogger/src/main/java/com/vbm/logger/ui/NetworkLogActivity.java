package com.vbm.logger.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vbm.logger.R;
import com.vbm.logger.data.NetworkLogSortOrder;
import com.vbm.logger.viewmodel.NetworkLogViewModel;

/**
 * Ready-made log viewer screen. Consumers can launch it with
 * {@code startActivity(NetworkLogActivity.newIntent(context))}.
 * Tap a row for details, long-press to delete it, use the menu to sort or clear all.
 */
public class NetworkLogActivity extends AppCompatActivity {

    private static final int MENU_SORT_TIME = 1;
    private static final int MENU_SORT_LATENCY = 2;
    private static final int MENU_SORT_STATUS = 3;
    private static final int MENU_CLEAR = 4;

    public static Intent newIntent(Context context) {
        return new Intent(context, NetworkLogActivity.class);
    }

    private NetworkLogAdapter adapter;
    private NetworkLogViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_log);

        viewModel = new ViewModelProvider(this).get(NetworkLogViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recycler_network_logs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NetworkLogAdapter(
                entity -> startActivity(NetworkLogDetailActivity.newIntent(this, entity.getId())),
                entity -> {
                    viewModel.deleteLogById(entity.getId());
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                });
        recyclerView.setAdapter(adapter);

        viewModel.getLogs().observe(this, adapter::submitList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SORT_TIME, 0, "Sort: Time");
        menu.add(0, MENU_SORT_LATENCY, 0, "Sort: Latency");
        menu.add(0, MENU_SORT_STATUS, 0, "Sort: Status");
        menu.add(0, MENU_CLEAR, 0, "Clear logs");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == MENU_SORT_TIME) {
            viewModel.setSortOrder(NetworkLogSortOrder.TIME_DESC);
            return true;
        }
        if (item.getItemId() == MENU_SORT_LATENCY) {
            viewModel.setSortOrder(NetworkLogSortOrder.LATENCY_DESC);
            return true;
        }
        if (item.getItemId() == MENU_SORT_STATUS) {
            viewModel.setSortOrder(NetworkLogSortOrder.STATUS_CODE);
            return true;
        }
        if (item.getItemId() == MENU_CLEAR) {
            viewModel.clearLogs();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
