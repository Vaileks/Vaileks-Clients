/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_408
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.gui;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.HudStyle;

public final class PvpEndNotice {
    private static final long HOLD_NANOS = 3000000000L;
    private static final long FADE_NANOS = 200000000L;
    private static long shownAt;

    private PvpEndNotice() {
    }

    public static void show() {
        class_437 screen = class_310.method_1551().field_1755;
        shownAt = screen == null || screen instanceof class_408 ? System.nanoTime() : 0L;
    }

    public static void clear() {
        shownAt = 0L;
    }

    public static boolean visible() {
        return shownAt != 0L && System.nanoTime() - shownAt < 3200000000L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 g) {
        if (shownAt == 0L) {
            return;
        }
        class_310 client = class_310.method_1551();
        ClientConfig config = BloodHelperClient.config();
        if (!(config.pvpTimerEnabled() && config.pvp.endNotification && client.field_1724 != null && client.field_1687 != null && client.field_1724.method_5805())) {
            PvpEndNotice.clear();
            return;
        }
        long age = System.nanoTime() - shownAt;
        if (age >= 3000000000L + (config.animations ? 200000000L : 0L)) {
            PvpEndNotice.clear();
            return;
        }
        if (client.field_1690.field_1842) {
            return;
        }
        if (client.field_1755 != null && !(client.field_1755 instanceof class_408)) {
            PvpEndNotice.clear();
            return;
        }
        double visibility = config.animations ? PvpEndNotice.ease((double)age / 2.0E8) * (1.0 - PvpEndNotice.ease((double)(age - 3000000000L) / 2.0E8)) : 1.0;
        int alpha = (int)Math.round(255.0 * visibility);
        if (alpha <= 0) {
            return;
        }
        String title = HudStyle.tr(config, "PvP \u0440\u0435\u0436\u0438\u043c \u0437\u0430\u0432\u0435\u0440\u0448\u0451\u043d", "PvP mode ended");
        CustomFont font = CustomFont.medium();
        int width = (int)Math.ceil(font.width(title, 8.0f)) + 26;
        int height = 18;
        float scale = (float)Math.min(1.0, Math.min(((double)g.method_51421() - 8.0) / (double)width, ((double)g.method_51443() - 8.0) / (double)height));
        if (scale <= 0.0f) {
            return;
        }
        g.method_71048();
        g.method_51448().pushMatrix();
        try {
            g.method_51448().translate(((float)g.method_51421() - (float)width * scale) / 2.0f, (float)((double)(((float)g.method_51443() - (float)height * scale) / 2.0f) + (1.0 - visibility) * 5.0));
            g.method_51448().scale(scale, scale);
            HudStyle.surface(g, 0, 0, width, height, 5, config, visibility);
            UiIcons.draw(g, "check", 6, 4, 10, UiDraw.alpha(config.accent, alpha));
            font.draw(g, title, 20.0f, 5.0f, 8.0f, UiDraw.alpha(-921101, alpha));
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    private static double ease(double value) {
        return 1.0 - Math.pow(1.0 - Math.clamp(value, 0.0, 1.0), 3.0);
    }
}

