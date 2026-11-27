/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  org.lwjgl.glfw.GLFW
 */
package pyz1k.screen.menu.sections;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import pyz1k.compat.LegacyInput;
import pyz1k.Pyz1k;
import pyz1k.abilitybind.AbilityBindManager;
import pyz1k.config.ConfigManager;
import pyz1k.cosmetics.CosmeticsManager;
import pyz1k.handrender.HandRenderManager;
import pyz1k.itemrender.ItemRenderManager;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class ConfigsSection {
    private static final float CONTENT_PAD_X = 12.0f;
    private static final float CONTENT_TOP = 14.0f;
    private static final float CONTENT_BOTTOM_PAD = 10.0f;
    private static final float HEADER_H = 16.0f;
    private static final float TOP_BUTTON_SIZE = 14.0f;
    private static final float TOP_BUTTON_GAP = 6.0f;
    private static final float LIST_GAP_TOP = 8.0f;
    private static final float ENTRY_H = 24.0f;
    private static final float ENTRY_GAP = 6.0f;
    private static final float ENTRY_PAD_X = 6.0f;
    private static final float ROW_BUTTON_W = 28.0f;
    private static final float ROW_BUTTON_H = 12.0f;
    private static final float ROW_BUTTON_GAP = 4.0f;
    private static final float LIST_SCROLL_SPEED = 26.0f;
    private static final float LIST_SCROLL_STEP = 22.0f;
    private String selectedConfigName;
    private float scrollTarget;
    private float scrollValue;
    private boolean active;
    private String editingConfigName;
    private String editingText;
    private int caretIndex;
    private int selectionIndex;
    private boolean selectingWithMouse;

    public void setActive(boolean active) {
        this.active = active;
    }

    public void render(GuiGraphicsExtractor context, UiAnimationEngine animation, float panelX, float panelY, float panelWidth, float panelHeight, int mouseX, int mouseY, float alpha) {
        float show;
        ConfigManager manager = Pyz1k.getInstance().getConfigManager();
        List<ConfigManager.ConfigEntry> entries = manager.listConfigEntries();
        if ((this.selectedConfigName == null || this.selectedConfigName.isBlank()) && !entries.isEmpty()) {
            this.selectedConfigName = manager.getLoadedConfigName();
        }
        if (this.editingConfigName != null && entries.stream().noneMatch(e -> e.name().equalsIgnoreCase(this.editingConfigName))) {
            this.cancelEditing();
        }
        if (!((show = animation.animate01("cfg:manager:show", this.active ? 1.0f : 0.0f, 16.0f, this.active ? 1.0f : 0.0f)) <= 0.001f)) {
            float drawAlpha = alpha * show;
            float yOffset = (1.0f - show) * 8.0f;
            Layout layout = this.layout(panelX, panelY + yOffset, panelWidth, panelHeight);
            int titleColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, drawAlpha);
            int subColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, drawAlpha * 0.95f);
            RenderUtil.drawText(context, "Configs", layout.contentX, layout.headerY + 2.0f, titleColor, GFonts.MEDIUM, 9);
            RenderUtil.drawText(context, "Loaded: " + manager.getLoadedConfigName(), layout.contentX + 58.0f, layout.headerY + 2.2f, subColor, GFonts.MEDIUM, 7);
            this.renderTopButton(context, animation, "cfg:create", "textures/gui/icons/create.png", layout.createBtnX, layout.headerY, mouseX, mouseY, drawAlpha);
            this.renderTopButton(context, animation, "cfg:reload", "textures/gui/icons/reload.png", layout.reloadBtnX, layout.headerY, mouseX, mouseY, drawAlpha);
            this.renderTopButton(context, animation, "cfg:folder", "textures/gui/icons/folder.png", layout.folderBtnX, layout.headerY, mouseX, mouseY, drawAlpha);
            float contentHeight = entries.isEmpty() ? 0.0f : (float)entries.size() * 24.0f + (float)Math.max(0, entries.size() - 1) * 6.0f;
            float maxScroll = Math.max(0.0f, contentHeight - layout.listH);
            this.scrollTarget = this.clamp(this.scrollTarget, 0.0f, maxScroll);
            this.scrollValue = animation.animate("cfg:list:scroll", this.scrollTarget, 26.0f, this.scrollValue);
            this.scrollValue = this.clamp(this.scrollValue, 0.0f, maxScroll);
            int listBg = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, drawAlpha * 0.5f);
            int listBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, drawAlpha * 0.95f);
            RenderUtil.drawRoundRect(context, layout.listX, layout.listY, layout.listW, layout.listH, 4.0f, listBg);
            RenderUtil.drawBorder(context, layout.listX, layout.listY, layout.listW, layout.listH, 4.0f, listBorder, 0.0f, 0.5f, 1.0f);
            int scX1 = (int)Math.floor(layout.listX + 1.0f);
            int scY1 = (int)Math.floor(layout.listY + 1.0f);
            int scX2 = (int)Math.ceil(layout.listX + layout.listW - 1.0f);
            int scY2 = (int)Math.ceil(layout.listY + layout.listH - 1.0f);
            context.enableScissor(scX1, scY1, scX2, scY2);
            for (int i = 0; i < entries.size(); ++i) {
                ConfigManager.ConfigEntry entry = entries.get(i);
                EntryLayout row = this.rowLayout(layout, i, this.scrollValue);
                if (row.y + row.h < layout.listY || row.y > layout.listY + layout.listH) continue;
                this.drawEntry(context, animation, manager, entry, row, mouseX, mouseY, drawAlpha);
            }
            context.disableScissor();
        }
    }

    public boolean handleClick(MouseButtonEvent click, float panelX, float panelY, float panelWidth, float panelHeight) {
        if (pyz1k.compat.LegacyInput.fromNativeMouse(click.button()) != 0) {
            return false;
        }
        ConfigManager manager = Pyz1k.getInstance().getConfigManager();
        List<ConfigManager.ConfigEntry> entries = manager.listConfigEntries();
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        if (this.hitTopButton(layout.createBtnX, layout.headerY, click)) {
            this.commitEditingIfNeeded();
            String created = manager.createDefaultConfig();
            if (created != null) {
                this.selectedConfigName = created;
            }
            return true;
        }
        if (this.hitTopButton(layout.reloadBtnX, layout.headerY, click)) {
            this.commitEditingIfNeeded();
            manager.reload();
            ItemRenderManager.getInstance().reloadFromDisk();
            HandRenderManager.getInstance().reloadFromDisk();
            AbilityBindManager.getInstance().reloadFromDisk();
            CosmeticsManager.getInstance().reloadFromDisk();
            return true;
        }
        if (this.hitTopButton(layout.folderBtnX, layout.headerY, click)) {
            this.commitEditingIfNeeded();
            manager.openConfigFolder();
            return true;
        }
        if (!this.isHovered(click.x(), click.y(), layout.listX, layout.listY, layout.listW, layout.listH)) {
            this.commitEditingIfNeeded();
            return false;
        }
        for (int i = 0; i < entries.size(); ++i) {
            int index;
            ConfigManager.ConfigEntry entry = entries.get(i);
            EntryLayout row = this.rowLayout(layout, i, this.scrollValue);
            if (!this.isHovered(click.x(), click.y(), row.x, row.y, row.w, row.h)) continue;
            this.selectedConfigName = entry.name();
            if (this.isHovered(click.x(), click.y(), row.loadX, row.btnY, 28.0f, 12.0f)) {
                this.commitEditingIfNeeded();
                manager.loadConfig(entry.name());
                return true;
            }
            if (this.isHovered(click.x(), click.y(), row.saveX, row.btnY, 28.0f, 12.0f)) {
                this.commitEditingIfNeeded();
                manager.saveConfig(entry.name());
                return true;
            }
            if (this.isHovered(click.x(), click.y(), row.deleteX, row.btnY, 28.0f, 12.0f)) {
                this.commitEditingIfNeeded();
                if (!entry.name().equalsIgnoreCase("auto")) {
                    manager.deleteConfig(entry.name());
                    if (entry.name().equalsIgnoreCase(this.selectedConfigName)) {
                        this.selectedConfigName = manager.getLoadedConfigName();
                    }
                }
                return true;
            }
            if (this.editingConfigName == null || !this.editingConfigName.equalsIgnoreCase(entry.name())) {
                this.startEditing(entry.name());
            }
            this.caretIndex = index = this.indexFromMouseX(this.editingText, row.nameTextX, (float)click.x());
            this.selectionIndex = index;
            this.selectingWithMouse = true;
            return true;
        }
        return true;
    }

    public boolean handleMouseDragged(double mouseX, double mouseY, int button, float panelX, float panelY, float panelWidth, float panelHeight) {
        if (this.selectingWithMouse && button == 0 && this.editingConfigName != null) {
            ConfigManager manager = Pyz1k.getInstance().getConfigManager();
            List<ConfigManager.ConfigEntry> entries = manager.listConfigEntries();
            Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
            for (int i = 0; i < entries.size(); ++i) {
                ConfigManager.ConfigEntry entry = entries.get(i);
                if (!entry.name().equalsIgnoreCase(this.editingConfigName)) continue;
                EntryLayout row = this.rowLayout(layout, i, this.scrollValue);
                this.caretIndex = this.indexFromMouseX(this.editingText, row.nameTextX, (float)mouseX);
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean handleMouseReleased(int button) {
        if (button == 0 && this.selectingWithMouse) {
            this.selectingWithMouse = false;
            return true;
        }
        return false;
    }

    public boolean handleKeyPressed(KeyEvent key) {
        if (this.editingConfigName == null) {
            return false;
        }
        int code = pyz1k.compat.LegacyInput.fromNativeKey(key.key());
        boolean ctrl = this.isCtrlDown();
        boolean shift = this.isShiftDown();
        if (code == 256) {
            this.cancelEditing();
            return true;
        }
        if (code == 257 || code == 335) {
            this.commitEditingIfNeeded();
            return true;
        }
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
            if (!this.isEditingAuto() && this.hasSelection()) {
                this.copySelection();
                this.deleteSelection();
            }
            return true;
        }
        if (ctrl && code == 86) {
            String clip;
            if (!this.isEditingAuto() && (clip = this.getClipboard()) != null && !clip.isEmpty()) {
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
            if (!this.isEditingAuto() && !this.deleteSelection() && this.caretIndex > 0) {
                this.editingText = this.editingText.substring(0, this.caretIndex - 1) + this.editingText.substring(this.caretIndex);
                --this.caretIndex;
                this.selectionIndex = this.caretIndex;
            }
            return true;
        }
        if (code == 261) {
            if (!this.isEditingAuto() && !this.deleteSelection() && this.caretIndex < this.editingText.length()) {
                this.editingText = this.editingText.substring(0, this.caretIndex) + this.editingText.substring(this.caretIndex + 1);
                this.selectionIndex = this.caretIndex;
            }
            return true;
        }
        return false;
    }

    public boolean handleCharTyped(int codePoint) {
        if (this.editingConfigName == null || this.isEditingAuto()) {
            return false;
        }
        if (!Character.isValidCodePoint(codePoint)) {
            return false;
        }
        char chr = (char)codePoint;
        if (chr >= ' ' && chr != '\u007f') {
            this.insertText(String.valueOf(chr));
            return true;
        }
        return false;
    }

    public boolean handleScroll(double mouseX, double mouseY, double verticalAmount, float panelX, float panelY, float panelWidth, float panelHeight) {
        if (verticalAmount == 0.0) {
            return false;
        }
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        if (!this.isHovered(mouseX, mouseY, layout.listX, layout.listY, layout.listW, layout.listH)) {
            return false;
        }
        List<ConfigManager.ConfigEntry> entries = Pyz1k.getInstance().getConfigManager().listConfigEntries();
        float contentHeight = entries.isEmpty() ? 0.0f : (float)entries.size() * 24.0f + (float)Math.max(0, entries.size() - 1) * 6.0f;
        float maxScroll = Math.max(0.0f, contentHeight - layout.listH);
        this.scrollTarget = this.clamp(this.scrollTarget - (float)verticalAmount * 22.0f, 0.0f, maxScroll);
        return true;
    }

    public void applyUiState(String selectedConfig, float configsScroll) {
        this.selectedConfigName = selectedConfig;
        this.scrollValue = this.scrollTarget = Math.max(0.0f, configsScroll);
    }

    public String getSelectedConfigName() {
        return this.selectedConfigName;
    }

    public float getScrollTarget() {
        return this.scrollTarget;
    }

    private void drawEntry(GuiGraphicsExtractor context, UiAnimationEngine animation, ConfigManager manager, ConfigManager.ConfigEntry entry, EntryLayout row, int mouseX, int mouseY, float alpha) {
        float hover;
        boolean loaded = entry.name().equalsIgnoreCase(manager.getLoadedConfigName());
        boolean selected = entry.name().equalsIgnoreCase(this.selectedConfigName);
        boolean hovered = this.isHovered(mouseX, mouseY, row.x, row.y, row.w, row.h);
        int rowBg = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * ((selected ? 0.18f : 0.0f) + (hover = animation.animate01("cfg:row:hover:" + entry.name(), hovered ? 1.0f : 0.0f, 14.0f, 0.0f)) * 0.1f));
        if (ColorUtil.alpha(rowBg) > 0) {
            RenderUtil.drawRoundRect(context, row.x, row.y, row.w, row.h, 3.5f, rowBg);
        }
        int rowBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha * 0.65f);
        RenderUtil.drawBorder(context, row.x, row.y, row.w, row.h, 3.5f, rowBorder, 0.0f, 0.5f, 1.0f);
        this.renderRowButton(context, animation, "cfg:row:load:" + entry.name(), "Load", row.loadX, row.btnY, mouseX, mouseY, alpha);
        this.renderRowButton(context, animation, "cfg:row:save:" + entry.name(), "Save", row.saveX, row.btnY, mouseX, mouseY, alpha);
        boolean canDelete = !entry.name().equalsIgnoreCase("auto");
        this.renderRowButton(context, animation, "cfg:row:del:" + entry.name(), canDelete ? "Delete" : "-", row.deleteX, row.btnY, mouseX, mouseY, alpha);
        boolean editingThis = this.editingConfigName != null && this.editingConfigName.equalsIgnoreCase(entry.name());
        int textColor = ColorUtil.multiplyAlpha(loaded ? ClientColors.TEXT_PRIMARY : ClientColors.CATEGORY_INACTIVE, alpha);
        float textY = row.y + 8.0f;
        if (editingThis) {
            this.drawEditingText(context, animation, row, textY, textColor, alpha);
        } else {
            RenderUtil.drawTextFadeRight(context, entry.name(), row.nameTextX, textY, textColor, GFonts.MEDIUM, 8, row.nameTextW, 4.0f);
        }
    }

    private void drawEditingText(GuiGraphicsExtractor context, UiAnimationEngine animation, EntryLayout row, float textY, int textColor, float alpha) {
        int selStart = Math.min(this.caretIndex, this.selectionIndex);
        int selEnd = Math.max(this.caretIndex, this.selectionIndex);
        if (selEnd > selStart) {
            String before = this.editingText.substring(0, selStart);
            String mid = this.editingText.substring(selStart, selEnd);
            float startX = row.nameTextX + RenderUtil.getTextWidth(before, GFonts.MEDIUM, 8);
            float selW = RenderUtil.getTextWidth(mid, GFonts.MEDIUM, 8);
            int selColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * 0.38f);
            RenderUtil.drawRoundRect(context, startX - 0.5f, row.y + 4.0f, selW + 1.0f, 11.0f, 2.0f, selColor);
        }
        RenderUtil.drawTextFadeRight(context, this.editingText, row.nameTextX, textY, textColor, GFonts.MEDIUM, 8, row.nameTextW, 4.0f);
        float caretBlink = animation.animate01("cfg:caret:blink", 1.0f, 10.0f, 0.0f);
        int caretColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha * (0.35f + 0.65f * caretBlink));
        float caretX = row.nameTextX + RenderUtil.getTextWidth(this.editingText.substring(0, this.clampInt(this.caretIndex, 0, this.editingText.length())), GFonts.MEDIUM, 8);
        RenderUtil.drawRoundRect(context, caretX, row.y + 4.0f, 1.0f, 11.0f, 0.5f, caretColor);
    }

    private void renderTopButton(GuiGraphicsExtractor context, UiAnimationEngine animation, String key, String iconPath, float x, float y, int mouseX, int mouseY, float alpha) {
        boolean hovered = this.isHovered(mouseX, mouseY, x, y, 14.0f, 14.0f);
        float hover = animation.animate01("cfg:topbtn:hover:" + key, hovered ? 1.0f : 0.0f, 14.0f, 0.0f);
        int bg = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha * (0.6f + hover * 0.24f));
        int border = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha * 0.95f);
        int icon = ColorUtil.multiplyAlpha(ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, hover * 0.8f), alpha);
        RenderUtil.drawRoundRect(context, x, y, 14.0f, 14.0f, 3.0f, bg);
        RenderUtil.drawBorder(context, x, y, 14.0f, 14.0f, 3.0f, border, 0.0f, 0.5f, 1.0f);
        RenderUtil.drawImage(context, iconPath, x + 3.0f, y + 3.0f, 8.0f, 8.0f, 0.0f, icon, 1.0f);
    }

    private void renderRowButton(GuiGraphicsExtractor context, UiAnimationEngine animation, String key, String label, float x, float y, int mouseX, int mouseY, float alpha) {
        boolean hovered = this.isHovered(mouseX, mouseY, x, y, 28.0f, 12.0f);
        float hover = animation.animate01("cfg:rowbtn:hover:" + key, hovered ? 1.0f : 0.0f, 14.0f, 0.0f);
        int bg = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha * (0.62f + hover * 0.18f));
        int border = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha * 0.92f);
        int text = ColorUtil.multiplyAlpha(ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, hover * 0.75f), alpha);
        RenderUtil.drawRoundRect(context, x, y, 28.0f, 12.0f, 2.5f, bg);
        RenderUtil.drawBorder(context, x, y, 28.0f, 12.0f, 2.5f, border, 0.0f, 0.5f, 1.0f);
        float tx = x + (28.0f - RenderUtil.getTextWidth(label, GFonts.MEDIUM, 6)) * 0.5f;
        RenderUtil.drawText(context, label, tx, y + 4.0f, text, GFonts.MEDIUM, 6);
    }

    private void startEditing(String name) {
        this.commitEditingIfNeeded();
        this.editingConfigName = name;
        this.editingText = name;
        this.selectionIndex = this.caretIndex = this.editingText.length();
    }

    private void commitEditingIfNeeded() {
        if (this.editingConfigName != null) {
            boolean renamed;
            String to;
            String from = this.editingConfigName;
            String string = to = this.editingText == null ? "" : this.editingText.trim();
            this.selectedConfigName = !from.equalsIgnoreCase(to) && !from.equalsIgnoreCase("auto") ? ((renamed = Pyz1k.getInstance().getConfigManager().renameConfig(from, to)) ? to : from) : from;
            this.cancelEditing();
        }
    }

    private void cancelEditing() {
        this.editingConfigName = null;
        this.editingText = null;
        this.caretIndex = 0;
        this.selectionIndex = 0;
        this.selectingWithMouse = false;
    }

    private boolean isEditingAuto() {
        return this.editingConfigName != null && this.editingConfigName.equalsIgnoreCase("auto");
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
        return true;
    }

    private void insertText(String text) {
        if (text != null && !text.isEmpty()) {
            this.deleteSelection();
            String safe = text.replaceAll("[\\r\\n\\t]", " ");
            this.editingText = this.editingText.substring(0, this.caretIndex) + safe + this.editingText.substring(this.caretIndex);
            this.caretIndex += safe.length();
            this.selectionIndex = this.caretIndex;
        }
    }

    private void moveCaret(int delta, boolean selecting) {
        int next = this.clampInt(this.caretIndex + delta, 0, this.editingText.length());
        this.setCaret(next, selecting);
    }

    private void setCaret(int next, boolean selecting) {
        this.caretIndex = this.clampInt(next, 0, this.editingText.length());
        if (!selecting) {
            this.selectionIndex = this.caretIndex;
        }
    }

    private void copySelection() {
        if (this.hasSelection()) {
            int start = Math.min(this.caretIndex, this.selectionIndex);
            int end = Math.max(this.caretIndex, this.selectionIndex);
            this.setClipboard(this.editingText.substring(start, end));
        }
    }

    private int indexFromMouseX(String text, float textStartX, float mouseX) {
        if (text != null && !text.isEmpty() && !(mouseX <= textStartX)) {
            float localX = mouseX - textStartX;
            int best = text.length();
            for (int i = 1; i <= text.length(); ++i) {
                float w = RenderUtil.getTextWidth(text.substring(0, i), GFonts.MEDIUM, 8);
                if (!(localX < w)) continue;
                best = i - 1;
                float prevW = RenderUtil.getTextWidth(text.substring(0, best), GFonts.MEDIUM, 8);
                if (Math.abs(localX - w) < Math.abs(localX - prevW)) {
                    best = i;
                }
                return this.clampInt(best, 0, text.length());
            }
            return text.length();
        }
        return 0;
    }

    private boolean isCtrlDown() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getWindow() != null) {
            long handle = mc.getWindow().handle();
            return LegacyInput.glfwGetKey((long)handle, (int)341) == 1 || LegacyInput.glfwGetKey((long)handle, (int)345) == 1;
        }
        return false;
    }

    private boolean isShiftDown() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getWindow() != null) {
            long handle = mc.getWindow().handle();
            return LegacyInput.glfwGetKey((long)handle, (int)340) == 1 || LegacyInput.glfwGetKey((long)handle, (int)344) == 1;
        }
        return false;
    }

    private String getClipboard() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.keyboardHandler != null) {
            String value = mc.keyboardHandler.getClipboard();
            return value == null ? "" : value;
        }
        return "";
    }

    private void setClipboard(String value) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.keyboardHandler != null) {
            mc.keyboardHandler.setClipboard(value == null ? "" : value);
        }
    }

    private boolean hitTopButton(float x, float y, MouseButtonEvent click) {
        return this.isHovered(click.x(), click.y(), x, y, 14.0f, 14.0f);
    }

    private EntryLayout rowLayout(Layout layout, int index, float currentScroll) {
        float y = layout.listY + (float)index * 30.0f - currentScroll;
        float rowX = layout.listX + 2.0f;
        float rowW = layout.listW - 4.0f;
        float buttonsBlockW = 92.0f;
        float deleteX = rowX + rowW - 6.0f - 28.0f;
        float saveX = deleteX - 4.0f - 28.0f;
        float loadX = saveX - 4.0f - 28.0f;
        float btnY = y + 6.0f;
        float nameX = rowX + 6.0f;
        float nameW = Math.max(16.0f, loadX - 8.0f - nameX);
        return new EntryLayout(rowX, y, rowW, 24.0f, nameX, nameW, loadX, saveX, deleteX, btnY);
    }

    private Layout layout(float panelX, float panelY, float panelWidth, float panelHeight) {
        float contentX = panelX + 12.0f;
        float contentW = panelWidth - 24.0f;
        float headerY = panelY + 14.0f;
        float topButtonsW = 54.0f;
        float btnStartX = contentX + contentW - topButtonsW;
        float listX = contentX;
        float listY = headerY + 16.0f + 8.0f;
        float listW = contentW;
        float listH = Math.max(24.0f, panelY + panelHeight - 10.0f - listY);
        return new Layout(contentX, headerY, listX, listY, listW, listH, btnStartX, btnStartX + 14.0f + 6.0f, btnStartX + 40.0f);
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean isHovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    private record Layout(float contentX, float headerY, float listX, float listY, float listW, float listH, float createBtnX, float reloadBtnX, float folderBtnX) {
    }

    private record EntryLayout(float x, float y, float w, float h, float nameTextX, float nameTextW, float loadX, float saveX, float deleteX, float btnY) {
    }
}

