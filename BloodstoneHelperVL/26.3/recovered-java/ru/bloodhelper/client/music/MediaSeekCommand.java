/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import ru.bloodhelper.client.music.MediaSnapshot;

record MediaSeekCommand(String trackToken, long requestId, long positionMs) {
    static MediaSeekCommand create(MediaSnapshot media, String expectedTrack, long requestId, long positionMs) {
        if (!(media.available() && media.canSeek() && requestId > 0L && media.trackToken().equals(expectedTrack))) {
            return null;
        }
        long target = Math.clamp(positionMs, 0L, Math.min(media.durationMs(), 31536000000L));
        return new MediaSeekCommand(media.trackToken(), requestId, target);
    }

    String encode() {
        return "seek:" + this.trackToken + ":" + this.requestId + ":" + this.positionMs;
    }
}

