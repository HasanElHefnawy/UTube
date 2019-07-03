package com.example.utube;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.utube.model.Video;
import com.example.utube.network.RetrofitApiClient;
import com.example.utube.network.RetrofitApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RetrofitApiService retrofitApiService = RetrofitApiClient.getClient().create(RetrofitApiService.class);

        Call<Video> call = retrofitApiService.getAllVideos("Java Android");
        call.enqueue(new Callback<Video>() {
            @Override
            public void onResponse(@NonNull Call<Video> call, @NonNull Response<Video> response) {
                if (response.body() != null) {
                    Log.e("zzzzz", "response " + response);
                    Log.e("zzzzz", "response.body().getItems().size() " + response.body().getItems().size());
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
