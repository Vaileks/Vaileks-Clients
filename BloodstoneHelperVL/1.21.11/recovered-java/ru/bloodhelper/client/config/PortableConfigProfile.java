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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import ru.bloodhelper.client.config.ClientConfig;

public final class PortableConfigProfile {
    public static final int MAX_BYTES = 524288;
    public static final String FORMAT = "bloodhelper-config";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<String> FIELDS = List.of("schemaVersion", "optimizer", "betterNameVisibility", "language", "accent", "themeMode", "hudEditor", "renderQuality", "textShadow", "background", "hudAboveChat", "hudMenuEffects", "hudAutoReadability", "inventorySearch", "screenshotAutoCopy", "screenshotActions", "hudDetails", "hudTitles", "backgroundColor", "backgroundAlpha", "animations", "surfaceStyle", "glassBlur", "glassRefraction", "guiScale", "menuKey", "cooldowns", "opponentCooldowns", "legendarySounds", "legendarySize", "legendaryVisuals", "resourcePack", "pvp", "music", "armor", "itemCounter", "notifications", "performance", "session", "effects", "deathRecap", "chatTools", "abilityGuide", "quickCommands", "waypoints", "sizeTimer", "streamer", "zoom", "deathScreenshot", "chatMentions", "cooldownReady", "combatTimeline", "combatHud", "musicDucking", "dropGuard", "slotLocks", "shulkerPreview", "chatDrafts", "coordinateLinks", "backgroundAlerts", "discordPresence", "crosshair", "watermark", "aspectRatio", "motionBlur", "inventoryProfiles", "inventoryUi", "skins");
    private static final List<String> LOCAL = List.of("voice", "serverSafeMode", "aiAssistant", "waypoints.entries", "music.preferredSource", "music.lyricAdjustments", "quickCommands.slots", "quickCommands.slotsVersion", "discordPresence.customText");
    private final String name;
    private final JsonObject settings;

    private PortableConfigProfile(String name, JsonObject settings) {
        this.name = PortableConfigProfile.validName(name);
        this.settings = PortableConfigProfile.project(settings);
    }

    public String name() {
        return this.name;
    }

    public PortableConfigProfile named(String name) {
        return new PortableConfigProfile(name, this.settings);
    }

    public static PortableConfigProfile capture(String name, ClientConfig config) {
        return new PortableConfigProfile(name, GSON.toJsonTree((Object)config).getAsJsonObject());
    }

    public String encode() {
        JsonObject root = new JsonObject();
        root.addProperty("format", FORMAT);
        root.addProperty("version", (Number)1);
        root.addProperty("name", this.name);
        root.add("settings", (JsonElement)this.settings.deepCopy());
        return GSON.toJson((JsonElement)root);
    }

    public static PortableConfigProfile read(Path path) throws IOException {
        return PortableConfigProfile.decode(PortableConfigProfile.readText(path));
    }

    static PortableConfigProfile readStored(Path path, String name) throws IOException {
        String text = PortableConfigProfile.readText(path);
        try {
            JsonObject root = PortableConfigProfile.parse(text);
            if (root.has("format")) {
                return PortableConfigProfile.decode(text).named(name);
            }
            if (!root.has("schemaVersion")) {
                throw new IOException("Unknown local profile format");
            }
            return PortableConfigProfile.validated(name, root);
        }
        catch (RuntimeException error) {
            throw new IOException("Invalid profile", error);
        }
    }

    public static PortableConfigProfile decode(String text) throws IOException {
        try {
            JsonObject root = PortableConfigProfile.parse(text);
            if (!(root.has("format") && FORMAT.equals(root.get("format").getAsString()) && root.has("version") && "1".equals(root.get("version").getAsString()))) {
                throw new IOException("Unsupported config format or version");
            }
            if (!(root.has("name") && root.has("settings") && root.get("settings").isJsonObject())) {
                throw new IOException("Missing profile name or settings");
            }
            return PortableConfigProfile.validated(root.get("name").getAsString(), root.getAsJsonObject("settings"));
        }
        catch (RuntimeException error) {
            throw new IOException("Invalid config file", error);
        }
    }

    private static PortableConfigProfile validated(String name, JsonObject data) throws IOException {
        PortableConfigProfile.validate((JsonElement)PortableConfigProfile.project(data), ClientConfig.class);
        if (data.has("schemaVersion") && data.get("schemaVersion").getAsInt() > new ClientConfig().schemaVersion) {
            throw new IOException("Config needs a newer BloodHelper version");
        }
        PortableConfigProfile profile = new PortableConfigProfile(name, data);
        if (profile.settings.size() == 0) {
            throw new IOException("Empty config");
        }
        ClientConfig candidate = profile.preview(new ClientConfig());
        return PortableConfigProfile.capture(name, candidate);
    }

    public ClientConfig preview(ClientConfig local) {
        JsonObject target = GSON.toJsonTree((Object)new ClientConfig()).getAsJsonObject();
        for (Object entry : this.settings.entrySet()) {
            target.add((String)entry.getKey(), ((JsonElement)entry.getValue()).deepCopy());
        }
        JsonObject personal = GSON.toJsonTree((Object)local).getAsJsonObject();
        for (String path : LOCAL) {
            JsonElement value = PortableConfigProfile.at(personal, path);
            if (value == null) continue;
            PortableConfigProfile.put(target, path, value.deepCopy());
        }
        ClientConfig candidate = (ClientConfig)GSON.fromJson((JsonElement)target, ClientConfig.class);
        candidate.sanitize();
        return candidate;
    }

    public static String validName(String name) {
        if (name == null || !name.matches("[\\p{L}\\p{N}_ -]{1,40}") || name.isBlank()) {
            throw new IllegalArgumentException("Use 1\u201340 letters, digits, spaces, _ or -");
        }
        String clean = name.strip();
        if (clean.toUpperCase(Locale.ROOT).matches("CON|PRN|AUX|NUL|COM[0-9]|LPT[0-9]")) {
            throw new IllegalArgumentException("Reserved file name");
        }
        return clean;
    }

    private static void validate(JsonElement value, Type type) {
        block18: {
            Class cls;
            block21: {
                block20: {
                    block19: {
                        block17: {
                            if (value == null || value.isJsonNull()) {
                                throw new IllegalArgumentException("Null setting");
                            }
                            if (type instanceof ParameterizedType) {
                                ParameterizedType parameterized = (ParameterizedType)type;
                                Class raw = (Class)parameterized.getRawType();
                                if (Map.class.isAssignableFrom(raw)) {
                                    if (!value.isJsonObject() || value.getAsJsonObject().size() > 4096) {
                                        throw new IllegalArgumentException("Invalid map");
                                    }
                                    for (Map.Entry entry : value.getAsJsonObject().entrySet()) {
                                        PortableConfigProfile.validate((JsonElement)entry.getValue(), parameterized.getActualTypeArguments()[1]);
                                    }
                                    return;
                                }
                                if (Collection.class.isAssignableFrom(raw)) {
                                    if (!value.isJsonArray() || value.getAsJsonArray().size() > 4096) {
                                        throw new IllegalArgumentException("Invalid list");
                                    }
                                    for (JsonElement entry : value.getAsJsonArray()) {
                                        PortableConfigProfile.validate(entry, parameterized.getActualTypeArguments()[0]);
                                    }
                                    return;
                                }
                            }
                            if (!(type instanceof Class)) {
                                throw new IllegalArgumentException("Unknown setting type");
                            }
                            cls = (Class)type;
                            if (!cls.isArray()) break block17;
                            if (!value.isJsonArray() || value.getAsJsonArray().size() > 4096) {
                                throw new IllegalArgumentException("Invalid array");
                            }
                            for (JsonElement entry : value.getAsJsonArray()) {
                                PortableConfigProfile.validate(entry, cls.getComponentType());
                            }
                            break block18;
                        }
                        if (cls != String.class) break block19;
                        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                            throw new IllegalArgumentException("Expected text");
                        }
                        break block18;
                    }
                    if (cls != Boolean.TYPE && cls != Boolean.class) break block20;
                    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
                        throw new IllegalArgumentException("Expected boolean");
                    }
                    break block18;
                }
                if (!cls.isPrimitive() && !Number.class.isAssignableFrom(cls)) break block21;
                if (!(value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber() && Double.isFinite(value.getAsDouble()))) {
                    throw new IllegalArgumentException("Expected finite number");
                }
                if (cls == Integer.TYPE || cls == Integer.class) {
                    value.getAsBigDecimal().intValueExact();
                }
                if (cls != Long.TYPE && cls != Long.class) break block18;
                value.getAsBigDecimal().longValueExact();
                break block18;
            }
            if (!value.isJsonObject()) {
                throw new IllegalArgumentException("Expected settings object");
            }
            JsonObject object = value.getAsJsonObject();
            for (Field field : cls.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) || !object.has(field.getName())) continue;
                PortableConfigProfile.validate(object.get(field.getName()), field.getGenericType());
            }
        }
    }

    private static JsonObject project(JsonObject source) {
        JsonObject result = new JsonObject();
        for (String field : FIELDS) {
            if (!source.has(field)) continue;
            result.add(field, source.get(field).deepCopy());
        }
        for (String path : LOCAL) {
            PortableConfigProfile.remove(result, path);
        }
        return result;
    }

    private static JsonElement at(JsonObject root, String path) {
        JsonObject current = root;
        for (String key : path.split("\\.")) {
            if (current == null || !current.isJsonObject()) {
                return null;
            }
            current = current.getAsJsonObject().get(key);
        }
        return current;
    }

    private static void put(JsonObject root, String path, JsonElement value) {
        String[] keys = path.split("\\.");
        for (int i = 0; i < keys.length - 1; ++i) {
            if (!root.has(keys[i]) || !root.get(keys[i]).isJsonObject()) {
                root.add(keys[i], (JsonElement)new JsonObject());
            }
            root = root.getAsJsonObject(keys[i]);
        }
        root.add(keys[keys.length - 1], value);
    }

    private static void remove(JsonObject root, String path) {
        String[] keys = path.split("\\.");
        for (int i = 0; i < keys.length - 1; ++i) {
            if (!root.has(keys[i]) || !root.get(keys[i]).isJsonObject()) {
                return;
            }
            root = root.getAsJsonObject(keys[i]);
        }
        root.remove(keys[keys.length - 1]);
    }

    private static String readText(Path path) throws IOException {
        if (!Files.isRegularFile(path, new LinkOption[0])) {
            throw new IOException("Not a regular file");
        }
        try (InputStream input = Files.newInputStream(path, new OpenOption[0]);){
            byte[] bytes = input.readNBytes(524289);
            if (bytes.length > 524288) {
                throw new IOException("Config exceeds 512 KB");
            }
            String string = new String(bytes, StandardCharsets.UTF_8);
            return string;
        }
    }

    private static JsonObject parse(String text) throws IOException {
        if (text == null || text.getBytes(StandardCharsets.UTF_8).length > 524288) {
            throw new IOException("Config exceeds 512 KB");
        }
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
            if (!(quoted || c != '{' && c != '[' || ++depth <= 24)) {
                throw new IOException("Config nesting too deep");
            }
            if (quoted || c != '}' && c != ']') continue;
            --depth;
        }
        return JsonParser.parseString((String)text).getAsJsonObject();
    }
}

