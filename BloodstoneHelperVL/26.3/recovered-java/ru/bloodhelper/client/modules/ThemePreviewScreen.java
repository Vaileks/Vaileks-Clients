/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 */
package ru.bloodhelper.client.modules;

import java.util.EnumMap;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.ThemePreset;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.MusicWidget;
import ru.bloodhelper.client.modules.ChatTools;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class ThemePreviewScreen
extends ModuleDetailScreen {
    private final EnumMap<ThemePreset, ClientConfig> previews = new EnumMap(ThemePreset.class);
    private ThemePreset selected = ThemePreset.BLOOD;

    public ThemePreviewScreen(Screen parent) {
        super(parent, HudStyle.tr("\u0422\u0435\u043c\u044b \u0441 \u043f\u0440\u0435\u0434\u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440\u043e\u043c", "Theme preview"));
        for (ThemePreset preset : ThemePreset.values()) {
            this.previews.put(preset, preset.preview(BloodHelperClient.config()));
        }
    }

    @Override
    protected int preferredPanelWidth() {
        return 740;
    }

    @Override
    protected int preferredPanelHeight() {
        return 440;
    }

    private void apply() {
        ConfigStore.applyAppearance(this.selected.appearance());
        MusicWidget.resetMotion();
        ChatTools.refreshTimestampColors();
        this.onClose();
    }

    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u0422\u0435\u043c\u044b \u0441 \u043f\u0440\u0435\u0434\u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440\u043e\u043c", "Theme preview"), mx, my);
        int column = Math.min(190, this.panelWidth / 3);
        int listX = this.left + 14;
        int right = this.left + column + 26;
        int previewWidth = Math.max(1, this.panelWidth - column - 40);
        int rowHeight = Math.clamp((long)((this.bodyBottom - this.bodyTop - 22) / 4 - 4), 18, 69);
        this.text(g, HudStyle.tr("\u0413\u043e\u0442\u043e\u0432\u044b\u0435 \u0442\u0435\u043c\u044b", "Built-in themes"), listX, this.bodyTop, -7105633);
        for (int i = 0; i < ThemePreset.values().length; ++i) {
            ThemePreset theme = ThemePreset.values()[i];
            this.card(g, theme, listX, this.bodyTop + 22 + i * (rowHeight + 4), column, rowHeight, mx, my);
        }
        ClientConfig candidate = this.previews.get((Object)this.selected);
        ThemePreviewScreen.raw(g, this.selected.title(), right, this.bodyTop, 14.0f, ThemeColors.text(-1381908));
        ThemePreviewScreen.raw(g, ThemePreviewScreen.fit(HudStyle.tr(this.selected.descriptionRu(), this.selected.descriptionEn()), 9.0f, previewWidth), right, this.bodyTop + 19, 9.0f, ThemeColors.text(-7105633));
        int previewY = this.bodyTop + 38;
        int previewHeight = Math.max(1, this.bodyBottom - previewY - 36);
        this.drawPreview(g, candidate, right, previewY, previewWidth, previewHeight);
        int applyWidth = Math.min(156, Math.max(1, (previewWidth - 8) / 2));
        int cancelWidth = Math.min(110, Math.max(1, (previewWidth - 8) / 2));
        int applyX = right + previewWidth - applyWidth;
        this.button(g, applyX - cancelWidth - 8, this.bodyBottom - 22, cancelWidth, HudStyle.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), mx, my, this::onClose);
        this.button(g, applyX, this.bodyBottom - 22, applyWidth, HudStyle.tr("\u041f\u0440\u0438\u043c\u0435\u043d\u0438\u0442\u044c ", "Apply ") + this.selected.title(), mx, my, this::apply);
        this.footer(g, HudStyle.tr("\u041c\u0435\u043d\u044f\u044e\u0442\u0441\u044f \u0442\u043e\u043b\u044c\u043a\u043e \u0446\u0432\u0435\u0442\u0430 \u0438 \u043f\u043e\u0432\u0435\u0440\u0445\u043d\u043e\u0441\u0442\u0438. \u041c\u043e\u0434\u0443\u043b\u0438 \u0438 \u0440\u0430\u0441\u043f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435 HUD \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u044e\u0442\u0441\u044f.", "Only colors and surfaces change. Modules and HUD placement stay as configured."));
    }

    private void card(GuiGraphicsExtractor g, ThemePreset theme, int x, int y, int w, int h, int mx, int my) {
        String material;
        ClientConfig candidate = this.previews.get((Object)theme);
        ModuleDetailScreen.Action action = new ModuleDetailScreen.Action(x, y, w, h, () -> {
            this.selected = theme;
        });
        this.actions.add(action);
        boolean chosen = this.selected == theme;
        boolean hovered = action.contains(mx, my);
        UiDraw.rounded(g, x, y, w, h, 8, ThemeColors.surface(chosen ? -13355970 : (hovered ? -13948107 : -14540247)));
        UiDraw.outline(g, x, y, w, h, 8, chosen ? candidate.accent : ThemeColors.line());
        int swatch = Math.min(34, h - 12);
        int sx = x + 8;
        int sy = y + (h - swatch) / 2;
        UiDraw.rounded(g, sx, sy, swatch, swatch, 6, ThemePreviewScreen.surface(candidate, candidate.backgroundColor));
        if (swatch >= 14) {
            UiDraw.rounded(g, sx + 5, sy + 6, Math.max(1, swatch - 10), 3, 1, candidate.accent);
        }
        if (swatch >= 24) {
            UiDraw.rounded(g, sx + 5, sy + 14, Math.max(1, swatch - 16), 2, 1, ThemePreviewScreen.foreground(candidate, true));
            UiDraw.rounded(g, sx + 5, sy + 20, Math.max(1, swatch - 12), 2, 1, ThemePreviewScreen.foreground(candidate, true));
        }
        int tx = sx + swatch + 9;
        int textWidth = w - (tx - x) - 8;
        ThemePreviewScreen.raw(g, ThemePreviewScreen.fit(theme.title(), 12.0f, textWidth), tx, (float)y + (float)h / 2.0f - (float)(h >= 32 ? 12 : 6), 12.0f, ThemeColors.text(-1381908));
        switch (candidate.surfaceStyle) {
            case "MATTE": {
                String string = HudStyle.tr("\u041c\u0430\u0442\u043e\u0432\u0430\u044f", "Matte");
                break;
            }
            case "OUTLINE": {
                String string = HudStyle.tr("\u041a\u043e\u043d\u0442\u0443\u0440\u043d\u0430\u044f", "Outline");
                break;
            }
            default: {
                String string = material = "LIGHT".equals(candidate.themeMode) ? HudStyle.tr("\u0421\u0432\u0435\u0442\u043b\u043e\u0435 \u0441\u0442\u0435\u043a\u043b\u043e", "Light glass") : HudStyle.tr("\u0422\u0451\u043c\u043d\u043e\u0435 \u0441\u0442\u0435\u043a\u043b\u043e", "Dark glass");
            }
        }
        if (h >= 32) {
            ThemePreviewScreen.raw(g, ThemePreviewScreen.fit(material, 8.0f, textWidth), tx, (float)y + (float)h / 2.0f + 5.0f, 8.0f, ThemeColors.text(-7105633));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawPreview(GuiGraphicsExtractor g, ClientConfig c, int x, int y, int w, int h) {
        UiDraw.rounded(g, x, y, w, h, 9, -13350568);
        g.enableScissor(x + 1, y + 1, x + w - 1, y + h - 1);
        try {
            int horizon = y + h * 2 / 3;
            g.fill(x, horizon, x + w, y + h, -12824249);
            for (int i = 0; i < 7; ++i) {
                int bw = Math.max(1, w / 7);
                int bx = x + i * bw;
                int height = 12 + i * 19 % 45;
                g.fill(bx, horizon - height, bx + bw + 1, horizon, i % 2 == 0 ? -13481919 : -13811652);
            }
            int fg = ThemePreviewScreen.foreground(c, false);
            int muted = ThemePreviewScreen.foreground(c, true);
            int pad = Math.min(14, Math.max(4, w / 20));
            int bannerWidth = Math.min(w - 2 * pad, 180);
            ThemePreviewScreen.panel(g, c, x + pad, y + 12, bannerWidth, 27, false);
            UiDraw.rounded(g, x + pad + 8, y + 21, 3, 10, 1, c.accent);
            ThemePreviewScreen.raw(g, ThemePreviewScreen.fit("BloodHelper  \u00b7  144 FPS", 9.0f, bannerWidth - 24), x + pad + 17, y + 21, 9.0f, fg);
            if (h < 144 || w < 180) {
                ThemePreviewScreen.raw(g, ThemePreviewScreen.fit(HudStyle.tr("\u041e\u0431\u0440\u0430\u0437\u0435\u0446 \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u044f", "Appearance sample"), 9.0f, w - pad * 2), x + pad, y + h - 20, 9.0f, -986121);
                return;
            }
            int menuW = Math.min(w - pad * 2, 288);
            int menuH = Math.min(112, h - 100);
            int menuX = x + (w - menuW) / 2;
            int menuY = y + 51;
            ThemePreviewScreen.panel(g, c, menuX, menuY, menuW, menuH, true);
            ThemePreviewScreen.raw(g, HudStyle.tr("\u041e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u0435", "Appearance"), menuX + 12, menuY + 10, 11.0f, fg);
            int pillWidth = Math.min(64, menuW / 3);
            UiDraw.rounded(g, menuX + menuW - pillWidth - 10, menuY + 9, pillWidth, 15, 4, UiDraw.alpha(c.accent, 42));
            ThemePreviewScreen.raw(g, ThemePreviewScreen.fit(this.selected.title(), 8.0f, pillWidth - 10), menuX + menuW - pillWidth - 5, menuY + 13, 8.0f, ThemeColors.text(c.accent, c.accent, "LIGHT".equals(c.themeMode)));
            if (menuH >= 78) {
                int sidebar = Math.max(48, menuW / 3);
                ThemePreviewScreen.raw(g, "HUD", menuX + 12, menuY + 38, 9.0f, muted);
                ThemePreviewScreen.raw(g, HudStyle.tr("\u0426\u0432\u0435\u0442\u0430", "Colors"), menuX + 12, menuY + 57, 9.0f, fg);
                UiDraw.rounded(g, menuX + 7, menuY + 55, 2, 12, 1, c.accent);
                int controlsX = menuX + sidebar;
                int controlsW = menuW - sidebar - 14;
                ThemePreviewScreen.raw(g, HudStyle.tr("\u0410\u043a\u0446\u0435\u043d\u0442", "Accent"), controlsX, menuY + 38, 9.0f, fg);
                for (int i = 0; i < 4; ++i) {
                    UiDraw.rounded(g, controlsX + i * 18, menuY + 55, 12, 12, 4, i == 0 ? c.accent : ThemePreviewScreen.surface(c, i == 1 ? -14474197 : (i == 2 ? -9407361 : -4867389)));
                }
                if (menuH >= 105) {
                    ThemePreviewScreen.raw(g, HudStyle.tr("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c", "Opacity"), controlsX, menuY + 78, 8.0f, muted);
                    UiDraw.rounded(g, controlsX, menuY + 94, controlsW, 2, 1, UiDraw.alpha(muted, 80));
                    int activeW = Math.max(1, controlsW * c.backgroundAlpha / 255);
                    UiDraw.rounded(g, controlsX, menuY + 94, activeW, 2, 1, c.accent);
                    UiDraw.rounded(g, controlsX + activeW - 3, menuY + 92, 6, 6, 3, c.accent);
                }
            }
            int hudY = y + h - 49;
            int hudW = Math.min(182, (w - 3 * pad) * 3 / 5);
            int secondW = w - 3 * pad - hudW;
            ThemePreviewScreen.panel(g, c, x + pad, hudY, hudW, 37, false);
            UiDraw.rounded(g, x + pad + 7, hudY + 8, 21, 21, 5, UiDraw.alpha(c.accent, 190));
            ThemePreviewScreen.raw(g, ThemePreviewScreen.fit(HudStyle.tr("\u041c\u0443\u0437\u044b\u043a\u0430 \u0440\u044f\u0434\u043e\u043c", "Music with you"), 9.0f, hudW - 42), x + pad + 36, hudY + 8, 9.0f, fg);
            UiDraw.rounded(g, x + pad + 36, hudY + 25, Math.max(1, hudW - 46), 2, 1, UiDraw.alpha(muted, 80));
            UiDraw.rounded(g, x + pad + 36, hudY + 25, Math.max(1, (hudW - 46) / 2), 2, 1, c.accent);
            int cooldownX = x + pad * 2 + hudW;
            ThemePreviewScreen.panel(g, c, cooldownX, hudY, secondW, 37, false);
            ThemePreviewScreen.raw(g, ThemePreviewScreen.fit(HudStyle.tr("\u0416\u0435\u043c\u0447\u0443\u0433", "Pearl"), 9.0f, secondW - 16), cooldownX + 8, hudY + 8, 9.0f, fg);
            ThemePreviewScreen.raw(g, "3.2 s", cooldownX + 8, hudY + 22, 8.0f, muted);
        }
        finally {
            g.disableScissor();
        }
        UiDraw.outline(g, x, y, w, h, 9, ThemeColors.line());
    }

    private static void panel(GuiGraphicsExtractor g, ClientConfig candidate, int x, int y, int w, int h, boolean menu) {
        int tint = ThemePreviewScreen.surface(candidate, menu ? -99018467 : candidate.backgroundColor);
        if (menu) {
            LiquidGlass.surfaceMenu(g, x, y, w, h, 8, tint, candidate);
        } else {
            LiquidGlass.surface(g, x, y, w, h, 8, UiDraw.alpha(tint, candidate.backgroundAlpha), candidate);
        }
    }

    private static int surface(ClientConfig c, int original) {
        return ThemeColors.surface(original, c.accent, "LIGHT".equals(c.themeMode));
    }

    private static int foreground(ClientConfig c, boolean muted) {
        return ThemeColors.text(muted ? -7105633 : -1381908, c.accent, "LIGHT".equals(c.themeMode));
    }

    private static String fit(String value, float size, int width) {
        return CustomFont.regular().ellipsize(value, size, Math.max(0, width));
    }

    private static void raw(GuiGraphicsExtractor g, String text, float x, float y, float size, int color) {
        CustomFont.regular().drawRaw(g, text, x, y, size, color);
    }
}

