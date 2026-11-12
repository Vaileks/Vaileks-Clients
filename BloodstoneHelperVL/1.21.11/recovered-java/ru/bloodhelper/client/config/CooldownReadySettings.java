/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class CooldownReadySettings {
    public static final int MAX_GROUPS = 128;
    public boolean enabled = false;
    public boolean notify = true;
    public boolean sound = true;
    public double volume = 0.18;
    public boolean onlySelected = false;
    public int minimumSeconds = 1;
    public List<String> selectedGroups = new ArrayList<String>();

    public void sanitize() {
        this.minimumSeconds = Math.clamp((long)this.minimumSeconds, 0, 60);
        this.volume = Double.isFinite(this.volume) ? Math.clamp(this.volume, 0.0, 1.0) : 0.18;
        LinkedHashSet<String> clean = new LinkedHashSet<String>();
        if (this.selectedGroups != null) {
            for (String group : this.selectedGroups) {
                if (CooldownReadySettings.validGroup(group)) {
                    clean.add(group);
                }
                if (clean.size() != 128) continue;
                break;
            }
        }
        this.selectedGroups = new ArrayList<String>(clean);
    }

    public static boolean validGroup(String value) {
        return value != null && value.length() <= 256 && value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    public boolean accepts(String group, String pvpGroup) {
        return CooldownReadySettings.validGroup(group) && !group.equals(pvpGroup) && (!this.onlySelected || this.selectedGroups != null && this.selectedGroups.contains(group));
    }
}

