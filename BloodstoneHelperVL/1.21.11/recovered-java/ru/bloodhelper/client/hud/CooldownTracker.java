/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1304
 *  net.minecraft.class_1792
 *  net.minecraft.class_1796
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_7923
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.class_1304;
import net.minecraft.class_1792;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_7923;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.mixin.CooldownInstanceAccessor;
import ru.bloodhelper.client.mixin.ItemCooldownsAccessor;
import ru.bloodhelper.client.modules.CombatStateModule;

public final class CooldownTracker {
    private static final Snapshot EMPTY = new Snapshot(List.of(), null);
    private static final class_1304[] EQUIPMENT_SLOTS = class_1304.values();
    private static final Map<class_2960, Object> observedCooldowns = new HashMap<class_2960, Object>();
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
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            CooldownTracker.clearCache();
            return EMPTY;
        }
        class_1796 manager = mc.field_1724.method_7357();
        ItemCooldownsAccessor accessor = (ItemCooldownsAccessor)manager;
        Map<class_2960, Object> cooldowns = accessor.bloodhelper$cooldowns();
        Entry pvp = CooldownTracker.combatEntry(config);
        if (cooldowns.isEmpty() || !config.cooldowns.enabled) {
            cachedManager = null;
            if (!cached.items().isEmpty() || cached.pvp() != pvp) {
                cached = pvp == null ? EMPTY : new Snapshot(List.of(), pvp);
            }
            return cached;
        }
        int tick = accessor.bloodhelper$tickCount();
        int inventoryRevision = mc.field_1724.method_31548().method_7364();
        if (cachedManager == manager && cachedTick == tick && cachedInventoryRevision == inventoryRevision && cachedPvpEnabled == config.pvpTimerEnabled() && cachedShowUnknown == config.cooldowns.showUnknown && cachedItemsEnabled == config.cooldowns.enabled && cached.pvp() == pvp && Objects.equals(cachedPvpGroup, config.pvp.cooldownGroup) && Objects.equals(cachedLanguage, config.language) && CooldownTracker.sameCooldowns(cooldowns)) {
            return cached;
        }
        HashMap<class_2960, class_1799> matchingItems = new HashMap<class_2960, class_1799>();
        for (class_1799 item : mc.field_1724.method_31548().method_67533()) {
            if (item.method_7960()) continue;
            matchingItems.putIfAbsent(manager.method_62836(item), item);
        }
        for (class_1304 slot : EQUIPMENT_SLOTS) {
            class_1799 item = mc.field_1724.method_6118(slot);
            if (item.method_7960()) continue;
            matchingItems.putIfAbsent(manager.method_62836(item), item);
        }
        ArrayList<Entry> items = new ArrayList<Entry>();
        for (Map.Entry<class_2960, Object> value : cooldowns.entrySet()) {
            class_1792 item;
            CooldownInstanceAccessor cd = (CooldownInstanceAccessor)value.getValue();
            int ticksLeft = cd.bloodhelper$endTime() - tick;
            if (ticksLeft <= 0) continue;
            int duration = Math.max(1, cd.bloodhelper$endTime() - cd.bloodhelper$startTime());
            class_2960 group = value.getKey();
            if (config.pvpTimerEnabled() && group.toString().equals(config.pvp.cooldownGroup)) continue;
            class_1799 stack = matchingItems.getOrDefault(group, class_1799.field_8037);
            if (stack.method_7960() && (item = (class_1792)class_7923.field_41178.method_17966(group).orElse(null)) != null && item != class_1802.field_8162) {
                stack = new class_1799((class_1935)item);
            }
            boolean known = !stack.method_7960();
            String label = known ? UiLanguage.text(stack.method_7964()) : group.method_12832();
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
            class_2960 group = class_2960.method_12829((String)config.pvp.cooldownGroup);
            if (group == null) {
                return null;
            }
            combatEntry = new Entry(group, class_1799.field_8037, "PvP", seconds, fraction, true);
        }
        return combatEntry;
    }

    private static boolean sameCooldowns(Map<class_2960, Object> cooldowns) {
        if (cooldowns.size() != observedCooldowns.size()) {
            return false;
        }
        for (Map.Entry<class_2960, Object> entry : cooldowns.entrySet()) {
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

    public record Entry(class_2960 group, class_1799 item, String label, double seconds, float remaining, boolean known) {
    }
}

