/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.List;

public final class AspectRatioSettings {
    public boolean enabled = false;
    public String preset = "4:3";
    public double customWidth = 4.0;
    public double customHeight = 3.0;

    public void sanitize() {
        if (this.preset == null || !List.of("NATIVE", "4:3", "5:4", "16:9", "16:10", "21:9", "CUSTOM").contains(this.preset)) {
            this.preset = "4:3";
        }
        this.customWidth = AspectRatioSettings.finite(this.customWidth, 4.0);
        this.customHeight = AspectRatioSettings.finite(this.customHeight, 3.0);
    }

    private static double finite(double value, double fallback) {
        return Double.isFinite(value) ? Math.clamp(value, 1.0, 64.0) : fallback;
    }

    public float ratio(float nativeRatio) {
        if (!this.enabled) {
            return nativeRatio;
        }
        return switch (this.preset == null ? "NATIVE" : this.preset) {
            case "4:3" -> 1.3333334f;
            case "5:4" -> 1.25f;
            case "16:9" -> 1.7777778f;
            case "16:10" -> 1.6f;
            case "21:9" -> 2.3333333f;
            case "CUSTOM" -> (float)Math.clamp(AspectRatioSettings.finite(this.customWidth, 4.0) / AspectRatioSettings.finite(this.customHeight, 3.0), 0.25, 4.0);
            default -> nativeRatio;
        };
    }
}

