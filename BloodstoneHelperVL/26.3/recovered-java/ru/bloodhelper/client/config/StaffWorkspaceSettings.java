/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import ru.bloodhelper.client.config.ClientConfig;

public final class StaffWorkspaceSettings {
    public boolean journal;
    public boolean shifts;
    public boolean reminders;
    public boolean returns;
    public boolean observations;
    public boolean status;
    public int idleMinutes;
    public String observeCommand = "";
    public ClientConfig.HudPosition statusPosition = new ClientConfig.HudPosition(0.4, 0.03);

    public void sanitize() {
        this.idleMinutes = Math.clamp((long)this.idleMinutes, 0, 120);
        if (this.statusPosition == null) {
            this.statusPosition = new ClientConfig.HudPosition(0.4, 0.03);
        }
        this.statusPosition.x = Double.isFinite(this.statusPosition.x) ? Math.clamp(this.statusPosition.x, 0.0, 1.0) : 0.4;
        this.statusPosition.y = Double.isFinite(this.statusPosition.y) ? Math.clamp(this.statusPosition.y, 0.0, 1.0) : 0.03;
        double d = this.statusPosition.scale = Double.isFinite(this.statusPosition.scale) ? Math.clamp(this.statusPosition.scale, 0.35, 2.0) : 0.65;
        if (this.observeCommand == null) {
            this.observeCommand = "";
        }
        this.observeCommand = this.observeCommand.replace('\n', ' ').replace('\r', ' ').strip();
        if (this.observeCommand.length() > 256) {
            this.observeCommand = this.observeCommand.substring(0, 256);
        }
    }
}

