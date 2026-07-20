package com.vbm.logger.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vbm.logger.R;
import com.vbm.logger.viewmodel.NetworkLogViewModel;

/**
 * Ready-made log viewer screen. Consumers can launch it with
 * {@code startActivity(NetworkLogActivity.newIntent(context))}.
 */
public class NetworkLogActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, NetworkLogActivity.class);
    }

    private NetworkLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_log);

        RecyclerView recyclerView = findViewById(R.id.recycler_network_logs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NetworkLogAdapter(entity ->
                startActivity(NetworkLogDetailActivity.newIntent(this, entity.getId())));
        recyclerView.setAdapter(adapter);

        NetworkLogViewModel viewModel = new ViewModelProvider(this).get(NetworkLogViewModel.class);
        viewModel.getLogs().observe(this, adapter::submitList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Clear logs");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1) {
            new ViewModelProvider(this).get(NetworkLogViewModel.class).clearLogs();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
