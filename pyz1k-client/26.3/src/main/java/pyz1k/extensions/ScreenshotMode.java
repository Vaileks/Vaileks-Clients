/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package pyz1k.extensions;

import net.minecraft.client.Minecraft;

public final class ScreenshotMode {
    private static boolean active;
    private static boolean previous;

    public static boolean active() {
        return active;
    }

    public static void toggle() {
        Minecraft mc = Minecraft.getInstance();
        if (active) {
            if (mc.gui.hud.isHidden() != previous) mc.gui.hud.toggle();
            active = false;
        } else {
            previous = mc.gui.hud.isHidden();
            active = true;
            if (!mc.gui.hud.isHidden()) mc.gui.hud.toggle();
        }
    }

    public static void reset() {
        if (active) {
            ScreenshotMode.toggle();
        }
    }
}

