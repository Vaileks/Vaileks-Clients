/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import ru.bloodhelper.client.music.MediaSnapshot;

public final class MediaSeekConfirmation {
    private static final long POSITION_TOLERANCE_MS = 1500L;

    private MediaSeekConfirmation() {
    }

    public static boolean arrived(MediaSnapshot sample, long requestId, long targetMs, long elapsedNanos) {
        if (requestId <= 0L || targetMs < 0L || elapsedNanos < 0L || !sample.canSeek() || sample.seekRequestId() != requestId || !sample.seekAccepted()) {
            return false;
        }
        double advance = sample.playing() ? (double)elapsedNanos / 1000000.0 * sample.playbackRate() : 0.0;
        double distance = (double)sample.positionMs() - (double)targetMs;
        return distance >= -1500.0 && distance <= advance + 1500.0;
    }
}

