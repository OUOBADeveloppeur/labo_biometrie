package com.hf.passport.other;

public class StringUtils {

    public static String replaceWithinRange(String input, int fromIndex, int toIndex, String toFind, String replaceWith) {
        return input.substring(0, fromIndex) + input.substring(fromIndex, toIndex).replace(toFind, replaceWith) + input.substring(toIndex);
    }

    public static String replaceWithinRange(String input, int fromIndex, String toFind, String replaceWith) {
        return replaceWithinRange(input, fromIndex, input.length(), toFind, replaceWith);
    }

    public static String repeat(String s, int n) {
        if (s == null || n <= 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < n; i++) {
            result.append(s);
        }

        return result.toString();
    }
}

