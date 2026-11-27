/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package ru.bloodhelper.client.lyrics;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.lyrics.LyricLine;

public final class LyricHighlight {
    private static long drawnWords;

    private LyricHighlight() {
    }

    public static void draw(GuiGraphicsExtractor g, CustomFont font, LyricLine line, String fragment, int offset, float x, float y, float size, int color, long time) {
        int limit = offset + fragment.length();
        for (LyricLine.Word word : line.words()) {
            float before;
            if (word.startMs() > time) break;
            int begin = Math.max(offset, word.start());
            int end = Math.min(limit, word.end());
            if (end <= begin) continue;
            double progress = word.endMs() == Long.MAX_VALUE ? 1.0 : Math.clamp((double)(time - word.startMs()) / (double)Math.max(1L, word.endMs() - word.startMs()), 0.0, 1.0);
            float left = x + font.width(fragment.substring(0, begin - offset), size);
            float right = x + font.width(fragment.substring(0, end - offset), size);
            float fullWidth = font.width(line.text().substring(word.start(), word.end()), size);
            float reveal = (float)((double)fullWidth * progress - (double)(before = font.width(line.text().substring(word.start(), begin), size)));
            if ((right = Math.min(right, left + reveal)) <= left) continue;
            g.enableScissor((int)Math.floor(left), (int)Math.floor(y - 3.0f), (int)Math.ceil(right), (int)Math.ceil(y + size + 5.0f));
            font.drawRaw(g, fragment, x, y, size, color);
            g.disableScissor();
            ++drawnWords;
        }
    }

    public static long drawnWords() {
        return drawnWords;
    }
}

