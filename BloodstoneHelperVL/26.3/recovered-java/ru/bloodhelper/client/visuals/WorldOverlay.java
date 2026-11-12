/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package ru.bloodhelper.client.visuals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.LegendaryVisualSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.lyrics.KineticLyricsRenderer;
import ru.bloodhelper.client.modules.WaypointProjection;
import ru.bloodhelper.client.visuals.WorldGeometry;
import ru.bloodhelper.client.visuals.WorldProjection;

public final class WorldOverlay {
    private static int labels;
    private static final int LABEL_LIMIT = 16;
    private static final List<int[]> occupied;
    private static final List<WorldGeometry.Line> lines;
    private static final List<WorldGeometry.Ring> rings;
    private static final List<Label> pendingLabels;

    private WorldOverlay() {
    }

    public static void begin() {
        labels = 0;
        occupied.clear();
        occupied.addAll(KineticLyricsRenderer.renderedBounds());
        for (HudRenderer.Bounds b : HudRenderer.bounds().values()) {
            occupied.add(new int[]{b.x() - 2, b.y() - 2, b.width() + 4, b.height() + 4});
        }
    }

    public static int labels() {
        return labels;
    }

    public static int segments() {
        return WorldGeometry.drawnLines() + WorldGeometry.drawnRings();
    }

    public static void extract() {
        lines.clear();
        rings.clear();
        pendingLabels.clear();
    }

    public static void line(Vec3 a, Vec3 b, int color, float thickness) {
        if (lines.size() < 2048 && a.distanceToSqr(b) > 1.0E-8) {
            lines.add(new WorldGeometry.Line(a, b, color, thickness));
        }
    }

    public static void ring(Vec3 center, double radius, int color) {
        if (rings.size() < 128 && Double.isFinite(radius) && radius > 0.0) {
            rings.add(new WorldGeometry.Ring(center, radius, color));
        }
    }

    public static void box(AABB b, int color) {
        for (int axis = 0; axis < 3; ++axis) {
            for (int u = 0; u < 2; ++u) {
                for (int v = 0; v < 2; ++v) {
                    double[] low = new double[]{b.minX, b.minY, b.minZ};
                    double[] high = new double[]{b.maxX, b.maxY, b.maxZ};
                    int a = (axis + 1) % 3;
                    int c = (axis + 2) % 3;
                    double[] from = (double[])low.clone();
                    double[] to = (double[])low.clone();
                    to[a] = u == 0 ? low[a] : high[a];
                    from[a] = to[a];
                    to[c] = v == 0 ? low[c] : high[c];
                    from[c] = to[c];
                    to[axis] = high[axis];
                    WorldOverlay.line(new Vec3(from[0], from[1], from[2]), new Vec3(to[0], to[1], to[2]), color, 1.6f);
                }
            }
        }
    }

    public static void label(Vec3 at, String text, int color) {
        if (pendingLabels.size() < 64) {
            pendingLabels.add(new Label(at, text, color));
        }
    }

    public static void drawWorld() {
        WorldGeometry.draw(lines, rings);
    }

    public static void drawLabels(GuiGraphicsExtractor g) {
        WorldOverlay.begin();
        for (Label label : pendingLabels) {
            WorldOverlay.label(g, label.at, label.text, label.color);
        }
    }

    public static void label(GuiGraphicsExtractor g, Vec3 at, String text, int color) {
        if (labels >= 16 || !WorldProjection.visible(at)) {
            return;
        }
        WaypointProjection.Point p = WorldProjection.project(at);
        if (p == null) {
            return;
        }
        LegendaryVisualSettings settings = BloodHelperClient.config().legendaryVisuals;
        float size = (float)(10.0 * settings.labelScale);
        CustomFont font = CustomFont.medium();
        text = font.ellipsize(text, size, 160.0f);
        if (text.isEmpty()) {
            return;
        }
        int w = (int)Math.ceil(font.width(text, size)) + 12;
        int h = (int)Math.ceil(size) + 8;
        int x = Math.round(p.x() - (float)w / 2.0f);
        int y = Math.round(p.y() - (float)h - 4.0f);
        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (x < 2 || x + w > sw - 2 || y < 2 || y + h > sh - 2) {
            return;
        }
        for (int attempt = 0; attempt < 6; ++attempt) {
            boolean collision = false;
            for (int[] r : occupied) {
                if (x >= r[0] + r[2] + 2 || x + w + 2 <= r[0] || y >= r[1] + r[3] + 2 || y + h + 2 <= r[1]) continue;
                collision = true;
                break;
            }
            if (!collision) break;
            if ((y -= h + 3) >= 2 && attempt != 5) continue;
            return;
        }
        occupied.add(new int[]{x, y, w, h});
        int opacity = color >>> 24;
        UiDraw.rounded(g, x, y, w, h, 5, UiDraw.alpha(1185052, (int)((double)opacity * 0.72)));
        UiDraw.rounded(g, x + 3, y + 4, 2, h - 8, 1, color);
        font.drawRaw(g, text, x + 8, y + 3, size, UiDraw.alpha(UiDraw.blend(color, -1, 0.65), opacity));
        ++labels;
    }

    static {
        occupied = new ArrayList<int[]>();
        lines = new ArrayList<WorldGeometry.Line>();
        rings = new ArrayList<WorldGeometry.Ring>();
        pendingLabels = new ArrayList<Label>();
    }

    public record Label(Vec3 at, String text, int color) {
    }
}

