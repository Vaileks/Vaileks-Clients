/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

public final class HitboxVisibility {
    private boolean moduleEnabled;
    private boolean hidden;

    public boolean visible(boolean enabled) {
        if (this.moduleEnabled != enabled) {
            this.moduleEnabled = enabled;
            this.hidden = false;
        }
        return enabled && !this.hidden;
    }

    public boolean toggle(boolean enabled) {
        this.visible(enabled);
        if (enabled) {
            this.hidden = !this.hidden;
        }
        return enabled && !this.hidden;
    }
}

