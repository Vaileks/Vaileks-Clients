/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.List;
import java.util.Locale;

public final class WorldVisualSettings {
    public static final List<String> TIMES = List.of("DAY", "EVENING", "NIGHT", "CUSTOM");
    public static final List<String> WEATHER = List.of("CLEAR", "RAIN", "SNOW");
    public boolean timeEnabled;
    public String timePreset = "DAY";
    public int customTime = 6000;
    public boolean weatherEnabled;
    public String weather = "CLEAR";
    public int precipitationPercent = 100;
    public boolean fullBrightEnabled;
    public int brightnessPercent = 100;
    public boolean ambienceEnabled;
    public String biome = "minecraft:pale_garden";

    public void sanitize() {
        this.timePreset = WorldVisualSettings.choice(this.timePreset, TIMES, "DAY");
        this.weather = WorldVisualSettings.choice(this.weather, WEATHER, "CLEAR");
        this.customTime = Math.clamp((long)this.customTime, 0, 23999);
        this.precipitationPercent = Math.clamp((long)this.precipitationPercent, 10, 100);
        this.brightnessPercent = Math.clamp((long)this.brightnessPercent, 0, 100);
        String string = this.biome = this.biome == null ? "minecraft:pale_garden" : this.biome.strip().toLowerCase(Locale.ROOT);
        if (!this.biome.contains(":")) {
            this.biome = "minecraft:" + this.biome;
        }
        if (this.biome.length() > 160 || !this.biome.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
            this.biome = "minecraft:pale_garden";
        }
    }

    private static String choice(String value, List<String> choices, String fallback) {
        String normalized = value == null ? fallback : value.strip().toUpperCase(Locale.ROOT);
        return choices.contains(normalized) ? normalized : fallback;
    }

    public int selectedTime() {
        return switch (this.timePreset == null ? "DAY" : this.timePreset) {
            case "EVENING" -> 12700;
            case "NIGHT" -> 18000;
            case "CUSTOM" -> Math.clamp((long)this.customTime, 0, 23999);
            default -> 6000;
        };
    }

    public long visualDayTime(long original) {
        return this.timeEnabled ? Math.floorDiv(original, 24000L) * 24000L + (long)this.selectedTime() : original;
    }

    public float rain(float original) {
        return this.weatherEnabled ? ("CLEAR".equals(this.weather) ? 0.0f : (float)Math.clamp((long)this.precipitationPercent, 10, 100) / 100.0f) : original;
    }

    public float thunder(float original) {
        return this.weatherEnabled ? 0.0f : original;
    }

    public float fullBrightStrength() {
        return this.fullBrightEnabled ? (float)Math.clamp((long)this.brightnessPercent, 0, 100) / 100.0f : 0.0f;
    }

    public float nightVision(float original) {
        float strength = this.fullBrightStrength();
        return strength > 0.0f ? Math.max(original, strength) : original;
    }

    public float ambientLight(float original) {
        float strength = this.fullBrightStrength();
        return strength > 0.0f ? Math.max(original, strength) : original;
    }
}

