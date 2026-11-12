/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

public final class AlertSoundGate {
    private long lastAt = Long.MIN_VALUE;

    public boolean acquire(long now) {
        if (this.lastAt != Long.MIN_VALUE && now >= this.lastAt && now - this.lastAt < 800L) {
            return false;
        }
        this.lastAt = now;
        return true;
    }

    public void clear() {
        this.lastAt = Long.MIN_VALUE;
    }
}

