/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2558
 *  net.minecraft.class_2561
 *  net.minecraft.class_2568
 *  net.minecraft.class_2568$class_10613
 *  net.minecraft.class_2583
 *  net.minecraft.class_5250
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_2583;
import net.minecraft.class_5250;
import ru.bloodhelper.client.modules.CoordinateParser;

public final class CoordinateLinkDisplay {
    private CoordinateLinkDisplay() {
    }

    public static class_2561 apply(class_2561 original, List<CoordinateParser.Coordinate> matches, Function<CoordinateParser.Coordinate, class_2558> eventFactory, class_2561 hover, int rgb) {
        if (matches.isEmpty()) {
            return original;
        }
        String expected = original.method_10858(4097);
        if (expected.length() > 4096) {
            return original;
        }
        ArrayList spans = new ArrayList();
        StringBuilder plain = new StringBuilder();
        original.method_27658((style, text) -> {
            spans.add(new Span(plain.length(), text, style));
            plain.append(text);
            return Optional.empty();
        }, class_2583.field_24360);
        if (!plain.toString().equals(expected)) {
            return original;
        }
        List<CoordinateParser.Coordinate> safe = matches.stream().filter(match -> spans.stream().noneMatch(span -> span.start < match.end() && span.end() > match.start() && (span.style.method_10970() != null || span.style.method_10955() != null))).toList();
        if (safe.isEmpty()) {
            return original;
        }
        List<class_2558> events = safe.stream().map(eventFactory).toList();
        class_5250 result = class_2561.method_43473();
        int matchIndex = 0;
        for (Span span : spans) {
            int local = 0;
            while (local < span.text.length()) {
                boolean inside;
                int absolute = span.start + local;
                while (matchIndex < safe.size() && safe.get(matchIndex).end() <= absolute) {
                    ++matchIndex;
                }
                CoordinateParser.Coordinate match2 = matchIndex < safe.size() ? safe.get(matchIndex) : null;
                boolean bl = inside = match2 != null && absolute >= match2.start() && absolute < match2.end();
                int boundary = match2 == null ? span.end() : (inside ? match2.end() : match2.start());
                int end = Math.min(span.text.length(), boundary - span.start);
                if (end <= local) {
                    return original;
                }
                class_2583 style2 = span.style;
                if (inside && (style2 = style2.method_10958(events.get(matchIndex)).method_30938(Boolean.valueOf(true)).method_36139(rgb & 0xFFFFFF)).method_10969() == null) {
                    style2 = style2.method_10949((class_2568)new class_2568.class_10613(hover));
                }
                result.method_10852((class_2561)class_2561.method_43470((String)span.text.substring(local, end)).method_10862(style2));
                local = end;
            }
        }
        return result;
    }

    private record Span(int start, String text, class_2583 style) {
        int end() {
            return this.start + this.text.length();
        }
    }
}

