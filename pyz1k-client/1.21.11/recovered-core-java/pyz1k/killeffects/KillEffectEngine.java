/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1299
 *  net.minecraft.class_1657
 *  net.minecraft.class_1676
 *  net.minecraft.class_1802
 *  net.minecraft.class_243
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3419
 *  net.minecraft.class_3965
 *  net.minecraft.class_4587
 *  net.minecraft.class_5819
 *  net.minecraft.class_638
 *  net.minecraft.class_742
 *  net.minecraft.class_7833
 *  org.joml.Quaternionfc
 */
package pyz1k.killeffects;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_1676;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.minecraft.class_5819;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;
import pyz1k.Pyz1k;
import pyz1k.killeffects.BloodEffectModel;
import pyz1k.module.impl.visuals.KillEffects;

public final class KillEffectEngine {
    private static final List<Active> ACTIVE = new ArrayList<Active>();
    private static final String[] NAMES = new String[]{"blood_1", "blood_2", "orbit", "trapdoor", "train", "red_bomb", "tiny_copper_golem", "copper_golem_slap", "robot_terminator", "plant_kill_effect"};
    private static final float[] DURATIONS = new float[]{3.1f, 2.5f, 4.3f, 3.0f, 1.4f, 3.0f, 3.0f, 2.0f, 1.75f, 3.0f};
    private static final BloodEffectModel[] MODELS = new BloodEffectModel[NAMES.length];
    private static final Map<Integer, Pending> TARGETS = new HashMap<Integer, Pending>();
    private static final Map<Integer, Long> PROCESSED = new HashMap<Integer, Long>();
    private static final ArrayDeque<ExplosiveUse> EXPLOSIVE_USES = new ArrayDeque();
    private static final ArrayDeque<RecentExplosion> EXPLOSIONS = new ArrayDeque();
    private static final ArrayDeque<KillRecord> KILLS = new ArrayDeque();
    private static final ArrayDeque<KillRecord> OTHER_KILLS = new ArrayDeque();
    private static class_638 trackedLevel;
    private static int spawnCount;

    private KillEffectEngine() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(KillEffectEngine::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(KillEffectEngine::render);
    }

    public static void recordAttack(class_1297 target) {
        class_1657 player;
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || target == null) {
            return;
        }
        KillEffectEngine.ensureLevel(mc.field_1687);
        if (target.method_5864() == class_1299.field_6053) {
            KillEffectEngine.markMinecart(target);
            return;
        }
        if (!(target instanceof class_1657) || (player = (class_1657)target) == mc.field_1724) {
            return;
        }
        TARGETS.put(player.method_5628(), new Pending(player, System.currentTimeMillis()));
    }

    public static void recordDamage(int victimId, int causingId, int directId) {
        class_1676 projectile;
        class_1657 player;
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        KillEffectEngine.ensureLevel(mc.field_1687);
        class_1297 victim = mc.field_1687.method_8469(victimId);
        class_1297 cause = mc.field_1687.method_8469(causingId);
        class_1297 direct = mc.field_1687.method_8469(directId);
        if (victim != null && victim.method_5864() == class_1299.field_6053) {
            class_1676 projectile2;
            if (cause == mc.field_1724 || direct == mc.field_1724 || direct instanceof class_1676 && (projectile2 = (class_1676)direct).method_24921() == mc.field_1724) {
                KillEffectEngine.markMinecart(victim);
            }
            return;
        }
        if (!(victim instanceof class_1657) || (player = (class_1657)victim) == mc.field_1724) {
            return;
        }
        if (cause == mc.field_1724 || direct == mc.field_1724 || direct instanceof class_1676 && (projectile = (class_1676)direct).method_24921() == mc.field_1724 || KillEffectEngine.isTrackedMinecart(causingId) || KillEffectEngine.isTrackedMinecart(directId)) {
            TARGETS.put(victimId, new Pending(player, System.currentTimeMillis()));
        } else if (cause instanceof class_1657) {
            TARGETS.remove(victimId);
        }
    }

    public static void recordExplosiveUse(class_1657 player, class_1268 hand, class_3965 hit) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || player != mc.field_1724 || !KillEffectEngine.isEnabled()) {
            return;
        }
        if (!(player.method_5998(hand).method_31574(class_1802.field_8069) || player.method_5998(hand).method_31574(class_1802.field_8626) || player.method_5998(hand).method_31574(class_1802.field_8301))) {
            return;
        }
        KillEffectEngine.ensureLevel(mc.field_1687);
        if (EXPLOSIVE_USES.size() >= 12) {
            EXPLOSIVE_USES.removeFirst();
        }
        EXPLOSIVE_USES.addLast(new ExplosiveUse(hit.method_17784(), System.currentTimeMillis(), player.method_5998(hand).method_31574(class_1802.field_8069), player.method_5998(hand).method_31574(class_1802.field_8069) ? 60000L : 15000L));
    }

    public static void recordExplosion(class_243 center, float radius) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || !KillEffectEngine.isEnabled()) {
            return;
        }
        KillEffectEngine.ensureLevel(mc.field_1687);
        long now = System.currentTimeMillis();
        Iterator<ExplosiveUse> iterator = EXPLOSIVE_USES.iterator();
        while (iterator.hasNext()) {
            ExplosiveUse use = iterator.next();
            if (now - use.time > use.maxAge || !(use.position.method_1025(center) <= 16.0)) continue;
            if (EXPLOSIONS.size() >= 8) {
                EXPLOSIONS.removeFirst();
            }
            EXPLOSIONS.addLast(new RecentExplosion(center, Math.max(5.0f, Math.min(8.0f, radius + 2.0f)), now));
            iterator.remove();
            break;
        }
    }

    public static void recordDeath(class_1297 entity) {
        class_1657 player;
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || !(entity instanceof class_1657) || (player = (class_1657)entity) == mc.field_1724) {
            return;
        }
        KillEffectEngine.ensureLevel(mc.field_1687);
        long now = System.currentTimeMillis();
        KillEffectEngine.trySpawn(mc, player, now);
        if (!PROCESSED.containsKey(player.method_5628())) {
            OTHER_KILLS.addLast(new KillRecord(player.method_73189(), now));
        }
    }

    private static boolean isEnabled() {
        Pyz1k client = Pyz1k.getInstance();
        KillEffects module = client == null ? null : client.getModuleManager().getModule(KillEffects.class);
        return module != null && module.isEnable();
    }

    private static boolean isTrackedMinecart(int id) {
        if (id < 0) {
            return false;
        }
        for (ExplosiveUse use : EXPLOSIVE_USES) {
            if (!use.minecart || use.entityId != id) continue;
            return true;
        }
        return false;
    }

    private static void markMinecart(class_1297 cart) {
        if (KillEffectEngine.isTrackedMinecart(cart.method_5628())) {
            return;
        }
        if (EXPLOSIVE_USES.size() >= 12) {
            EXPLOSIVE_USES.removeFirst();
        }
        ExplosiveUse use = new ExplosiveUse(cart.method_73189(), System.currentTimeMillis(), true, 15000L);
        use.entityId = cart.method_5628();
        EXPLOSIVE_USES.addLast(use);
    }

    private static void updateMinecarts(class_638 level, long now) {
        for (ExplosiveUse use : EXPLOSIVE_USES) {
            class_1297 cart;
            if (!use.minecart) continue;
            class_1297 class_12972 = cart = use.entityId < 0 ? null : level.method_8469(use.entityId);
            if (cart == null && use.entityId < 0 && now - use.time < 3000L) {
                for (class_1297 candidate : level.method_18112()) {
                    if (candidate.method_5864() != class_1299.field_6053 || !(candidate.method_73189().method_1025(use.position) < 9.0)) continue;
                    cart = candidate;
                    use.entityId = candidate.method_5628();
                    break;
                }
            }
            if (cart == null) continue;
            use.position = cart.method_73189();
        }
    }

    private static void ensureLevel(class_638 level) {
        if (trackedLevel == level) {
            return;
        }
        trackedLevel = level;
        TARGETS.clear();
        PROCESSED.clear();
        EXPLOSIVE_USES.clear();
        EXPLOSIONS.clear();
        KILLS.clear();
        OTHER_KILLS.clear();
        ACTIVE.clear();
    }

    private static void tick(class_310 mc) {
        if (mc.field_1687 == null || !KillEffectEngine.isEnabled()) {
            KillEffectEngine.ensureLevel(null);
            return;
        }
        KillEffectEngine.ensureLevel(mc.field_1687);
        long now = System.currentTimeMillis();
        ACTIVE.removeIf(active -> now - active.startedAt > active.durationMs + 200L);
        TARGETS.values().removeIf(target -> now - target.time > 5000L);
        PROCESSED.entrySet().removeIf(entry -> {
            class_1657 player;
            class_1297 patt0$temp;
            return now - (Long)entry.getValue() > 20000L || now - (Long)entry.getValue() > 1500L && (patt0$temp = mc.field_1687.method_8469(((Integer)entry.getKey()).intValue())) instanceof class_1657 && !(player = (class_1657)patt0$temp).method_29504() && player.method_6032() > 0.0f;
        });
        EXPLOSIVE_USES.removeIf(use -> now - use.time > use.maxAge);
        KillEffectEngine.updateMinecarts(mc.field_1687, now);
        EXPLOSIONS.removeIf(explosion -> now - explosion.time > 2500L);
        while (!KILLS.isEmpty() && now - KillEffectEngine.KILLS.peekFirst().time > 6500L) {
            KILLS.removeFirst();
        }
        while (!OTHER_KILLS.isEmpty() && now - KillEffectEngine.OTHER_KILLS.peekFirst().time > 6500L) {
            OTHER_KILLS.removeFirst();
        }
        for (Pending target2 : new ArrayList<Pending>(TARGETS.values())) {
            if (!target2.player.method_29504() && !(target2.player.method_6032() <= 0.0f)) continue;
            KillEffectEngine.trySpawn(mc, target2.player, now);
        }
        if (!EXPLOSIONS.isEmpty()) {
            for (class_742 player : mc.field_1687.method_18456()) {
                if (player == mc.field_1724 || !player.method_29504() && !(player.method_6032() <= 0.0f)) continue;
                KillEffectEngine.trySpawn(mc, (class_1657)player, now);
            }
        }
    }

    private static void trySpawn(class_310 mc, class_1657 player, long now) {
        class_2960 class_29602;
        if (!KillEffectEngine.isEnabled() || PROCESSED.containsKey(player.method_5628())) {
            return;
        }
        Pending target = TARGETS.get(player.method_5628());
        boolean direct = target != null && target.player == player && now - target.time <= 5000L;
        boolean explosive = false;
        if (!direct) {
            for (RecentExplosion explosion : EXPLOSIONS) {
                if (now - explosion.time > 2500L || !(player.method_73189().method_1025(explosion.position) <= (double)(explosion.radius * explosion.radius))) continue;
                explosive = true;
                break;
            }
        }
        if (!direct && !explosive) {
            return;
        }
        KillEffects module = Pyz1k.getInstance().getModuleManager().getModule(KillEffects.class);
        if (module == null || !module.isEnable()) {
            return;
        }
        int variant = module.variant(spawnCount++);
        BloodEffectModel model = KillEffectEngine.model(variant);
        class_243 location = player.method_73189();
        if (player instanceof class_742) {
            class_742 clientPlayer = (class_742)player;
            class_29602 = clientPlayer.method_52814().comp_1626().comp_3627();
        } else {
            class_29602 = null;
        }
        class_2960 victimSkin = class_29602;
        ACTIVE.add(new Active(location, player.method_36454(), now, Math.round(model.duration() * 1000.0f), variant, module.scale(), victimSkin));
        if (ACTIVE.size() > 8) {
            ACTIVE.remove(0);
        }
        KILLS.addLast(new KillRecord(location, now));
        OTHER_KILLS.removeIf(kill -> kill.position.method_1025(location) <= 1.0);
        PROCESSED.put(player.method_5628(), now);
        TARGETS.remove(player.method_5628());
        if (variant != 0) {
            String sound = variant == 1 ? "bats" : NAMES[variant];
            mc.method_1483().method_4873((class_1113)new class_1109(class_3414.method_47908((class_2960)class_2960.method_60655((String)"pyz1k", (String)("killeffects." + sound))), class_3419.field_15248, 1.0f, 1.0f, class_5819.method_43047(), location.field_1352, location.field_1351, location.field_1350));
        }
    }

    private static BloodEffectModel model(int variant) {
        if (MODELS[variant] == null) {
            KillEffectEngine.MODELS[variant] = BloodEffectModel.load(NAMES[variant], DURATIONS[variant]);
        }
        return MODELS[variant];
    }

    private static void render(WorldRenderContext context) {
        if (ACTIVE.isEmpty() || class_310.method_1551().field_1687 != trackedLevel) {
            return;
        }
        class_243 camera = context.worldState().field_63082.field_63078;
        long now = System.currentTimeMillis();
        for (Active effect : ACTIVE) {
            float time = Math.min((float)effect.durationMs / 1000.0f, (float)(now - effect.startedAt) / 1000.0f);
            if (time < 0.0f || time > (float)effect.durationMs / 1000.0f) continue;
            class_4587 pose = new class_4587();
            pose.method_22904(effect.position.field_1352 - camera.field_1352, effect.position.field_1351 - camera.field_1351, effect.position.field_1350 - camera.field_1350);
            pose.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(180.0f - effect.yaw));
            KillEffectEngine.model(effect.variant).submit(pose, context.commandQueue(), time, effect.scale, effect.victimSkin);
        }
    }

    public static boolean nearRecentKill(double x, double y, double z) {
        return KillEffectEngine.nearKill(x, y, z, 6500L, 64.0);
    }

    public static boolean nearConfirmedKill(double x, double y, double z) {
        return KillEffectEngine.nearKill(x, y, z, 6500L, 16.0);
    }

    public static boolean nearOtherKill(double x, double y, double z, double distanceSq) {
        if (trackedLevel == null || trackedLevel != class_310.method_1551().field_1687 || KillEffectEngine.nearKill(x, y, z, 6500L, distanceSq)) {
            return false;
        }
        long now = System.currentTimeMillis();
        for (KillRecord kill : OTHER_KILLS) {
            if (now - kill.time > 6500L || !(kill.position.method_1028(x, y, z) <= distanceSq)) continue;
            return true;
        }
        return false;
    }

    private static boolean nearKill(double x, double y, double z, long duration, double distanceSq) {
        if (trackedLevel == null || trackedLevel != class_310.method_1551().field_1687) {
            return false;
        }
        long now = System.currentTimeMillis();
        for (KillRecord kill : KILLS) {
            if (now - kill.time > duration || !(kill.position.method_1028(x, y, z) <= distanceSq)) continue;
            return true;
        }
        return false;
    }

    private record Pending(class_1657 player, long time) {
    }

    private static final class ExplosiveUse {
        class_243 position;
        final long time;
        final boolean minecart;
        final long maxAge;
        int entityId = -1;

        ExplosiveUse(class_243 position, long time, boolean minecart, long maxAge) {
            this.position = position;
            this.time = time;
            this.minecart = minecart;
            this.maxAge = maxAge;
        }
    }

    private record RecentExplosion(class_243 position, float radius, long time) {
    }

    private record KillRecord(class_243 position, long time) {
    }

    private record Active(class_243 position, float yaw, long startedAt, long durationMs, int variant, float scale, class_2960 victimSkin) {
    }
}

