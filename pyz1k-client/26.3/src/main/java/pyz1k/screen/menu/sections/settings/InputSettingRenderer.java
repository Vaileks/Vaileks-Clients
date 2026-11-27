/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package pyz1k.screen.menu.sections.settings;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import pyz1k.module.setting.InputSetting;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class InputSettingRenderer {
    private static final float TITLE_SIZE = 7.5f;
    private static final float INPUT_H = 14.0f;
    private static final float LABEL_GAP = 4.0f;
    private static final float INPUT_PAD_X = 5.0f;

    private InputSettingRenderer() {
    }

    public static float measureHeight(InputSetting setting, float width) {
        return 25.5f;
    }

    public static void render(GuiGraphicsExtractor context, String key, InputSetting setting, float x, float y, float width, int mouseX, int mouseY, float alpha, boolean focused, int caretIndex) {
        int titleColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int bgColor = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha);
        int borderColor = ColorUtil.multiplyAlpha(focused ? ClientColors.CATEGORY_ACCENT : ClientColors.SETTINGS_OUTLINE, alpha);
        int textColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int placeholderColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha);
        RenderUtil.drawTextFadeRight(context, setting.getName(), x, y, titleColor, GFonts.MEDIUM, 7.5f, width, 7.0f);
        float boxY = y + 7.5f + 4.0f;
        RenderUtil.drawRoundRect(context, x, boxY, width, 14.0f, 3.0f, bgColor);
        RenderUtil.drawBorder(context, x, boxY, width, 14.0f, 3.0f, borderColor, 0.0f, 0.5f, 1.0f);
        String value = setting.getValue();
        boolean empty = value == null || value.isEmpty();
        String drawText = empty ? "Enter value..." : value;
        RenderUtil.drawTextFadeRight(context, drawText, x + 5.0f, boxY + 4.0f, empty ? placeholderColor : textColor, GFonts.REGULAR, 6.5f, width - 10.0f, 8.0f);
        if (focused) {
            String safe = value == null ? "" : value;
            int caret = Math.max(0, Math.min(caretIndex, safe.length()));
            float caretX = x + 5.0f + RenderUtil.getTextWidth(safe.substring(0, caret), GFonts.REGULAR, 6.5f);
            RenderUtil.drawRoundRect(context, caretX, boxY + 3.0f, 1.0f, 8.0f, 0.5f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha));
        }
    }

    public static boolean hitInputBox(float x, float y, float width, float mouseX, float mouseY) {
        float boxY = y + 7.5f + 4.0f;
        return mouseX >= x && mouseX <= x + width && mouseY >= boxY && mouseY <= boxY + 14.0f;
    }
}

