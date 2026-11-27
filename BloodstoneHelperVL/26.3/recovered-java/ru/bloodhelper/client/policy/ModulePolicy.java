/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package ru.bloodhelper.client.policy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import ru.bloodhelper.client.policy.ServerModuleRule;

public record ModulePolicy(long revision, boolean safeMode, Map<String, Boolean> modules, Map<String, String> updates, Map<String, ServerModuleRule> serverRules) {
    public static final Set<String> BASE_MODULES = Set.of("aiAssistant", "aspectRatio", "motionBlur", "betterNameVisibility", "optimizer", "legendaryVisuals", "sizeTimer", "inventorySearch", "screenshotActions", "combatHud", "cooldowns", "music", "armor", "itemCounter", "notifications", "performance", "session", "deathRecap", "chatTools", "effects", "opponentCooldowns", "visualHat", "legendarySize", "legendarySounds", "resourcePack", "abilityGuide", "quickCommands", "waypoints", "streamer", "zoom", "deathScreenshot", "chatMentions", "cooldownReady", "musicDucking", "dropGuard", "slotLocks", "shulkerPreview", "chatDrafts", "coordinateLinks", "backgroundAlerts", "discordPresence", "watermark");
    public static final Set<String> EXTENSION_MODULES = Set.of("loginGuard", "bloodstoneCircles", "heads", "simpleVoice", "plasmoVoice", "sanctions", "staffRulebook", "staffChat", "staffChecks", "hotbarMarkers", "hotbarKeys", "totemAnimation", "scoreboardControl", "healthDisplay", "titlesControl", "actionbarControl", "chatCalculator", "timeChanger", "weatherChanger", "fullBright", "ambience", "staffJournal", "staffShift", "playerReminders", "playerReturns", "observationHistory", "staffStatus", "crosshair", "generalHud", "reachDisplay", "comboCounter", "saturation", "hitboxes", "freeLook", "otherTiers", "inventoryProfiles", "skins", "spectatorTimer", "staffStats", "chatViolations", "nicknameCopy", "commandLayout");
    public static final Set<String> POLICY_MODULES;
    public static final Set<String> MODULES;
    public static final Set<String> SAFE_BLOCKS;
    public static final Set<String> GAME_VERSIONS;
    public static final int MAX_BYTES = 32768;

    public ModulePolicy {
        modules = Map.copyOf(modules);
        updates = Map.copyOf(updates);
        serverRules = Map.copyOf(serverRules);
    }

    public ModulePolicy(long revision, boolean safeMode, Map<String, Boolean> modules, Map<String, String> updates) {
        this(revision, safeMode, modules, updates, Map.of());
    }

    public static String settingPath(String id) {
        return switch (id) {
            case "loginGuard" -> "loginGuard";
            case "sanctions" -> "sanctions.enabled";
            case "staffRulebook" -> "staffRulebook.enabled";
            case "staffChat" -> "staffChat.enabled";
            case "staffChecks" -> "staffChecks.enabled";
            case "hotbarMarkers" -> "hudItemControls.hotbar.markersEnabled";
            case "hotbarKeys" -> "hudItemControls.hotbar.keysEnabled";
            case "totemAnimation" -> "hudItemControls.totem.enabled";
            case "scoreboardControl" -> "hudItemControls.scoreboard.enabled";
            case "healthDisplay" -> "hudItemControls.health.enabled";
            case "titlesControl" -> "hudItemControls.titles.enabled";
            case "actionbarControl" -> "hudItemControls.actionbar.enabled";
            case "chatCalculator" -> "chatCalculator.enabled";
            case "timeChanger" -> "worldVisuals.timeEnabled";
            case "weatherChanger" -> "worldVisuals.weatherEnabled";
            case "fullBright" -> "worldVisuals.fullBrightEnabled";
            case "ambience" -> "worldVisuals.ambienceEnabled";
            case "staffJournal" -> "staffWorkspace.journal";
            case "staffShift" -> "staffWorkspace.shifts";
            case "playerReminders" -> "staffWorkspace.reminders";
            case "playerReturns" -> "staffWorkspace.returns";
            case "observationHistory" -> "staffWorkspace.observations";
            case "staffStatus" -> "staffWorkspace.status";
            default -> id;
        };
    }

    public boolean allows(String id, boolean localSafeMode) {
        return MODULES.contains(id) && this.modules.getOrDefault(id, EXTENSION_MODULES.contains(id)) != false && (!this.safeMode && !localSafeMode || !SAFE_BLOCKS.contains(id));
    }

    public boolean allows(String id, boolean localSafeMode, String serverAddress) {
        return this.allows(id, localSafeMode) && ServerModuleRule.allows(this.serverRules, id, serverAddress);
    }

    public static ModulePolicy parse(String json) {
        if (json == null || json.length() > 32768) {
            throw new IllegalArgumentException("Policy too large");
        }
        JsonObject root = JsonParser.parseString((String)json).getAsJsonObject();
        long schema = ModulePolicy.integer(root, "schemaVersion");
        if (!root.keySet().equals(Set.of("schemaVersion", "revision", "safeMode", "modules", "updates")) || schema != 1L && schema != 2L) {
            throw new IllegalArgumentException("Unsupported policy schema");
        }
        long revision = ModulePolicy.integer(root, "revision");
        if (revision < 1L) {
            throw new IllegalArgumentException("Invalid revision");
        }
        boolean safe = ModulePolicy.bool(root, "safeMode");
        JsonObject source = root.getAsJsonObject("modules");
        if (!source.keySet().containsAll(BASE_MODULES) || !POLICY_MODULES.containsAll(source.keySet())) {
            throw new IllegalArgumentException("Incomplete or unknown module list");
        }
        LinkedHashMap<String, Boolean> modules = new LinkedHashMap<String, Boolean>();
        LinkedHashMap<String, ServerModuleRule> serverRules = new LinkedHashMap<String, ServerModuleRule>();
        for (String id : source.keySet()) {
            JsonObject rule = source.getAsJsonObject(id);
            if (!rule.keySet().containsAll(Set.of("allowed", "enabled")) || !(schema == 1L ? Set.of("allowed", "enabled") : Set.of("allowed", "enabled", "ips", "banned-ips")).containsAll(rule.keySet())) {
                throw new IllegalArgumentException("Invalid module rule");
            }
            boolean allowed = ModulePolicy.bool(rule, "allowed");
            boolean enabled = ModulePolicy.bool(rule, "enabled");
            modules.put(id, allowed && enabled);
            if (schema != 2L) continue;
            serverRules.put(id, ServerModuleRule.parse(rule));
        }
        JsonObject releases = root.getAsJsonObject("updates");
        if (!GAME_VERSIONS.containsAll(releases.keySet())) {
            throw new IllegalArgumentException("Unknown game version");
        }
        LinkedHashMap<String, String> updates = new LinkedHashMap<String, String>();
        for (String version : releases.keySet()) {
            JsonElement value = releases.get(version);
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("Invalid release");
            }
            String release = value.getAsString();
            if (!release.matches("[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}")) {
                throw new IllegalArgumentException("Invalid release version");
            }
            updates.put(version, release);
        }
        return new ModulePolicy(revision, safe, modules, updates, serverRules);
    }

    private static boolean bool(JsonObject o, String name) {
        JsonElement value = o.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Expected boolean: " + name);
        }
        return value.getAsBoolean();
    }

    private static long integer(JsonObject o, String name) {
        JsonElement value = o.get(name);
        if (!(value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber() && value.getAsString().matches("[0-9]{1,15}"))) {
            throw new IllegalArgumentException("Expected integer: " + name);
        }
        return value.getAsLong();
    }

    public static boolean newer(String candidate, String installed) {
        if (candidate == null || !candidate.matches("[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}") || !installed.matches("[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}")) {
            return false;
        }
        String[] a = candidate.split("\\.");
        String[] b = installed.split("\\.");
        for (int i = 0; i < 3; ++i) {
            int difference = Integer.compare(Integer.parseInt(a[i]), Integer.parseInt(b[i]));
            if (difference == 0) continue;
            return difference > 0;
        }
        return false;
    }

    static {
        HashSet<String> all = new HashSet<String>(BASE_MODULES);
        all.addAll(EXTENSION_MODULES);
        POLICY_MODULES = Set.copyOf(all);
        all.remove("visualHat");
        all.remove("simpleVoice");
        all.remove("plasmoVoice");
        MODULES = Set.copyOf(all);
        SAFE_BLOCKS = Set.of("legendaryVisuals", "opponentCooldowns", "betterNameVisibility");
        GAME_VERSIONS = Set.of("1.21.4", "1.21.8", "1.21.11", "26.1", "26.2", "26.3");
    }
}

