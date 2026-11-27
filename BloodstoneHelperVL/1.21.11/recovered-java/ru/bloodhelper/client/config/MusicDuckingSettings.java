/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class MusicDuckingSettings {
    public boolean enabled = false;
    public double reductionPercent = 60.0;

    public void sanitize() {
        this.reductionPercent = Double.isFinite(this.reductionPercent) ? Math.clamp(this.reductionPercent, 0.0, 100.0) : 60.0;
    }
}

