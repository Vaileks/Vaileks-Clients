/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_310;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.SizeTimerSettings;
import ru.bloodhelper.client.modules.SizeTimerState;

public final class SizeTimerModule {
    private static final SizeTimerState STATE = new SizeTimerState();
    private static Object player;
    private static Object world;

    private SizeTimerModule() {
    }

    public static void tick(class_310 client) {
        SizeTimerSettings settings = BloodHelperClient.config().sizeTimer;
        if (!settings.enabled || client.field_1724 == null || client.field_1687 == null || !client.field_1724.method_5805() || client.field_1724.method_7325()) {
            SizeTimerModule.reset();
            return;
        }
        if (world != client.field_1687 || player != client.field_1724) {
            SizeTimerModule.reset();
            world = client.field_1687;
            player = client.field_1724;
        }
        STATE.observe(client.field_1724.method_55693(), client.field_1724.field_6012, settings.enlargedSeconds, settings.reducedSeconds);
    }

    public static SizeTimerState.Snapshot snapshot() {
        class_310 client = class_310.method_1551();
        if (!BloodHelperClient.config().sizeTimer.enabled || client.field_1724 != player || client.field_1687 != world || client.field_1724 == null || !client.field_1724.method_5805() || client.field_1724.method_7325()) {
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

