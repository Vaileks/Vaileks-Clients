/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.bloodhelper.client.config.DropGuardSettings;
import ru.bloodhelper.client.modules.LegendaryItemCatalog;

public final class LegendarySizeSettings {
    public boolean enabled = false;
    public boolean enlargeDropped = true;
    public boolean scaleAllDropped = false;
    public double droppedScale = 1.0;
    public Map<String, Double> handScales = new LinkedHashMap<String, Double>();
    public List<SelectedItem> selectedItems = new ArrayList<SelectedItem>();

    public SelectedItem selected(String key) {
        if (this.selectedItems == null) {
            return null;
        }
        for (SelectedItem entry : this.selectedItems) {
            if (entry == null || entry.item == null || !entry.item.key().equals(key)) continue;
            return entry;
        }
        return null;
    }

    public double handScale(String string) {
        return 1.0;
    }

    public void setHandScale(String string, double d) {
    }

    public void sanitize() {
        this.droppedScale = this.groundScale();
        LinkedHashMap<String, Double> safe = new LinkedHashMap<String, Double>();
        for (LegendaryItemCatalog.Weapon weapon : LegendaryItemCatalog.weapons()) {
            if (this.handScales == null || !this.handScales.containsKey(weapon.id())) continue;
            safe.put(weapon.id(), this.handScale(weapon.id()));
        }
        this.handScales = safe;
        LinkedHashMap<String, SelectedItem> choices = new LinkedHashMap<String, SelectedItem>();
        if (this.selectedItems != null) {
            for (SelectedItem entry : this.selectedItems) {
                if (entry == null || entry.item == null) continue;
                DropGuardSettings identity = new DropGuardSettings();
                identity.protectedItems.add(entry.item);
                identity.sanitize();
                if (identity.protectedItems.isEmpty()) continue;
                entry.percent = Math.clamp((long)entry.percent, 10, 500);
                entry.droppedPercent = Math.clamp((long)entry.droppedPercent, 10, 500);
                choices.putIfAbsent(entry.item.key(), entry);
                if (choices.size() < 128) continue;
                break;
            }
        }
        this.selectedItems = new ArrayList(choices.values());
    }

    public float scale(String string, boolean bl, boolean bl2) {
        return 1.0f;
    }

    public double groundScale() {
        return 1.0;
    }

    public static final class SelectedItem {
        public DropGuardSettings.Entry item = new DropGuardSettings.Entry();
        public int percent = 65;
        public boolean handEnabled = true;
        public boolean droppedEnabled = false;
        public int droppedPercent = 100;

        public SelectedItem() {
        }

        public SelectedItem(DropGuardSettings.Entry item) {
            this.item = item;
        }
    }
}

