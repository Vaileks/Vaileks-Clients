/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.cosmetic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
import ru.bloodhelper.client.cosmetic.CosmeticRoster;

public record NicknamePalette(List<Integer> colors, boolean animated, boolean stepped, int periodMs, boolean leftToRight) {
    public NicknamePalette(List<Integer> colors, boolean animated, boolean stepped, int periodMs) {
        this(colors, animated, stepped, periodMs, false);
    }

    public NicknamePalette {
        colors = List.copyOf(colors);
        if (colors.isEmpty() || colors.size() > 8 || colors.stream().anyMatch(c -> c == null || c < 0 || c > 0xFFFFFF) || periodMs < 1000 || periodMs > 30000) {
            throw new IllegalArgumentException("Invalid nickname palette");
        }
    }

    public static NicknamePalette solid(int rgb) {
        return new NicknamePalette(List.of(Integer.valueOf(rgb)), false, false, 6000);
    }

    public int at(int letter, int length, long milliseconds) {
        double phase;
        if (this.colors.size() == 1) {
            return this.colors.getFirst();
        }
        double d = phase = this.animated ? (double)Math.floorMod(milliseconds, this.periodMs) / (double)this.periodMs : 0.0;
        if (this.stepped) {
            return this.colors.get(Math.floorMod(letter + (this.leftToRight ? -1 : 1) * (int)(phase * (double)this.colors.size()), this.colors.size()));
        }
        if (this.leftToRight) {
            phase = -phase;
        }
        double x = length <= 1 ? 0.0 : Math.clamp((double)letter / (double)(length - 1), 0.0, 1.0);
        double position = this.animated ? (x + phase) * (double)this.colors.size() : x * (double)(this.colors.size() - 1);
        int first = (int)Math.floor(position);
        int second = this.animated ? Math.floorMod(first + 1, this.colors.size()) : Math.min(first + 1, this.colors.size() - 1);
        first = this.animated ? Math.floorMod(first, this.colors.size()) : Math.min(first, this.colors.size() - 1);
        double t = position - Math.floor(position);
        int a = this.colors.get(first);
        int b = this.colors.get(second);
        int rgb = 0;
        for (int shift : new int[]{16, 8, 0}) {
            rgb |= (int)Math.round((double)(a >> shift & 0xFF) * (1.0 - t) + (double)(b >> shift & 0xFF) * t) << shift;
        }
        return rgb;
    }

    public static NicknamePalette parse(JsonElement value, int fallback) {
        if (value == null || value.isJsonNull()) {
            return NicknamePalette.solid(fallback);
        }
        if (!value.isJsonObject()) {
            throw new IllegalArgumentException("Invalid nickname palette");
        }
        JsonObject json = value.getAsJsonObject();
        JsonElement array = json.get("colors");
        if (array == null || !array.isJsonArray() || array.getAsJsonArray().size() < 2 || array.getAsJsonArray().size() > 8) {
            throw new IllegalArgumentException("Invalid palette colors");
        }
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (JsonElement item : array.getAsJsonArray()) {
            if (!item.isJsonPrimitive() || !item.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("Invalid palette color");
            }
            OptionalInt color = CosmeticRoster.parseColor(item.getAsString());
            if (color.isEmpty()) {
                throw new IllegalArgumentException("Invalid palette color");
            }
            colors.add(color.getAsInt());
        }
        JsonElement mode = json.get("mode");
        JsonElement animated = json.get("animated");
        JsonElement period = json.get("periodMs");
        if (!(mode != null && mode.isJsonPrimitive() && mode.getAsJsonPrimitive().isString() && List.of("GRADIENT", "MULTICOLOR").contains(mode.getAsString()))) {
            throw new IllegalArgumentException("Invalid palette mode");
        }
        if (animated == null || !animated.isJsonPrimitive() || !animated.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Invalid palette animation");
        }
        if (!(period != null && period.isJsonPrimitive() && period.getAsJsonPrimitive().isNumber() && period.getAsString().matches("[0-9]{4,5}"))) {
            throw new IllegalArgumentException("Invalid palette period");
        }
        JsonElement direction = json.get("direction");
        if (!(direction == null || direction.isJsonPrimitive() && direction.getAsJsonPrimitive().isString() && List.of("LEFT_TO_RIGHT", "RIGHT_TO_LEFT").contains(direction.getAsString()))) {
            throw new IllegalArgumentException("Invalid palette direction");
        }
        return new NicknamePalette(colors, animated.getAsBoolean(), mode.getAsString().equals("MULTICOLOR"), period.getAsInt(), direction != null && direction.getAsString().equals("LEFT_TO_RIGHT"));
    }

    public JsonElement json() {
        if (this.colors.size() == 1) {
            return JsonNull.INSTANCE;
        }
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        this.colors.forEach(c -> array.add(String.format(Locale.ROOT, "#%06X", c)));
        json.add("colors", (JsonElement)array);
        json.addProperty("mode", this.stepped ? "MULTICOLOR" : "GRADIENT");
        json.addProperty("animated", Boolean.valueOf(this.animated));
        json.addProperty("periodMs", (Number)this.periodMs);
        if (this.leftToRight) {
            json.addProperty("direction", "LEFT_TO_RIGHT");
        }
        return json;
    }
}

