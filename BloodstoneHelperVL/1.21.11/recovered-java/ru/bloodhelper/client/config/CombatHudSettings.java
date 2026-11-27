/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import ru.bloodhelper.client.config.BlastLeggingsSettings;
import ru.bloodhelper.client.config.ClientConfig;

public final class CombatHudSettings {
    private static final Set<String> WIDGETS = Set.of("cooldowns", "pvp", "music", "notifications", "effects", "performance", "session", "waypoints", "sizeTimer", "armor", "itemCounter", "watermark", "armor.head", "armor.chest", "armor.legs", "armor.feet", "blastLeggings", "reachDisplay", "comboCounter", "saturation", "generalHud.fps", "generalHud.cps", "generalHud.ram", "generalHud.compass", "generalHud.playtime", "generalHud.days", "generalHud.keys", "generalHud.speed");
    public static final int MAX_POSES = 288;
    public BlastLeggingsSettings blastLeggings = new BlastLeggingsSettings();
    public boolean enabled = false;
    public boolean useCombatLayout = true;
    public int moduleRevision;
    public boolean compactMusic = true;
    public int layoutRevision;
    public Map<String, Pose> layout = new LinkedHashMap<String, Pose>();

    public void sanitize() {
        if (this.blastLeggings == null) {
            this.blastLeggings = new BlastLeggingsSettings();
        }
        this.blastLeggings.sanitize();
        LinkedHashMap<String, Pose> safe = new LinkedHashMap<String, Pose>();
        if (this.layout != null) {
            for (Map.Entry<String, Pose> entry : this.layout.entrySet()) {
                if (safe.size() >= 288) break;
                if (!CombatHudSettings.validKey(entry.getKey()) || entry.getValue() == null) continue;
                entry.getValue().sanitize();
                safe.put(entry.getKey(), entry.getValue());
            }
        }
        this.layout = safe;
    }

    public void migrateModules(ClientConfig.PvpSettings timer) {
        if (this.moduleRevision >= 1) {
            return;
        }
        this.useCombatLayout = this.enabled;
        this.enabled = this.enabled || timer.enabled;
        this.moduleRevision = 1;
    }

    public static boolean validKey(String key) {
        if (key == null) {
            return false;
        }
        return WIDGETS.contains(key) || key.startsWith("itemCounter.") && key.length() <= 140 && key.substring("itemCounter.".length()).matches("[a-z0-9_.-]+:[a-z0-9/._-]+");
    }

    public void migrateCooldownPreset(ClientConfig.CooldownSettings normal) {
        if (this.layoutRevision >= 1) {
            return;
        }
        Pose old = this.layout.get("cooldowns");
        if (old != null && old.position.x == 0.27 && old.position.y == 0.36 && "LEFT".equals(old.alignment) && !old.bottom) {
            this.layout.put("cooldowns", new Pose(normal.position, normal.alignment, normal.bottom));
        }
        this.layoutRevision = 1;
    }

    public Pose ensure(String key, ClientConfig.HudPosition normal, String alignment, boolean bottom) {
        Pose found = this.layout.get(key);
        if (found != null) {
            return found;
        }
        Pose result = CombatHudSettings.initialPose(key, normal, alignment, bottom);
        if (CombatHudSettings.validKey(key)) {
            if (this.layout.size() >= 288) {
                String oldestItem = this.layout.keySet().stream().filter(k -> k.startsWith("itemCounter.")).findFirst().orElse(this.layout.keySet().iterator().next());
                this.layout.remove(oldestItem);
            }
            this.layout.put(key, result);
        }
        return result;
    }

    public static Pose initialPose(String key, ClientConfig.HudPosition normal, String alignment, boolean bottom) {
        Pose pose = new Pose(normal, alignment, bottom);
        switch (key) {
            case "pvp": {
                pose.position.x = 0.5;
                pose.position.y = 0.69;
                pose.alignment = "CENTER";
                pose.bottom = false;
                break;
            }
            case "armor": {
                pose.position.x = 0.73;
                pose.position.y = 0.59;
                pose.alignment = "RIGHT";
                pose.bottom = false;
                break;
            }
            case "armor.head": 
            case "armor.chest": 
            case "armor.legs": 
            case "armor.feet": {
                int slot = switch (key) {
                    case "armor.head" -> 0;
                    case "armor.chest" -> 1;
                    case "armor.legs" -> 2;
                    default -> 3;
                };
                pose.position.x = 0.73;
                pose.position.y = 0.34 + (double)slot * 0.095;
                pose.alignment = "RIGHT";
                pose.bottom = false;
                break;
            }
        }
        return pose;
    }

    private static double finite(double value, double fallback, double min, double max) {
        return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }

    public static final class Pose {
        public ClientConfig.HudPosition position = new ClientConfig.HudPosition(0.5, 0.5);
        public String alignment = "LEFT";
        public boolean bottom;

        public Pose() {
        }

        public Pose(ClientConfig.HudPosition source, String alignment, boolean bottom) {
            this.position = new ClientConfig.HudPosition(source.x, source.y);
            this.position.scale = source.scale;
            this.alignment = alignment;
            this.bottom = bottom;
        }

        public void sanitize() {
            if (this.position == null) {
                this.position = new ClientConfig.HudPosition(0.5, 0.5);
            }
            this.position.x = CombatHudSettings.finite(this.position.x, 0.5, 0.0, 1.0);
            this.position.y = CombatHudSettings.finite(this.position.y, 0.5, 0.0, 1.0);
            this.position.scale = CombatHudSettings.finite(this.position.scale, 0.65, 0.35, 2.0);
            if (!("LEFT".equals(this.alignment) || "CENTER".equals(this.alignment) || "RIGHT".equals(this.alignment))) {
                this.alignment = "LEFT";
            }
        }
    }
}

