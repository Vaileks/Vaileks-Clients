/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.PlayerInfo
 */
package ru.bloodhelper.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.hud.FrameHistory;

public final class PerformanceTracker {
    private static final double[] NO_FRAMES = new double[0];
    private static final FrameHistory HISTORY = new FrameHistory(90);

    private PerformanceTracker() {
    }

    public static void frame() {
        Minecraft mc = Minecraft.getInstance();
        if (!BloodHelperClient.config().performance.enabled || mc.level == null || mc.player == null || mc.isPaused() || mc.gui.screen() != null) {
            HISTORY.reset();
            return;
        }
        HISTORY.frame(System.nanoTime(), true);
    }

    public static void reset() {
        HISTORY.reset();
    }

    public static Snapshot snapshot() {
        return PerformanceTracker.snapshot(true);
    }

    public static Snapshot snapshot(boolean includeGraph) {
        PlayerInfo entry;
        Minecraft mc = Minecraft.getInstance();
        boolean active = mc.level != null && mc.player != null && !mc.isPaused() && mc.gui.screen() == null;
        int ping = -1;
        if (mc.player != null && mc.getConnection() != null && (entry = mc.getConnection().getPlayerInfo(mc.player.getUUID())) != null && entry.getLatency() >= 0) {
            ping = entry.getLatency();
        }
        return new Snapshot(active ? Math.max(0, mc.getFps()) : -1, ping, active ? HISTORY.mean() : Double.NaN, active && includeGraph ? HISTORY.samples() : NO_FRAMES);
    }

    public record Snapshot(int fps, int pingMs, double frameTimeMs, double[] frames) {
    }
}

