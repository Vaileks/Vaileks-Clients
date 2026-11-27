/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentContents
 *  net.minecraft.network.chat.HoverEvent
 *  net.minecraft.network.chat.HoverEvent$ShowText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.contents.PlainTextContents
 */
package ru.bloodhelper.client.modules;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;

public final class ChatTimestamps {
    private static final String MARKER = "bloodhelper:timestamp:";
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private ChatTimestamps() {
    }

    public static Saved read(Component message) {
        Component current = message;
        for (int depth = 0; depth < 8; ++depth) {
            PlainTextContents text;
            ComponentContents componentContents = current.getContents();
            if (!(componentContents instanceof PlainTextContents) || !(text = (PlainTextContents)componentContents).text().isEmpty()) {
                return null;
            }
            List parts = current.getSiblings();
            if (parts.size() < 2) {
                return null;
            }
            String insertion = ((Component)parts.getFirst()).getStyle().getInsertion();
            boolean own = insertion != null && insertion.startsWith(MARKER);
            Instant instant = ChatTimestamps.parse(own ? insertion.substring(MARKER.length()) : insertion);
            if (instant != null && (own && parts.size() == 2 || !own && parts.size() == 3)) {
                return new Saved(current, (Component)parts.get(1), instant, !own);
            }
            if (parts.size() != 2 || !((Component)parts.getFirst()).getString().matches("\\[\\d{2}:\\d{2}:\\d{2}] ")) {
                return null;
            }
            current = (Component)parts.get(1);
        }
        return null;
    }

    public static Component showSavedClock(Component display, Saved saved, boolean show, int color) {
        if (!(show && saved != null && saved.chatPatches() && ((Component)display.getSiblings().getFirst()).getString().isBlank())) {
            return display;
        }
        MutableComponent result = display.copy();
        MutableComponent clock = ChatTimestamps.prefix(saved.instant(), color);
        clock.withStyle(style -> style.withInsertion(((Component)saved.display().getSiblings().getFirst()).getStyle().getInsertion()));
        result.getSiblings().set(0, clock);
        return result;
    }

    private static Instant parse(String value) {
        if (value == null || !value.matches("[0-9]{1,14}")) {
            return null;
        }
        try {
            return Instant.ofEpochMilli(Long.parseLong(value));
        }
        catch (RuntimeException ignored) {
            return null;
        }
    }

    public static Instant receivedAt(Saved saved, boolean externalOwner, boolean restoring, Supplier<Instant> clock) {
        if (saved != null) {
            return saved.instant();
        }
        return externalOwner || restoring ? null : clock.get();
    }

    public static String time(Instant instant) {
        return instant == null ? "\u2014" : TIME.format(instant);
    }

    public static MutableComponent prefix(Instant instant, int color) {
        return Component.literal((String)("[" + ChatTimestamps.time(instant) + "] ")).withStyle(style -> style.withColor(color).withInsertion(MARKER + instant.toEpochMilli()).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)DATE.format(instant)))));
    }

    public record Saved(Component display, Component body, Instant instant, boolean chatPatches) {
        public Component mapBody(UnaryOperator<Component> transform) {
            Component changed = (Component)transform.apply(this.body);
            if (changed == this.body) {
                return this.display;
            }
            MutableComponent copy = this.display.copy();
            copy.getSiblings().set(1, changed);
            return copy;
        }
    }
}

