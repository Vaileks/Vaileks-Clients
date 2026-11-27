/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  org.lwjgl.glfw.GLFW
 */
package pyz1k.screen.menu.sections;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import pyz1k.compat.LegacyInput;
import pyz1k.itemrender.ItemRenderManager;
import pyz1k.itemrender.ItemRenderRule;
import pyz1k.module.setting.ColorSetting;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.screen.menu.sections.CategoryBarSection;
import pyz1k.screen.menu.sections.ModuleGridSection;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class ItemRenderSection {
    private static final float HEADER_H = 54.0f;
    private static final float CARD_H = 103.0f;
    private static final float CARD_GAP = 7.0f;
    private static final float FIELD_H = 14.0f;
    private static final float CARET_X_CORRECTION = 2.25f;
    private float scrollTarget;
    private float scrollValue;
    private String searchText = "";
    private boolean draggingScrollbar;
    private float scrollbarGrabOffset;
    private boolean confirmDeleteAll;
    private String focusedRuleId;
    private Field focusedField = Field.NONE;
    private String editingText = "";
    private int caretIndex;
    private int selectionIndex;
    private static ItemRenderSection activeTextEditor;
    private String draggingScaleRuleId;
    private final ModuleGridSection colorPickerHost = new ModuleGridSection();
    private ColorSetting colorPickerSetting;
    private String colorPickerRuleId;
    private int colorPickerLastRgb = -1;

    public void render(GuiGraphicsExtractor context, UiAnimationEngine animation, float panelX, float panelY, float panelWidth, float panelHeight, int mouseX, int mouseY, float alpha) {
        ItemRenderManager manager = ItemRenderManager.getInstance();
        this.syncColorPicker(manager);
        List<ItemRenderRule> rules = this.filteredRules(manager.getRules());
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        int titleColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int subColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha * 0.92f);
        RenderUtil.drawText(context, "ItemRender", layout.contentX, layout.headerY + 1.0f, titleColor, GFonts.MEDIUM, 9.0f);
        this.drawButton(context, animation, "itemrender:delete-all", "Удалить все", layout.deleteX, layout.headerY, layout.deleteW, 16.0f, mouseX, mouseY, alpha);
        this.drawButton(context, animation, "itemrender:add", "+  Добавить", layout.addX, layout.headerY, layout.addW, 16.0f, mouseX, mouseY, alpha);
        this.drawSearchField(context, layout.searchX, layout.searchY, layout.searchW, 14.0f, alpha);
        float totalH = rules.isEmpty() ? 0.0f : (float)rules.size() * 103.0f + (float)Math.max(0, rules.size() - 1) * 7.0f;
        float maxScroll = Math.max(0.0f, totalH - layout.listH);
        this.scrollTarget = ItemRenderSection.clamp(this.scrollTarget, 0.0f, maxScroll);
        this.scrollValue = animation.animate("itemrender:scroll", this.scrollTarget, 26.0f, this.scrollValue);
        this.scrollValue = ItemRenderSection.clamp(this.scrollValue, 0.0f, maxScroll);
        int listBg = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha * 0.28f);
        int listBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha * 0.75f);
        RenderUtil.drawRoundRect(context, layout.listX, layout.listY, layout.listW, layout.listH, 4.0f, listBg);
        RenderUtil.drawBorder(context, layout.listX, layout.listY, layout.listW, layout.listH, 4.0f, listBorder, 0.0f, 0.5f, 1.0f);
        if (rules.isEmpty()) {
            boolean searching = !this.currentSearchText().isBlank();
            String a = searching && !manager.getRules().isEmpty() ? "Ничего не найдено" : "Пока нет предметов";
            String b = searching ? "Поиск по ID или имени" : "Нажми + Добавить";
            float ax = layout.listX + (layout.listW - RenderUtil.getTextWidth(a, GFonts.MEDIUM, 7.0f)) * 0.5f;
            float bx = layout.listX + (layout.listW - RenderUtil.getTextWidth(b, GFonts.MEDIUM, 5.5f)) * 0.5f;
            RenderUtil.drawText(context, a, ax, layout.listY + layout.listH * 0.5f - 8.0f, titleColor, GFonts.MEDIUM, 7.0f);
            RenderUtil.drawText(context, b, bx, layout.listY + layout.listH * 0.5f + 3.0f, subColor, GFonts.MEDIUM, 5.5f);
        } else {
            context.enableScissor((int)Math.floor(layout.listX + 1.0f), (int)Math.floor(layout.listY + 1.0f), (int)Math.ceil(layout.listX + layout.listW - 1.0f), (int)Math.ceil(layout.listY + layout.listH - 1.0f));
            for (int i = 0; i < rules.size(); ++i) {
                ItemRenderRule rule = rules.get(i);
                float y = layout.listY + 5.0f + (float)i * 110.0f - this.scrollValue;
                if (!(y + 103.0f >= layout.listY) || !(y <= layout.listY + layout.listH)) continue;
                this.renderRule(context, animation, manager, rule, i, layout.listX + 5.0f, y, layout.listW - 18.0f, mouseX, mouseY, alpha);
            }
            context.disableScissor();
        }
        this.drawScrollbar(context, layout, totalH, maxScroll, alpha);
        manager.flushAutoSave();
        if (this.colorPickerHost.isColorPopupOpen()) {
            this.colorPickerHost.render(context, animation, panelX, panelY, panelWidth, panelHeight, mouseX, mouseY, 0.0f, CategoryBarSection.TopTab.VISUALS);
            this.colorPickerHost.renderOverlay(context, animation, mouseX, mouseY, alpha);
        }
        if (this.confirmDeleteAll) {
            this.renderDeleteAllConfirm(context, animation, mouseX, mouseY, alpha);
        }
    }

    private void renderRule(GuiGraphicsExtractor context, UiAnimationEngine animation, ItemRenderManager manager, ItemRenderRule rule, int index, float x, float y, float w, int mouseX, int mouseY, float alpha) {
        int bg = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha * 0.96f);
        int border = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND_OUTLINE, alpha);
        int text = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int muted = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha * 0.95f);
        RenderUtil.drawRoundRect(context, x, y, w, 103.0f, 5.0f, bg);
        RenderUtil.drawBorder(context, x, y, w, 103.0f, 5.0f, border, 1.0f, 0.0f, 0.5f);
        RenderUtil.drawText(context, "Предмет " + (index + 1), x + 10.0f, y + 8.0f, text, GFonts.MEDIUM, 7.0f);
        float delX = x + w - 22.0f;
        boolean delHover = ItemRenderSection.hovered(mouseX, mouseY, delX, y + 5.0f, 14.0f, 13.0f);
        int delBg = ColorUtil.multiplyAlpha(delHover ? -7259065 : ClientColors.SETTINGS_BACKGROUND, alpha);
        RenderUtil.drawRoundRect(context, delX, y + 5.0f, 14.0f, 13.0f, 3.0f, delBg);
        RenderUtil.drawText(context, "-", delX + 6.0f, y + 8.0f, ColorUtil.multiplyAlpha(-19011, alpha), GFonts.MEDIUM, 7.0f);
        float labelX = x + 8.0f;
        float itemY = y + 24.0f;
        this.drawToggle(context, rule.checkId, labelX, itemY + 1.0f, alpha);
        RenderUtil.drawText(context, "Проверять ID", labelX + 18.0f, itemY + 4.0f, rule.checkId ? text : muted, GFonts.MEDIUM, 5.5f);
        float fieldX = x + 104.0f;
        float fieldW = w - 112.0f;
        String itemText = this.fieldText(rule, Field.ITEM_ID);
        boolean valid = manager.isValidItemId(itemText);
        int itemBorder = rule.checkId ? (valid ? -11090538 : -1808782) : ClientColors.SETTINGS_OUTLINE;
        this.drawTextField(context, rule, Field.ITEM_ID, itemText, fieldX, itemY, fieldW, 14.0f, itemBorder, alpha * (rule.checkId ? 1.0f : 0.45f));
        float nameY = y + 44.0f;
        this.drawToggle(context, rule.checkName, labelX, nameY + 1.0f, alpha);
        RenderUtil.drawText(context, "Проверять имя", labelX + 18.0f, nameY + 4.0f, rule.checkName ? text : muted, GFonts.MEDIUM, 5.5f);
        float nameX = x + 104.0f;
        float nameW = w - 112.0f;
        this.drawTextField(context, rule, Field.NAME, this.fieldText(rule, Field.NAME), nameX, nameY, nameW, 14.0f, ClientColors.SETTINGS_OUTLINE, alpha * (rule.checkName ? 1.0f : 0.45f));
        float scaleY = y + 65.0f;
        RenderUtil.drawText(context, "Размер", labelX, scaleY + 3.0f, muted, GFonts.MEDIUM, 5.5f);
        float sliderX = x + 52.0f;
        float valueW = 34.0f;
        float sliderW = w - 52.0f - valueW - 10.0f;
        float t = (ItemRenderManager.clampScale(rule.groundScale) - 0.25f) / 4.75f;
        int track = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha);
        int fill = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha);
        RenderUtil.drawRoundRect(context, sliderX, scaleY + 4.5f, sliderW, 3.0f, 1.5f, track);
        RenderUtil.drawRoundRect(context, sliderX, scaleY + 4.5f, Math.max(2.0f, sliderW * t), 3.0f, 1.5f, fill);
        RenderUtil.drawRoundRect(context, sliderX + sliderW * t - 2.0f, scaleY + 2.8f, 4.0f, 6.5f, 2.0f, fill);
        String scaleText = String.format(Locale.ROOT, "%.2fx", Float.valueOf(rule.groundScale));
        RenderUtil.drawText(context, scaleText, x + w - 8.0f - RenderUtil.getTextWidth(scaleText, GFonts.MEDIUM, 5.5f), scaleY + 3.0f, text, GFonts.MEDIUM, 5.5f);
        float glowY = y + 84.0f;
        this.drawToggle(context, rule.glow, labelX, glowY + 1.0f, alpha);
        RenderUtil.drawText(context, "Подсветка", labelX + 18.0f, glowY + 4.0f, rule.glow ? text : muted, GFonts.MEDIUM, 5.5f);
        float colorX = x + 104.0f;
        float colorW = w - 112.0f;
        this.drawColorPickerField(context, rule, colorX, glowY, colorW, 14.0f, alpha * (rule.glow ? 1.0f : 0.45f));
    }

    private ItemStack previewStack(ItemRenderRule rule) {
        if (rule == null || !rule.checkId || rule.itemId == null || rule.itemId.isBlank()) {
            return pyz1k.compat.PreviewStacks.of((ItemLike)Items.BARRIER);
        }
        try {
            Identifier id = Identifier.parse((String)rule.itemId.trim().toLowerCase(Locale.ROOT));
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            return item == null ? pyz1k.compat.PreviewStacks.of((ItemLike)Items.BARRIER) : pyz1k.compat.PreviewStacks.of((ItemLike)item);
        }
        catch (Exception ignored) {
            return pyz1k.compat.PreviewStacks.of((ItemLike)Items.BARRIER);
        }
    }

    private void drawTextField(GuiGraphicsExtractor context, ItemRenderRule rule, Field field, String value, float x, float y, float w, float h, int accentBorder, float alpha) {
        boolean focused = this.isFocused(rule, field);
        int bg = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha);
        int border = ColorUtil.multiplyAlpha(focused ? ClientColors.CATEGORY_ACCENT : accentBorder, alpha);
        int textColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        RenderUtil.drawRoundRect(context, x, y, w, h, 3.0f, bg);
        RenderUtil.drawBorder(context, x, y, w, h, 3.0f, border, 0.0f, 0.5f, 1.0f);
        String safe = value == null ? "" : value;
        float available = Math.max(1.0f, w - 8.0f);
        if (!focused) {
            String draw = ItemRenderSection.fitRight(safe, available, 5.5f);
            RenderUtil.drawText(context, draw, x + 4.0f, y + 4.0f, textColor, GFonts.MEDIUM, 5.5f);
            return;
        }
        TextViewport viewport = this.buildViewport(safe, available, 5.5f);
        int selStart = Math.min(this.caretIndex, this.selectionIndex);
        int selEnd = Math.max(this.caretIndex, this.selectionIndex);
        int visibleSelStart = Math.max(selStart, viewport.start());
        int visibleSelEnd = Math.min(selEnd, viewport.end());
        if (visibleSelEnd > visibleSelStart) {
            float selectionX = x + 4.0f + RenderUtil.getTextWidth(safe.substring(viewport.start(), visibleSelStart), GFonts.MEDIUM, 5.5f);
            float selectionW = RenderUtil.getTextWidth(safe.substring(visibleSelStart, visibleSelEnd), GFonts.MEDIUM, 5.5f);
            RenderUtil.drawRoundRect(context, selectionX - 0.5f, y + 2.0f, selectionW + 1.0f, h - 4.0f, 1.5f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * 0.38f));
        }
        String draw = safe.substring(viewport.start(), viewport.end());
        RenderUtil.drawText(context, draw, x + 4.0f, y + 4.0f, textColor, GFonts.MEDIUM, 5.5f);
        if ((System.currentTimeMillis() / 450L & 1L) == 0L) {
            int caret = ItemRenderSection.clampInt(this.caretIndex, viewport.start(), viewport.end());
            float caretX = x + 4.0f + RenderUtil.getTextWidth(safe.substring(viewport.start(), caret), GFonts.MEDIUM, 5.5f) - 2.25f;
            RenderUtil.drawRoundRect(context, Math.max(x + 3.0f, Math.min(x + w - 3.0f, caretX)), y + 3.0f, 0.8f, h - 6.0f, 0.4f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha));
        }
    }

    private void drawColorPickerField(GuiGraphicsExtractor context, ItemRenderRule rule, float x, float y, float w, float h, float alpha) {
        int bg = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha);
        int border = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha);
        int textColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int rgb = rule.glowColor & 0xFFFFFF;
        RenderUtil.drawRoundRect(context, x, y, w, h, 3.0f, bg);
        RenderUtil.drawBorder(context, x, y, w, h, 3.0f, border, 0.0f, 0.5f, 1.0f);
        RenderUtil.drawRoundRect(context, x + 3.0f, y + 2.0f, 10.0f, 10.0f, 2.0f, 0xFF000000 | rgb);
        String value = String.format(Locale.ROOT, "#%06X", rgb);
        RenderUtil.drawText(context, value, x + 17.0f, y + 4.0f, textColor, GFonts.MEDIUM, 5.5f);
    }

    private void drawSearchField(GuiGraphicsExtractor context, float x, float y, float w, float h, float alpha) {
        boolean focused = this.focusedField == Field.SEARCH;
        int bg = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha * 0.96f);
        int textColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int placeholderColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha * 0.9f);
        RenderUtil.drawRoundRect(context, x, y, w, h, 3.0f, bg);
        String value = this.currentSearchText();
        if (value.isEmpty() && !focused) {
            RenderUtil.drawText(context, "Поиск по ID или имени...", x + 4.0f, y + 4.0f, placeholderColor, GFonts.MEDIUM, 5.5f);
            return;
        }
        float available = Math.max(1.0f, w - 8.0f);
        if (!focused) {
            String draw = ItemRenderSection.fitRight(value, available, 5.5f);
            RenderUtil.drawText(context, draw, x + 4.0f, y + 4.0f, textColor, GFonts.MEDIUM, 5.5f);
            return;
        }
        TextViewport viewport = this.buildViewport(value, available, 5.5f);
        int selStart = Math.min(this.caretIndex, this.selectionIndex);
        int selEnd = Math.max(this.caretIndex, this.selectionIndex);
        int visibleSelStart = Math.max(selStart, viewport.start());
        int visibleSelEnd = Math.min(selEnd, viewport.end());
        if (visibleSelEnd > visibleSelStart) {
            float selectionX = x + 4.0f + RenderUtil.getTextWidth(value.substring(viewport.start(), visibleSelStart), GFonts.MEDIUM, 5.5f);
            float selectionW = RenderUtil.getTextWidth(value.substring(visibleSelStart, visibleSelEnd), GFonts.MEDIUM, 5.5f);
            RenderUtil.drawRoundRect(context, selectionX - 0.5f, y + 2.0f, selectionW + 1.0f, h - 4.0f, 1.5f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * 0.38f));
        }
        String draw = value.substring(viewport.start(), viewport.end());
        RenderUtil.drawText(context, draw, x + 4.0f, y + 4.0f, textColor, GFonts.MEDIUM, 5.5f);
        if ((System.currentTimeMillis() / 450L & 1L) == 0L) {
            int caret = ItemRenderSection.clampInt(this.caretIndex, viewport.start(), viewport.end());
            float caretX = x + 4.0f + RenderUtil.getTextWidth(value.substring(viewport.start(), caret), GFonts.MEDIUM, 5.5f) - 2.25f;
            RenderUtil.drawRoundRect(context, Math.max(x + 3.0f, Math.min(x + w - 3.0f, caretX)), y + 3.0f, 0.8f, h - 6.0f, 0.4f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha));
        }
    }

    private void drawScrollbar(GuiGraphicsExtractor context, Layout layout, float totalH, float maxScroll, float alpha) {
        float trackX = layout.listX + layout.listW - 7.0f;
        float trackY = layout.listY;
        float trackH = Math.max(1.0f, layout.listH);
        int track = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha * 0.96f);
        RenderUtil.drawRoundRect(context, trackX, trackY, 3.0f, trackH, 1.5f, track);
        if (maxScroll <= 0.0f || totalH <= 0.0f) {
            return;
        }
        float thumbH = this.scrollbarThumbHeight(trackH, totalH, layout.listH);
        float travel = Math.max(0.0f, trackH - thumbH);
        float t = ItemRenderSection.clamp(this.scrollValue / maxScroll, 0.0f, 1.0f);
        float thumbY = trackY + travel * t;
        int thumb = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * (this.draggingScrollbar ? 1.0f : 0.8f));
        RenderUtil.drawRoundRect(context, trackX, thumbY, 3.0f, thumbH, 1.5f, thumb);
    }

    private float scrollbarThumbHeight(float trackH, float totalH, float viewportH) {
        if (totalH <= 0.0f) {
            return trackH;
        }
        return Math.min(trackH, Math.max(22.0f, trackH * Math.min(1.0f, viewportH / totalH)));
    }

    private boolean isScrollbarArea(double mouseX, double mouseY, Layout layout) {
        return ItemRenderSection.hovered(mouseX, mouseY, layout.listX + layout.listW - 11.0f, layout.listY + 2.0f, 10.0f, layout.listH - 4.0f);
    }

    private void startScrollbarDrag(float mouseY, Layout layout, float totalH, float maxScroll) {
        if (maxScroll <= 0.0f || totalH <= 0.0f) {
            return;
        }
        float trackY = layout.listY;
        float trackH = Math.max(1.0f, layout.listH);
        float thumbH = this.scrollbarThumbHeight(trackH, totalH, layout.listH);
        float travel = Math.max(0.0f, trackH - thumbH);
        float thumbY = trackY + travel * ItemRenderSection.clamp(this.scrollValue / maxScroll, 0.0f, 1.0f);
        this.draggingScrollbar = true;
        if (mouseY >= thumbY && mouseY <= thumbY + thumbH) {
            this.scrollbarGrabOffset = mouseY - thumbY;
        } else {
            this.scrollbarGrabOffset = thumbH * 0.5f;
            this.updateScrollbarFromMouse(mouseY, layout, totalH, maxScroll);
        }
    }

    private void updateScrollbarFromMouse(float mouseY, Layout layout, float totalH, float maxScroll) {
        float thumbH;
        if (maxScroll <= 0.0f || totalH <= 0.0f) {
            this.scrollTarget = 0.0f;
            this.scrollValue = 0.0f;
            return;
        }
        float trackY = layout.listY;
        float trackH = Math.max(1.0f, layout.listH);
        float travel = Math.max(0.0f, trackH - (thumbH = this.scrollbarThumbHeight(trackH, totalH, layout.listH)));
        if (travel <= 0.0f) {
            this.scrollTarget = 0.0f;
            this.scrollValue = 0.0f;
            return;
        }
        float thumbY = ItemRenderSection.clamp(mouseY - this.scrollbarGrabOffset, trackY, trackY + travel);
        float t = (thumbY - trackY) / travel;
        this.scrollValue = this.scrollTarget = t * maxScroll;
    }

    private void drawToggle(GuiGraphicsExtractor context, boolean enabled, float x, float y, float alpha) {
        int bg = ColorUtil.multiplyAlpha(enabled ? ClientColors.CATEGORY_ACCENT : ClientColors.SETTINGS_BACKGROUND, alpha);
        int outline = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha);
        RenderUtil.drawRoundRect(context, x, y, 14.0f, 9.0f, 4.0f, bg);
        RenderUtil.drawBorder(context, x, y, 14.0f, 9.0f, 4.0f, outline, 0.0f, 0.5f, 1.0f);
        float knobX = x + (enabled ? 7.0f : 1.5f);
        RenderUtil.drawRoundRect(context, knobX, y + 1.5f, 6.0f, 6.0f, 3.0f, ColorUtil.multiplyAlpha(-723464, alpha));
    }

    private void drawButton(GuiGraphicsExtractor context, UiAnimationEngine animation, String key, String label, float x, float y, float w, float h, int mouseX, int mouseY, float alpha) {
        boolean hover = ItemRenderSection.hovered(mouseX, mouseY, x, y, w, h);
        float hoverAnim = animation.animate01(key + ":hover", hover ? 1.0f : 0.0f, 12.0f, 0.0f);
        int base = ColorUtil.interpolate(ClientColors.CATEGORY_BACKGROUND, ClientColors.CATEGORY_ACCENT, hoverAnim * 0.28f);
        RenderUtil.drawRoundRect(context, x, y, w, h, 4.0f, ColorUtil.multiplyAlpha(base, alpha * 0.96f));
        float tx = x + (w - RenderUtil.getTextWidth(label, GFonts.MEDIUM, 6.0f)) * 0.5f;
        RenderUtil.drawText(context, label, tx, y + 5.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.MEDIUM, 6.0f);
    }

    public boolean handleClick(MouseButtonEvent click, float panelX, float panelY, float panelWidth, float panelHeight) {
        ItemRenderManager manager = ItemRenderManager.getInstance();
        if (this.confirmDeleteAll) {
            if (pyz1k.compat.LegacyInput.fromNativeMouse(click.button()) != 0) {
                return true;
            }
            ConfirmLayout confirm = this.confirmLayout();
            if (ItemRenderSection.hovered(click.x(), click.y(), confirm.confirmX, confirm.buttonY, confirm.buttonW, confirm.buttonH)) {
                this.deleteAllRules(manager);
                this.confirmDeleteAll = false;
                return true;
            }
            if (ItemRenderSection.hovered(click.x(), click.y(), confirm.cancelX, confirm.buttonY, confirm.buttonW, confirm.buttonH)) {
                this.confirmDeleteAll = false;
                return true;
            }
            return true;
        }
        if (this.colorPickerHost.isColorPopupOpen()) {
            boolean handled = this.colorPickerHost.handleClick(click, panelX, panelY, panelWidth, panelHeight, CategoryBarSection.TopTab.VISUALS);
            this.syncColorPicker(manager);
            return handled;
        }
        if (pyz1k.compat.LegacyInput.fromNativeMouse(click.button()) != 0) {
            return false;
        }
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        if (ItemRenderSection.hovered(click.x(), click.y(), layout.searchX, layout.searchY, layout.searchW, 14.0f)) {
            this.focusSearch();
            return true;
        }
        if (ItemRenderSection.hovered(click.x(), click.y(), layout.deleteX, layout.headerY, layout.deleteW, 16.0f)) {
            this.commitEdit();
            if (!manager.getRules().isEmpty()) {
                this.confirmDeleteAll = true;
            }
            return true;
        }
        if (ItemRenderSection.hovered(click.x(), click.y(), layout.addX, layout.headerY, layout.addW, 16.0f)) {
            this.commitEdit();
            this.searchText = "";
            ItemRenderRule rule = manager.addRule();
            this.focus(rule, Field.ITEM_ID);
            this.scrollTarget = Float.MAX_VALUE;
            return true;
        }
        if (!ItemRenderSection.hovered(click.x(), click.y(), layout.listX, layout.listY, layout.listW, layout.listH)) {
            this.commitEdit();
            return false;
        }
        List<ItemRenderRule> rules = this.filteredRules(manager.getRules());
        float totalH = this.contentHeight(rules.size());
        float maxScroll = Math.max(0.0f, totalH - layout.listH);
        if (this.isScrollbarArea(click.x(), click.y(), layout) && maxScroll > 0.0f) {
            this.commitEdit();
            this.startScrollbarDrag((float)click.y(), layout, totalH, maxScroll);
            return true;
        }
        for (int i = 0; i < rules.size(); ++i) {
            ItemRenderRule rule = rules.get(i);
            float x = layout.listX + 5.0f;
            float y = layout.listY + 5.0f + (float)i * 110.0f - this.scrollValue;
            float w = layout.listW - 18.0f;
            if (!ItemRenderSection.hovered(click.x(), click.y(), x, y, w, 103.0f)) continue;
            if (ItemRenderSection.hovered(click.x(), click.y(), x + w - 22.0f, y + 5.0f, 14.0f, 13.0f)) {
                this.commitEdit();
                manager.removeRule(rule);
                if (rule.id.equals(this.draggingScaleRuleId)) {
                    this.draggingScaleRuleId = null;
                }
                return true;
            }
            if (ItemRenderSection.hovered(click.x(), click.y(), x + 8.0f, y + 25.0f, 94.0f, 13.0f)) {
                this.commitEdit();
                rule.checkId = !rule.checkId;
                manager.markDirty();
                return true;
            }
            float fieldX = x + 104.0f;
            float fieldW = w - 112.0f;
            if (rule.checkId && ItemRenderSection.hovered(click.x(), click.y(), fieldX, y + 24.0f, fieldW, 14.0f)) {
                this.focus(rule, Field.ITEM_ID);
                return true;
            }
            if (ItemRenderSection.hovered(click.x(), click.y(), x + 8.0f, y + 45.0f, 94.0f, 13.0f)) {
                this.commitEdit();
                rule.checkName = !rule.checkName;
                manager.markDirty();
                return true;
            }
            float nameX = x + 104.0f;
            float nameW = w - 112.0f;
            if (rule.checkName && ItemRenderSection.hovered(click.x(), click.y(), nameX, y + 44.0f, nameW, 14.0f)) {
                this.focus(rule, Field.NAME);
                return true;
            }
            float sliderX = x + 52.0f;
            float sliderW = w - 52.0f - 34.0f - 10.0f;
            if (ItemRenderSection.hovered(click.x(), click.y(), sliderX - 2.0f, y + 64.0f, sliderW + 4.0f, 12.0f)) {
                this.commitEdit();
                this.draggingScaleRuleId = rule.id;
                this.setScaleFromMouse(rule, (float)click.x(), sliderX, sliderW);
                return true;
            }
            if (ItemRenderSection.hovered(click.x(), click.y(), x + 8.0f, y + 85.0f, 94.0f, 13.0f)) {
                this.commitEdit();
                rule.glow = !rule.glow;
                manager.markDirty();
                return true;
            }
            float colorX = x + 104.0f;
            float colorW = w - 112.0f;
            if (rule.glow && ItemRenderSection.hovered(click.x(), click.y(), colorX, y + 84.0f, colorW, 14.0f)) {
                this.commitEdit();
                this.openColorPicker(rule, (float)click.x(), (float)click.y());
                return true;
            }
            this.commitEdit();
            return true;
        }
        this.commitEdit();
        return true;
    }

    public boolean handleMouseDragged(double mouseX, double mouseY, int button, float panelX, float panelY, float panelWidth, float panelHeight) {
        if (button != 0) {
            return false;
        }
        if (this.draggingScrollbar) {
            Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
            List<ItemRenderRule> rules = this.filteredRules(ItemRenderManager.getInstance().getRules());
            float totalH = this.contentHeight(rules.size());
            float maxScroll = Math.max(0.0f, totalH - layout.listH);
            this.updateScrollbarFromMouse((float)mouseY, layout, totalH, maxScroll);
            return true;
        }
        if (this.draggingScaleRuleId == null) {
            return false;
        }
        this.updateScaleFromMouse((float)mouseX, panelX, panelY, panelWidth, panelHeight);
        return true;
    }

    public boolean handleMouseReleased(int button) {
        if (this.colorPickerHost.isColorPopupOpen()) {
            boolean handled = this.colorPickerHost.handleMouseReleased(button);
            this.syncColorPicker(ItemRenderManager.getInstance());
            if (handled) {
                return true;
            }
        }
        if (button == 0 && this.draggingScrollbar) {
            this.draggingScrollbar = false;
            return true;
        }
        if (button == 0 && this.draggingScaleRuleId != null) {
            this.draggingScaleRuleId = null;
            ItemRenderManager.getInstance().flushAutoSave();
            return true;
        }
        return false;
    }

    public boolean handleScroll(double mouseX, double mouseY, double verticalAmount, float panelX, float panelY, float panelWidth, float panelHeight) {
        if (this.confirmDeleteAll) {
            return true;
        }
        if (this.colorPickerHost.isColorPopupOpen()) {
            return true;
        }
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        if (!ItemRenderSection.hovered(mouseX, mouseY, layout.listX, layout.listY, layout.listW, layout.listH) || verticalAmount == 0.0) {
            return false;
        }
        this.scrollTarget = Math.max(0.0f, this.scrollTarget - (float)verticalAmount * 24.0f);
        return true;
    }

    public boolean handleKeyPressed(KeyEvent key) {
        if (this.confirmDeleteAll) {
            if (pyz1k.compat.LegacyInput.fromNativeKey(key.key()) == 256) {
                this.confirmDeleteAll = false;
            }
            return true;
        }
        if (this.colorPickerHost.isColorPopupOpen()) {
            this.syncColorPicker(ItemRenderManager.getInstance());
            if (pyz1k.compat.LegacyInput.fromNativeKey(key.key()) == 256) {
                this.colorPickerHost.requestClosePopup();
            }
            return true;
        }
        if (this.focusedField == Field.NONE) {
            return false;
        }
        int code = pyz1k.compat.LegacyInput.fromNativeKey(key.key());
        if (code == 256 || code == 257 || code == 335) {
            this.commitEdit();
            return true;
        }
        boolean ctrl = this.isCtrlDown();
        boolean shift = this.isShiftDown();
        if (ctrl && code == 65) {
            this.selectionIndex = 0;
            this.caretIndex = this.editingText.length();
            return true;
        }
        if (ctrl && code == 67) {
            this.copySelection();
            return true;
        }
        if (ctrl && code == 88) {
            if (this.hasSelection()) {
                this.copySelection();
                this.deleteSelection();
            }
            return true;
        }
        if (ctrl && code == 86) {
            String clip = this.getClipboard();
            if (clip != null && !clip.isEmpty()) {
                this.insertText(clip);
            }
            return true;
        }
        if (code == 263) {
            this.moveCaret(-1, shift);
            return true;
        }
        if (code == 262) {
            this.moveCaret(1, shift);
            return true;
        }
        if (code == 268) {
            this.setCaret(0, shift);
            return true;
        }
        if (code == 269) {
            this.setCaret(this.editingText.length(), shift);
            return true;
        }
        if (code == 259) {
            if (!this.deleteSelection() && this.caretIndex > 0) {
                int previous = this.previousCodePointIndex(this.editingText, this.caretIndex);
                this.editingText = this.editingText.substring(0, previous) + this.editingText.substring(this.caretIndex);
                this.caretIndex = previous;
                this.selectionIndex = previous;
                this.onTextEdited();
            }
            return true;
        }
        if (code == 261) {
            if (!this.deleteSelection() && this.caretIndex < this.editingText.length()) {
                int next = this.nextCodePointIndex(this.editingText, this.caretIndex);
                this.editingText = this.editingText.substring(0, this.caretIndex) + this.editingText.substring(next);
                this.selectionIndex = this.caretIndex;
                this.onTextEdited();
            }
            return true;
        }
        return true;
    }

    public boolean handleCharTyped(int codePoint) {
        if (this.confirmDeleteAll) {
            return true;
        }
        if (this.colorPickerHost.isColorPopupOpen()) {
            return true;
        }
        if (this.focusedField == Field.NONE || !Character.isValidCodePoint(codePoint) || codePoint < 32 || codePoint == 127) {
            return false;
        }
        String value = this.sanitizeForField(new String(Character.toChars(codePoint)), this.focusedField);
        if (value.isEmpty()) {
            return true;
        }
        this.insertText(value);
        return true;
    }

    public boolean isTextInputActive() {
        return this.focusedField != Field.NONE;
    }

    public static boolean isAnyTextInputActive() {
        return activeTextEditor != null && activeTextEditor.isTextInputActive();
    }

    public void commitEdit() {
        if (this.focusedField == Field.NONE) {
            return;
        }
        if (this.focusedField == Field.SEARCH) {
            this.searchText = this.editingText;
            this.clearEditorFocus();
            return;
        }
        if (this.focusedRuleId != null) {
            ItemRenderManager manager = ItemRenderManager.getInstance();
            ItemRenderRule rule = this.findRule(this.focusedRuleId);
            if (rule != null) {
                switch (this.focusedField.ordinal()) {
                    case 2: {
                        rule.itemId = this.editingText.trim();
                        break;
                    }
                    case 3: {
                        rule.nameContains = this.editingText;
                        break;
                    }
                }
                manager.markDirty();
            }
        }
        this.clearEditorFocus();
    }

    private void clearEditorFocus() {
        this.focusedRuleId = null;
        this.focusedField = Field.NONE;
        this.editingText = "";
        this.caretIndex = 0;
        this.selectionIndex = 0;
        if (activeTextEditor == this) {
            activeTextEditor = null;
        }
    }

    private void focusSearch() {
        if (this.focusedField == Field.SEARCH) {
            return;
        }
        this.commitEdit();
        this.focusedRuleId = null;
        this.focusedField = Field.SEARCH;
        this.editingText = this.searchText;
        this.selectionIndex = this.caretIndex = this.editingText.length();
        activeTextEditor = this;
    }

    private String currentSearchText() {
        return this.focusedField == Field.SEARCH ? this.editingText : this.searchText;
    }

    private List<ItemRenderRule> filteredRules(List<ItemRenderRule> source) {
        String query = this.currentSearchText().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return source;
        }
        ArrayList<ItemRenderRule> result = new ArrayList<ItemRenderRule>();
        for (ItemRenderRule rule : source) {
            String name;
            String itemId = rule.itemId == null ? "" : rule.itemId.toLowerCase(Locale.ROOT);
            String string = name = rule.nameContains == null ? "" : rule.nameContains.toLowerCase(Locale.ROOT);
            if (!itemId.contains(query) && !name.contains(query)) continue;
            result.add(rule);
        }
        return result;
    }

    private float contentHeight(int count) {
        return count <= 0 ? 0.0f : (float)count * 103.0f + (float)Math.max(0, count - 1) * 7.0f;
    }

    private void focus(ItemRenderRule rule, Field field) {
        if (this.isFocused(rule, field)) {
            return;
        }
        this.commitEdit();
        this.focusedRuleId = rule.id;
        this.focusedField = field;
        this.editingText = switch (field.ordinal()) {
            case 2 -> {
                if (rule.itemId == null) {
                    yield "";
                }
                yield rule.itemId;
            }
            case 3 -> {
                if (rule.nameContains == null) {
                    yield "";
                }
                yield rule.nameContains;
            }
            default -> "";
        };
        this.selectionIndex = this.caretIndex = this.editingText.length();
        activeTextEditor = this;
    }

    private String fieldText(ItemRenderRule rule, Field field) {
        if (this.isFocused(rule, field)) {
            return this.editingText;
        }
        return switch (field.ordinal()) {
            case 2 -> {
                if (rule.itemId == null) {
                    yield "";
                }
                yield rule.itemId;
            }
            case 3 -> {
                if (rule.nameContains == null) {
                    yield "";
                }
                yield rule.nameContains;
            }
            default -> "";
        };
    }

    private boolean isFocused(ItemRenderRule rule, Field field) {
        return rule != null && rule.id != null && rule.id.equals(this.focusedRuleId) && this.focusedField == field;
    }

    private ItemRenderRule findRule(String id) {
        if (id == null) {
            return null;
        }
        for (ItemRenderRule rule : ItemRenderManager.getInstance().getRules()) {
            if (!id.equals(rule.id)) continue;
            return rule;
        }
        return null;
    }

    private void updateScaleFromMouse(float mouseX, float panelX, float panelY, float panelWidth, float panelHeight) {
        ItemRenderRule rule = this.findRule(this.draggingScaleRuleId);
        if (rule == null) {
            this.draggingScaleRuleId = null;
            return;
        }
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        List<ItemRenderRule> rules = ItemRenderManager.getInstance().getRules();
        int index = rules.indexOf(rule);
        if (index < 0) {
            return;
        }
        float x = layout.listX + 5.0f;
        float w = layout.listW - 18.0f;
        float sliderX = x + 52.0f;
        float sliderW = w - 52.0f - 34.0f - 10.0f;
        this.setScaleFromMouse(rule, mouseX, sliderX, sliderW);
    }

    private void setScaleFromMouse(ItemRenderRule rule, float mouseX, float sliderX, float sliderW) {
        float t = ItemRenderSection.clamp((mouseX - sliderX) / sliderW, 0.0f, 1.0f);
        float value = 0.25f + t * 4.75f;
        rule.groundScale = (float)Math.round(value * 20.0f) / 20.0f;
        ItemRenderManager.getInstance().markDirty();
    }

    private String sanitizeForField(String input, Field field) {
        int cp;
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < input.length(); i += Character.charCount(cp)) {
            cp = input.codePointAt(i);
            if (cp < 32 || cp == 127) continue;
            char ch = (char)cp;
            if (field == Field.ITEM_ID) {
                if (!((ch = Character.toLowerCase(ch)) >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9' || ch == '_' || ch == ':' || ch == '/' || ch == '.') && ch != '-') continue;
                out.append(ch);
                continue;
            }
            out.appendCodePoint(cp);
        }
        return out.toString();
    }

    private boolean hasSelection() {
        return this.caretIndex != this.selectionIndex;
    }

    private boolean deleteSelection() {
        if (!this.hasSelection()) {
            return false;
        }
        int start = Math.min(this.caretIndex, this.selectionIndex);
        int end = Math.max(this.caretIndex, this.selectionIndex);
        this.editingText = this.editingText.substring(0, start) + this.editingText.substring(end);
        this.caretIndex = start;
        this.selectionIndex = start;
        this.onTextEdited();
        return true;
    }

    private void insertText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        String safe = this.sanitizeForField(text, this.focusedField);
        if (safe.isEmpty()) {
            return;
        }
        this.deleteSelection();
        int caret = ItemRenderSection.clampInt(this.caretIndex, 0, this.editingText.length());
        this.editingText = this.editingText.substring(0, caret) + safe + this.editingText.substring(caret);
        this.selectionIndex = this.caretIndex = caret + safe.length();
        this.onTextEdited();
    }

    private void onTextEdited() {
        if (this.focusedField == Field.SEARCH) {
            this.scrollTarget = 0.0f;
            this.scrollValue = 0.0f;
        }
    }

    private void moveCaret(int direction, boolean selecting) {
        if (!selecting && this.hasSelection()) {
            int collapsed = direction < 0 ? Math.min(this.caretIndex, this.selectionIndex) : Math.max(this.caretIndex, this.selectionIndex);
            this.setCaret(collapsed, false);
            return;
        }
        int next = direction < 0 ? this.previousCodePointIndex(this.editingText, this.caretIndex) : this.nextCodePointIndex(this.editingText, this.caretIndex);
        this.setCaret(next, selecting);
    }

    private void setCaret(int index, boolean selecting) {
        this.caretIndex = ItemRenderSection.clampInt(index, 0, this.editingText.length());
        if (!selecting) {
            this.selectionIndex = this.caretIndex;
        }
    }

    private void copySelection() {
        if (!this.hasSelection()) {
            return;
        }
        int start = Math.min(this.caretIndex, this.selectionIndex);
        int end = Math.max(this.caretIndex, this.selectionIndex);
        this.setClipboard(this.editingText.substring(start, end));
    }

    private int previousCodePointIndex(String text, int index) {
        int safe = ItemRenderSection.clampInt(index, 0, text.length());
        return safe <= 0 ? 0 : text.offsetByCodePoints(safe, -1);
    }

    private int nextCodePointIndex(String text, int index) {
        int safe = ItemRenderSection.clampInt(index, 0, text.length());
        return safe >= text.length() ? text.length() : text.offsetByCodePoints(safe, 1);
    }

    private TextViewport buildViewport(String text, float maxWidth, float size) {
        int previous;
        int next;
        int caret = ItemRenderSection.clampInt(this.caretIndex, 0, text.length());
        if (RenderUtil.getTextWidth(text, GFonts.MEDIUM, size) <= maxWidth) {
            return new TextViewport(0, text.length());
        }
        int start = 0;
        while (start < caret && RenderUtil.getTextWidth(text.substring(start, caret), GFonts.MEDIUM, size) > maxWidth) {
            start = this.nextCodePointIndex(text, start);
        }
        int end = caret;
        while (end < text.length() && !(RenderUtil.getTextWidth(text.substring(start, next = this.nextCodePointIndex(text, end)), GFonts.MEDIUM, size) > maxWidth)) {
            end = next;
        }
        while (start > 0 && !(RenderUtil.getTextWidth(text.substring(previous = this.previousCodePointIndex(text, start), end), GFonts.MEDIUM, size) > maxWidth)) {
            start = previous;
        }
        return new TextViewport(start, Math.max(start, end));
    }

    private void openColorPicker(ItemRenderRule rule, float anchorX, float anchorY) {
        this.colorPickerRuleId = rule.id;
        this.colorPickerSetting = new ColorSetting("Цвет подсветки", rule.glowColor);
        this.colorPickerLastRgb = rule.glowColor & 0xFFFFFF;
        this.colorPickerHost.openStandaloneColorPicker(this.colorPickerSetting, "Цвет подсветки", anchorX, anchorY);
    }

    private void syncColorPicker(ItemRenderManager manager) {
        if (this.colorPickerSetting == null || this.colorPickerRuleId == null) {
            return;
        }
        ItemRenderRule rule = this.findRule(this.colorPickerRuleId);
        if (rule == null) {
            this.colorPickerHost.requestClosePopup();
            this.colorPickerSetting = null;
            this.colorPickerRuleId = null;
            this.colorPickerLastRgb = -1;
            return;
        }
        int rgb = this.colorPickerSetting.getColor() & 0xFFFFFF;
        if (rgb != this.colorPickerLastRgb) {
            this.colorPickerLastRgb = rgb;
            rule.glowColor = 0xFF000000 | rgb;
            manager.markDirty();
        }
    }

    private String getClipboard() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.keyboardHandler != null ? mc.keyboardHandler.getClipboard() : "";
    }

    private void setClipboard(String value) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.keyboardHandler != null) {
            mc.keyboardHandler.setClipboard(value == null ? "" : value);
        }
    }

    private boolean isCtrlDown() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) {
            return false;
        }
        long handle = mc.getWindow().handle();
        return LegacyInput.glfwGetKey((long)handle, (int)341) == 1 || LegacyInput.glfwGetKey((long)handle, (int)345) == 1;
    }

    private boolean isShiftDown() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) {
            return false;
        }
        long handle = mc.getWindow().handle();
        return LegacyInput.glfwGetKey((long)handle, (int)340) == 1 || LegacyInput.glfwGetKey((long)handle, (int)344) == 1;
    }

    private void renderDeleteAllConfirm(GuiGraphicsExtractor context, UiAnimationEngine animation, int mouseX, int mouseY, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        float screenW = mc != null && mc.getWindow() != null ? (float)mc.getWindow().getGuiScaledWidth() : 320.0f;
        float screenH = mc != null && mc.getWindow() != null ? (float)mc.getWindow().getGuiScaledHeight() : 240.0f;
        RenderUtil.drawRoundRect(context, 0.0f, 0.0f, screenW, screenH, 0.0f, ColorUtil.multiplyAlpha(-805306368, alpha * 0.72f));
        ConfirmLayout box = this.confirmLayout();
        int boxBg = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha);
        int boxBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha);
        int title = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int muted = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha);
        RenderUtil.drawRoundRect(context, box.x, box.y, box.w, box.h, 6.0f, boxBg);
        RenderUtil.drawBorder(context, box.x, box.y, box.w, box.h, 6.0f, boxBorder, 0.0f, 0.5f, 1.0f);
        String titleText = "Точно удалить?";
        float titleX = box.x + (box.w - RenderUtil.getTextWidth(titleText, GFonts.MEDIUM, 8.0f)) * 0.5f;
        RenderUtil.drawText(context, titleText, titleX, box.y + 12.0f, title, GFonts.MEDIUM, 8.0f);
        String hint = "Все будет удаленно";
        float hintX = box.x + (box.w - RenderUtil.getTextWidth(hint, GFonts.MEDIUM, 5.5f)) * 0.5f;
        RenderUtil.drawText(context, hint, hintX, box.y + 29.0f, muted, GFonts.MEDIUM, 5.5f);
        this.drawButton(context, animation, "itemrender:confirm-delete", "Удалить", box.confirmX, box.buttonY, box.buttonW, box.buttonH, mouseX, mouseY, alpha);
        this.drawButton(context, animation, "itemrender:cancel-delete", "Отмена", box.cancelX, box.buttonY, box.buttonW, box.buttonH, mouseX, mouseY, alpha);
    }

    private ConfirmLayout confirmLayout() {
        Minecraft mc = Minecraft.getInstance();
        float screenW = mc != null && mc.getWindow() != null ? (float)mc.getWindow().getGuiScaledWidth() : 320.0f;
        float screenH = mc != null && mc.getWindow() != null ? (float)mc.getWindow().getGuiScaledHeight() : 240.0f;
        float w = 190.0f;
        float h = 78.0f;
        float x = (screenW - w) * 0.5f;
        float y = (screenH - h) * 0.5f;
        float buttonW = 72.0f;
        float buttonH = 16.0f;
        float gap = 8.0f;
        float buttonsW = buttonW * 2.0f + gap;
        float confirmX = x + (w - buttonsW) * 0.5f;
        float cancelX = confirmX + buttonW + gap;
        float buttonY = y + h - 25.0f;
        return new ConfirmLayout(x, y, w, h, confirmX, cancelX, buttonY, buttonW, buttonH);
    }

    private void deleteAllRules(ItemRenderManager manager) {
        this.commitEdit();
        for (ItemRenderRule rule : new ArrayList<ItemRenderRule>(manager.getRules())) {
            manager.removeRule(rule);
        }
        manager.markDirty();
        this.searchText = "";
        this.scrollTarget = 0.0f;
        this.scrollValue = 0.0f;
        this.draggingScrollbar = false;
        this.draggingScaleRuleId = null;
        this.colorPickerRuleId = null;
        this.colorPickerSetting = null;
        this.colorPickerLastRgb = -1;
        this.clearEditorFocus();
    }

    private Layout layout(float panelX, float panelY, float panelWidth, float panelHeight) {
        float contentX = panelX + 10.0f;
        float headerY = panelY + 12.0f;
        float addW = 66.0f;
        float addX = panelX + panelWidth - 10.0f - addW;
        float deleteW = 62.0f;
        float deleteX = addX - 6.0f - deleteW;
        float searchX = contentX;
        float searchY = headerY + 20.0f;
        float searchW = Math.max(40.0f, panelWidth - 20.0f);
        float listX = panelX + 9.0f;
        float listY = panelY + 54.0f;
        float listW = panelWidth - 18.0f;
        float listH = panelHeight - 54.0f - 8.0f;
        return new Layout(contentX, headerY, addX, addW, deleteX, deleteW, searchX, searchY, searchW, listX, listY, listW, listH);
    }

    private static String fitRight(String text, float maxWidth, float size) {
        int start;
        int cp;
        if (RenderUtil.getTextWidth(text, GFonts.MEDIUM, size) <= maxWidth) {
            return text;
        }
        for (start = 0; start < text.length() && RenderUtil.getTextWidth(text.substring(start), GFonts.MEDIUM, size) > maxWidth; start += Character.charCount(cp)) {
            cp = text.codePointAt(start);
        }
        return text.substring(Math.min(start, text.length()));
    }

    private static boolean hovered(double mouseX, double mouseY, float x, float y, float w, float h) {
        return mouseX >= (double)x && mouseX <= (double)(x + w) && mouseY >= (double)y && mouseY <= (double)(y + h);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static enum Field {
        NONE,
        SEARCH,
        ITEM_ID,
        NAME;

    }

    private record Layout(float contentX, float headerY, float addX, float addW, float deleteX, float deleteW, float searchX, float searchY, float searchW, float listX, float listY, float listW, float listH) {
    }

    private record TextViewport(int start, int end) {
    }

    private record ConfirmLayout(float x, float y, float w, float h, float confirmX, float cancelX, float buttonY, float buttonW, float buttonH) {
    }
}

