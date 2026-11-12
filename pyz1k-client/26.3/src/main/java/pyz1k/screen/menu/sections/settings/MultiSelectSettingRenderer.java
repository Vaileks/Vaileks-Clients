/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package pyz1k.screen.menu.sections.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import pyz1k.module.setting.MultiSelectSetting;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class MultiSelectSettingRenderer {
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
    private static final float TOGGLE_W = 12.0f;
    private static final float TOGGLE_H = 8.0f;
    private static final float TOGGLE_KNOB = 5.0f;
    private static final float TOGGLE_INNER_PAD = 1.0f;
    private static final float RIGHT_PAD = 1.5f;

    private MultiSelectSettingRenderer() {
    }

    public static float measureHeight(MultiSelectSetting setting, float width, float expandProgress) {
        Layout layout = MultiSelectSettingRenderer.layout(setting, 0.0f, 0.0f, width, expandProgress);
        return layout.blockHeight;
    }

    public static void render(GuiGraphicsExtractor context, UiAnimationEngine animation, String key, MultiSelectSetting setting, float x, float y, float width, int mouseX, int mouseY, float alpha, float expandProgress) {
        float expand = ColorUtil.clamp01(expandProgress);
        Layout layout = MultiSelectSettingRenderer.layout(setting, x, y, width, expand);
        int titleColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int descColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha * 0.95f);
        int iconColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha);
        RenderUtil.drawTextFadeRight(context, setting.getName(), x, y + 0.2f, titleColor, GFonts.MEDIUM, 8, layout.titleTextMaxW, 5.0f);
        RenderUtil.drawImage(context, "textures/gui/icons/multienum.png", layout.titleIconX, y + 0.1f, 8.0f, 8.0f, 0.0f, iconColor, 1.0f);
        if (!layout.descriptionLines.isEmpty()) {
            float lineStep = RenderUtil.getTextHeight(GFonts.MEDIUM, 7) + -1.0f;
            for (int i = 0; i < layout.descriptionLines.size(); ++i) {
                RenderUtil.drawText(context, layout.descriptionLines.get(i), x, layout.descY + (float)i * lineStep, descColor, GFonts.MEDIUM, 7);
            }
        }
        boolean selectHoveredRaw = MultiSelectSettingRenderer.isHovered(layout.selectX, layout.selectY, layout.selectW, 13.0f, mouseX, mouseY);
        float selectHover = animation.animate01("setting:multi:select:hover:" + key, selectHoveredRaw ? 1.0f : 0.0f, 14.0f, 0.0f);
        int selectBgBase = ColorUtil.interpolate(ClientColors.SETTINGS_BACKGROUND, ClientColors.CATEGORY_ACCENT, selectHover * 0.12f);
        int selectBg = ColorUtil.multiplyAlpha(selectBgBase, alpha);
        int selectBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha);
        RenderUtil.drawRoundRect(context, layout.selectX, layout.selectY, layout.selectW, 13.0f, 2.5f, selectBg);
        RenderUtil.drawBorder(context, layout.selectX, layout.selectY, layout.selectW, 13.0f, 2.5f, selectBorder, 0.0f, 0.5f, 1.0f);
        float leftIconY = layout.selectY + 3.0f;
        RenderUtil.drawImage(context, "textures/gui/icons/enum.png", layout.selectX + 3.0f, leftIconY - 0.5f, 7.0f, 7.0f, 0.0f, iconColor, 1.0f);
        String selectedText = MultiSelectSettingRenderer.formatSelectedText(setting);
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
            context.enableScissor(scX1, scY1, scX2, scY2);
            for (int i = 0; i < setting.getOptions().size(); ++i) {
                String option = setting.getOptions().get(i);
                float itemY = layout.listY + 4.0f + (float)i * MultiSelectSettingRenderer.getItemStep();
                float toggleX = layout.listX + layout.listW - 1.5f - 12.0f;
                float toggleY = itemY + 1.0f;
                float rowX = layout.listX + 2.0f;
                float rowY = itemY - 1.0f;
                float rowW = layout.listW - 4.0f;
                float rowH = 12.0f;
                boolean selected = setting.isSelected(option);
                boolean hoveredRaw = MultiSelectSettingRenderer.isHovered(rowX, rowY, rowW, rowH, mouseX, mouseY);
                float hover = animation.animate01("setting:multi:item:hover:" + key + ":" + i, hoveredRaw ? 1.0f : 0.0f, 14.0f, 0.0f);
                float state = animation.animate01("setting:multi:item:state:" + key + ":" + i, selected ? 1.0f : 0.0f, 16.0f, selected ? 1.0f : 0.0f);
                int rowBg = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, listAlpha * hover * 0.1f);
                if (ColorUtil.alpha(rowBg) > 0) {
                    RenderUtil.drawRoundRect(context, rowX, rowY, rowW, rowH, 2.0f, rowBg);
                }
                RenderUtil.drawTextFadeRight(context, option, layout.listX + 8.0f, itemY + 2.2f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, listAlpha), GFonts.MEDIUM, 7, Math.max(12.0f, toggleX - 4.0f - (layout.listX + 8.0f)), 4.0f);
                int toggleTrackBase = ColorUtil.interpolate(ClientColors.SETTINGS_BACKGROUND, ClientColors.CATEGORY_ACCENT, state);
                toggleTrackBase = ColorUtil.interpolate(toggleTrackBase, ClientColors.TEXT_PRIMARY, hover * 0.12f);
                int toggleTrackColor = ColorUtil.multiplyAlpha(toggleTrackBase, alpha);
                int trackOutlineColor = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha * (1.0f - state));
                RenderUtil.drawRoundRect(context, toggleX - 4.0f, toggleY, 12.0f, 8.0f, 3.0f, toggleTrackColor);
                RenderUtil.drawBorder(context, toggleX - 4.0f, toggleY, 12.0f, 8.0f, 3.0f, trackOutlineColor, 0.0f, 0.5f, 1.0f);
                float knobTravel = 5.0f;
                float knobX = toggleX + 1.0f + knobTravel * state;
                float knobY = toggleY + 1.5f;
                int knobBase = ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, state);
                knobBase = ColorUtil.interpolate(knobBase, ClientColors.TEXT_PRIMARY, hover * 0.08f);
                int knobColor = ColorUtil.multiplyAlpha(knobBase, alpha * 0.95f);
                RenderUtil.drawRoundRect(context, knobX - 4.0f, knobY, 5.0f, 5.0f, 1.5f, knobColor);
            }
            context.disableScissor();
        }
    }

    public static boolean hitSelectBox(MultiSelectSetting setting, float x, float y, float width, double mouseX, double mouseY) {
        Layout layout = MultiSelectSettingRenderer.layout(setting, x, y, width, 0.0f);
        return MultiSelectSettingRenderer.isHovered(layout.selectX, layout.selectY, layout.selectW, 13.0f, mouseX, mouseY);
    }

    public static int hitOptionIndex(MultiSelectSetting setting, float x, float y, float width, double mouseX, double mouseY, float expandProgress) {
        float expand = ColorUtil.clamp01(expandProgress);
        if (expand <= 0.001f) {
            return -1;
        }
        Layout layout = MultiSelectSettingRenderer.layout(setting, x, y, width, expand);
        float visibleListBottom = layout.listY + layout.listH * expand;
        for (int i = 0; i < setting.getOptions().size(); ++i) {
            float itemY = layout.listY + 4.0f + (float)i * MultiSelectSettingRenderer.getItemStep();
            float rowX = layout.listX + 2.0f;
            float rowY = itemY - 1.0f;
            float rowW = layout.listW - 4.0f;
            float rowH = 12.0f;
            if (rowY + rowH > visibleListBottom || !MultiSelectSettingRenderer.isHovered(rowX, rowY, rowW, rowH, mouseX, mouseY)) continue;
            return i;
        }
        return -1;
    }

    private static Layout layout(MultiSelectSetting setting, float x, float y, float width, float expandProgress) {
        float titleH = RenderUtil.getTextHeight(GFonts.MEDIUM, 8);
        float titleIconX = x + width - 8.0f - 1.5f;
        float titleTextMaxW = Math.max(16.0f, titleIconX - 4.0f - x);
        List<String> descLines = MultiSelectSettingRenderer.wrapDescription(setting.getDescription(), Math.max(16.0f, width - 1.5f));
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

    private static String formatSelectedText(MultiSelectSetting setting) {
        Set<String> selected = setting.getSelectedValues();
        return selected.isEmpty() ? "None" : String.join((CharSequence)", ", selected);
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

