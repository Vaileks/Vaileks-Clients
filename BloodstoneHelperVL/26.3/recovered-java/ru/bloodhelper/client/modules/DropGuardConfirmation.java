/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

public final class DropGuardConfirmation<T> {
    public static final long WINDOW_NANOS = 2000000000L;
    private final BiPredicate<T, T> equal;
    private final UnaryOperator<T> copy;
    private Object context;
    private T fingerprint;
    private long firstToken;
    private long startedAt;
    private long consumedToken;
    private long lastAttemptToken;

    public DropGuardConfirmation(BiPredicate<T, T> equal, UnaryOperator<T> copy) {
        this.equal = equal;
        this.copy = copy;
    }

    public boolean confirm(Object context, T fingerprint, long physicalPress, long now) {
        if (physicalPress <= 0L || physicalPress == this.consumedToken || physicalPress <= this.lastAttemptToken) {
            return false;
        }
        this.lastAttemptToken = physicalPress;
        if (this.context != null && Objects.equals(this.context, context) && this.equal.test(this.fingerprint, fingerprint) && now >= this.startedAt && now - this.startedAt <= 2000000000L) {
            if (physicalPress <= this.firstToken) {
                return false;
            }
            this.consumedToken = physicalPress;
            this.clearPending();
            return true;
        }
        this.context = context;
        this.fingerprint = this.copy.apply(fingerprint);
        this.firstToken = physicalPress;
        this.startedAt = now;
        return false;
    }

    public void expire(long now) {
        if (this.context != null && (now < this.startedAt || now - this.startedAt > 2000000000L)) {
            this.clearPending();
        }
    }

    public void reset() {
        this.clearPending();
        this.lastAttemptToken = 0L;
        this.consumedToken = 0L;
    }

    private void clearPending() {
        this.context = null;
        this.fingerprint = null;
        this.startedAt = 0L;
        this.firstToken = 0L;
    }
}

