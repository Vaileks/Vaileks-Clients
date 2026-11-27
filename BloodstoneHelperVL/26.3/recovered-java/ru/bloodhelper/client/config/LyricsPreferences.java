/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LyricsPreferences {
    public Map<String, Integer> offsets = new LinkedHashMap<String, Integer>();
    public Map<String, String> records = new LinkedHashMap<String, String>();

    public int offset(String key) {
        Integer value = this.offsets == null ? null : this.offsets.get(key);
        return value == null ? 0 : Math.clamp((long)value.intValue(), -30000, 30000);
    }

    public String preferredRecord(String key) {
        return this.records == null || this.records.get(key) == null ? "" : this.records.get(key);
    }

    public void setOffset(String key, int value) {
        if (key == null || key.isEmpty()) {
            return;
        }
        if (this.offsets == null) {
            this.offsets = new LinkedHashMap<String, Integer>();
        }
        this.offsets.remove(key);
        if (value != 0) {
            this.offsets.put(key, Math.clamp((long)value, -30000, 30000));
        }
        LyricsPreferences.trim(this.offsets);
    }

    public void preferRecord(String key, String id) {
        if (key == null || key.isEmpty() || id == null || !id.matches("[0-9]{1,20}")) {
            return;
        }
        if (this.records == null) {
            this.records = new LinkedHashMap<String, String>();
        }
        this.records.remove(key);
        this.records.put(key, id);
        LyricsPreferences.trim(this.records);
    }

    public void sanitize() {
        if (this.offsets == null) {
            this.offsets = new LinkedHashMap<String, Integer>();
        }
        if (this.records == null) {
            this.records = new LinkedHashMap<String, String>();
        }
        this.offsets.entrySet().removeIf(e -> LyricsPreferences.invalidKey((String)e.getKey()) || e.getValue() == null || (Integer)e.getValue() == 0);
        this.offsets.replaceAll((key, value) -> Math.clamp((long)value.intValue(), -30000, 30000));
        this.records.entrySet().removeIf(e -> LyricsPreferences.invalidKey((String)e.getKey()) || e.getValue() == null || !((String)e.getValue()).matches("[0-9]{1,20}"));
        LyricsPreferences.trim(this.offsets);
        LyricsPreferences.trim(this.records);
    }

    private static boolean invalidKey(String key) {
        return key == null || key.isEmpty() || key.length() > 1400;
    }

    private static void trim(Map<?, ?> map) {
        while (map.size() > 128) {
            map.remove(map.keySet().iterator().next());
        }
    }
}

