/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1304
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 *  net.minecraft.class_7923
 *  net.minecraft.class_9334
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_9334;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.UiLanguage;

public final class InventorySnapshot {
    private static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of(), List.of());
    private static final class_1304[] ARMOR_SLOTS = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
    private static Object cachedPlayer;
    private static int cachedTick;
    private static int cachedRevision;
    private static String cachedLanguage;
    private static Snapshot cached;

    private InventorySnapshot() {
    }

    public static void clearCache() {
        cachedPlayer = null;
        cachedLanguage = null;
        cached = EMPTY;
    }

    public static Snapshot capture() {
        class_746 player = class_310.method_1551().field_1724;
        if (player == null) {
            InventorySnapshot.clearCache();
            return EMPTY;
        }
        String language = BloodHelperClient.config().language;
        int tick = player.field_6012;
        int revision = player.method_31548().method_7364();
        if (cachedPlayer == player && cachedTick == tick && cachedRevision == revision && Objects.equals(cachedLanguage, language)) {
            return cached;
        }
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, class_1799> icons = new LinkedHashMap<String, class_1799>();
        for (class_1799 stack : player.method_31548().method_67533()) {
            CountedItem item = InventorySnapshot.counted(stack);
            if (item == null) continue;
            InventorySnapshot.add(counts, item);
            icons.computeIfAbsent(item.id(), ignored -> stack.method_46651(1));
        }
        class_1799 offhand = player.method_6118(class_1304.field_6171);
        CountedItem offhandCount = InventorySnapshot.counted(offhand);
        if (offhandCount != null) {
            InventorySnapshot.add(counts, offhandCount);
            icons.computeIfAbsent(offhandCount.id(), ignored -> offhand.method_46651(1));
        }
        ArrayList<ArmorPiece> armor = new ArrayList<ArmorPiece>(4);
        for (class_1304 slot : ARMOR_SLOTS) {
            class_1799 item = player.method_6118(slot);
            armor.add(InventorySnapshot.armorPiece(slot, item));
        }
        cachedPlayer = player;
        cachedTick = tick;
        cachedRevision = revision;
        cachedLanguage = language;
        cached = new Snapshot(counts, icons, armor);
        return cached;
    }

    public static Map<String, Integer> countOwned(List<CountedItem> mainInventory, CountedItem offhand) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (CountedItem item : mainInventory) {
            InventorySnapshot.add(result, item);
        }
        InventorySnapshot.add(result, offhand);
        return Map.copyOf(result);
    }

    public static Durability durability(boolean damageable, boolean unbreakable, int maximum, int damage) {
        if (unbreakable) {
            return new Durability(false, 0, 0, true);
        }
        if (!damageable || maximum <= 0) {
            return new Durability(false, 0, 0, false);
        }
        int remaining = Math.clamp((long)maximum - (long)damage, 0, maximum);
        return new Durability(true, remaining, maximum, false);
    }

    static ArmorPiece armorPiece(class_1304 slot, class_1799 item) {
        if (item == null || item.method_7960()) {
            return new ArmorPiece(slot, "", "", class_1799.field_8037, InventorySnapshot.durability(false, false, 0, 0));
        }
        class_1799 copy = item.method_46651(1);
        return new ArmorPiece(slot, class_7923.field_41178.method_10221((Object)copy.method_7909()).toString(), UiLanguage.text(copy.method_7964()), copy, InventorySnapshot.durability(copy.method_7963(), copy.method_57826(class_9334.field_49630), copy.method_7936(), copy.method_7919()));
    }

    private static CountedItem counted(class_1799 stack) {
        return stack == null || stack.method_7960() ? null : new CountedItem(class_7923.field_41178.method_10221((Object)stack.method_7909()).toString(), stack.method_7947());
    }

    private static void add(Map<String, Integer> target, CountedItem item) {
        if (item == null || item.id() == null || item.id().isBlank() || item.count() <= 0) {
            return;
        }
        target.merge(item.id(), item.count(), (left, right) -> (int)Math.min(Integer.MAX_VALUE, (long)left.intValue() + (long)right.intValue()));
    }

    static {
        cached = EMPTY;
    }

    public record Snapshot(Map<String, Integer> counts, Map<String, class_1799> icons, List<ArmorPiece> armor) {
        public Snapshot {
            counts = Map.copyOf(counts);
            icons = Map.copyOf(icons);
            armor = List.copyOf(armor);
        }

        public int count(String id) {
            return this.counts.getOrDefault(id, 0);
        }
    }

    public record CountedItem(String id, int count) {
    }

    public record ArmorPiece(class_1304 slot, String id, String displayName, class_1799 item, Durability durability) {
    }

    public record Durability(boolean damageable, int remaining, int maximum, boolean unbreakable) {
        public int percent() {
            return this.damageable && this.maximum > 0 ? (int)Math.ceil((double)this.remaining * 100.0 / (double)this.maximum) : -1;
        }

        public float fraction() {
            return this.damageable && this.maximum > 0 ? Math.clamp((float)this.remaining / (float)this.maximum, 0.0f, 1.0f) : 1.0f;
        }

        public boolean low(int thresholdPercent) {
            return this.damageable && this.maximum > 0 && (long)this.remaining * 100L <= (long)this.maximum * (long)thresholdPercent;
        }

        public String display(String mode) {
            if (this.unbreakable) {
                return "\u221e";
            }
            if (!this.damageable) {
                return "\u2014";
            }
            return "POINTS".equals(mode) ? Integer.toString(this.remaining) : this.percent() + "%";
        }
    }
}

