/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package pyz1k.screen.menu;

import net.minecraft.class_310;
import net.minecraft.class_332;
import pyz1k.screen.menu.MenuScreen;

public final class MenuCloseAnimation {
    private static final long DURATION_NANOS = 350000000L;
    private static MenuScreen source;
    private static long startedAt;

    private MenuCloseAnimation() {
    }

    public static void start(MenuScreen screen) {
        if (screen != null) {
            source = screen;
            startedAt = System.nanoTime();
        }
    }

    public static void render(class_332 context) {
        MenuScreen screen = source;
        if (screen == null) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1755 != null) {
            MenuCloseAnimation.clear();
            return;
        }
        float progress = Math.clamp((float)(System.nanoTime() - startedAt) / 3.5E8f, 0.0f, 1.0f);
        if (progress >= 1.0f) {
            MenuCloseAnimation.clear();
            return;
        }
        float eased = progress * progress * (3.0f - 2.0f * progress);
        screen.renderSpatialAfterimage(context, 1.0f - eased, 0.0f, 0.0f);
    }

    public static void clear() {
        source = null;
        startedAt = 0L;
    }
}

