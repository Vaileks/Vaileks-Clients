/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.visuals;

public final class MotionBlurHistory {
    private Object world;
    private Object camera;
    private Object perspective;
    private Object settings;
    private int width;
    private int height;
    private long previousNanos;
    private double x;
    private double y;
    private double z;
    private double strength;
    private double duration;
    private boolean valid;

    public double advance(long now, Object world, Object camera, Object perspective, Object settings, int width, int height, double x, double y, double z, double strength, double durationMs) {
        long elapsed = now - this.previousNanos;
        double dx = x - this.x;
        double dy = y - this.y;
        double dz = z - this.z;
        boolean fresh = this.valid && this.world == world && this.camera == camera && this.perspective == perspective && this.settings == settings && this.width == width && this.height == height && this.strength == strength && this.duration == durationMs && elapsed > 0L && elapsed <= 250000000L && dx * dx + dy * dy + dz * dz <= 64.0;
        this.world = world;
        this.camera = camera;
        this.perspective = perspective;
        this.settings = settings;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.z = z;
        this.strength = strength;
        this.duration = durationMs;
        this.previousNanos = now;
        this.valid = world != null && camera != null && width > 1 && height > 1 && Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
        return fresh && this.valid ? MotionBlurHistory.weight((double)elapsed / 1000000.0, strength, durationMs) : 0.0;
    }

    public static double weight(double elapsedMs, double strength, double durationMs) {
        if (!Double.isFinite(elapsedMs) || !Double.isFinite(strength) || !Double.isFinite(durationMs) || elapsedMs <= 0.0 || strength <= 0.0 || durationMs <= 0.0) {
            return 0.0;
        }
        return Math.exp(-elapsedMs / (Math.clamp(strength, 0.0, 0.9) * Math.clamp(durationMs, 20.0, 250.0)));
    }

    public void reset() {
        this.valid = false;
        this.settings = null;
        this.perspective = null;
        this.camera = null;
        this.world = null;
        this.previousNanos = 0L;
    }

    public boolean valid() {
        return this.valid;
    }
}

