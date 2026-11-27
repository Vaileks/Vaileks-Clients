/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package pyz1k.screen.menu.sections.settings;

import java.util.Locale;
import net.minecraft.class_332;
import pyz1k.module.setting.SliderSetting;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class SliderSettingRenderer {
    private static final float ROW_HEIGHT = 23.0f;
    private static final float TITLE_Y = 0.5f;
    private static final float VALUE_Y = 1.2f;
    private static final float TRACK_Y = 13.7f;
    private static final float TRACK_H = 2.4f;
    private static final float KNOB_SIZE = 7.0f;
    private static final float RIGHT_PAD = 1.0f;
    private static final float VALUE_GAP = 0.4f;
    private static final float NAME_FADE_W = 1.8f;

    private SliderSettingRenderer() {
    }

    public static float measureHeight(SliderSetting setting, float width) {
        return 23.0f;
    }

    public static void render(class_332 context, UiAnimationEngine animation, String key, SliderSetting setting, float x, float y, float width, int mouseX, int mouseY, float alpha) {
        Layout layout = SliderSettingRenderer.layout(setting, x, y, width);
        float valueAnim = animation.animate("setting:slider:value:" + key, setting.getNormalized(), 24.0f, setting.getNormalized());
        float knobAppear = animation.animate01("setting:slider:knob:appear:" + key, Math.max(0.0f, Math.min(1.0f, alpha)), 16.0f, Math.max(0.0f, Math.min(1.0f, alpha)));
        float shownValue = valueAnim;
        int nameColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int valueColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha);
        int trackBgBase = ColorUtil.rgb(8, 9, 11);
        int trackFgBase = ColorUtil.interpolate(ClientColors.CATEGORY_ACCENT, ClientColors.TEXT_PRIMARY, 0.12f);
        int trackBg = ColorUtil.multiplyAlpha(trackBgBase, alpha * 0.95f);
        int trackFg = ColorUtil.multiplyAlpha(trackFgBase, alpha);
        int knobFill = ColorUtil.multiplyAlpha(ColorUtil.rgb(9, 10, 12), alpha * knobAppear);
        int knobBorder = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * knobAppear);
        RenderUtil.drawTextFadeRight(context, setting.getName(), x, y + 0.5f, nameColor, GFonts.MEDIUM, 8, layout.nameMaxW, 1.8f);
        RenderUtil.drawText(context, SliderSettingRenderer.formatValue(setting), layout.valueX, y + 1.2f, valueColor, GFonts.REGULAR, 7);
        float trackRound = 1.2f;
        RenderUtil.drawRoundRect(context, layout.trackX, layout.trackY, layout.trackW, 2.4f, trackRound, trackBg);
        float fillW = Math.max(0.0f, Math.min(layout.trackW, layout.trackW * shownValue));
        RenderUtil.drawRoundRect(context, layout.trackX, layout.trackY, fillW, 2.4f, trackRound, trackFg);
        float knobFillW = Math.max(0.0f, Math.min(layout.trackW, layout.trackW * valueAnim));
        float knobX = layout.trackX + knobFillW - 3.5f;
        float knobY = layout.trackY + -2.3f;
        float knobRound = 2.5f;
        float knobScale = 0.78f + 0.22f * knobAppear;
        float knobDrawSize = 7.0f * knobScale;
        float knobDx = (7.0f - knobDrawSize) * 0.5f;
        RenderUtil.drawRoundRect(context, knobX + knobDx, knobY + knobDx, knobDrawSize, knobDrawSize, knobRound * knobScale, knobFill);
        RenderUtil.drawBorder(context, knobX + knobDx, knobY + knobDx, knobDrawSize, knobDrawSize, knobRound * knobScale, knobBorder, 0.0f, 1.0f, 1.0f);
    }

    public static boolean hitTrack(SliderSetting setting, float x, float y, float width, double mouseX, double mouseY) {
        Layout layout = SliderSettingRenderer.layout(setting, x, y, width);
        return SliderSettingRenderer.isHovered(layout.trackX, layout.trackY - 4.0f, layout.trackW, 15.0f, mouseX, mouseY);
    }

    public static void setFromMouse(SliderSetting setting, float x, float y, float width, double mouseX) {
        Layout layout = SliderSettingRenderer.layout(setting, x, y, width);
        float t = (float)((mouseX - (double)layout.trackX) / (double)Math.max(1.0f, layout.trackW));
        setting.setNormalized(Math.max(0.0f, Math.min(1.0f, t)));
    }

    private static Layout layout(SliderSetting setting, float x, float y, float width) {
        String value = SliderSettingRenderer.formatValue(setting);
        float valueW = RenderUtil.getTextWidth(value, GFonts.REGULAR, 7);
        float valueX = x + width - 1.0f - valueW;
        float nameMaxW = Math.max(12.0f, valueX - 0.4f - x);
        float trackX = x;
        float trackW = Math.max(16.0f, x + width - 1.0f - trackX);
        float trackY = y + 13.7f;
        return new Layout(valueX, nameMaxW, trackX, trackY, trackW);
    }

    private static String formatValue(SliderSetting setting) {
        float value = ((Float)setting.getValue()).floatValue();
        String num = setting.getStep() >= 1.0f ? Integer.toString(Math.round(value)) : String.format(Locale.US, "%.2f", Float.valueOf(value)).replaceAll("0+$", "").replaceAll("\\.$", "");
        String suffix = setting.getSuffix();
        return suffix != null && !suffix.isBlank() ? num + " " + suffix : num;
    }

    private static boolean isHovered(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    private record Layout(float valueX, float nameMaxW, float trackX, float trackY, float trackW) {
    }
}

