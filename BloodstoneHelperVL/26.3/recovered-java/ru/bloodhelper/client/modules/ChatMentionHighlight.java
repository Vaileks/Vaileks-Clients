/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import ru.bloodhelper.client.modules.ChatMentionMatcher;

public final class ChatMentionHighlight {
    private ChatMentionHighlight() {
    }

    public static Component apply(Component original, List<ChatMentionMatcher.Range> ranges, int rgb) {
        if (ranges.isEmpty()) {
            return original;
        }
        String expected = original.getString(8193);
        if (expected.length() > 8192) {
            return original;
        }
        int previousEnd = 0;
        for (ChatMentionMatcher.Range range : ranges) {
            if (range.start() < previousEnd || range.start() < 0 || range.end() <= range.start() || range.end() > expected.length()) {
                return original;
            }
            previousEnd = range.end();
        }
        ArrayList spans = new ArrayList();
        StringBuilder plain = new StringBuilder(expected.length());
        original.visit((style, text) -> {
            spans.add(new Span(style, text));
            plain.append(text);
            return Optional.empty();
        }, Style.EMPTY);
        if (!plain.toString().equals(expected)) {
            return original;
        }
        MutableComponent result = Component.empty();
        int offset = 0;
        int rangeIndex = 0;
        for (Span span : spans) {
            int local = 0;
            while (local < span.text.length()) {
                boolean highlighted;
                int absolute = offset + local;
                while (rangeIndex < ranges.size() && ranges.get(rangeIndex).end() <= absolute) {
                    ++rangeIndex;
                }
                ChatMentionMatcher.Range range = rangeIndex < ranges.size() ? ranges.get(rangeIndex) : null;
                boolean bl = highlighted = range != null && range.start() <= absolute && absolute < range.end();
                int boundary = range == null ? offset + span.text.length() : (highlighted ? range.end() : range.start());
                int end = Math.min(span.text.length(), boundary - offset);
                if (end <= local) {
                    return original;
                }
                Style style2 = highlighted ? span.style.withColor(rgb & 0xFFFFFF).withBold(Boolean.valueOf(true)) : span.style;
                result.append((Component)Component.literal((String)span.text.substring(local, end)).setStyle(style2));
                local = end;
            }
            offset += span.text.length();
        }
        return result;
    }

    private record Span(Style style, String text) {
    }
}

