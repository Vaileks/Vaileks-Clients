/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package ru.bloodhelper.client.modules;

import net.minecraft.client.Minecraft;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.SizeTimerSettings;
import ru.bloodhelper.client.modules.SizeTimerState;

public final class SizeTimerModule {
    private static final SizeTimerState STATE = new SizeTimerState();
    private static Object player;
    private static Object world;

    private SizeTimerModule() {
    }

    public static void tick(Minecraft client) {
        SizeTimerSettings settings = BloodHelperClient.config().sizeTimer;
        if (!settings.enabled || client.player == null || client.level == null || !client.player.isAlive() || client.player.isSpectator()) {
            SizeTimerModule.reset();
            return;
        }
        if (world != client.level || player != client.player) {
            SizeTimerModule.reset();
            world = client.level;
            player = client.player;
        }
        STATE.observe(client.player.getScale(), client.player.tickCount, settings.enlargedSeconds, settings.reducedSeconds);
    }

    public static SizeTimerState.Snapshot snapshot() {
        Minecraft client = Minecraft.getInstance();
        if (!BloodHelperClient.config().sizeTimer.enabled || client.player != player || client.level != world || client.player == null || !client.player.isAlive() || client.player.isSpectator()) {
            return null;
        }
        return STATE.snapshot();
    }

    public static void reset() {
        STATE.reset();
        world = null;
        player = null;
    }
}

