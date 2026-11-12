/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.lyrics;

import java.util.List;

public record LyricLine(long timestampMs, String text, List<Word> words) {
    public LyricLine(long timestampMs, String text) {
        this(timestampMs, text, List.of());
    }

    public LyricLine {
        words = words == null ? List.of() : List.copyOf(words);
    }

    public boolean wordTimed() {
        return !this.words.isEmpty();
    }

    public long vocalEndMs() {
        return this.words.isEmpty() ? Long.MAX_VALUE : this.words.getLast().endMs();
    }

    public record Word(int start, int end, long startMs, long endMs) {
    }
}

