package com.vbm.logger.sample;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.vbm.logger.NetworkLogger;
import com.vbm.logger.sample.api.ApiClient;
import com.vbm.logger.sample.api.JsonPlaceholderApi;
import com.vbm.logger.ui.NetworkLogActivity;
import com.vbm.logger.viewmodel.NetworkLogViewModel;

import java.io.IOException;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_logs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SampleLogAdapter adapter = new SampleLogAdapter();
        recyclerView.setAdapter(adapter);

        NetworkLogViewModel viewModel = new ViewModelProvider(this).get(NetworkLogViewModel.class);
        viewModel.getLogs().observe(this, adapter::submitList);

        MaterialButton getButton = findViewById(R.id.button_get);
        MaterialButton postButton = findViewById(R.id.button_post);
        MaterialButton failButton = findViewById(R.id.button_fail);
        MaterialButton viewLogsButton = findViewById(R.id.button_view_logs);
        SwitchMaterial bubbleSwitch = findViewById(R.id.switch_debug_bubble);

        getButton.setOnClickListener(v -> performGet());
        postButton.setOnClickListener(v -> performPost());
        failButton.setOnClickListener(v -> performFailingCall());
        viewLogsButton.setOnClickListener(v -> startActivity(NetworkLogActivity.newIntent(this)));

        bubbleSwitch.setChecked(NetworkLogger.getInstance().isFloatingBubbleVisible());
        bubbleSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                NetworkLogger.getInstance().showFloatingBubble();
            } else {
                NetworkLogger.getInstance().hideFloatingBubble();
            }
        });
    }

    private void performGet() {
        ApiClient.getApi().getPost().enqueue(new Callback<JsonPlaceholderApi.Post>() {
            @Override
            public void onResponse(Call<JsonPlaceholderApi.Post> call, Response<JsonPlaceholderApi.Post> response) {
                toast("GET completed: " + response.code());
            }

            @Override
            public void onFailure(Call<JsonPlaceholderApi.Post> call, Throwable t) {
                toast("GET failed: " + t.getMessage());
            }
        });
    }

    private void performPost() {
        JsonPlaceholderApi.Post post = new JsonPlaceholderApi.Post();
        post.title = "NetworkLogger demo";
        post.body = "Hello from the sample app";
        post.userId = 1;

        ApiClient.getApi().createPost(post).enqueue(new Callback<JsonPlaceholderApi.Post>() {
            @Override
            public void onResponse(Call<JsonPlaceholderApi.Post> call, Response<JsonPlaceholderApi.Post> response) {
                toast("POST completed: " + response.code());
            }

            @Override
            public void onFailure(Call<JsonPlaceholderApi.Post> call, Throwable t) {
                toast("POST failed: " + t.getMessage());
            }
        });
    }

    private void performFailingCall() {
        Request request = new Request.Builder()
                .url("https://this-host-does-not-exist.invalid/")
                .build();

        ApiClient.getFailingClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> toast("Call failed as expected: " + e.getMessage()));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                response.close();
            }
        });
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
