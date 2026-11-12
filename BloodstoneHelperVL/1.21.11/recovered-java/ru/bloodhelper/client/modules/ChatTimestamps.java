/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_2568
 *  net.minecraft.class_2568$class_10613
 *  net.minecraft.class_5250
 *  net.minecraft.class_7417
 *  net.minecraft.class_8828
 */
package ru.bloodhelper.client.modules;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_5250;
import net.minecraft.class_7417;
import net.minecraft.class_8828;

public final class ChatTimestamps {
    private static final String MARKER = "bloodhelper:timestamp:";
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private ChatTimestamps() {
    }

    public static Saved read(class_2561 message) {
        class_2561 current = message;
        for (int depth = 0; depth < 8; ++depth) {
            class_8828 text;
            class_7417 class_74172 = current.method_10851();
            if (!(class_74172 instanceof class_8828) || !(text = (class_8828)class_74172).comp_737().isEmpty()) {
                return null;
            }
            List parts = current.method_10855();
            if (parts.size() < 2) {
                return null;
            }
            String insertion = ((class_2561)parts.getFirst()).method_10866().method_10955();
            boolean own = insertion != null && insertion.startsWith(MARKER);
            Instant instant = ChatTimestamps.parse(own ? insertion.substring(MARKER.length()) : insertion);
            if (instant != null && (own && parts.size() == 2 || !own && parts.size() == 3)) {
                return new Saved(current, (class_2561)parts.get(1), instant, !own);
            }
            if (parts.size() != 2 || !((class_2561)parts.getFirst()).getString().matches("\\[\\d{2}:\\d{2}:\\d{2}] ")) {
                return null;
            }
            current = (class_2561)parts.get(1);
        }
        return null;
    }

    public static class_2561 showSavedClock(class_2561 display, Saved saved, boolean show, int color) {
        if (!(show && saved != null && saved.chatPatches() && ((class_2561)display.method_10855().getFirst()).getString().isBlank())) {
            return display;
        }
        class_5250 result = display.method_27661();
        class_5250 clock = ChatTimestamps.prefix(saved.instant(), color);
        clock.method_27694(style -> style.method_10975(((class_2561)saved.display().method_10855().getFirst()).method_10866().method_10955()));
        result.method_10855().set(0, clock);
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

    public static class_5250 prefix(Instant instant, int color) {
        return class_2561.method_43470((String)("[" + ChatTimestamps.time(instant) + "] ")).method_27694(style -> style.method_36139(color).method_10975(MARKER + instant.toEpochMilli()).method_10949((class_2568)new class_2568.class_10613((class_2561)class_2561.method_43470((String)DATE.format(instant)))));
    }

    public record Saved(class_2561 display, class_2561 body, Instant instant, boolean chatPatches) {
        public class_2561 mapBody(UnaryOperator<class_2561> transform) {
            class_2561 changed = (class_2561)transform.apply(this.body);
            if (changed == this.body) {
                return this.display;
            }
            class_5250 copy = this.display.method_27661();
            copy.method_10855().set(1, changed);
            return copy;
        }
    }
}

