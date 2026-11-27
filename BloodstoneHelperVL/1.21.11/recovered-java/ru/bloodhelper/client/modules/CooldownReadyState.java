/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CooldownReadyState {
    private static final int LIMIT = 256;
    private final Map<String, Cycle> cycles = new LinkedHashMap<String, Cycle>();

    public void started(String group, int tick, int duration, int minimumTicks) {
        this.cycles.remove(group);
        if (group == null || duration <= 0 || duration < Math.max(0, minimumTicks)) {
            return;
        }
        if (this.cycles.size() >= 256) {
            this.cycles.remove(this.cycles.keySet().iterator().next());
        }
        this.cycles.put(group, new Cycle(tick, duration));
    }

    public boolean naturallyEnded(String group, int tick) {
        Cycle cycle = this.cycles.remove(group);
        return cycle != null && (long)tick - (long)cycle.start == (long)cycle.duration;
    }

    public void cancelled(String group) {
        this.cycles.remove(group);
    }

    public void reset() {
        this.cycles.clear();
    }

    public int size() {
        return this.cycles.size();
    }

    private record Cycle(int start, int duration) {
    }
}

