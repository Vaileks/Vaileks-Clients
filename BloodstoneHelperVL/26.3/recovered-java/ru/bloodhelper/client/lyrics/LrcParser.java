/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.lyrics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.bloodhelper.client.lyrics.LyricLine;
import ru.bloodhelper.client.lyrics.WordTiming;

public final class LrcParser {
    private static final Pattern STAMP = Pattern.compile("\\[(\\d{1,3}):([0-5]\\d)(?:[.:](\\d{1,3}))?]");
    private static final Pattern OFFSET = Pattern.compile("(?im)^[\\h\\uFEFF]*\\[offset:([+-]?\\d{1,7})]\\h*$");
    private static final Pattern WORD_STAMP = Pattern.compile("<\\d{1,3}:[0-5]\\d(?:[.:]\\d{1,3})?>");
    private static final int MAX_CUES = 8192;

    private LrcParser() {
    }

    public static List<LyricLine> parse(String lrc) {
        if (lrc == null || lrc.isBlank() || lrc.length() > 0x100000) {
            return List.of();
        }
        long offset = 0L;
        Matcher offsets = OFFSET.matcher(lrc);
        while (offsets.find()) {
            offset = Long.parseLong(offsets.group(1));
        }
        ArrayList<LyricLine> result = new ArrayList<LyricLine>();
        for (String raw : lrc.split("\\R")) {
            String line = raw.strip();
            if (line.startsWith("\ufeff")) {
                line = line.substring(1);
            }
            Matcher matcher = STAMP.matcher(line);
            ArrayList<Long> timestamps = new ArrayList<Long>();
            int end = 0;
            while (matcher.find() && line.substring(end, matcher.start()).isBlank()) {
                String fraction = matcher.group(3);
                long millis = fraction == null ? 0L : Long.parseLong((fraction + "000").substring(0, 3));
                long time = Long.parseLong(matcher.group(1)) * 60000L + Long.parseLong(matcher.group(2)) * 1000L + millis;
                timestamps.add(time - offset);
                end = matcher.end();
                if (timestamps.size() < 64) continue;
                break;
            }
            if (timestamps.isEmpty()) continue;
            String rawText = line.substring(end);
            Iterator iterator = timestamps.iterator();
            while (iterator.hasNext()) {
                long time = (Long)iterator.next();
                LyricLine cue = WordTiming.enhanced(rawText, time, time - (Long)timestamps.getFirst() - offset);
                String text = cue.text();
                if (text.codePointCount(0, text.length()) > 512) {
                    cue = new LyricLine(time, text.substring(0, text.offsetByCodePoints(0, 512)));
                }
                result.add(cue);
                if (result.size() < 8192) continue;
                break;
            }
            if (result.size() >= 8192) break;
        }
        result.sort(Comparator.comparingLong(LyricLine::timestampMs));
        ArrayList<LyricLine> merged = new ArrayList<LyricLine>();
        for (LyricLine line : result) {
            if (!merged.isEmpty() && ((LyricLine)merged.getLast()).timestampMs() == line.timestampMs()) {
                String text;
                LyricLine previous = (LyricLine)merged.removeLast();
                String string = previous.text().isBlank() ? line.text() : (text = line.text().isBlank() || line.text().equals(previous.text()) ? previous.text() : previous.text() + " / " + line.text());
                if (text.length() > 1024) {
                    text = previous.text();
                }
                merged.add(new LyricLine(line.timestampMs(), text, text.equals(previous.text()) ? previous.words() : (text.equals(line.text()) ? line.words() : List.of())));
                continue;
            }
            merged.add(line);
        }
        return List.copyOf(merged);
    }

    public static List<LyricLine> splitLongLines(List<LyricLine> original) {
        ArrayList<LyricLine> split = new ArrayList<LyricLine>();
        for (int i = 0; i < original.size(); ++i) {
            LyricLine line = original.get(i);
            long duration = i + 1 < original.size() ? original.get(i + 1).timestampMs() - line.timestampMs() : 4000L;
            String[] words = line.text().split("\\s+");
            if (line.wordTimed() || words.length <= 5 || duration < 900L) {
                split.add(line);
                continue;
            }
            int chunks = Math.min((words.length + 2) / 3, Math.min(12, (int)(duration / 450L)));
            int wordsPerChunk = (words.length + chunks - 1) / chunks;
            chunks = (words.length + wordsPerChunk - 1) / wordsPerChunk;
            for (int c = 0; c < chunks; ++c) {
                int start = c * wordsPerChunk;
                int end = Math.min(words.length, start + wordsPerChunk);
                StringBuilder text = new StringBuilder();
                for (int w = start; w < end; ++w) {
                    if (!text.isEmpty()) {
                        text.append(' ');
                    }
                    text.append(words[w]);
                }
                split.add(new LyricLine(line.timestampMs() + duration * (long)c / (long)chunks, text.toString()));
            }
        }
        return List.copyOf(split);
    }

    public static int indexAt(List<LyricLine> lines, long timeMs) {
        int low = 0;
        int high = lines.size() - 1;
        int found = -1;
        while (low <= high) {
            int mid = low + high >>> 1;
            if (lines.get(mid).timestampMs() <= timeMs) {
                found = mid;
                low = mid + 1;
                continue;
            }
            high = mid - 1;
        }
        return found;
    }
}

