/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
 *  net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 */
package ru.bloodhelper.client.visuals;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.LegendaryVisualSettings;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.visuals.AbilityTracker;
import ru.bloodhelper.client.visuals.LootHighlight;
import ru.bloodhelper.client.visuals.OrbitalTracker;
import ru.bloodhelper.client.visuals.TrailTracker;
import ru.bloodhelper.client.visuals.WorldGeometry;
import ru.bloodhelper.client.visuals.WorldOverlay;
import ru.bloodhelper.client.visuals.ZoneTracker;

public final class LegendaryVisuals {
    private static Object level;

    private LegendaryVisuals() {
    }

    public static void register() {
        WorldRenderEvents.END_EXTRACTION.register(context -> LegendaryVisuals.extract());
        WorldRenderEvents.END_MAIN.register(context -> {
            if (LegendaryVisuals.visible()) {
                WorldOverlay.drawWorld();
            } else {
                WorldGeometry.clearCounters();
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> WorldGeometry.close());
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> AbilityTracker.onEntityLoad(entity));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> AbilityTracker.onEntityUnload(entity));
        HudElementRegistry.attachElementBefore((class_2960)VanillaHudElements.CHAT, (class_2960)class_2960.method_60655((String)"bloodhelper", (String)"legendary_visuals"), (g, d) -> {
            if (LegendaryVisuals.visible()) {
                WorldOverlay.drawLabels(g);
            } else {
                WorldOverlay.begin();
            }
        });
    }

    public static void reset() {
        TrailTracker.reset();
        ZoneTracker.reset();
        AbilityTracker.reset();
        LootHighlight.reset();
        OrbitalTracker.reset();
        WorldOverlay.extract();
        level = null;
    }

    public static void tick(class_310 mc) {
        if (!BloodHelperClient.config().legendaryVisualsEnabled() || mc.field_1724 == null || mc.field_1687 == null) {
            LegendaryVisuals.reset();
            return;
        }
        if (level != mc.field_1687) {
            LegendaryVisuals.reset();
            level = mc.field_1687;
        }
        TrailTracker.tick(mc);
        ZoneTracker.tick(mc);
        AbilityTracker.tick(mc);
        LootHighlight.tick(mc);
        OrbitalTracker.tick(mc);
    }

    private static boolean visible() {
        class_310 mc = class_310.method_1551();
        return BloodHelperClient.config().legendaryVisualsEnabled() && mc.field_1687 == level && mc.field_1724 != null && mc.field_1755 == null && !mc.field_1690.field_1842;
    }

    private static void extract() {
        double fade;
        int color;
        WorldOverlay.extract();
        if (!LegendaryVisuals.visible()) {
            return;
        }
        class_310 mc = class_310.method_1551();
        LegendaryVisualSettings s = BloodHelperClient.config().legendaryVisuals;
        float partial = mc.method_61966().method_60637(true);
        LootHighlight.extract(partial);
        if (!s.trails) {
            return;
        }
        OrbitalTracker.extract();
        long now = System.nanoTime() / 1000000L;
        for (TrailTracker.Trail t : TrailTracker.TRAILS) {
            if (t.points.isEmpty() || t.points.stream().allMatch(p -> p.method_1025(mc.field_1724.method_73189()) > s.distance * s.distance) || !(t.self ? s.self : s.others) || !(t.kind == TrailTracker.Kind.KATANA ? s.katana : s.mace)) continue;
            color = t.kind == TrailTracker.Kind.KATANA ? s.katanaColor : s.maceColor;
            fade = t.fadeAt == 0L ? Math.clamp((double)(now - t.start) / 180.0, 0.0, 1.0) : Math.max(0.0, 1.0 - (double)(now - t.fadeAt) / 700.0);
            color = UiDraw.alpha(color, (int)(220.0 * fade));
            for (int i = 1; i < t.points.size(); ++i) {
                WorldOverlay.line(t.points.get(i - 1), t.points.get(i), color, 1.5f);
            }
            if (!s.names || t.points.isEmpty()) continue;
            WorldOverlay.label(t.points.getLast().method_1031(0.0, 0.4, 0.0), t.itemName(), color);
        }
        for (ZoneTracker.Zone z : ZoneTracker.ZONES) {
            if (z.center.method_1025(mc.field_1724.method_73189()) > s.distance * s.distance || !(z.self ? s.self : s.others) || !(z.kind == ZoneTracker.Kind.STAFF ? s.staff : s.sculk)) continue;
            color = z.kind == ZoneTracker.Kind.STAFF ? s.staffColor : s.sculkColor;
            fade = z.fadeAt == 0L ? Math.clamp((double)(now - z.start) / 200.0, 0.0, 1.0) : Math.max(0.0, 1.0 - (double)(now - z.fadeAt) / 700.0);
            color = UiDraw.alpha(color, (int)(210.0 * fade));
            WorldOverlay.ring(z.center.method_1031(0.0, 0.055, 0.0), z.radius, color);
            if (z.from != null) {
                WorldOverlay.line(z.from, z.center, color, 1.0f);
            }
            if (!s.names) continue;
            WorldOverlay.label(z.center.method_1031(0.0, 0.6, 0.0), z.itemName(), color);
        }
        for (AbilityTracker.Mark m : AbilityTracker.MARKS) {
            boolean self = m.name().equals(mc.field_1724.method_5477().getString());
            if (!(self ? s.self : s.others) || m.box().method_1005().method_1025(mc.field_1724.method_73189()) > s.distance * s.distance || !(m.kind() == AbilityTracker.Kind.BLADE ? s.shadow : s.timeBow)) continue;
            int color2 = m.kind() == AbilityTracker.Kind.BLADE ? s.shadowColor : s.timeBowColor;
            double fade2 = Math.min(Math.clamp((double)(now - m.start()) / 180.0, 0.0, 1.0), Math.clamp((double)m.remaining(now) / 0.5, 0.0, 1.0));
            color2 = UiDraw.alpha(color2, (int)(200.0 * fade2));
            WorldOverlay.box(m.box(), color2);
            if (!s.names) continue;
            WorldOverlay.label(m.box().method_1005().method_1031(0.0, 1.1, 0.0), (m.kind() == AbilityTracker.Kind.BLADE ? "\u0422\u0435\u043d\u0435\u0432\u043e\u0439 \u043a\u043b\u0438\u043d\u043e\u043a" : "\u041b\u0443\u043a \u0432\u0440\u0435\u043c\u0435\u043d\u0438") + " \u00b7 \u2248" + (int)Math.ceil(m.remaining(now)) + " \u0441", color2);
        }
    }
}

