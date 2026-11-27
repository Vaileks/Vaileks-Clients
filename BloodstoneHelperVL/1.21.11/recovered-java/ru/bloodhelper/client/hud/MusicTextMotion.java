/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public final class MusicTextMotion {
    private static final Pattern GRAPHEME = Pattern.compile("\\X");

    private MusicTextMotion() {
    }

    public static void gradient(String text, double x, double width, GradientDraw draw) {
        if (text.isEmpty() || !Double.isFinite(x) || !Double.isFinite(width) || width <= 0.0) {
            return;
        }
        int left = (int)Math.floor(x) - 2;
        int right = (int)Math.ceil(x + width) + 2;
        int bands = Math.min(16, right - left);
        for (int i = 0; i < bands; ++i) {
            int from = left + (right - left) * i / bands;
            int to = left + (right - left) * (i + 1) / bands;
            draw.draw(text, from, to, 0.15 + 0.55 * (double)i / (double)Math.max(1, bands - 1));
        }
    }

    public static double offset(double textWidth, double viewport, double elapsedSeconds, double cueSeconds, boolean repeat) {
        double returnTime;
        double cycle;
        double travel;
        if (!Double.isFinite(textWidth) || !Double.isFinite(viewport) || viewport <= 0.0) {
            return 0.0;
        }
        double overflow = textWidth - viewport;
        if (overflow <= 0.0) {
            return (viewport - textWidth) * 0.5;
        }
        double elapsed = Double.isFinite(elapsedSeconds) ? Math.max(0.0, elapsedSeconds) : 0.0;
        cueSeconds = Double.isFinite(cueSeconds) ? Math.max(0.0, cueSeconds) : 0.0;
        double hold = repeat ? 1.35 : Math.min(0.7, Math.max(0.0, cueSeconds) * 0.12);
        double d = travel = repeat ? Math.max(1.2, overflow / 22.0) : Math.max(0.25, cueSeconds - hold - 0.3);
        if (repeat && (elapsed %= (cycle = hold + travel + 1.1 + (returnTime = Math.max(0.6, Math.min(1.4, travel * 0.25))))) > hold + travel + 1.1) {
            double back = MusicTextMotion.smooth((elapsed - hold - travel - 1.1) / returnTime);
            return -overflow * (1.0 - back);
        }
        return -overflow * MusicTextMotion.smooth((elapsed - hold) / travel);
    }

    private static double smooth(double t) {
        t = Math.clamp(t, 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    @FunctionalInterface
    public static interface GradientDraw {
        public void draw(String var1, int var2, int var3, double var4);
    }

    public static final class Reveal {
        private String text = "";
        private int[] ends = new int[0];

        public String prefix(String value, double fraction) {
            if (!this.text.equals(value)) {
                this.text = value;
                this.ends = GRAPHEME.matcher(this.text).results().mapToInt(MatchResult::end).toArray();
            }
            double progress = Double.isFinite(fraction) ? Math.clamp(fraction, 0.0, 1.0) : 0.0;
            int count = (int)Math.ceil((double)this.ends.length * progress);
            return count == 0 ? "" : this.text.substring(0, this.ends[count - 1]);
        }
    }
}

