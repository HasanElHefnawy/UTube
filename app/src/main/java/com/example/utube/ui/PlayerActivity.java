package com.example.utube.ui;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.utube.R;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "zzzzz PlayerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Bundle bundle = getIntent().getExtras();
        String videoId = "";
        if (bundle != null) {
            videoId = bundle.getString("videoId");
            Log.e(TAG, "onCreate: videoId " + videoId);
        }
        if (savedInstanceState == null) {
            PlayerFragment playerFragment = new PlayerFragment();
            playerFragment.setVideoId(videoId);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.container, playerFragment)
                    .commit();
        }
    }
}
