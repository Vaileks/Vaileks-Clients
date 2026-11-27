/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import ru.bloodhelper.client.config.QuickCommandsSettings;

public final class DropGuardSettings {
    public static final int MAX_ITEMS = 128;
    public boolean enabled = false;
    public List<Entry> protectedItems = new ArrayList<Entry>();

    public void sanitize() {
        LinkedHashMap<String, Entry> unique = new LinkedHashMap<String, Entry>();
        if (this.protectedItems != null) {
            for (Entry entry : this.protectedItems) {
                if (entry == null || entry.itemId == null || !entry.itemId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || entry.itemId.length() > 256 || entry.itemId.equals("minecraft:air")) continue;
                entry.name = DropGuardSettings.clean(entry.name, 160);
                entry.customName = DropGuardSettings.clean(entry.customName, 512);
                entry.itemModel = DropGuardSettings.clean(entry.itemModel, 256);
                String string = entry.customModel = entry.customModel == null ? "" : entry.customModel;
                if (!entry.customModel.isEmpty() && !entry.customModel.matches("[0-9a-f]{64}")) continue;
                unique.putIfAbsent(entry.key(), entry);
                if (unique.size() < 128) continue;
                break;
            }
        }
        this.protectedItems = new ArrayList(unique.values());
    }

    public static String clean(String text, int maximum) {
        return QuickCommandsSettings.clean(text, maximum);
    }

    public static final class Entry {
        public String itemId = "";
        public String name = "";
        public String customName = "";
        public String itemModel = "";
        public String customModel = "";

        public Entry() {
        }

        public Entry(String itemId, String name, String customName, String itemModel, String customModel) {
            this.itemId = itemId;
            this.name = name;
            this.customName = customName;
            this.itemModel = itemModel;
            this.customModel = customModel;
        }

        public String key() {
            return this.itemId + "\u0000" + this.customName + "\u0000" + this.itemModel + "\u0000" + this.customModel;
        }
    }
}

