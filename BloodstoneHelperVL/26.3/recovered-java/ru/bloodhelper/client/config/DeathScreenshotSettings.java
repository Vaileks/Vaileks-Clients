/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class DeathScreenshotSettings {
    public boolean enabled = false;
    public boolean notifySaved = true;
    public int galleryLimit = 100;

    public void sanitize() {
        this.galleryLimit = Math.clamp((long)this.galleryLimit, 10, 200);
    }
}

