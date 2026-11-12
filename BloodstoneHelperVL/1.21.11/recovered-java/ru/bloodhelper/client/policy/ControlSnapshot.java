/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.policy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import ru.bloodhelper.client.cosmetic.NicknamePalette;
import ru.bloodhelper.client.policy.ModulePolicy;
import ru.bloodhelper.client.policy.ReleaseAnnouncement;

public record ControlSnapshot(long revision, String publicUid, boolean clientDisabled, Set<String> globalDisabledModules, Set<String> deviceDisabledModules, boolean gold, boolean purple, String nicknameColor, String minecraftUuid, String minecraftName, long generatedAt, NicknamePalette nicknameGradient) {
    public ControlSnapshot(long revision, String uid, boolean disabled, Set<String> global, Set<String> device, boolean gold, boolean purple, String color, String uuid, String name, long at) {
        this(revision, uid, disabled, global, device, gold, purple, color, uuid, name, at, null);
    }

    public ControlSnapshot {
        globalDisabledModules = Set.copyOf(globalDisabledModules);
        deviceDisabledModules = Set.copyOf(deviceDisabledModules);
    }

    public static ControlSnapshot empty() {
        return new ControlSnapshot(0L, "", false, Set.of(), Set.of(), false, false, null, null, null, 0L);
    }

    public static ControlSnapshot parse(JsonObject json) {
        ControlSnapshot.schema(json);
        long revision = ControlSnapshot.number(json, "revision", 1L, Long.MAX_VALUE);
        String uid = ControlSnapshot.string(json, "publicUid", 12, false);
        if (!uid.matches("[1-9][0-9]{0,11}")) {
            throw new IllegalArgumentException("Invalid public UID");
        }
        Set<String> global = ControlSnapshot.modules(json, "globalDisabledModules");
        Set<String> device = ControlSnapshot.modules(json, "deviceDisabledModules");
        HashSet<String> union = new HashSet<String>(global);
        union.addAll(device);
        if (!union.equals(ControlSnapshot.modules(json, "disabledModules"))) {
            throw new IllegalArgumentException("Inconsistent restrictions");
        }
        JsonObject access = json.getAsJsonObject("earlyAccess");
        String color = ControlSnapshot.string(json, "nicknameColor", 7, true);
        String uuid = ControlSnapshot.string(json, "minecraftUuid", 36, true);
        String name = ControlSnapshot.string(json, "minecraftName", 16, true);
        if (color != null && !color.matches("#[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException("Invalid color");
        }
        if (uuid != null && !uuid.matches("[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}")) {
            throw new IllegalArgumentException("Invalid UUID");
        }
        if (name != null && !name.matches("[A-Za-z0-9_]{1,16}")) {
            throw new IllegalArgumentException("Invalid Minecraft name");
        }
        return new ControlSnapshot(revision, uid, ControlSnapshot.bool(json, "clientDisabled"), global, device, ControlSnapshot.bool(access, "gold"), ControlSnapshot.bool(access, "purple"), color, uuid, name, ControlSnapshot.number(json, "generatedAt", 0L, 10000000000L), color == null ? null : NicknamePalette.parse(json.get("nicknameGradient"), Integer.parseInt(color.substring(1), 16)));
    }

    public JsonObject json() {
        JsonObject json = new JsonObject();
        json.addProperty("schema", (Number)1);
        json.addProperty("revision", (Number)this.revision);
        json.addProperty("publicUid", this.publicUid);
        json.addProperty("clientDisabled", Boolean.valueOf(this.clientDisabled));
        json.add("globalDisabledModules", (JsonElement)ControlSnapshot.array(this.globalDisabledModules));
        json.add("deviceDisabledModules", (JsonElement)ControlSnapshot.array(this.deviceDisabledModules));
        HashSet<String> union = new HashSet<String>(this.globalDisabledModules);
        union.addAll(this.deviceDisabledModules);
        json.add("disabledModules", (JsonElement)ControlSnapshot.array(union));
        JsonObject access = new JsonObject();
        access.addProperty("gold", Boolean.valueOf(this.gold));
        access.addProperty("purple", Boolean.valueOf(this.purple));
        json.add("earlyAccess", (JsonElement)access);
        json.addProperty("nicknameColor", this.nicknameColor);
        json.addProperty("minecraftUuid", this.minecraftUuid);
        json.addProperty("minecraftName", this.minecraftName);
        json.add("nicknameGradient", (JsonElement)(this.nicknameGradient == null ? JsonNull.INSTANCE : this.nicknameGradient.json()));
        json.addProperty("generatedAt", (Number)this.generatedAt);
        return json;
    }

    private static JsonArray array(Set<String> values) {
        JsonArray array = new JsonArray();
        values.stream().sorted().forEach(arg_0 -> ((JsonArray)array).add(arg_0));
        return array;
    }

    private static void schema(JsonObject json) {
        if (ControlSnapshot.number(json, "schema", 1L, 1L) != 1L) {
            throw new IllegalArgumentException("Unknown control schema");
        }
    }

    private static Set<String> modules(JsonObject json, String key) {
        if (json == null || !json.has(key) || !json.get(key).isJsonArray()) {
            throw new IllegalArgumentException("Missing modules");
        }
        JsonArray array = json.getAsJsonArray(key);
        if (array.size() > ModulePolicy.POLICY_MODULES.size()) {
            throw new IllegalArgumentException("Too many restrictions");
        }
        HashSet<String> result = new HashSet<String>();
        for (JsonElement value : array) {
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() && ModulePolicy.POLICY_MODULES.contains(value.getAsString()) && result.add(value.getAsString())) continue;
            throw new IllegalArgumentException("Unknown or duplicate module");
        }
        return Set.copyOf(result);
    }

    private static boolean bool(JsonObject json, String key) {
        if (!(json != null && json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isBoolean())) {
            throw new IllegalArgumentException("Invalid boolean");
        }
        return json.get(key).getAsBoolean();
    }

    private static long number(JsonObject json, String key, long min, long max) {
        if (!(json != null && json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isNumber() && json.get(key).getAsString().matches("[0-9]{1,19}"))) {
            throw new IllegalArgumentException("Invalid number");
        }
        long result = Long.parseLong(json.get(key).getAsString());
        if (result < min || result > max) {
            throw new IllegalArgumentException("Invalid number");
        }
        return result;
    }

    private static String string(JsonObject json, String key, int max, boolean nullable) {
        if (json == null || !json.has(key)) {
            throw new IllegalArgumentException("Missing string");
        }
        JsonElement value = json.get(key);
        if (nullable && value.isJsonNull()) {
            return null;
        }
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString() || value.getAsString().length() > max) {
            throw new IllegalArgumentException("Invalid string");
        }
        return value.getAsString();
    }

    public record Global(long revision, Set<String> disabledModules, long generatedAt, ReleaseAnnouncement announcement) {
        public Global(long revision, Set<String> modules, long at) {
            this(revision, modules, at, ReleaseAnnouncement.NONE);
        }

        public Global {
            disabledModules = Set.copyOf(disabledModules);
            Objects.requireNonNull(announcement);
        }

        public static Global empty() {
            return new Global(0L, Set.of(), 0L);
        }

        public static Global parse(JsonObject json) {
            ControlSnapshot.schema(json);
            return new Global(ControlSnapshot.number(json, "revision", 1L, Long.MAX_VALUE), ControlSnapshot.modules(json, "disabledModules"), ControlSnapshot.number(json, "generatedAt", 0L, 10000000000L), ReleaseAnnouncement.parse(json.get("announcement")));
        }

        public JsonObject json() {
            JsonObject json = new JsonObject();
            json.addProperty("schema", (Number)1);
            json.addProperty("revision", (Number)this.revision);
            json.add("disabledModules", (JsonElement)ControlSnapshot.array(this.disabledModules));
            json.addProperty("generatedAt", (Number)this.generatedAt);
            json.add("announcement", (JsonElement)this.announcement.json());
            return json;
        }
    }
}

