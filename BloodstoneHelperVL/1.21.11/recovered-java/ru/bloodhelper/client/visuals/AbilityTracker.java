/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1282
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1665
 *  net.minecraft.class_1799
 *  net.minecraft.class_238
 *  net.minecraft.class_310
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
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1665;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_310;
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

    private static boolean wanted(class_1657 p) {
        return p == class_310.method_1551().field_1724 ? AbilityTracker.settings().self : AbilityTracker.settings().others;
    }

    public static void tick(class_310 mc) {
        if (!BloodHelperClient.config().legendaryVisualsEnabled() || !AbilityTracker.settings().trails || mc.field_1687 == null || mc.field_1724 == null) {
            AbilityTracker.reset();
            return;
        }
        long now = System.nanoTime() / 1000000L;
        MARKS.removeIf(m -> now > m.start() + m.durationMs());
        if (mc.field_1687 == null) {
            LAST.clear();
            BOW_ARROWS.clear();
            MARKS.clear();
        } else {
            boolean active = BloodHelperClient.config().legendaryVisualsEnabled() && AbilityTracker.settings().trails && AbilityTracker.settings().shadow;
            HashMap<UUID, Seen> current = new HashMap<UUID, Seen>();
            int observed = 0;
            for (class_1657 class_16572 : mc.field_1687.method_18456()) {
                if (++observed > 64) break;
                if (!AbilityTracker.wanted(class_16572) || class_16572.method_5858((class_1297)mc.field_1724) > AbilityTracker.settings().distance * AbilityTracker.settings().distance) continue;
                Seen before = LAST.get(class_16572.method_5667());
                long heldAt = AbilityTracker.holds(class_16572, BLADE) ? now : (before != null ? before.bladeHeldAt() : 0L);
                boolean invisible = class_16572.method_5767();
                if (active && before != null && !before.invisible() && invisible && now - heldAt < 1500L) {
                    AbilityTracker.add(Kind.BLADE, before, (long)(AbilityTracker.settings().shadowSeconds * 1000.0));
                }
                current.put(class_16572.method_5667(), AbilityTracker.seen(class_16572, invisible, heldAt));
            }
            if (active && mc.field_1724 != null) {
                for (Map.Entry entry : LAST.entrySet()) {
                    Seen s = (Seen)entry.getValue();
                    if (current.containsKey(entry.getKey()) || s.invisible() || now - s.bladeHeldAt() >= 1500L || !(s.box().method_1005().method_1022(mc.field_1724.method_73189()) < 48.0)) continue;
                    AbilityTracker.add(Kind.BLADE, s, (long)(AbilityTracker.settings().shadowSeconds * 1000.0));
                }
            }
            LAST.clear();
            LAST.putAll(current);
        }
    }

    public static void onEntityLoad(class_1297 entity) {
        class_1657 owner;
        class_1665 arrow;
        class_1297 class_12972;
        if (!(BloodHelperClient.config().legendaryVisualsEnabled() && AbilityTracker.settings().trails && AbilityTracker.settings().timeBow && BOW_ARROWS.size() < 256)) {
            return;
        }
        if (entity instanceof class_1665 && (class_12972 = (arrow = (class_1665)entity).method_24921()) instanceof class_1657 && AbilityTracker.holds(owner = (class_1657)class_12972, BOW)) {
            BOW_ARROWS.add(arrow.method_5628());
        }
    }

    public static void onEntityUnload(class_1297 entity) {
        BOW_ARROWS.remove(entity.method_5628());
    }

    public static void onDamage(class_1297 victim, class_1282 source) {
        class_1297 direct;
        class_1657 player;
        if (BloodHelperClient.config().legendaryVisualsEnabled() && AbilityTracker.settings().trails && AbilityTracker.settings().timeBow && victim instanceof class_1657 && AbilityTracker.wanted(player = (class_1657)victim) && (direct = source.method_5526()) instanceof class_1665) {
            class_1657 shooter;
            class_1297 class_12972;
            boolean fromBow;
            boolean bl = fromBow = BOW_ARROWS.contains(direct.method_5628()) || (class_12972 = source.method_5529()) instanceof class_1657 && AbilityTracker.holds(shooter = (class_1657)class_12972, BOW);
            if (fromBow) {
                String name = player.method_5477().getString();
                MARKS.removeIf(m -> m.kind() == Kind.BOW && m.name().equals(name));
                AbilityTracker.add(Kind.BOW, AbilityTracker.seen(player, false, 0L), (long)(AbilityTracker.settings().timeBowSeconds * 1000.0));
            }
        }
    }

    private static Seen seen(class_1657 p, boolean invisible, long bladeHeldAt) {
        return new Seen(p.method_5477().getString(), p.method_5829(), p.method_5791(), p.method_36455(), p.method_5751(), invisible, bladeHeldAt);
    }

    private static void add(Kind kind, Seen s, long durationMs) {
        AbilityTracker.add(kind, s.name(), s.box(), s.yaw(), s.pitch(), s.eye(), durationMs);
    }

    public static void add(Kind kind, String name, class_238 box, float yaw, float pitch, float eye, long durationMs) {
        while (MARKS.size() >= 24) {
            MARKS.removeFirst();
        }
        MARKS.add(new Mark(kind, name, box, yaw, pitch, eye, System.nanoTime() / 1000000L, durationMs));
    }

    private static boolean holds(class_1657 p, String itemName) {
        return AbilityTracker.named(p.method_6047(), itemName) || AbilityTracker.named(p.method_6079(), itemName);
    }

    private static boolean named(class_1799 stack, String itemName) {
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
        return !stack.method_7960() && stack.method_7964().getString().toLowerCase(Locale.ROOT).contains(itemName);
    }

    private static LegendaryVisualSettings settings() {
        return BloodHelperClient.config().legendaryVisuals;
    }

    private AbilityTracker() {
    }

    private record Seen(String name, class_238 box, float yaw, float pitch, float eye, boolean invisible, long bladeHeldAt) {
    }

    public static enum Kind {
        BLADE,
        BOW;

    }

    public record Mark(Kind kind, String name, class_238 box, float yaw, float pitch, float eye, long start, long durationMs) {
        public float remaining(long now) {
            return Math.max(0.0f, (float)(this.start + this.durationMs - now) / 1000.0f);
        }
    }
}

