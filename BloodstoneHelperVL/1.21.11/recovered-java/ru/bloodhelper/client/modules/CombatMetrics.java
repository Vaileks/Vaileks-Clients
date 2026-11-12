/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

public final class CombatMetrics {
    private int pending = -1;
    private int target = -1;
    private int combo;
    private long attemptedAt;
    private long lastHit;
    private double pendingReach;
    private double reach = Double.NaN;

    public void attack(int entity, double distance, long now) {
        if (!Double.isFinite(distance) || distance < 0.0 || distance > 64.0) {
            return;
        }
        this.pending = entity;
        this.pendingReach = distance;
        this.attemptedAt = now;
    }

    public void confirmed(int victim, boolean localAttacker, long now, long timeout) {
        if (!localAttacker || victim != this.pending || now - this.attemptedAt < 0L || now - this.attemptedAt > 800000000L) {
            return;
        }
        this.combo = victim == this.target && now - this.lastHit <= timeout ? Math.min(999, this.combo + 1) : 1;
        this.target = victim;
        this.reach = this.pendingReach;
        this.lastHit = now;
        this.pending = -1;
    }

    public int combo(long now, long timeout) {
        return now - this.lastHit <= timeout ? this.combo : 0;
    }

    public double reach(long now, long timeout) {
        return now - this.lastHit <= timeout ? this.reach : Double.NaN;
    }

    public void reset() {
        this.target = -1;
        this.pending = -1;
        this.combo = 0;
        this.attemptedAt = 0L;
        this.lastHit = 0L;
        this.reach = Double.NaN;
    }
}

