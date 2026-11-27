/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.lyrics;

public final class LyricCrossfade {
    private String track = "";
    private String current = "";
    private String previous = "";
    private long changed;
    private long lastFrame;
    private long position = Long.MIN_VALUE;

    public Frame update(String identity, String text, long playback, long now, boolean enabled) {
        double t;
        boolean reset;
        boolean bl = reset = !enabled || !this.track.equals(identity) || now - this.lastFrame > 1500000000L || this.position != Long.MIN_VALUE && (playback < this.position - 400L || playback > this.position + 1800L);
        if (reset) {
            this.current = text;
            this.previous = "";
            this.changed = 0L;
        } else if (!text.equals(this.current)) {
            this.previous = this.current;
            this.current = text;
            this.changed = now;
        }
        this.track = identity;
        this.position = playback;
        this.lastFrame = now;
        double d = t = this.changed == 0L ? 1.0 : Math.clamp((double)(now - this.changed) / 2.2E8, 0.0, 1.0);
        if (t >= 1.0) {
            this.previous = "";
        }
        return new Frame(this.previous, t * t * (3.0 - 2.0 * t));
    }

    public record Frame(String previous, double progress) {
    }
}

