/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class AbilityGuideSettings {
    public boolean enabled = false;
    public List<Entry> entries = new ArrayList<Entry>();
    public static final int LIMIT = 64;

    public void sanitize() {
        LinkedHashMap<String, Entry> valid = new LinkedHashMap<String, Entry>();
        if (this.entries != null) {
            for (Entry entry : this.entries) {
                if (entry == null || entry.id == null || !entry.id.matches("[0-9a-f]{64}")) continue;
                entry.name = AbilityGuideSettings.clean(entry.name, 128);
                if (entry.name.isBlank()) continue;
                entry.itemId = AbilityGuideSettings.clean(entry.itemId, 128);
                if (!entry.itemId.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
                    entry.itemId = "minecraft:air";
                }
                entry.notes = AbilityGuideSettings.clean(entry.notes, 384);
                entry.description = new ArrayList<String>(entry.description == null ? List.of() : entry.description.stream().limit(64L).map(line -> AbilityGuideSettings.clean(line, 512)).toList());
                valid.putIfAbsent(entry.id, entry);
                if (valid.size() != 64) continue;
                break;
            }
        }
        this.entries = new ArrayList(valid.values());
    }

    public Entry remember(Entry observation) {
        this.sanitize();
        for (Entry entry : this.entries) {
            if (!entry.id.equals(observation.id)) continue;
            entry.name = observation.name;
            entry.itemId = observation.itemId;
            entry.description = new ArrayList<String>(observation.description);
            return entry;
        }
        this.entries.addFirst(observation);
        if (this.entries.size() > 64) {
            this.entries.removeLast();
        }
        return observation;
    }

    public static String clean(String value, int limit) {
        if (value == null || limit <= 0) {
            return "";
        }
        StringBuilder safe = new StringBuilder(Math.min(limit, value.length()));
        int offset = 0;
        while (offset < value.length()) {
            int cp = value.codePointAt(offset);
            offset += Character.charCount(cp);
            if (cp == 167) {
                if (offset >= value.length()) continue;
                offset += Character.charCount(value.codePointAt(offset));
                continue;
            }
            if (cp >= 55296 && cp <= 57343) continue;
            if (Character.isISOControl(cp) || Character.getType(cp) == 13 || Character.getType(cp) == 14) {
                cp = 32;
            }
            safe.appendCodePoint(cp);
        }
        String text = safe.toString().strip();
        int end = Math.min(limit, text.length());
        if (end > 0 && end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))) {
            --end;
        }
        return text.substring(0, end);
    }

    public static final class Entry {
        public String id = "";
        public String name = "";
        public String itemId = "minecraft:air";
        public String notes = "";
        public List<String> description = new ArrayList<String>();
    }
}

