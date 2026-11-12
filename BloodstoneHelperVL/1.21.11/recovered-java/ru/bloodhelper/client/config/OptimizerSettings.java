/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import ru.bloodhelper.client.config.DroppedItemFilterSettings;

public final class OptimizerSettings {
    public boolean enabled = false;
    public boolean signs = true;
    public boolean hideGrass = true;
    public boolean disableEntityShadows = true;
    public boolean decorativeParticles = true;
    public int signDistance = 32;
    public int signTextDistance = 16;
    public DroppedItemFilterSettings droppedItems = new DroppedItemFilterSettings();

    public void sanitize() {
        if (this.droppedItems == null) {
            this.droppedItems = new DroppedItemFilterSettings();
        }
        this.droppedItems.sanitize();
        this.signDistance = Math.clamp((long)this.signDistance, 16, 64);
        this.signTextDistance = Math.clamp((long)this.signTextDistance, 8, this.signDistance);
    }

    public Snapshot snapshot() {
        int distance = Math.clamp((long)this.signDistance, 16, 64);
        int text = Math.clamp((long)this.signTextDistance, 8, distance);
        return new Snapshot(this.enabled && this.signs, this.enabled && this.hideGrass, this.enabled && this.disableEntityShadows, this.enabled && this.decorativeParticles, distance * distance, text * text);
    }

    public record Snapshot(boolean signs, boolean hideGrass, boolean disableEntityShadows, boolean decorativeParticles, int signDistanceSquared, int textDistanceSquared) {
        public boolean hideSign(double distanceSquared) {
            return this.signs && Double.isFinite(distanceSquared) && distanceSquared > (double)this.signDistanceSquared;
        }

        public boolean hideSignText(double distanceSquared) {
            return this.signs && Double.isFinite(distanceSquared) && distanceSquared > (double)this.textDistanceSquared;
        }
    }
}

