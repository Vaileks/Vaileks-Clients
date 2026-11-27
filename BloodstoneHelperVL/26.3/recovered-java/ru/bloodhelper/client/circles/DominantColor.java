/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.circles;

import java.util.function.IntBinaryOperator;

public final class DominantColor {
    public static final int UNKNOWN = -1;
    private static final int HUE_BUCKETS = 24;

    private DominantColor() {
    }

    public static int extract(int width, int height, IntBinaryOperator pixels) {
        Bucket[] colors = new Bucket[40];
        long totalAlpha = 0L;
        long coloredAlpha = 0L;
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int key;
                int argb = pixels.applyAsInt(x, y);
                int alpha = argb >>> 24;
                if (alpha < 16) continue;
                int red = argb >> 16 & 0xFF;
                int green = argb >> 8 & 0xFF;
                int blue = argb & 0xFF;
                int max = Math.max(red, Math.max(green, blue));
                int min = Math.min(red, Math.min(green, blue));
                boolean colored = max >= 24 && (double)(max - min) >= (double)max * 0.2;
                int n = key = colored ? DominantColor.hueBucket(red, green, blue, max, min) : 24 + (max >> 4);
                if (colors[key] == null) {
                    colors[key] = new Bucket();
                }
                colors[key].add(red, green, blue, alpha, max);
                totalAlpha += (long)alpha;
                if (!colored) continue;
                coloredAlpha += (long)alpha;
            }
        }
        Bucket best = null;
        boolean preferColored = coloredAlpha > 0L && coloredAlpha * 10L >= totalAlpha;
        int start = preferColored ? 0 : 24;
        int end = preferColored ? 24 : colors.length;
        for (int key = start; key < end; ++key) {
            Bucket bucket = new Bucket();
            bucket.merge(colors[key]);
            if (preferColored) {
                bucket.merge(colors[(key + 24 - 1) % 24]);
                bucket.merge(colors[(key + 1) % 24]);
            }
            if (bucket.weight <= 0L || best != null && bucket.weight <= best.weight) continue;
            best = bucket;
        }
        return best == null ? -1 : best.rgb();
    }

    private static int hueBucket(int red, int green, int blue, int max, int min) {
        double delta = max - min;
        double hue = max == red ? (double)(green - blue) / delta : (max == green ? 2.0 + (double)(blue - red) / delta : 4.0 + (double)(red - green) / delta);
        return (int)Math.floor((hue + 6.0) * 24.0 / 6.0) % 24;
    }

    public static int tint(int rgb, int tint) {
        int red = (rgb >> 16 & 0xFF) * (tint >> 16 & 0xFF) / 255;
        int green = (rgb >> 8 & 0xFF) * (tint >> 8 & 0xFF) / 255;
        int blue = (rgb & 0xFF) * (tint & 0xFF) / 255;
        return red << 16 | green << 8 | blue;
    }

    private static final class Bucket {
        private long weight;
        private long red;
        private long green;
        private long blue;

        private Bucket() {
        }

        void add(int r, int g, int b, int alpha, int brightness) {
            long contribution = (long)alpha * (long)(32 + brightness);
            this.weight += contribution;
            this.red += (long)r * contribution;
            this.green += (long)g * contribution;
            this.blue += (long)b * contribution;
        }

        void merge(Bucket other) {
            if (other != null) {
                this.weight += other.weight;
                this.red += other.red;
                this.green += other.green;
                this.blue += other.blue;
            }
        }

        int rgb() {
            return (int)(this.red / this.weight) << 16 | (int)(this.green / this.weight) << 8 | (int)(this.blue / this.weight);
        }
    }
}

