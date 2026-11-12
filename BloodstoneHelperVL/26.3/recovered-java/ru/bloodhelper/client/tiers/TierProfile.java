/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.tiers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public record TierProfile(Map<String, String> ranks) {
    public static final TierProfile EMPTY = new TierProfile(Map.of());

    public TierProfile {
        ranks = Map.copyOf(ranks);
    }

    public Badge select(String mode) {
        if (!"BEST".equals(mode)) {
            String tier = this.ranks.get(mode);
            return tier == null ? null : new Badge(mode, tier);
        }
        return this.ranks.entrySet().stream().sorted(Comparator.comparingInt(e -> TierProfile.order((String)e.getValue())).thenComparing(Map.Entry::getKey)).map(e -> new Badge((String)e.getKey(), (String)e.getValue())).findFirst().orElse(null);
    }

    public static int order(String tier) {
        return (tier.charAt(2) - 49) * 2 + (tier.charAt(0) == 'H' ? 0 : 1);
    }

    public static String mode(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "crystal" -> "vanilla";
            case "dpot" -> "pot";
            case "netherite", "neth_pot", "netherpot", "npot" -> "nethpot";
            case "op", "nethop", "neth_op" -> "netherop";
            default -> value.toLowerCase(Locale.ROOT);
        };
    }

    private static void add(Map<String, String> ranks, String key, String value) {
        if (value == null || key == null || !key.matches("[a-zA-Z_]{1,24}")) {
            return;
        }
        String tier = value.toUpperCase(Locale.ROOT);
        if (tier.matches("[HL]T[1-5]")) {
            ranks.put(TierProfile.mode(key), tier);
        }
    }

    public static TierProfile parse(String provider, JsonObject root) {
        LinkedHashMap<String, String> ranks;
        block7: {
            block8: {
                block6: {
                    if (TierProfile.flag(root, "is_restricted") || TierProfile.flag(root, "_fullyRestricted") || TierProfile.flag(root, "banned") || TierProfile.flag(root, "restricted")) {
                        return EMPTY;
                    }
                    ranks = new LinkedHashMap<String, String>();
                    if (!provider.equals("MY")) break block6;
                    block0: for (Map.Entry e : root.entrySet()) {
                        if (!((String)e.getKey()).endsWith("Tiers") || ((String)e.getKey()).endsWith("PeakTiers") || !((JsonElement)e.getValue()).isJsonArray()) continue;
                        for (JsonElement tier : ((JsonElement)e.getValue()).getAsJsonArray()) {
                            if (!tier.isJsonPrimitive() || !tier.getAsJsonPrimitive().isString()) continue;
                            String key = ((String)e.getKey()).substring(0, ((String)e.getKey()).length() - 5);
                            String value = tier.getAsString().toUpperCase(Locale.ROOT);
                            if (!value.matches("[HL]T[1-5]")) continue;
                            TierProfile.add(ranks, key, value);
                            continue block0;
                        }
                    }
                    break block7;
                }
                if (!provider.equals("CIS") && !provider.equals("A")) break block8;
                JsonElement node = root.get(provider.equals("CIS") ? "tiers" : "ranks");
                if (node == null || !node.isJsonObject()) break block7;
                for (Map.Entry e : node.getAsJsonObject().entrySet()) {
                    if (!((JsonElement)e.getValue()).isJsonPrimitive() || !((JsonElement)e.getValue()).getAsJsonPrimitive().isString()) continue;
                    TierProfile.add(ranks, (String)e.getKey(), ((JsonElement)e.getValue()).getAsString());
                }
                break block7;
            }
            JsonElement node = root.get("rankings");
            if (node != null && node.isJsonObject()) {
                for (Map.Entry e : node.getAsJsonObject().entrySet()) {
                    JsonObject rank;
                    if (!((JsonElement)e.getValue()).isJsonObject() || TierProfile.flag(rank = ((JsonElement)e.getValue()).getAsJsonObject(), "retired")) continue;
                    int tier = TierProfile.integer(rank, "tier", -1);
                    int pos = TierProfile.integer(rank, "pos", -1);
                    if (tier < 1 || tier > 5 || pos != 0 && pos != 1) continue;
                    TierProfile.add(ranks, (String)e.getKey(), (pos == 0 ? "HT" : "LT") + tier);
                }
            }
        }
        return new TierProfile(ranks);
    }

    private static int integer(JsonObject o, String key, int fallback) {
        JsonElement v = o.get(key);
        if (!(v != null && v.isJsonPrimitive() && v.getAsJsonPrimitive().isNumber() && v.getAsString().matches("[0-9]"))) {
            return fallback;
        }
        return v.getAsInt();
    }

    private static boolean flag(JsonObject o, String key) {
        JsonElement v = o.get(key);
        return v != null && v.isJsonPrimitive() && v.getAsJsonPrimitive().isBoolean() && v.getAsBoolean();
    }

    public record Badge(String mode, String tier) {
    }
}

