/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.lyrics;

public final class LyricTransition {
    private int current = -1;
    private int previous = -1;
    private long changed;
    private long position = Long.MIN_VALUE;
    private String track = "";

    public void update(String identity, int index, long playback, long now, boolean animate) {
        boolean reset;
        boolean bl = reset = !this.track.equals(identity) || this.position != Long.MIN_VALUE && (playback < this.position - 400L || playback > this.position + 1800L);
        if (reset) {
            this.current = index;
            this.previous = -1;
            this.changed = 0L;
            this.track = identity;
        } else if (this.current != index) {
            this.previous = animate ? this.current : -1;
            this.current = index;
            this.changed = animate ? now : 0L;
        }
        this.position = playback;
        if (!animate || now - this.changed >= 240000000L) {
            this.previous = -1;
        }
    }

    public double weight(int index, long now) {
        double t = this.changed == 0L ? 1.0 : Math.clamp((double)(now - this.changed) / 2.4E8, 0.0, 1.0);
        double smooth = t * t * (3.0 - 2.0 * t);
        return index == this.current ? (this.previous < 0 ? 1.0 : smooth) : (index == this.previous ? 1.0 - smooth : 0.0);
    }
}

