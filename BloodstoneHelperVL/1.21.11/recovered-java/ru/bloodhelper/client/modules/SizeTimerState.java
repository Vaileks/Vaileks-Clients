/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

public final class SizeTimerState {
    private boolean observed;
    private boolean timed;
    private Size size = Size.NORMAL;
    private long startedTick;
    private long lastTick;
    private int durationTicks;

    public void observe(double scale, long tick, int enlargedSeconds, int reducedSeconds) {
        Size next;
        if (!Double.isFinite(scale) || scale <= 0.0 || this.observed && tick < this.lastTick) {
            this.reset();
        }
        if (!Double.isFinite(scale) || scale <= 0.0) {
            return;
        }
        Size size = scale > 1.15 ? Size.ENLARGED : (next = scale < 0.85 ? Size.REDUCED : Size.NORMAL);
        if (!this.observed || next != this.size) {
            this.timed = this.observed && next != Size.NORMAL;
            this.startedTick = tick;
            this.durationTicks = Math.clamp(next == Size.ENLARGED ? (long)enlargedSeconds : (long)reducedSeconds, 1, 120) * 20;
            this.size = next;
        }
        this.observed = true;
        this.lastTick = tick;
    }

    public Snapshot snapshot() {
        if (!this.observed || this.size == Size.NORMAL) {
            return null;
        }
        long remaining = (long)this.durationTicks - Math.max(0L, this.lastTick - this.startedTick);
        boolean overdue = this.timed && remaining <= 0L;
        return new Snapshot(this.size, this.timed && !overdue ? (double)remaining / 20.0 : Double.NaN, this.timed ? (float)Math.clamp((double)remaining / (double)this.durationTicks, 0.0, 1.0) : 0.0f, this.timed, overdue);
    }

    public void reset() {
        this.timed = false;
        this.observed = false;
        this.size = Size.NORMAL;
        this.lastTick = 0L;
        this.startedTick = 0L;
        this.durationTicks = 0;
    }

    public static enum Size {
        NORMAL,
        ENLARGED,
        REDUCED;

    }

    public record Snapshot(Size size, double seconds, float remaining, boolean estimated, boolean overdue) {
    }
}

