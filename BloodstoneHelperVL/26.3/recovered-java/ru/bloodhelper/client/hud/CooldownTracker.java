/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemCooldowns
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.mixin.CooldownInstanceAccessor;
import ru.bloodhelper.client.mixin.ItemCooldownsAccessor;
import ru.bloodhelper.client.modules.CombatStateModule;

public final class CooldownTracker {
    private static final Snapshot EMPTY = new Snapshot(List.of(), null);
    private static final EquipmentSlot[] EQUIPMENT_SLOTS = EquipmentSlot.values();
    private static final Map<Identifier, Object> observedCooldowns = new HashMap<Identifier, Object>();
    private static Object cachedManager;
    private static int cachedTick;
    private static int cachedInventoryRevision;
    private static String cachedLanguage;
    private static String cachedPvpGroup;
    private static boolean cachedPvpEnabled;
    private static boolean cachedShowUnknown;
    private static boolean cachedItemsEnabled;
    private static Snapshot cached;
    private static Entry combatEntry;

    public static void clearCache() {
        cachedManager = null;
        observedCooldowns.clear();
        cached = EMPTY;
    }

    public static Snapshot snapshot(ClientConfig config) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            CooldownTracker.clearCache();
            return EMPTY;
        }
        ItemCooldowns manager = mc.player.getCooldowns();
        ItemCooldownsAccessor accessor = (ItemCooldownsAccessor)manager;
        Map<Identifier, Object> cooldowns = accessor.bloodhelper$cooldowns();
        Entry pvp = CooldownTracker.combatEntry(config);
        if (cooldowns.isEmpty() || !config.cooldowns.enabled) {
            cachedManager = null;
            if (!cached.items().isEmpty() || cached.pvp() != pvp) {
                cached = pvp == null ? EMPTY : new Snapshot(List.of(), pvp);
            }
            return cached;
        }
        int tick = accessor.bloodhelper$tickCount();
        int inventoryRevision = mc.player.getInventory().getTimesChanged();
        if (cachedManager == manager && cachedTick == tick && cachedInventoryRevision == inventoryRevision && cachedPvpEnabled == config.pvpTimerEnabled() && cachedShowUnknown == config.cooldowns.showUnknown && cachedItemsEnabled == config.cooldowns.enabled && cached.pvp() == pvp && Objects.equals(cachedPvpGroup, config.pvp.cooldownGroup) && Objects.equals(cachedLanguage, config.language) && CooldownTracker.sameCooldowns(cooldowns)) {
            return cached;
        }
        HashMap<Identifier, ItemStack> matchingItems = new HashMap<Identifier, ItemStack>();
        for (ItemStack item : mc.player.getInventory().getNonEquipmentItems()) {
            if (item.isEmpty()) continue;
            matchingItems.putIfAbsent(manager.getCooldownGroup(item), item);
        }
        for (EquipmentSlot slot : EQUIPMENT_SLOTS) {
            ItemStack item = mc.player.getItemBySlot(slot);
            if (item.isEmpty()) continue;
            matchingItems.putIfAbsent(manager.getCooldownGroup(item), item);
        }
        ArrayList<Entry> items = new ArrayList<Entry>();
        for (Map.Entry<Identifier, Object> value : cooldowns.entrySet()) {
            Item item;
            CooldownInstanceAccessor cd = (CooldownInstanceAccessor)value.getValue();
            int ticksLeft = cd.bloodhelper$endTime() - tick;
            if (ticksLeft <= 0) continue;
            int duration = Math.max(1, cd.bloodhelper$endTime() - cd.bloodhelper$startTime());
            Identifier group = value.getKey();
            if (config.pvpTimerEnabled() && group.toString().equals(config.pvp.cooldownGroup)) continue;
            ItemStack stack = matchingItems.getOrDefault(group, ItemStack.EMPTY);
            if (stack.isEmpty() && (item = (Item)BuiltInRegistries.ITEM.getOptional(group).orElse(null)) != null && item != Items.AIR) {
                stack = new ItemStack((ItemLike)item);
            }
            boolean known = !stack.isEmpty();
            String label = known ? UiLanguage.text(stack.getHoverName()) : group.getPath();
            Entry entry = new Entry(group, stack, label, (double)ticksLeft / 20.0, Math.clamp((float)ticksLeft / (float)duration, 0.0f, 1.0f), known);
            if (!known && !config.cooldowns.showUnknown) continue;
            items.add(entry);
        }
        items.sort(Comparator.comparingDouble(Entry::seconds).thenComparing(e -> e.group().toString()));
        cachedManager = manager;
        cachedTick = tick;
        cachedInventoryRevision = inventoryRevision;
        cachedPvpEnabled = config.pvpTimerEnabled();
        cachedItemsEnabled = config.cooldowns.enabled;
        cachedShowUnknown = config.cooldowns.showUnknown;
        cachedPvpGroup = config.pvp.cooldownGroup;
        cachedLanguage = config.language;
        observedCooldowns.clear();
        observedCooldowns.putAll(cooldowns);
        cached = new Snapshot(List.copyOf(items), pvp);
        return cached;
    }

    private static Entry combatEntry(ClientConfig config) {
        if (!config.pvpTimerEnabled() || !CombatStateModule.active()) {
            combatEntry = null;
            return null;
        }
        double seconds = CombatStateModule.secondsRemaining();
        float fraction = CombatStateModule.remainingFraction();
        if (combatEntry == null || combatEntry.seconds() != seconds || combatEntry.remaining() != fraction || !combatEntry.group().toString().equals(config.pvp.cooldownGroup)) {
            Identifier group = Identifier.tryParse((String)config.pvp.cooldownGroup);
            if (group == null) {
                return null;
            }
            combatEntry = new Entry(group, ItemStack.EMPTY, "PvP", seconds, fraction, true);
        }
        return combatEntry;
    }

    private static boolean sameCooldowns(Map<Identifier, Object> cooldowns) {
        if (cooldowns.size() != observedCooldowns.size()) {
            return false;
        }
        for (Map.Entry<Identifier, Object> entry : cooldowns.entrySet()) {
            if (observedCooldowns.get(entry.getKey()) == entry.getValue()) continue;
            return false;
        }
        return true;
    }

    static {
        cached = EMPTY;
    }

    public record Snapshot(List<Entry> items, Entry pvp) {
    }

    public record Entry(Identifier group, ItemStack item, String label, double seconds, float remaining, boolean known) {
    }
}

