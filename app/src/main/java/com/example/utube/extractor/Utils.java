package com.example.utube.extractor;

import java.util.ArrayList;
import java.util.List;

class Utils {

    static List<YoutubeMedia> filterInvalidLinks(List<YoutubeMedia> urls) {
        List<YoutubeMedia> workingLinks = new ArrayList<>();
        for (YoutubeMedia media : urls) {
            if (!media.getUrl().contains("&dur=0.0")) {
                workingLinks.add(media);
            }
        }
        return workingLinks;
    }

    static String extractVideoID(String Url) {
        String r = "(?<=(be/|v=))(.*?)(?=(&|\n| |\\z))";
        if (RegexUtils.hasMatch(r, Url)) {
            return RegexUtils.matchGroup(r, Url);
        }
        return Url;
    }

    static boolean isListContain(List<String> arraylist, String statement) {
        for (String str : arraylist) {
            if (statement.toLowerCase().contains(str)) {
                return true;
            }
        }
        return false;
    }
}
