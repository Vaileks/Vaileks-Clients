/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

public final class LegendarySoundGain {
    private LegendarySoundGain() {
    }

    public static double bounded(double value) {
        return Double.isFinite(value) ? Math.clamp(value, 0.0, 1.0) : 1.0;
    }

    public static float multiplier(boolean enabled, double overall, double ability) {
        return enabled ? (float)(LegendarySoundGain.bounded(overall) * LegendarySoundGain.bounded(ability)) : 1.0f;
    }

    public static float apply(float vanillaGain, float multiplier) {
        if (multiplier >= 1.0f) {
            return vanillaGain;
        }
        return multiplier <= 0.0f ? 0.0f : vanillaGain * multiplier;
    }
}

