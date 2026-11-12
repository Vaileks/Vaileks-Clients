/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class ChatMentionsSettings {
    public boolean enabled = false;
    public boolean highlight = true;
    public boolean notification = true;
    public boolean sound = true;
    public double soundVolume = 0.12;
    public int cooldownSeconds = 3;

    public void sanitize() {
        this.soundVolume = Double.isFinite(this.soundVolume) ? Math.clamp(this.soundVolume, 0.0, 0.5) : 0.12;
        this.cooldownSeconds = Math.clamp((long)this.cooldownSeconds, 1, 30);
    }
}

