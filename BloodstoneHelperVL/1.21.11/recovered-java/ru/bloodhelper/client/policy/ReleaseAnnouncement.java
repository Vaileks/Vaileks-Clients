/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.policy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record ReleaseAnnouncement(boolean enabled, String version) {
    public static final String DISCORD = "https://discord.gg/QWcgKf9Md7";
    public static final ReleaseAnnouncement NONE = new ReleaseAnnouncement(false, "");

    public ReleaseAnnouncement {
        if (version == null || (!version.isEmpty() || enabled) && !version.matches("[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}")) {
            throw new IllegalArgumentException("Invalid release version");
        }
    }

    public boolean visibleFor(String installed) {
        if (!this.enabled || installed == null) {
            return false;
        }
        String base = installed.split("\\+", 2)[0].replaceFirst("-hotfix$", "");
        if (!base.matches("[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}")) {
            return false;
        }
        String[] next = this.version.split("\\.");
        String[] current = base.split("\\.");
        for (int i = 0; i < 3; ++i) {
            int compare = Integer.compare(Integer.parseInt(next[i]), Integer.parseInt(current[i]));
            if (compare == 0) continue;
            return compare > 0;
        }
        return false;
    }

    public static ReleaseAnnouncement parse(JsonElement value) {
        if (value == null) {
            return NONE;
        }
        if (!value.isJsonObject()) {
            throw new IllegalArgumentException("Invalid release announcement");
        }
        JsonObject json = value.getAsJsonObject();
        JsonElement enabled = json.get("enabled");
        JsonElement version = json.get("version");
        if (!(enabled != null && enabled.isJsonPrimitive() && enabled.getAsJsonPrimitive().isBoolean() && version != null && version.isJsonPrimitive() && version.getAsJsonPrimitive().isString())) {
            throw new IllegalArgumentException("Invalid release announcement");
        }
        return new ReleaseAnnouncement(enabled.getAsBoolean(), version.getAsString());
    }

    public JsonObject json() {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", Boolean.valueOf(this.enabled));
        json.addProperty("version", this.version);
        return json;
    }
}

