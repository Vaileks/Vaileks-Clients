/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class AiAssistantSettings {
    public boolean enabled = false;
    public int threads = 1;

    public void sanitize() {
        this.threads = Math.clamp((long)this.threads, 1, 2);
    }
}

