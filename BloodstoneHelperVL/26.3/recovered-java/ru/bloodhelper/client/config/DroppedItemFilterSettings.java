/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class DroppedItemFilterSettings {
    public boolean enabled;
    public List<Entry> selected = new ArrayList<Entry>();
    public static final int MAX_ENTRIES = 4096;

    public static boolean validId(String id) {
        return id != null && id.length() <= 256 && id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    public void sanitize() {
        LinkedHashMap<String, Entry> unique = new LinkedHashMap<String, Entry>();
        if (this.selected != null) {
            for (Entry entry : this.selected) {
                if (entry == null || !DroppedItemFilterSettings.validId(entry.itemId) || entry.itemId.equals("minecraft:air")) continue;
                if (entry.potionId == null) {
                    entry.potionId = "";
                }
                if (!entry.potionId.isEmpty() && !DroppedItemFilterSettings.validId(entry.potionId)) continue;
                unique.putIfAbsent(entry.key(), entry);
                if (unique.size() != 4096) continue;
                break;
            }
        }
        this.selected = new ArrayList(unique.values());
    }

    public static final class Entry {
        public String itemId = "";
        public String potionId = "";

        public Entry() {
        }

        public Entry(String itemId, String potionId) {
            this.itemId = itemId;
            this.potionId = potionId;
        }

        public String key() {
            return this.itemId + "\u0000" + this.potionId;
        }
    }
}

