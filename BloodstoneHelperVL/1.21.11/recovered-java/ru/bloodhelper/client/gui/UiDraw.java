/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.gui;

import net.minecraft.class_332;
import ru.bloodhelper.client.gui.EmbeddedReGlass;
import ru.bloodhelper.client.gui.ReGlassCompat;
import ru.bloodhelper.client.gui.RoundedShape;
import ru.bloodhelper.client.gui.SimpleHud;

public final class UiDraw {
    private UiDraw() {
    }

    public static int alpha(int rgb, int opacity) {
        return Math.clamp((long)opacity, 0, 255) << 24 | rgb & 0xFFFFFF;
    }

    public static int blend(int first, int second, double amount) {
        double t = Math.clamp(amount, 0.0, 1.0);
        int r = (int)((double)(first >> 16 & 0xFF) * (1.0 - t) + (double)(second >> 16 & 0xFF) * t);
        int g = (int)((double)(first >> 8 & 0xFF) * (1.0 - t) + (double)(second >> 8 & 0xFF) * t);
        int b = (int)((double)(first & 0xFF) * (1.0 - t) + (double)(second & 0xFF) * t);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    public static void rounded(class_332 graphics, int x, int y, int w, int h, int radius, int color) {
        if (SimpleHud.active()) {
            UiDraw.roundedSolid(graphics, x, y, w, h, 0, color);
            return;
        }
        if (w >= 24 && h >= 18 && color >>> 24 >= 96 && (ReGlassCompat.draw(graphics, x, y, w, h, radius, color) || EmbeddedReGlass.draw(graphics, x, y, w, h, radius, color))) {
            return;
        }
        UiDraw.roundedSolid(graphics, x, y, w, h, radius, color);
    }

    public static void roundedSolid(class_332 graphics, int x, int y, int w, int h, int radius, int color) {
        if (radius <= 0 || SimpleHud.active()) {
            if (w > 0 && h > 0 && color >>> 24 != 0) {
                graphics.method_25294(x, y, x + w, y + h, color);
            }
            return;
        }
        RoundedShape.draw(graphics, x, y, w, h, radius, color, false);
    }

    public static void border(class_332 graphics, int x, int y, int w, int h, int radius, int fill, int border) {
        if (ReGlassCompat.installed() || EmbeddedReGlass.active()) {
            UiDraw.rounded(graphics, x, y, w, h, radius, fill);
            UiDraw.outline(graphics, x, y, w, h, radius, border);
        } else {
            UiDraw.rounded(graphics, x, y, w, h, radius, border);
            UiDraw.rounded(graphics, x + 1, y + 1, w - 2, h - 2, Math.max(0, radius - 1), fill);
        }
    }

    public static void outline(class_332 g, int x, int y, int w, int h, int radius, int color) {
        if (SimpleHud.active()) {
            return;
        }
        RoundedShape.draw(g, x, y, w, h, radius, color, true);
    }
}

