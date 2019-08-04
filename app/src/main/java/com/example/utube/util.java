package com.example.utube;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class util {

    public static String parseDuration(Context context, String duration) {
        if (duration.equals("PT0S") || duration.equals("PT0H0M0S"))
            return "Live";
        Period period = Period.parse(duration);
        int hours = period.getHours();
        int mins = period.getMinutes();
        int secs = period.getSeconds();
        if (hours == 0)
            return String.format(context.getResources().getStringArray(R.array.duration)[0], mins, secs);
        else
            return String.format(context.getResources().getStringArray(R.array.duration)[1], hours, mins, secs);
    }

    public static String parseDateTime(String publishedAt) {
        DateTime publishedAtDateTime = DateTime.parse(publishedAt);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MMM, d yyyy");
        return dateTimeFormatter.print(publishedAtDateTime);
    }
}
