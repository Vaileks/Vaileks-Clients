/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import ru.bloodhelper.client.config.ConfigStore;

public final class ThemeColors {
    private static int cachedAccent;
    private static boolean cachedLight;
    private static final Map<Integer, Integer> textCache;
    private static final Map<Integer, Integer> iconCache;
    private static final Map<Integer, Integer> surfaceCache;

    private ThemeColors() {
    }

    public static int accent() {
        return ConfigStore.get().accent | 0xFF000000;
    }

    public static boolean light() {
        return "LIGHT".equals(ConfigStore.get().themeMode);
    }

    public static int text(int original) {
        return ThemeColors.cached(original, true);
    }

    public static int icon(int original) {
        return ThemeColors.cached(original, false);
    }

    private static int cached(int original, boolean text) {
        int accent = ThemeColors.accent();
        boolean light = ThemeColors.light();
        if (cachedAccent != accent || cachedLight != light) {
            textCache.clear();
            iconCache.clear();
            surfaceCache.clear();
            cachedAccent = accent;
            cachedLight = light;
        }
        Map<Integer, Integer> cache = text ? textCache : iconCache;
        int key = original & 0xFFFFFF;
        if (cache.size() > 512) {
            cache.clear();
        }
        int rgb = cache.computeIfAbsent(key, ignored -> text ? ThemeColors.text(original, accent, light) : ThemeColors.icon(original, accent, light));
        return ThemeColors.alphaOf(original, rgb);
    }

    public static int surface(int original) {
        int accent = ThemeColors.accent();
        boolean light = ThemeColors.light();
        if (cachedAccent != accent || cachedLight != light) {
            textCache.clear();
            iconCache.clear();
            surfaceCache.clear();
            cachedAccent = accent;
            cachedLight = light;
        }
        if (surfaceCache.size() > 512) {
            surfaceCache.clear();
        }
        int rgb = surfaceCache.computeIfAbsent(original & 0xFFFFFF, ignored -> ThemeColors.surface(original, accent, light));
        return ThemeColors.alphaOf(original, rgb);
    }

    public static int line() {
        return ThemeColors.light() ? -3748394 : -13222584;
    }

    public static int selectedSurface() {
        return ThemeColors.light() ? -1972499 : -14340807;
    }

    public static int text(int original, int accent) {
        return ThemeColors.text(original, accent, false);
    }

    public static int icon(int original, int accent) {
        return ThemeColors.icon(original, accent, false);
    }

    public static int text(int original, int accent, boolean light) {
        return ThemeColors.foreground(original, accent, light, 4.5);
    }

    public static int icon(int original, int accent, boolean light) {
        return ThemeColors.foreground(original, accent, light, 3.0);
    }

    private static int foreground(int original, int accent, boolean light, double minimumContrast) {
        boolean coloured;
        double emphasis = ThemeColors.brightness(original);
        int r = original >>> 16 & 0xFF;
        int g = original >>> 8 & 0xFF;
        int b = original & 0xFF;
        boolean bl = coloured = Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) > 48;
        int neutral = light ? (emphasis > 0.72 ? 2106928 : (emphasis > 0.48 ? 4739936 : 5661039)) : (emphasis > 0.72 ? 15265010 : (emphasis > 0.48 ? 11384773 : 9608879));
        int value = coloured ? original & 0xFFFFFF : neutral;
        return ThemeColors.alphaOf(original, ThemeColors.readable(value, ThemeColors.surface(-14342356, accent, light), minimumContrast));
    }

    private static int readable(int color, int background, double minimumContrast) {
        if (ThemeColors.contrast(color, background) >= minimumContrast) {
            return color;
        }
        int target = ThemeColors.luminance(background) > 0.45 ? 0 : 0xFFFFFF;
        double low = 0.0;
        double high = 1.0;
        for (int i = 0; i < 11; ++i) {
            double mid = (low + high) * 0.5;
            if (ThemeColors.contrast(ThemeColors.mix(color, target, mid), background) < minimumContrast) {
                low = mid;
                continue;
            }
            high = mid;
        }
        return ThemeColors.mix(color, target, high);
    }

    private static double contrast(int a, int b) {
        double first = ThemeColors.luminance(a);
        double second = ThemeColors.luminance(b);
        return (Math.max(first, second) + 0.05) / (Math.min(first, second) + 0.05);
    }

    private static double luminance(int rgb) {
        return 0.2126 * ThemeColors.linear(rgb >>> 16 & 0xFF) + 0.7152 * ThemeColors.linear(rgb >>> 8 & 0xFF) + 0.0722 * ThemeColors.linear(rgb & 0xFF);
    }

    private static double linear(int channel) {
        double c = (double)channel / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    public static int surface(int original, int accent) {
        return ThemeColors.surface(original, accent, false);
    }

    public static int surface(int original, int accent, boolean light) {
        double level = ThemeColors.brightness(original);
        int neutral = ThemeColors.gray((int)Math.round(light ? 250.0 - 56.0 * level : 15.0 + 40.0 * level));
        return ThemeColors.alphaOf(original, ThemeColors.mix(neutral, accent, light ? 0.008 : 0.025));
    }

    public static int timestamp() {
        return ThemeColors.timestamp(ThemeColors.accent());
    }

    public static int timestamp(int accent) {
        return ThemeColors.readable(accent & 0xFFFFFF, 0x151515, 4.5) & 0xFFFFFF;
    }

    public static int defaultAccent() {
        return -3401955;
    }

    private static int alphaOf(int original, int rgb) {
        return original & 0xFF000000 | rgb & 0xFFFFFF;
    }

    private static int gray(int value) {
        return value << 16 | value << 8 | value;
    }

    private static double brightness(int rgb) {
        return (0.2126 * (double)(rgb >>> 16 & 0xFF) + 0.7152 * (double)(rgb >>> 8 & 0xFF) + 0.0722 * (double)(rgb & 0xFF)) / 255.0;
    }

    private static int mix(int first, int second, double amount) {
        double t = Math.clamp(amount, 0.0, 1.0);
        int r = (int)Math.round((double)(first >>> 16 & 0xFF) * (1.0 - t) + (double)(second >>> 16 & 0xFF) * t);
        int g = (int)Math.round((double)(first >>> 8 & 0xFF) * (1.0 - t) + (double)(second >>> 8 & 0xFF) * t);
        int b = (int)Math.round((double)(first & 0xFF) * (1.0 - t) + (double)(second & 0xFF) * t);
        return r << 16 | g << 8 | b;
    }

    public static int hsv(double hue, double saturation, double value) {
        if (!Double.isFinite(hue)) {
            hue = 0.0;
        }
        hue -= Math.floor(hue);
        return Color.HSBtoRGB((float)hue, ThemeColors.finiteUnit(saturation), ThemeColors.finiteUnit(value));
    }

    public static float[] hsv(int rgb) {
        return Color.RGBtoHSB(rgb >>> 16 & 0xFF, rgb >>> 8 & 0xFF, rgb & 0xFF, null);
    }

    private static float finiteUnit(double value) {
        return (float)(Double.isFinite(value) ? Math.clamp(value, 0.0, 1.0) : 0.0);
    }

    public static String hex(int rgb) {
        return String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
    }

    public static OptionalInt parseHex(String value) {
        String digits;
        if (value == null) {
            return OptionalInt.empty();
        }
        String string = digits = value.startsWith("#") ? value.substring(1) : value;
        if (!digits.matches("[0-9a-fA-F]{6}")) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(0xFF000000 | Integer.parseInt(digits, 16));
    }

    static {
        textCache = new HashMap<Integer, Integer>();
        iconCache = new HashMap<Integer, Integer>();
        surfaceCache = new HashMap<Integer, Integer>();
    }
}

