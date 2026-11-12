/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import ru.bloodhelper.client.music.MediaSnapshot;

record MediaFavoriteCommand(String trackToken, long requestId) {
    static MediaFavoriteCommand create(MediaSnapshot snapshot, String expectedTrack, long requestId) {
        if (!(snapshot != null && snapshot.available() && snapshot.favorite().canAdd() && requestId > 0L && snapshot.trackToken().matches("[a-f0-9]{32}") && snapshot.trackToken().equals(expectedTrack))) {
            return null;
        }
        return new MediaFavoriteCommand(expectedTrack, requestId);
    }

    String encode() {
        return "favorite:" + this.trackToken + ":" + this.requestId;
    }
}

