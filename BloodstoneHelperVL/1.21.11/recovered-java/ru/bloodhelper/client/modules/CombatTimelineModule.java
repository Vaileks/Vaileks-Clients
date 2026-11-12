/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1937
 *  net.minecraft.class_2663
 *  net.minecraft.class_310
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1937;
import net.minecraft.class_2663;
import net.minecraft.class_310;
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

    public static void tick(class_310 client) {
        CombatTimelineModule.sample(client, false);
    }

    private static void sample(class_310 client, boolean serverRemoval) {
        ClientConfig config = BloodHelperClient.config();
        CombatTimelineSettings settings = config.combatTimeline;
        if (!config.deathRecap.enabled || !settings.enabled || client.field_1724 == null || client.field_1687 == null || client.method_1562() == null) {
            CombatTimelineModule.reset();
            return;
        }
        boolean alive = client.field_1724.method_5805() && !client.field_1724.method_29504();
        int nextOptions = settings.windowSeconds * 8 + (settings.showHealth ? 1 : 0) + (settings.showEffects ? 2 : 0) + (settings.showTotems ? 4 : 0);
        if (connection != client.method_1562() || player != client.field_1724 || level != client.field_1687 || profile != config || options != nextOptions || !wasAlive && alive) {
            CombatTimelineModule.reset();
            connection = client.method_1562();
            player = client.field_1724;
            level = client.field_1687;
            profile = config;
            options = nextOptions;
            if (alive) {
                RECORDER.begin(CombatTimelineModule.now(), client.field_1724.method_6032(), client.field_1724.method_6067(), settings.windowSeconds, settings.showHealth, settings.showEffects, settings.showTotems);
            }
        }
        wasAlive = alive;
        if (!RECORDER.active()) {
            return;
        }
        long now = CombatTimelineModule.now();
        RECORDER.health(now, client.field_1724.method_6032(), client.field_1724.method_6067());
        if (alive && settings.showEffects) {
            EFFECTS.clear();
            for (class_1293 effect : client.field_1724.method_6026()) {
                EFFECTS.add(new CombatTimelineRecorder.EffectState(((class_1291)effect.method_5579().comp_349()).method_5567(), effect.method_5578() + 1, effect.method_5584(), effect.method_48559()));
            }
            RECORDER.effects(now, EFFECTS, serverRemoval);
            EFFECTS.clear();
        }
    }

    public static void effectsChanged(class_310 client, boolean serverRemoval) {
        CombatTimelineModule.sample(client, serverRemoval);
    }

    public static void receiveStatus(class_310 client, class_2663 packet) {
        if (packet.method_11470() != 35 || client.field_1724 == null || client.field_1687 == null || packet.method_11469((class_1937)client.field_1687) != client.field_1724) {
            return;
        }
        CombatTimelineModule.sample(client, false);
        if (player == client.field_1724) {
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

