/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package pyz1k.screen.menu.sections.settings;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import pyz1k.module.setting.ColorSetting;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class ColorSettingRenderer {
    private static final float ROW_HEIGHT = 16.0f;
    private static final float TITLE_Y = 0.5f;
    private static final float RIGHT_PAD = 1.0f;
    private static final float ICON_SIZE = 8.0f;
    private static final float SWATCH_SIZE = 8.0f;
    private static final float GAP = 4.0f;
    private static final float NAME_FADE_W = 3.0f;

    private ColorSettingRenderer() {
    }

    public static float measureHeight(ColorSetting setting, float width) {
        return 16.0f;
    }

    public static void render(GuiGraphicsExtractor context, UiAnimationEngine animation, String key, ColorSetting setting, float x, float y, float width, int mouseX, int mouseY, float alpha) {
        Layout layout = ColorSettingRenderer.layout(x, y, width);
        boolean hovered = ColorSettingRenderer.isHovered(layout.iconX, layout.iconY, 8.0f, 8.0f, mouseX, mouseY);
        float hover = animation.animate01("setting:color:hover:" + key, hovered ? 1.0f : 0.0f, 16.0f, 0.0f);
        int textColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int iconColor = ColorUtil.multiplyAlpha(ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, hover * 0.7f), alpha);
        int swatchBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha);
        int swatchColor = ColorUtil.multiplyAlpha(setting.getColor(), alpha);
        RenderUtil.drawTextFadeRight(context, setting.getName(), x, y + 0.5f, textColor, GFonts.MEDIUM, 8, layout.nameMaxW, 3.0f);
        RenderUtil.drawRoundRect(context, layout.swatchX, layout.swatchY, 8.0f, 8.0f, 3.0f, swatchColor);
        RenderUtil.drawBorder(context, layout.swatchX, layout.swatchY, 8.0f, 8.0f, 3.0f, swatchBorder, 0.0f, 0.6f, 1.0f);
        RenderUtil.drawImage(context, "textures/gui/icons/theme.png", layout.iconX, layout.iconY, 8.0f, 8.0f, 0.0f, iconColor, 0.0f);
    }

    public static boolean hitThemeButton(ColorSetting setting, float x, float y, float width, double mouseX, double mouseY) {
        Layout layout = ColorSettingRenderer.layout(x, y, width);
        return ColorSettingRenderer.isHovered(layout.iconX, layout.iconY, 8.0f, 8.0f, mouseX, mouseY);
    }

    private static Layout layout(float x, float y, float width) {
        float iconX = x + width - 1.0f - 8.0f;
        float iconY = y + 1.0f;
        float swatchX = iconX - 4.0f - 8.0f;
        float swatchY = y + 1.0f;
        float nameMaxW = Math.max(16.0f, swatchX - 4.0f - x);
        return new Layout(iconX, iconY, swatchX, swatchY, nameMaxW);
    }

    private static boolean isHovered(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    private record Layout(float iconX, float iconY, float swatchX, float swatchY, float nameMaxW) {
    }
}

