/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.arrow.AbstractArrow
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.AABB
 */
package ru.bloodhelper.client.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.LegendaryVisualSettings;
import ru.bloodhelper.client.modules.LegendarySizeModule;

public final class AbilityTracker {
    public static final List<Mark> MARKS = new ArrayList<Mark>();
    private static final String BLADE = "\u0442\u0435\u043d\u0435\u0432\u043e\u0439 \u043a\u043b\u0438\u043d\u043e\u043a";
    private static final String BOW = "\u043b\u0443\u043a \u0432\u0440\u0435\u043c\u0435\u043d\u0438";
    private static final long HOLD_GRACE_MS = 1500L;
    private static final Map<UUID, Seen> LAST = new HashMap<UUID, Seen>();
    private static final Set<Integer> BOW_ARROWS = new HashSet<Integer>();

    public static void reset() {
        LAST.clear();
        BOW_ARROWS.clear();
        MARKS.clear();
    }

    private static boolean wanted(Player p) {
        return p == Minecraft.getInstance().player ? AbilityTracker.settings().self : AbilityTracker.settings().others;
    }

    public static void tick(Minecraft mc) {
        if (!BloodHelperClient.config().legendaryVisualsEnabled() || !AbilityTracker.settings().trails || mc.level == null || mc.player == null) {
            AbilityTracker.reset();
            return;
        }
        long now = System.nanoTime() / 1000000L;
        MARKS.removeIf(m -> now > m.start() + m.durationMs());
        if (mc.level == null) {
            LAST.clear();
            BOW_ARROWS.clear();
            MARKS.clear();
        } else {
            boolean active = BloodHelperClient.config().legendaryVisualsEnabled() && AbilityTracker.settings().trails && AbilityTracker.settings().shadow;
            HashMap<UUID, Seen> current = new HashMap<UUID, Seen>();
            int observed = 0;
            for (Player player : mc.level.players()) {
                if (++observed > 64) break;
                if (!AbilityTracker.wanted(player) || player.distanceToSqr((Entity)mc.player) > AbilityTracker.settings().distance * AbilityTracker.settings().distance) continue;
                Seen before = LAST.get(player.getUUID());
                long heldAt = AbilityTracker.holds(player, BLADE) ? now : (before != null ? before.bladeHeldAt() : 0L);
                boolean invisible = player.isInvisible();
                if (active && before != null && !before.invisible() && invisible && now - heldAt < 1500L) {
                    AbilityTracker.add(Kind.BLADE, before, (long)(AbilityTracker.settings().shadowSeconds * 1000.0));
                }
                current.put(player.getUUID(), AbilityTracker.seen(player, invisible, heldAt));
            }
            if (active && mc.player != null) {
                for (Map.Entry entry : LAST.entrySet()) {
                    Seen s = (Seen)entry.getValue();
                    if (current.containsKey(entry.getKey()) || s.invisible() || now - s.bladeHeldAt() >= 1500L || !(s.box().getCenter().distanceTo(mc.player.position()) < 48.0)) continue;
                    AbilityTracker.add(Kind.BLADE, s, (long)(AbilityTracker.settings().shadowSeconds * 1000.0));
                }
            }
            LAST.clear();
            LAST.putAll(current);
        }
    }

    public static void onEntityLoad(Entity entity) {
        Player owner;
        AbstractArrow arrow;
        Entity entity2;
        if (!(BloodHelperClient.config().legendaryVisualsEnabled() && AbilityTracker.settings().trails && AbilityTracker.settings().timeBow && BOW_ARROWS.size() < 256)) {
            return;
        }
        if (entity instanceof AbstractArrow && (entity2 = (arrow = (AbstractArrow)entity).getOwner()) instanceof Player && AbilityTracker.holds(owner = (Player)entity2, BOW)) {
            BOW_ARROWS.add(arrow.getId());
        }
    }

    public static void onEntityUnload(Entity entity) {
        BOW_ARROWS.remove(entity.getId());
    }

    public static void onDamage(Entity victim, DamageSource source) {
        Entity direct;
        Player player;
        if (BloodHelperClient.config().legendaryVisualsEnabled() && AbilityTracker.settings().trails && AbilityTracker.settings().timeBow && victim instanceof Player && AbilityTracker.wanted(player = (Player)victim) && (direct = source.getDirectEntity()) instanceof AbstractArrow) {
            Player shooter;
            Entity entity;
            boolean fromBow;
            boolean bl = fromBow = BOW_ARROWS.contains(direct.getId()) || (entity = source.getEntity()) instanceof Player && AbilityTracker.holds(shooter = (Player)entity, BOW);
            if (fromBow) {
                String name = player.getName().getString();
                MARKS.removeIf(m -> m.kind() == Kind.BOW && m.name().equals(name));
                AbilityTracker.add(Kind.BOW, AbilityTracker.seen(player, false, 0L), (long)(AbilityTracker.settings().timeBowSeconds * 1000.0));
            }
        }
    }

    private static Seen seen(Player p, boolean invisible, long bladeHeldAt) {
        return new Seen(p.getName().getString(), p.getBoundingBox(), p.getYHeadRot(), p.getXRot(), p.getEyeHeight(), invisible, bladeHeldAt);
    }

    private static void add(Kind kind, Seen s, long durationMs) {
        AbilityTracker.add(kind, s.name(), s.box(), s.yaw(), s.pitch(), s.eye(), durationMs);
    }

    public static void add(Kind kind, String name, AABB box, float yaw, float pitch, float eye, long durationMs) {
        while (MARKS.size() >= 24) {
            MARKS.removeFirst();
        }
        MARKS.add(new Mark(kind, name, box, yaw, pitch, eye, System.nanoTime() / 1000000L, durationMs));
    }

    private static boolean holds(Player p, String itemName) {
        return AbilityTracker.named(p.getMainHandItem(), itemName) || AbilityTracker.named(p.getOffhandItem(), itemName);
    }

    private static boolean named(ItemStack stack, String itemName) {
        String id = LegendarySizeModule.identify(stack);
        if (id != null) {
            String expected = switch (itemName) {
                case "\u043a\u0430\u0442\u0430\u043d\u0430 \u0434\u0440\u0430\u043a\u043e\u043d\u0430" -> "dragon_katana";
                case "\u043b\u0435\u0433\u043a\u0430\u044f \u0431\u0443\u043b\u0430\u0432\u0430" -> "lite_mace";
                case "\u043f\u043e\u0441\u043e\u0445 \u0436\u0438\u0442\u0435\u043b\u044f" -> "villager_staff";
                case "\u0441\u043a\u0430\u043b\u043a\u043e\u0432\u044b\u0439 \u0430\u0440\u0431\u0430\u043b\u0435\u0442" -> "sculk_crossbow";
                case BLADE -> "shadow_blade";
                case BOW -> "time_bow";
                default -> "";
            };
            return id.equals(expected);
        }
        return !stack.isEmpty() && stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(itemName);
    }

    private static LegendaryVisualSettings settings() {
        return BloodHelperClient.config().legendaryVisuals;
    }

    private AbilityTracker() {
    }

    private record Seen(String name, AABB box, float yaw, float pitch, float eye, boolean invisible, long bladeHeldAt) {
    }

    public static enum Kind {
        BLADE,
        BOW;

    }

    public record Mark(Kind kind, String name, AABB box, float yaw, float pitch, float eye, long start, long durationMs) {
        public float remaining(long now) {
            return Math.max(0.0f, (float)(this.start + this.durationMs - now) / 1000.0f);
        }
    }
}

