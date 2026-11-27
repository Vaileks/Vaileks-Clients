/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import ru.bloodhelper.client.music.MediaSnapshot;

final class PendingMediaPlayback {
    private String token = "";
    private boolean target;
    private long queuedAt;
    private long position;

    PendingMediaPlayback() {
    }

    void toggle(MediaSnapshot current, long now) {
        MediaSnapshot visible = this.view(current, now);
        this.token = current.trackToken();
        this.target = !visible.playing();
        this.position = visible.playbackPosition(now);
        this.queuedAt = now;
    }

    MediaSnapshot view(MediaSnapshot current, long now) {
        if (this.token.isEmpty()) {
            return current;
        }
        long elapsed = now - this.queuedAt;
        if (!current.available() || !this.token.equals(current.trackToken()) || elapsed < 0L || elapsed > 1500000000L || current.observedAtNanos() > this.queuedAt && current.playing() == this.target) {
            this.clear();
            return current;
        }
        long advanced = this.target ? (long)((double)elapsed / 1000000.0 * current.playbackRate()) : 0L;
        return current.withPlayback(this.position + advanced, this.target, now);
    }

    void clear() {
        this.token = "";
    }
}

