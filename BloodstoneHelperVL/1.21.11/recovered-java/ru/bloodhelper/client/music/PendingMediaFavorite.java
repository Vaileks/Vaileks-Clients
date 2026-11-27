/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import ru.bloodhelper.client.music.MediaFavoriteCommand;
import ru.bloodhelper.client.music.MediaSnapshot;

final class PendingMediaFavorite {
    private MediaFavoriteCommand command;
    private long queuedAt;

    PendingMediaFavorite() {
    }

    void queued(MediaFavoriteCommand value, long now) {
        this.command = value;
        this.queuedAt = now;
    }

    void clear() {
        this.command = null;
    }

    void rejected(String wire) {
        if (this.command != null && this.command.encode().equals(wire)) {
            this.clear();
        }
    }

    boolean active(MediaSnapshot snapshot, long now) {
        if (this.command != null && (now - this.queuedAt >= 10000000000L || !this.command.trackToken().equals(snapshot.trackToken()) || snapshot.favorite().requestId() >= this.command.requestId() || snapshot.favorite().saved())) {
            this.clear();
        }
        return this.command != null;
    }
}

