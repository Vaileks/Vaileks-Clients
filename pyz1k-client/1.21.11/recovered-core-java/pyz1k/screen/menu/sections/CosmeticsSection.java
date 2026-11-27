/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  org.joml.Matrix3x2fStack
 *  org.lwjgl.glfw.GLFW
 */
package pyz1k.screen.menu.sections;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;
import pyz1k.cosmetics.ChickenMesh;
import pyz1k.cosmetics.CosmeticHat;
import pyz1k.cosmetics.CosmeticsManager;
import pyz1k.cosmetics.KirbyMesh;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class CosmeticsSection {
    private static final float CARD_H = 72.0f;
    private static final float CARD_GAP = 6.0f;
    private static final float SEARCH_H = 14.0f;
    private static final int COLUMNS = 3;
    private Page page = Page.HATS;
    private String searchText = "";
    private boolean searchFocused;
    private float scrollTarget;
    private float scrollValue;
    private boolean draggingScrollbar;
    private float scrollbarGrabOffset;

    public void render(class_332 context, UiAnimationEngine animation, float panelX, float panelY, float panelWidth, float panelHeight, int mouseX, int mouseY, float alpha) {
        CosmeticsManager manager = CosmeticsManager.getInstance();
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        List<CosmeticHat> hats = this.filteredHats(manager);
        int itemCount = hats.size();
        int titleColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int muted = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha * 0.9f);
        RenderUtil.drawText(context, "Cosmetics", layout.contentX, layout.titleY, titleColor, GFonts.MEDIUM, 9.0f);
        this.drawButton(context, animation, "cosmetics:page:hats", "\u0428\u043b\u044f\u043f\u044b", layout.contentX, layout.filterY, 68.0f, 16.0f, mouseX, mouseY, alpha, this.page == Page.HATS);
        this.drawButton(context, animation, "cosmetics:page:models", "\u041c\u043e\u0434\u0435\u043b\u0438", layout.contentX + 73.0f, layout.filterY, 68.0f, 16.0f, mouseX, mouseY, alpha, this.page == Page.MODELS);
        if (this.page == Page.MODELS) {
            this.renderModels(context, animation, manager, layout, mouseX, mouseY, alpha);
            return;
        }
        this.drawHelmetMode(context, animation, manager, layout.optionX, layout.filterY - 1.0f, layout.optionW, 16.0f, mouseX, mouseY, alpha);
        this.drawButton(context, animation, "cosmetics:none:hat", "\u0411\u0435\u0437 \u0448\u043b\u044f\u043f\u044b", layout.clearX, layout.titleY - 3.0f, layout.clearW, 16.0f, mouseX, mouseY, alpha, manager.getSelected() == null);
        int searchBg = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha);
        RenderUtil.drawRoundRect(context, layout.searchX, layout.searchY, layout.searchW, 14.0f, 3.0f, searchBg);
        String searchDraw = this.searchText.isEmpty() && !this.searchFocused ? "\u041f\u043e\u0438\u0441\u043a \u043f\u043e \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u044e..." : this.searchText;
        int searchColor = this.searchText.isEmpty() && !this.searchFocused ? muted : titleColor;
        String fittedSearch = this.fitText(searchDraw, Math.max(1.0f, layout.searchW - 8.0f), 5.5f);
        RenderUtil.drawText(context, fittedSearch, layout.searchX + 4.0f, layout.searchY + 4.0f, searchColor, GFonts.MEDIUM, 5.5f);
        if (this.searchFocused && (System.currentTimeMillis() / 450L & 1L) == 0L) {
            float caretX = Math.min(layout.searchX + layout.searchW - 3.0f, layout.searchX + 4.0f + RenderUtil.getTextWidth(fittedSearch, GFonts.MEDIUM, 5.5f) + 1.0f);
            RenderUtil.drawRoundRect(context, caretX, layout.searchY + 3.0f, 0.8f, 8.0f, 0.4f, titleColor);
        }
        int listBg = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha * 0.28f);
        int listBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha * 0.75f);
        RenderUtil.drawRoundRect(context, layout.listX, layout.listY, layout.listW, layout.listH, 4.0f, listBg);
        RenderUtil.drawBorder(context, layout.listX, layout.listY, layout.listW, layout.listH, 4.0f, listBorder, 0.0f, 0.5f, 1.0f);
        float maxScroll = this.maxScroll(itemCount, layout);
        this.scrollTarget = CosmeticsSection.clamp(this.scrollTarget, 0.0f, maxScroll);
        this.scrollValue = animation.animate("cosmetics:scroll:hats", this.scrollTarget, 26.0f, this.scrollValue);
        this.scrollValue = CosmeticsSection.clamp(this.scrollValue, 0.0f, maxScroll);
        if (itemCount == 0) {
            String empty = "\u041d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e";
            float tx = layout.listX + (layout.listW - RenderUtil.getTextWidth(empty, GFonts.MEDIUM, 7.0f)) * 0.5f;
            RenderUtil.drawText(context, empty, tx, layout.listY + layout.listH * 0.5f - 3.0f, muted, GFonts.MEDIUM, 7.0f);
        } else {
            context.method_44379((int)Math.floor(layout.listX + 1.0f), (int)Math.floor(layout.listY + 1.0f), (int)Math.ceil(layout.listX + layout.listW - 1.0f), (int)Math.ceil(layout.listY + layout.listH - 1.0f));
            float cardW = this.cardWidth(layout);
            float startX = layout.listX + 5.0f;
            float startY = layout.listY + 5.0f - this.scrollValue;
            for (int i = 0; i < itemCount; ++i) {
                int col = i % 3;
                int row = i / 3;
                float x = startX + (float)col * (cardW + 6.0f);
                float y = startY + (float)row * 78.0f;
                if (y + 72.0f < layout.listY || y > layout.listY + layout.listH) continue;
                this.drawHatCard(context, animation, manager, hats.get(i), x, y, cardW, mouseX, mouseY, alpha);
            }
            context.method_44380();
        }
        this.drawScrollbar(context, layout, itemCount, maxScroll, alpha);
    }

    private void renderModels(class_332 context, UiAnimationEngine animation, CosmeticsManager manager, Layout layout, int mouseX, int mouseY, float alpha) {
        int listBg = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha * 0.28f);
        int listBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha * 0.75f);
        RenderUtil.drawRoundRect(context, layout.listX, layout.listY, layout.listW, layout.listH, 4.0f, listBg);
        RenderUtil.drawBorder(context, layout.listX, layout.listY, layout.listW, layout.listH, 4.0f, listBorder, 0.0f, 0.5f, 1.0f);
        float cardW = (layout.listW - 16.0f) * 0.5f;
        this.drawModelCard(context, animation, "Chicken Gun", "C", manager.isChickenSelected(), ChickenMesh.isAvailable(), layout.listX + 5.0f, layout.listY + 5.0f, cardW, mouseX, mouseY, alpha);
        this.drawModelCard(context, animation, "Kirby", "K", manager.isKirbySelected(), KirbyMesh.isAvailable(), layout.listX + 11.0f + cardW, layout.listY + 5.0f, cardW, mouseX, mouseY, alpha);
    }

    private void drawModelCard(class_332 context, UiAnimationEngine animation, String name, String glyph, boolean selected, boolean available, float x, float y, float w, int mouseX, int mouseY, float alpha) {
        float hover = animation.animate01("cosmetics:model:" + name, CosmeticsSection.hovered(mouseX, mouseY, x, y, w, 72.0f) ? 1.0f : 0.0f, 12.0f, 0.0f);
        int bg = ColorUtil.interpolate(ClientColors.CATEGORY_BACKGROUND, ClientColors.SETTINGS_BACKGROUND, hover * 0.35f);
        RenderUtil.drawRoundRect(context, x, y, w, 72.0f, 5.0f, ColorUtil.multiplyAlpha(bg, alpha));
        RenderUtil.drawBorder(context, x, y, w, 72.0f, 5.0f, ColorUtil.multiplyAlpha(selected ? ClientColors.CATEGORY_ACCENT : ClientColors.SETTINGS_OUTLINE, alpha), 0.0f, selected ? 1.0f : 0.5f, 1.0f);
        RenderUtil.drawRoundRect(context, x + (w - 28.0f) * 0.5f, y + 6.0f, 28.0f, 28.0f, 8.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * (selected ? 0.55f : 0.25f)));
        float gx = x + (w - RenderUtil.getTextWidth(glyph, GFonts.MEDIUM, 14.0f)) * 0.5f;
        RenderUtil.drawText(context, glyph, gx, y + 13.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.MEDIUM, 14.0f);
        String label = this.fitText(name, w - 8.0f, 6.5f);
        RenderUtil.drawText(context, label, x + (w - RenderUtil.getTextWidth(label, GFonts.MEDIUM, 6.5f)) * 0.5f, y + 43.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.MEDIUM, 6.5f);
        String status = !available ? "\u041c\u043e\u0434\u0435\u043b\u044c \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430" : (selected ? "\u0412\u044b\u0431\u0440\u0430\u043d\u043e" : "\u0412\u044b\u0431\u0440\u0430\u0442\u044c");
        RenderUtil.drawText(context, status, x + (w - RenderUtil.getTextWidth(status, GFonts.MEDIUM, 5.0f)) * 0.5f, y + 58.0f, ColorUtil.multiplyAlpha(selected ? ClientColors.CATEGORY_ACCENT : ClientColors.CATEGORY_INACTIVE, alpha), GFonts.MEDIUM, 5.0f);
    }

    private void drawHatCard(class_332 context, UiAnimationEngine animation, CosmeticsManager manager, CosmeticHat hat, float x, float y, float w, int mouseX, int mouseY, float alpha) {
        boolean hover = CosmeticsSection.hovered(mouseX, mouseY, x, y, w, 72.0f);
        boolean selected = manager.isSelected(hat);
        float hoverAnim = animation.animate01("cosmetics:hat:" + hat.id() + ":hover", hover ? 1.0f : 0.0f, 12.0f, 0.0f);
        int base = ColorUtil.interpolate(ClientColors.CATEGORY_BACKGROUND, ClientColors.SETTINGS_BACKGROUND, hoverAnim * 0.35f);
        RenderUtil.drawRoundRect(context, x, y, w, 72.0f, 5.0f, ColorUtil.multiplyAlpha(base, alpha));
        RenderUtil.drawBorder(context, x, y, w, 72.0f, 5.0f, ColorUtil.multiplyAlpha(selected ? ClientColors.CATEGORY_ACCENT : ClientColors.SETTINGS_OUTLINE, alpha), 0.0f, selected ? 1.0f : 0.5f, 1.0f);
        class_1799 stack = manager.getStack(hat);
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        float scale = 2.0f;
        float itemX = x + w * 0.5f - 8.0f * scale;
        float itemY = y + 5.0f;
        matrices.translate(itemX, itemY);
        matrices.scale(scale, scale);
        context.method_51427(stack, 0, 0);
        matrices.popMatrix();
        int text = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        int muted = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha * 0.9f);
        String name = this.fitText(hat.name(), Math.max(1.0f, w - 8.0f), 5.5f);
        float nx = x + (w - RenderUtil.getTextWidth(name, GFonts.MEDIUM, 5.5f)) * 0.5f;
        RenderUtil.drawText(context, name, nx, y + 46.0f, text, GFonts.MEDIUM, 5.5f);
        String source = selected ? "\u0412\u044b\u0431\u0440\u0430\u043d\u043e" : "\u0412\u044b\u0431\u0440\u0430\u0442\u044c";
        int sourceColor = selected ? ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha) : muted;
        float sx = x + (w - RenderUtil.getTextWidth(source, GFonts.MEDIUM, 4.7f)) * 0.5f;
        RenderUtil.drawText(context, source, sx, y + 59.0f, sourceColor, GFonts.MEDIUM, 4.7f);
    }

    private void drawScrollbar(class_332 context, Layout layout, int itemCount, float maxScroll, float alpha) {
        float trackX = layout.listX + layout.listW - 7.0f;
        float trackY = layout.listY;
        float trackH = Math.max(1.0f, layout.listH);
        RenderUtil.drawRoundRect(context, trackX, trackY, 3.0f, trackH, 1.5f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha));
        if (maxScroll <= 0.0f) {
            return;
        }
        float contentH = this.contentHeight(itemCount);
        float thumbH = Math.max(24.0f, trackH * (layout.listH / Math.max(layout.listH, contentH)));
        thumbH = Math.min(trackH, thumbH);
        float thumbY = trackY + (trackH - thumbH) * (this.scrollValue / maxScroll);
        RenderUtil.drawRoundRect(context, trackX, thumbY, 3.0f, thumbH, 1.5f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha));
    }

    private void drawHelmetMode(class_332 context, UiAnimationEngine animation, CosmeticsManager manager, float x, float y, float w, float h, int mouseX, int mouseY, float alpha) {
        boolean onlyWithHelmet = manager.isOnlyWithHelmet();
        boolean hover = CosmeticsSection.hovered(mouseX, mouseY, x, y, w, h);
        float hoverAnim = animation.animate01("cosmetics:helmet-mode:hover", hover ? 1.0f : 0.0f, 12.0f, 0.0f);
        int bg = ColorUtil.interpolate(ClientColors.CATEGORY_BACKGROUND, ClientColors.CATEGORY_ACCENT, onlyWithHelmet ? 0.18f : hoverAnim * 0.14f);
        RenderUtil.drawRoundRect(context, x, y, w, h, 4.0f, ColorUtil.multiplyAlpha(bg, alpha));
        String label = onlyWithHelmet ? "\u0421\u043e \u0448\u043b\u0435\u043c\u043e\u043c" : "\u0412\u0441\u0435\u0433\u0434\u0430";
        int textColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        RenderUtil.drawText(context, label, x + 6.0f, y + 5.0f, textColor, GFonts.MEDIUM, 5.2f);
        float switchW = 14.0f;
        float switchH = 8.0f;
        float switchX = x + w - switchW - 5.0f;
        float switchY = y + (h - switchH) * 0.5f;
        int switchBg = ColorUtil.multiplyAlpha(onlyWithHelmet ? ClientColors.CATEGORY_ACCENT : ClientColors.SETTINGS_BACKGROUND, alpha);
        RenderUtil.drawRoundRect(context, switchX, switchY, switchW, switchH, 4.0f, switchBg);
        float knobX = switchX + (onlyWithHelmet ? 7.0f : 1.5f);
        RenderUtil.drawRoundRect(context, knobX, switchY + 1.5f, 5.0f, 5.0f, 2.5f, ColorUtil.multiplyAlpha(-723464, alpha));
    }

    private void drawButton(class_332 context, UiAnimationEngine animation, String key, String label, float x, float y, float w, float h, int mouseX, int mouseY, float alpha, boolean active) {
        boolean hover = CosmeticsSection.hovered(mouseX, mouseY, x, y, w, h);
        float hoverAnim = animation.animate01(key + ":hover", hover ? 1.0f : 0.0f, 12.0f, 0.0f);
        int bg = ColorUtil.interpolate(ClientColors.CATEGORY_BACKGROUND, ClientColors.CATEGORY_ACCENT, active ? 0.25f : hoverAnim * 0.22f);
        RenderUtil.drawRoundRect(context, x, y, w, h, 4.0f, ColorUtil.multiplyAlpha(bg, alpha));
        int color = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        float tx = x + (w - RenderUtil.getTextWidth(label, GFonts.MEDIUM, 5.5f)) * 0.5f;
        RenderUtil.drawText(context, label, tx, y + 5.0f, color, GFonts.MEDIUM, 5.5f);
    }

    public boolean handleClick(class_11909 click, float panelX, float panelY, float panelWidth, float panelHeight) {
        if (click.method_74245() != 0) {
            return false;
        }
        CosmeticsManager manager = CosmeticsManager.getInstance();
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        if (CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), layout.contentX, layout.filterY, 68.0f, 16.0f)) {
            this.page = Page.HATS;
            this.searchFocused = false;
            return true;
        }
        if (CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), layout.contentX + 73.0f, layout.filterY, 68.0f, 16.0f)) {
            this.page = Page.MODELS;
            this.draggingScrollbar = false;
            this.searchFocused = false;
            return true;
        }
        if (this.page == Page.MODELS) {
            float cardW = (layout.listW - 16.0f) * 0.5f;
            float y = layout.listY + 5.0f;
            if (CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), layout.listX + 5.0f, y, cardW, 72.0f)) {
                if (ChickenMesh.isAvailable()) {
                    manager.toggleChicken();
                }
                return true;
            }
            if (CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), layout.listX + 11.0f + cardW, y, cardW, 72.0f)) {
                if (KirbyMesh.isAvailable()) {
                    manager.toggleKirby();
                }
                return true;
            }
            return CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), layout.listX, layout.listY, layout.listW, layout.listH);
        }
        if (CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), layout.clearX, layout.titleY - 3.0f, layout.clearW, 16.0f)) {
            manager.clearSelection();
            this.searchFocused = false;
            return true;
        }
        if (CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), layout.optionX, layout.filterY - 1.0f, layout.optionW, 16.0f)) {
            manager.toggleHelmetMode();
            this.searchFocused = false;
            return true;
        }
        if (CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), layout.searchX, layout.searchY, layout.searchW, 14.0f)) {
            this.searchFocused = true;
            return true;
        }
        this.searchFocused = false;
        List<CosmeticHat> hats = this.filteredHats(manager);
        int itemCount = hats.size();
        float maxScroll = this.maxScroll(itemCount, layout);
        if (maxScroll > 0.0f) {
            float trackX = layout.listX + layout.listW - 7.0f;
            float trackY = layout.listY;
            float trackH = Math.max(1.0f, layout.listH);
            float contentH = this.contentHeight(itemCount);
            float thumbH = Math.min(trackH, Math.max(24.0f, trackH * (layout.listH / Math.max(layout.listH, contentH))));
            float thumbY = trackY + (trackH - thumbH) * (this.scrollValue / maxScroll);
            if (CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), trackX - 2.0f, thumbY, 7.0f, thumbH)) {
                this.draggingScrollbar = true;
                this.scrollbarGrabOffset = (float)click.comp_4799() - thumbY;
                return true;
            }
            if (CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), trackX - 2.0f, trackY, 7.0f, trackH)) {
                this.draggingScrollbar = true;
                this.scrollbarGrabOffset = thumbH * 0.5f;
                this.updateScrollFromMouse((float)click.comp_4799(), layout, itemCount, maxScroll);
                return true;
            }
        }
        if (!CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), layout.listX, layout.listY, layout.listW - 9.0f, layout.listH)) {
            return false;
        }
        float cardW = this.cardWidth(layout);
        float startX = layout.listX + 5.0f;
        float startY = layout.listY + 5.0f - this.scrollValue;
        for (int i = 0; i < itemCount; ++i) {
            int col = i % 3;
            int row = i / 3;
            float x = startX + (float)col * (cardW + 6.0f);
            float y = startY + (float)row * 78.0f;
            if (y + 72.0f < layout.listY || y > layout.listY + layout.listH || !CosmeticsSection.hovered(click.comp_4798(), click.comp_4799(), x, y, cardW, 72.0f)) continue;
            manager.select(hats.get(i));
            return true;
        }
        return true;
    }

    public boolean handleScroll(double mouseX, double mouseY, double amount, float panelX, float panelY, float panelWidth, float panelHeight) {
        if (this.page == Page.MODELS) {
            return false;
        }
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        if (!CosmeticsSection.hovered(mouseX, mouseY, layout.listX, layout.listY, layout.listW, layout.listH)) {
            return false;
        }
        int itemCount = this.currentItemCount();
        float maxScroll = this.maxScroll(itemCount, layout);
        this.scrollTarget = CosmeticsSection.clamp(this.scrollTarget - (float)amount * 31.0f, 0.0f, maxScroll);
        return true;
    }

    public boolean handleMouseDragged(double mouseX, double mouseY, int button, float panelX, float panelY, float panelWidth, float panelHeight) {
        if (this.page == Page.MODELS) {
            return false;
        }
        if (button != 0 || !this.draggingScrollbar) {
            return false;
        }
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        int itemCount = this.currentItemCount();
        float maxScroll = this.maxScroll(itemCount, layout);
        this.updateScrollFromMouse((float)mouseY, layout, itemCount, maxScroll);
        return true;
    }

    public boolean handleMouseReleased(int button) {
        if (button == 0 && this.draggingScrollbar) {
            this.draggingScrollbar = false;
            return true;
        }
        return false;
    }

    public boolean handleKeyPressed(class_11908 key) {
        if (this.page == Page.MODELS) {
            return false;
        }
        if (!this.searchFocused) {
            return false;
        }
        int code = key.comp_4795();
        if (code == 256 || code == 257 || code == 335) {
            this.searchFocused = false;
            return true;
        }
        if (code == 259) {
            if (!this.searchText.isEmpty()) {
                int previous = this.searchText.offsetByCodePoints(this.searchText.length(), -1);
                this.searchText = this.searchText.substring(0, previous);
                this.resetScroll();
            }
            return true;
        }
        if (code == 261) {
            this.searchText = "";
            this.resetScroll();
            return true;
        }
        if (this.isCtrlDown() && code == 65) {
            this.searchText = "";
            this.resetScroll();
            return true;
        }
        return true;
    }

    public boolean handleCharTyped(int codePoint) {
        if (this.page == Page.MODELS) {
            return false;
        }
        if (!this.searchFocused || !Character.isValidCodePoint(codePoint) || codePoint < 32 || codePoint == 127) {
            return false;
        }
        this.searchText = this.searchText + new String(Character.toChars(codePoint));
        this.resetScroll();
        return true;
    }

    private int currentItemCount() {
        return this.filteredHats(CosmeticsManager.getInstance()).size();
    }

    private void updateScrollFromMouse(float mouseY, Layout layout, int itemCount, float maxScroll) {
        if (maxScroll <= 0.0f) {
            this.scrollTarget = 0.0f;
            this.scrollValue = 0.0f;
            return;
        }
        float trackY = layout.listY;
        float trackH = Math.max(1.0f, layout.listH);
        float contentH = this.contentHeight(itemCount);
        float thumbH = Math.min(trackH, Math.max(24.0f, trackH * (layout.listH / Math.max(layout.listH, contentH))));
        float travel = Math.max(1.0f, trackH - thumbH);
        float thumbY = CosmeticsSection.clamp(mouseY - this.scrollbarGrabOffset, trackY, trackY + travel);
        float ratio = (thumbY - trackY) / travel;
        this.scrollValue = this.scrollTarget = ratio * maxScroll;
    }

    private List<CosmeticHat> filteredHats(CosmeticsManager manager) {
        String query = this.searchText.trim().toLowerCase(Locale.ROOT);
        ArrayList<CosmeticHat> result = new ArrayList<CosmeticHat>();
        for (CosmeticHat hat : manager.getHats()) {
            String haystack;
            if (!query.isEmpty() && !(haystack = (hat.name() + " " + hat.id() + " " + hat.source().label()).toLowerCase(Locale.ROOT)).contains(query)) continue;
            result.add(hat);
        }
        return result;
    }

    private float contentHeight(int count) {
        if (count <= 0) {
            return 0.0f;
        }
        int rows = (count + 3 - 1) / 3;
        return 10.0f + (float)rows * 72.0f + (float)Math.max(0, rows - 1) * 6.0f;
    }

    private float maxScroll(int itemCount, Layout layout) {
        return Math.max(0.0f, this.contentHeight(itemCount) - layout.listH);
    }

    private float cardWidth(Layout layout) {
        float innerW = layout.listW - 18.0f;
        return (innerW - 12.0f) / 3.0f;
    }

    private String fitText(String text, float maxWidth, float size) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (RenderUtil.getTextWidth(text, GFonts.MEDIUM, size) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        int end = text.length();
        while (end > 0) {
            int prev = text.offsetByCodePoints(end, -1);
            String candidate = text.substring(0, prev) + suffix;
            if (RenderUtil.getTextWidth(candidate, GFonts.MEDIUM, size) <= maxWidth) {
                return candidate;
            }
            end = prev;
        }
        return suffix;
    }

    private void resetScroll() {
        this.scrollTarget = 0.0f;
        this.scrollValue = 0.0f;
    }

    private boolean isCtrlDown() {
        long window = class_310.method_1551().method_22683().method_4490();
        return GLFW.glfwGetKey((long)window, (int)341) == 1 || GLFW.glfwGetKey((long)window, (int)345) == 1;
    }

    private Layout layout(float panelX, float panelY, float panelWidth, float panelHeight) {
        float contentX = panelX + 10.0f;
        float contentW = panelWidth - 20.0f;
        float titleY = panelY + 13.0f;
        float clearW = 62.0f;
        float clearX = contentX + contentW - clearW;
        float optionW = 82.0f;
        float optionX = contentX + contentW - optionW;
        float filterY = panelY + 34.0f;
        float searchX = contentX;
        float searchY = panelY + 53.0f;
        float searchW = contentW;
        float listX = contentX;
        float listY = panelY + 73.0f;
        float listW = contentW;
        float listH = Math.max(40.0f, panelHeight - 81.0f);
        return new Layout(contentX, contentW, titleY, clearX, clearW, optionX, optionW, filterY, searchX, searchY, searchW, listX, listY, listW, listH);
    }

    private static boolean hovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static enum Page {
        HATS,
        MODELS;

    }

    private record Layout(float contentX, float contentW, float titleY, float clearX, float clearW, float optionX, float optionW, float filterY, float searchX, float searchY, float searchW, float listX, float listY, float listW, float listH) {
    }
}

