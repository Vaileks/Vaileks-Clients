/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.nio.charset.StandardCharsets;

public final class DiscordPresenceSettings {
    public boolean enabled = true;
    public boolean showNickname = true;
    public boolean showServerIcon = true;
    public String activityMode = "SERVER";
    public String customText = "";
    public boolean showTrack = false;
    public boolean showElapsed = true;

    public void sanitize() {
        this.activityMode = "CUSTOM".equals(this.activityMode) ? "CUSTOM" : "SERVER";
        this.customText = DiscordPresenceSettings.text(this.customText, 128);
    }

    public static String text(String value, int limit) {
        if (value == null || limit <= 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        int bytes = 0;
        int i = 0;
        while (i < value.length()) {
            int cp = value.codePointAt(i);
            if (cp == 167 && (i += Character.charCount(cp)) < value.length()) {
                i += Character.charCount(value.codePointAt(i));
                continue;
            }
            if (Character.isISOControl(cp) || Character.getType(cp) == 16 || Character.getType(cp) == 19) continue;
            String part = new String(Character.toChars(cp));
            int size = part.getBytes(StandardCharsets.UTF_8).length;
            if (bytes + size > limit) break;
            result.append(part);
            bytes += size;
        }
        return result.toString().strip();
    }
}

