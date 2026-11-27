/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_408
 *  net.minecraft.class_433
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_481
 *  net.minecraft.class_638
 *  net.minecraft.class_7923
 *  net.minecraft.class_9779
 */
package ru.bloodhelper.client.hud;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_433;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_481;
import net.minecraft.class_638;
import net.minecraft.class_7923;
import net.minecraft.class_9779;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.access.StaffAccess;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.CombatHudSettings;
import ru.bloodhelper.client.gui.BackgroundMenuScreen;
import ru.bloodhelper.client.gui.HudEditorScreen;
import ru.bloodhelper.client.gui.RenderMetrics;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.SimpleHud;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.AdaptiveHudLayout;
import ru.bloodhelper.client.hud.ArmorWidget;
import ru.bloodhelper.client.hud.BlastLeggingsWidget;
import ru.bloodhelper.client.hud.CombatHudMotion;
import ru.bloodhelper.client.hud.CooldownTracker;
import ru.bloodhelper.client.hud.CooldownWidget;
import ru.bloodhelper.client.hud.EffectsWidget;
import ru.bloodhelper.client.hud.FeatureAlerts;
import ru.bloodhelper.client.hud.GeneralHudWidget;
import ru.bloodhelper.client.hud.HeldCooldownWidget;
import ru.bloodhelper.client.hud.HudDefaultStack;
import ru.bloodhelper.client.hud.HudLayout;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.HudWidgets;
import ru.bloodhelper.client.hud.ItemCounterWidget;
import ru.bloodhelper.client.hud.MusicWidget;
import ru.bloodhelper.client.hud.NotificationWidget;
import ru.bloodhelper.client.hud.PerformanceTracker;
import ru.bloodhelper.client.hud.PerformanceWidget;
import ru.bloodhelper.client.hud.PvpWidget;
import ru.bloodhelper.client.hud.SessionWidget;
import ru.bloodhelper.client.hud.SizeTimerWidget;
import ru.bloodhelper.client.hud.WatermarkWidget;
import ru.bloodhelper.client.hud.WaypointsWidget;
import ru.bloodhelper.client.hud.WorldWaypoints;
import ru.bloodhelper.client.mixin.ContainerBoundsAccessor;
import ru.bloodhelper.client.modules.CombatHudModule;
import ru.bloodhelper.client.modules.StreamerMode;

public final class HudRenderer {
    private static final CombatHudMotion combatMotion = new CombatHudMotion();
    private static final Map<String, Double> renderedScales = new LinkedHashMap<String, Double>();
    private static boolean renderedCombatLayout;
    private static final Map<String, Bounds> bounds;
    private static final Map<String, Bounds> readOnlyBounds;
    private static final Map<String, Long> appeared;
    private static final Map<String, Long> seen;
    private static final CooldownTracker.Snapshot EMPTY_COOLDOWNS;
    private static Object lastWorld;
    private static Object lastConfig;
    private static boolean lastPreview;
    private static boolean lastEditing;
    private static boolean motionActive;

    private HudRenderer() {
    }

    public static Map<String, Bounds> bounds() {
        return readOnlyBounds;
    }

    public static boolean hiddenInPauseMenu() {
        class_310 mc = class_310.method_1551();
        return BloodHelperClient.config().hideHudInPauseMenu && mc.field_1755 instanceof class_433;
    }

    public static boolean renderedCombatLayout() {
        return renderedCombatLayout;
    }

    public static double renderedScale(String key) {
        return renderedScales.getOrDefault(key, 1.0);
    }

    public static void render(class_332 g, class_9779 delta) {
        PerformanceTracker.frame();
        class_310 mc = class_310.method_1551();
        if (!BloodHelperClient.config().music.enabled) {
            MusicWidget.releaseCover();
        }
        if (HudRenderer.hiddenInPauseMenu() || mc.field_1724 == null || mc.field_1690.field_1842 || mc.field_1755 instanceof HudEditorScreen) {
            bounds.clear();
            MusicWidget.clearControls();
            MusicWidget.cancelInteraction();
            renderedScales.clear();
            HudRenderer.resetMotion();
            return;
        }
        if (HudRenderer.screenOverlay() || !BloodHelperClient.config().hudAboveChat || mc.field_1755 instanceof class_408) {
            return;
        }
        HudRenderer.renderAll(g, false, false);
    }

    public static void renderBehindChat(class_332 g, class_9779 delta) {
        class_310 mc = class_310.method_1551();
        if (HudRenderer.hiddenInPauseMenu() || HudRenderer.screenOverlay() || BloodHelperClient.config().hudAboveChat || mc.field_1724 == null || mc.field_1690.field_1842 || mc.field_1755 instanceof HudEditorScreen) {
            return;
        }
        HudRenderer.renderAll(g, false, BloodHelperClient.isChatHudEditing());
    }

    public static boolean screenOverlay() {
        class_310 mc = class_310.method_1551();
        return !HudRenderer.hiddenInPauseMenu() && !BloodHelperClient.config().hudMenuEffects && mc.field_1724 != null && !mc.field_1690.field_1842 && mc.field_1755 != null && !(mc.field_1755 instanceof class_408) && !(mc.field_1755 instanceof HudEditorScreen) && !(mc.field_1755 instanceof BackgroundMenuScreen) && !mc.field_1729.method_1613();
    }

    public static void renderScreen(class_332 g) {
        if (!HudRenderer.screenOverlay()) {
            return;
        }
        g.method_71048();
        HudRenderer.renderAll(g, false, false);
        boolean previous = SimpleHud.enter(BloodHelperClient.config().simpleHud);
        try {
            MusicWidget.renderScreen(g);
        }
        finally {
            SimpleHud.restore(previous);
        }
    }

    public static void renderChat(class_332 g) {
        if (BloodHelperClient.config().hudAboveChat) {
            HudRenderer.renderAll(g, false, BloodHelperClient.isChatHudEditing());
        }
    }

    public static void renderPreview(class_332 g) {
        HudRenderer.renderAll(g, true, false);
    }

    public static void renderCombatPreview(class_332 g) {
        HudRenderer.renderAll(g, false, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderAll(class_332 g, boolean preview, boolean editingEnabledOnly) {
        long measurement = RenderMetrics.begin();
        boolean previous = SimpleHud.enter(BloodHelperClient.config().simpleHud);
        try {
            HudRenderer.renderWidgets(g, preview, editingEnabledOnly);
            if (!preview && !editingEnabledOnly) {
                HeldCooldownWidget.render(g, BloodHelperClient.config());
            }
        }
        finally {
            SimpleHud.restore(previous);
            RenderMetrics.endHud(measurement);
        }
    }

    private static void renderWidgets(class_332 g, boolean preview, boolean editingEnabledOnly) {
        CooldownTracker.Snapshot snapshot;
        ClientConfig c = BloodHelperClient.config();
        class_638 world = class_310.method_1551().field_1687;
        if (!motionActive || lastWorld != world || lastConfig != c || lastPreview != preview || lastEditing != editingEnabledOnly) {
            HudRenderer.resetMotion();
        }
        lastWorld = world;
        lastConfig = c;
        lastPreview = preview;
        lastEditing = editingEnabledOnly;
        motionActive = true;
        bounds.clear();
        renderedScales.clear();
        renderedCombatLayout = CombatHudModule.beginFrame(c);
        MusicWidget.clearControls();
        if (!c.music.enabled) {
            MusicWidget.releaseCover();
        }
        CooldownTracker.Snapshot snapshot2 = snapshot = c.cooldowns.enabled || c.pvpTimerEnabled() ? CooldownTracker.snapshot(c) : EMPTY_COOLDOWNS;
        if (!renderedCombatLayout) {
            HudRenderer.renderCombatWidgets(g, c, snapshot, preview);
        }
        if (c.watermark.enabled || preview) {
            WatermarkWidget.render(g, c, preview);
        }
        if ((c.music.enabled || preview) && (preview || !MusicWidget.screenOverlay())) {
            MusicWidget.render(g, c, preview);
        }
        if (!renderedCombatLayout && (c.armor.enabled || preview)) {
            ArmorWidget.render(g, c, preview);
        }
        if (c.itemCounter.enabled || preview) {
            ItemCounterWidget.render(g, c, preview);
        }
        GeneralHudWidget.render(g, c, preview);
        if (c.performance.enabled || preview) {
            PerformanceWidget.render(g, c, preview);
        }
        if (c.session.enabled || preview) {
            SessionWidget.render(g, c, preview);
        }
        if (c.notifications.enabled || FeatureAlerts.enabled(c) || preview) {
            NotificationWidget.render(g, c, preview);
        }
        if (c.effects.enabled && c.effects.showHud || preview) {
            EffectsWidget.render(g, c, preview);
        }
        if (c.waypoints.enabled || preview) {
            WaypointsWidget.render(g, c, preview);
        }
        if (c.sizeTimer.enabled || preview) {
            SizeTimerWidget.render(g, c, preview);
        }
        if (c.combatHud.enabled && c.combatHud.blastLeggings.enabled || preview) {
            BlastLeggingsWidget.render(g, c, preview);
        }
        if (!preview && c.waypoints.enabled) {
            WorldWaypoints.render(g, c);
        }
        if (renderedCombatLayout) {
            g.method_71048();
            HudRenderer.renderCombatWidgets(g, c, snapshot, preview);
            if (c.armor.enabled || preview) {
                ArmorWidget.render(g, c, preview);
            }
        }
        if (editingEnabledOnly) {
            HudRenderer.renderMissingEnabled(g, c);
        }
    }

    private static void renderCombatWidgets(class_332 g, ClientConfig c, CooldownTracker.Snapshot snapshot, boolean preview) {
        if (c.cooldowns.enabled || preview) {
            List<CooldownTracker.Entry> items = snapshot.items();
            if (preview && items.isEmpty()) {
                items = List.of(new CooldownTracker.Entry(class_2960.method_60656((String)"ender_pearl"), new class_1799((class_1935)class_1802.field_8634), HudStyle.tr("\u042d\u043d\u0434\u0435\u0440-\u0436\u0435\u043c\u0447\u0443\u0433", "Ender Pearl"), 8.4, 0.65f, true), new CooldownTracker.Entry(class_2960.method_60656((String)"golden_apple"), new class_1799((class_1935)class_1802.field_8463), HudStyle.tr("\u0417\u043e\u043b\u043e\u0442\u043e\u0435 \u044f\u0431\u043b\u043e\u043a\u043e", "Golden Apple"), 25.0, 0.4f, true));
            }
            if (!items.isEmpty()) {
                CooldownWidget.render(g, items, c);
            }
        }
        if (c.pvpTimerEnabled() && snapshot.pvp() != null || preview) {
            CooldownTracker.Entry entry = snapshot.pvp();
            if (entry == null) {
                entry = new CooldownTracker.Entry(class_2960.method_60656((String)"trident"), class_1799.field_8037, "PvP", 30.0, 1.0f, true);
            }
            PvpWidget.render(g, entry, c);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderMissingEnabled(class_332 g, ClientConfig c) {
        for (HudWidgets.Widget widget : HudWidgets.all(c)) {
            String key = widget.key();
            if (bounds.containsKey(key) || !HudRenderer.enabled(key, c)) continue;
            if ("blastLeggings".equals(key)) {
                BlastLeggingsWidget.render(g, c, true);
                continue;
            }
            if ("pvp".equals(key)) {
                PvpWidget.render(g, new CooldownTracker.Entry(class_2960.method_60656((String)"trident"), class_1799.field_8037, "PvP", Double.NaN, 0.0f, true), c);
                continue;
            }
            String title = widget.title();
            String message = HudStyle.tr(c, "\u041d\u0435\u0442 \u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0445 \u0434\u0430\u043d\u043d\u044b\u0445", "No active data");
            String icon = "modules";
            int width = 156;
            int height = 44;
            class_1799 item = class_1799.field_8037;
            if ("cooldowns".equals(key)) {
                message = HudStyle.tr(c, "\u041d\u0435\u0442 \u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0445 \u043f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043e\u043a", "No active cooldowns");
                icon = "clock";
            } else if ("music".equals(key)) {
                message = HudStyle.tr(c, "\u041c\u0443\u0437\u044b\u043a\u0430 \u043d\u0435 \u0432\u043e\u0441\u043f\u0440\u043e\u0438\u0437\u0432\u043e\u0434\u0438\u0442\u0441\u044f", "No music playing");
                icon = "music";
                width = CombatHudModule.compactMusic(c) ? 190 : 214;
                height = CombatHudModule.compactMusic(c) ? 36 : 75;
            } else if ("notifications".equals(key)) {
                message = HudStyle.tr(c, "\u041d\u0435\u0442 \u043d\u043e\u0432\u044b\u0445 \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0439", "No new notifications");
                icon = "bell";
                width = 214;
                height = c.hudDetails ? 43 : 32;
            } else if ("effects".equals(key)) {
                message = HudStyle.tr(c, "\u041d\u0435\u0442 \u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0445 \u044d\u0444\u0444\u0435\u043a\u0442\u043e\u0432", "No active effects");
                icon = "effects";
                width = c.effects.showNames ? 180 : 120;
            } else if (key.startsWith("armor")) {
                message = HudStyle.tr(c, "\u041d\u0435\u0442 \u044d\u043a\u0438\u043f\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u043e\u0439 \u0431\u0440\u043e\u043d\u0438", "No armor equipped");
                icon = "armor";
                if (key.startsWith("armor.")) {
                    width = c.armor.showNames ? 188 : 58;
                    height = c.armor.showNames ? 36 : 45;
                    message = "\u2014";
                } else {
                    width = "VERTICAL".equals(c.armor.orientation) ? (c.armor.showNames ? 188 : 104) : (c.armor.showNames ? 392 : 160);
                    height = (c.hudDetails ? 21 : 0) + ("VERTICAL".equals(c.armor.orientation) ? 104 : (c.armor.showNames ? 61 : 45));
                }
            } else if (key.startsWith("itemCounter.")) {
                class_2960 id = class_2960.method_12829((String)key.substring("itemCounter.".length()));
                if (id != null) {
                    item = class_7923.field_41178.method_17966(id).map(class_1799::new).orElse(class_1799.field_8037);
                }
                if (item.method_7960()) continue;
                title = UiLanguage.text(item.method_7964());
                message = "0";
                width = c.itemCounter.showNames ? 174 : 58;
                height = c.itemCounter.showNames ? 36 : 42;
            } else if ("itemCounter".equals(key)) {
                message = HudStyle.tr(c, "\u0412\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0445 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u043d\u0435\u0442", "Selected items are absent");
                width = 174;
            } else if ("performance".equals(key)) {
                icon = "performance";
                message = HudStyle.tr(c, "\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u0435\u043b\u0438 \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u044b", "Metrics are hidden");
            } else if ("sizeTimer".equals(key)) {
                message = HudStyle.tr(c, "\u0420\u0430\u0437\u043c\u0435\u0440 \u043e\u0431\u044b\u0447\u043d\u044b\u0439", "Normal size");
                icon = "clock";
                width = 180;
            } else if ("waypoints".equals(key)) {
                message = HudStyle.tr(c, "\u0412 \u044d\u0442\u043e\u043c \u0438\u0437\u043c\u0435\u0440\u0435\u043d\u0438\u0438 \u043d\u0435\u0442 \u0432\u0438\u0434\u0438\u043c\u044b\u0445 \u043c\u0435\u0442\u043e\u043a", "No visible waypoints in this dimension");
                width = 190;
            }
            HudRenderer.begin(g, key, widget.position(), widget.alignment(), widget.bottom(), width, height);
            try {
                if (c.background && c.hudDetails) {
                    HudStyle.surface(g, 0, 0, width, height, "music".equals(key) ? 14 : 8, c);
                }
                int centerY = Math.max(2, (height - 30) / 2);
                if (!item.method_7960()) {
                    g.method_51427(item, 6, centerY + 6);
                } else {
                    UiIcons.draw(g, icon, 7, centerY + 7, 13, -4802625);
                }
                int textX = width < 100 ? 24 : 28;
                HudStyle.strong(g, HudStyle.fitStrong(title, width - textX - 6), textX, centerY + 2, -921101, c.textShadow);
                HudStyle.label(g, HudStyle.fitLabel(message, width - textX - 6), textX, centerY + 17, -4802625, c.textShadow);
            }
            finally {
                g.method_51448().popMatrix();
            }
        }
    }

    private static boolean enabled(String key, ClientConfig c) {
        if (key.startsWith("performance.")) {
            return c.performance.enabled;
        }
        if (key.startsWith("watermark.")) {
            return c.watermark.enabled;
        }
        if (key.startsWith("armor")) {
            return c.armor.enabled;
        }
        if (key.startsWith("itemCounter")) {
            return c.itemCounter.enabled;
        }
        return switch (key) {
            case "blastLeggings" -> {
                if (c.combatHud.enabled && c.combatHud.blastLeggings.enabled) {
                    yield true;
                }
                yield false;
            }
            case "cooldowns" -> c.cooldowns.enabled;
            case "pvp" -> c.pvpTimerEnabled();
            case "music" -> c.music.enabled;
            case "notifications" -> {
                if (c.notifications.enabled || FeatureAlerts.enabled(c)) {
                    yield true;
                }
                yield false;
            }
            case "effects" -> {
                if (c.effects.enabled && c.effects.showHud) {
                    yield true;
                }
                yield false;
            }
            case "performance" -> c.performance.enabled;
            case "watermark" -> c.watermark.enabled;
            case "session" -> c.session.enabled;
            case "spectatorTimer" -> {
                if (StaffAccess.active() && c.spectatorTimer.enabled) {
                    yield true;
                }
                yield false;
            }
            case "staffStats" -> {
                if (StaffAccess.active() && c.staffStats.enabled) {
                    yield true;
                }
                yield false;
            }
            case "staffStatus" -> {
                if (StaffAccess.active() && c.staffWorkspace.status) {
                    yield true;
                }
                yield false;
            }
            case "sizeTimer" -> c.sizeTimer.enabled;
            case "waypoints" -> {
                if (c.waypoints.enabled && c.waypoints.showList && !StreamerMode.hideCoordinates()) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    private static void resetMotion() {
        if (!motionActive) {
            return;
        }
        appeared.clear();
        seen.clear();
        combatMotion.reset();
        renderedScales.clear();
        EffectsWidget.resetMotion();
        CooldownWidget.resetMotion();
        HudStyle.resetMotion();
        MusicWidget.resetMotion();
        NotificationWidget.resetMotion();
        motionActive = false;
    }

    public static boolean clickMusic(double x, double y, int button) {
        return MusicWidget.click(x, y, button);
    }

    public static Bounds begin(class_332 g, String key, ClientConfig.HudPosition p, String align, boolean bottom, int w, int h) {
        HudStyle.beginWidget();
        ClientConfig c = BloodHelperClient.config();
        if (renderedCombatLayout) {
            CombatHudSettings.Pose pose = CombatHudModule.pose(key, p, align, bottom, c);
            p = pose.position;
            align = pose.alignment;
            bottom = pose.bottom;
        }
        HudLayout.Placement target = HudLayout.place(g.method_51421(), g.method_51443(), w, h, p.scale, p.x, p.y, align, bottom);
        if ("watermark".equals(key)) {
            target = HudDefaultStack.watermark(c, renderedCombatLayout, target, MusicWidget.reservedPlacement(c, g.method_51421(), g.method_51443()), g.method_51443());
        } else if ("music".equals(key)) {
            Bounds watermark = bounds.get("watermark");
            target = HudDefaultStack.music(c, renderedCombatLayout, target, watermark == null ? -1 : watermark.y() + watermark.height(), g.method_51443());
        }
        target = AdaptiveHudLayout.place(c.hudEditor, key, renderedCombatLayout, p, align, bottom, target, g.method_51421(), g.method_51443());
        long now = System.nanoTime();
        boolean editing = class_310.method_1551().field_1755 instanceof HudEditorScreen || BloodHelperClient.isChatHudEditing();
        HudLayout.Placement placement = combatMotion.place(key, target, w, h, g.method_51421(), g.method_51443(), renderedCombatLayout, editing, c.animations, now);
        renderedScales.put(key, placement.scale());
        Long previous = seen.put(key, now);
        if (previous == null || now - previous > 500000000L) {
            appeared.put(key, now);
        }
        double enter = Math.clamp((double)(now - appeared.getOrDefault(key, now)) / (RenderQuality.animationMillis(160.0) * 1000000.0), 0.0, 1.0);
        int slide = BloodHelperClient.config().animations && !editing ? (int)Math.round(5.0 * Math.pow(1.0 - enter, 3.0)) : 0;
        int drawY = Math.clamp((long)(placement.y() + slide), 0, Math.max(0, g.method_51443() - placement.height()));
        Bounds result = new Bounds(placement.x(), drawY, placement.width(), placement.height());
        class_437 class_4372 = class_310.method_1551().field_1755;
        if (class_4372 instanceof class_465) {
            int pad;
            class_465 screen = (class_465)class_4372;
            ContainerBoundsAccessor area = (ContainerBoundsAccessor)screen;
            int n = pad = screen instanceof class_481 ? 30 : 8;
            if (result.x() < area.bloodhelper$left() + area.bloodhelper$width() + pad && result.x() + result.width() > area.bloodhelper$left() - pad && result.y() < area.bloodhelper$top() + area.bloodhelper$height() + pad && result.y() + result.height() > area.bloodhelper$top() - pad) {
                result = new Bounds(-10000, -10000, result.width(), result.height());
            }
        }
        bounds.put(key, result);
        g.method_51448().pushMatrix();
        g.method_51448().translate((float)result.x(), (float)result.y());
        g.method_51448().scale((float)placement.scale(), (float)placement.scale());
        return result;
    }

    static {
        bounds = new LinkedHashMap<String, Bounds>();
        readOnlyBounds = Collections.unmodifiableMap(bounds);
        appeared = new LinkedHashMap<String, Long>();
        seen = new LinkedHashMap<String, Long>();
        EMPTY_COOLDOWNS = new CooldownTracker.Snapshot(List.of(), null);
    }

    public record Bounds(int x, int y, int width, int height) {
        public boolean contains(double px, double py) {
            return px >= (double)this.x && py >= (double)this.y && px < (double)(this.x + this.width) && py < (double)(this.y + this.height);
        }
    }
}

