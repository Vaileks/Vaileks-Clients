/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import java.util.concurrent.TimeUnit;
import ru.bloodhelper.client.music.MediaSeekCommand;
import ru.bloodhelper.client.music.MediaSeekConfirmation;
import ru.bloodhelper.client.music.MediaSnapshot;

final class PendingMediaSeek {
    private static final long TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(2L);
    private long requestId;
    private long positionMs;
    private long queuedAtNanos;
    private String trackToken = "";

    PendingMediaSeek() {
    }

    void queued(MediaSeekCommand command, long nowNanos) {
        this.requestId = command.requestId();
        this.positionMs = command.positionMs();
        this.queuedAtNanos = nowNanos;
        this.trackToken = command.trackToken();
    }

    long base(MediaSnapshot sample, long nowNanos) {
        long observedPosition = sample.playbackPosition(nowNanos);
        if (this.requestId == 0L) {
            return observedPosition;
        }
        long elapsed = nowNanos - this.queuedAtNanos;
        boolean acknowledged = sample.seekRequestId() == this.requestId;
        boolean expired = elapsed < 0L || elapsed >= TIMEOUT_NANOS;
        boolean rejected = acknowledged && !sample.seekAccepted();
        boolean arrived = MediaSeekConfirmation.arrived(sample, this.requestId, this.positionMs, elapsed);
        if (expired || rejected || !sample.canSeek() || !this.trackToken.equals(sample.trackToken()) || arrived) {
            this.clear();
            return observedPosition;
        }
        return this.positionMs;
    }

    void clear() {
        this.requestId = 0L;
        this.trackToken = "";
    }
}

