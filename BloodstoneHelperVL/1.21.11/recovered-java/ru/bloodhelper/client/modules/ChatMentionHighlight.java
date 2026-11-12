/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_5250
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;
import ru.bloodhelper.client.modules.ChatMentionMatcher;

public final class ChatMentionHighlight {
    private ChatMentionHighlight() {
    }

    public static class_2561 apply(class_2561 original, List<ChatMentionMatcher.Range> ranges, int rgb) {
        if (ranges.isEmpty()) {
            return original;
        }
        String expected = original.method_10858(8193);
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
        original.method_27658((style, text) -> {
            spans.add(new Span(style, text));
            plain.append(text);
            return Optional.empty();
        }, class_2583.field_24360);
        if (!plain.toString().equals(expected)) {
            return original;
        }
        class_5250 result = class_2561.method_43473();
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
                class_2583 style2 = highlighted ? span.style.method_36139(rgb & 0xFFFFFF).method_10982(Boolean.valueOf(true)) : span.style;
                result.method_10852((class_2561)class_2561.method_43470((String)span.text.substring(local, end)).method_10862(style2));
                local = end;
            }
            offset += span.text.length();
        }
        return result;
    }

    private record Span(class_2583 style, String text) {
    }
}

