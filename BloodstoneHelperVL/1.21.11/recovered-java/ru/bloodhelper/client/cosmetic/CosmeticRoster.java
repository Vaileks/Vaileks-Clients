/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.cosmetic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.regex.Pattern;
import ru.bloodhelper.client.cosmetic.NicknamePalette;

public final class CosmeticRoster {
    public static final int MAX_ENTRIES = 256;
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    private static final Pattern COLOR = Pattern.compile("#[0-9A-Fa-f]{6}");
    private static final Pattern UUID_TEXT = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private final long revision;
    private final Map<UUID, Entry> byUuid;
    private final Map<String, Entry> byName;

    private CosmeticRoster(long revision, Map<UUID, Entry> byUuid, Map<String, Entry> byName) {
        this.revision = revision;
        this.byUuid = Map.copyOf(byUuid);
        this.byName = Map.copyOf(byName);
    }

    public static CosmeticRoster empty() {
        return new CosmeticRoster(0L, Map.of(), Map.of());
    }

    public long revision() {
        return this.revision;
    }

    public int size() {
        return this.byUuid.size();
    }

    public OptionalInt color(UUID uuid, String name) {
        if (!CosmeticRoster.validName(name)) {
            return OptionalInt.empty();
        }
        Entry entry = uuid == null ? this.byName.get(name.toLowerCase(Locale.ROOT)) : this.byUuid.get(uuid);
        return entry != null && entry.matches(uuid, name) ? OptionalInt.of(entry.rgb) : OptionalInt.empty();
    }

    public Optional<NicknamePalette> palette(UUID uuid, String name) {
        if (!CosmeticRoster.validName(name)) {
            return Optional.empty();
        }
        Entry entry = uuid == null ? this.byName.get(name.toLowerCase(Locale.ROOT)) : this.byUuid.get(uuid);
        return entry != null && entry.matches(uuid, name) ? Optional.of(entry.palette) : Optional.empty();
    }

    public static CosmeticRoster parse(JsonObject root) throws IOException {
        if (root == null || CosmeticRoster.number(root, "schema") != 1L) {
            throw new IOException("Unsupported cosmetic schema");
        }
        long revision = CosmeticRoster.number(root, "revision");
        if (revision < 0L || CosmeticRoster.number(root, "generatedAt") < 0L) {
            throw new IOException("Invalid cosmetic revision");
        }
        JsonElement entries = root.get("entries");
        if (entries == null || !entries.isJsonArray() || entries.getAsJsonArray().size() > 256) {
            throw new IOException("Invalid cosmetic entries");
        }
        HashMap<UUID, Entry> uuids = new HashMap<UUID, Entry>();
        HashMap<String, Entry> names = new HashMap<String, Entry>();
        HashSet<String> ambiguousNames = new HashSet<String>();
        for (JsonElement value : entries.getAsJsonArray()) {
            Entry binding;
            if (!value.isJsonObject()) {
                throw new IOException("Invalid cosmetic entry");
            }
            JsonObject entry = value.getAsJsonObject();
            UUID uuid = CosmeticRoster.canonicalUuid(CosmeticRoster.string(entry, "uuid", 36));
            String name = CosmeticRoster.string(entry, "name", 16);
            OptionalInt color = CosmeticRoster.parseColor(CosmeticRoster.string(entry, "color", 7));
            if (uuid == null || !CosmeticRoster.validName(name) || color.isEmpty()) {
                throw new IOException("Invalid cosmetic binding");
            }
            try {
                binding = new Entry(uuid, name, color.getAsInt(), NicknamePalette.parse(entry.get("gradient"), color.getAsInt()));
            }
            catch (IllegalArgumentException bad) {
                throw new IOException("Invalid nickname palette", bad);
            }
            if (uuids.putIfAbsent(uuid, binding) != null) {
                throw new IOException("Duplicate cosmetic UUID");
            }
            String key = name.toLowerCase(Locale.ROOT);
            if (names.putIfAbsent(key, binding) == null) continue;
            ambiguousNames.add(key);
        }
        ambiguousNames.forEach(names::remove);
        return new CosmeticRoster(revision, uuids, names);
    }

    public static OptionalInt parseColor(String color) {
        if (color == null || !COLOR.matcher(color).matches()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(color.substring(1), 16));
    }

    public static UUID canonicalUuid(String value) {
        if (value == null || !UUID_TEXT.matcher(value).matches()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        }
        catch (IllegalArgumentException malformed) {
            return null;
        }
    }

    public static boolean validName(String value) {
        return value != null && NAME.matcher(value).matches();
    }

    private static String string(JsonObject root, String field, int limit) throws IOException {
        JsonElement value = root.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IOException("Missing cosmetic field: " + field);
        }
        String text = value.getAsString();
        if (text.length() > limit) {
            throw new IOException("Oversized cosmetic field: " + field);
        }
        return text;
    }

    private static long number(JsonObject root, String field) throws IOException {
        JsonElement value = root.get(field);
        if (!(value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber() && value.getAsString().matches("[0-9]{1,18}"))) {
            throw new IOException("Invalid cosmetic number: " + field);
        }
        try {
            return Long.parseLong(value.getAsString());
        }
        catch (NumberFormatException malformed) {
            throw new IOException("Invalid cosmetic number: " + field);
        }
    }

    public record Entry(UUID uuid, String name, int rgb, NicknamePalette palette) {
        public Entry(UUID uuid, String name, int rgb) {
            this(uuid, name, rgb, NicknamePalette.solid(rgb));
        }

        public Entry {
            if (uuid == null || !CosmeticRoster.validName(name) || rgb < 0 || rgb > 0xFFFFFF || palette == null) {
                throw new IllegalArgumentException("Invalid cosmetic identity");
            }
        }

        public boolean matches(UUID candidateUuid, String candidateName) {
            if (!CosmeticRoster.validName(candidateName)) {
                return false;
            }
            return (candidateUuid == null || this.uuid.equals(candidateUuid)) && this.name.equalsIgnoreCase(candidateName);
        }
    }
}

