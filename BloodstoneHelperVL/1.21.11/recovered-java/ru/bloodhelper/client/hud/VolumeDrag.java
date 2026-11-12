/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

public final class VolumeDrag {
    private static final long INTERVAL_NS = 50000000L;
    private String target = "";
    private double value;
    private double sentValue = Double.NaN;
    private long sentAt;

    public void begin(String token, double amount) {
        this.target = token == null ? "" : token;
        this.value = VolumeDrag.bounded(amount);
        this.sentValue = Double.NaN;
        this.sentAt = 0L;
    }

    public boolean active() {
        return !this.target.isEmpty();
    }

    public String target() {
        return this.target;
    }

    public double value() {
        return this.value;
    }

    public boolean retain(String currentTarget) {
        if (this.active() && this.target.equals(currentTarget)) {
            return true;
        }
        this.cancel();
        return false;
    }

    public void move(double amount) {
        this.value = VolumeDrag.bounded(amount);
    }

    public double request(long now, boolean release) {
        if (!this.active() || this.value == this.sentValue) {
            return Double.NaN;
        }
        if (!release && !Double.isNaN(this.sentValue) && now - this.sentAt < 50000000L) {
            return Double.NaN;
        }
        this.sentValue = this.value;
        this.sentAt = now;
        return this.value;
    }

    public void cancel() {
        this.target = "";
        this.sentValue = Double.NaN;
    }

    public static double at(double x, double left, double width) {
        return VolumeDrag.bounded((x - left) / Math.max(0.001, width));
    }

    private static double bounded(double value) {
        return Double.isFinite(value) ? Math.clamp(value, 0.0, 1.0) : 0.0;
    }
}

