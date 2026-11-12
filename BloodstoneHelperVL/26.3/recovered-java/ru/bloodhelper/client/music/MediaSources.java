/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.music;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public record MediaSources(List<Source> apps, String activeId) {
    public MediaSources {
        apps = List.copyOf(apps);
        activeId = MediaSources.cleanId(activeId);
    }

    public static MediaSources empty() {
        return new MediaSources(List.of(), "");
    }

    public static String cleanId(String value) {
        return value == null || value.length() > 512 || value.codePoints().anyMatch(Character::isISOControl) ? "" : value.strip();
    }

    public static String labelFor(String id) {
        String clean = MediaSources.cleanId(id);
        String lower = clean.toLowerCase(Locale.ROOT);
        if ("bloodhelper:local".equals(clean)) {
            return "Local music";
        }
        if (lower.contains("spotify")) {
            return "Spotify";
        }
        if (lower.contains("yandex") || lower.contains("\u044f\u043d\u0434\u0435\u043a\u0441")) {
            return "\u042f\u043d\u0434\u0435\u043a\u0441 \u041c\u0443\u0437\u044b\u043a\u0430";
        }
        if (lower.contains("soundcloud")) {
            return "SoundCloud";
        }
        if (lower.contains("chrome")) {
            return "Chrome";
        }
        if (lower.contains("msedge")) {
            return "Microsoft Edge";
        }
        if (lower.contains("firefox")) {
            return "Firefox";
        }
        if (lower.contains("vlc")) {
            return "VLC";
        }
        if (clean.startsWith("org.mpris.MediaPlayer2.")) {
            clean = clean.substring("org.mpris.MediaPlayer2.".length());
        }
        int slash = Math.max(clean.lastIndexOf(47), clean.lastIndexOf(92));
        String name = clean.substring(slash + 1).replaceAll("(?i)\\.exe$", "").replaceAll("[\\p{Cf}]|\u00a7.", "");
        return name.codePoints().limit(56L).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    static MediaSources read(JsonObject packet) {
        LinkedHashMap<String, Source> apps = new LinkedHashMap<String, Source>();
        JsonArray values = packet.getAsJsonArray("apps");
        if (values == null || values.size() > 64) {
            throw new IllegalArgumentException("Invalid media sources");
        }
        for (JsonElement value : values) {
            JsonObject item = value.getAsJsonObject();
            String id = MediaSources.cleanId(item.get("id").getAsString());
            if (id.isBlank()) continue;
            apps.putIfAbsent(id, new Source(id, item.has("playing") && item.get("playing").getAsBoolean()));
        }
        return new MediaSources(List.copyOf(apps.values()), packet.has("activeId") ? packet.get("activeId").getAsString() : "");
    }

    public record Source(String id, boolean playing) {
        public String label() {
            return MediaSources.labelFor(this.id);
        }
    }
}

