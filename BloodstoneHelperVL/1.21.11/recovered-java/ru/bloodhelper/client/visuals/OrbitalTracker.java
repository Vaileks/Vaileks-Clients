/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 */
package ru.bloodhelper.client.visuals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.LegendaryVisualSettings;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.modules.LegendarySizeModule;
import ru.bloodhelper.client.visuals.WorldOverlay;

public final class OrbitalTracker {
    private static final List<Strike> strikes = new ArrayList<Strike>();
    private static class_243 preview;
    private static class_243 pending;
    private static class_1799 pendingItem;
    private static long pendingAt;
    private static Object world;

    private OrbitalTracker() {
    }

    public static boolean cannon(class_1799 stack) {
        return "orbital_nuke".equals(LegendarySizeModule.identify(stack));
    }

    private static boolean enabled() {
        LegendaryVisualSettings s = BloodHelperClient.config().legendaryVisuals;
        return BloodHelperClient.config().legendaryVisualsEnabled() && s.trails && s.self && s.orbital;
    }

    public static void reset() {
        pending = null;
        preview = null;
        pendingItem = class_1799.field_8037;
        pendingAt = 0L;
        strikes.clear();
        world = null;
    }

    public static void onUse(class_1657 player, class_1268 hand) {
        class_310 mc = class_310.method_1551();
        if (!OrbitalTracker.enabled() || player != mc.field_1724 || mc.field_1687 == null || mc.field_1755 != null || player.method_5715()) {
            return;
        }
        class_1799 item = player.method_5998(hand);
        if (!OrbitalTracker.cannon(item) || player.method_7357().method_7904(item)) {
            return;
        }
        long now = System.nanoTime() / 1000000L;
        if (pending != null && now - pendingAt < 150L) {
            return;
        }
        class_243 aim = OrbitalTracker.aim(mc);
        if (aim == null) {
            return;
        }
        world = mc.field_1687;
        pending = aim;
        pendingItem = item.method_7972();
        pendingAt = now;
    }

    private static class_243 aim(class_310 mc) {
        class_243 eye = mc.field_1724.method_33571();
        class_3965 hit = mc.field_1687.method_17742(new class_3959(eye, eye.method_1019(mc.field_1724.method_5720().method_1021(512.0)), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)mc.field_1724));
        return hit.method_17783() == class_239.class_240.field_1333 ? null : hit.method_17784().method_1031(0.0, 0.055, 0.0);
    }

    public static void tick(class_310 mc) {
        class_1799 item;
        if (!OrbitalTracker.enabled() || mc.field_1724 == null || mc.field_1687 == null) {
            OrbitalTracker.reset();
            return;
        }
        if (world != mc.field_1687) {
            OrbitalTracker.reset();
            world = mc.field_1687;
        }
        long now = System.nanoTime() / 1000000L;
        LegendaryVisualSettings settings = BloodHelperClient.config().legendaryVisuals;
        if (pending != null) {
            if (now - pendingAt > 1500L) {
                pending = null;
                pendingItem = class_1799.field_8037;
            } else if (mc.field_1724.method_7357().method_7904(pendingItem)) {
                strikes.add(new Strike(pending, now, settings.orbitalRadius));
                while (strikes.size() > 4) {
                    strikes.removeFirst();
                }
                pending = null;
                pendingItem = class_1799.field_8037;
            }
        }
        strikes.removeIf(s -> (double)(now - s.started) > (settings.orbitalSeconds + 0.45) * 1000.0);
        preview = null;
        class_1799 class_17992 = item = OrbitalTracker.cannon(mc.field_1724.method_6047()) ? mc.field_1724.method_6047() : mc.field_1724.method_6079();
        if (settings.orbitalPreview && mc.field_1755 == null && !mc.field_1724.method_5715() && OrbitalTracker.cannon(item) && !mc.field_1724.method_7357().method_7904(item)) {
            preview = OrbitalTracker.aim(mc);
        }
    }

    public static void extract() {
        class_243 aim;
        if (!OrbitalTracker.enabled()) {
            return;
        }
        LegendaryVisualSettings s = BloodHelperClient.config().legendaryVisuals;
        long now = System.nanoTime() / 1000000L;
        class_310 mc = class_310.method_1551();
        if (preview != null && mc.field_1724 != null && !mc.field_1724.method_5715() && (aim = OrbitalTracker.aim(mc)) != null) {
            OrbitalTracker.draw(aim, s.orbitalRadius, UiDraw.alpha(s.orbitalColor, 165), true);
        }
        for (Strike strike : strikes) {
            double age = (double)(now - strike.started) / 1000.0;
            double alpha = Math.min(Math.clamp(age / 0.12, 0.0, 1.0), Math.clamp((s.orbitalSeconds + 0.45 - age) / 0.45, 0.0, 1.0));
            OrbitalTracker.draw(strike.center, strike.radius, UiDraw.alpha(s.orbitalColor, (int)(230.0 * alpha)), false);
        }
    }

    private static void draw(class_243 center, double radius, int color, boolean aiming) {
        WorldOverlay.ring(center, radius, color);
        WorldOverlay.ring(center, 0.3, UiDraw.alpha(color, (int)((double)(color >>> 24) * 0.7)));
        WorldOverlay.line(center, center.method_1031(0.0, aiming ? 2.2 : 5.0, 0.0), color, 1.7f);
        if (BloodHelperClient.config().legendaryVisuals.names) {
            WorldOverlay.label(center.method_1031(0.0, 2.4, 0.0), (aiming ? "\u0421\u0442\u0430\u0431\u0448\u043e\u0442 \u00b7 \u2248" : "\u0421\u0442\u0430\u0431\u0448\u043e\u0442 \u00b7 \u0443\u0434\u0430\u0440 \u00b7 \u2248") + Math.round(radius) + " \u043c", color);
        }
    }

    public static class_243 preview() {
        return preview;
    }

    public static List<Strike> strikes() {
        return List.copyOf(strikes);
    }

    static {
        pendingItem = class_1799.field_8037;
    }

    public record Strike(class_243 center, long started, double radius) {
    }
}

