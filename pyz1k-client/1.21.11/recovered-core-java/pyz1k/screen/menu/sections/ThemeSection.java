/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_332
 */
package pyz1k.screen.menu.sections;

import java.util.List;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_332;
import pyz1k.config.ThemeManager;
import pyz1k.module.setting.ColorSetting;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.screen.menu.sections.ModuleGridSection;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ClientTheme;
import pyz1k.utilites.color.ColorUtil;

public final class ThemeSection {
    private static final float PAD = 12.0f;
    private static final float TOP = 14.0f;
    private static final float HEADER_H = 16.0f;
    private static final float COLUMN_GAP = 8.0f;
    private static final float LEFT_W = 108.0f;
    private static final float ROW_H = 36.0f;
    private static final float ROW_GAP = 5.0f;
    private static final float EDITOR_ROW_H = 23.0f;
    private static final float EDITOR_ROW_GAP = 4.0f;
    private static final float SCROLL_STEP = 24.0f;
    private final ThemeManager themeManager = new ThemeManager();
    private String selectedTheme;
    private float presetScrollTarget;
    private float presetScroll;
    private float editorScrollTarget;
    private float editorScroll;
    private String editingThemeName;
    private String editingText;
    private int caretIndex;

    public void render(class_332 context, UiAnimationEngine animation, float panelX, float panelY, float panelWidth, float panelHeight, int mouseX, int mouseY, float alpha) {
        ClientTheme.updateDynamicColors();
        if (this.selectedTheme == null || this.selectedTheme.isBlank()) {
            this.selectedTheme = ClientTheme.getActiveThemeName();
        }
        List<ThemeManager.ThemeEntry> themes = this.themeManager.listThemes();
        List<ColorSetting> settings = ClientTheme.getEditableSettings();
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        RenderUtil.drawText(context, "\u0422\u0435\u043c\u044b", layout.contentX, layout.headerY + 2.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.MEDIUM, 9.0f);
        String activeText = "\u0410\u043a\u0442\u0438\u0432\u043d\u0430: " + this.displayThemeName(ClientTheme.getActiveThemeName());
        RenderUtil.drawTextFadeRight(context, activeText, layout.contentX + 48.0f, layout.headerY + 3.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha), GFonts.MEDIUM, 6.0f, Math.max(24.0f, layout.createX - layout.contentX - 54.0f), 4.0f);
        this.renderTopButton(context, animation, "theme:create", "textures/gui/icons/create.png", layout.createX, layout.headerY, mouseX, mouseY, alpha);
        this.renderTopButton(context, animation, "theme:reload", "textures/gui/icons/reload.png", layout.reloadX, layout.headerY, mouseX, mouseY, alpha);
        this.renderTopButton(context, animation, "theme:folder", "textures/gui/icons/folder.png", layout.folderX, layout.headerY, mouseX, mouseY, alpha);
        this.drawContainer(context, layout.leftX, layout.bodyY, layout.leftW, layout.bodyH, alpha);
        this.drawContainer(context, layout.rightX, layout.bodyY, layout.rightW, layout.bodyH, alpha);
        float presetContentH = themes.isEmpty() ? 0.0f : (float)themes.size() * 36.0f + (float)(themes.size() - 1) * 5.0f;
        float presetMaxScroll = Math.max(0.0f, presetContentH - (layout.bodyH - 8.0f));
        this.presetScrollTarget = this.clamp(this.presetScrollTarget, 0.0f, presetMaxScroll);
        this.presetScroll = animation.animate("theme:preset:scroll", this.presetScrollTarget, 26.0f, this.presetScroll);
        context.method_44379((int)Math.floor(layout.leftX + 1.0f), (int)Math.floor(layout.bodyY + 1.0f), (int)Math.ceil(layout.leftX + layout.leftW - 1.0f), (int)Math.ceil(layout.bodyY + layout.bodyH - 1.0f));
        for (int i = 0; i < themes.size(); ++i) {
            ThemeManager.ThemeEntry entry = themes.get(i);
            ThemeRow row = this.themeRow(layout, i, this.presetScroll);
            if (!(row.y + row.h >= layout.bodyY) || !(row.y <= layout.bodyY + layout.bodyH)) continue;
            this.renderThemeRow(context, animation, entry, row, mouseX, mouseY, alpha);
        }
        context.method_44380();
        this.renderEditor(context, animation, settings, layout, mouseX, mouseY, alpha);
    }

    public boolean handleClick(class_11909 click, float panelX, float panelY, float panelWidth, float panelHeight, ModuleGridSection moduleGridSection) {
        Record row;
        if (click.method_74245() != 0) {
            return false;
        }
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        if (this.hovered(click.comp_4798(), click.comp_4799(), layout.createX, layout.headerY, 14.0f, 14.0f)) {
            this.commitEditingIfNeeded();
            String created = this.themeManager.createTheme();
            if (created != null) {
                this.selectedTheme = created;
                this.startEditing(created);
            }
            return true;
        }
        if (this.hovered(click.comp_4798(), click.comp_4799(), layout.reloadX, layout.headerY, 14.0f, 14.0f)) {
            this.commitEditingIfNeeded();
            this.themeManager.reload();
            return true;
        }
        if (this.hovered(click.comp_4798(), click.comp_4799(), layout.folderX, layout.headerY, 14.0f, 14.0f)) {
            this.commitEditingIfNeeded();
            this.themeManager.openThemeFolder();
            return true;
        }
        List<ThemeManager.ThemeEntry> themes = this.themeManager.listThemes();
        for (int i = 0; i < themes.size(); ++i) {
            ThemeManager.ThemeEntry entry = themes.get(i);
            row = this.themeRow(layout, i, this.presetScroll);
            if (row.y + row.h < layout.bodyY || row.y > layout.bodyY + layout.bodyH || !this.hovered(click.comp_4798(), click.comp_4799(), row.x, row.y, row.w, row.h)) continue;
            String clickableName = this.editingThemeName != null && this.editingThemeName.equalsIgnoreCase(entry.name()) && this.editingText != null ? this.editingText : this.displayThemeName(entry.name());
            float clickableNameW = Math.min(row.w - 24.0f, RenderUtil.getTextWidth(clickableName, GFonts.SEMIBOLD, 6.0f) + 2.0f);
            boolean nameClicked = this.hovered(click.comp_4798(), click.comp_4799(), row.x + 18.0f, row.y + 3.0f, clickableNameW, 12.0f);
            if (nameClicked && !this.themeManager.isBuiltin(entry.name())) {
                if (this.editingThemeName == null || !this.editingThemeName.equalsIgnoreCase(entry.name())) {
                    this.commitEditingIfNeeded();
                    this.startEditing(entry.name());
                }
                this.caretIndex = this.indexFromMouseX(this.editingText, row.x + 18.0f, (float)click.comp_4798());
                this.selectedTheme = entry.name();
                return true;
            }
            if (this.hovered(click.comp_4798(), click.comp_4799(), row.saveX, row.buttonY, row.buttonW, row.buttonH)) {
                String actionName = this.resolveActionName(entry.name());
                if (!this.themeManager.isBuiltin(actionName) && this.themeManager.saveTheme(actionName)) {
                    this.selectedTheme = actionName;
                    ClientTheme.setActiveThemeName(actionName);
                }
                return true;
            }
            if (this.hovered(click.comp_4798(), click.comp_4799(), row.deleteX, row.buttonY, row.buttonW, row.buttonH)) {
                String actionName = this.resolveActionName(entry.name());
                if (this.themeManager.deleteTheme(actionName)) {
                    this.selectedTheme = ClientTheme.getActiveThemeName();
                }
                return true;
            }
            String actionName = this.resolveActionName(entry.name());
            if (this.themeManager.loadTheme(actionName)) {
                this.selectedTheme = actionName;
            }
            return true;
        }
        this.commitEditingIfNeeded();
        if (ClientTheme.isRainbowMode()) {
            return this.hovered(click.comp_4798(), click.comp_4799(), layout.rightX, layout.bodyY, layout.rightW, layout.bodyH);
        }
        List<ColorSetting> settings = ClientTheme.getEditableSettings();
        for (int i = 0; i < settings.size(); ++i) {
            row = this.editorRow(layout, i, this.editorScroll);
            if (!this.hovered(click.comp_4798(), click.comp_4799(), ((EditorRow)row).x, ((EditorRow)row).y, ((EditorRow)row).w, ((EditorRow)row).h)) continue;
            ColorSetting setting = settings.get(i);
            moduleGridSection.openStandaloneColorPicker(setting, setting.getName(), (float)click.comp_4798(), (float)click.comp_4799());
            return true;
        }
        return this.hovered(click.comp_4798(), click.comp_4799(), layout.leftX, layout.bodyY, layout.leftW + 8.0f + layout.rightW, layout.bodyH);
    }

    public boolean handleKeyPressed(class_11908 key) {
        if (this.editingThemeName != null && this.editingText != null) {
            int code = key.comp_4795();
            if (code == 256) {
                this.cancelEditing();
                return true;
            }
            if (code == 257 || code == 335) {
                this.commitEditingIfNeeded();
                return true;
            }
            if (code == 263) {
                this.caretIndex = Math.max(0, this.caretIndex - 1);
                return true;
            }
            if (code == 262) {
                this.caretIndex = Math.min(this.editingText.length(), this.caretIndex + 1);
                return true;
            }
            if (code == 268) {
                this.caretIndex = 0;
                return true;
            }
            if (code == 269) {
                this.caretIndex = this.editingText.length();
                return true;
            }
            if (code == 259) {
                if (this.caretIndex > 0) {
                    this.editingText = this.editingText.substring(0, this.caretIndex - 1) + this.editingText.substring(this.caretIndex);
                    --this.caretIndex;
                }
                return true;
            }
            if (code == 261) {
                if (this.caretIndex < this.editingText.length()) {
                    this.editingText = this.editingText.substring(0, this.caretIndex) + this.editingText.substring(this.caretIndex + 1);
                }
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean handleCharTyped(int codePoint) {
        if (this.editingThemeName != null && this.editingText != null && Character.isValidCodePoint(codePoint) && !Character.isISOControl(codePoint)) {
            String inserted = new String(Character.toChars(codePoint));
            if (this.editingText.length() + inserted.length() > 50) {
                return true;
            }
            this.editingText = this.editingText.substring(0, this.caretIndex) + inserted + this.editingText.substring(this.caretIndex);
            this.caretIndex += inserted.length();
            return true;
        }
        return false;
    }

    public boolean handleScroll(double mouseX, double mouseY, double amount, float panelX, float panelY, float panelWidth, float panelHeight) {
        if (amount == 0.0) {
            return false;
        }
        Layout layout = this.layout(panelX, panelY, panelWidth, panelHeight);
        if (this.hovered(mouseX, mouseY, layout.leftX, layout.bodyY, layout.leftW, layout.bodyH)) {
            int count = this.themeManager.listThemes().size();
            float contentH = count == 0 ? 0.0f : (float)count * 36.0f + (float)(count - 1) * 5.0f;
            float max = Math.max(0.0f, contentH - (layout.bodyH - 8.0f));
            this.presetScrollTarget = this.clamp(this.presetScrollTarget - (float)amount * 24.0f, 0.0f, max);
            return true;
        }
        if (this.hovered(mouseX, mouseY, layout.rightX, layout.bodyY, layout.rightW, layout.bodyH) && !ClientTheme.isRainbowMode()) {
            float viewportH = Math.max(1.0f, layout.bodyH - 43.0f);
            int count = ClientTheme.getEditableSettings().size();
            float contentH = (float)count * 23.0f + (float)Math.max(0, count - 1) * 4.0f;
            float max = Math.max(0.0f, contentH - viewportH);
            this.editorScrollTarget = this.clamp(this.editorScrollTarget - (float)amount * 24.0f, 0.0f, max);
            return true;
        }
        return false;
    }

    private void renderEditor(class_332 context, UiAnimationEngine animation, List<ColorSetting> settings, Layout layout, int mouseX, int mouseY, float alpha) {
        String title = ClientTheme.isRainbowMode() ? "\u0420\u0430\u0434\u0443\u0433\u0430" : "\u0420\u0435\u0434\u0430\u043a\u0442\u043e\u0440 \u0442\u0435\u043c\u044b";
        RenderUtil.drawText(context, title, layout.rightX + 8.0f, layout.bodyY + 7.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.SEMIBOLD, 7.0f);
        String subtitle = ClientTheme.isRainbowMode() ? "\u0422\u041e\u041b\u042c\u041a\u041e \u0427\u0422\u0415\u041d\u0418\u0415 \u2022 RGB-\u0430\u043d\u0438\u043c\u0430\u0446\u0438\u044f" : "\u0426\u0432\u0435\u0442 \u0438 \u043f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u043e\u0432";
        RenderUtil.drawTextFadeRight(context, subtitle, layout.rightX + 8.0f, layout.bodyY + 18.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha), GFonts.MEDIUM, 5.2f, layout.rightW - 16.0f, 4.0f);
        float previewY = layout.bodyY + 29.0f;
        int[] previews = new int[]{ClientColors.CATEGORY_ACCENT, ClientColors.CATEGORY_ACCENT_SECOND, ClientColors.HUD_BACKGROUND, ClientColors.TEXT_PRIMARY};
        for (int i = 0; i < previews.length; ++i) {
            RenderUtil.drawCircle(context, layout.rightX + 8.0f + (float)i * 13.0f, previewY, 8.0f, ColorUtil.multiplyAlpha(previews[i], alpha));
        }
        if (ClientTheme.isRainbowMode()) {
            float lockY = layout.bodyY + 52.0f;
            RenderUtil.drawRoundRect(context, layout.rightX + 8.0f, lockY, layout.rightW - 16.0f, 31.0f, 4.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha * 0.72f));
            RenderUtil.drawText(context, "\u042d\u0442\u043e\u0442 \u043f\u0440\u0435\u0441\u0435\u0442 \u0437\u0430\u0449\u0438\u0449\u0451\u043d", layout.rightX + 16.0f, lockY + 8.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.MEDIUM, 6.0f);
            RenderUtil.drawText(context, "\u0421\u043e\u0437\u0434\u0430\u0439\u0442\u0435 \u0442\u0435\u043c\u0443 \u0434\u043b\u044f \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438", layout.rightX + 16.0f, lockY + 18.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha), GFonts.MEDIUM, 5.0f);
        } else {
            float listTop = layout.bodyY + 43.0f;
            float viewportH = Math.max(1.0f, layout.bodyH - 43.0f);
            float contentH = (float)settings.size() * 23.0f + (float)Math.max(0, settings.size() - 1) * 4.0f;
            float maxScroll = Math.max(0.0f, contentH - viewportH);
            this.editorScrollTarget = this.clamp(this.editorScrollTarget, 0.0f, maxScroll);
            this.editorScroll = animation.animate("theme:editor:scroll", this.editorScrollTarget, 26.0f, this.editorScroll);
            context.method_44379((int)Math.floor(layout.rightX + 1.0f), (int)Math.floor(listTop), (int)Math.ceil(layout.rightX + layout.rightW - 1.0f), (int)Math.ceil(layout.bodyY + layout.bodyH - 1.0f));
            for (int i = 0; i < settings.size(); ++i) {
                EditorRow row = this.editorRow(layout, i, this.editorScroll);
                if (row.y + row.h < listTop || row.y > layout.bodyY + layout.bodyH) continue;
                this.renderEditorRow(context, animation, settings.get(i), row, mouseX, mouseY, alpha);
            }
            context.method_44380();
            if (maxScroll > 0.0f) {
                float trackH = viewportH - 4.0f;
                float thumbH = Math.max(18.0f, trackH * viewportH / contentH);
                float thumbY = listTop + 2.0f + (trackH - thumbH) * (this.editorScroll / maxScroll);
                RenderUtil.drawRoundRect(context, layout.rightX + layout.rightW - 3.0f, thumbY, 1.2f, thumbH, 0.6f, ColorUtil.multiplyAlpha(ClientColors.SCROLLBAR, alpha * 0.82f));
            }
        }
    }

    private void renderThemeRow(class_332 context, UiAnimationEngine animation, ThemeManager.ThemeEntry entry, ThemeRow row, int mouseX, int mouseY, float alpha) {
        float maxCaretX;
        int safeCaret;
        float caretX;
        boolean active = entry.name().equalsIgnoreCase(ClientTheme.getActiveThemeName());
        boolean selected = entry.name().equalsIgnoreCase(this.selectedTheme);
        boolean hovered = this.hovered(mouseX, mouseY, row.x, row.y, row.w, row.h);
        float hover = animation.animate01("theme:file:hover:" + entry.name(), hovered ? 1.0f : 0.0f, 14.0f, 0.0f);
        int bg = this.themedCardBackground(hover, alpha, selected || active);
        int border = this.themedCardBorder(hover, alpha, selected || active);
        RenderUtil.drawRoundRect(context, row.x, row.y, row.w, row.h, 4.0f, bg);
        RenderUtil.drawBorder(context, row.x, row.y, row.w, row.h, 4.0f, border, 0.7f, 0.0f, 0.5f);
        int preview = "Rainbow".equalsIgnoreCase(entry.name()) ? ClientColors.CATEGORY_ACCENT : entry.previewColor();
        RenderUtil.drawCircle(context, row.x + 6.0f, row.y + 6.0f, 8.0f, ColorUtil.multiplyAlpha(preview, alpha));
        boolean editing = this.editingThemeName != null && this.editingThemeName.equalsIgnoreCase(entry.name());
        String shownName = editing && this.editingText != null ? this.editingText : this.displayThemeName(entry.name());
        RenderUtil.drawTextFadeRight(context, shownName, row.x + 18.0f, row.y + 6.5f, ColorUtil.multiplyAlpha(!active && !editing ? ClientColors.CATEGORY_INACTIVE : ClientColors.TEXT_PRIMARY, alpha), GFonts.SEMIBOLD, 6.0f, row.w - 24.0f, 3.0f);
        if (editing && (caretX = row.x + 18.0f + RenderUtil.getTextWidth(this.editingText.substring(0, safeCaret = Math.max(0, Math.min(this.caretIndex, this.editingText.length()))), GFonts.SEMIBOLD, 6.0f)) <= (maxCaretX = row.x + row.w - 5.0f) && (System.currentTimeMillis() / 480L & 1L) == 0L) {
            RenderUtil.drawRoundRect(context, caretX, row.y + 4.0f, 0.8f, 9.0f, 0.4f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha));
        }
        boolean builtin = this.themeManager.isBuiltin(entry.name());
        this.renderRowButton(context, animation, "theme:save:" + entry.name(), builtin ? "-" : "\u0421\u043e\u0445\u0440", row.saveX, row.buttonY, row.buttonW, row.buttonH, !builtin, mouseX, mouseY, alpha);
        this.renderRowButton(context, animation, "theme:delete:" + entry.name(), this.themeManager.isBuiltin(entry.name()) ? "-" : "\u0423\u0434\u0430\u043b", row.deleteX, row.buttonY, row.buttonW, row.buttonH, !this.themeManager.isBuiltin(entry.name()), mouseX, mouseY, alpha);
    }

    private void renderEditorRow(class_332 context, UiAnimationEngine animation, ColorSetting setting, EditorRow row, int mouseX, int mouseY, float alpha) {
        boolean hovered = this.hovered(mouseX, mouseY, row.x, row.y, row.w, row.h);
        float hover = animation.animate01("theme:editor:hover:" + setting.getName(), hovered ? 1.0f : 0.0f, 14.0f, 0.0f);
        RenderUtil.drawRoundRect(context, row.x, row.y, row.w, row.h, 3.5f, this.themedCardBackground(hover, alpha, false));
        RenderUtil.drawTextFadeRight(context, setting.getName(), row.x + 7.0f, row.y + 5.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.MEDIUM, 5.8f, row.w - 39.0f, 3.0f);
        String opacity = Math.round(setting.getAlpha01() * 100.0f) + "%";
        RenderUtil.drawText(context, opacity, row.x + 7.0f, row.y + 14.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha), GFonts.MEDIUM, 4.8f);
        RenderUtil.drawRoundRect(context, row.x + row.w - 22.0f, row.y + 5.5f, 12.0f, 12.0f, 4.0f, ColorUtil.multiplyAlpha(setting.getColor(), alpha));
        RenderUtil.drawBorder(context, row.x + row.w - 22.0f, row.y + 5.5f, 12.0f, 12.0f, 4.0f, ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha), 0.0f, 0.55f, 1.0f);
    }

    private void renderTopButton(class_332 context, UiAnimationEngine animation, String key, String icon, float x, float y, int mouseX, int mouseY, float alpha) {
        boolean hovered = this.hovered(mouseX, mouseY, x, y, 14.0f, 14.0f);
        float hover = animation.animate01(key + ":hover", hovered ? 1.0f : 0.0f, 14.0f, 0.0f);
        if (hover > 0.01f) {
            RenderUtil.drawRoundRect(context, x, y, 14.0f, 14.0f, 3.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * hover * 0.16f));
        }
        RenderUtil.drawImage(context, icon, x + 3.0f, y + 3.0f, 8.0f, 8.0f, 0.0f, ColorUtil.multiplyAlpha(ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, hover), alpha), 1.0f);
    }

    private void renderRowButton(class_332 context, UiAnimationEngine animation, String key, String text, float x, float y, float width, float height, boolean enabled, int mouseX, int mouseY, float alpha) {
        boolean hovered = enabled && this.hovered(mouseX, mouseY, x, y, width, height);
        float hover = animation.animate01(key + ":hover", hovered ? 1.0f : 0.0f, 14.0f, 0.0f);
        int bg = ColorUtil.interpolate(ClientColors.SETTINGS_BACKGROUND, ClientColors.CATEGORY_ACCENT, hover * 0.16f);
        RenderUtil.drawRoundRect(context, x, y, width, height, 2.2f, ColorUtil.multiplyAlpha(bg, alpha * (enabled ? 0.9f : 0.42f)));
        float textW = RenderUtil.getTextWidth(text, GFonts.MEDIUM, 4.5f);
        RenderUtil.drawText(context, text, x + (width - textW) * 0.5f, y + 3.2f, ColorUtil.multiplyAlpha(enabled ? ClientColors.TEXT_PRIMARY : ClientColors.CATEGORY_INACTIVE, alpha * (enabled ? 1.0f : 0.55f)), GFonts.MEDIUM, 4.5f);
    }

    private void drawContainer(class_332 context, float x, float y, float width, float height, float alpha) {
        RenderUtil.drawRoundRect(context, x, y, width, height, 4.0f, ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha * 0.5f));
        RenderUtil.drawBorder(context, x, y, width, height, 4.0f, ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha * 0.95f), 0.0f, 0.5f, 1.0f);
    }

    private int themedCardBackground(float hover, float alpha, boolean selected) {
        int base = ColorUtil.interpolate(ClientColors.CATEGORY_BACKGROUND, ClientColors.SETTINGS_BACKGROUND, 0.52f + hover * 0.18f);
        if (selected) {
            base = ColorUtil.interpolate(base, ColorUtil.withAlpha(ClientColors.CATEGORY_ACCENT, 255), 0.08f);
        }
        return ColorUtil.multiplyAlpha(base, alpha);
    }

    private int themedCardBorder(float hover, float alpha, boolean selected) {
        int base = ColorUtil.interpolate(ClientColors.CATEGORY_BACKGROUND_OUTLINE, ClientColors.SETTINGS_OUTLINE, 0.5f + hover * 0.15f);
        if (selected) {
            base = ColorUtil.interpolate(base, ClientColors.CATEGORY_ACCENT, 0.3f);
        }
        return ColorUtil.multiplyAlpha(base, alpha);
    }

    private Layout layout(float panelX, float panelY, float panelWidth, float panelHeight) {
        float contentX = panelX + 12.0f;
        float contentW = panelWidth - 24.0f;
        float headerY = panelY + 14.0f;
        float bodyY = headerY + 16.0f + 5.0f;
        float bodyH = Math.max(40.0f, panelY + panelHeight - 10.0f - bodyY);
        float leftW = Math.min(108.0f, Math.max(82.0f, contentW * 0.42f));
        float rightX = contentX + leftW + 8.0f;
        float rightW = Math.max(70.0f, contentW - leftW - 8.0f);
        float folderX = contentX + contentW - 14.0f;
        float reloadX = folderX - 20.0f;
        float createX = reloadX - 20.0f;
        return new Layout(contentX, headerY, createX, reloadX, folderX, contentX, rightX, leftW, rightW, bodyY, bodyH);
    }

    private ThemeRow themeRow(Layout layout, int index, float scroll) {
        float x = layout.leftX + 4.0f;
        float y = layout.bodyY + 4.0f + (float)index * 41.0f - scroll;
        float w = layout.leftW - 8.0f;
        float buttonGap = 3.0f;
        float buttonW = (w - 8.0f - buttonGap) / 2.0f;
        float buttonY = y + 22.0f;
        float saveX = x + 4.0f;
        float deleteX = saveX + buttonW + buttonGap;
        return new ThemeRow(x, y, w, 36.0f, saveX, deleteX, buttonY, buttonW, 10.0f);
    }

    private EditorRow editorRow(Layout layout, int index, float scroll) {
        float x = layout.rightX + 5.0f;
        float y = layout.bodyY + 43.0f + (float)index * 27.0f - scroll;
        return new EditorRow(x, y, layout.rightW - 10.0f, 23.0f);
    }

    private void startEditing(String name) {
        this.editingThemeName = name;
        this.editingText = name;
        this.caretIndex = this.editingText.length();
    }

    private String commitEditingIfNeeded() {
        if (this.editingThemeName != null && this.editingText != null) {
            String from = this.editingThemeName;
            String next = this.editingText.trim();
            String renamed = next.isEmpty() ? null : this.themeManager.renameTheme(from, next);
            this.selectedTheme = renamed != null ? renamed : from;
            this.editingThemeName = null;
            this.editingText = null;
            this.caretIndex = 0;
            return this.selectedTheme;
        }
        return null;
    }

    private String resolveActionName(String fallback) {
        if (this.editingThemeName != null && this.editingThemeName.equalsIgnoreCase(fallback)) {
            String committed = this.commitEditingIfNeeded();
            return committed == null ? fallback : committed;
        }
        this.commitEditingIfNeeded();
        return fallback;
    }

    private void cancelEditing() {
        this.editingThemeName = null;
        this.editingText = null;
        this.caretIndex = 0;
    }

    private int indexFromMouseX(String text, float textX, float mouseX) {
        if (text != null && !text.isEmpty()) {
            float local = Math.max(0.0f, mouseX - textX);
            for (int i = 1; i <= text.length(); ++i) {
                float width = RenderUtil.getTextWidth(text.substring(0, i), GFonts.SEMIBOLD, 6.0f);
                if (!(local < width)) continue;
                return i - 1;
            }
            return text.length();
        }
        return 0;
    }

    private String displayThemeName(String name) {
        if (name == null) {
            return "";
        }
        if ("Dark".equalsIgnoreCase(name)) {
            return "\u0422\u0451\u043c\u043d\u0430\u044f";
        }
        if ("Light".equalsIgnoreCase(name)) {
            return "\u0421\u0432\u0435\u0442\u043b\u0430\u044f";
        }
        return "Rainbow".equalsIgnoreCase(name) ? "\u0420\u0430\u0434\u0443\u0433\u0430" : name;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean hovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    private record Layout(float contentX, float headerY, float createX, float reloadX, float folderX, float leftX, float rightX, float leftW, float rightW, float bodyY, float bodyH) {
    }

    private record ThemeRow(float x, float y, float w, float h, float saveX, float deleteX, float buttonY, float buttonW, float buttonH) {
    }

    private record EditorRow(float x, float y, float w, float h) {
    }
}

