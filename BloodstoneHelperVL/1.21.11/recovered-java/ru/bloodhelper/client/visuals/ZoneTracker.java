/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1799
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2374
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_2680
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
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
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
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
    private static final Set<class_2338> FRESH_SCULK = new LinkedHashSet<class_2338>();
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

    public static void onBlock(class_2338 pos, class_2680 state) {
        boolean sculk;
        class_310 mc = class_310.method_1551();
        if (!BloodHelperClient.config().legendaryVisualsEnabled() || !ZoneTracker.settings().trails || !ZoneTracker.settings().sculk || mc.field_1724 == null || pos.method_19770((class_2374)mc.field_1724.method_73189()) > ZoneTracker.settings().distance * ZoneTracker.settings().distance) {
            return;
        }
        boolean bl = sculk = state.method_27852(class_2246.field_37568) || state.method_27852(class_2246.field_37569);
        if (sculk) {
            if (FRESH_SCULK.size() < 400) {
                FRESH_SCULK.add(pos.method_10062());
            }
            freshAt = System.nanoTime() / 1000000L;
        } else {
            FRESH_SCULK.remove(pos);
            for (Zone z : ZONES) {
                z.blocks.remove(pos);
            }
        }
    }

    public static void tick(class_310 mc) {
        if (!BloodHelperClient.config().legendaryVisualsEnabled() || !ZoneTracker.settings().trails || mc.field_1687 == null || mc.field_1724 == null) {
            ZoneTracker.reset();
            return;
        }
        long now = System.nanoTime() / 1000000L;
        ZONES.removeIf(zx -> zx.fadeAt != 0L && now - zx.fadeAt > 700L);
        if (mc.field_1687 != null && mc.field_1724 != null) {
            class_243 hit;
            boolean on = BloodHelperClient.config().legendaryVisualsEnabled() && ZoneTracker.settings().trails;
            float staffCd = ZoneTracker.cooldownLeft(mc, STAFF);
            float bowCd = ZoneTracker.cooldownLeft(mc, SCULK_BOW);
            if (!initialized) {
                staffCdBefore = staffCd;
                bowCdBefore = bowCd;
                initialized = true;
            }
            if (on && ZoneTracker.settings().staff && ZoneTracker.settings().self && staffCd > 0.0f && staffCdBefore <= 0.0f && (hit = ZoneTracker.aim(mc, 120.0)) != null) {
                ZoneTracker.add(new Zone(Kind.STAFF, true, mc.field_1724.method_5477().getString(), hit, 1.6));
            }
            if (bowCd > 0.0f && bowCdBefore <= 0.0f) {
                bowUsedAt = now;
                if (on && ZoneTracker.settings().sculk && ZoneTracker.settings().self) {
                    class_243 eye = mc.field_1724.method_33571();
                    class_243 hit2 = ZoneTracker.aim(mc, 80.0);
                    class_243 end = hit2 != null ? hit2 : eye.method_1019(mc.field_1724.method_5720().method_1021(40.0));
                    Zone z = new Zone(Kind.SCULK, true, mc.field_1724.method_5477().getString(), end, 1.0);
                    z.from = eye.method_1031(0.0, -0.25, 0.0);
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

    private static void groupSculk(class_310 mc, long now) {
        HashSet<class_2338> remaining = new HashSet<class_2338>(FRESH_SCULK);
        while (!remaining.isEmpty()) {
            ArrayList<class_2338> component = new ArrayList<class_2338>();
            ArrayDeque<class_2338> queue = new ArrayDeque<class_2338>();
            class_2338 first = (class_2338)remaining.iterator().next();
            remaining.remove(first);
            queue.add(first);
            while (!queue.isEmpty()) {
                class_2338 p = (class_2338)queue.removeFirst();
                component.add(p);
                for (int x = -1; x <= 1; ++x) {
                    for (int y = -1; y <= 1; ++y) {
                        for (int z = -1; z <= 1; ++z) {
                            class_2338 next = p.method_10069(x, y, z);
                            if (!remaining.remove(next)) continue;
                            queue.addLast(next);
                        }
                    }
                }
            }
            ZoneTracker.addSculkGroup(mc, now, component);
        }
    }

    private static void addSculkGroup(class_310 mc, long now, List<class_2338> blocks) {
        if (blocks.size() >= 3) {
            class_243 from;
            double sx = 0.0;
            double sy = 0.0;
            double sz = 0.0;
            for (class_2338 p : blocks) {
                sx += (double)p.method_10263() + 0.5;
                sy += (double)(p.method_10264() + 1);
                sz += (double)p.method_10260() + 0.5;
            }
            int n = blocks.size();
            class_243 c = new class_243(sx / (double)n, sy / (double)n, sz / (double)n);
            double r = 0.0;
            for (class_2338 p : blocks) {
                r = Math.max(r, Math.hypot((double)p.method_10263() + 0.5 - c.field_1352, (double)p.method_10260() + 0.5 - c.field_1350));
            }
            r = Math.max(2.0, Math.min(10.0, r + 0.6));
            for (Zone z2 : ZONES) {
                if (z2.kind != Kind.SCULK || !z2.self || z2.fadeAt != 0L || z2.hadBlocks || now - z2.start >= 6000L || !(z2.center.method_1022(c) < 16.0)) continue;
                z2.center = c;
                z2.radius = r;
                for (class_2338 block : blocks) {
                    if (z2.blocks.size() >= 400) continue;
                    z2.blocks.add(block);
                }
                z2.hadBlocks = true;
                return;
            }
            for (Zone z2 : ZONES) {
                if (z2.kind != Kind.SCULK || z2.fadeAt != 0L || !(z2.center.method_1022(c) < z2.radius + 4.0)) continue;
                for (class_2338 block : blocks) {
                    if (z2.blocks.size() >= 400) continue;
                    z2.blocks.add(block);
                }
                z2.hadBlocks = true;
                z2.radius = Math.max(z2.radius, Math.min(10.0, r + z2.center.method_1022(c) * 0.5));
                return;
            }
            boolean self = now - bowUsedAt < 6000L && ZoneTracker.settings().self && ZONES.stream().anyMatch(z -> z.self && z.kind == Kind.SCULK && z.center.method_1022(c) < 16.0);
            String owner = self ? mc.field_1724.method_5477().getString() : "";
            class_243 class_2432 = from = self ? mc.field_1724.method_33571().method_1031(0.0, -0.25, 0.0) : null;
            if (!self) {
                double best = 64.0;
                for (class_1657 p : mc.field_1687.method_18456()) {
                    double d;
                    if (p == mc.field_1724 || !ZoneTracker.holds(p, SCULK_BOW) || !((d = p.method_73189().method_1022(c)) < best)) continue;
                    best = d;
                    owner = p.method_5477().getString();
                    from = p.method_33571().method_1031(0.0, -0.25, 0.0);
                }
                if (!ZoneTracker.settings().others) {
                    return;
                }
            }
            Zone z3 = new Zone(Kind.SCULK, self, owner, c, r);
            z3.from = from;
            for (class_2338 block : blocks) {
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

    private static class_243 aim(class_310 mc, double range) {
        class_243 end;
        class_243 eye = mc.field_1724.method_33571();
        class_3965 hit = mc.field_1687.method_17742(new class_3959(eye, end = eye.method_1019(mc.field_1724.method_5720().method_1021(range)), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)mc.field_1724));
        return hit.method_17783() == class_239.class_240.field_1333 ? null : hit.method_17784();
    }

    static float cooldownLeft(class_310 mc, String name) {
        if (mc.field_1724 == null) {
            return 0.0f;
        }
        class_1661 inv = mc.field_1724.method_31548();
        ItemCooldownsAccessor acc = (ItemCooldownsAccessor)mc.field_1724.method_7357();
        for (int i = 0; i < inv.method_5439(); ++i) {
            class_1799 s = inv.method_5438(i);
            if (!ZoneTracker.named(s, name)) continue;
            class_2960 group = mc.field_1724.method_7357().method_62836(s);
            for (Map.Entry<class_2960, Object> en : acc.bloodhelper$cooldowns().entrySet()) {
                if (!en.getKey().equals(group)) continue;
                int end = ((CooldownInstanceAccessor)en.getValue()).bloodhelper$endTime();
                return Math.max(0.0f, (float)(end - acc.bloodhelper$tickCount()) / 20.0f);
            }
            return 0.0f;
        }
        return 0.0f;
    }

    private static boolean holds(class_1657 p, String name) {
        return ZoneTracker.named(p.method_6047(), name) || ZoneTracker.named(p.method_6079(), name);
    }

    private static boolean named(class_1799 stack, String name) {
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
        return !stack.method_7960() && stack.method_7964().getString().toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').contains(name);
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
        public class_243 center;
        public class_243 from;
        public double radius;
        public long fadeAt;
        final Set<class_2338> blocks = new HashSet<class_2338>();
        boolean hadBlocks;

        Zone(Kind kind, boolean self, String owner, class_243 center, double radius) {
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
            return !this.self ? -1.0f : ZoneTracker.cooldownLeft(class_310.method_1551(), this.kind == Kind.STAFF ? ZoneTracker.STAFF : ZoneTracker.SCULK_BOW);
        }
    }

    public static enum Kind {
        STAFF,
        SCULK;

    }
}

