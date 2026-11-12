/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.resourcepack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ServerPackSession {
    private Object owner;
    private final Map<UUID, Phase> packs = new LinkedHashMap<UUID, Phase>();

    public void offer(Object connection, UUID id) {
        if (this.owner != connection) {
            this.clear();
            this.owner = connection;
        }
        this.packs.remove(id);
        this.packs.put(id, Phase.LOADING);
    }

    public void result(Object connection, UUID id, boolean applied) {
        if (this.owner != connection || !this.packs.containsKey(id)) {
            return;
        }
        this.packs.put(id, applied ? Phase.APPLIED : Phase.FAILED);
    }

    public void pop(Object connection, Optional<UUID> id) {
        if (this.owner != connection) {
            return;
        }
        if (id.isPresent()) {
            this.packs.remove(id.get());
        } else {
            this.packs.clear();
        }
    }

    public Counts counts() {
        int completed = 0;
        int failed = 0;
        for (Phase phase : this.packs.values()) {
            if (phase == Phase.APPLIED) {
                ++completed;
                continue;
            }
            if (phase != Phase.FAILED) continue;
            ++failed;
        }
        return new Counts(completed, this.packs.size(), failed);
    }

    public void clear() {
        this.owner = null;
        this.packs.clear();
    }

    public static enum Phase {
        LOADING,
        APPLIED,
        FAILED;

    }

    public record Counts(int completedPacks, int totalPacks, int failedPacks) {
    }
}

