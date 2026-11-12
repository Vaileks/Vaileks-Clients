/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import ru.bloodhelper.client.music.MediaSnapshot;

record MediaVolumeCommand(String targetToken, long requestId, int thousandths) {
    static MediaVolumeCommand create(MediaSnapshot media, String expectedTarget, long requestId, double volume) {
        if (!(media.available() && media.canControlVolume() && requestId > 0L && Double.isFinite(volume) && media.volumeTargetToken().equals(expectedTarget))) {
            return null;
        }
        return new MediaVolumeCommand(expectedTarget, requestId, (int)Math.round(Math.clamp(volume, 0.0, 1.0) * 1000.0));
    }

    double volume() {
        return (double)this.thousandths / 1000.0;
    }

    String encode() {
        return "volume:" + this.targetToken + ":" + this.requestId + ":" + this.thousandths;
    }
}

