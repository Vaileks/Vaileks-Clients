/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.lyrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ru.bloodhelper.client.lyrics.LyricLine;

public final class WorldLyricWords {
    private WorldLyricWords() {
    }

    public static List<Cue> cues(LyricLine line, long end, boolean split) {
        if (split && line.wordTimed()) {
            return line.words().stream().limit(48L).map(w -> new Cue(line.text().substring(w.start(), w.end()), w.startMs())).toList();
        }
        return WorldLyricWords.cues(line.text(), line.timestampMs(), end, split).stream().map(c -> new Cue(c.text(), line.timestampMs())).toList();
    }

    public static List<Cue> cues(String text, long start, long end, boolean split) {
        String[] stringArray;
        if (text == null || text.isBlank() || end <= start) {
            return List.of();
        }
        String bounded = text.codePoints().limit(512L).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        if (split) {
            stringArray = bounded.strip().split("\\s+", 48);
        } else {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = bounded.strip();
        }
        String[] parts = stringArray;
        int total = Math.max(1, Arrays.stream(parts).mapToInt(s -> s.codePointCount(0, s.length())).sum());
        int before = 0;
        ArrayList<Cue> result = new ArrayList<Cue>();
        for (String part : parts) {
            result.add(new Cue(part, start + (long)((double)(end - start) * 0.85 * (double)before / (double)total)));
            before += part.codePointCount(0, part.length());
        }
        return List.copyOf(result);
    }

    public record Cue(String text, long reveal) {
    }
}

