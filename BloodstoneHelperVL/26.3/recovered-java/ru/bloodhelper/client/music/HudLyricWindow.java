/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import java.util.ArrayList;
import java.util.List;
import ru.bloodhelper.client.lyrics.LrcParser;
import ru.bloodhelper.client.lyrics.LyricLine;

public final class HudLyricWindow {
    private HudLyricWindow() {
    }

    public static List<String> at(List<LyricLine> lines, long time, int count) {
        int active = LrcParser.indexAt(lines, time);
        if (active < 0 || lines.get(active).text().isBlank()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<String>();
        for (int i = active; i < lines.size() && result.size() < Math.clamp((long)count, 1, 3) && !lines.get(i).text().isBlank(); ++i) {
            result.add(lines.get(i).text());
        }
        return List.copyOf(result);
    }
}

