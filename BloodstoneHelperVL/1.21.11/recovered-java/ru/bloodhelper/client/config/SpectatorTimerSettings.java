/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import ru.bloodhelper.client.config.ClientConfig;

public final class SpectatorTimerSettings
extends ClientConfig.WidgetSettings {
    public boolean continueAcrossModes = true;
    public boolean onlyWhileSpectating = false;

    public SpectatorTimerSettings() {
        this.position = new ClientConfig.HudPosition(0.02, 0.36);
    }
}

