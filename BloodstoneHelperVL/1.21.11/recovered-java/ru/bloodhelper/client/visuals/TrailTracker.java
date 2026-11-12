/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1661
 *  net.minecraft.class_1799
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_638
 */
package ru.bloodhelper.client.visuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_638;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.LegendaryVisualSettings;
import ru.bloodhelper.client.modules.LegendarySizeModule;

public final class TrailTracker {
    public static final List<Trail> TRAILS = new ArrayList<Trail>();
    public static final long FADE_MS = 700L;
    private static final int MAX_TRAILS = 8;
    private static final long USE_WINDOW_MS = 800L;
    private static final String KATANA = "\u043a\u0430\u0442\u0430\u043d\u0430 \u0434\u0440\u0430\u043a\u043e\u043d\u0430";
    private static final String MACE = "\u043b\u0435\u0433\u043a\u0430\u044f \u0431\u0443\u043b\u0430\u0432\u0430";
    private static final Map<UUID, State> STATES = new HashMap<UUID, State>();
    private static float katanaCdBefore;
    private static float maceCdBefore;
    private static long katanaUsedAt;
    private static long maceUsedAt;
    private static boolean katanaHasCooldown;
    private static boolean maceHasCooldown;
    private static boolean initialized;

    public static void reset() {
        STATES.clear();
        TRAILS.clear();
        maceCdBefore = 0.0f;
        katanaCdBefore = 0.0f;
        maceUsedAt = 0L;
        katanaUsedAt = 0L;
        maceHasCooldown = false;
        katanaHasCooldown = false;
        initialized = false;
    }

    public static void tick(class_310 mc) {
        if (!BloodHelperClient.config().legendaryVisualsEnabled() || !TrailTracker.settings().trails || mc.field_1687 == null || mc.field_1724 == null) {
            TrailTracker.reset();
            return;
        }
        long now = System.nanoTime() / 1000000L;
        TRAILS.removeIf(tx -> tx.fadeAt != 0L && now - tx.fadeAt > 700L);
        if (mc.field_1687 != null && mc.field_1724 != null) {
            boolean on = BloodHelperClient.config().legendaryVisualsEnabled() && TrailTracker.settings().trails;
            float katanaCd = TrailTracker.cooldownPercent(mc, KATANA);
            float maceCd = TrailTracker.cooldownPercent(mc, MACE);
            if (!initialized) {
                katanaCdBefore = katanaCd;
                maceCdBefore = maceCd;
                initialized = true;
            }
            if (katanaCd > 0.0f && katanaCdBefore <= 0.0f) {
                katanaUsedAt = now;
                katanaHasCooldown = true;
            }
            if (maceCd > 0.0f && maceCdBefore <= 0.0f) {
                maceUsedAt = now;
                maceHasCooldown = true;
            }
            katanaCdBefore = katanaCd;
            maceCdBefore = maceCd;
            HashMap<UUID, State> seen = new HashMap<UUID, State>();
            int observed = 0;
            for (class_1657 p : mc.field_1687.method_18456()) {
                boolean wanted;
                if (++observed > 64) break;
                if (p.method_5858((class_1297)mc.field_1724) > TrailTracker.settings().distance * TrailTracker.settings().distance || p.method_5767()) continue;
                boolean self = p == mc.field_1724;
                State s = STATES.computeIfAbsent(p.method_5667(), k -> new State());
                seen.put(p.method_5667(), s);
                class_243 pos = p.method_73189();
                class_243 prev = s.history.isEmpty() ? pos : s.history.get(s.history.size() - 1);
                s.history.add(pos);
                while (s.history.size() > 5) {
                    s.history.remove(0);
                }
                boolean katanaNow = TrailTracker.holds(p, KATANA);
                boolean maceNow = TrailTracker.holds(p, MACE);
                boolean bl = on && (self ? TrailTracker.settings().self : TrailTracker.settings().others) ? true : (wanted = false);
                if (wanted) {
                    double dy;
                    double step = pos.method_1022(prev);
                    double jump = pos.method_1022(s.history.get(0));
                    if (step > 2.5 && jump > 6.0 && jump < 26.0 && now - s.lastTeleport > 1000L) {
                        s.lastTeleport = now;
                        s.jumpFrom = s.history.get(0);
                        s.jumpTo = pos;
                        s.jumpAt = now;
                        s.history.clear();
                        s.history.add(pos);
                        if (!self && s.katanaBefore && katanaNow) {
                            TrailTracker.startTeleport(p.method_5667(), false, s);
                        }
                    }
                    if ((dy = pos.field_1351 - prev.field_1351) > 0.9 && now - s.boostAt > 1000L) {
                        s.boostAt = now;
                        s.boostFrom = prev;
                        s.boostTo = pos;
                        if (!self && s.maceBefore && maceNow) {
                            TrailTracker.startBoost(p.method_5667(), false, s, now);
                        }
                    }
                    if (self) {
                        boolean maceUse;
                        boolean katanaUse;
                        boolean bl2 = katanaHasCooldown ? Math.abs(katanaUsedAt - s.jumpAt) < 800L : (katanaUse = s.katanaBefore && katanaNow);
                        if (s.jumpFrom != null && now - s.jumpAt < 800L && katanaUse) {
                            TrailTracker.startTeleport(p.method_5667(), true, s);
                        }
                        boolean bl3 = maceHasCooldown ? Math.abs(maceUsedAt - s.boostAt) < 800L : (maceUse = s.maceBefore && maceNow);
                        if (s.boostFrom != null && now - s.boostAt < 800L && maceUse) {
                            TrailTracker.startBoost(p.method_5667(), true, s, now);
                        }
                    }
                }
                s.katanaBefore = katanaNow;
                s.maceBefore = maceNow;
                for (Trail t : TRAILS) {
                    if (!t.recording || !t.owner.equals(p.method_5667())) continue;
                    class_243 last = t.points.get(t.points.size() - 1);
                    class_243 point = pos.method_1031(0.0, 0.2, 0.0);
                    if (t.points.size() < 96 && point.method_1022(last) > 0.6) {
                        t.points.add(point);
                        ++t.version;
                    }
                    if ((!p.method_24828() || now - t.recordStart <= 400L) && now - t.recordStart <= 6000L) continue;
                    t.recording = false;
                }
            }
            for (Trail t : TRAILS) {
                if (!t.recording || seen.containsKey(t.owner)) continue;
                t.recording = false;
            }
            STATES.keySet().retainAll(seen.keySet());
            long fallback = (long)(TrailTracker.settings().trailSeconds * 1000.0);
            for (Trail t : TRAILS) {
                if (t.fadeAt != 0L || t.recording) continue;
                long age = now - t.start;
                if (t.self) {
                    boolean cd;
                    float percent = t.kind == Kind.KATANA ? katanaCd : maceCd;
                    boolean bl = cd = percent > 0.0f;
                    if (cd && !t.sawCooldown) {
                        t.sawCooldown = true;
                        t.cdFirst = percent;
                        t.cdSeenAt = now;
                    }
                    t.cdNow = percent;
                    if ((!t.sawCooldown || cd || age <= 1500L) && (t.sawCooldown || age <= fallback)) continue;
                    t.fadeAt = now;
                    continue;
                }
                if (age <= fallback) continue;
                t.fadeAt = now;
            }
            while (TRAILS.size() > 16) {
                TRAILS.removeFirst();
            }
            long alive = TRAILS.stream().filter(tx -> tx.fadeAt == 0L).count();
            for (Trail t : TRAILS) {
                if (alive > 8L) {
                    if (t.fadeAt != 0L) continue;
                    t.fadeAt = now;
                    --alive;
                    continue;
                }
                break;
            }
        } else {
            STATES.clear();
            TRAILS.clear();
        }
    }

    private static void startTeleport(UUID owner, boolean self, State s) {
        if (!TrailTracker.settings().katana) {
            s.jumpFrom = null;
        } else {
            TrailTracker.replace(owner, Kind.KATANA);
            Trail t = new Trail(Kind.KATANA, owner, self);
            t.points.add(s.jumpFrom.method_1031(0.0, 0.9, 0.0));
            t.points.add(s.jumpTo.method_1031(0.0, 0.9, 0.0));
            TRAILS.add(t);
            s.jumpFrom = null;
        }
    }

    private static void startBoost(UUID owner, boolean self, State s, long now) {
        if (!TrailTracker.settings().mace) {
            s.boostFrom = null;
        } else {
            TrailTracker.replace(owner, Kind.MACE);
            Trail t = new Trail(Kind.MACE, owner, self);
            t.points.add(s.boostFrom.method_1031(0.0, 0.2, 0.0));
            t.points.add(s.boostTo.method_1031(0.0, 0.2, 0.0));
            t.recording = true;
            t.recordStart = now;
            TRAILS.add(t);
            s.boostFrom = null;
        }
    }

    private static void replace(UUID owner, Kind kind) {
        long now = System.nanoTime() / 1000000L;
        for (Trail t : TRAILS) {
            if (!t.owner.equals(owner) || t.kind != kind || t.fadeAt != 0L) continue;
            t.fadeAt = now;
            t.recording = false;
        }
    }

    private static float cooldownPercent(class_310 mc, String itemName) {
        class_1661 inv = mc.field_1724.method_31548();
        for (int i = 0; i < inv.method_5439(); ++i) {
            class_1799 s = inv.method_5438(i);
            if (!TrailTracker.named(s, itemName)) continue;
            return mc.field_1724.method_7357().method_7905(s, 0.0f);
        }
        return 0.0f;
    }

    private static boolean holds(class_1657 p, String itemName) {
        return TrailTracker.named(p.method_6047(), itemName) || TrailTracker.named(p.method_6079(), itemName);
    }

    private static boolean named(class_1799 stack, String itemName) {
        String id = LegendarySizeModule.identify(stack);
        if (id != null) {
            String expected = switch (itemName) {
                case KATANA -> "dragon_katana";
                case MACE -> "lite_mace";
                case "\u043f\u043e\u0441\u043e\u0445 \u0436\u0438\u0442\u0435\u043b\u044f" -> "villager_staff";
                case "\u0441\u043a\u0430\u043b\u043a\u043e\u0432\u044b\u0439 \u0430\u0440\u0431\u0430\u043b\u0435\u0442" -> "sculk_crossbow";
                case "\u0442\u0435\u043d\u0435\u0432\u043e\u0439 \u043a\u043b\u0438\u043d\u043e\u043a" -> "shadow_blade";
                case "\u043b\u0443\u043a \u0432\u0440\u0435\u043c\u0435\u043d\u0438" -> "time_bow";
                default -> "";
            };
            return id.equals(expected);
        }
        return !stack.method_7960() && stack.method_7964().getString().toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').contains(itemName);
    }

    private static LegendaryVisualSettings settings() {
        return BloodHelperClient.config().legendaryVisuals;
    }

    private TrailTracker() {
    }

    private static final class State {
        final List<class_243> history = new ArrayList<class_243>();
        boolean katanaBefore;
        boolean maceBefore;
        long lastTeleport;
        long jumpAt;
        long boostAt;
        class_243 jumpFrom;
        class_243 jumpTo;
        class_243 boostFrom;
        class_243 boostTo;

        private State() {
        }
    }

    public static final class Trail {
        public final Kind kind;
        public final UUID owner;
        public final boolean self;
        public final long start;
        public final List<class_243> points = new ArrayList<class_243>();
        boolean recording;
        boolean sawCooldown;
        long recordStart;
        public long fadeAt;
        public int version;
        float cdFirst;
        float cdNow;
        long cdSeenAt;
        public final String ownerName;

        Trail(Kind kind, UUID owner, boolean self) {
            this.kind = kind;
            this.owner = owner;
            this.self = self;
            this.start = System.nanoTime() / 1000000L;
            class_638 level = class_310.method_1551().field_1687;
            class_1657 p = level == null ? null : level.method_18470(owner);
            this.ownerName = p == null ? "" : p.method_5477().getString();
        }

        public String itemName() {
            return this.kind == Kind.KATANA ? "\u041a\u0430\u0442\u0430\u043d\u0430 \u0414\u0440\u0430\u043a\u043e\u043d\u0430" : "\u041b\u0451\u0433\u043a\u0430\u044f \u0411\u0443\u043b\u0430\u0432\u0430";
        }

        public long remainingMs(long now) {
            if (this.fadeAt != 0L) {
                return 0L;
            }
            if (this.recording) {
                return -1L;
            }
            if (this.self && this.sawCooldown) {
                float used = this.cdFirst - this.cdNow;
                return !(used < 0.01f) && now - this.cdSeenAt >= 300L ? (long)(this.cdNow / (used / (float)(now - this.cdSeenAt))) : -1L;
            }
            return Math.max(0L, (long)(TrailTracker.settings().trailSeconds * 1000.0) - (now - this.start));
        }
    }

    public static enum Kind {
        KATANA,
        MACE;

    }
}

