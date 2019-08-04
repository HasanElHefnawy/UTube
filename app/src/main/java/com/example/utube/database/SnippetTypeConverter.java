package com.example.utube.database;

import android.arch.persistence.room.TypeConverter;

import com.example.utube.model.Videos;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class SnippetTypeConverter {
    private static Moshi moshi = new Moshi.Builder().build();

    @TypeConverter
    public static Videos.Item.Snippet stringToNestedData(String json) {
        try {
            return moshi.adapter(Videos.Item.Snippet.class).fromJson(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TypeConverter
    public static String nestedDataToString(Videos.Item.Snippet nestedData) {
        return moshi.adapter(Videos.Item.Snippet.class).toJson(nestedData);
    }
}