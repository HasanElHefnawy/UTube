package com.example.utube.di;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.NonNull;

import com.example.utube.database.AppDatabase;
import com.example.utube.database.VideoDao;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dagger.Module;
import dagger.Provides;

@Module(includes = ContextModule.class)
public class DatabaseModule {

    public static final String DATABASE_NAME = "videos.db";

    @Provides
    @SingletonApplicationScope
    AppDatabase provideDatabase(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
                .build();
    }

    @Provides
    @SingletonApplicationScope
    VideoDao provideVideoDao(@NonNull AppDatabase appDatabase) {
        return appDatabase.videoDao();
    }

    @Provides
    @SingletonApplicationScope
    Executor provideExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
