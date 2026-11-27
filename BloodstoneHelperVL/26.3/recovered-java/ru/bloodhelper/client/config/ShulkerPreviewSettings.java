/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class ShulkerPreviewSettings {
    public boolean enabled = false;
    public double scale = 1.0;

    public void sanitize() {
        this.scale = ShulkerPreviewSettings.boundedScale(this.scale);
    }

    public static double boundedScale(double value) {
        return Double.isFinite(value) ? Math.clamp(value, 0.5, 2.0) : 1.0;
    }
}

