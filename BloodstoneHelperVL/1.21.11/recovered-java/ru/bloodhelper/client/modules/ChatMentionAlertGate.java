/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

public final class ChatMentionAlertGate {
    private boolean alerted;
    private long lastAt;

    public boolean acquire(long nowMillis, int cooldownSeconds) {
        long elapsed = nowMillis - this.lastAt;
        if (this.alerted && elapsed >= 0L && elapsed < (long)Math.clamp((long)cooldownSeconds, 1, 30) * 1000L) {
            return false;
        }
        this.alerted = true;
        this.lastAt = nowMillis;
        return true;
    }

    public void clear() {
        this.alerted = false;
        this.lastAt = 0L;
    }
}

