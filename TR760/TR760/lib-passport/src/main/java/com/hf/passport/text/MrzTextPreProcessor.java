package com.hf.passport.text;

import static com.hf.passport.other.StringUtils.repeat;

import com.innovatrics.mrz.types.MrzFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MrzTextPreProcessor {

    private static final int MIN_SIZE_THRESHOLD = 4;
    private static final int MIN_CHAR_LENGTH_PER_LINE = 30;
    private static final int MAX_POSSIBLE_CHAR_LENGTH_PER_LINE = 44;

    private static Pattern regexK = Pattern.compile("<K+<");
    private static Pattern regexS = Pattern.compile("<S+<");
    private static Pattern regexE = Pattern.compile("<E+<");
    private static Pattern regexC = Pattern.compile("<C+<");
    private static Pattern nonValidMrzChar = Pattern.compile("[^\\n<\\dA-Z]");

    public static String correctWrongFiller(String input, Pattern... regexs) {
        String processed = input;

        for (Pattern regex : regexs) {
            Matcher matcher = regex.matcher(processed);
            while (matcher.find()) {
                processed = matcher.replaceAll(repeat("<", matcher.group().length()));
            }
        }
        return processed;
    }
    public static List<String> fillToMinimumSize(List<String> lines, int minSize) {
        return lines.stream()
                .map(line -> line.length() >= minSize - MIN_SIZE_THRESHOLD && line.length() < minSize
                        ? line + repeat("<", minSize - line.length())
                        : line)
                .collect(Collectors.toList());
    }

    public static List<String> fillToSameSize(List<String> lines) {
        int maxLength = lines.stream().mapToInt(String::length).max().orElse(0);
        return fillToMinimumSize(lines, maxLength);
    }

    public static String correctLines(String input) {
        try {
            List<String> lines = new ArrayList<>(Arrays.asList(input.split("\n")));
            MrzFormat format = MrzFormat.get(input); // Assume MrzFormat.get is a static method
            switch (format) {
                case MRTD_TD1:
                    lines.set(1, replaceWithinRange(lines.get(1), 0, 6, "O", "0"));
                    return fillToMinimumSizeAndJoinTogether(lines, 30);
                case FRENCH_ID:
                    lines.set(1, replaceWithinRange(lines.get(1), 27, 33, "O", "0"));
                    return fillToMinimumSizeAndJoinTogether(lines, 36);
                case MRV_VISA_B:
                    lines.set(1, replaceWithinRange(lines.get(1), 13, 19, "O", "0"));
                    return fillToMinimumSizeAndJoinTogether(lines, 36);
                case MRTD_TD2:
                    lines.set(1, replaceWithinRange(lines.get(1), 13, 19, "O", "0"));
                    return fillToMinimumSizeAndJoinTogether(lines, 36);
                case MRV_VISA_A:
                    lines.set(1, replaceWithinRange(lines.get(1), 13, 19, "O", "0"));
                    return fillToMinimumSizeAndJoinTogether(lines, 44);
                case PASSPORT:
                    lines.set(1, replaceWithinRange(lines.get(1), 13, 19, "O", "0"));
                    return fillToMinimumSizeAndJoinTogether(lines, 44);
                case SLOVAK_ID_234:
                    lines.set(1, replaceWithinRange(lines.get(1), 13, 19, "O", "0"));
                    return fillToMinimumSizeAndJoinTogether(lines, 34);
                default:
                    return input;
            }
        } catch (Exception e) {
            // Log the exception using Timber or any other logging framework
            return input;
        }
    }

    private static String replaceWithinRange(String text, int start, int end, String from, String to) {
        StringBuilder sb = new StringBuilder(text);
        for (int i = start; i < end && i < sb.length(); i++) {
            if (sb.substring(i, i + 1).equals(from)) {
                sb.setCharAt(i, to.charAt(0));
            }
        }
        return sb.toString();
    }

    private static String fillToMinimumSizeAndJoinTogether(List<String> lines, int minSize) {
        // Assume fillToMinimumSize is a method that fills strings in the list to the minimum size
        List<String> filledLines = fillToMinimumSize(lines, minSize);
        return String.join("\n", filledLines);
    }

    public static String process(String raw) {
        int lineBreaks = (int) raw.chars().filter(ch -> ch == '\n').count();
        if (lineBreaks < 1) {
            return null;
        }

        String preProcessed = raw.replace(" ", "")
                .toUpperCase(Locale.ENGLISH)
                .replaceAll(nonValidMrzChar.toString(), "<");
        List<String> lines = new ArrayList<>(Arrays.asList(preProcessed.split("\n")));
        lines = lines.stream()
                .filter(line -> line.length() >= MIN_CHAR_LENGTH_PER_LINE - MIN_SIZE_THRESHOLD
                        && line.length() <= MAX_POSSIBLE_CHAR_LENGTH_PER_LINE)
                .collect(Collectors.toList());
        lines = fillToSameSize(lines);
        String joinedString = String.join("\n", lines);
        joinedString = correctWrongFiller(joinedString, regexS, regexK, regexE, regexC);
        joinedString = correctLines(joinedString);

        return checkIfQualifiedForMrzParsing(joinedString) ? joinedString : null;
    }

    private static boolean checkIfQualifiedForMrzParsing(String text) {
        int lineBreaks = (int) text.chars().filter(ch -> ch == '\n').count();
        return lineBreaks >= 1 && lineBreaks <= 2 && stringsSameLength(text);
    }

    private static boolean stringsSameLength(String text) {
        String[] lines = text.split("\n");
        int firstLength = lines[0].length();
        for (String line : lines) {
            if (line.length() != firstLength) {
                return false;
            }
        }
        return true;
    }

    // Add a main method or other necessary methods as needed
}