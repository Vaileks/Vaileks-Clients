/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_640
 */
package ru.bloodhelper.client.hud;

import net.minecraft.class_310;
import net.minecraft.class_640;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.hud.FrameHistory;

public final class PerformanceTracker {
    private static final double[] NO_FRAMES = new double[0];
    private static final FrameHistory HISTORY = new FrameHistory(90);

    private PerformanceTracker() {
    }

    public static void frame() {
        class_310 mc = class_310.method_1551();
        if (!BloodHelperClient.config().performance.enabled || mc.field_1687 == null || mc.field_1724 == null || mc.method_1493() || mc.field_1755 != null) {
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
        class_640 entry;
        class_310 mc = class_310.method_1551();
        boolean active = mc.field_1687 != null && mc.field_1724 != null && !mc.method_1493() && mc.field_1755 == null;
        int ping = -1;
        if (mc.field_1724 != null && mc.method_1562() != null && (entry = mc.method_1562().method_2871(mc.field_1724.method_5667())) != null && entry.method_2959() >= 0) {
            ping = entry.method_2959();
        }
        return new Snapshot(active ? Math.max(0, mc.method_47599()) : -1, ping, active ? HISTORY.mean() : Double.NaN, active && includeGraph ? HISTORY.samples() : NO_FRAMES);
    }

    public record Snapshot(int fps, int pingMs, double frameTimeMs, double[] frames) {
    }
}

