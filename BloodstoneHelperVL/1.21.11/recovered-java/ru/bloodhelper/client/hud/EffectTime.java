/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

public final class EffectTime {
    public static final int PERMANENT_DISPLAY_TICKS = 1728000;

    private EffectTime() {
    }

    public static boolean indefinite(int ticks, boolean infinite) {
        return infinite || ticks >= 1728000;
    }

    public static String format(int ticks, boolean infinite) {
        if (EffectTime.indefinite(ticks, infinite)) {
            return "\u221e";
        }
        long seconds = Math.max(0L, ((long)ticks + 19L) / 20L);
        return seconds / 60L + ":" + (seconds % 60L < 10L ? "0" : "") + seconds % 60L;
    }
}

