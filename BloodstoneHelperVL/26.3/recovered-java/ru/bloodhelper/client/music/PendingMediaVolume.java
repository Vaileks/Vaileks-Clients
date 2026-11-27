/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import java.util.concurrent.TimeUnit;
import ru.bloodhelper.client.music.MediaSnapshot;
import ru.bloodhelper.client.music.MediaVolumeCommand;

final class PendingMediaVolume {
    private MediaVolumeCommand pending;
    private long queuedNanos;

    PendingMediaVolume() {
    }

    void queued(MediaVolumeCommand command, long nowNanos) {
        this.pending = command;
        this.queuedNanos = nowNanos;
    }

    MediaSnapshot reconcile(MediaSnapshot observed, long nowNanos) {
        if (this.pending == null) {
            return observed;
        }
        long elapsed = nowNanos - this.queuedNanos;
        if (!observed.canControlVolume() || !this.pending.targetToken().equals(observed.volumeTargetToken()) || elapsed < 0L || elapsed >= TimeUnit.SECONDS.toNanos(2L) || observed.volumeRequestId() >= this.pending.requestId()) {
            this.clear();
            return observed;
        }
        return observed.withVolume(this.pending.volume());
    }

    void clear() {
        this.pending = null;
    }
}

