/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1282
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_239
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3966
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.modules.ClickWindow;
import ru.bloodhelper.client.modules.CombatMetrics;
import ru.bloodhelper.client.policy.ClientControlPolicy;

public final class PlayerMetrics {
    public static final CombatMetrics COMBAT = new CombatMetrics();
    public static final ClickWindow LEFT = new ClickWindow();
    public static final ClickWindow RIGHT = new ClickWindow();
    private static Object world;
    private static long lastTick;
    private static long elapsed;
    private static class_243 previous;
    private static double speed;

    private PlayerMetrics() {
    }

    public static void tick(class_310 mc) {
        double dz;
        double dx;
        double distance;
        if (!ClientControlPolicy.enabled()) {
            PlayerMetrics.reset();
            return;
        }
        long now = System.nanoTime();
        if (!BloodHelperClient.config().comboCounter.enabled) {
            COMBAT.reset();
        }
        if (mc.field_1687 != world) {
            COMBAT.reset();
            previous = null;
            lastTick = 0L;
            world = mc.field_1687;
        }
        if (mc.field_1724 == null) {
            return;
        }
        if (lastTick != 0L && !mc.method_1493()) {
            elapsed += Math.max(0L, now - lastTick);
        }
        lastTick = now;
        class_243 pos = mc.field_1724.method_73189();
        speed = previous != null && !mc.method_1493() ? ((distance = Math.sqrt((dx = pos.field_1352 - PlayerMetrics.previous.field_1352) * dx + (dz = pos.field_1350 - PlayerMetrics.previous.field_1350) * dz)) > 8.0 ? 0.0 : distance * 20.0) : 0.0;
        previous = pos;
        if (mc.field_1724.method_29504()) {
            COMBAT.reset();
        }
    }

    public static long seconds() {
        return elapsed / 1000000000L;
    }

    public static double speed() {
        return speed;
    }

    public static void click(int button, int action) {
        if (!ClientControlPolicy.enabled()) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (action != 1 || mc.field_1724 == null || mc.field_1755 != null || !mc.method_1569()) {
            return;
        }
        if (button == 0) {
            LEFT.press(System.nanoTime());
        } else if (button == 1) {
            RIGHT.press(System.nanoTime());
        }
    }

    public static void attack(class_1297 target) {
        class_3966 hit;
        class_310 mc = class_310.method_1551();
        ClientConfig c = BloodHelperClient.config();
        if (mc.field_1724 == null || !c.comboCounter.enabled) {
            return;
        }
        boolean isPlayer = target instanceof class_1657;
        if (!(isPlayer || c.comboCounter.enabled && !c.comboCounter.playersOnly)) {
            return;
        }
        class_243 eye = mc.field_1724.method_33571();
        class_239 class_2392 = mc.field_1765;
        if (class_2392 instanceof class_3966 && (hit = (class_3966)class_2392).method_17782() == target) {
            long now = System.nanoTime();
            double distance = eye.method_1022(hit.method_17784());
            if (c.comboCounter.enabled && (isPlayer || !c.comboCounter.playersOnly)) {
                COMBAT.attack(target.method_5628(), distance, now);
            }
        }
    }

    public static void damage(class_1297 victim, class_1282 source) {
        if (!ClientControlPolicy.enabled()) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        if (victim == mc.field_1724) {
            COMBAT.reset();
            return;
        }
        COMBAT.confirmed(victim.method_5628(), source.method_5529() == mc.field_1724 && source.method_5526() == mc.field_1724, System.nanoTime(), (long)BloodHelperClient.config().comboCounter.timeoutSeconds * 1000000000L);
    }

    public static void reset() {
        COMBAT.reset();
        LEFT.reset();
        RIGHT.reset();
        previous = null;
        elapsed = 0L;
        lastTick = 0L;
        speed = 0.0;
        world = null;
    }
}

