/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.EnumMap;
import ru.bloodhelper.client.config.BackgroundAlertsSettings;

final class BackgroundAlertPolicy {
    private final EnumMap<Event, Long> last = new EnumMap(Event.class);
    private boolean sampledCombat;
    private boolean previousCombat;

    BackgroundAlertPolicy() {
    }

    boolean combatStarted(boolean active) {
        boolean result = this.sampledCombat && !this.previousCombat && active;
        this.sampledCombat = true;
        this.previousCombat = active;
        return result;
    }

    boolean acquire(Event event, BackgroundAlertsSettings settings, boolean focused, long now) {
        if (!settings.enabled || focused || !BackgroundAlertPolicy.selected(event, settings)) {
            return false;
        }
        Long previous = this.last.get((Object)event);
        long cooldown = (long)Math.clamp((long)settings.cooldownSeconds, 3, 60) * 1000L;
        if (previous != null && now - previous < cooldown) {
            return false;
        }
        this.last.put(event, now);
        return true;
    }

    private static boolean selected(Event event, BackgroundAlertsSettings settings) {
        return switch (event.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> settings.mentions;
            case 1 -> settings.combat;
            case 2 -> settings.disconnect;
        };
    }

    void reset() {
        this.last.clear();
        this.previousCombat = false;
        this.sampledCombat = false;
    }

    static enum Event {
        MENTION,
        COMBAT,
        DISCONNECT;

    }
}

