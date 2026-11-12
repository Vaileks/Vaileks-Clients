/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ItemStack
 */
package ru.bloodhelper.client.modules;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.hud.InventorySnapshot;
import ru.bloodhelper.client.modules.CombatTimelineModule;
import ru.bloodhelper.client.modules.CombatTimelineRecorder;
import ru.bloodhelper.client.modules.DeathObservation;
import ru.bloodhelper.client.modules.DeathRecapScreen;
import ru.bloodhelper.client.modules.DeathScreenshotModule;

public final class DeathRecapModule {
    private static final DeathObservation<Snapshot> OBSERVATION = new DeathObservation();
    private static Object connection;
    private static UUID account;
    private static final List<Snapshot> HISTORY;
    private static Object pendingPlayer;
    private static Object lastDeathPlayer;
    private static Component pendingMessage;
    private static long pendingAt;
    private static long lastDeathAt;

    private DeathRecapModule() {
    }

    public static void tick(Minecraft client) {
        UUID nextAccount;
        DeathScreenshotModule.tick(client);
        ClientPacketListener next = client.getConnection();
        UUID uUID = nextAccount = next == null ? null : next.getLocalGameProfile().id();
        if (next != connection || !Objects.equals(account, nextAccount)) {
            DeathRecapModule.reset();
            connection = next;
            account = nextAccount;
        }
        CombatTimelineModule.tick(client);
        LocalPlayer player = client.player;
        if (next == null || player == null || client.level == null || !BloodHelperClient.config().deathRecap.enabled) {
            OBSERVATION.reset();
            return;
        }
        boolean alive = player.isAlive() && !player.isDeadOrDying();
        Snapshot beforeDeath = OBSERVATION.sample(player, alive, alive ? DeathRecapModule.capture(client) : null);
        if (beforeDeath != null) {
            BlockPos pos = player.blockPosition();
            long now = System.nanoTime();
            Component message = pendingPlayer == player && now - pendingAt < 5000000000L ? pendingMessage : null;
            HISTORY.addFirst(new Snapshot(Instant.now(), pos.getX(), pos.getY(), pos.getZ(), client.level.dimension().identifier().toString(), message, beforeDeath.armor(), beforeDeath.effects(), DeathScreenshotModule.fileForCurrentDeath(player), CombatTimelineModule.freezeForDeath(player)));
            lastDeathPlayer = player;
            lastDeathAt = now;
            pendingPlayer = null;
            pendingMessage = null;
        }
        DeathRecapModule.trim();
    }

    private static Snapshot capture(Minecraft client) {
        LocalPlayer player = client.player;
        ArrayList<Armor> armor = new ArrayList<Armor>(4);
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack copy = player.getItemBySlot(slot).copy();
            armor.add(new Armor(slot, copy, InventorySnapshot.durability(copy.isDamageableItem(), copy.has(DataComponents.UNBREAKABLE), copy.getMaxDamage(), copy.getDamageValue())));
        }
        List<Effect> effects = player.getActiveEffects().stream().map(effect -> new Effect((Component)((MobEffect)effect.getEffect().value()).getDisplayName().copy(), effect.getAmplifier() + 1, effect.getDuration(), effect.isInfiniteDuration())).toList();
        BlockPos pos = player.blockPosition();
        return new Snapshot(Instant.now(), pos.getX(), pos.getY(), pos.getZ(), client.level.dimension().identifier().toString(), null, armor, effects);
    }

    public static void receiveDeathMessage(int playerId, Component message) {
        Minecraft client = Minecraft.getInstance();
        if (!BloodHelperClient.config().deathRecap.enabled || client.player == null || client.player.getId() != playerId || message == null) {
            return;
        }
        DeathRecapModule.tick(client);
        long now = System.nanoTime();
        if (lastDeathPlayer == client.player && !client.player.isAlive() && now - lastDeathAt < 5000000000L && !HISTORY.isEmpty()) {
            Snapshot latest = HISTORY.getFirst();
            HISTORY.set(0, new Snapshot(latest.time(), latest.x(), latest.y(), latest.z(), latest.dimension(), (Component)message.copy(), latest.armor(), latest.effects(), latest.screenshotFile(), latest.timeline()));
        } else {
            pendingPlayer = client.player;
            pendingMessage = message.copy();
            pendingAt = now;
        }
    }

    private static void trim() {
        int limit = Math.clamp((long)BloodHelperClient.config().deathRecap.historyLimit, 1, 30);
        if (HISTORY.size() > limit) {
            HISTORY.subList(limit, HISTORY.size()).clear();
        }
    }

    public static Snapshot last() {
        return HISTORY.isEmpty() ? null : HISTORY.getFirst();
    }

    public static boolean isCurrentConnection(Snapshot snapshot) {
        Minecraft client = Minecraft.getInstance();
        return client.getConnection() != null && connection == client.getConnection() && HISTORY.contains(snapshot);
    }

    public static List<Snapshot> history() {
        DeathRecapModule.trim();
        return List.copyOf(HISTORY);
    }

    public static void clear() {
        HISTORY.clear();
        lastDeathPlayer = null;
    }

    public static void reset() {
        OBSERVATION.reset();
        CombatTimelineModule.reset();
        DeathRecapModule.clear();
        pendingPlayer = null;
        pendingMessage = null;
    }

    public static void openScreen(Screen parent) {
        Minecraft.getInstance().gui.setScreen((Screen)new DeathRecapScreen(parent));
    }

    static {
        HISTORY = new ArrayList<Snapshot>();
    }

    public record Snapshot(Instant time, int x, int y, int z, String dimension, Component deathMessage, List<Armor> armor, List<Effect> effects, String screenshotFile, List<CombatTimelineRecorder.Event> timeline) {
        public Snapshot {
            armor = List.copyOf(armor);
            effects = List.copyOf(effects);
            timeline = List.copyOf(timeline);
        }

        public Snapshot(Instant time, int x, int y, int z, String dimension, Component deathMessage, List<Armor> armor, List<Effect> effects, String screenshotFile) {
            this(time, x, y, z, dimension, deathMessage, armor, effects, screenshotFile, List.of());
        }

        public Snapshot(Instant time, int x, int y, int z, String dimension, Component deathMessage, List<Armor> armor, List<Effect> effects) {
            this(time, x, y, z, dimension, deathMessage, armor, effects, null);
        }
    }

    public record Armor(EquipmentSlot slot, ItemStack item, InventorySnapshot.Durability durability) {
    }

    public record Effect(Component name, int level, int ticks, boolean infinite) {
    }
}

