/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class StreamerSettings {
    public boolean enabled = false;
    public boolean hideChat = true;
    public boolean hideCoordinates = true;
    public boolean hideServerAddress = true;
    public boolean hideNickname = true;
    public boolean hideServerOverlays = true;
    public boolean hideNametags = true;

    public void sanitize() {
    }

    public boolean hidesChat() {
        return this.enabled && this.hideChat;
    }

    public boolean hidesCoordinates() {
        return this.enabled && this.hideCoordinates;
    }

    public boolean hidesServerAddress() {
        return this.enabled && this.hideServerAddress;
    }

    public boolean hidesNickname() {
        return this.enabled && this.hideNickname;
    }

    public boolean hidesServerOverlays() {
        return this.enabled && this.hideServerOverlays;
    }

    public boolean hidesNametags() {
        return this.enabled && this.hideNametags;
    }

    public boolean hidesDebug() {
        return this.enabled && (this.hideCoordinates || this.hideServerAddress || this.hideNickname);
    }

    public String displayName(String original) {
        return this.hidesNickname() ? "Streamer" : original;
    }
}

