/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.HoverEvent
 *  net.minecraft.network.chat.HoverEvent$ShowText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import ru.bloodhelper.client.modules.CoordinateParser;

public final class CoordinateLinkDisplay {
    private CoordinateLinkDisplay() {
    }

    public static Component apply(Component original, List<CoordinateParser.Coordinate> matches, Function<CoordinateParser.Coordinate, ClickEvent> eventFactory, Component hover, int rgb) {
        if (matches.isEmpty()) {
            return original;
        }
        String expected = original.getString(4097);
        if (expected.length() > 4096) {
            return original;
        }
        ArrayList spans = new ArrayList();
        StringBuilder plain = new StringBuilder();
        original.visit((style, text) -> {
            spans.add(new Span(plain.length(), text, style));
            plain.append(text);
            return Optional.empty();
        }, Style.EMPTY);
        if (!plain.toString().equals(expected)) {
            return original;
        }
        List<CoordinateParser.Coordinate> safe = matches.stream().filter(match -> spans.stream().noneMatch(span -> span.start < match.end() && span.end() > match.start() && (span.style.getClickEvent() != null || span.style.getInsertion() != null))).toList();
        if (safe.isEmpty()) {
            return original;
        }
        List<ClickEvent> events = safe.stream().map(eventFactory).toList();
        MutableComponent result = Component.empty();
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
                Style style2 = span.style;
                if (inside && (style2 = style2.withClickEvent(events.get(matchIndex)).withUnderlined(Boolean.valueOf(true)).withColor(rgb & 0xFFFFFF)).getHoverEvent() == null) {
                    style2 = style2.withHoverEvent((HoverEvent)new HoverEvent.ShowText(hover));
                }
                result.append((Component)Component.literal((String)span.text.substring(local, end)).setStyle(style2));
                local = end;
            }
        }
        return result;
    }

    private record Span(int start, String text, Style style) {
        int end() {
            return this.start + this.text.length();
        }
    }
}

