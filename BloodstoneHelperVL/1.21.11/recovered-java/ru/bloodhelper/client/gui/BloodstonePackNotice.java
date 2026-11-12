/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.gui;

import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.HudMotion;

public final class BloodstonePackNotice {
    private static final int WIDTH = 240;
    private static final int BODY_SIZE = 9;
    private static final int BODY_LINE_HEIGHT = 12;
    private static final int BUTTON_WIDTH = 72;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_X = 84;
    private static final int BUTTON_BOTTOM = 9;
    private static final int CLOSE_SIZE = 18;
    private static final int CLOSE_X = 213;
    private static final int CLOSE_Y = 8;
    private static final long VISIBLE_NANOS = 12000000000L;
    private static final long TRANSITION_NANOS = 220000000L;
    private static final HudMotion.Value buttonHover = new HudMotion.Value();
    private static boolean initialized;
    private static boolean enabled;
    private static long shownAt;
    private static long hideAt;
    private static double panelX;
    private static double panelY;
    private static double panelScale;
    private static int panelHeight;
    private static boolean interactive;
    private static Object languageToken;
    private static String language;
    private static String title;
    private static String dismissLabel;
    private static List<String> instruction;

    private BloodstonePackNotice() {
    }

    public static void initialize(boolean moduleEnabled) {
        initialized = true;
        enabled = moduleEnabled;
        hideAt = 0L;
        shownAt = 0L;
        interactive = false;
    }

    public static void settingsChanged(boolean moduleEnabled) {
        if (!initialized) {
            BloodstonePackNotice.initialize(moduleEnabled);
            return;
        }
        if (moduleEnabled && !enabled) {
            BloodstonePackNotice.showInstructions();
        } else if (!moduleEnabled && enabled) {
            BloodstonePackNotice.dismiss();
        }
        enabled = moduleEnabled;
    }

    public static void showInstructions() {
        shownAt = System.nanoTime();
        hideAt = shownAt + 12000000000L;
        interactive = false;
        buttonHover.to(0.0, shownAt, false, 0.0);
    }

    public static void dismiss() {
        if (shownAt != 0L) {
            hideAt = Math.min(hideAt, System.nanoTime());
        }
        interactive = false;
    }

    public static void render(class_332 g, double mouseX, double mouseY) {
        double leaving;
        if (shownAt == 0L) {
            return;
        }
        ClientConfig config = BloodHelperClient.config();
        long now = System.nanoTime();
        if (now >= hideAt + (config.animations ? 220000000L : 0L)) {
            shownAt = 0L;
            interactive = false;
            return;
        }
        double entering = config.animations ? BloodstonePackNotice.ease((double)(now - shownAt) / 2.2E8) : 1.0;
        double visibility = entering * (leaving = now <= hideAt ? 1.0 : 1.0 - BloodstonePackNotice.ease((double)(now - hideAt) / 2.2E8));
        int opacity = (int)Math.round(255.0 * visibility);
        if (opacity <= 0) {
            return;
        }
        Object token = UiLanguage.cacheToken();
        if (languageToken != token || !config.language.equals(language)) {
            languageToken = token;
            language = config.language;
            title = CustomFont.medium().ellipsize(BloodstonePackNotice.text("title"), 10.0f, 175.0f);
            dismissLabel = CustomFont.medium().ellipsize(BloodstonePackNotice.text("dismiss"), 9.0f, 60.0f);
            instruction = CustomFont.regular().wrap(BloodstonePackNotice.text("instruction"), 9.0f, 216.0f);
        }
        panelHeight = 66 + instruction.size() * 12;
        panelScale = Math.max(0.1, Math.min(1.0, Math.min(((double)g.method_51421() - 24.0) / 240.0, ((double)g.method_51443() - 24.0) / (double)panelHeight)));
        panelX = ((double)g.method_51421() - 240.0 * panelScale) * 0.5;
        panelY = ((double)g.method_51443() - (double)panelHeight * panelScale) * 0.5 + (1.0 - visibility) * 8.0;
        interactive = now < hideAt;
        double localX = (mouseX - panelX) / panelScale;
        double localY = (mouseY - panelY) / panelScale;
        int buttonY = BloodstonePackNotice.buttonY();
        boolean buttonOver = BloodstonePackNotice.contains(localX, localY, 84, buttonY, 72, 20);
        double hover = buttonHover.to(buttonOver ? 1.0 : 0.0, now, config.animations, 130.0);
        g.method_71048();
        g.method_51448().pushMatrix();
        g.method_51448().translate((float)panelX, (float)panelY);
        g.method_51448().scale((float)panelScale, (float)panelScale);
        UiDraw.rounded(g, 0, 2, 240, panelHeight, 10, UiDraw.alpha(0, opacity / 4));
        LiquidGlass.surfaceMenu(g, 0, 0, 240, panelHeight, 10, UiDraw.alpha(ThemeColors.surface(-15131613), (int)((double)opacity * 0.97)), config);
        if (!"OUTLINE".equals(config.surfaceStyle)) {
            UiDraw.outline(g, 0, 0, 240, panelHeight, 10, UiDraw.alpha(ThemeColors.icon(config.accent), opacity / 3));
        }
        UiIcons.draw(g, "info", 12, 11, 13, UiDraw.alpha(config.accent, opacity));
        CustomFont.medium().draw(g, title, 32.0f, 12.0f, 10.0f, UiDraw.alpha(-921101, opacity));
        boolean closeOver = BloodstonePackNotice.contains(localX, localY, 213, 8, 18, 18);
        if (closeOver) {
            UiDraw.rounded(g, 213, 8, 18, 18, 5, UiDraw.alpha(ThemeColors.surface(-11381151), opacity));
        }
        UiIcons.draw(g, "close", 217, 12, 10, UiDraw.alpha(-2696993, opacity));
        for (int i = 0; i < instruction.size(); ++i) {
            String line = instruction.get(i);
            float x = (240.0f - CustomFont.regular().width(line, 9.0f)) * 0.5f;
            CustomFont.regular().draw(g, line, x, 33 + i * 12, 9.0f, UiDraw.alpha(-2039324, opacity));
        }
        int buttonColor = UiDraw.blend(ThemeColors.surface(-12960184), config.accent, 0.16 + hover * 0.12);
        UiDraw.rounded(g, 84, buttonY, 72, 20, 6, UiDraw.alpha(buttonColor, opacity));
        CustomFont.medium().draw(g, dismissLabel, (240.0f - CustomFont.medium().width(dismissLabel, 9.0f)) * 0.5f, (float)(buttonY + 5) - (float)(hover * 0.7), 9.0f, UiDraw.alpha(-921101, opacity));
        g.method_51448().popMatrix();
    }

    public static boolean click(class_11909 event) {
        double y;
        if (!interactive || shownAt == 0L || System.nanoTime() >= hideAt) {
            return false;
        }
        double x = (event.comp_4798() - panelX) / panelScale;
        if (!BloodstonePackNotice.contains(x, y = (event.comp_4799() - panelY) / panelScale, 0, 0, 240, panelHeight)) {
            return false;
        }
        if (event.method_74245() == 0 && (BloodstonePackNotice.contains(x, y, 213, 8, 18, 18) || BloodstonePackNotice.contains(x, y, 84, BloodstonePackNotice.buttonY(), 72, 20))) {
            BloodstonePackNotice.dismiss();
        }
        return true;
    }

    private static int buttonY() {
        return panelHeight - 9 - 20;
    }

    private static boolean contains(double x, double y, int left, int top, int width, int height) {
        return x >= (double)left && x < (double)(left + width) && y >= (double)top && y < (double)(top + height);
    }

    private static double ease(double progress) {
        return 1.0 - Math.pow(1.0 - Math.clamp(progress, 0.0, 1.0), 3.0);
    }

    private static String text(String suffix) {
        return UiLanguage.text((class_2561)class_2561.method_43471((String)("bloodhelper.resource_pack.setup." + suffix)));
    }

    static {
        panelScale = 1.0;
        title = "";
        dismissLabel = "";
        instruction = List.of();
    }
}

