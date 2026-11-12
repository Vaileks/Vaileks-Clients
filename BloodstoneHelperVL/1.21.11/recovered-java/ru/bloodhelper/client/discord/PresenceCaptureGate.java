/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.discord;

import java.util.Objects;

final class PresenceCaptureGate {
    private long next;
    private long revision = Long.MIN_VALUE;
    private String uid;
    private boolean invalid = true;

    PresenceCaptureGate() {
    }

    boolean due(long now, long revision, String uid) {
        if (!this.invalid && now < this.next && this.revision == revision && Objects.equals(this.uid, uid)) {
            return false;
        }
        this.invalid = false;
        this.revision = revision;
        this.uid = uid;
        this.next = now + 500000000L;
        return true;
    }

    void invalidate() {
        this.invalid = true;
    }
}

