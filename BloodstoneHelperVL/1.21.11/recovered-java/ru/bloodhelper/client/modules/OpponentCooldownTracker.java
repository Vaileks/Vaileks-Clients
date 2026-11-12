/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1132
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_2479
 *  net.minecraft.class_2481
 *  net.minecraft.class_2487
 *  net.minecraft.class_2503
 *  net.minecraft.class_2519
 *  net.minecraft.class_2520
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_742
 *  net.minecraft.class_7923
 *  net.minecraft.class_9279
 *  net.minecraft.class_9334
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1132;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2479;
import net.minecraft.class_2481;
import net.minecraft.class_2487;
import net.minecraft.class_2503;
import net.minecraft.class_2519;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_742;
import net.minecraft.class_7923;
import net.minecraft.class_9279;
import net.minecraft.class_9334;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.modules.LegendaryCooldownData;
import ru.bloodhelper.client.modules.OpponentCooldownState;

public final class OpponentCooldownTracker {
    private static final OpponentCooldownState<IconData> STATE = new OpponentCooldownState(IconData::copy);
    private static final Map<SlotKey, SeenStack> SEEN = new LinkedHashMap<SlotKey, SeenStack>();
    private static Object world;

    private OpponentCooldownTracker() {
    }

    public static boolean available(class_310 client) {
        if (client == null || client.field_1687 == null || client.field_1724 == null || client.method_1562() == null) {
            return false;
        }
        class_1132 localServer = client.method_1576();
        return localServer != null && !localServer.method_3860() && client.method_1562().method_48296().method_10756();
    }

    public static void tick(class_310 client, ClientConfig config) {
        if (!(config != null && config.opponentCooldowns != null && config.opponentCooldownsEnabled() && OpponentCooldownTracker.available(client) && client.field_1724.method_5805())) {
            OpponentCooldownTracker.clear();
            return;
        }
        if (world != client.field_1687) {
            OpponentCooldownTracker.clear();
            world = client.field_1687;
        }
        STATE.world(client.field_1687);
        HashSet<OpponentCooldownState.PlayerKey> present = new HashSet<OpponentCooldownState.PlayerKey>();
        for (class_742 player : client.field_1687.method_18456()) {
            if (player == client.field_1724 || !player.method_5805() || player.method_31481() || player.method_7325()) continue;
            present.add(new OpponentCooldownState.PlayerKey(player.method_5667(), player.method_5628()));
        }
        STATE.retainPlayers(present);
        SEEN.keySet().removeIf(slot -> !present.contains(slot.player()));
        double configuredRange = config.opponentCooldowns.maxDistance;
        double range = Double.isFinite(configuredRange) ? Math.clamp(configuredRange, 4.0, 128.0) : 48.0;
        long epochMillis = System.currentTimeMillis();
        long nowNanos = System.nanoTime();
        for (class_742 player : client.field_1687.method_18456()) {
            OpponentCooldownState.PlayerKey key = new OpponentCooldownState.PlayerKey(player.method_5667(), player.method_5628());
            if (!present.contains(key) || player.method_5756((class_1657)client.field_1724) || player.method_5858((class_1297)client.field_1724) > range * range) continue;
            OpponentCooldownTracker.observe(new SlotKey(key, false), player, player.method_6047(), epochMillis, nowNanos);
            OpponentCooldownTracker.observe(new SlotKey(key, true), player, player.method_6079(), epochMillis, nowNanos);
        }
    }

    public static void clear() {
        STATE.clear();
        SEEN.clear();
        world = null;
    }

    public static List<Entry> snapshot(UUID playerId, int entityId) {
        class_310 client = class_310.method_1551();
        if (!BloodHelperClient.config().opponentCooldownsEnabled() || !OpponentCooldownTracker.available(client) || world != client.field_1687) {
            OpponentCooldownTracker.clear();
            return List.of();
        }
        if (playerId == null || entityId < 0) {
            return List.of();
        }
        ArrayList<Entry> result = new ArrayList<Entry>();
        for (OpponentCooldownState.Remaining<IconData> remaining : STATE.snapshot(new OpponentCooldownState.PlayerKey(playerId, entityId), System.nanoTime())) {
            IconData icon = remaining.value();
            result.add(new Entry(icon.item(), icon.weaponKey(), icon.abilityKey(), icon.icon(), remaining.seconds()));
        }
        return List.copyOf(result);
    }

    private static void observe(SlotKey slot, Object owner, class_1799 stack, long epochMillis, long nowNanos) {
        OpponentCooldownState.PlayerKey player = slot.player();
        class_9279 custom = (class_9279)stack.method_58694(class_9334.field_49628);
        class_2960 model = (class_2960)stack.method_58694(class_9334.field_54199);
        SeenStack previous = SEEN.get(slot);
        if (previous != null && previous.owner() != owner) {
            STATE.forgetPlayer(player);
            SEEN.keySet().removeIf(key -> key.player().equals(player));
            previous = null;
        }
        if (previous != null && previous.stack() == stack && previous.customData() == custom && previous.model() == model) {
            return;
        }
        if (!SEEN.containsKey(slot) && SEEN.size() >= 128) {
            SEEN.remove(SEEN.keySet().iterator().next());
        }
        SEEN.put(slot, new SeenStack(owner, stack, custom, model));
        if (stack.method_7960()) {
            return;
        }
        if (custom == null) {
            return;
        }
        class_2487 pdc = custom.method_57461().method_68568("PublicBukkitValues");
        if (pdc.method_33133() || pdc.method_10546() > 128) {
            return;
        }
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<String, Object>();
        for (Map.Entry entry : pdc.method_59874()) {
            class_2479 bytes;
            class_2519 string;
            String key2 = (String)entry.getKey();
            if (key2.length() > 160) continue;
            class_2520 tag = (class_2520)entry.getValue();
            if (tag instanceof class_2503) {
                class_2503 number = (class_2503)tag;
                metadata.put(key2, number.method_10699());
                continue;
            }
            if (tag instanceof class_2481) {
                class_2481 number = (class_2481)tag;
                metadata.put(key2, number.method_10698());
                continue;
            }
            if (tag instanceof class_2519 && (string = (class_2519)tag).comp_3831().length() <= 128) {
                metadata.put(key2, string.comp_3831());
                continue;
            }
            if (!key2.equals("bloodstone:uuid") || !(tag instanceof class_2479) || (bytes = (class_2479)tag).size() > 256) continue;
            metadata.put(key2, bytes.method_10521());
        }
        Optional<LegendaryCooldownData.Weapon> parsed = LegendaryCooldownData.parse(metadata, model == null ? null : model.toString(), class_7923.field_41178.method_10221((Object)stack.method_7909()).toString());
        if (parsed.isEmpty()) {
            return;
        }
        LegendaryCooldownData.Weapon weapon = parsed.get();
        for (LegendaryCooldownData.Timer timer : weapon.timers()) {
            STATE.observe(player, weapon.itemKey(), timer.abilityKey(), timer.deadlineEpochTicks(), epochMillis, nowNanos, new IconData(stack, weapon.weaponKey(), timer.abilityKey(), AbilityIcon.valueOf(timer.icon())));
        }
    }

    private record SlotKey(OpponentCooldownState.PlayerKey player, boolean offhand) {
    }

    private record IconData(class_1799 item, String weaponKey, String abilityKey, AbilityIcon icon) {
        IconData copy() {
            return new IconData(this.item.method_7972(), this.weaponKey, this.abilityKey, this.icon);
        }
    }

    public record Entry(class_1799 item, String weaponKey, String abilityKey, AbilityIcon icon, int seconds) {
    }

    public static enum AbilityIcon {
        BOOST,
        TELEPORT,
        SHADOW,
        SHIELD,
        SHRINK,
        REAP,
        WAVE,
        ABILITY;

    }

    private record SeenStack(Object owner, class_1799 stack, Object customData, Object model) {
    }
}

