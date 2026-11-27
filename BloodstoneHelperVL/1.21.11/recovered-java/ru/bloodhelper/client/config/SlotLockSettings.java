/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.LinkedHashSet;
import java.util.Set;

public final class SlotLockSettings {
    public boolean enabled = false;
    public boolean showHint = true;
    public Set<Integer> slots = new LinkedHashSet<Integer>();

    public static boolean valid(int slot) {
        return slot >= 0 && slot <= 40;
    }

    public boolean locked(int slot) {
        return this.enabled && SlotLockSettings.valid(slot) && this.slots != null && this.slots.contains(slot);
    }

    public void toggle(int slot) {
        if (!SlotLockSettings.valid(slot)) {
            return;
        }
        if (this.slots == null) {
            this.slots = new LinkedHashSet<Integer>();
        }
        if (!this.slots.remove(slot)) {
            this.slots.add(slot);
        }
    }

    public void sanitize() {
        LinkedHashSet<Integer> safe = new LinkedHashSet<Integer>();
        if (this.slots != null) {
            for (Integer slot : this.slots) {
                if (slot == null || !SlotLockSettings.valid(slot)) continue;
                safe.add(slot);
            }
        }
        this.slots = safe;
    }
}

