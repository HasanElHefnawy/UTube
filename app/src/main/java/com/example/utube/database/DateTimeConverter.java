package com.example.utube.database;

import android.arch.persistence.room.TypeConverter;

import org.joda.time.DateTime;

public class DateTimeConverter {
    @TypeConverter
    public static DateTime toDateTime(Long millis) {
        return millis == null ? null : new DateTime(millis);
    }

    @TypeConverter
    public static Long toMillis(DateTime dateTime) {
        return dateTime == null ? null : dateTime.getMillis();
    }
}
