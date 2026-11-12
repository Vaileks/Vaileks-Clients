/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class EffectNoticeTracker {
    private final Map<String, Observed> effects = new HashMap<String, Observed>();

    EffectNoticeTracker() {
    }

    Change observe(String id, int ticks, int amplifier, boolean infinite, boolean baseline) {
        Observed previous = this.effects.put(id, new Observed(ticks, amplifier, infinite));
        if (baseline) {
            return Change.NONE;
        }
        if (previous == null) {
            return Change.GRANTED;
        }
        if (amplifier != previous.amplifier || infinite != previous.infinite) {
            return Change.REFRESHED;
        }
        return !infinite && (long)ticks - (long)previous.ticks >= 40L ? Change.REFRESHED : Change.NONE;
    }

    void retain(Set<String> present) {
        this.effects.keySet().retainAll(present);
    }

    void clear() {
        this.effects.clear();
    }

    static String body(Change change, int ticks, boolean infinite, String language) {
        String time;
        boolean ru = "ru".equals(language);
        String string = infinite ? (ru ? "\u0431\u0435\u0437 \u043e\u0433\u0440\u0430\u043d\u0438\u0447\u0435\u043d\u0438\u044f" : "indefinitely") : (time = (ru ? "\u043d\u0430 " : "for ") + Math.max(1L, ((long)ticks + 19L) / 20L) + (ru ? "\u0441" : "s"));
        return (change == Change.REFRESHED ? (ru ? "\u043e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u043e " : "refreshed ") : (ru ? "\u0432\u044b\u0434\u0430\u043d\u043e " : "granted ")) + time;
    }

    private record Observed(int ticks, int amplifier, boolean infinite) {
    }

    static enum Change {
        NONE,
        GRANTED,
        REFRESHED;

    }
}

