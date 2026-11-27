/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.hud;

import net.minecraft.class_310;
import net.minecraft.class_332;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.HudEditorScreen;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudMotion;

public final class HudStyle {
    public static final int TEXT = -921101;
    public static final int MUTED = -4802625;
    public static final int FAINT = -7763050;
    public static final int TRACK = -14013391;
    public static final int BAR = -6907228;
    private static final float TEXT_SCALE = 9.0f;
    private static final float LABEL_SCALE = 8.0f;
    private static final HudMotion motion = new HudMotion();
    private static boolean widgetSurface;

    static void beginWidget() {
        widgetSurface = false;
    }

    private HudStyle() {
    }

    public static void text(class_332 g, String value, int x, int y, int color, boolean shadow) {
        HudStyle.draw(g, CustomFont.regular(), value, x, y, 9.0f, color, shadow);
    }

    public static void text(class_332 g, String value, int x, int y, float size, int color, boolean shadow) {
        HudStyle.draw(g, CustomFont.regular(), value, x, y, size, color, shadow);
    }

    public static void strong(class_332 g, String value, int x, int y, int color, boolean shadow) {
        HudStyle.draw(g, CustomFont.medium(), value, x, y, 9.0f, color, shadow);
    }

    public static void label(class_332 g, String value, int x, int y, int color, boolean shadow) {
        HudStyle.draw(g, CustomFont.regular(), value, x, y, 8.0f, color, shadow);
    }

    public static int width(String value) {
        return HudStyle.measured(CustomFont.regular(), value, 9.0f);
    }

    public static int strongWidth(String value) {
        return HudStyle.measured(CustomFont.medium(), value, 9.0f);
    }

    public static int labelWidth(String value) {
        return HudStyle.measured(CustomFont.regular(), value, 8.0f);
    }

    public static String fit(String value, int maxWidth) {
        return HudStyle.fitted(CustomFont.regular(), value, maxWidth, 9.0f);
    }

    public static String fitStrong(String value, int maxWidth) {
        return HudStyle.fitted(CustomFont.medium(), value, maxWidth, 9.0f);
    }

    public static String fitLabel(String value, int maxWidth) {
        return HudStyle.fitted(CustomFont.regular(), value, maxWidth, 8.0f);
    }

    public static void panel(class_332 g, int width, int height, ClientConfig config) {
        HudStyle.surface(g, 0, 0, width, height, 4, config);
    }

    public static void surface(class_332 g, int x, int y, int width, int height, int radius, ClientConfig config) {
        HudStyle.surface(g, x, y, width, height, radius, config, 1.0);
    }

    public static void surface(class_332 g, int x, int y, int width, int height, int radius, ClientConfig config, double visibility) {
        int corner;
        if (!config.background || !config.hudDetails) {
            return;
        }
        int opacity = (int)((double)Math.clamp((long)config.backgroundAlpha, 0, 255) * Math.clamp(visibility, 0.0, 1.0));
        if (opacity == 0 || config.simpleHud) {
            return;
        }
        widgetSurface = true;
        if (config.simpleHud) {
            return;
        }
        boolean glass = "LIQUID_GLASS".equals(config.surfaceStyle);
        int n = corner = glass ? Math.min(Math.max(radius, 8), Math.min(width, height) / 2) : radius;
        if (glass && RenderQuality.shadows()) {
            UiDraw.rounded(g, x, y + 1, width, height, corner, UiDraw.alpha(0, opacity / 14));
        }
        LiquidGlass.surface(g, x, y, width, height, corner, UiDraw.alpha(ThemeColors.surface(config.backgroundColor, config.accent, ThemeColors.light()), opacity), config);
    }

    public static void separator(class_332 g, int x, int y, int height) {
        if (BloodHelperClient.config().simpleHud) {
            return;
        }
        g.method_25294(x, y + height / 4, x + 1, y + height - height / 4, ThemeColors.surface(894390874));
    }

    public static void divider(class_332 g, int x, int y, int width) {
        if (BloodHelperClient.config().simpleHud) {
            return;
        }
        g.method_25294(x, y, x + width, y + 1, ThemeColors.surface(810504794));
    }

    public static void resetMotion() {
        motion.reset();
    }

    public static void progress(class_332 g, String key, int x, int y, int width, float progress, int color) {
        float target = Float.isFinite(progress) ? Math.clamp(progress, 0.0f, 1.0f) : 0.0f;
        boolean animate = !BloodHelperClient.config().simpleHud && BloodHelperClient.config().animations && !(class_310.method_1551().field_1755 instanceof HudEditorScreen);
        float amount = (float)motion.value(key, target, System.nanoTime(), animate, RenderQuality.animationMillis(140.0));
        HudStyle.progress(g, x, y, width, amount, color);
    }

    public static void progress(class_332 g, int x, int y, int width, float progress, int color) {
        HudStyle.progress(g, x, y, width, progress, color, 1.0);
    }

    public static void progress(class_332 g, int x, int y, int width, float progress, int color, double visibility) {
        if (width <= 0) {
            return;
        }
        int opacity = (int)(255.0 * Math.clamp(visibility, 0.0, 1.0));
        UiDraw.rounded(g, x, y, width, 2, 1, UiDraw.alpha(ThemeColors.surface(-14013391), opacity));
        float amount = Float.isFinite(progress) ? Math.clamp(progress, 0.0f, 1.0f) : 0.0f;
        int filled = Math.round((float)width * amount);
        if (filled > 0) {
            UiDraw.rounded(g, x, y, filled, 2, 1, UiDraw.alpha(ThemeColors.icon(color), opacity));
        }
    }

    public static String tr(String ru, String en) {
        return HudStyle.tr(BloodHelperClient.config(), ru, en);
    }

    public static String tr(ClientConfig config, String ru, String en) {
        return "ru".equals(config.language) ? ru : en;
    }

    private static void draw(class_332 g, CustomFont font, String value, int x, int y, float size, int color, boolean shadow) {
        if (value == null || value.isEmpty()) {
            return;
        }
        ClientConfig cfg = BloodHelperClient.config();
        if (!(cfg.simpleHud || !cfg.hudAutoReadability || "LIQUID_GLASS".equals(cfg.surfaceStyle) && widgetSurface && cfg.background && cfg.hudDetails)) {
            int backing = ThemeColors.light() ? -654311425 : -1073741824;
            UiDraw.rounded(g, x - 1, y - 1, (int)Math.ceil(font.width(value, size)) + 2, (int)Math.ceil(size) + 3, 2, UiDraw.alpha(backing, (backing >>> 24) * (color >>> 24) / 255));
        }
        if (shadow && BloodHelperClient.config().textShadow && (cfg.simpleHud || RenderQuality.shadows())) {
            font.drawRaw(g, value, (float)x + 0.5f, (float)y + 0.5f, size, UiDraw.alpha(0, (color >>> 24) / 2));
        }
        font.draw(g, value, x, y, size, color);
    }

    private static int measured(CustomFont font, String value, float size) {
        return value == null ? 0 : (int)Math.ceil(font.width(value, size));
    }

    private static String fitted(CustomFont font, String value, int maxWidth, float size) {
        return font.ellipsize(value, size, maxWidth);
    }
}

