package com.example.utube.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.example.utube.model.Videos;

@Database(entities = {Videos.Item.class}, version = 1, exportSchema = false)
@TypeConverters(value = {IdTypeConverter.class, SnippetTypeConverter.class, ThumbnailsTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract VideoDao videoDao();

}
