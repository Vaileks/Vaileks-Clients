/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class ZoomSettings {
    public boolean enabled = false;
    public double magnification = 2.5;
    public double sensitivity = 0.35;
    public boolean toggle = false;
    public boolean relativeSensitivity = true;
    public boolean affectHandFov = true;
    public boolean relativeViewBobbing = true;
    public boolean scrollEnabled = true;
    public double scrollStep = 1.5;
    public boolean rememberZoom = false;
    public double zoomInSeconds = 1.0;
    public double zoomOutSeconds = 0.5;
    private int defaultsVersion;

    public void sanitize() {
        if (this.defaultsVersion == 0 && this.hasLegacyDefaults()) {
            this.magnification = 2.5;
            this.zoomInSeconds = 1.0;
            this.zoomOutSeconds = 0.5;
        }
        this.defaultsVersion = 1;
        this.magnification = Double.isFinite(this.magnification) ? Math.clamp(this.magnification, 2.0, 100.0) : 2.5;
        this.sensitivity = Double.isFinite(this.sensitivity) ? Math.clamp(this.sensitivity, 0.05, 1.0) : 0.35;
        this.scrollStep = Double.isFinite(this.scrollStep) ? Math.clamp(this.scrollStep, 1.05, 2.0) : 1.5;
        this.zoomInSeconds = Double.isFinite(this.zoomInSeconds) ? Math.clamp(this.zoomInSeconds, 0.0, 1.0) : 1.0;
        this.zoomOutSeconds = Double.isFinite(this.zoomOutSeconds) ? Math.clamp(this.zoomOutSeconds, 0.0, 1.0) : 0.5;
    }

    private boolean hasLegacyDefaults() {
        return this.magnification == 4.0 && this.sensitivity == 0.35 && !this.toggle && this.relativeSensitivity && this.affectHandFov && this.relativeViewBobbing && this.scrollEnabled && this.scrollStep == 1.5 && !this.rememberZoom && this.zoomInSeconds == 0.2 && this.zoomOutSeconds == 0.15;
    }
}

