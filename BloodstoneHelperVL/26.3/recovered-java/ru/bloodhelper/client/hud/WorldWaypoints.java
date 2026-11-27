/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.WaypointSettings;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.modules.WaypointLabelLayout;
import ru.bloodhelper.client.modules.WaypointProjection;
import ru.bloodhelper.client.modules.WaypointsModule;

public final class WorldWaypoints {
    private static final Matrix4f transform = new Matrix4f();
    private static Vec3 camera = Vec3.ZERO;
    private static Object world;
    private static long captured;
    private static final List<WaypointLabelLayout.Rect> occupied;
    private static final List<Runnable> labels;
    private static final List<HudRenderer.Bounds> drawn;

    private WorldWaypoints() {
    }

    public static void capture(CameraRenderState source, Matrix4f view, Matrix4f projection) {
        if (!BloodHelperClient.config().waypoints.enabled) {
            WorldWaypoints.reset();
            return;
        }
        transform.set((Matrix4fc)projection).mul((Matrix4fc)view);
        camera = source.pos;
        world = Minecraft.getInstance().level;
        captured = System.nanoTime();
    }

    public static void reset() {
        world = null;
        captured = 0L;
        drawn.clear();
    }

    public static void beginFrame() {
        drawn.clear();
    }

    public static List<HudRenderer.Bounds> renderedBounds() {
        return List.copyOf(drawn);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(GuiGraphicsExtractor g, ClientConfig config) {
        drawn.clear();
        labels.clear();
        Minecraft mc = Minecraft.getInstance();
        if (!config.waypoints.showWorld || mc.level != world || world == null || mc.player == null || mc.gui.screen() != null || mc.gui.hud.isHidden() || StreamerMode.hideCoordinates() || System.nanoTime() - captured > 500000000L) {
            return;
        }
        occupied.clear();
        for (HudRenderer.Bounds b : HudRenderer.bounds().values()) {
            if (b.x() < 0 || b.y() < 0) continue;
            occupied.add(new WaypointLabelLayout.Rect(b.x(), b.y(), b.width(), b.height()));
        }
        WaypointsModule.Ping ping = WaypointsModule.ping();
        if (ping != null) {
            WorldWaypoints.draw(g, config, ping.x(), ping.y(), ping.z(), HudStyle.tr(config, "\u041c\u0435\u0442\u043a\u0430", "Ping"), config.accent, (int)Math.ceil(WaypointsModule.pingSeconds()) + HudStyle.tr(config, " \u0441", " s"));
        }
        for (WaypointSettings.Entry entry : WaypointsModule.visible().stream().sorted(Comparator.comparing(e -> e.id)).toList()) {
            WorldWaypoints.draw(g, config, (double)entry.x + 0.5, (double)entry.y + 1.15, (double)entry.z + 0.5, entry.name, entry.color, "");
        }
        try {
            for (Runnable label : labels) {
                label.run();
            }
        }
        finally {
            labels.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void draw(GuiGraphicsExtractor g, ClientConfig c, double x, double y, double z, String name, int color, String expiry) {
        Object detail;
        int sh;
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        WaypointProjection.Point p = WaypointProjection.project(transform, x - WorldWaypoints.camera.x, y - WorldWaypoints.camera.y, z - WorldWaypoints.camera.z, sw, sh = mc.getWindow().getGuiScaledHeight());
        if (p == null) {
            return;
        }
        String label = HudStyle.fitStrong(name, 145);
        Object object = detail = c.waypoints.showDistance ? Math.round(mc.player.position().distanceTo(new Vec3(x, y, z))) + HudStyle.tr(c, " \u043c", " m") : "";
        if (c.waypoints.showCoordinates) {
            detail = (String)detail + (((String)detail).isEmpty() ? "" : " \u00b7 ") + (int)Math.floor(x) + " " + (int)Math.floor(y - (expiry.isEmpty() ? 1.15 : 0.0) + 1.0E-6) + " " + (int)Math.floor(z);
        }
        if (!expiry.isEmpty()) {
            detail = (String)detail + (((String)detail).isEmpty() ? "" : " \u00b7 ") + expiry;
        }
        int w = Math.max(65, Math.max(HudStyle.strongWidth(label), HudStyle.labelWidth((String)detail)) + 20);
        int h = ((String)detail).isEmpty() ? 24 : 39;
        float scale = (float)(0.9 * c.waypoints.worldScale);
        int bw = (int)Math.ceil((float)w * scale);
        int bh = (int)Math.ceil((float)h * scale);
        WaypointLabelLayout.Rect placed = WaypointLabelLayout.place(p.x(), p.y(), bw, bh, sw, sh, occupied);
        UiDraw.roundedSolid(g, Math.round(p.x()) - 4, Math.round(p.y()) - 4, 8, 8, 4, -1340992228);
        UiDraw.roundedSolid(g, Math.round(p.x()) - 2, Math.round(p.y()) - 2, 4, 4, 2, color);
        if (placed == null) {
            return;
        }
        int px = placed.x();
        int py = placed.y();
        occupied.add(placed);
        drawn.add(new HudRenderer.Bounds(px, py, bw, bh));
        int endX = Math.round(Math.clamp(p.x(), (float)(px + 4), (float)(px + bw - 4)));
        int endY = Math.round(p.y() < (float)py ? (float)py : (float)(py + bh));
        double dx = (float)endX - p.x();
        double dy = (float)endY - p.y();
        double length = Math.hypot(dx, dy);
        if (length > 5.0) {
            g.pose().pushMatrix();
            try {
                g.pose().translate(p.x(), p.y());
                g.pose().rotate((float)Math.atan2(dy, dx));
                UiDraw.roundedSolid(g, 0, 0, (int)Math.ceil(length), 1, 0, UiDraw.alpha(color, 155));
            }
            finally {
                g.pose().popMatrix();
            }
        }
        Object shownDetail = detail;
        labels.add(() -> WorldWaypoints.lambda$draw$0(g, px, py, scale, c, w, h, color, label, (String)shownDetail));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static /* synthetic */ void lambda$draw$0(GuiGraphicsExtractor g, int px, int py, float scale, ClientConfig c, int w, int h, int color, String label, String shownDetail) {
        g.pose().pushMatrix();
        g.pose().translate((float)px, (float)py);
        g.pose().scale(scale, scale);
        try {
            HudStyle.beginWidget();
            if (c.waypoints.background && c.background) {
                HudStyle.panel(g, w, h, c);
            }
            UiDraw.rounded(g, 5, 6, 2, h - 12, 1, color);
            HudStyle.strong(g, label, 12, 5, -921101, c.waypoints.shadow);
            if (!shownDetail.isEmpty()) {
                HudStyle.label(g, shownDetail, 12, 23, -4802625, c.waypoints.shadow);
            }
        }
        finally {
            g.pose().popMatrix();
        }
    }

    static {
        occupied = new ArrayList<WaypointLabelLayout.Rect>();
        labels = new ArrayList<Runnable>();
        drawn = new ArrayList<HudRenderer.Bounds>();
    }
}

