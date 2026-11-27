/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1569
 *  net.minecraft.class_1657
 *  net.minecraft.class_239
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3966
 */
package ru.bloodhelper.client.modules;

import java.awt.Color;
import java.util.Arrays;
import net.minecraft.class_10799;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1569;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3966;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.compat.OptionalMixinHooks;
import ru.bloodhelper.client.config.CrosshairSettings;
import ru.bloodhelper.client.modules.CrosshairPattern;

public final class CrosshairRenderer {
    private static final class_2960 PIXEL = class_2960.method_60655((String)"bloodhelper", (String)"crosshair_pixel");
    private static ShapeKey key;
    private static CrosshairPattern.Mask shape;
    private static CrosshairPattern.Mask border;

    private CrosshairRenderer() {
    }

    public static boolean enabled() {
        return OptionalMixinHooks.crosshair() && BloodHelperClient.config().crosshair.enabled;
    }

    public static boolean showInF5() {
        return CrosshairRenderer.enabled() && OptionalMixinHooks.crosshairPerspective() && BloodHelperClient.config().crosshair.showInF5;
    }

    public static void render(class_332 g) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1690.field_1842) {
            return;
        }
        CrosshairRenderer.draw(g, g.method_51421() / 2, g.method_51443() / 2, BloodHelperClient.config().crosshair, 0, false);
    }

    public static void draw(class_332 g, int cx, int cy, CrosshairSettings c, int previewTarget, boolean preview) {
        int color;
        if (key == null || !key.matches(c)) {
            key = new ShapeKey(c.mode, c.preset, c.size, c.thickness, c.gap, c.dotSize, c.dot, c.outline, c.gridSize, (int[])c.pixels.clone());
            shape = CrosshairPattern.create(c);
            border = CrosshairPattern.outline(shape, c.outline);
        }
        int target = preview ? previewTarget : CrosshairRenderer.target();
        switch (c.colorMode) {
            case "DYNAMIC": {
                int n;
                if (target == 1) {
                    n = c.enemyColor;
                    break;
                }
                if (target == 2) {
                    n = c.friendlyColor;
                    break;
                }
                n = c.color;
                break;
            }
            case "CHROMA": {
                int n = Color.HSBtoRGB((float)(System.nanoTime() % 6000000000L) / 6.0E9f, 0.8f, 1.0f);
                break;
            }
            default: {
                int n = color = c.color;
            }
        }
        if (c.outline > 0) {
            CrosshairRenderer.paint(g, border, cx, cy, c.scale, CrosshairRenderer.alpha(c.outlineColor, c.opacity), false);
        }
        boolean invert = c.colorMode.equals("INVERT");
        CrosshairRenderer.paint(g, shape, cx, cy, c.scale, CrosshairRenderer.alpha(invert ? -1 : color, c.opacity), invert);
    }

    private static int target() {
        class_3966 hit;
        class_239 class_2392;
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || !((class_2392 = mc.field_1765) instanceof class_3966) || !((class_2392 = (hit = (class_3966)class_2392).method_17782()) instanceof class_1309)) {
            return 0;
        }
        class_1309 entity = (class_1309)class_2392;
        if (mc.field_1724.method_5722((class_1297)entity)) {
            return 2;
        }
        return entity instanceof class_1657 || entity instanceof class_1569 ? 1 : 2;
    }

    private static int alpha(int color, double opacity) {
        return color & 0xFFFFFF | (int)Math.round((double)(color >>> 24) * opacity) << 24;
    }

    public static void paint(class_332 g, CrosshairPattern.Mask mask, int cx, int cy, double scale, int color, boolean invert) {
        int n = mask.size();
        double center = (double)n / 2.0;
        for (int y = 0; y < n; ++y) {
            int x = 0;
            while (x < n) {
                if (!mask.at(x, y)) {
                    ++x;
                    continue;
                }
                int start = x;
                while (x < n && mask.at(x, y)) {
                    ++x;
                }
                int x0 = (int)Math.round((double)cx + ((double)start - center) * scale);
                int x1 = (int)Math.round((double)cx + ((double)x - center) * scale);
                int y0 = (int)Math.round((double)cy + ((double)y - center) * scale);
                int y1 = (int)Math.round((double)cy + ((double)(y + 1) - center) * scale);
                if (x1 <= x0 || y1 <= y0) continue;
                if (invert) {
                    g.method_52707(class_10799.field_56890, PIXEL, x0, y0, x1 - x0, y1 - y0, color);
                    continue;
                }
                g.method_25294(x0, y0, x1, y1, color);
            }
        }
    }

    private record ShapeKey(String mode, String preset, int size, int thickness, int gap, int dotSize, boolean dot, int outline, int grid, int[] pixels) {
        boolean matches(CrosshairSettings c) {
            return this.mode.equals(c.mode) && this.preset.equals(c.preset) && this.size == c.size && this.thickness == c.thickness && this.gap == c.gap && this.dotSize == c.dotSize && this.dot == c.dot && this.outline == c.outline && this.grid == c.gridSize && Arrays.equals(this.pixels, c.pixels);
        }
    }
}

