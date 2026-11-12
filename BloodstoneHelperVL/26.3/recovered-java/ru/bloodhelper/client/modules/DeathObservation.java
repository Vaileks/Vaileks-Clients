/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

public final class DeathObservation<T> {
    private Object player;
    private T lastAlive;
    private boolean alive;

    public T sample(Object currentPlayer, boolean currentAlive, T currentSnapshot) {
        if (this.player != currentPlayer) {
            this.player = currentPlayer;
            this.alive = false;
            this.lastAlive = null;
        }
        if (currentAlive) {
            this.alive = true;
            this.lastAlive = currentSnapshot;
            return null;
        }
        T death = this.alive ? (T)this.lastAlive : null;
        this.alive = false;
        return death;
    }

    public void reset() {
        this.player = null;
        this.lastAlive = null;
        this.alive = false;
    }
}

