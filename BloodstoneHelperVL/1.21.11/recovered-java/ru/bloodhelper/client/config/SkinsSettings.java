/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.Set;

public final class SkinsSettings {
    public boolean enabled = false;
    public String character = "RUDY";
    public boolean heldItems = true;
    public boolean share = true;
    public boolean showOthers = true;

    public void sanitize() {
        if (this.character == null || !Set.of("RUDY", "VERITI", "JENNY", "MIMI_TYPH").contains(this.character) && !this.character.matches("CUSTOM_[a-f0-9]{64}")) {
            this.character = "RUDY";
        }
    }
}

