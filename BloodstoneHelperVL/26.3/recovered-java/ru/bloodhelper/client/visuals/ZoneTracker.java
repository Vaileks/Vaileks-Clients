/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package ru.bloodhelper.client.visuals;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.LegendaryVisualSettings;
import ru.bloodhelper.client.mixin.CooldownInstanceAccessor;
import ru.bloodhelper.client.mixin.ItemCooldownsAccessor;
import ru.bloodhelper.client.modules.LegendarySizeModule;

public final class ZoneTracker {
    public static final List<Zone> ZONES = new ArrayList<Zone>();
    public static final long FADE_MS = 700L;
    private static final String STAFF = "\u043f\u043e\u0441\u043e\u0445 \u0436\u0438\u0442\u0435\u043b\u044f";
    private static final String SCULK_BOW = "\u0441\u043a\u0430\u043b\u043a\u043e\u0432\u044b\u0439 \u0430\u0440\u0431\u0430\u043b\u0435\u0442";
    private static final long OTHERS_MAX_MS = 60000L;
    private static final Set<BlockPos> FRESH_SCULK = new LinkedHashSet<BlockPos>();
    private static long freshAt;
    private static float staffCdBefore;
    private static float bowCdBefore;
    private static long bowUsedAt;
    private static boolean initialized;

    public static void reset() {
        ZONES.clear();
        FRESH_SCULK.clear();
        bowCdBefore = 0.0f;
        staffCdBefore = 0.0f;
        freshAt = 0L;
        bowUsedAt = 0L;
        initialized = false;
    }

    public static void onBlock(BlockPos pos, BlockState state) {
        boolean sculk;
        Minecraft mc = Minecraft.getInstance();
        if (!BloodHelperClient.config().legendaryVisualsEnabled() || !ZoneTracker.settings().trails || !ZoneTracker.settings().sculk || mc.player == null || pos.distToCenterSqr((Position)mc.player.position()) > ZoneTracker.settings().distance * ZoneTracker.settings().distance) {
            return;
        }
        boolean bl = sculk = state.is((Object)Blocks.SCULK) || state.is((Object)Blocks.SCULK_VEIN);
        if (sculk) {
            if (FRESH_SCULK.size() < 400) {
                FRESH_SCULK.add(pos.immutable());
            }
            freshAt = System.nanoTime() / 1000000L;
        } else {
            FRESH_SCULK.remove(pos);
            for (Zone z : ZONES) {
                z.blocks.remove(pos);
            }
        }
    }

    public static void tick(Minecraft mc) {
        if (!BloodHelperClient.config().legendaryVisualsEnabled() || !ZoneTracker.settings().trails || mc.level == null || mc.player == null) {
            ZoneTracker.reset();
            return;
        }
        long now = System.nanoTime() / 1000000L;
        ZONES.removeIf(zx -> zx.fadeAt != 0L && now - zx.fadeAt > 700L);
        if (mc.level != null && mc.player != null) {
            Vec3 hit;
            boolean on = BloodHelperClient.config().legendaryVisualsEnabled() && ZoneTracker.settings().trails;
            float staffCd = ZoneTracker.cooldownLeft(mc, STAFF);
            float bowCd = ZoneTracker.cooldownLeft(mc, SCULK_BOW);
            if (!initialized) {
                staffCdBefore = staffCd;
                bowCdBefore = bowCd;
                initialized = true;
            }
            if (on && ZoneTracker.settings().staff && ZoneTracker.settings().self && staffCd > 0.0f && staffCdBefore <= 0.0f && (hit = ZoneTracker.aim(mc, 120.0)) != null) {
                ZoneTracker.add(new Zone(Kind.STAFF, true, mc.player.getName().getString(), hit, 1.6));
            }
            if (bowCd > 0.0f && bowCdBefore <= 0.0f) {
                bowUsedAt = now;
                if (on && ZoneTracker.settings().sculk && ZoneTracker.settings().self) {
                    Vec3 eye = mc.player.getEyePosition();
                    Vec3 hit2 = ZoneTracker.aim(mc, 80.0);
                    Vec3 end = hit2 != null ? hit2 : eye.add(mc.player.getLookAngle().scale(40.0));
                    Zone z = new Zone(Kind.SCULK, true, mc.player.getName().getString(), end, 1.0);
                    z.from = eye.add(0.0, -0.25, 0.0);
                    ZoneTracker.add(z);
                }
            }
            staffCdBefore = staffCd;
            bowCdBefore = bowCd;
            if (!(FRESH_SCULK.isEmpty() || now - freshAt <= 250L && FRESH_SCULK.size() < 300)) {
                if (on && ZoneTracker.settings().sculk) {
                    ZoneTracker.groupSculk(mc, now);
                }
                FRESH_SCULK.clear();
            }
            for (Zone z : ZONES) {
                if (z.fadeAt != 0L) continue;
                long age = now - z.start;
                if (z.self) {
                    if (z.secondsLeft() <= 0.0f && age > 1500L) {
                        z.fadeAt = now;
                    }
                } else if (z.hadBlocks && z.blocks.isEmpty() || age > 60000L) {
                    z.fadeAt = now;
                }
                if (on) continue;
                z.fadeAt = now;
            }
        } else {
            ZONES.clear();
            FRESH_SCULK.clear();
        }
    }

    private static void groupSculk(Minecraft mc, long now) {
        HashSet<BlockPos> remaining = new HashSet<BlockPos>(FRESH_SCULK);
        while (!remaining.isEmpty()) {
            ArrayList<BlockPos> component = new ArrayList<BlockPos>();
            ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>();
            BlockPos first = (BlockPos)remaining.iterator().next();
            remaining.remove(first);
            queue.add(first);
            while (!queue.isEmpty()) {
                BlockPos p = (BlockPos)queue.removeFirst();
                component.add(p);
                for (int x = -1; x <= 1; ++x) {
                    for (int y = -1; y <= 1; ++y) {
                        for (int z = -1; z <= 1; ++z) {
                            BlockPos next = p.offset(x, y, z);
                            if (!remaining.remove(next)) continue;
                            queue.addLast(next);
                        }
                    }
                }
            }
            ZoneTracker.addSculkGroup(mc, now, component);
        }
    }

    private static void addSculkGroup(Minecraft mc, long now, List<BlockPos> blocks) {
        if (blocks.size() >= 3) {
            Vec3 from;
            double sx = 0.0;
            double sy = 0.0;
            double sz = 0.0;
            for (BlockPos p : blocks) {
                sx += (double)p.getX() + 0.5;
                sy += (double)(p.getY() + 1);
                sz += (double)p.getZ() + 0.5;
            }
            int n = blocks.size();
            Vec3 c = new Vec3(sx / (double)n, sy / (double)n, sz / (double)n);
            double r = 0.0;
            for (BlockPos p : blocks) {
                r = Math.max(r, Math.hypot((double)p.getX() + 0.5 - c.x, (double)p.getZ() + 0.5 - c.z));
            }
            r = Math.max(2.0, Math.min(10.0, r + 0.6));
            for (Zone z2 : ZONES) {
                if (z2.kind != Kind.SCULK || !z2.self || z2.fadeAt != 0L || z2.hadBlocks || now - z2.start >= 6000L || !(z2.center.distanceTo(c) < 16.0)) continue;
                z2.center = c;
                z2.radius = r;
                for (BlockPos block : blocks) {
                    if (z2.blocks.size() >= 400) continue;
                    z2.blocks.add(block);
                }
                z2.hadBlocks = true;
                return;
            }
            for (Zone z2 : ZONES) {
                if (z2.kind != Kind.SCULK || z2.fadeAt != 0L || !(z2.center.distanceTo(c) < z2.radius + 4.0)) continue;
                for (BlockPos block : blocks) {
                    if (z2.blocks.size() >= 400) continue;
                    z2.blocks.add(block);
                }
                z2.hadBlocks = true;
                z2.radius = Math.max(z2.radius, Math.min(10.0, r + z2.center.distanceTo(c) * 0.5));
                return;
            }
            boolean self = now - bowUsedAt < 6000L && ZoneTracker.settings().self && ZONES.stream().anyMatch(z -> z.self && z.kind == Kind.SCULK && z.center.distanceTo(c) < 16.0);
            String owner = self ? mc.player.getName().getString() : "";
            Vec3 vec3 = from = self ? mc.player.getEyePosition().add(0.0, -0.25, 0.0) : null;
            if (!self) {
                double best = 64.0;
                for (Player p : mc.level.players()) {
                    double d;
                    if (p == mc.player || !ZoneTracker.holds(p, SCULK_BOW) || !((d = p.position().distanceTo(c)) < best)) continue;
                    best = d;
                    owner = p.getName().getString();
                    from = p.getEyePosition().add(0.0, -0.25, 0.0);
                }
                if (!ZoneTracker.settings().others) {
                    return;
                }
            }
            Zone z3 = new Zone(Kind.SCULK, self, owner, c, r);
            z3.from = from;
            for (BlockPos block : blocks) {
                if (z3.blocks.size() >= 400) continue;
                z3.blocks.add(block);
            }
            z3.hadBlocks = true;
            ZoneTracker.add(z3);
        }
    }

    private static void add(Zone z) {
        long now = System.nanoTime() / 1000000L;
        for (Zone o : ZONES) {
            if (o.kind != z.kind || o.fadeAt != 0L || !o.self || !z.self) continue;
            o.fadeAt = now;
        }
        ZONES.add(z);
        while (ZONES.size() > 20) {
            ZONES.removeFirst();
        }
        block2: while (ZONES.stream().filter(ox -> ox.fadeAt == 0L).count() > 10L) {
            for (Zone o : ZONES) {
                if (o.fadeAt != 0L) continue;
                o.fadeAt = now;
                continue block2;
            }
        }
    }

    private static Vec3 aim(Minecraft mc, double range) {
        Vec3 end;
        Vec3 eye = mc.player.getEyePosition();
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, end = eye.add(mc.player.getLookAngle().scale(range)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)mc.player));
        return hit.getType() == HitResult.Type.MISS ? null : hit.getLocation();
    }

    static float cooldownLeft(Minecraft mc, String name) {
        if (mc.player == null) {
            return 0.0f;
        }
        Inventory inv = mc.player.getInventory();
        ItemCooldownsAccessor acc = (ItemCooldownsAccessor)mc.player.getCooldowns();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack s = inv.getItem(i);
            if (!ZoneTracker.named(s, name)) continue;
            Identifier group = mc.player.getCooldowns().getCooldownGroup(s);
            for (Map.Entry<Identifier, Object> en : acc.bloodhelper$cooldowns().entrySet()) {
                if (!en.getKey().equals(group)) continue;
                int end = ((CooldownInstanceAccessor)en.getValue()).bloodhelper$endTime();
                return Math.max(0.0f, (float)(end - acc.bloodhelper$tickCount()) / 20.0f);
            }
            return 0.0f;
        }
        return 0.0f;
    }

    private static boolean holds(Player p, String name) {
        return ZoneTracker.named(p.getMainHandItem(), name) || ZoneTracker.named(p.getOffhandItem(), name);
    }

    private static boolean named(ItemStack stack, String name) {
        String id = LegendarySizeModule.identify(stack);
        if (id != null) {
            String expected = switch (name) {
                case "\u043a\u0430\u0442\u0430\u043d\u0430 \u0434\u0440\u0430\u043a\u043e\u043d\u0430" -> "dragon_katana";
                case "\u043b\u0435\u0433\u043a\u0430\u044f \u0431\u0443\u043b\u0430\u0432\u0430" -> "lite_mace";
                case STAFF -> "villager_staff";
                case SCULK_BOW -> "sculk_crossbow";
                case "\u0442\u0435\u043d\u0435\u0432\u043e\u0439 \u043a\u043b\u0438\u043d\u043e\u043a" -> "shadow_blade";
                case "\u043b\u0443\u043a \u0432\u0440\u0435\u043c\u0435\u043d\u0438" -> "time_bow";
                default -> "";
            };
            return id.equals(expected);
        }
        return !stack.isEmpty() && stack.getHoverName().getString().toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').contains(name);
    }

    private static LegendaryVisualSettings settings() {
        return BloodHelperClient.config().legendaryVisuals;
    }

    private ZoneTracker() {
    }

    public static final class Zone {
        public final Kind kind;
        public final boolean self;
        public final String owner;
        public final long start = System.nanoTime() / 1000000L;
        public Vec3 center;
        public Vec3 from;
        public double radius;
        public long fadeAt;
        final Set<BlockPos> blocks = new HashSet<BlockPos>();
        boolean hadBlocks;

        Zone(Kind kind, boolean self, String owner, Vec3 center, double radius) {
            this.kind = kind;
            this.self = self;
            this.owner = owner;
            this.center = center;
            this.radius = radius;
        }

        public String itemName() {
            return this.kind == Kind.STAFF ? "\u041f\u043e\u0441\u043e\u0445 \u0416\u0438\u0442\u0435\u043b\u044f" : "\u0421\u043a\u0430\u043b\u043a\u043e\u0432\u044b\u0439 \u0410\u0440\u0431\u0430\u043b\u0435\u0442";
        }

        public float secondsLeft() {
            return !this.self ? -1.0f : ZoneTracker.cooldownLeft(Minecraft.getInstance(), this.kind == Kind.STAFF ? ZoneTracker.STAFF : ZoneTracker.SCULK_BOW);
        }
    }

    public static enum Kind {
        STAFF,
        SCULK;

    }
}

