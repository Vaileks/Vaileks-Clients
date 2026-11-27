/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import ru.bloodhelper.client.config.ClientConfig;

public final class StaffStatsSettings
extends ClientConfig.WidgetSettings {
    public boolean showBans = true;
    public boolean showMutes = true;
    public boolean showVoiceMutes = true;
    public boolean showReports = true;

    public StaffStatsSettings() {
        this.position = new ClientConfig.HudPosition(0.02, 0.47);
    }
}

