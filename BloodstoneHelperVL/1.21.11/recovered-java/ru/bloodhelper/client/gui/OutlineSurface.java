/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.gui;

import net.minecraft.class_332;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;

public final class OutlineSurface {
    private OutlineSurface() {
    }

    public static Colors colors(int opacity, int accent, boolean light, boolean menu) {
        int alpha = Math.clamp((long)opacity, 0, 255);
        int fill = light ? 16054009 : 0x11161D;
        int rim = ThemeColors.icon(accent | 0xFF000000, accent, light);
        return new Colors(UiDraw.alpha(fill, (int)Math.round((double)alpha * (menu ? 0.92 : (light ? 0.8 : 0.68)))), UiDraw.alpha(rim, alpha));
    }

    public static void draw(class_332 graphics, int x, int y, int width, int height, int radius, int opacity, ClientConfig config, boolean menu) {
        if (width <= 0 || height <= 0 || opacity <= 0) {
            return;
        }
        Colors colors = OutlineSurface.colors(opacity, config.accent, "LIGHT".equals(config.themeMode), menu);
        UiDraw.rounded(graphics, x, y, width, height, radius, colors.fill());
        UiDraw.outline(graphics, x, y, width, height, radius, colors.rim());
    }

    public record Colors(int fill, int rim) {
    }
}

