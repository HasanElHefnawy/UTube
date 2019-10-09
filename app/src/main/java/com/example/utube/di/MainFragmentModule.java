package com.example.utube.di;

import android.content.Context;

import com.example.utube.ui.VideoAdapter;

import dagger.Module;
import dagger.Provides;

@Module
public class MainFragmentModule {
    private Context context;

    public MainFragmentModule(Context context) {
        this.context = context;
    }

    @Provides
    VideoAdapter provideVideoAdapter() {
        return new VideoAdapter(context);
    }
}
