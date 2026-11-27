/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class ChatViolationsSettings {
    public boolean enabled = false;
    public boolean notification = true;
    public String phrases = "";
    public int highlightColor = -43691;
    public boolean wholeWords = true;
    public boolean sound = true;
    public int cooldownSeconds = 2;

    public void sanitize() {
        if (this.phrases == null) {
            this.phrases = "";
        }
        this.phrases = this.phrases.replace('\n', ' ').replace('\r', ' ').replace('\u0000', ' ');
        if (this.phrases.length() > 4096) {
            this.phrases = this.phrases.substring(0, 4096);
        }
        this.highlightColor |= 0xFF000000;
        this.cooldownSeconds = Math.clamp((long)this.cooldownSeconds, 1, 30);
    }
}

