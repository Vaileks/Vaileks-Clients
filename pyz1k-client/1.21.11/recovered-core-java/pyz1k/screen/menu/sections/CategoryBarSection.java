/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_332
 */
package pyz1k.screen.menu.sections;

import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_332;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class CategoryBarSection {
    public static final float HEADER_HEIGHT = 43.0f;
    private static final float TAB_Y = 9.0f;
    private static final float TAB_H = 25.0f;
    private static final float ICON_W = 30.0f;
    private static final float ACTIVE_W = 87.0f;
    private static final float GAP = 4.0f;
    private static final List<TopTab> VISIBLE_TABS = List.of(TopTab.VISUALS, TopTab.HAND_RENDER, TopTab.ABILITY_BIND, TopTab.COSMETICS, TopTab.CONFIGS);
    private TopTab activeTab = TopTab.VISUALS;

    public void setActiveTab(TopTab tab) {
        if (tab != null && VISIBLE_TABS.contains((Object)tab)) {
            this.activeTab = tab;
        }
    }

    public TopTab getActiveTab() {
        return this.activeTab;
    }

    public void render(class_332 context, UiAnimationEngine animation, float panelX, float panelY, float panelWidth, float panelHeight, int mouseX, int mouseY, float alpha) {
        RenderUtil.drawRoundRect(context, panelX, panelY, panelWidth, 43.0f, 5.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha));
        RenderUtil.drawRoundRect(context, panelX + 9.0f, panelY + 43.0f - 1.0f, panelWidth - 18.0f, 1.0f, 0.5f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND_OUTLINE, alpha));
        float x = panelX + 12.0f;
        for (TopTab tab : VISIBLE_TABS) {
            boolean selected = tab == this.activeTab;
            float width = selected ? 87.0f : 30.0f;
            boolean hovered = CategoryBarSection.inside(mouseX, mouseY, x, panelY + 9.0f, width, 25.0f);
            float hover = animation.animate01("header:hover:" + tab.name(), hovered ? 1.0f : 0.0f, 13.0f, 0.0f);
            float active = animation.animate01("header:active:" + tab.name(), selected ? 1.0f : 0.0f, 16.0f, selected ? 1.0f : 0.0f);
            int bg = ColorUtil.interpolate(ClientColors.CATEGORY_BACKGROUND, ClientColors.CATEGORY_ACCENT, 0.14f + active * 0.24f + hover * 0.08f);
            RenderUtil.drawRoundRect(context, x, panelY + 9.0f, width, 25.0f, 4.0f, ColorUtil.multiplyAlpha(bg, alpha));
            if (selected) {
                RenderUtil.drawBorder(context, x, panelY + 9.0f, width, 25.0f, 4.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * 0.58f), 0.0f, 0.5f, 1.0f);
            }
            if (!selected) {
                RenderUtil.drawBorder(context, x, panelY + 9.0f, width, 25.0f, 4.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND_OUTLINE, alpha * (0.8f + hover * 0.2f)), 0.0f, 0.5f, 1.0f);
            }
            int iconColor = ColorUtil.multiplyAlpha(ColorUtil.interpolate(ClientColors.TEXT_PRIMARY, ClientColors.CATEGORY_ACCENT_SECOND, active + hover * 0.25f), alpha * (selected ? 1.0f : 0.76f));
            float iconX = x + (selected ? 9.0f : (width - 8.0f) * 0.5f);
            float iconY = panelY + 9.0f + 8.0f;
            if (tab.iconGlyph != null) {
                RenderUtil.drawText(context, tab.iconGlyph, iconX + 1.0f, iconY + 1.0f, iconColor, GFonts.NOTIFICATION_ICONS, 7.0f);
            } else {
                RenderUtil.drawImage(context, tab.iconPath, iconX, iconY, 8.0f, 8.0f, 0.0f, iconColor, 1.0f);
            }
            if (selected) {
                RenderUtil.drawText(context, tab.label, x + 22.0f, panelY + 9.0f + 10.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.MEDIUM, 7.0f);
            }
            x += width + 4.0f;
        }
    }

    public boolean handleClick(class_11909 click, float panelX, float panelY, float panelWidth, float panelHeight) {
        if (click.method_74245() != 0) {
            return false;
        }
        float x = panelX + 12.0f;
        for (TopTab tab : VISIBLE_TABS) {
            float width;
            float f = width = tab == this.activeTab ? 87.0f : 30.0f;
            if (CategoryBarSection.inside(click.comp_4798(), click.comp_4799(), x, panelY + 9.0f, width, 25.0f)) {
                this.activeTab = tab;
                return true;
            }
            x += width + 4.0f;
        }
        return false;
    }

    private static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= (double)x && mx <= (double)(x + w) && my >= (double)y && my <= (double)(y + h);
    }

    public static enum TopTab {
        VISUALS("Visuals", "textures/gui/icons/visuals.png"),
        ITEM_RENDER("ItemRender", "textures/gui/icons/misc.png"),
        HAND_RENDER("HandRender", "textures/gui/icons/property.png"),
        ABILITY_BIND("Ability", "textures/gui/icons/buttons.png"),
        CONFIGS("Configs", "textures/gui/icons/config.png"),
        THEMES("Themes", "textures/gui/icons/theme.png"),
        COSMETICS("Cosmetics", null, "G");

        private final String label;
        private final String iconPath;
        private final String iconGlyph;

        private TopTab(String label, String iconPath) {
            this(label, iconPath, null);
        }

        private TopTab(String label, String iconPath, String iconGlyph) {
            this.label = label;
            this.iconPath = iconPath;
            this.iconGlyph = iconGlyph;
        }
    }
}

