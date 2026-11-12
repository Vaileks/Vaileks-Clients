/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class QuickCommandsSettings {
    public static final int SLOT_COUNT = 4;
    public boolean enabled = false;
    public int slotsVersion;
    public Slot[] slots = QuickCommandsSettings.emptySlots();

    private static Slot[] emptySlots() {
        return new Slot[]{new Slot(), new Slot(), new Slot(), new Slot()};
    }

    public void sanitize() {
        Slot[] normalized = QuickCommandsSettings.emptySlots();
        if (this.slots != null) {
            for (int i = 0; i < Math.min(4, this.slots.length); ++i) {
                if (this.slots[i] == null) continue;
                String label = QuickCommandsSettings.clean(this.slots[i].label, 28);
                String value = QuickCommandsSettings.clean(this.slots[i].value, 256);
                if (this.slotsVersion < 1 && value.isEmpty() && !label.isEmpty()) {
                    value = label;
                }
                normalized[i] = new Slot(label, value);
            }
        }
        this.slots = normalized;
        this.slotsVersion = 1;
    }

    public static String clean(String value, int maximum) {
        if (value == null || maximum <= 0) {
            return "";
        }
        StringBuilder result = new StringBuilder(Math.min(maximum, value.length()));
        int offset = 0;
        while (offset < value.length()) {
            int cp = value.codePointAt(offset);
            offset += Character.charCount(cp);
            if (cp == 167 || cp >= 55296 && cp <= 57343) continue;
            if (Character.isISOControl(cp) || Character.getType(cp) == 13 || Character.getType(cp) == 14) {
                cp = 32;
            }
            if (result.length() + Character.charCount(cp) > maximum) break;
            result.appendCodePoint(cp);
        }
        return result.toString().strip();
    }

    public static final class Slot {
        public String label = "";
        public String value = "";

        public Slot() {
        }

        public Slot(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }
}

