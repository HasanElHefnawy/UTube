package com.example.utube.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.utube.R;

public class MainActivity extends AppCompatActivity implements MainFragment.VideoItemClickListener {
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // This layout will only initially exist in the two-pane device case
        mTwoPane = findViewById(R.id.container) != null;
    }

    @Override
    public void onVideoItemClicked(String videoId) {
        if (mTwoPane) {
            // Create two-pane interaction
            PlayerFragment playerFragment = new PlayerFragment();
            playerFragment.setVideoId(videoId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, playerFragment)
                    .commit();
        } else {
            // Handle the single-pane phone case
            Bundle bundle = new Bundle();
            bundle.putString("videoId", videoId);
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}
