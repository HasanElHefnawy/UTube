package com.example.utube.database;

import android.arch.persistence.room.TypeConverter;

import com.example.utube.model.Videos;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class IdTypeConverter {
    private static Moshi moshi = new Moshi.Builder().build();

    @TypeConverter
    public static Videos.Item.Id stringToNestedData(String json) {
        try {
            return moshi.adapter(Videos.Item.Id.class).fromJson(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TypeConverter
    public static String nestedDataToString(Videos.Item.Id nestedData) {
        return moshi.adapter(Videos.Item.Id.class).toJson(nestedData);
    }
}