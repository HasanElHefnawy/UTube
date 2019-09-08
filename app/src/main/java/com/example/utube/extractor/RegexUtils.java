package com.example.utube.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexUtils {

    static String matchGroup(String pattern, String input) {
        Pattern pat = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher mat = pat.matcher(input);
        if (mat.find()) {
            return mat.group();
        } else
            return null;
    }

    static List<String> getAllMatches(String pattern, String input) {
        Pattern pat = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher mat = pat.matcher(input);
        List<String> list = new ArrayList<>();
        while (mat.find()) {
            list.add(mat.group());
        }
        return list;
    }

    static boolean hasMatch(String pattern, String input) {
        Pattern pat = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher mat = pat.matcher(input);
        return mat.find();
    }
}
