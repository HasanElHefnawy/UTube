package com.example.utube.activity;

import android.net.Uri;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.utube.R;
import com.example.utube.extractor.ExtractorException;
import com.example.utube.extractor.YoutubeMedia;
import com.example.utube.extractor.YoutubeMeta;
import com.example.utube.extractor.YoutubeStreamExtractor;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

public class PlayerActivity extends AppCompatActivity implements Player.EventListener {
    private static final String TAG = "zzzzz PlayerActivity";
    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;
    private long videoCurrentPosition;

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
        playerView = findViewById(R.id.playerView);
        playerView.setDefaultArtwork(getResources().getDrawable(R.drawable.ic_launcher_background));
        initializeMediaSession();
        new YoutubeStreamExtractor(new YoutubeStreamExtractor.ExtractorListner() {
            @Override
            public void onExtractionDone(List<YoutubeMedia> adaptiveStream, List<YoutubeMedia> muxedStream, YoutubeMeta meta) {
                if (muxedStream.isEmpty()) {
                    Log.e(TAG, "null ha");
                    return;
                }
                String url = muxedStream.get(0).getUrl();
                Log.e(TAG, url);
                initializePlayer(Uri.parse(url));
            }

            @Override
            public void onExtractionGoesWrong(final ExtractorException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).Extract(videoId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.seekTo(0, videoCurrentPosition);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            videoCurrentPosition = exoPlayer.getCurrentPosition();
        }
    }

    private void initializeMediaSession() {
        // Create a MediaSessionCompat.
        MediaSessionCompat mMediaSession = new MediaSessionCompat(this, TAG);
        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);
        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        PlaybackStateCompat.Builder mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());
        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());
        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);
    }

    private void initializePlayer(Uri mediaUri) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this);
            DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "Test1"));
            // If the file type is .m3u, then it's HLS (HTTP Live Streaming)
            if (mediaUri.toString().contains("m3u")) {
                HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaUri);
                exoPlayer.prepare(hlsMediaSource);
            } else {
                ExtractorMediaSource.Factory extractorMediaSourceFactory = new ExtractorMediaSource.Factory(dataSourceFactory);
                ExtractorMediaSource extractorMediaSource = extractorMediaSourceFactory.createMediaSource(mediaUri);
                exoPlayer.prepare(extractorMediaSource);
            }
            playerView.setPlayer(exoPlayer);
            exoPlayer.addListener(this);
        }
    }

    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            exoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            exoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            exoPlayer.seekTo(0);
        }
    }
}
