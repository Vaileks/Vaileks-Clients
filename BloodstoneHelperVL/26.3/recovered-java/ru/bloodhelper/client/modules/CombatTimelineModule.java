/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.level.Level
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.CombatTimelineSettings;
import ru.bloodhelper.client.modules.CombatTimelineRecorder;

public final class CombatTimelineModule {
    private static final CombatTimelineRecorder RECORDER = new CombatTimelineRecorder();
    private static final ArrayList<CombatTimelineRecorder.EffectState> EFFECTS = new ArrayList();
    private static Object connection;
    private static Object player;
    private static Object level;
    private static Object profile;
    private static int options;
    private static boolean wasAlive;

    private CombatTimelineModule() {
    }

    public static void tick(Minecraft client) {
        CombatTimelineModule.sample(client, false);
    }

    private static void sample(Minecraft client, boolean serverRemoval) {
        ClientConfig config = BloodHelperClient.config();
        CombatTimelineSettings settings = config.combatTimeline;
        if (!config.deathRecap.enabled || !settings.enabled || client.player == null || client.level == null || client.getConnection() == null) {
            CombatTimelineModule.reset();
            return;
        }
        boolean alive = client.player.isAlive() && !client.player.isDeadOrDying();
        int nextOptions = settings.windowSeconds * 8 + (settings.showHealth ? 1 : 0) + (settings.showEffects ? 2 : 0) + (settings.showTotems ? 4 : 0);
        if (connection != client.getConnection() || player != client.player || level != client.level || profile != config || options != nextOptions || !wasAlive && alive) {
            CombatTimelineModule.reset();
            connection = client.getConnection();
            player = client.player;
            level = client.level;
            profile = config;
            options = nextOptions;
            if (alive) {
                RECORDER.begin(CombatTimelineModule.now(), client.player.getHealth(), client.player.getAbsorptionAmount(), settings.windowSeconds, settings.showHealth, settings.showEffects, settings.showTotems);
            }
        }
        wasAlive = alive;
        if (!RECORDER.active()) {
            return;
        }
        long now = CombatTimelineModule.now();
        RECORDER.health(now, client.player.getHealth(), client.player.getAbsorptionAmount());
        if (alive && settings.showEffects) {
            EFFECTS.clear();
            for (MobEffectInstance effect : client.player.getActiveEffects()) {
                EFFECTS.add(new CombatTimelineRecorder.EffectState(((MobEffect)effect.getEffect().value()).getDescriptionId(), effect.getAmplifier() + 1, effect.getDuration(), effect.isInfiniteDuration()));
            }
            RECORDER.effects(now, EFFECTS, serverRemoval);
            EFFECTS.clear();
        }
    }

    public static void effectsChanged(Minecraft client, boolean serverRemoval) {
        CombatTimelineModule.sample(client, serverRemoval);
    }

    public static void receiveStatus(Minecraft client, ClientboundEntityEventPacket packet) {
        if (packet.getEventId() != 35 || client.player == null || client.level == null || packet.getEntity((Level)client.level) != client.player) {
            return;
        }
        CombatTimelineModule.sample(client, false);
        if (player == client.player) {
            RECORDER.totem(CombatTimelineModule.now(), packet);
        }
    }

    public static List<CombatTimelineRecorder.Event> freezeForDeath(Object deadPlayer) {
        return deadPlayer == player ? RECORDER.freeze(CombatTimelineModule.now()) : List.of();
    }

    public static void reset() {
        if (player == null && profile == null) {
            return;
        }
        RECORDER.reset();
        EFFECTS.clear();
        connection = null;
        player = null;
        level = null;
        profile = null;
        options = 0;
        wasAlive = false;
    }

    private static long now() {
        return System.nanoTime() / 1000000L;
    }
}

