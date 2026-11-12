/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.player.LocalPlayer
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.WaypointSettings;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.modules.WaypointMath;
import ru.bloodhelper.client.modules.WaypointsModule;

public final class WaypointsWidget {
    private static final ArrayList<Hit> hits = new ArrayList();
    private static long drawnAt;
    private static Object connection;
    private static int screenWidth;
    private static int screenHeight;
    private static final List<WaypointSettings.Entry> EXAMPLES;

    private WaypointsWidget() {
    }

    public static List<Hit> hitRows() {
        return List.copyOf(hits);
    }

    public static boolean click(double x, double y, int button) {
        Minecraft mc = Minecraft.getInstance();
        ClientConfig c = BloodHelperClient.config();
        if (button != 1 || !(mc.gui.screen() instanceof ChatScreen) || BloodHelperClient.isChatHudEditing() || !c.waypoints.enabled || !c.waypoints.showList || HudRenderer.bounds().get("waypoints") == null || StreamerMode.hideCoordinates() || mc.getConnection() != connection || System.nanoTime() - drawnAt > 500000000L || mc.getWindow().getGuiScaledWidth() != screenWidth || mc.getWindow().getGuiScaledHeight() != screenHeight) {
            return false;
        }
        for (Hit hit : List.copyOf(hits)) {
            if (!hit.bounds.contains(x, y)) continue;
            return WaypointsModule.editSaved(hit.id, hit.scope, mc.gui.screen());
        }
        return false;
    }

    private static WaypointSettings.Entry example() {
        WaypointSettings.Entry entry = new WaypointSettings.Entry();
        entry.name = "Waypoint";
        entry.x = 128;
        entry.y = 64;
        entry.z = -256;
        return entry;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(GuiGraphicsExtractor g, ClientConfig config, boolean preview) {
        List<WaypointSettings.Entry> entries;
        hits.clear();
        drawnAt = 0L;
        if (StreamerMode.hideCoordinates() || !preview && !config.waypoints.showList) {
            return;
        }
        WaypointSettings settings = config.waypoints;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        List<WaypointSettings.Entry> list = entries = preview ? EXAMPLES : WaypointsModule.visible();
        if (entries.isEmpty() || !preview && player == null) {
            return;
        }
        int count = Math.min(entries.size(), settings.maxVisible);
        int contentY = config.hudTitle("waypoints") ? 22 : 0;
        int rowHeight = settings.showCoordinates ? 30 : 23;
        int width = 190;
        int height = contentY + rowHeight * count + 3;
        HudRenderer.Bounds bounds = HudRenderer.begin(g, "waypoints", settings.position, settings.alignment, settings.bottom, width, height);
        try {
            if (settings.background) {
                HudStyle.panel(g, width, height, config);
            }
            if (config.hudTitle("waypoints")) {
                HudStyle.strong(g, HudStyle.tr(config, "\u041c\u0435\u0442\u043a\u0438", "Waypoints"), 9, 4, -921101, settings.shadow);
                HudStyle.divider(g, 7, 19, width - 14);
            }
            for (int i = 0; i < count; ++i) {
                WaypointSettings.Entry entry = entries.get(i);
                int y = contentY + i * rowHeight;
                if (!preview) {
                    double my;
                    double mx;
                    double scale = HudRenderer.renderedScale("waypoints");
                    HudRenderer.Bounds hit = new HudRenderer.Bounds(bounds.x(), bounds.y() + (int)Math.round((double)y * scale), bounds.width(), (int)Math.ceil((double)rowHeight * scale));
                    hits.add(new Hit(entry.id, entry.scope, hit));
                    if (client.gui.screen() instanceof ChatScreen && !BloodHelperClient.isChatHudEditing() && hit.contains(mx = client.mouseHandler.xpos() * (double)client.getWindow().getGuiScaledWidth() / Math.max(1.0, (double)client.getWindow().getScreenWidth()), my = client.mouseHandler.ypos() * (double)client.getWindow().getGuiScaledHeight() / Math.max(1.0, (double)client.getWindow().getScreenHeight()))) {
                        UiDraw.roundedSolid(g, 4, y, width - 8, rowHeight - 1, 4, 1082035626);
                    }
                }
                Camera camera = client.gameRenderer.mainCamera();
                double bearing = preview ? -35.0 : WaypointMath.bearing(player.getX(), player.getZ(), camera.yRot(), (double)entry.x + 0.5, (double)entry.z + 0.5);
                WaypointsWidget.arrow(g, 14, y + 11, bearing, entry.color);
                double distance = preview ? 124.0 : WaypointMath.distance(player.getX(), player.getY(), player.getZ(), entry);
                String range = settings.showDistance ? Math.round(distance) + HudStyle.tr(config, " \u043c", " m") : "";
                int rangeX = width - 7 - HudStyle.labelWidth(range);
                String name = preview ? HudStyle.tr(config, "\u041c\u043e\u044f \u043c\u0435\u0442\u043a\u0430", "My waypoint") : entry.name;
                HudStyle.text(g, HudStyle.fit(name, Math.max(1, rangeX - 34)), 29, y + 5, -921101, settings.shadow);
                HudStyle.label(g, range, rangeX, y + 6, -4802625, settings.shadow);
                if (!settings.showCoordinates) continue;
                HudStyle.label(g, entry.x + "  " + entry.y + "  " + entry.z, 29, y + 18, -7763050, settings.shadow);
            }
            if (!preview) {
                drawnAt = System.nanoTime();
                connection = client.getConnection();
                screenWidth = client.getWindow().getGuiScaledWidth();
                screenHeight = client.getWindow().getGuiScaledHeight();
            }
        }
        finally {
            g.pose().popMatrix();
        }
    }

    private static void arrow(GuiGraphicsExtractor g, int x, int y, double degrees, int color) {
        g.pose().pushMatrix();
        g.pose().translate((float)x, (float)y);
        g.pose().rotate((float)Math.toRadians(degrees));
        UiDraw.rounded(g, -1, -5, 2, 12, 1, color);
        g.pose().pushMatrix();
        g.pose().translate(0.0f, -5.0f);
        g.pose().rotate(0.7853982f);
        UiDraw.rounded(g, 0, 0, 2, 7, 1, color);
        g.pose().popMatrix();
        g.pose().pushMatrix();
        g.pose().translate(0.0f, -5.0f);
        g.pose().rotate(-0.7853982f);
        UiDraw.rounded(g, -2, 0, 2, 7, 1, color);
        g.pose().popMatrix();
        g.pose().popMatrix();
    }

    static {
        EXAMPLES = List.of(WaypointsWidget.example());
    }

    public record Hit(String id, String scope, HudRenderer.Bounds bounds) {
    }
}

