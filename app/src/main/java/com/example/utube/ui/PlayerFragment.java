/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.utube.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Objects;

public class PlayerFragment extends Fragment implements Player.EventListener {
    private static final String TAG = "zzzzz PlayerFragment";
    private SimpleExoPlayer exoPlayer;
    private PlayerView playerView;
    private long videoCurrentPosition;
    private String videoId;

    public PlayerFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            videoId = savedInstanceState.getString("videoId");
        }
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        playerView = rootView.findViewById(R.id.playerView);
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
                Log.e(TAG, "videoId " + videoId);
                Toast.makeText(Objects.requireNonNull(getContext()), e.getMessage() + " videoId " + videoId, Toast.LENGTH_LONG).show();
            }
        }).Extract(videoId);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.seekTo(0, videoCurrentPosition);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            videoCurrentPosition = exoPlayer.getCurrentPosition();
        }
    }

    private void initializeMediaSession() {
        // Create a MediaSessionCompat.
        MediaSessionCompat mMediaSession = new MediaSessionCompat(Objects.requireNonNull(getContext()), TAG);
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
            exoPlayer = ExoPlayerFactory.newSimpleInstance(Objects.requireNonNull(getContext()));
            DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(Objects.requireNonNull(getContext()), "Test1"));
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

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle currentState) {
        currentState.putString("videoId", videoId);
    }
}
