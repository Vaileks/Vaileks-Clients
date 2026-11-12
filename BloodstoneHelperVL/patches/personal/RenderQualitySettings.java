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

    public double animationMillis(double normalMillis) {
        double speed = Double.isFinite(this.animationSpeed) ? Math.clamp(this.animationSpeed, 0.5, 2.0) : 1.0;
        return normalMillis / speed;
    }

    public int blurWidth(int width) {
        return Math.max(1, width >> this.blurMipLevel());
    }

    public int blurHeight(int height) {
        return Math.max(1, height >> this.blurMipLevel());
    }

    public long blurPixels(int width, int height, double blur) {
        return Double.isFinite(blur) && blur > 0.0 ? 2L * (long)this.blurWidth(width) * (long)this.blurHeight(height) : 0L;
    }
}
