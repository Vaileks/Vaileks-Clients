/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CoordinateParser {
    public static final int MAX_TEXT = 4096;
    public static final int MAX_MATCHES = 8;
    private static final String NUMBER = "([+-]?\\d{1,8})(?![\\p{L}\\p{N}_./+-])";
    private static final String SEPARATOR = "[\\s,;]+";
    private static final String START = "(?<![\\p{L}\\p{N}_/])";
    private static final Pattern AXES = Pattern.compile("(?<![\\p{L}\\p{N}_/])x\\s*[:=]?\\s*([+-]?\\d{1,8})(?![\\p{L}\\p{N}_./+-])[\\s,;]+(?:y\\s*[:=]?\\s*([+-]?\\d{1,8})(?![\\p{L}\\p{N}_./+-])[\\s,;]+)?z\\s*[:=]?\\s*([+-]?\\d{1,8})(?![\\p{L}\\p{N}_./+-])", 66);
    private static final Pattern LABELLED = Pattern.compile("(?<![\\p{L}\\p{N}_/])(?:coords?|coordinates?|\u043a\u043e\u043e\u0440\u0434(?:\u044b|\u0438\u043d\u0430\u0442\u044b)?|\u043a\u043e\u0440\u0434\u044b|xyz)\\s*[:=]\\s*([+-]?\\d{1,8})(?![\\p{L}\\p{N}_./+-])[\\s,;]+([+-]?\\d{1,8})(?![\\p{L}\\p{N}_./+-])(?:[\\s,;]+([+-]?\\d{1,8})(?![\\p{L}\\p{N}_./+-]))?", 66);
    private static final Pattern TRIPLE = Pattern.compile("(?<![\\p{L}\\p{N}_./+-])([+-]?\\d{1,8})(?![\\p{L}\\p{N}_./+-])[\\s,;]+([+-]?\\d{1,8})(?![\\p{L}\\p{N}_./+-])[\\s,;]+([+-]?\\d{1,8})(?![\\p{L}\\p{N}_./+-])");

    private CoordinateParser() {
    }

    public static List<Coordinate> find(String text) {
        if (text == null || text.isEmpty() || text.length() > 4096) {
            return List.of();
        }
        ArrayList<Coordinate> candidates = new ArrayList<Coordinate>();
        Matcher axes = AXES.matcher(text);
        while (axes.find() && candidates.size() < 16) {
            CoordinateParser.add(candidates, axes.start(), axes.end(), axes.group(1), axes.group(2), axes.group(3));
        }
        Matcher labelled = LABELLED.matcher(text);
        while (labelled.find() && candidates.size() < 24) {
            CoordinateParser.add(candidates, labelled.start(), labelled.end(), labelled.group(1), labelled.group(3) == null ? null : labelled.group(2), labelled.group(3) == null ? labelled.group(2) : labelled.group(3));
        }
        Matcher triples = TRIPLE.matcher(text);
        while (triples.find() && candidates.size() < 32) {
            if (CoordinateParser.continuesNumbers(text, triples.start() - 1, -1) || CoordinateParser.continuesNumbers(text, triples.end(), 1)) continue;
            CoordinateParser.add(candidates, triples.start(), triples.end(), triples.group(1), triples.group(2), triples.group(3));
        }
        candidates.sort(Comparator.comparingInt(Coordinate::start));
        ArrayList<Coordinate> result = new ArrayList<Coordinate>();
        int end = -1;
        for (Coordinate item : candidates) {
            if (item.start < end) continue;
            result.add(item);
            end = item.end;
            if (result.size() != 8) continue;
            break;
        }
        return List.copyOf(result);
    }

    private static boolean continuesNumbers(String text, int at, int direction) {
        while (at >= 0 && at < text.length() && (Character.isWhitespace(text.charAt(at)) || text.charAt(at) == ',' || text.charAt(at) == ';')) {
            at += direction;
        }
        return at >= 0 && at < text.length() && (Character.isDigit(text.charAt(at)) || text.charAt(at) == '+' || text.charAt(at) == '-');
    }

    private static void add(List<Coordinate> results, int start, int end, String xText, String yText, String zText) {
        try {
            Integer y;
            int x = Integer.parseInt(xText);
            int z = Integer.parseInt(zText);
            Integer n = y = yText == null ? null : Integer.valueOf(yText);
            if (x < -30000000 || x > 30000000 || z < -30000000 || z > 30000000 || y != null && (y < -2048 || y > 2048)) {
                return;
            }
            results.add(new Coordinate(start, end, x, y, z));
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
    }

    public record Coordinate(int start, int end, int x, Integer y, int z) {
    }
}

