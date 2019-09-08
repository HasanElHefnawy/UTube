package com.example.utube.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.util.Log;

import com.example.utube.model.Videos;

@Database(entities = {Videos.Item.class}, version = 1, exportSchema = false)
@TypeConverters(value = {IdTypeConverter.class, SnippetTypeConverter.class, ThumbnailsTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "zzzzz " + AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    public static final String DATABASE_NAME = "videos.db";
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        Log.e(TAG, "getInstance: sInstance " + sInstance);
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.e(TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .build();
            }
        }
        Log.e(TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract VideoDao videoDao();

}
