/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

final class CombatStateClock {
    private int remainingTicks;
    private int durationTicks;
    private long endTick;
    private int lastTick;
    private long indicatorEndTick;
    private boolean correctedIndicator;

    CombatStateClock() {
    }

    void sample(int nativeTick, int startTick, int endTick) {
        this.sample(nativeTick, startTick, endTick, false);
    }

    void sample(int nativeTick, int startTick, int nativeEndTick, boolean bloodstone) {
        boolean corrected;
        long duration = (long)nativeEndTick - (long)startTick;
        boolean bl = corrected = bloodstone && duration == 300L;
        if (corrected) {
            duration = 600L;
        }
        long remaining = (long)startTick + duration - (long)nativeTick;
        if (startTick > nativeTick || duration <= 0L || duration > Integer.MAX_VALUE || remaining <= 0L || remaining > duration) {
            this.clear();
            return;
        }
        this.remainingTicks = (int)remaining;
        this.durationTicks = (int)duration;
        this.endTick = (long)startTick + duration;
        this.lastTick = nativeTick;
        this.indicatorEndTick = nativeEndTick;
        this.correctedIndicator = corrected;
    }

    void removed(int nativeTick) {
        if (this.correctedIndicator && this.active() && nativeTick >= this.lastTick && (long)nativeTick >= this.indicatorEndTick - 1L) {
            this.advance(nativeTick);
        } else {
            this.clear();
        }
    }

    void advance(int nativeTick) {
        if (!this.active()) {
            return;
        }
        long remaining = this.endTick - (long)nativeTick;
        if (nativeTick < this.lastTick || remaining <= 0L) {
            this.clear();
            return;
        }
        this.remainingTicks = (int)remaining;
        this.lastTick = nativeTick;
    }

    void clear() {
        this.remainingTicks = 0;
        this.durationTicks = 0;
        this.endTick = 0L;
        this.lastTick = 0;
        this.indicatorEndTick = 0L;
        this.correctedIndicator = false;
    }

    boolean active() {
        return this.remainingTicks > 0;
    }

    double secondsRemaining() {
        return (double)this.remainingTicks / 20.0;
    }

    float remainingFraction() {
        return this.durationTicks == 0 ? 0.0f : (float)this.remainingTicks / (float)this.durationTicks;
    }
}

