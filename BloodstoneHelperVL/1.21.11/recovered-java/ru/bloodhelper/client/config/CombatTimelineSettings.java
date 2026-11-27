/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class CombatTimelineSettings {
    public boolean enabled = false;
    public int windowSeconds = 20;
    public boolean showHealth = true;
    public boolean showEffects = true;
    public boolean showTotems = true;

    public void sanitize() {
        this.windowSeconds = Math.clamp((long)this.windowSeconds, 10, 60);
    }
}

