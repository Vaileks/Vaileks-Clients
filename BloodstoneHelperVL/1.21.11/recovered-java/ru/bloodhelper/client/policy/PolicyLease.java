/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.policy;

import java.util.Objects;
import ru.bloodhelper.client.policy.ModulePolicy;

final class PolicyLease {
    private final long duration;
    private ModulePolicy current;
    private long acceptedAt;
    private long highestRevision;

    PolicyLease(long duration) {
        this.duration = duration;
    }

    ModulePolicy current() {
        return this.current;
    }

    boolean accept(ModulePolicy fetched, long now) {
        if (fetched != null && fetched.revision() < this.highestRevision) {
            fetched = null;
        }
        if (fetched != null) {
            this.highestRevision = fetched.revision();
            this.acceptedAt = now;
        }
        boolean changed = !Objects.equals(this.current, fetched);
        this.current = fetched;
        return changed;
    }

    boolean expire(long now) {
        if (this.current == null || now - this.acceptedAt < this.duration) {
            return false;
        }
        this.current = null;
        return true;
    }
}

