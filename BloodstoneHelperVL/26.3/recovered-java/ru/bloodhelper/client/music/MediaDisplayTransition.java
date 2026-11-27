/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import ru.bloodhelper.client.music.MediaFavoriteState;
import ru.bloodhelper.client.music.MediaSnapshot;

public final class MediaDisplayTransition {
    private static final long GRACE_NANOS = 800000000L;
    private MediaSnapshot last;
    private MediaSnapshot frozen;
    private long lastAvailable;
    private boolean holding;

    public MediaSnapshot observe(MediaSnapshot sample, long now) {
        if (sample.available()) {
            this.last = sample;
            this.lastAvailable = now;
            this.frozen = null;
            this.holding = false;
            return sample;
        }
        long gap = now - this.lastAvailable;
        if (this.last == null || gap < 0L || gap >= 800000000L) {
            this.clear();
            return sample;
        }
        if (this.frozen == null) {
            this.frozen = new MediaSnapshot(this.last.title(), this.last.artist(), this.last.playbackPosition(now), this.last.durationMs(), this.last.playing(), this.last.artwork(), this.last.revision(), false, this.last.trackToken(), 0L, false, 0.0, now, false, 0.0, "", 0L, false, MediaFavoriteState.unavailable());
        }
        this.holding = true;
        return this.frozen;
    }

    public boolean holding() {
        return this.holding;
    }

    public void clear() {
        this.last = null;
        this.frozen = null;
        this.lastAvailable = 0L;
        this.holding = false;
    }
}

