/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.HashMap;
import java.util.Map;

public final class DropGuardInput {
    private final Map<Long, Long> down = new HashMap<Long, Long>();
    private long serial;
    private long token;

    public void event(long input, int action) {
        if (action == 0) {
            this.token = this.down.getOrDefault(input, 0L);
            this.down.remove(input);
        } else if (action == 1) {
            this.token = this.down.computeIfAbsent(input, ignored -> ++this.serial);
        } else if (action == 2) {
            this.token = this.down.getOrDefault(input, 0L);
        }
    }

    public long token() {
        return this.token;
    }

    public void reset() {
        this.down.clear();
        this.token = 0L;
        this.serial = 0L;
    }
}

