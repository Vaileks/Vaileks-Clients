/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class NameVisibilitySettings {
    public boolean enabled = false;
    public int textColor = -1;
    public int backgroundColor = -16777216;
    public int textOpacity = 100;
    public int backgroundOpacity = 24;
    public boolean fullbright = true;
    public double distantScale = 0.2;
    public boolean unlimitedDistance = false;
    public boolean disableTeamColors = false;

    public void sanitize() {
        this.textColor |= 0xFF000000;
        this.backgroundColor |= 0xFF000000;
        this.textOpacity = Math.clamp((long)this.textOpacity, 10, 100);
        this.backgroundOpacity = Math.clamp((long)this.backgroundOpacity, 0, 100);
        this.distantScale = Double.isFinite(this.distantScale) ? Math.clamp(this.distantScale, 0.0, 1.0) : 0.2;
    }

    public int foreground() {
        return NameVisibilitySettings.withOpacity(this.textColor, this.textOpacity);
    }

    public int background() {
        return NameVisibilitySettings.withOpacity(this.backgroundColor, this.backgroundOpacity);
    }

    private static int withOpacity(int rgb, int percent) {
        return rgb & 0xFFFFFF | Math.round((float)Math.clamp((long)percent, 0, 100) * 2.55f) << 24;
    }

    public static float scale(double distance, double exponent) {
        if (!Double.isFinite(distance) || !Double.isFinite(exponent) || exponent <= 0.0) {
            return 1.0f;
        }
        return (float)Math.pow(Math.clamp(distance, 1.0, 1024.0), Math.clamp(exponent, 0.0, 1.0));
    }
}

