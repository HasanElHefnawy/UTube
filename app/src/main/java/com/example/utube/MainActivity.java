package com.example.utube;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.widget.Toast;

import com.example.utube.databinding.ActivityMainBinding;
import com.example.utube.model.Video;
import com.example.utube.network.RetrofitApiClient;
import com.example.utube.network.RetrofitApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        final List<Video.Item> items = new ArrayList<>();
        final VideoAdapter adapter = new VideoAdapter(items, this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RetrofitApiService retrofitApiService = RetrofitApiClient.getClient().create(RetrofitApiService.class);
        Call<Video> call = retrofitApiService.getAllVideos(binding.searchEditText.getText().toString());
        call.enqueue(new Callback<Video>() {
            @Override
            public void onResponse(@NonNull Call<Video> call, @NonNull Response<Video> response) {
                if (response.body() != null) {
                    Log.e("zzzzz", "response " + response);
                    Log.e("zzzzz", "response.body().getItems().size() " + response.body().getItems().size());
                    items.addAll(response.body().getItems());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Video> call, @NonNull Throwable throwable) {
                Log.e("zzzzz", "throwable " + throwable);
                Toast.makeText(MainActivity.this, "Error!!!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
