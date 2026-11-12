/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.server.IntegratedServer
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.ByteArrayTag
 *  net.minecraft.nbt.ByteTag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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

    public static boolean available(Minecraft client) {
        if (client == null || client.level == null || client.player == null || client.getConnection() == null) {
            return false;
        }
        IntegratedServer localServer = client.getSingleplayerServer();
        return localServer != null && !localServer.isPublished() && client.getConnection().getConnection().isMemoryConnection();
    }

    public static void tick(Minecraft client, ClientConfig config) {
        if (!(config != null && config.opponentCooldowns != null && config.opponentCooldownsEnabled() && OpponentCooldownTracker.available(client) && client.player.isAlive())) {
            OpponentCooldownTracker.clear();
            return;
        }
        if (world != client.level) {
            OpponentCooldownTracker.clear();
            world = client.level;
        }
        STATE.world(client.level);
        HashSet<OpponentCooldownState.PlayerKey> present = new HashSet<OpponentCooldownState.PlayerKey>();
        for (AbstractClientPlayer player : client.level.players()) {
            if (player == client.player || !player.isAlive() || player.isRemoved() || player.isSpectator()) continue;
            present.add(new OpponentCooldownState.PlayerKey(player.getUUID(), player.getId()));
        }
        STATE.retainPlayers(present);
        SEEN.keySet().removeIf(slot -> !present.contains(slot.player()));
        double configuredRange = config.opponentCooldowns.maxDistance;
        double range = Double.isFinite(configuredRange) ? Math.clamp(configuredRange, 4.0, 128.0) : 48.0;
        long epochMillis = System.currentTimeMillis();
        long nowNanos = System.nanoTime();
        for (AbstractClientPlayer player : client.level.players()) {
            OpponentCooldownState.PlayerKey key = new OpponentCooldownState.PlayerKey(player.getUUID(), player.getId());
            if (!present.contains(key) || player.isInvisibleTo((Player)client.player) || player.distanceToSqr((Entity)client.player) > range * range) continue;
            OpponentCooldownTracker.observe(new SlotKey(key, false), player, player.getMainHandItem(), epochMillis, nowNanos);
            OpponentCooldownTracker.observe(new SlotKey(key, true), player, player.getOffhandItem(), epochMillis, nowNanos);
        }
    }

    public static void clear() {
        STATE.clear();
        SEEN.clear();
        world = null;
    }

    public static List<Entry> snapshot(UUID playerId, int entityId) {
        Minecraft client = Minecraft.getInstance();
        if (!BloodHelperClient.config().opponentCooldownsEnabled() || !OpponentCooldownTracker.available(client) || world != client.level) {
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

    private static void observe(SlotKey slot, Object owner, ItemStack stack, long epochMillis, long nowNanos) {
        OpponentCooldownState.PlayerKey player = slot.player();
        CustomData custom = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
        Identifier model = (Identifier)stack.get(DataComponents.ITEM_MODEL);
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
        if (stack.isEmpty()) {
            return;
        }
        if (custom == null) {
            return;
        }
        CompoundTag pdc = custom.copyTag().getCompoundOrEmpty("PublicBukkitValues");
        if (pdc.isEmpty() || pdc.size() > 128) {
            return;
        }
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<String, Object>();
        for (Map.Entry entry : pdc.entrySet()) {
            ByteArrayTag bytes;
            StringTag string;
            String key2 = (String)entry.getKey();
            if (key2.length() > 160) continue;
            Tag tag = (Tag)entry.getValue();
            if (tag instanceof LongTag) {
                LongTag number = (LongTag)tag;
                metadata.put(key2, number.longValue());
                continue;
            }
            if (tag instanceof ByteTag) {
                ByteTag number = (ByteTag)tag;
                metadata.put(key2, number.byteValue());
                continue;
            }
            if (tag instanceof StringTag && (string = (StringTag)tag).value().length() <= 128) {
                metadata.put(key2, string.value());
                continue;
            }
            if (!key2.equals("bloodstone:uuid") || !(tag instanceof ByteArrayTag) || (bytes = (ByteArrayTag)tag).size() > 256) continue;
            metadata.put(key2, bytes.getAsByteArray());
        }
        Optional<LegendaryCooldownData.Weapon> parsed = LegendaryCooldownData.parse(metadata, model == null ? null : model.toString(), BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).toString());
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

    private record IconData(ItemStack item, String weaponKey, String abilityKey, AbilityIcon icon) {
        IconData copy() {
            return new IconData(this.item.copy(), this.weaponKey, this.abilityKey, this.icon);
        }
    }

    public record Entry(ItemStack item, String weaponKey, String abilityKey, AbilityIcon icon, int seconds) {
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

    private record SeenStack(Object owner, ItemStack stack, Object customData, Object model) {
    }
}

