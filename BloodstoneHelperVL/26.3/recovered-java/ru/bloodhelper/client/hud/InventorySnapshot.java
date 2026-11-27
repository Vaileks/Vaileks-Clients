/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ItemStack
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.UiLanguage;

public final class InventorySnapshot {
    private static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of(), List.of());
    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
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
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            InventorySnapshot.clearCache();
            return EMPTY;
        }
        String language = BloodHelperClient.config().language;
        int tick = player.tickCount;
        int revision = player.getInventory().getTimesChanged();
        if (cachedPlayer == player && cachedTick == tick && cachedRevision == revision && Objects.equals(cachedLanguage, language)) {
            return cached;
        }
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, ItemStack> icons = new LinkedHashMap<String, ItemStack>();
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            CountedItem item = InventorySnapshot.counted(stack);
            if (item == null) continue;
            InventorySnapshot.add(counts, item);
            icons.computeIfAbsent(item.id(), ignored -> stack.copyWithCount(1));
        }
        ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
        CountedItem offhandCount = InventorySnapshot.counted(offhand);
        if (offhandCount != null) {
            InventorySnapshot.add(counts, offhandCount);
            icons.computeIfAbsent(offhandCount.id(), ignored -> offhand.copyWithCount(1));
        }
        ArrayList<ArmorPiece> armor = new ArrayList<ArmorPiece>(4);
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack item = player.getItemBySlot(slot);
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

    static ArmorPiece armorPiece(EquipmentSlot slot, ItemStack item) {
        if (item == null || item.isEmpty()) {
            return new ArmorPiece(slot, "", "", ItemStack.EMPTY, InventorySnapshot.durability(false, false, 0, 0));
        }
        ItemStack copy = item.copyWithCount(1);
        return new ArmorPiece(slot, BuiltInRegistries.ITEM.getKey((Object)copy.getItem()).toString(), UiLanguage.text(copy.getHoverName()), copy, InventorySnapshot.durability(copy.isDamageableItem(), copy.has(DataComponents.UNBREAKABLE), copy.getMaxDamage(), copy.getDamageValue()));
    }

    private static CountedItem counted(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : new CountedItem(BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).toString(), stack.getCount());
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

    public record Snapshot(Map<String, Integer> counts, Map<String, ItemStack> icons, List<ArmorPiece> armor) {
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

    public record ArmorPiece(EquipmentSlot slot, String id, String displayName, ItemStack item, Durability durability) {
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

