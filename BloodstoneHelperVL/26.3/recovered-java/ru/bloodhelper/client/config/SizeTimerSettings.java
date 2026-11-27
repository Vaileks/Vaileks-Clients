/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import ru.bloodhelper.client.config.ClientConfig;

public final class SizeTimerSettings
extends ClientConfig.WidgetSettings {
    public int enlargedSeconds = 30;
    public int reducedSeconds = 25;
    public boolean showProgress = true;

    public SizeTimerSettings() {
        this.position = new ClientConfig.HudPosition(0.02, 0.26);
    }

    public void sanitize() {
        this.enlargedSeconds = Math.clamp((long)this.enlargedSeconds, 1, 120);
        this.reducedSeconds = Math.clamp((long)this.reducedSeconds, 1, 120);
    }
}

