/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.utilites.color;

public final class ColorUtil {
    private ColorUtil() {
    }

    public static int rgba(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    public static int argb(int a, int r, int g, int b) {
        return ColorUtil.rgba(r, g, b, a);
    }

    public static int rgb(int r, int g, int b) {
        return ColorUtil.rgba(r, g, b, 255);
    }

    public static int alpha(int color) {
        return color >> 24 & 0xFF;
    }

    public static int red(int color) {
        return color >> 16 & 0xFF;
    }

    public static int green(int color) {
        return color >> 8 & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int withAlpha(int color, int alpha) {
        return (alpha & 0xFF) << 24 | color & 0xFFFFFF;
    }

    public static int multiplyAlpha(int color, float multiplier) {
        return ColorUtil.withAlpha(color, Math.round((float)ColorUtil.alpha(color) * ColorUtil.clamp01(multiplier)));
    }

    public static int interpolate(int from, int to, float progress) {
        float clamped = ColorUtil.clamp01(progress);
        return ColorUtil.rgba(Math.round((float)ColorUtil.red(from) + (float)(ColorUtil.red(to) - ColorUtil.red(from)) * clamped), Math.round((float)ColorUtil.green(from) + (float)(ColorUtil.green(to) - ColorUtil.green(from)) * clamped), Math.round((float)ColorUtil.blue(from) + (float)(ColorUtil.blue(to) - ColorUtil.blue(from)) * clamped), Math.round((float)ColorUtil.alpha(from) + (float)(ColorUtil.alpha(to) - ColorUtil.alpha(from)) * clamped));
    }

    public static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}

