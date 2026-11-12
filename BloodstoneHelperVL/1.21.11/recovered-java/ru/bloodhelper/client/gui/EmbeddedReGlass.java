/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.gui;

import net.minecraft.class_332;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.ReGlassCompat;

public final class EmbeddedReGlass {
    private static long draws;

    private EmbeddedReGlass() {
    }

    public static boolean active() {
        return !ReGlassCompat.installed() && BloodHelperClient.config().reglassEnabled && "LIQUID_GLASS".equals(BloodHelperClient.config().surfaceStyle);
    }

    public static long draws() {
        return draws;
    }

    static void recordDraw() {
        ++draws;
    }

    public static boolean draw(class_332 g, int x, int y, int w, int h, int radius, int color) {
        if (!EmbeddedReGlass.active()) {
            return false;
        }
        ClientConfig c = BloodHelperClient.config();
        return LiquidGlass.draw(g, x, y, w, h, radius, color, c.glassBlur, c.glassRefraction);
    }
}

