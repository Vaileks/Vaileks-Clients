/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

import ru.bloodhelper.client.gui.RenderQuality;

public final class RenderMetrics {
    private static final Samples GUI = new Samples(120);
    private static final Samples HUD = new Samples(120);
    private static long captures;
    private static long passes;
    private static long pixels;
    private static long publishedAt;
    private static boolean collecting;
    private static Snapshot published;

    private RenderMetrics() {
    }

    public static long begin() {
        boolean enabled = RenderQuality.diagnostics();
        if (collecting != enabled) {
            RenderMetrics.clear();
            collecting = enabled;
        }
        return enabled ? System.nanoTime() : 0L;
    }

    public static void endMenu(long start) {
        if (start != 0L) {
            GUI.add(System.nanoTime() - start);
        }
    }

    public static void endHud(long start) {
        if (start != 0L) {
            HUD.add(System.nanoTime() - start);
        }
    }

    static void capture() {
        if (collecting) {
            ++captures;
        }
    }

    static void blur(long processedPixels) {
        if (collecting) {
            passes += 2L;
            pixels += processedPixels;
        }
    }

    public static Snapshot snapshot() {
        long now = System.nanoTime();
        if (now - publishedAt >= 250000000L) {
            published = new Snapshot(GUI.meanMillis(), HUD.meanMillis(), GUI.size(), HUD.size(), captures, passes, pixels);
            publishedAt = now;
        }
        return published;
    }

    public static void clear() {
        GUI.clear();
        HUD.clear();
        publishedAt = 0L;
        pixels = 0L;
        passes = 0L;
        captures = 0L;
        published = new Snapshot(Double.NaN, Double.NaN, 0, 0, 0L, 0L, 0L);
    }

    static {
        published = new Snapshot(Double.NaN, Double.NaN, 0, 0, 0L, 0L, 0L);
    }

    static final class Samples {
        private final long[] values;
        private int next;
        private int count;
        private long sum;

        Samples(int capacity) {
            if (capacity < 1) {
                throw new IllegalArgumentException("capacity");
            }
            this.values = new long[capacity];
        }

        void add(long nanos) {
            if (nanos < 0L || nanos > 10000000000L) {
                return;
            }
            if (this.count == this.values.length) {
                this.sum -= this.values[this.next];
            } else {
                ++this.count;
            }
            this.values[this.next] = nanos;
            this.sum += nanos;
            this.next = (this.next + 1) % this.values.length;
        }

        int size() {
            return this.count;
        }

        double meanMillis() {
            return this.count == 0 ? Double.NaN : (double)this.sum / ((double)this.count * 1000000.0);
        }

        void clear() {
            this.count = 0;
            this.next = 0;
            this.sum = 0L;
        }
    }

    public record Snapshot(double guiMs, double hudMs, int guiSamples, int hudSamples, long glassCaptures, long blurPasses, long blurPixels) {
    }
}

