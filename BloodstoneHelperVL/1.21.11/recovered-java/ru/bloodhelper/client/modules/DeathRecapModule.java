/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1291
 *  net.minecraft.class_1304
 *  net.minecraft.class_1799
 *  net.minecraft.class_2338
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  net.minecraft.class_634
 *  net.minecraft.class_746
 *  net.minecraft.class_9334
 */
package ru.bloodhelper.client.modules;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.class_1291;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_634;
import net.minecraft.class_746;
import net.minecraft.class_9334;
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
    private static class_2561 pendingMessage;
    private static long pendingAt;
    private static long lastDeathAt;

    private DeathRecapModule() {
    }

    public static void tick(class_310 client) {
        UUID nextAccount;
        DeathScreenshotModule.tick(client);
        class_634 next = client.method_1562();
        UUID uUID = nextAccount = next == null ? null : next.method_2879().id();
        if (next != connection || !Objects.equals(account, nextAccount)) {
            DeathRecapModule.reset();
            connection = next;
            account = nextAccount;
        }
        CombatTimelineModule.tick(client);
        class_746 player = client.field_1724;
        if (next == null || player == null || client.field_1687 == null || !BloodHelperClient.config().deathRecap.enabled) {
            OBSERVATION.reset();
            return;
        }
        boolean alive = player.method_5805() && !player.method_29504();
        Snapshot beforeDeath = OBSERVATION.sample(player, alive, alive ? DeathRecapModule.capture(client) : null);
        if (beforeDeath != null) {
            class_2338 pos = player.method_24515();
            long now = System.nanoTime();
            class_2561 message = pendingPlayer == player && now - pendingAt < 5000000000L ? pendingMessage : null;
            HISTORY.addFirst(new Snapshot(Instant.now(), pos.method_10263(), pos.method_10264(), pos.method_10260(), client.field_1687.method_27983().method_29177().toString(), message, beforeDeath.armor(), beforeDeath.effects(), DeathScreenshotModule.fileForCurrentDeath(player), CombatTimelineModule.freezeForDeath(player)));
            lastDeathPlayer = player;
            lastDeathAt = now;
            pendingPlayer = null;
            pendingMessage = null;
        }
        DeathRecapModule.trim();
    }

    private static Snapshot capture(class_310 client) {
        class_746 player = client.field_1724;
        ArrayList<Armor> armor = new ArrayList<Armor>(4);
        for (class_1304 slot : List.of(class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166)) {
            class_1799 copy = player.method_6118(slot).method_7972();
            armor.add(new Armor(slot, copy, InventorySnapshot.durability(copy.method_7963(), copy.method_57826(class_9334.field_49630), copy.method_7936(), copy.method_7919())));
        }
        List<Effect> effects = player.method_6026().stream().map(effect -> new Effect((class_2561)((class_1291)effect.method_5579().comp_349()).method_5560().method_27661(), effect.method_5578() + 1, effect.method_5584(), effect.method_48559())).toList();
        class_2338 pos = player.method_24515();
        return new Snapshot(Instant.now(), pos.method_10263(), pos.method_10264(), pos.method_10260(), client.field_1687.method_27983().method_29177().toString(), null, armor, effects);
    }

    public static void receiveDeathMessage(int playerId, class_2561 message) {
        class_310 client = class_310.method_1551();
        if (!BloodHelperClient.config().deathRecap.enabled || client.field_1724 == null || client.field_1724.method_5628() != playerId || message == null) {
            return;
        }
        DeathRecapModule.tick(client);
        long now = System.nanoTime();
        if (lastDeathPlayer == client.field_1724 && !client.field_1724.method_5805() && now - lastDeathAt < 5000000000L && !HISTORY.isEmpty()) {
            Snapshot latest = HISTORY.getFirst();
            HISTORY.set(0, new Snapshot(latest.time(), latest.x(), latest.y(), latest.z(), latest.dimension(), (class_2561)message.method_27661(), latest.armor(), latest.effects(), latest.screenshotFile(), latest.timeline()));
        } else {
            pendingPlayer = client.field_1724;
            pendingMessage = message.method_27661();
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
        class_310 client = class_310.method_1551();
        return client.method_1562() != null && connection == client.method_1562() && HISTORY.contains(snapshot);
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

    public static void openScreen(class_437 parent) {
        class_310.method_1551().method_1507((class_437)new DeathRecapScreen(parent));
    }

    static {
        HISTORY = new ArrayList<Snapshot>();
    }

    public record Snapshot(Instant time, int x, int y, int z, String dimension, class_2561 deathMessage, List<Armor> armor, List<Effect> effects, String screenshotFile, List<CombatTimelineRecorder.Event> timeline) {
        public Snapshot {
            armor = List.copyOf(armor);
            effects = List.copyOf(effects);
            timeline = List.copyOf(timeline);
        }

        public Snapshot(Instant time, int x, int y, int z, String dimension, class_2561 deathMessage, List<Armor> armor, List<Effect> effects, String screenshotFile) {
            this(time, x, y, z, dimension, deathMessage, armor, effects, screenshotFile, List.of());
        }

        public Snapshot(Instant time, int x, int y, int z, String dimension, class_2561 deathMessage, List<Armor> armor, List<Effect> effects) {
            this(time, x, y, z, dimension, deathMessage, armor, effects, null);
        }
    }

    public record Armor(class_1304 slot, class_1799 item, InventorySnapshot.Durability durability) {
    }

    public record Effect(class_2561 name, int level, int ticks, boolean infinite) {
    }
}

