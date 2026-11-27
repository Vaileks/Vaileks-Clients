/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 */
package pyz1k.killeffects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
    private static ClientLevel trackedLevel;
    private static int spawnCount;

    private KillEffectEngine() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(KillEffectEngine::tick);
        LevelRenderEvents.COLLECT_SUBMITS.register(KillEffectEngine::render);
    }

    public static void recordAttack(Entity target) {
        Player player;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || target == null) {
            return;
        }
        KillEffectEngine.ensureLevel(mc.level);
        if (target.getType() == net.minecraft.world.entity.EntityTypes.TNT_MINECART) {
            KillEffectEngine.markMinecart(target);
            return;
        }
        if (!(target instanceof Player) || (player = (Player)target) == mc.player) {
            return;
        }
        TARGETS.put(player.getId(), new Pending(player, System.currentTimeMillis()));
    }

    public static void recordDamage(int victimId, int causingId, int directId) {
        Projectile projectile;
        Player player;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        KillEffectEngine.ensureLevel(mc.level);
        Entity victim = mc.level.getEntity(victimId);
        Entity cause = mc.level.getEntity(causingId);
        Entity direct = mc.level.getEntity(directId);
        if (victim != null && victim.getType() == net.minecraft.world.entity.EntityTypes.TNT_MINECART) {
            Projectile projectile2;
            if (cause == mc.player || direct == mc.player || direct instanceof Projectile && (projectile2 = (Projectile)direct).getOwner() == mc.player) {
                KillEffectEngine.markMinecart(victim);
            }
            return;
        }
        if (!(victim instanceof Player) || (player = (Player)victim) == mc.player) {
            return;
        }
        if (cause == mc.player || direct == mc.player || direct instanceof Projectile && (projectile = (Projectile)direct).getOwner() == mc.player || KillEffectEngine.isTrackedMinecart(causingId) || KillEffectEngine.isTrackedMinecart(directId)) {
            TARGETS.put(victimId, new Pending(player, System.currentTimeMillis()));
        } else if (cause instanceof Player) {
            TARGETS.remove(victimId);
        }
    }

    public static void recordExplosiveUse(Player player, InteractionHand hand, BlockHitResult hit) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || player != mc.player || !KillEffectEngine.isEnabled()) {
            return;
        }
        if (!(player.getItemInHand(hand).is(Items.TNT_MINECART) || player.getItemInHand(hand).is(Items.TNT) || player.getItemInHand(hand).is(Items.END_CRYSTAL))) {
            return;
        }
        KillEffectEngine.ensureLevel(mc.level);
        if (EXPLOSIVE_USES.size() >= 12) {
            EXPLOSIVE_USES.removeFirst();
        }
        EXPLOSIVE_USES.addLast(new ExplosiveUse(hit.getLocation(), System.currentTimeMillis(), player.getItemInHand(hand).is(Items.TNT_MINECART), player.getItemInHand(hand).is(Items.TNT_MINECART) ? 60000L : 15000L));
    }

    public static void recordExplosion(Vec3 center, float radius) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !KillEffectEngine.isEnabled()) {
            return;
        }
        KillEffectEngine.ensureLevel(mc.level);
        long now = System.currentTimeMillis();
        Iterator<ExplosiveUse> iterator = EXPLOSIVE_USES.iterator();
        while (iterator.hasNext()) {
            ExplosiveUse use = iterator.next();
            if (now - use.time > use.maxAge || !(use.position.distanceToSqr(center) <= 16.0)) continue;
            if (EXPLOSIONS.size() >= 8) {
                EXPLOSIONS.removeFirst();
            }
            EXPLOSIONS.addLast(new RecentExplosion(center, Math.max(5.0f, Math.min(8.0f, radius + 2.0f)), now));
            iterator.remove();
            break;
        }
    }

    public static void recordDeath(Entity entity) {
        Player player;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !(entity instanceof Player) || (player = (Player)entity) == mc.player) {
            return;
        }
        KillEffectEngine.ensureLevel(mc.level);
        long now = System.currentTimeMillis();
        KillEffectEngine.trySpawn(mc, player, now);
        if (!PROCESSED.containsKey(player.getId())) {
            OTHER_KILLS.addLast(new KillRecord(player.position(), now));
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

    private static void markMinecart(Entity cart) {
        if (KillEffectEngine.isTrackedMinecart(cart.getId())) {
            return;
        }
        if (EXPLOSIVE_USES.size() >= 12) {
            EXPLOSIVE_USES.removeFirst();
        }
        ExplosiveUse use = new ExplosiveUse(cart.position(), System.currentTimeMillis(), true, 15000L);
        use.entityId = cart.getId();
        EXPLOSIVE_USES.addLast(use);
    }

    private static void updateMinecarts(ClientLevel level, long now) {
        for (ExplosiveUse use : EXPLOSIVE_USES) {
            Entity cart;
            if (!use.minecart) continue;
            Entity entity = cart = use.entityId < 0 ? null : level.getEntity(use.entityId);
            if (cart == null && use.entityId < 0 && now - use.time < 3000L) {
                for (Entity candidate : level.entitiesForRendering()) {
                    if (candidate.getType() != net.minecraft.world.entity.EntityTypes.TNT_MINECART || !(candidate.position().distanceToSqr(use.position) < 9.0)) continue;
                    cart = candidate;
                    use.entityId = candidate.getId();
                    break;
                }
            }
            if (cart == null) continue;
            use.position = cart.position();
        }
    }

    private static void ensureLevel(ClientLevel level) {
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

    private static void tick(Minecraft mc) {
        if (mc.level == null || !KillEffectEngine.isEnabled()) {
            KillEffectEngine.ensureLevel(null);
            return;
        }
        KillEffectEngine.ensureLevel(mc.level);
        long now = System.currentTimeMillis();
        ACTIVE.removeIf(active -> now - active.startedAt > active.durationMs + 200L);
        TARGETS.values().removeIf(target -> now - target.time > 5000L);
        PROCESSED.entrySet().removeIf(entry -> {
            Player player;
            Entity patt0$temp;
            return now - (Long)entry.getValue() > 20000L || now - (Long)entry.getValue() > 1500L && (patt0$temp = mc.level.getEntity(((Integer)entry.getKey()).intValue())) instanceof Player && !(player = (Player)patt0$temp).isDeadOrDying() && player.getHealth() > 0.0f;
        });
        EXPLOSIVE_USES.removeIf(use -> now - use.time > use.maxAge);
        KillEffectEngine.updateMinecarts(mc.level, now);
        EXPLOSIONS.removeIf(explosion -> now - explosion.time > 2500L);
        while (!KILLS.isEmpty() && now - KillEffectEngine.KILLS.peekFirst().time > 6500L) {
            KILLS.removeFirst();
        }
        while (!OTHER_KILLS.isEmpty() && now - KillEffectEngine.OTHER_KILLS.peekFirst().time > 6500L) {
            OTHER_KILLS.removeFirst();
        }
        for (Pending target2 : new ArrayList<Pending>(TARGETS.values())) {
            if (!target2.player.isDeadOrDying() && !(target2.player.getHealth() <= 0.0f)) continue;
            KillEffectEngine.trySpawn(mc, target2.player, now);
        }
        if (!EXPLOSIONS.isEmpty()) {
            for (AbstractClientPlayer player : mc.level.players()) {
                if (player == mc.player || !player.isDeadOrDying() && !(player.getHealth() <= 0.0f)) continue;
                KillEffectEngine.trySpawn(mc, (Player)player, now);
            }
        }
    }

    private static void trySpawn(Minecraft mc, Player player, long now) {
        Identifier identifier;
        if (!KillEffectEngine.isEnabled() || PROCESSED.containsKey(player.getId())) {
            return;
        }
        Pending target = TARGETS.get(player.getId());
        boolean direct = target != null && target.player == player && now - target.time <= 5000L;
        boolean explosive = false;
        if (!direct) {
            for (RecentExplosion explosion : EXPLOSIONS) {
                if (now - explosion.time > 2500L || !(player.position().distanceToSqr(explosion.position) <= (double)(explosion.radius * explosion.radius))) continue;
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
        Vec3 location = player.position();
        if (player instanceof AbstractClientPlayer) {
            AbstractClientPlayer clientPlayer = (AbstractClientPlayer)player;
            identifier = clientPlayer.getSkin().body().texturePath();
        } else {
            identifier = null;
        }
        Identifier victimSkin = identifier;
        ACTIVE.add(new Active(location, player.getYRot(), now, Math.round(model.duration() * 1000.0f), variant, module.scale(), victimSkin));
        if (ACTIVE.size() > 8) {
            ACTIVE.remove(0);
        }
        KILLS.addLast(new KillRecord(location, now));
        OTHER_KILLS.removeIf(kill -> kill.position.distanceToSqr(location) <= 1.0);
        PROCESSED.put(player.getId(), now);
        TARGETS.remove(player.getId());
        if (variant != 0) {
            String sound = variant == 1 ? "bats" : NAMES[variant];
            mc.getSoundManager().play((SoundInstance)new SimpleSoundInstance(SoundEvent.createVariableRangeEvent((Identifier)Identifier.fromNamespaceAndPath((String)"pyz1k", (String)("killeffects." + sound))), SoundSource.PLAYERS, 1.0f, 1.0f, RandomSource.create(), location.x, location.y, location.z));
        }
    }

    private static BloodEffectModel model(int variant) {
        if (MODELS[variant] == null) {
            KillEffectEngine.MODELS[variant] = BloodEffectModel.load(NAMES[variant], DURATIONS[variant]);
        }
        return MODELS[variant];
    }

    private static void render(LevelRenderContext context) {
        if (ACTIVE.isEmpty() || Minecraft.getInstance().level != trackedLevel) {
            return;
        }
        Vec3 camera = context.levelState().cameraRenderState.pos;
        long now = System.currentTimeMillis();
        for (Active effect : ACTIVE) {
            float time = Math.min((float)effect.durationMs / 1000.0f, (float)(now - effect.startedAt) / 1000.0f);
            if (time < 0.0f || time > (float)effect.durationMs / 1000.0f) continue;
            PoseStack pose = new PoseStack();
            pose.translate(effect.position.x - camera.x, effect.position.y - camera.y, effect.position.z - camera.z);
            pose.rotate((Quaternionfc)Axis.YP.rotationDegrees(180.0f - effect.yaw));
            KillEffectEngine.model(effect.variant).submit(pose, context.submitNodeCollector(), time, effect.scale, effect.victimSkin);
        }
    }

    public static boolean nearRecentKill(double x, double y, double z) {
        return KillEffectEngine.nearKill(x, y, z, 6500L, 64.0);
    }

    public static boolean nearConfirmedKill(double x, double y, double z) {
        return KillEffectEngine.nearKill(x, y, z, 6500L, 16.0);
    }

    public static boolean nearOtherKill(double x, double y, double z, double distanceSq) {
        if (trackedLevel == null || trackedLevel != Minecraft.getInstance().level || KillEffectEngine.nearKill(x, y, z, 6500L, distanceSq)) {
            return false;
        }
        long now = System.currentTimeMillis();
        for (KillRecord kill : OTHER_KILLS) {
            if (now - kill.time > 6500L || !(kill.position.distanceToSqr(x, y, z) <= distanceSq)) continue;
            return true;
        }
        return false;
    }

    private static boolean nearKill(double x, double y, double z, long duration, double distanceSq) {
        if (trackedLevel == null || trackedLevel != Minecraft.getInstance().level) {
            return false;
        }
        long now = System.currentTimeMillis();
        for (KillRecord kill : KILLS) {
            if (now - kill.time > duration || !(kill.position.distanceToSqr(x, y, z) <= distanceSq)) continue;
            return true;
        }
        return false;
    }

    private record Pending(Player player, long time) {
    }

    private static final class ExplosiveUse {
        Vec3 position;
        final long time;
        final boolean minecart;
        final long maxAge;
        int entityId = -1;

        ExplosiveUse(Vec3 position, long time, boolean minecart, long maxAge) {
            this.position = position;
            this.time = time;
            this.minecart = minecart;
            this.maxAge = maxAge;
        }
    }

    private record RecentExplosion(Vec3 position, float radius, long time) {
    }

    private record KillRecord(Vec3 position, long time) {
    }

    private record Active(Vec3 position, float yaw, long startedAt, long durationMs, int variant, float scale, Identifier victimSkin) {
    }
}

