/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 */
package ru.bloodhelper.client.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
    private static Vec3 previous;
    private static double speed;

    private PlayerMetrics() {
    }

    public static void tick(Minecraft mc) {
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
        if (mc.level != world) {
            COMBAT.reset();
            previous = null;
            lastTick = 0L;
            world = mc.level;
        }
        if (mc.player == null) {
            return;
        }
        if (lastTick != 0L && !mc.isPaused()) {
            elapsed += Math.max(0L, now - lastTick);
        }
        lastTick = now;
        Vec3 pos = mc.player.position();
        speed = previous != null && !mc.isPaused() ? ((distance = Math.sqrt((dx = pos.x - PlayerMetrics.previous.x) * dx + (dz = pos.z - PlayerMetrics.previous.z) * dz)) > 8.0 ? 0.0 : distance * 20.0) : 0.0;
        previous = pos;
        if (mc.player.isDeadOrDying()) {
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
        Minecraft mc = Minecraft.getInstance();
        if (action != 1 || mc.player == null || mc.gui.screen() != null || !mc.isWindowActive()) {
            return;
        }
        if (button == 1) {
            LEFT.press(System.nanoTime());
        } else if (button == 3) {
            RIGHT.press(System.nanoTime());
        }
    }

    public static void attack(Entity target) {
        EntityHitResult hit;
        Minecraft mc = Minecraft.getInstance();
        ClientConfig c = BloodHelperClient.config();
        if (mc.player == null || !c.comboCounter.enabled) {
            return;
        }
        boolean isPlayer = target instanceof Player;
        if (!(isPlayer || c.comboCounter.enabled && !c.comboCounter.playersOnly)) {
            return;
        }
        Vec3 eye = mc.player.getEyePosition();
        HitResult hitResult = mc.hitResult;
        if (hitResult instanceof EntityHitResult && (hit = (EntityHitResult)hitResult).getEntity() == target) {
            long now = System.nanoTime();
            double distance = eye.distanceTo(hit.getLocation());
            if (c.comboCounter.enabled && (isPlayer || !c.comboCounter.playersOnly)) {
                COMBAT.attack(target.getId(), distance, now);
            }
        }
    }

    public static void damage(Entity victim, DamageSource source) {
        if (!ClientControlPolicy.enabled()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (victim == mc.player) {
            COMBAT.reset();
            return;
        }
        COMBAT.confirmed(victim.getId(), source.getEntity() == mc.player && source.getDirectEntity() == mc.player, System.nanoTime(), (long)BloodHelperClient.config().comboCounter.timeoutSeconds * 1000000000L);
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

