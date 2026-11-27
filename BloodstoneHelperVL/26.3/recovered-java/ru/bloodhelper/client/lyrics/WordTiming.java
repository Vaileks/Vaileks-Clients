/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.lyrics;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import ru.bloodhelper.client.lyrics.LyricLine;

public final class WordTiming {
    private static final Pattern ENHANCED = Pattern.compile("<(\\d{1,3}):([0-5]\\d)(?:[.:](\\d{1,3}))?>");
    private static final Pattern LINE = Pattern.compile("^\\[(\\d{1,9}),(\\d{1,7})](.*)$");
    private static final Pattern KRC_WORD = Pattern.compile("<(\\d{1,9}),(\\d{1,7}),\\d{1,5}>");
    private static final Pattern YRC_WORD = Pattern.compile("\\((\\d{1,9}),(\\d{1,7}),\\d{1,5}\\)");
    private static final byte[] KEY = new byte[]{64, 71, 97, 119, 94, 50, 116, 71, 81, 54, 49, 45, -50, -46, 110, 105};

    private WordTiming() {
    }

    private static String clean(String s) {
        return s.replaceAll("\u00a7.", "").replaceAll("[\\p{Cntrl}]", " ");
    }

    public static LyricLine enhanced(String raw, long lineStart, long shift) {
        List<MatchResult> matches = ENHANCED.matcher(raw).results().limit(512L).toList();
        if (matches.isEmpty()) {
            return new LyricLine(lineStart, WordTiming.clean(raw).strip());
        }
        long lastStamp = Long.MIN_VALUE;
        for (MatchResult mark : matches) {
            long at = WordTiming.stamp(mark);
            if (at < lastStamp) {
                return new LyricLine(lineStart, WordTiming.clean(ENHANCED.matcher(raw).replaceAll("")).strip());
            }
            lastStamp = at;
        }
        StringBuilder text = new StringBuilder(WordTiming.clean(raw.substring(0, matches.getFirst().start())));
        ArrayList<LyricLine.Word> words = new ArrayList<LyricLine.Word>();
        for (int i = 0; i < matches.size(); ++i) {
            int start;
            MatchResult m = matches.get(i);
            String fragment = WordTiming.clean(raw.substring(m.end(), i + 1 < matches.size() ? matches.get(i + 1).start() : raw.length()));
            text.append(fragment);
            long at = WordTiming.stamp(m) + shift;
            long until = i + 1 < matches.size() ? WordTiming.stamp(matches.get(i + 1)) + shift : Long.MAX_VALUE;
            int end = text.length();
            for (start = text.length(); start < end && Character.isWhitespace(text.charAt(start)); ++start) {
            }
            while (end > start && Character.isWhitespace(text.charAt(end - 1))) {
                --end;
            }
            if (start >= end || until < at) continue;
            words.add(new LyricLine.Word(start, end, at, Math.max(at + 1L, until)));
        }
        int leading = text.length() - text.toString().stripLeading().length();
        String value = text.toString().strip();
        if (value.length() > 1024) {
            return new LyricLine(lineStart, value.substring(0, Math.min(512, value.length())));
        }
        ArrayList<LyricLine.Word> result = new ArrayList<LyricLine.Word>();
        long last = Long.MIN_VALUE;
        for (LyricLine.Word word : words) {
            if (word.startMs() < last) {
                return new LyricLine(lineStart, value);
            }
            result.add(new LyricLine.Word(word.start() - leading, word.end() - leading, word.startMs(), word.endMs()));
            last = word.startMs();
        }
        return new LyricLine(lineStart, value, result);
    }

    private static long stamp(MatchResult m) {
        return Long.parseLong(m.group(1)) * 60000L + Long.parseLong(m.group(2)) * 1000L + (m.group(3) == null ? 0L : Long.parseLong((m.group(3) + "000").substring(0, 3)));
    }

    public static List<LyricLine> provider(String source, boolean relative) {
        if (source == null || source.length() > 0x100000) {
            return List.of();
        }
        long offset = 0L;
        Matcher off = Pattern.compile("(?m)^\\[offset:([+-]?\\d{1,7})]$").matcher(source);
        while (off.find()) {
            offset = Long.parseLong(off.group(1));
        }
        ArrayList<LyricLine> lines = new ArrayList<LyricLine>();
        for (String row : source.split("\\R")) {
            String raw;
            Matcher line = LINE.matcher(row);
            if (!line.matches()) continue;
            long start = Long.parseLong(line.group(1));
            long end = start + Long.parseLong(line.group(2));
            List<MatchResult> marks = (relative ? KRC_WORD : YRC_WORD).matcher(raw = line.group(3)).results().limit(512L).toList();
            if (marks.isEmpty()) continue;
            StringBuilder text = new StringBuilder();
            ArrayList<LyricLine.Word> words = new ArrayList<LyricLine.Word>();
            long previous = Long.MIN_VALUE;
            boolean valid = true;
            for (int i = 0; i < marks.size(); ++i) {
                int from;
                MatchResult m = marks.get(i);
                long at = Long.parseLong(m.group(1)) + (relative ? start : 0L);
                long until = at + Long.parseLong(m.group(2));
                String fragment = WordTiming.clean(raw.substring(m.end(), i + 1 < marks.size() ? marks.get(i + 1).start() : raw.length()));
                text.append(fragment);
                int to = text.length();
                for (from = text.length(); from < to && Character.isWhitespace(text.charAt(from)); ++from) {
                }
                while (to > from && Character.isWhitespace(text.charAt(to - 1))) {
                    --to;
                }
                if (at < start || at < previous || until > end + 100L || until < at || text.length() > 1024) {
                    valid = false;
                    break;
                }
                if (from < to) {
                    words.add(new LyricLine.Word(from, to, at - offset, Math.max(at + 1L, until) - offset));
                }
                previous = at;
            }
            if (valid && !words.isEmpty()) {
                lines.add(new LyricLine(start - offset, text.toString(), words));
            }
            if (lines.size() >= 8192) break;
        }
        lines.sort(Comparator.comparingLong(LyricLine::timestampMs));
        return List.copyOf(lines);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static List<LyricLine> decodeKrc(byte[] data) {
        if (data.length < 6 || data.length > 0x100000 || data[0] != 107 || data[1] != 114 || data[2] != 99 || data[3] != 49) {
            return List.of();
        }
        byte[] compressed = Arrays.copyOfRange(data, 4, data.length);
        for (int i = 0; i < compressed.length; ++i) {
            int n = i;
            compressed[n] = (byte)(compressed[n] ^ KEY[i % KEY.length]);
        }
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(compressed);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] block = new byte[4096];
            while (!inflater.finished()) {
                int n = inflater.inflate(block);
                if (n == 0 || result.size() + n > 0x100000) {
                    List<LyricLine> list = List.of();
                    return list;
                }
                result.write(block, 0, n);
            }
            List<LyricLine> list = WordTiming.provider(result.toString(StandardCharsets.UTF_8), true);
            return list;
        }
        catch (DataFormatException invalid) {
            List<LyricLine> list = List.of();
            return list;
        }
        finally {
            inflater.end();
        }
    }

    public static LyricLine.Word active(LyricLine line, long time) {
        for (LyricLine.Word word : line.words()) {
            if (time < word.startMs() || time >= word.endMs()) continue;
            return word;
        }
        return null;
    }
}

