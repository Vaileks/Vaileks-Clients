/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.skins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SharedSkins {
    private static volatile Map<UUID, Entry> entries = Map.of();
    private static volatile long received;
    public static volatile String selected;

    private SharedSkins() {
    }

    public static void accept(JsonObject value) {
        JsonArray array = value.getAsJsonArray("entries");
        if (array == null || array.size() > 256) {
            throw new IllegalArgumentException("Invalid model roster");
        }
        HashMap<UUID, Entry> parsed = new HashMap<UUID, Entry>();
        for (JsonElement e : array) {
            JsonObject o = e.getAsJsonObject();
            UUID id = UUID.fromString(o.get("uuid").getAsString());
            String name = o.get("name").getAsString();
            String model = o.get("model").getAsString();
            if (name.matches("[A-Za-z0-9_]{3,16}") && Set.of("RUDY", "VERITI", "JENNY", "MIMI_TYPH").contains(model) && parsed.put(id, new Entry(name, model)) == null) continue;
            throw new IllegalArgumentException("Invalid shared model");
        }
        entries = Map.copyOf(parsed);
        received = System.nanoTime();
    }

    public static String model(UUID uuid, String name) {
        if (System.nanoTime() - received > 180000000000L) {
            return "";
        }
        Entry entry = entries.get(uuid);
        return entry != null && entry.name.equalsIgnoreCase(name) ? entry.model : "";
    }

    static {
        selected = "NONE";
    }

    private record Entry(String name, String model) {
    }
}

