/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class MotionBlurSettings {
    public boolean enabled = false;
    public double strength = 0.45;
    public double durationMs = 90.0;
    public boolean duringZoom = true;

    public void normalize() {
        this.strength = Math.clamp(Double.isFinite(this.strength) ? this.strength : 0.45, 0.0, 0.9);
        this.durationMs = Math.clamp(Double.isFinite(this.durationMs) ? this.durationMs : 90.0, 20.0, 250.0);
    }
}

