/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class BackgroundAlertsSettings {
    public boolean enabled = false;
    public boolean mentions = true;
    public boolean combat = true;
    public boolean disconnect = true;
    public boolean sound = false;
    public int cooldownSeconds = 10;

    public void sanitize() {
        this.cooldownSeconds = Math.clamp((long)this.cooldownSeconds, 3, 60);
    }
}

