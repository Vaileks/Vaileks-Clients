/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package pyz1k.screen.menu.sections.settings;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import pyz1k.module.setting.BooleanSetting;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.render.font.GIcons;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class BooleanSettingRenderer {
    private static final float CHECK_SIZE = 10.0f;
    private static final float CHECK_RADIUS = 2.0f;
    private static final float KEY_ICON_SIZE = 8.0f;
    private static final float RIGHT_PAD = 2.0f;
    private static final float KEY_ICON_GAP = 3.0f;
    private static final float TEXT_RIGHT_PAD = 4.0f;
    private static final float TITLE_SIZE = 8.0f;
    private static final float DESC_SIZE = 7.0f;
    private static final float DESC_TOP_GAP = 10.2f;
    private static final float DESC_LINE_GAP = 0.0f;
    private static final float BOTTOM_PAD = 0.8f;
    private static final float Y_RENDER_OFFSET = -2.5f;

    private BooleanSettingRenderer() {
    }

    public static float measureHeight(BooleanSetting setting, float width) {
        Layout layout = BooleanSettingRenderer.layout(setting, 0.0f, 0.0f, width, true);
        return layout.blockHeight;
    }

    public static void render(class_332 context, UiAnimationEngine animation, String key, BooleanSetting setting, float x, float y, float width, int mouseX, int mouseY, float alpha) {
        BooleanSettingRenderer.render(context, animation, key, setting, x, y, width, mouseX, mouseY, alpha, true);
    }

    public static void render(class_332 context, UiAnimationEngine animation, String key, BooleanSetting setting, float x, float y, float width, int mouseX, int mouseY, float alpha, boolean showBindIcon) {
        Layout layout = BooleanSettingRenderer.layout(setting, x, y + -2.5f, width, showBindIcon);
        boolean checkboxHoveredRaw = !setting.isBindOnly() && BooleanSettingRenderer.isHovered(layout.checkX, layout.checkY, 10.0f, 10.0f, mouseX, mouseY);
        boolean keyIconHoveredRaw = showBindIcon && BooleanSettingRenderer.isHovered(layout.keyIconX, layout.keyIconY, 8.0f, 8.0f, mouseX, mouseY);
        float checkboxHover = animation.animate01("setting:boolean:hover:" + key, checkboxHoveredRaw ? 1.0f : 0.0f, 12.0f, 0.0f);
        float keyIconHover = animation.animate01("setting:boolean:key:hover:" + key, keyIconHoveredRaw ? 1.0f : 0.0f, 12.0f, 0.0f);
        float state = animation.animate01("setting:boolean:state:" + key, setting.isValue() ? 1.0f : 0.0f, 16.0f, setting.isValue() ? 1.0f : 0.0f);
        if (showBindIcon) {
            int keyIconBase = ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, keyIconHover * 0.7f);
            int keyIconColor = ColorUtil.multiplyAlpha(keyIconBase, alpha);
            RenderUtil.drawIcon(context, GIcons.KEYBOARD, layout.keyIconX, layout.keyIconY, 8.0f, keyIconColor);
        }
        int titleColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int descColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha * 0.95f);
        int titleX1 = (int)Math.floor(x);
        int titleY1 = (int)Math.floor(y);
        int titleX2 = (int)Math.ceil(x + layout.textMaxW);
        int titleY2 = (int)Math.ceil(y + RenderUtil.getTextHeight(GFonts.MEDIUM, 8) + 2.0f);
        context.method_44379(titleX1, titleY1, titleX2, titleY2);
        RenderUtil.drawText(context, setting.getName(), x, y + 0.3f, titleColor, GFonts.MEDIUM, 8);
        context.method_44380();
        if (!layout.descriptionLines.isEmpty()) {
            float descY = y + 10.2f;
            float lineStep = RenderUtil.getTextHeight(GFonts.MEDIUM, 7) + 0.0f;
            for (int i = 0; i < layout.descriptionLines.size(); ++i) {
                RenderUtil.drawText(context, layout.descriptionLines.get(i), x, descY + (float)i * lineStep, descColor, GFonts.MEDIUM, 7);
            }
        }
        if (!setting.isBindOnly()) {
            int boxBgOff = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha);
            int boxBgOn = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * (0.22f + 0.68f * state));
            int boxBg = ColorUtil.interpolate(boxBgOff, boxBgOn, state);
            int boxBgHover = ColorUtil.interpolate(boxBg, ColorUtil.withAlpha(ClientColors.TEXT_PRIMARY, ColorUtil.alpha(boxBg)), checkboxHover * 0.08f);
            RenderUtil.drawRoundRect(context, layout.checkX, layout.checkY, 10.0f, 10.0f, 2.0f, boxBgHover);
            if (state < 0.999f) {
                int boxBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha * (1.0f - state));
                RenderUtil.drawBorder(context, layout.checkX, layout.checkY, 10.0f, 10.0f, 2.0f, boxBorder, 0.0f, 0.5f, 1.0f);
            }
            if (state > 0.001f) {
                int markColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha * state);
                RenderUtil.drawIcon(context, GIcons.CHECKMARK, layout.checkX + 1.4f, layout.checkY + 1.4f, 7.2f, markColor);
            }
        }
    }

    public static boolean hitCheckbox(BooleanSetting setting, float x, float y, float width, double mouseX, double mouseY) {
        return BooleanSettingRenderer.hitCheckbox(setting, x, y, width, mouseX, mouseY, true);
    }

    public static boolean hitCheckbox(BooleanSetting setting, float x, float y, float width, double mouseX, double mouseY, boolean showBindIcon) {
        if (setting.isBindOnly()) {
            return false;
        }
        Layout layout = BooleanSettingRenderer.layout(setting, x, y + -2.5f, width, showBindIcon);
        return BooleanSettingRenderer.isHovered(layout.checkX, layout.checkY, 10.0f, 10.0f, mouseX, mouseY);
    }

    public static boolean hitKeyIcon(BooleanSetting setting, float x, float y, float width, double mouseX, double mouseY) {
        return BooleanSettingRenderer.hitKeyIcon(setting, x, y, width, mouseX, mouseY, true);
    }

    public static boolean hitKeyIcon(BooleanSetting setting, float x, float y, float width, double mouseX, double mouseY, boolean showBindIcon) {
        if (!showBindIcon) {
            return false;
        }
        Layout layout = BooleanSettingRenderer.layout(setting, x, y + -2.5f, width, true);
        return BooleanSettingRenderer.isHovered(layout.keyIconX, layout.keyIconY, 8.0f, 8.0f, mouseX, mouseY);
    }

    private static Layout layout(BooleanSetting setting, float x, float y, float width, boolean showBindIcon) {
        float checkY;
        float checkX = x + width - 2.0f - 10.0f;
        float keyIconX = setting.isBindOnly() ? x + width - 2.0f - 8.0f : checkX - 3.0f - 8.0f;
        float textMaxW = showBindIcon ? Math.max(14.0f, keyIconX - x - 4.0f) : Math.max(14.0f, checkX - x - 4.0f);
        List<String> descriptionLines = BooleanSettingRenderer.wrapDescription(setting.getDescription(), textMaxW);
        if (descriptionLines.isEmpty()) {
            float titleH = RenderUtil.getTextHeight(GFonts.MEDIUM, 8);
            checkY = y + 0.3f + (titleH - 10.0f) / 2.0f;
        } else {
            float descH = RenderUtil.getTextHeight(GFonts.MEDIUM, 7);
            checkY = y + 10.2f + (descH - 10.0f) / 2.0f;
        }
        keyIconX = showBindIcon ? keyIconX : checkX;
        float keyIconY = checkY + 1.0f;
        float blockHeight = BooleanSettingRenderer.computeBlockHeight(descriptionLines, checkY - y);
        return new Layout(checkX, checkY, keyIconX, keyIconY, textMaxW, descriptionLines, blockHeight);
    }

    private static float computeBlockHeight(List<String> descriptionLines, float checkTopOffset) {
        float titleH;
        float textBottom = titleH = RenderUtil.getTextHeight(GFonts.MEDIUM, 8);
        if (!descriptionLines.isEmpty()) {
            float descH = RenderUtil.getTextHeight(GFonts.MEDIUM, 7);
            float lineStep = descH + 0.0f;
            textBottom = 10.2f + descH + (float)Math.max(0, descriptionLines.size() - 1) * lineStep;
        }
        float checkBottom = checkTopOffset + 10.0f;
        return Math.max(textBottom, checkBottom) + 0.8f;
    }

    private static List<String> wrapDescription(String description, float maxWidth) {
        ArrayList<String> lines = new ArrayList<String>();
        if (description != null && !description.isBlank() && !(maxWidth <= 1.0f)) {
            String[] words = description.trim().split("\\s+");
            Object current = "";
            for (String word : words) {
                if (((String)current).isEmpty()) {
                    if (RenderUtil.getTextWidth(word, GFonts.MEDIUM, 7) <= maxWidth) {
                        current = word;
                        continue;
                    }
                    lines.addAll(BooleanSettingRenderer.splitLongWord(word, maxWidth));
                    current = "";
                    continue;
                }
                String candidate = (String)current + " " + word;
                if (RenderUtil.getTextWidth(candidate, GFonts.MEDIUM, 7) <= maxWidth) {
                    current = candidate;
                    continue;
                }
                lines.add((String)current);
                if (RenderUtil.getTextWidth(word, GFonts.MEDIUM, 7) <= maxWidth) {
                    current = word;
                    continue;
                }
                lines.addAll(BooleanSettingRenderer.splitLongWord(word, maxWidth));
                current = "";
            }
            if (!((String)current).isEmpty()) {
                lines.add((String)current);
            }
            return lines;
        }
        return lines;
    }

    private static List<String> splitLongWord(String word, float maxWidth) {
        ArrayList<String> out = new ArrayList<String>();
        String rest = word;
        while (!rest.isEmpty()) {
            int cut;
            for (cut = rest.length(); cut > 1 && RenderUtil.getTextWidth(rest.substring(0, cut), GFonts.MEDIUM, 7) > maxWidth; --cut) {
            }
            out.add(rest.substring(0, cut));
            rest = rest.substring(cut);
        }
        return out;
    }

    private static boolean isHovered(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    private static final class Layout {
        private final float checkX;
        private final float checkY;
        private final float keyIconX;
        private final float keyIconY;
        private final float textMaxW;
        private final List<String> descriptionLines;
        private final float blockHeight;

        private Layout(float checkX, float checkY, float keyIconX, float keyIconY, float textMaxW, List<String> descriptionLines, float blockHeight) {
            this.checkX = checkX;
            this.checkY = checkY;
            this.keyIconX = keyIconX;
            this.keyIconY = keyIconY;
            this.textMaxW = textMaxW;
            this.descriptionLines = descriptionLines;
            this.blockHeight = blockHeight;
        }
    }
}

