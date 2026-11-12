/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package ru.bloodhelper.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.CombatHudSettings;

public final class AppearancePreset {
    public static final int MAX_BYTES = 262144;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<String> GLOBAL = List.of("accent", "themeMode", "textShadow", "background", "hudAboveChat", "hudMenuEffects", "hideHudInPauseMenu", "hudAutoReadability", "hudDetails", "backgroundColor", "backgroundAlpha", "animations", "surfaceStyle", "reglassEnabled", "reglassFullGui", "reglassChatMessages", "glassBlur", "glassRefraction", "guiScale");
    public static final List<String> WIDGETS = List.of("cooldowns", "pvp", "music", "armor", "itemCounter", "notifications", "performance", "session", "effects", "waypoints", "sizeTimer", "watermark", "reachDisplay", "comboCounter", "saturation", "generalHud.widgets.fps", "generalHud.widgets.cps", "generalHud.widgets.ram", "generalHud.widgets.compass", "generalHud.widgets.playtime", "generalHud.widgets.days", "generalHud.widgets.keys", "generalHud.widgets.speed");
    private final JsonObject appearance;

    private AppearancePreset(JsonObject appearance) {
        this.appearance = appearance.deepCopy();
    }

    public static AppearancePreset capture(ClientConfig config) {
        return new AppearancePreset(AppearancePreset.project(GSON.toJsonTree((Object)config).getAsJsonObject()));
    }

    public String encode() {
        JsonObject document = new JsonObject();
        document.addProperty("format", "bloodhelper-appearance");
        document.addProperty("version", (Number)1);
        document.add("appearance", (JsonElement)this.appearance.deepCopy());
        return GSON.toJson((JsonElement)document);
    }

    public static AppearancePreset read(Path path) throws IOException {
        if (!Files.isRegularFile(path, new LinkOption[0])) {
            throw new IOException("Not a regular file");
        }
        try (InputStream input = Files.newInputStream(path, new OpenOption[0]);){
            byte[] bytes = input.readNBytes(262145);
            if (bytes.length > 262144) {
                throw new IOException("Appearance file exceeds 256 KB");
            }
            AppearancePreset appearancePreset = AppearancePreset.decode(new String(bytes, StandardCharsets.UTF_8));
            return appearancePreset;
        }
    }

    public static AppearancePreset decode(String json) throws IOException {
        if (json == null || json.getBytes(StandardCharsets.UTF_8).length > 262144) {
            throw new IOException("Appearance file too large");
        }
        AppearancePreset.checkDepth(json);
        try {
            JsonObject root = JsonParser.parseString((String)json).getAsJsonObject();
            if (!(root.has("format") && "bloodhelper-appearance".equals(root.get("format").getAsString()) && root.has("version") && "1".equals(root.get("version").getAsString()))) {
                throw new IOException("Unsupported appearance format");
            }
            JsonObject data = root.getAsJsonObject("appearance");
            if (data == null) {
                throw new IOException("Missing appearance");
            }
            AppearancePreset result = new AppearancePreset(AppearancePreset.project(data));
            if (result.appearance.size() == 0) {
                throw new IOException("Empty appearance");
            }
            result.preview(new ClientConfig());
            return result;
        }
        catch (RuntimeException failure) {
            throw new IOException("Invalid appearance file", failure);
        }
    }

    public ClientConfig preview(ClientConfig current) {
        JsonObject merged = GSON.toJsonTree((Object)current).getAsJsonObject();
        AppearancePreset.merge(merged, this.appearance);
        ClientConfig result = (ClientConfig)GSON.fromJson((JsonElement)merged, ClientConfig.class);
        result.sanitize();
        return result;
    }

    private static JsonObject project(JsonObject source) {
        JsonObject result = new JsonObject();
        for (String field : GLOBAL) {
            AppearancePreset.copy(source, result, field);
        }
        JsonObject titles = AppearancePreset.objectAt(source, "hudTitles");
        if (titles != null) {
            if (titles.size() > 256) {
                throw new IllegalArgumentException("Too many HUD titles");
            }
            JsonObject safe = new JsonObject();
            for (Object entry : titles.entrySet()) {
                if (((String)entry.getKey()).length() > 96 || !((JsonElement)entry.getValue()).isJsonPrimitive() || !((JsonElement)entry.getValue()).getAsJsonPrimitive().isBoolean()) {
                    throw new IllegalArgumentException("Invalid HUD title");
                }
                safe.add((String)entry.getKey(), ((JsonElement)entry.getValue()).deepCopy());
            }
            result.add("hudTitles", (JsonElement)safe);
        }
        for (String widget : WIDGETS) {
            for (String field : List.of("position.x", "position.y", "position.scale", "alignment", "bottom", "background", "shadow")) {
                AppearancePreset.copy(source, result, widget + "." + field);
            }
        }
        for (String path : List.of("music.compact", "music.showCover", "music.lyricAnimation", "music.lyricColor", "music.lyricSize", "music.lyricGlow", "music.lyricShadow", "music.lyricDistance", "music.lyricSpread", "legendaryVisuals.labelScale", "legendaryVisuals.lootStyle", "legendaryVisuals.katanaColor", "legendaryVisuals.maceColor", "legendaryVisuals.orbitalColor", "legendaryVisuals.staffColor", "legendaryVisuals.sculkColor", "legendaryVisuals.shadowColor", "legendaryVisuals.timeBowColor", "legendaryVisuals.legendaryColor", "legendaryVisuals.totemColor", "legendaryVisuals.appleColor", "generalHud.keysStyle", "cooldowns.layout", "armor.layout", "armor.orientation", "itemCounter.layout", "itemCounter.orientation", "watermark.layout", "performance.layout")) {
            AppearancePreset.copy(source, result, path);
        }
        for (String key : List.of("armor", "itemCounter", "watermark", "performance")) {
            JsonObject parts = AppearancePreset.objectAt(source, key + ".parts");
            if (parts == null) continue;
            if (parts.size() > 256) {
                throw new IllegalArgumentException("Too many HUD parts");
            }
            JsonObject safe = new JsonObject();
            for (Map.Entry entry : parts.entrySet()) {
                boolean valid;
                switch (key) {
                    case "armor": {
                        boolean bl = List.of("head", "chest", "legs", "feet").contains(entry.getKey());
                        break;
                    }
                    case "watermark": {
                        boolean bl = List.of("brand", "fps", "ping", "server", "coordinates", "clock", "session", "online").contains(entry.getKey());
                        break;
                    }
                    case "performance": {
                        boolean bl = List.of("fps", "ping", "frame", "graph").contains(entry.getKey());
                        break;
                    }
                    default: {
                        boolean bl = valid = ((String)entry.getKey()).matches("[a-z0-9_.-]+:[a-z0-9/._-]{1,128}");
                    }
                }
                if (!valid) continue;
                JsonObject position = new JsonObject();
                for (String field : List.of("x", "y", "scale")) {
                    AppearancePreset.copy(((JsonElement)entry.getValue()).getAsJsonObject(), position, field);
                }
                safe.add((String)entry.getKey(), (JsonElement)position);
            }
            AppearancePreset.put(result, key + ".parts", (JsonElement)safe);
        }
        JsonObject combat = AppearancePreset.objectAt(source, "combatHud.layout");
        if (combat != null) {
            if (combat.size() > 288) {
                throw new IllegalArgumentException("Too many HUD positions");
            }
            JsonObject safe = new JsonObject();
            for (Map.Entry entry : combat.entrySet()) {
                if (!CombatHudSettings.validKey((String)entry.getKey())) continue;
                JsonObject pose = new JsonObject();
                for (String field : List.of("position.x", "position.y", "position.scale", "alignment", "bottom")) {
                    AppearancePreset.copy(((JsonElement)entry.getValue()).getAsJsonObject(), pose, field);
                }
                safe.add((String)entry.getKey(), (JsonElement)pose);
            }
            AppearancePreset.put(result, "combatHud.layout", (JsonElement)safe);
        }
        return result;
    }

    public static JsonObject previewWidget(JsonObject config, String path, boolean combat) {
        if (combat) {
            JsonObject layout = AppearancePreset.objectAt(config, "combatHud.layout");
            String hudId = path.replace("generalHud.widgets.", "generalHud.");
            if (layout != null && layout.has(hudId) && layout.get(hudId).isJsonObject()) {
                return layout.getAsJsonObject(hudId);
            }
        }
        return AppearancePreset.objectAt(config, path);
    }

    private static JsonObject objectAt(JsonObject source, String path) {
        JsonElement value = AppearancePreset.at(source, path);
        return value == null ? null : value.getAsJsonObject();
    }

    private static JsonElement at(JsonObject source, String path) {
        JsonObject current = source;
        for (String part : path.split("\\.")) {
            if (current == null) {
                return null;
            }
            current = current.getAsJsonObject().get(part);
        }
        return current;
    }

    private static void copy(JsonObject source, JsonObject target, String path) {
        JsonElement value = AppearancePreset.at(source, path);
        if (value == null) {
            return;
        }
        if (!value.isJsonPrimitive()) {
            throw new IllegalArgumentException("Expected a visual setting");
        }
        AppearancePreset.put(target, path, value.deepCopy());
    }

    private static void put(JsonObject target, String path, JsonElement value) {
        String[] parts = path.split("\\.");
        for (int i = 0; i < parts.length - 1; ++i) {
            if (!target.has(parts[i])) {
                target.add(parts[i], (JsonElement)new JsonObject());
            }
            target = target.getAsJsonObject(parts[i]);
        }
        target.add(parts[parts.length - 1], value);
    }

    private static void merge(JsonObject target, JsonObject overlay) {
        for (Map.Entry entry : overlay.entrySet()) {
            if (((JsonElement)entry.getValue()).isJsonObject() && target.has((String)entry.getKey()) && target.get((String)entry.getKey()).isJsonObject()) {
                AppearancePreset.merge(target.getAsJsonObject((String)entry.getKey()), ((JsonElement)entry.getValue()).getAsJsonObject());
                continue;
            }
            target.add((String)entry.getKey(), ((JsonElement)entry.getValue()).deepCopy());
        }
    }

    private static void checkDepth(String text) throws IOException {
        boolean quoted = false;
        boolean escaped = false;
        int depth = 0;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (quoted && c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '\"') {
                quoted = !quoted;
                continue;
            }
            if (!(quoted || c != '{' && c != '[' || ++depth <= 20)) {
                throw new IOException("Appearance nesting too deep");
            }
            if (quoted || c != '}' && c != ']') continue;
            --depth;
        }
    }
}

