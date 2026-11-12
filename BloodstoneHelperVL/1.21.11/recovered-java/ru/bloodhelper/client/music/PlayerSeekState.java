/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import ru.bloodhelper.client.music.MediaSeekConfirmation;
import ru.bloodhelper.client.music.MediaSnapshot;

public final class PlayerSeekState {
    private String token = "";
    private long duration;
    private long position;
    private long request;
    private long queued;
    private boolean dragging;

    public boolean dragging() {
        return this.dragging;
    }

    public String token() {
        return this.token;
    }

    public long target() {
        return this.position;
    }

    public boolean begin(MediaSnapshot sample, double ratio) {
        this.cancel();
        if (!sample.available() || !sample.canSeek() || sample.trackToken().isEmpty() || sample.durationMs() <= 0L) {
            return false;
        }
        this.token = sample.trackToken();
        this.duration = sample.durationMs();
        this.dragging = true;
        this.move(ratio);
        return true;
    }

    public void move(double ratio) {
        if (this.dragging) {
            this.position = Math.round((double)this.duration * (Double.isFinite(ratio) ? Math.clamp(ratio, 0.0, 1.0) : 0.0));
        }
    }

    public void submitted(long id, long now) {
        this.dragging = false;
        this.request = id;
        this.queued = now;
        if (id <= 0L) {
            this.cancel();
        }
    }

    public long observe(MediaSnapshot sample, long now) {
        if (!this.token.equals(sample.trackToken()) || !sample.canSeek()) {
            this.cancel();
        }
        if (this.request > 0L && (now - this.queued >= 2000000000L || now < this.queued || sample.seekRequestId() == this.request && !sample.seekAccepted() || MediaSeekConfirmation.arrived(sample, this.request, this.position, now - this.queued))) {
            this.cancel();
        }
        return this.dragging || this.request > 0L ? this.position : sample.playbackPosition(now);
    }

    public void cancel() {
        this.token = "";
        this.dragging = false;
        this.request = 0L;
    }
}

