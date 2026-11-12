/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import ru.bloodhelper.client.config.ClientConfig;

public final class BlastLeggingsSettings
extends ClientConfig.WidgetSettings {
    public boolean onlyInCombat = false;
    public boolean showLevel = true;

    public BlastLeggingsSettings() {
        this.enabled = false;
        this.position = new ClientConfig.HudPosition(0.535, 0.5);
        this.position.scale = 1.0;
        this.background = false;
    }

    public void sanitize() {
        if (this.position == null) {
            this.position = new BlastLeggingsSettings().position;
        }
        this.position.x = Double.isFinite(this.position.x) ? Math.clamp(this.position.x, 0.0, 1.0) : 0.535;
        this.position.y = Double.isFinite(this.position.y) ? Math.clamp(this.position.y, 0.0, 1.0) : 0.5;
        this.position.scale = Double.isFinite(this.position.scale) ? Math.clamp(this.position.scale, 0.35, 2.0) : 1.0;
        this.alignment = "LEFT";
        this.bottom = false;
    }
}

