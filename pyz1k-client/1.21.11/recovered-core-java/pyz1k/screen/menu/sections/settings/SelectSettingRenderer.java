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
import pyz1k.module.setting.SelectSetting;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class SelectSettingRenderer {
    private static final float TITLE_SIZE = 8.0f;
    private static final float DESC_SIZE = 7.0f;
    private static final float DESC_TOP_GAP = 9.8f;
    private static final float DESC_LINE_GAP = -1.0f;
    private static final float SECTION_GAP = 3.0f;
    private static final float SELECT_Y_OFFSET = -2.0f;
    private static final float BOTTOM_PAD = 1.0f;
    private static final float ICON_SIZE = 8.0f;
    private static final float TITLE_RIGHT_PAD = 4.0f;
    private static final float SELECT_BOX_H = 13.0f;
    private static final float SELECT_BOX_RADIUS = 2.5f;
    private static final float SELECT_PAD_X = 3.0f;
    private static final float SELECT_LEFT_ICON_SIZE = 7.0f;
    private static final float SELECT_ARROW_SIZE = 7.0f;
    private static final float LIST_TOP_GAP = 1.0f;
    private static final float LIST_PAD_Y = 4.0f;
    private static final float ITEM_H = 10.0f;
    private static final float ITEM_GAP = 2.0f;
    private static final float RIGHT_PAD = 1.5f;
    private static final float CHECKMARK_SIZE = 7.0f;

    private SelectSettingRenderer() {
    }

    public static float measureHeight(SelectSetting setting, float width, float expandProgress) {
        Layout layout = SelectSettingRenderer.layout(setting, 0.0f, 0.0f, width, expandProgress);
        return layout.blockHeight;
    }

    public static void render(class_332 context, UiAnimationEngine animation, String key, SelectSetting setting, float x, float y, float width, int mouseX, int mouseY, float alpha, float expandProgress) {
        float expand = ColorUtil.clamp01(expandProgress);
        Layout layout = SelectSettingRenderer.layout(setting, x, y, width, expand);
        int titleColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int descColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha * 0.95f);
        int iconColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha);
        RenderUtil.drawTextFadeRight(context, setting.getName(), x, y + 0.2f, titleColor, GFonts.MEDIUM, 8, layout.titleTextMaxW, 5.0f);
        RenderUtil.drawImage(context, "textures/gui/icons/enum.png", layout.titleIconX, y + 0.1f, 8.0f, 8.0f, 0.0f, iconColor, 1.0f);
        if (!layout.descriptionLines.isEmpty()) {
            float lineStep = RenderUtil.getTextHeight(GFonts.MEDIUM, 7) + -1.0f;
            for (int i = 0; i < layout.descriptionLines.size(); ++i) {
                RenderUtil.drawText(context, layout.descriptionLines.get(i), x, layout.descY + (float)i * lineStep, descColor, GFonts.MEDIUM, 7);
            }
        }
        boolean selectHoveredRaw = SelectSettingRenderer.isHovered(layout.selectX, layout.selectY, layout.selectW, 13.0f, mouseX, mouseY);
        float selectHover = animation.animate01("setting:select:select:hover:" + key, selectHoveredRaw ? 1.0f : 0.0f, 14.0f, 0.0f);
        int selectBgBase = ColorUtil.interpolate(ClientColors.SETTINGS_BACKGROUND, ClientColors.CATEGORY_ACCENT, selectHover * 0.12f);
        int selectBg = ColorUtil.multiplyAlpha(selectBgBase, alpha);
        int selectBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha);
        RenderUtil.drawRoundRect(context, layout.selectX, layout.selectY, layout.selectW, 13.0f, 2.5f, selectBg);
        RenderUtil.drawBorder(context, layout.selectX, layout.selectY, layout.selectW, 13.0f, 2.5f, selectBorder, 0.0f, 0.5f, 1.0f);
        float leftIconY = layout.selectY + 3.0f;
        RenderUtil.drawImage(context, "textures/gui/icons/enum.png", layout.selectX + 3.0f, leftIconY - 0.5f, 7.0f, 7.0f, 0.0f, iconColor, 1.0f);
        String selectedText = SelectSettingRenderer.formatSelectedText(setting);
        int selectedColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        float selectedX = layout.selectX + 3.0f + 7.0f + 3.0f;
        RenderUtil.drawTextFadeRight(context, selectedText, selectedX, layout.selectY + 4.0f, selectedColor, GFonts.MEDIUM, 7, layout.selectTextW, 2.0f);
        int arrowDownColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha * (1.0f - expand));
        int arrowUpColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha * expand);
        float arrowY = layout.selectY + 3.0f;
        if (ColorUtil.alpha(arrowDownColor) > 0) {
            RenderUtil.drawImage(context, "textures/gui/icons/arrow.png", layout.arrowX, arrowY, 7.0f, 7.0f, 0.0f, arrowDownColor, 1.0f);
        }
        if (ColorUtil.alpha(arrowUpColor) > 0) {
            RenderUtil.drawImage(context, "textures/gui/icons/arrow_up.png", layout.arrowX, arrowY, 7.0f, 7.0f, 0.0f, arrowUpColor, 1.0f);
        }
        if (!(expand <= 0.001f)) {
            float visibleListH = layout.listH * expand;
            float listAlpha = alpha * (0.35f + 0.65f * expand);
            int listBg = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, listAlpha);
            int listBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, listAlpha);
            RenderUtil.drawRoundRect(context, layout.listX, layout.listY, layout.listW, visibleListH, 3.0f, listBg);
            RenderUtil.drawBorder(context, layout.listX, layout.listY, layout.listW, visibleListH, 3.0f, listBorder, 0.0f, 0.5f, 1.0f);
            int scX1 = (int)Math.floor(layout.listX);
            int scY1 = (int)Math.floor(layout.listY);
            int scX2 = (int)Math.ceil(layout.listX + layout.listW);
            int scY2 = (int)Math.ceil(layout.listY + visibleListH);
            context.method_44379(scX1, scY1, scX2, scY2);
            for (int i = 0; i < setting.getOptions().size(); ++i) {
                String option = setting.getOptions().get(i);
                float itemY = layout.listY + 4.0f + (float)i * SelectSettingRenderer.getItemStep();
                float rowX = layout.listX + 2.0f;
                float rowY = itemY - 1.0f;
                float rowW = layout.listW - 4.0f;
                float rowH = 12.0f;
                boolean hoveredRaw = SelectSettingRenderer.isHovered(rowX, rowY, rowW, rowH, mouseX, mouseY);
                float hover = animation.animate01("setting:select:item:hover:" + key + ":" + i, hoveredRaw ? 1.0f : 0.0f, 14.0f, 0.0f);
                boolean selected = option.equals(setting.getValue());
                float selectedAnim = animation.animate01("setting:select:item:selected:" + key + ":" + i, selected ? 1.0f : 0.0f, 16.0f, selected ? 1.0f : 0.0f);
                int rowBg = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, listAlpha * (hover * 0.1f + selectedAnim * 0.12f));
                if (ColorUtil.alpha(rowBg) > 0) {
                    RenderUtil.drawRoundRect(context, rowX, rowY, rowW, rowH, 2.0f, rowBg);
                }
                float textX = layout.listX + 8.0f;
                float checkX = layout.listX + layout.listW - 1.5f - 7.0f - 2.0f;
                RenderUtil.drawTextFadeRight(context, option, textX, itemY + 2.2f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, listAlpha), GFonts.MEDIUM, 7, Math.max(12.0f, checkX - 5.0f - textX), 4.0f);
                int checkColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, listAlpha * selectedAnim);
                if (ColorUtil.alpha(checkColor) <= 0) continue;
                float checkY = itemY + 1.5f;
                RenderUtil.drawImage(context, "textures/gui/icons/checkmark.png", checkX - 2.0f, checkY, 7.0f, 7.0f, 0.0f, checkColor, 1.0f);
            }
            context.method_44380();
        }
    }

    public static boolean hitSelectBox(SelectSetting setting, float x, float y, float width, double mouseX, double mouseY) {
        Layout layout = SelectSettingRenderer.layout(setting, x, y, width, 0.0f);
        return SelectSettingRenderer.isHovered(layout.selectX, layout.selectY, layout.selectW, 13.0f, mouseX, mouseY);
    }

    public static int hitOptionIndex(SelectSetting setting, float x, float y, float width, double mouseX, double mouseY, float expandProgress) {
        float expand = ColorUtil.clamp01(expandProgress);
        if (expand <= 0.001f) {
            return -1;
        }
        Layout layout = SelectSettingRenderer.layout(setting, x, y, width, expand);
        float visibleListBottom = layout.listY + layout.listH * expand;
        for (int i = 0; i < setting.getOptions().size(); ++i) {
            float itemY = layout.listY + 4.0f + (float)i * SelectSettingRenderer.getItemStep();
            float rowX = layout.listX + 2.0f;
            float rowY = itemY - 1.0f;
            float rowW = layout.listW - 4.0f;
            float rowH = 12.0f;
            if (rowY + rowH > visibleListBottom || !SelectSettingRenderer.isHovered(rowX, rowY, rowW, rowH, mouseX, mouseY)) continue;
            return i;
        }
        return -1;
    }

    private static Layout layout(SelectSetting setting, float x, float y, float width, float expandProgress) {
        float titleH = RenderUtil.getTextHeight(GFonts.MEDIUM, 8);
        float titleIconX = x + width - 8.0f - 1.5f;
        float titleTextMaxW = Math.max(16.0f, titleIconX - 4.0f - x);
        List<String> descLines = SelectSettingRenderer.wrapDescription(setting.getDescription(), Math.max(16.0f, width - 1.5f));
        float descY = y + 9.8f;
        float descH = 0.0f;
        if (!descLines.isEmpty()) {
            float step = RenderUtil.getTextHeight(GFonts.MEDIUM, 7) + -1.0f;
            descH = RenderUtil.getTextHeight(GFonts.MEDIUM, 7) + (float)Math.max(0, descLines.size() - 1) * step;
        }
        float afterHeaderY = y + titleH;
        if (!descLines.isEmpty()) {
            afterHeaderY = Math.max(afterHeaderY, descY + descH);
        }
        float selectY = afterHeaderY + 3.0f + -2.0f;
        float selectX = x;
        float selectW = width - 1.5f;
        float arrowX = selectX + selectW - 3.0f - 7.0f;
        float selectTextW = Math.max(14.0f, arrowX - 3.0f - (selectX + 3.0f + 7.0f + 3.0f));
        float listX = x;
        float listY = selectY + 13.0f + 1.0f;
        float listW = width - 1.5f;
        int optionCount = setting.getOptions().size();
        float listH = 8.0f + (float)optionCount * 10.0f + (float)Math.max(0, optionCount - 1) * 2.0f;
        float collapsedBottom = selectY + 13.0f;
        float expandedBottom = listY + listH;
        float blockBottom = collapsedBottom + (expandedBottom - collapsedBottom) * ColorUtil.clamp01(expandProgress);
        float blockHeight = blockBottom - y + 1.0f;
        return new Layout(titleIconX, titleTextMaxW, descY, descLines, selectX, selectY, selectW, arrowX, selectTextW, listX, listY, listW, listH, blockHeight);
    }

    private static float getItemStep() {
        return 12.0f;
    }

    private static String formatSelectedText(SelectSetting setting) {
        String value = setting.getValue();
        return value != null && !value.isBlank() ? value : "None";
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
                    lines.add(word);
                    continue;
                }
                String candidate = (String)current + " " + word;
                if (RenderUtil.getTextWidth(candidate, GFonts.MEDIUM, 7) <= maxWidth) {
                    current = candidate;
                    continue;
                }
                lines.add((String)current);
                current = word;
            }
            if (!((String)current).isEmpty()) {
                lines.add((String)current);
            }
            return lines;
        }
        return lines;
    }

    private static boolean isHovered(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    private record Layout(float titleIconX, float titleTextMaxW, float descY, List<String> descriptionLines, float selectX, float selectY, float selectW, float arrowX, float selectTextW, float listX, float listY, float listW, float listH, float blockHeight) {
    }
}

