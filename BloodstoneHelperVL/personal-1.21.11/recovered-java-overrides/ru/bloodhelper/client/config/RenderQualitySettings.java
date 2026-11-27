/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class RenderQualitySettings {
    public String glassQuality = "LOW";
    public boolean shadows = false;
    public double animationSpeed = 1.0;
    public boolean diagnostics = false;

    public void sanitize() {
        if (!("LOW".equals(this.glassQuality) || "BALANCED".equals(this.glassQuality) || "HIGH".equals(this.glassQuality))) {
            this.glassQuality = "LOW";
        }
        this.animationSpeed = Double.isFinite(this.animationSpeed) ? Math.clamp(this.animationSpeed, 0.5, 2.0) : 1.0;
    }

    public int blurMipLevel() {
        return "HIGH".equals(this.glassQuality) ? 1 : ("LOW".equals(this.glassQuality) ? 3 : 2);
    }

    public int blurTaps() {
        return "LOW".equals(this.glassQuality) ? 5 : 9;
    }

    public boolean dispersion() {
        return !"LOW".equals(this.glassQuality);
    }

    public double animationMillis(double d) {
        double d2 = Double.isFinite(this.animationSpeed) ? Math.clamp(this.animationSpeed, 0.5, 2.0) : 1.0;
        return d / d2;
    }

    public int blurWidth(int n) {
        return Math.max(1, n >> this.blurMipLevel());
    }

    public int blurHeight(int n) {
        return Math.max(1, n >> this.blurMipLevel());
    }

    public long blurPixels(int n, int n2, double d) {
        return Double.isFinite(d) && d > 0.0 ? 2L * (long)this.blurWidth(n) * (long)this.blurHeight(n2) : 0L;
    }
}

