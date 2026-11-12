/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  org.lwjgl.glfw.GLFW
 */
package pyz1k.screen.menu.sections;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.lwjgl.glfw.GLFW;
import pyz1k.Pyz1k;
import pyz1k.module.base.Category;
import pyz1k.module.base.Module;
import pyz1k.module.bind.BindData;
import pyz1k.module.bind.BindMode;
import pyz1k.module.setting.BooleanSetting;
import pyz1k.module.setting.ColorSetting;
import pyz1k.module.setting.MultiSelectSetting;
import pyz1k.module.setting.SelectSetting;
import pyz1k.module.setting.Setting;
import pyz1k.module.setting.SliderSetting;
import pyz1k.render.RenderUtil;
import pyz1k.render.builders.states.QuadRadiusState;
import pyz1k.render.font.GFonts;
import pyz1k.render.font.GIcons;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.screen.menu.sections.CategoryBarSection;
import pyz1k.screen.menu.sections.settings.BooleanSettingRenderer;
import pyz1k.screen.menu.sections.settings.ColorSettingRenderer;
import pyz1k.screen.menu.sections.settings.MultiSelectSettingRenderer;
import pyz1k.screen.menu.sections.settings.SelectSettingRenderer;
import pyz1k.screen.menu.sections.settings.SliderSettingRenderer;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ClientTheme;
import pyz1k.utilites.color.ColorUtil;

public final class ModuleGridSection {
    private static final int COLUMNS = 2;
    private static final float CONTENT_PAD_X = 10.0f;
    private static final float CONTENT_TOP = 14.0f;
    private static final float CONTENT_BOTTOM_PAD = 8.0f;
    private static final float SCROLL_SPEED = 26.0f;
    private static final float SCROLL_STEP = 22.0f;
    private static final float SCROLLBAR_WIDTH = 2.0f;
    private static final float SCROLLBAR_MIN_THUMB_H = 18.0f;
    private static final float SCROLLBAR_SIDE_PAD = 4.0f;
    private static final float CARD_BASE_HEIGHT = 25.0f;
    private static final float CARD_GAP_X = 8.0f;
    private static final float CARD_GAP_Y = 8.0f;
    private static final float MODULE_SETTING_BOTTOM_PAD = 4.0f;
    private static final float MODULE_ICON_SIZE = 8.0f;
    private static final float MODULE_ICON_X_PAD = 8.0f;
    private static final float MODULE_TEXT_X_GAP = 5.0f;
    private static final float MODULE_TEXT_GAP_TO_BIND = 6.0f;
    private static final float MODULE_TEXT_FADE_WIDTH = 22.0f;
    private static final float MODULE_TITLE_FADE_WIDTH = 9.0f;
    private static final float MODULE_TEXT_MIN_FINAL_ALPHA = 0.004f;
    private static final float MODULE_TITLE_SCROLL_SPEED = 4.8f;
    private static final float MODULE_TITLE_TO_SETTING_GAP_EXTRA = 4.0f;
    private static final float MODULE_SETTINGS_GAP = 2.0f;
    private static final float MODULE_SETTINGS_SELECT_TO_BOOLEAN_GAP = 5.0f;
    private static final float TOGGLE_WIDTH = 15.0f;
    private static final float TOGGLE_HEIGHT = 10.0f;
    private static final float TOGGLE_KNOB_SIZE = 7.0f;
    private static final float TOGGLE_INNER_PAD = 1.0f;
    private static final float BIND_HEIGHT = 8.5f;
    private static final float BIND_GAP_FROM_TOGGLE = 4.0f;
    private static final float BIND_ICON_SIZE = 6.5f;
    private static final float BIND_PAD_X = 3.0f;
    private static final float BIND_ICON_GAP = 2.0f;
    private static final int BIND_TEXT_SIZE = 5;
    private static final float BIND_HOVER_SPEED = 12.0f;
    private static final float BIND_HOVER_BG_MIX = 0.22f;
    private static final float BIND_HOVER_ICON_MIX = 0.55f;
    private static final float BIND_HOVER_TEXT_MIX = 0.6f;
    private static final float BIND_WIDTH_ANIM_SPEED = 20.0f;
    private static final float BIND_TEXT_REVEAL_SPEED = 16.0f;
    private static final float BIND_TEXT_REVEAL_EPS = 0.12f;
    private static final float BIND_WIDTH_SETTLE_EPS = 0.06f;
    private static final float SELECT_EXPAND_SPEED = 18.0f;
    private static final float MULTI_SELECT_EXPAND_SPEED = 18.0f;
    private static final float POPUP_WIDTH = 156.0f;
    private static final float POPUP_HEIGHT = 67.0f;
    private static final float POPUP_PAD = 8.0f;
    private static final float MODE_BUTTON_W = 34.0f;
    private static final float MODE_BUTTON_H = 14.0f;
    private static final float POPUP_TITLE_Y = 8.5f;
    private static final float POPUP_BADGE_Y = 7.0f;
    private static final float POPUP_MODE_Y = 32.0f;
    private static final float POPUP_KEY_Y = 52.0f;
    private static final float COLOR_POPUP_WIDTH = 170.0f;
    private static final float COLOR_POPUP_HEIGHT = 194.0f;
    private static final float COLOR_POPUP_PAD = 8.0f;
    private static final float COLOR_PICKER_SIZE = 110.0f;
    private static final float COLOR_BAR_W = 8.0f;
    private static final float COLOR_BAR_GAP = 6.0f;
    private static final float COLOR_PRESET_SIZE = 7.0f;
    private static final float COLOR_PRESET_GAP = 4.0f;
    private static final float COLOR_CLOSE_H = 16.0f;
    private static final float COLOR_INFO_H = 14.0f;
    private static final float COLOR_ACTION_ICON_SIZE = 7.0f;
    private static final int[] COLOR_PRESETS = new int[]{ColorUtil.rgb(255, 77, 77), ColorUtil.rgb(85, 214, 109), ColorUtil.rgb(244, 108, 196), ClientColors.CATEGORY_ACCENT, ColorUtil.rgb(255, 183, 77), ColorUtil.rgb(97, 189, 255), ColorUtil.rgb(186, 120, 255), ColorUtil.rgb(255, 92, 140), ColorUtil.rgb(117, 230, 201), ColorUtil.rgb(238, 238, 238), ColorUtil.rgb(255, 140, 56), ColorUtil.rgb(255, 232, 102)};
    private Module settingsModule;
    private float gridScrollBeforeSettings;
    private CategoryBarSection.TopTab displayTab;
    private CategoryBarSection.TopTab previousTab;
    private int switchEpoch;
    private Module bindPopupModule;
    private Setting bindPopupSetting;
    private boolean bindPopupOpen;
    private boolean bindCapture;
    private Module draggingSliderModule;
    private SliderSetting draggingSliderSetting;
    private Module selectedSliderModule;
    private SliderSetting selectedSliderSetting;
    private float draggingSliderX;
    private float draggingSliderY;
    private float draggingSliderW;
    private float bindPopupAnchorX;
    private float bindPopupAnchorY;
    private Module colorPopupModule;
    private ColorSetting colorPopupSetting;
    private String colorPopupTitleOverride;
    private boolean colorPopupOpen;
    private float colorPopupAnchorX;
    private float colorPopupAnchorY;
    private ColorDragMode colorDragMode = ColorDragMode.NONE;
    private final LinkedHashSet<Integer> capturePressedCodes = new LinkedHashSet();
    private final LinkedHashSet<Integer> captureSelectedCodes = new LinkedHashSet();
    private final Map<String, Float> moduleBindWidths = new HashMap<String, Float>();
    private final Map<String, Float> popupKeyWidths = new HashMap<String, Float>();
    private final Map<String, String> moduleBindTexts = new HashMap<String, String>();
    private final Map<String, Boolean> moduleBindTextUnlocked = new HashMap<String, Boolean>();
    private final Map<String, String> popupKeyTexts = new HashMap<String, String>();
    private final Map<String, Boolean> popupKeyTextUnlocked = new HashMap<String, Boolean>();
    private final Map<String, Float> moduleTitleScrollValues = new HashMap<String, Float>();
    private final Map<String, Boolean> moduleTitleScrollForward = new HashMap<String, Boolean>();
    private final Map<CategoryBarSection.TopTab, Float> tabScrollTargets = new HashMap<CategoryBarSection.TopTab, Float>();
    private final Map<CategoryBarSection.TopTab, Float> tabScrollValues = new HashMap<CategoryBarSection.TopTab, Float>();
    private final Map<CategoryBarSection.TopTab, Float> tabMaxScroll = new HashMap<CategoryBarSection.TopTab, Float>();
    private final Map<CategoryBarSection.TopTab, ScrollbarState> tabScrollbars = new HashMap<CategoryBarSection.TopTab, ScrollbarState>();
    private boolean scrollbarDragging;
    private float scrollbarDragOffsetY;
    private final Map<String, Boolean> selectExpanded = new HashMap<String, Boolean>();
    private final Map<String, Float> selectExpandProgress = new HashMap<String, Float>();
    private final Map<String, Boolean> multiSelectExpanded = new HashMap<String, Boolean>();
    private final Map<String, Float> multiSelectExpandProgress = new HashMap<String, Float>();

    public boolean isSettingsOpen() {
        return this.settingsModule != null;
    }

    public void openSettings(Module module) {
        if (module == null) {
            return;
        }
        this.requestClosePopup();
        CategoryBarSection.TopTab tab = this.toTab(module.getCategory());
        if (tab == null) {
            return;
        }
        if (this.settingsModule == null) {
            this.gridScrollBeforeSettings = this.tabScrollTargets.getOrDefault((Object)tab, Float.valueOf(0.0f)).floatValue();
        }
        this.settingsModule = module;
        this.tabScrollTargets.put(tab, Float.valueOf(0.0f));
        this.tabScrollValues.put(tab, Float.valueOf(0.0f));
        this.scrollbarDragging = false;
    }

    public void closeSettings() {
        if (this.settingsModule == null) {
            return;
        }
        CategoryBarSection.TopTab tab = this.toTab(this.settingsModule.getCategory());
        this.requestClosePopup();
        this.settingsModule = null;
        this.tabScrollTargets.put(tab, Float.valueOf(this.gridScrollBeforeSettings));
        this.tabScrollValues.put(tab, Float.valueOf(this.gridScrollBeforeSettings));
        this.scrollbarDragging = false;
    }

    public void render(class_332 context, UiAnimationEngine animation, float panelX, float panelY, float panelWidth, float panelHeight, int mouseX, int mouseY, float alpha, CategoryBarSection.TopTab activeTab) {
        float newAlpha;
        if (this.displayTab == null) {
            this.displayTab = activeTab;
            this.previousTab = null;
        }
        if (activeTab != this.displayTab) {
            this.previousTab = this.displayTab;
            this.displayTab = activeTab;
            ++this.switchEpoch;
            this.scrollbarDragging = false;
        }
        this.updateScrollbarDrag(activeTab, mouseY);
        float switchAnim = this.previousTab == null ? 1.0f : animation.animate01("modules:switch:" + this.switchEpoch, 1.0f, 18.0f, 0.0f);
        float oldAlpha = alpha * (1.0f - switchAnim);
        if (oldAlpha > 0.01f && this.previousTab != null) {
            this.renderModulesForTab(context, animation, panelX, panelY, panelWidth, panelHeight, mouseX, mouseY, oldAlpha, this.previousTab, false);
        }
        if ((newAlpha = alpha * switchAnim) > 0.01f) {
            this.renderModulesForTab(context, animation, panelX, panelY, panelWidth, panelHeight, mouseX, mouseY, newAlpha, this.displayTab, true);
        }
        if (switchAnim >= 0.999f) {
            this.previousTab = null;
        }
        this.updateSliderDrag(mouseX);
        this.renderBindPopup(context, animation, panelX, panelY, panelWidth, panelHeight, mouseX, mouseY, alpha);
        this.updateColorDrag(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
    }

    public CategoryBarSection.TopTab focusModule(Module target, float panelWidth, float panelHeight) {
        if (target == null) {
            return null;
        }
        CategoryBarSection.TopTab tab = this.toTab(target.getCategory());
        if (tab == null) {
            return null;
        }
        List<Module> modules = Pyz1k.getInstance().getModuleManager().getModules(target.getCategory());
        int index = modules.indexOf(target);
        if (index < 0) {
            return tab;
        }
        float contentW = panelWidth - 20.0f;
        float cardWidth = (contentW - 8.0f) / 2.0f;
        float viewportHeight = Math.max(1.0f, panelHeight - 14.0f - 8.0f);
        float[] columnBottomY = new float[2];
        float moduleY = 0.0f;
        for (int i = 0; i <= index; ++i) {
            int col = i % 2;
            Module module = modules.get(i);
            float y = columnBottomY[col];
            if (i == index) {
                moduleY = y;
                break;
            }
            columnBottomY[col] = y + this.getModuleCardHeight(module, cardWidth, null) + 8.0f;
        }
        float maxScroll = this.computeMaxScroll(modules, cardWidth, viewportHeight, null);
        float targetScroll = Math.max(0.0f, moduleY - viewportHeight * 0.32f);
        this.tabScrollTargets.put(tab, Float.valueOf(this.clamp(targetScroll, 0.0f, maxScroll)));
        this.displayTab = tab;
        this.previousTab = null;
        this.scrollbarDragging = false;
        return tab;
    }

    private void renderModulesForTab(class_332 context, UiAnimationEngine animation, float panelX, float panelY, float panelWidth, float panelHeight, int mouseX, int mouseY, float alpha, CategoryBarSection.TopTab tab, boolean animateSlotState) {
        Category category = this.toCategory(tab);
        if (category != null) {
            List<Module> modules;
            boolean detail = this.settingsModule != null && this.settingsModule.getCategory() == category;
            List<Module> list = modules = detail ? List.of(this.settingsModule) : this.visibleModules(category);
            if (!modules.isEmpty()) {
                float contentX = panelX + 10.0f;
                float contentY = panelY + (detail ? 39.0f : 14.0f);
                float contentW = panelWidth - 20.0f;
                float cardWidth = detail ? contentW - 16.0f : (contentW - 8.0f) / 2.0f;
                float viewportTop = contentY;
                float viewportBottom = panelY + panelHeight - 8.0f;
                float viewportHeight = Math.max(1.0f, viewportBottom - viewportTop);
                float maxScroll = this.computeMaxScroll(modules, cardWidth, viewportHeight, animation);
                float scrollTarget = this.clamp(this.tabScrollTargets.getOrDefault((Object)tab, Float.valueOf(0.0f)).floatValue(), 0.0f, maxScroll);
                this.tabScrollTargets.put(tab, Float.valueOf(scrollTarget));
                this.tabMaxScroll.put(tab, Float.valueOf(maxScroll));
                String scrollKey = "modules:scroll:" + tab.name();
                float scrollValue = animation.animate(scrollKey, scrollTarget, 26.0f, scrollTarget);
                if (Math.abs(scrollValue - scrollTarget) <= 0.01f) {
                    scrollValue = scrollTarget;
                    animation.snap(scrollKey, scrollTarget);
                }
                this.tabScrollValues.put(tab, Float.valueOf(scrollValue));
                int scX1 = (int)Math.floor(contentX);
                int scY1 = (int)Math.floor(viewportTop);
                int scX2 = (int)Math.ceil(contentX + contentW);
                int scY2 = (int)Math.ceil(viewportBottom);
                if (!detail) {
                    RenderUtil.drawText(context, "\u041c\u043e\u0434\u0443\u043b\u0438: " + modules.size(), contentX, panelY + 3.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha), GFonts.MEDIUM, 6.0f);
                }
                if (detail) {
                    RenderUtil.drawRoundRect(context, panelX + 13.0f, panelY + 8.0f, 53.0f, 21.0f, 4.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha));
                    RenderUtil.drawText(context, "<  \u041d\u0430\u0437\u0430\u0434", panelX + 23.0f, panelY + 14.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.MEDIUM, 7.0f);
                    RenderUtil.drawText(context, this.settingsModule.getName(), panelX + 76.0f, panelY + 13.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT_SECOND, alpha), GFonts.SEMIBOLD, 10.0f);
                }
                context.method_44379(scX1, scY1, scX2, scY2);
                float[] columnBottomY = new float[2];
                for (int c = 0; c < 2; ++c) {
                    columnBottomY[c] = contentY;
                }
                for (int i = 0; i < modules.size(); ++i) {
                    int col = detail ? 0 : i % 2;
                    float x = detail ? contentX + 8.0f : contentX + (float)col * (cardWidth + 8.0f);
                    float y = columnBottomY[col];
                    Module module = modules.get(i);
                    float moduleCardHeight = this.getModuleCardHeight(module, cardWidth, animation);
                    float drawY = y - scrollValue;
                    if (!(drawY >= viewportBottom) && !(drawY + moduleCardHeight <= viewportTop)) {
                        if (!detail) {
                            this.renderCompactRow(context, animation, module, x, drawY, cardWidth, moduleCardHeight, i, animateSlotState, alpha);
                            columnBottomY[col] = y + moduleCardHeight + 8.0f;
                            continue;
                        }
                        int cardColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha * 0.95f);
                        int borderColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND_OUTLINE, alpha);
                        int textColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
                        RenderUtil.drawRoundRect(context, x, drawY, cardWidth, moduleCardHeight, 5.0f, cardColor);
                        RenderUtil.drawBorder(context, x, drawY, cardWidth, moduleCardHeight, 5.0f, borderColor, 1.0f, 0.0f, 0.5f);
                        String moduleKey = module.getClass().getName();
                        float targetState = module.isEnable() ? 1.0f : 0.0f;
                        float stateAnim = animateSlotState ? animation.animate01("module:state:slot:" + i, targetState, 11.0f, targetState) : targetState;
                        float toggleX = x + cardWidth - 8.0f - 15.0f;
                        float toggleY = drawY + 7.5f;
                        String bindText = this.formatBindText(module.getBind().getKeys());
                        float bindTargetWidth = this.getModuleBindTargetWidth(bindText);
                        float bindWidth = this.animateWidth(animation, this.moduleBindWidths, "module:bind:w:" + moduleKey, bindTargetWidth, 20.0f);
                        float bindRightX = toggleX - 4.0f;
                        float bindX = bindRightX - bindWidth;
                        float bindY = drawY + 8.25f;
                        boolean bindHoveredRaw = this.isHovered(mouseX, mouseY, bindX, bindY, bindWidth, 8.5f);
                        float bindHover = animation.animate01("module:bind:hover:" + moduleKey, bindHoveredRaw ? 1.0f : 0.0f, 12.0f, 0.0f);
                        float bindTextReveal = this.animateTextRevealAfterWidth(animation, this.moduleBindTexts, this.moduleBindTextUnlocked, moduleKey, "module:bind:text:reveal:" + moduleKey, bindText, bindWidth, bindTargetWidth);
                        int bindBgBase = ColorUtil.interpolate(ClientColors.SETTINGS_BACKGROUND, ClientColors.CATEGORY_ACCENT, bindHover * 0.22f);
                        int bindBgColor = ColorUtil.multiplyAlpha(bindBgBase, alpha);
                        int bindBorderColor = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha);
                        int bindIconBase = ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, bindHover * 0.55f);
                        int bindTextBase = ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, bindHover * 0.6f);
                        int bindIconColor = ColorUtil.multiplyAlpha(bindIconBase, alpha);
                        int bindTextColor = ColorUtil.multiplyAlpha(bindTextBase, alpha * bindTextReveal);
                        RenderUtil.drawRoundRect(context, bindX, bindY, bindWidth, 8.5f, 2.5f, bindBgColor);
                        RenderUtil.drawBorder(context, bindX, bindY, bindWidth, 8.5f, 2.5f, bindBorderColor, 0.0f, 0.5f, 1.0f);
                        float bindIconX = bindX + 3.0f;
                        float bindIconY = bindY + 1.0f;
                        RenderUtil.drawIcon(context, GIcons.KEYBOARD, bindIconX, bindIconY, 6.5f, bindIconColor);
                        float bindTextX = bindIconX + 6.5f + 2.0f;
                        float bindTextY = bindY + (8.5f + RenderUtil.getTextHeight(GFonts.BOLD, 5)) / 2.0f - 5.5f;
                        RenderUtil.drawText(context, bindText, bindTextX, bindTextY, bindTextColor, GFonts.BOLD, 5);
                        boolean toggleHoveredRaw = this.isHovered(mouseX, mouseY, toggleX - 1.0f, toggleY - 1.0f, 17.0f, 12.0f);
                        float toggleHover = animation.animate01("module:toggle:hover:" + moduleKey, toggleHoveredRaw ? 1.0f : 0.0f, 14.0f, 0.0f);
                        int toggleTrackBase = ColorUtil.interpolate(ClientColors.SETTINGS_BACKGROUND, ClientColors.CATEGORY_ACCENT, stateAnim);
                        toggleTrackBase = ColorUtil.interpolate(toggleTrackBase, ClientColors.TEXT_PRIMARY, toggleHover * 0.12f);
                        int toggleTrackColor = ColorUtil.multiplyAlpha(toggleTrackBase, alpha);
                        int trackOutlineColor = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha * (1.0f - stateAnim));
                        RenderUtil.drawRoundRect(context, toggleX, toggleY, 15.0f, 10.0f, 4.0f, toggleTrackColor);
                        RenderUtil.drawBorder(context, toggleX, toggleY, 15.0f, 10.0f, 4.0f, trackOutlineColor, 0.0f, 0.5f, 1.0f);
                        float knobTravel = 6.0f;
                        float knobX = toggleX + 1.0f + knobTravel * stateAnim;
                        float knobY = toggleY + 1.5f;
                        int knobBase = ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, stateAnim);
                        knobBase = ColorUtil.interpolate(knobBase, ClientColors.TEXT_PRIMARY, toggleHover * 0.08f);
                        int knobColor = ColorUtil.multiplyAlpha(knobBase, alpha * 0.95f);
                        float knobOffsetX = 1.0f + -2.0f * stateAnim;
                        RenderUtil.drawRoundRect(context, knobX + knobOffsetX, knobY, 7.0f, 7.0f, 2.5f, knobColor);
                        float textY = drawY + (25.0f + RenderUtil.getTextHeight(GFonts.MEDIUM, 9)) / 2.0f - 8.0f;
                        float textX = x + 8.0f;
                        float maxModuleTextWidth = bindX - 6.0f - textX;
                        float moduleNameWidth = RenderUtil.getTextWidth(module.getName(), GFonts.MEDIUM, 9);
                        if (moduleNameWidth <= maxModuleTextWidth) {
                            this.moduleTitleScrollValues.remove(moduleKey);
                            this.moduleTitleScrollForward.remove(moduleKey);
                            RenderUtil.drawText(context, module.getName(), textX, textY - 0.5f, textColor, GFonts.MEDIUM, 9);
                        } else {
                            float overflowWidth;
                            boolean titleHoveredRaw = this.isHovered(mouseX, mouseY, textX, drawY, maxModuleTextWidth, 25.0f);
                            float titleHover = animation.animate01("module:title:hover:" + moduleKey, titleHoveredRaw ? 1.0f : 0.0f, 8.0f, 0.0f);
                            float titleMix = this.smootherStep01(titleHover);
                            float motionBlend = this.smootherStep01(ColorUtil.clamp01((titleMix - 0.22f) / 0.78f));
                            float gradientBlend = this.smootherStep01(1.0f - ColorUtil.clamp01((titleMix - 0.1f) / 0.78f));
                            float shiftMax = overflowWidth = Math.max(0.0f, moduleNameWidth - maxModuleTextWidth);
                            float scrollOffset = this.updateModuleTitleShift(animation, moduleKey, titleMix, shiftMax);
                            float motionAlpha = motionBlend;
                            float fadeAlpha = gradientBlend;
                            float easedShift = scrollOffset * motionBlend;
                            if (fadeAlpha > 0.004f) {
                                int fadeColor = ColorUtil.multiplyAlpha(textColor, fadeAlpha);
                                this.drawTextWithRightFade(context, module.getName(), textX, textY, fadeColor, GFonts.MEDIUM, 9, maxModuleTextWidth, 9.0f, drawY, 25.0f);
                            }
                            if (motionAlpha > 0.004f) {
                                int motionColor = ColorUtil.multiplyAlpha(textColor, motionAlpha);
                                this.drawTextClipped(context, module.getName(), textX - easedShift, textY - 0.5f, motionColor, GFonts.MEDIUM, 9, textX, maxModuleTextWidth, drawY, 25.0f);
                            }
                        }
                        List<Setting> renderSettings = this.getRenderableSettings(module);
                        if (!renderSettings.isEmpty()) {
                            float titleOffset = (25.0f + RenderUtil.getTextHeight(GFonts.MEDIUM, 9)) / 2.0f - 8.0f;
                            float settingX = x + 8.0f;
                            float settingW = Math.max(16.0f, x + cardWidth - 8.0f - settingX);
                            float settingY = drawY + titleOffset + titleOffset + 4.0f;
                            for (int si = 0; si < renderSettings.size(); ++si) {
                                float expandProgress;
                                boolean expanded;
                                String settingKey;
                                Setting nextSetting;
                                Setting setting = renderSettings.get(si);
                                Setting setting2 = nextSetting = si + 1 < renderSettings.size() ? renderSettings.get(si + 1) : null;
                                if (setting instanceof BooleanSetting) {
                                    BooleanSetting moduleBoolean = (BooleanSetting)setting;
                                    BooleanSettingRenderer.render(context, animation, moduleKey + ":" + moduleBoolean.getName(), moduleBoolean, settingX, settingY, settingW, mouseX, mouseY, alpha);
                                    settingY += BooleanSettingRenderer.measureHeight(moduleBoolean, settingW) + this.getSettingsGap(setting, nextSetting);
                                    continue;
                                }
                                if (setting instanceof SliderSetting) {
                                    SliderSetting sliderSetting = (SliderSetting)setting;
                                    SliderSettingRenderer.render(context, animation, moduleKey + ":" + sliderSetting.getName(), sliderSetting, settingX, settingY, settingW, mouseX, mouseY, alpha);
                                    settingY += SliderSettingRenderer.measureHeight(sliderSetting, settingW) + this.getSettingsGap(setting, nextSetting);
                                    continue;
                                }
                                if (setting instanceof ColorSetting) {
                                    ColorSetting colorSetting = (ColorSetting)setting;
                                    ColorSettingRenderer.render(context, animation, moduleKey + ":" + colorSetting.getName(), colorSetting, settingX, settingY, settingW, mouseX, mouseY, alpha);
                                    settingY += ColorSettingRenderer.measureHeight(colorSetting, settingW) + this.getSettingsGap(setting, nextSetting);
                                    continue;
                                }
                                if (setting instanceof SelectSetting) {
                                    SelectSetting selectSetting = (SelectSetting)setting;
                                    settingKey = moduleKey + ":" + selectSetting.getName();
                                    expanded = this.selectExpanded.getOrDefault(settingKey, false);
                                    expandProgress = this.getSelectExpandProgress(settingKey, expanded);
                                    SelectSettingRenderer.render(context, animation, settingKey, selectSetting, settingX, settingY, settingW, mouseX, mouseY, alpha, expandProgress);
                                    settingY += SelectSettingRenderer.measureHeight(selectSetting, settingW, expandProgress) + this.getSettingsGap(setting, nextSetting);
                                    continue;
                                }
                                if (!(setting instanceof MultiSelectSetting)) continue;
                                MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
                                settingKey = moduleKey + ":" + multiSelectSetting.getName();
                                expanded = this.multiSelectExpanded.getOrDefault(settingKey, false);
                                expandProgress = this.getMultiSelectExpandProgress(settingKey, expanded);
                                MultiSelectSettingRenderer.render(context, animation, settingKey, multiSelectSetting, settingX, settingY, settingW, mouseX, mouseY, alpha, expandProgress);
                                settingY += MultiSelectSettingRenderer.measureHeight(multiSelectSetting, settingW, expandProgress) + this.getSettingsGap(setting, nextSetting);
                            }
                        }
                        columnBottomY[col] = y + moduleCardHeight + 8.0f;
                        continue;
                    }
                    columnBottomY[col] = y + moduleCardHeight + 8.0f;
                }
                context.method_44380();
                if (maxScroll <= 0.01f) {
                    this.tabScrollbars.remove((Object)tab);
                } else {
                    float trackX = panelX + panelWidth - 4.0f - 2.0f;
                    float trackY = viewportTop + 2.0f;
                    float trackH = Math.max(8.0f, viewportHeight - 4.0f);
                    float contentHeight = viewportHeight + maxScroll;
                    float thumbH = Math.max(18.0f, trackH * (viewportHeight / Math.max(1.0f, contentHeight)));
                    thumbH = Math.min(trackH, thumbH);
                    float travel = Math.max(0.0f, trackH - thumbH);
                    float progress = maxScroll <= 0.0f ? 0.0f : this.clamp(scrollValue / maxScroll, 0.0f, 1.0f);
                    float thumbY = trackY + travel * progress;
                    boolean scrollbarHoveredRaw = this.isHovered(mouseX, mouseY, trackX - 2.0f, trackY, 6.0f, trackH);
                    float scrollbarHover = animation.animate01("modules:scrollbar:hover:" + tab.name(), scrollbarHoveredRaw ? 1.0f : 0.0f, 14.0f, 0.0f);
                    boolean scrollbarPressed = this.scrollbarDragging && tab == this.displayTab;
                    float scrollbarPress = animation.animate01("modules:scrollbar:press:" + tab.name(), scrollbarPressed ? 1.0f : 0.0f, 18.0f, 0.0f);
                    float trackAlpha = alpha * (0.2f + scrollbarHover * 0.14f + scrollbarPress * 0.1f);
                    int trackColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, trackAlpha);
                    int thumbBase = ColorUtil.interpolate(ClientColors.SCROLLBAR, ClientColors.TEXT_PRIMARY, scrollbarHover * 0.32f + scrollbarPress * 0.18f);
                    int thumbColor = ColorUtil.multiplyAlpha(thumbBase, alpha * (0.68f + scrollbarHover * 0.16f + scrollbarPress * 0.16f));
                    RenderUtil.drawRoundRect(context, trackX, trackY, 2.0f, trackH, 1.0f, trackColor);
                    RenderUtil.drawRoundRect(context, trackX, thumbY, 2.0f, thumbH, 1.0f, thumbColor);
                    this.tabScrollbars.put(tab, new ScrollbarState(trackX, trackY, 2.0f, trackH, thumbY, thumbH, maxScroll));
                }
            }
        }
    }

    public boolean handleClick(class_11909 click, float panelX, float panelY, float panelWidth, float panelHeight, CategoryBarSection.TopTab activeTab) {
        List<Module> modules;
        if (this.isColorPopupActive()) {
            if (!this.colorPopupOpen) {
                return true;
            }
            float[] popupRect = this.getColorPopupRect();
            float px = popupRect[0];
            float py = popupRect[1];
            float pw = popupRect[2];
            float ph = popupRect[3];
            if (!this.isHovered(click.comp_4798(), click.comp_4799(), px, py, pw, ph)) {
                this.colorPopupOpen = false;
                this.colorDragMode = ColorDragMode.NONE;
                return true;
            }
            if (click.method_74245() == 0) {
                ColorPopupLayout layout = this.getColorPopupLayout(px, py);
                if (this.isHovered(click.comp_4798(), click.comp_4799(), layout.closeX, layout.closeY, layout.closeW, layout.closeH)) {
                    this.colorPopupOpen = false;
                    this.colorDragMode = ColorDragMode.NONE;
                    return true;
                }
                int presetIndex = this.hitColorPresetIndex(layout, click.comp_4798(), click.comp_4799());
                if (presetIndex >= 0) {
                    this.applyColorPreset(presetIndex);
                    return true;
                }
                if (this.isHovered(click.comp_4798(), click.comp_4799(), layout.copyX, layout.infoY + 4.0f, 7.0f, 7.0f)) {
                    this.copyCurrentColor();
                    return true;
                }
                if (this.isHovered(click.comp_4798(), click.comp_4799(), layout.loadX, layout.infoY + 4.0f, 7.0f, 7.0f)) {
                    this.pasteCurrentColor();
                    return true;
                }
                if (this.isHovered(click.comp_4798(), click.comp_4799(), layout.paletteX, layout.paletteY, layout.paletteSize, layout.paletteSize)) {
                    this.colorDragMode = ColorDragMode.PALETTE;
                    this.updateColorFromMouse(ColorDragMode.PALETTE, click.comp_4798(), click.comp_4799(), layout);
                    return true;
                }
                if (this.isHovered(click.comp_4798(), click.comp_4799(), layout.hueX, layout.paletteY, layout.barW, layout.paletteSize)) {
                    this.colorDragMode = ColorDragMode.HUE;
                    this.updateColorFromMouse(ColorDragMode.HUE, click.comp_4798(), click.comp_4799(), layout);
                    return true;
                }
                if (this.isHovered(click.comp_4798(), click.comp_4799(), layout.alphaX, layout.paletteY, layout.barW, layout.paletteSize)) {
                    this.colorDragMode = ColorDragMode.ALPHA;
                    this.updateColorFromMouse(ColorDragMode.ALPHA, click.comp_4798(), click.comp_4799(), layout);
                    return true;
                }
            }
            return true;
        }
        if (this.isBindPopupActive()) {
            if (!this.bindPopupOpen) {
                return true;
            }
            float[] popupRect = this.getPopupRect(panelX, panelY, panelWidth, panelHeight);
            float px = popupRect[0];
            float py = popupRect[1];
            float pw = popupRect[2];
            float ph = popupRect[3];
            if (!this.isHovered(click.comp_4798(), click.comp_4799(), px, py, pw, ph)) {
                this.bindPopupOpen = false;
                this.bindCapture = false;
                this.clearCaptureState();
                return true;
            }
            if (this.bindCapture) {
                this.onCapturePress(Pyz1k.mouseButtonToBindCode(click.method_74245()));
                return true;
            }
            if (click.method_74245() == 0) {
                float keyTargetW;
                float modeY = py + 32.0f;
                float segW = 72.0f;
                float segX = px + pw - 8.0f - segW;
                float segY = modeY - 7.0f;
                float halfW = segW * 0.5f;
                if (this.isHovered(click.comp_4798(), click.comp_4799(), segX, segY, halfW, 14.0f)) {
                    this.getActivePopupBind().setMode(BindMode.HOLD);
                    return true;
                }
                if (this.isHovered(click.comp_4798(), click.comp_4799(), segX + halfW, segY, halfW, 14.0f)) {
                    this.getActivePopupBind().setMode(BindMode.TOGGLE);
                    return true;
                }
                float keyY = py + 52.0f;
                String keyText = this.bindCapture ? "..." : this.formatBindText(this.getActivePopupBind().getKeys());
                float keyW = keyTargetW = this.getPopupKeyTargetWidth(keyText);
                float keyX = px + pw - 8.0f - keyW;
                if (this.isHovered(click.comp_4798(), click.comp_4799(), keyX, keyY - 7.0f, keyW, 14.0f)) {
                    this.bindCapture = true;
                    this.clearCaptureState();
                    return true;
                }
            }
            return true;
        }
        if (this.settingsModule != null && click.method_74245() == 0 && this.isHovered(click.comp_4798(), click.comp_4799(), panelX + 13.0f, panelY + 8.0f, 53.0f, 21.0f)) {
            this.closeSettings();
            return true;
        }
        if (click.method_74245() == 0 && this.handleScrollbarPress((float)click.comp_4798(), (float)click.comp_4799(), activeTab)) {
            return true;
        }
        if (click.method_74245() != 0 && click.method_74245() != 1 && click.method_74245() != 2) {
            return false;
        }
        this.selectedSliderModule = null;
        this.selectedSliderSetting = null;
        Category category = this.toCategory(activeTab);
        if (category == null) {
            return false;
        }
        boolean detail = this.settingsModule != null && this.settingsModule.getCategory() == category;
        List<Module> list = modules = detail ? List.of(this.settingsModule) : this.visibleModules(category);
        if (modules.isEmpty()) {
            return false;
        }
        float contentX = panelX + 10.0f;
        float contentY = panelY + (detail ? 39.0f : 14.0f);
        float contentW = panelWidth - 20.0f;
        float cardWidth = detail ? contentW - 16.0f : (contentW - 8.0f) / 2.0f;
        float viewportTop = contentY;
        float viewportBottom = panelY + panelHeight - 8.0f;
        float viewportHeight = Math.max(1.0f, viewportBottom - viewportTop);
        if (!this.isHovered(click.comp_4798(), click.comp_4799(), contentX, viewportTop, contentW, viewportHeight)) {
            return false;
        }
        float maxScroll = this.computeMaxScroll(modules, cardWidth, viewportHeight, null);
        float scrollValue = this.clamp(this.tabScrollValues.getOrDefault((Object)activeTab, this.tabScrollTargets.getOrDefault((Object)activeTab, Float.valueOf(0.0f))).floatValue(), 0.0f, maxScroll);
        this.tabMaxScroll.put(activeTab, Float.valueOf(maxScroll));
        float[] columnBottomY = new float[2];
        for (int c = 0; c < 2; ++c) {
            columnBottomY[c] = contentY;
        }
        for (int i = 0; i < modules.size(); ++i) {
            int col = detail ? 0 : i % 2;
            float x = detail ? contentX + 8.0f : contentX + (float)col * (cardWidth + 8.0f);
            float y = columnBottomY[col];
            Module module = modules.get(i);
            float moduleCardHeight = this.getModuleCardHeight(module, cardWidth, null);
            float drawY = y - scrollValue;
            if (!(drawY >= viewportBottom) && !(drawY + moduleCardHeight <= viewportTop)) {
                float bindTargetWidth;
                if (!detail) {
                    if (this.isHovered(click.comp_4798(), click.comp_4799(), x, drawY, cardWidth, moduleCardHeight)) {
                        if (click.method_74245() == 1 || click.method_74245() == 0 && this.isHovered(click.comp_4798(), click.comp_4799(), x + cardWidth - 48.0f, drawY + 4.0f, 20.0f, 19.0f)) {
                            this.openSettings(module);
                        } else if (click.method_74245() == 2) {
                            this.bindPopupModule = module;
                            this.bindPopupSetting = null;
                            this.bindPopupOpen = true;
                            this.bindPopupAnchorX = (float)click.comp_4798();
                            this.bindPopupAnchorY = (float)click.comp_4799();
                            this.bindCapture = false;
                            this.clearCaptureState();
                        } else if (click.method_74245() == 0) {
                            module.toggle();
                        }
                        return true;
                    }
                    columnBottomY[col] = y + moduleCardHeight + 8.0f;
                    continue;
                }
                if (click.method_74245() != 0) {
                    return false;
                }
                float toggleX = x + cardWidth - 8.0f - 15.0f;
                float toggleY = drawY + 7.5f;
                if (this.isHovered(click.comp_4798(), click.comp_4799(), toggleX - 1.0f, toggleY - 1.0f, 17.0f, 12.0f)) {
                    module.toggle();
                    return true;
                }
                String bindText = this.formatBindText(module.getBind().getKeys());
                float bindWidth = bindTargetWidth = this.getModuleBindTargetWidth(bindText);
                float bindX = toggleX - 4.0f - bindWidth;
                float bindY = drawY + 8.25f;
                if (this.isHovered(click.comp_4798(), click.comp_4799(), bindX, bindY, bindWidth, 8.5f)) {
                    this.bindPopupModule = module;
                    this.bindPopupSetting = null;
                    this.bindPopupOpen = true;
                    this.bindPopupAnchorX = (float)click.comp_4798();
                    this.bindPopupAnchorY = (float)click.comp_4799();
                    this.bindCapture = false;
                    this.clearCaptureState();
                    return true;
                }
                List<Setting> renderSettings = this.getRenderableSettings(module);
                if (!renderSettings.isEmpty()) {
                    float iconX = x + 8.0f;
                    float titleOffset = (25.0f + RenderUtil.getTextHeight(GFonts.MEDIUM, 9)) / 2.0f - 8.0f;
                    float settingX = iconX;
                    float settingW = Math.max(16.0f, x + cardWidth - 8.0f - settingX);
                    float settingY = drawY + titleOffset + titleOffset + 4.0f;
                    for (int si = 0; si < renderSettings.size(); ++si) {
                        int optionIndex;
                        float expandProgress;
                        boolean expanded;
                        String settingKey;
                        Setting nextSetting;
                        Setting setting = renderSettings.get(si);
                        Setting setting2 = nextSetting = si + 1 < renderSettings.size() ? renderSettings.get(si + 1) : null;
                        if (setting instanceof BooleanSetting) {
                            BooleanSetting moduleBoolean = (BooleanSetting)setting;
                            if (BooleanSettingRenderer.hitKeyIcon(moduleBoolean, settingX, settingY, settingW, click.comp_4798(), click.comp_4799())) {
                                this.bindPopupModule = module;
                                this.bindPopupSetting = moduleBoolean;
                                this.bindPopupOpen = true;
                                this.bindPopupAnchorX = (float)click.comp_4798();
                                this.bindPopupAnchorY = (float)click.comp_4799();
                                this.bindCapture = false;
                                this.clearCaptureState();
                                return true;
                            }
                            if (BooleanSettingRenderer.hitCheckbox(moduleBoolean, settingX, settingY, settingW, click.comp_4798(), click.comp_4799())) {
                                moduleBoolean.toggle();
                                return true;
                            }
                            settingY += BooleanSettingRenderer.measureHeight(moduleBoolean, settingW) + this.getSettingsGap(setting, nextSetting);
                            continue;
                        }
                        if (setting instanceof SliderSetting) {
                            SliderSetting sliderSetting = (SliderSetting)setting;
                            if (click.method_74245() == 0 && SliderSettingRenderer.hitTrack(sliderSetting, settingX, settingY, settingW, click.comp_4798(), click.comp_4799())) {
                                SliderSettingRenderer.setFromMouse(sliderSetting, settingX, settingY, settingW, click.comp_4798());
                                this.draggingSliderModule = module;
                                this.draggingSliderSetting = sliderSetting;
                                this.selectedSliderModule = module;
                                this.selectedSliderSetting = sliderSetting;
                                this.draggingSliderX = settingX;
                                this.draggingSliderY = settingY;
                                this.draggingSliderW = settingW;
                                return true;
                            }
                            settingY += SliderSettingRenderer.measureHeight(sliderSetting, settingW) + this.getSettingsGap(setting, nextSetting);
                            continue;
                        }
                        if (setting instanceof ColorSetting) {
                            ColorSetting colorSetting = (ColorSetting)setting;
                            if (ColorSettingRenderer.hitThemeButton(colorSetting, settingX, settingY, settingW, click.comp_4798(), click.comp_4799())) {
                                this.colorPopupModule = module;
                                this.colorPopupSetting = colorSetting;
                                this.colorPopupOpen = true;
                                this.colorPopupAnchorX = (float)click.comp_4798();
                                this.colorPopupAnchorY = (float)click.comp_4799();
                                this.colorDragMode = ColorDragMode.NONE;
                                return true;
                            }
                            settingY += ColorSettingRenderer.measureHeight(colorSetting, settingW) + this.getSettingsGap(setting, nextSetting);
                            continue;
                        }
                        if (setting instanceof SelectSetting) {
                            SelectSetting selectSetting = (SelectSetting)setting;
                            settingKey = module.getClass().getName() + ":" + selectSetting.getName();
                            expanded = this.selectExpanded.getOrDefault(settingKey, false);
                            expandProgress = this.getSelectExpandProgress(settingKey, expanded);
                            if (SelectSettingRenderer.hitSelectBox(selectSetting, settingX, settingY, settingW, click.comp_4798(), click.comp_4799())) {
                                this.selectExpanded.put(settingKey, !expanded);
                                return true;
                            }
                            optionIndex = SelectSettingRenderer.hitOptionIndex(selectSetting, settingX, settingY, settingW, click.comp_4798(), click.comp_4799(), expandProgress);
                            if (optionIndex >= 0 && optionIndex < selectSetting.getOptions().size()) {
                                selectSetting.setIndex(optionIndex);
                                return true;
                            }
                            settingY += SelectSettingRenderer.measureHeight(selectSetting, settingW, expandProgress) + this.getSettingsGap(setting, nextSetting);
                            continue;
                        }
                        if (!(setting instanceof MultiSelectSetting)) continue;
                        MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
                        settingKey = module.getClass().getName() + ":" + multiSelectSetting.getName();
                        expanded = this.multiSelectExpanded.getOrDefault(settingKey, false);
                        expandProgress = this.getMultiSelectExpandProgress(settingKey, expanded);
                        if (MultiSelectSettingRenderer.hitSelectBox(multiSelectSetting, settingX, settingY, settingW, click.comp_4798(), click.comp_4799())) {
                            this.multiSelectExpanded.put(settingKey, !expanded);
                            return true;
                        }
                        optionIndex = MultiSelectSettingRenderer.hitOptionIndex(multiSelectSetting, settingX, settingY, settingW, click.comp_4798(), click.comp_4799(), expandProgress);
                        if (optionIndex >= 0 && optionIndex < multiSelectSetting.getOptions().size()) {
                            multiSelectSetting.toggle(multiSelectSetting.getOptions().get(optionIndex));
                            return true;
                        }
                        settingY += MultiSelectSettingRenderer.measureHeight(multiSelectSetting, settingW, expandProgress) + this.getSettingsGap(setting, nextSetting);
                    }
                }
                columnBottomY[col] = y + moduleCardHeight + 8.0f;
                continue;
            }
            columnBottomY[col] = y + moduleCardHeight + 8.0f;
        }
        return false;
    }

    public boolean handleKeyPressed(class_11908 key) {
        if (this.isBindPopupActive() && this.bindPopupOpen) {
            int code = key.comp_4795();
            if (this.bindCapture) {
                if (code == 256) {
                    this.getActivePopupBind().clearKeys();
                    this.bindCapture = false;
                    this.clearCaptureState();
                    return true;
                }
                this.onCapturePress(code);
                return true;
            }
            if (code == 256) {
                this.bindPopupOpen = false;
                this.bindCapture = false;
                this.clearCaptureState();
                return true;
            }
            return false;
        }
        int code = key.comp_4795();
        if (code == 256 && this.settingsModule != null) {
            this.closeSettings();
            return true;
        }
        if (this.selectedSliderSetting != null) {
            if (code == 263) {
                this.selectedSliderSetting.setValue(((Float)this.selectedSliderSetting.getValue()).floatValue() - this.selectedSliderSetting.getStep());
                return true;
            }
            if (code == 262) {
                this.selectedSliderSetting.setValue(((Float)this.selectedSliderSetting.getValue()).floatValue() + this.selectedSliderSetting.getStep());
                return true;
            }
        }
        return false;
    }

    public boolean handleKeyReleased(class_11908 key) {
        if (this.isBindPopupActive() && this.bindPopupOpen && this.bindCapture) {
            this.onCaptureRelease(key.comp_4795());
            return true;
        }
        return false;
    }

    public boolean handleMouseReleased(int button) {
        if (button == 0 && this.draggingSliderSetting != null) {
            this.draggingSliderModule = null;
            this.draggingSliderSetting = null;
            return true;
        }
        if (button == 0 && this.colorDragMode != ColorDragMode.NONE) {
            this.colorDragMode = ColorDragMode.NONE;
            return true;
        }
        if (button == 0 && this.scrollbarDragging) {
            this.scrollbarDragging = false;
            return true;
        }
        if (this.isBindPopupActive() && this.bindPopupOpen && this.bindCapture) {
            this.onCaptureRelease(Pyz1k.mouseButtonToBindCode(button));
            return true;
        }
        return false;
    }

    public boolean handleScroll(double mouseX, double mouseY, double verticalAmount, float panelX, float panelY, float panelWidth, float panelHeight, CategoryBarSection.TopTab activeTab) {
        if (!this.isBindPopupActive() && !this.isColorPopupActive() && verticalAmount != 0.0) {
            List<Module> modules;
            float contentH;
            float contentW;
            float contentX = panelX + 10.0f;
            boolean detail = this.settingsModule != null;
            float contentY = panelY + (detail ? 39.0f : 14.0f);
            if (!this.isHovered(mouseX, mouseY, contentX, contentY, contentW = panelWidth - 20.0f, contentH = panelHeight - (detail ? 39.0f : 14.0f) - 8.0f)) {
                return false;
            }
            Category category = this.toCategory(activeTab);
            if (category == null) {
                return false;
            }
            List<Module> list = modules = detail ? List.of(this.settingsModule) : this.visibleModules(category);
            if (modules.isEmpty()) {
                return false;
            }
            float cardWidth = detail ? contentW - 16.0f : (contentW - 8.0f) / 2.0f;
            float viewportHeight = Math.max(1.0f, contentH);
            float maxScroll = this.computeMaxScroll(modules, cardWidth, viewportHeight, null);
            this.tabMaxScroll.put(activeTab, Float.valueOf(maxScroll));
            if (maxScroll <= 0.01f) {
                this.tabScrollTargets.put(activeTab, Float.valueOf(0.0f));
                this.tabScrollValues.put(activeTab, Float.valueOf(0.0f));
                return true;
            }
            float currentTarget = this.tabScrollTargets.getOrDefault((Object)activeTab, this.tabScrollValues.getOrDefault((Object)activeTab, Float.valueOf(0.0f))).floatValue();
            float nextTarget = this.clamp(currentTarget - (float)verticalAmount * 22.0f, 0.0f, maxScroll);
            this.tabScrollTargets.put(activeTab, Float.valueOf(nextTarget));
            return true;
        }
        return false;
    }

    private boolean handleScrollbarPress(float mouseX, float mouseY, CategoryBarSection.TopTab activeTab) {
        ScrollbarState state = this.tabScrollbars.get((Object)activeTab);
        if (state != null && !(state.maxScroll <= 0.01f)) {
            if (!this.isHovered(mouseX, mouseY, state.trackX - 2.0f, state.trackY, state.trackW + 4.0f, state.trackH)) {
                return false;
            }
            this.scrollbarDragging = true;
            if (mouseY >= state.thumbY && mouseY <= state.thumbY + state.thumbH) {
                this.scrollbarDragOffsetY = mouseY - state.thumbY;
            } else {
                this.scrollbarDragOffsetY = state.thumbH * 0.5f;
                float thumbTop = mouseY - this.scrollbarDragOffsetY;
                this.setScrollFromThumbTop(activeTab, state, thumbTop);
            }
            return true;
        }
        return false;
    }

    private void updateScrollbarDrag(CategoryBarSection.TopTab activeTab, float mouseY) {
        if (this.scrollbarDragging) {
            ScrollbarState state = this.tabScrollbars.get((Object)activeTab);
            if (state != null && !(state.maxScroll <= 0.01f)) {
                class_310 mc = class_310.method_1551();
                if (mc != null && mc.method_22683() != null) {
                    long handle = mc.method_22683().method_4490();
                    if (GLFW.glfwGetMouseButton((long)handle, (int)0) != 1) {
                        this.scrollbarDragging = false;
                    } else {
                        float thumbTop = mouseY - this.scrollbarDragOffsetY;
                        this.setScrollFromThumbTop(activeTab, state, thumbTop);
                    }
                } else {
                    this.scrollbarDragging = false;
                }
            } else {
                this.scrollbarDragging = false;
            }
        }
    }

    private void setScrollFromThumbTop(CategoryBarSection.TopTab tab, ScrollbarState state, float thumbTop) {
        float travel = Math.max(0.0f, state.trackH - state.thumbH);
        if (!(travel <= 0.001f) && !(state.maxScroll <= 0.001f)) {
            float clampedThumbTop = this.clamp(thumbTop, state.trackY, state.trackY + travel);
            float progress = (clampedThumbTop - state.trackY) / travel;
            float targetScroll = this.clamp(progress * state.maxScroll, 0.0f, state.maxScroll);
            this.tabScrollTargets.put(tab, Float.valueOf(targetScroll));
            this.tabScrollValues.put(tab, Float.valueOf(targetScroll));
        } else {
            this.tabScrollTargets.put(tab, Float.valueOf(0.0f));
            this.tabScrollValues.put(tab, Float.valueOf(0.0f));
        }
    }

    public void requestClosePopup() {
        this.bindPopupOpen = false;
        this.bindCapture = false;
        this.colorPopupOpen = false;
        this.colorPopupTitleOverride = null;
        this.colorDragMode = ColorDragMode.NONE;
        this.draggingSliderModule = null;
        this.draggingSliderSetting = null;
        this.selectedSliderModule = null;
        this.selectedSliderSetting = null;
        this.clearCaptureState();
    }

    public boolean isPopupOpen() {
        return this.isBindPopupOpen() || this.isColorPopupOpen();
    }

    public boolean isBindPopupOpen() {
        return this.isBindPopupActive() && this.bindPopupOpen;
    }

    public boolean isColorPopupOpen() {
        return this.isColorPopupActive() && this.colorPopupOpen;
    }

    public void openStandaloneColorPicker(ColorSetting setting, String title, float anchorX, float anchorY) {
        this.colorPopupModule = null;
        this.colorPopupSetting = setting;
        this.colorPopupTitleOverride = title;
        this.colorPopupOpen = true;
        this.colorPopupAnchorX = anchorX;
        this.colorPopupAnchorY = anchorY;
        this.colorDragMode = ColorDragMode.NONE;
    }

    public void renderOverlay(class_332 context, UiAnimationEngine animation, int mouseX, int mouseY, float alpha) {
        this.renderColorPopup(context, animation, mouseX, mouseY, alpha);
    }

    private void renderBindPopup(class_332 context, UiAnimationEngine animation, float panelX, float panelY, float panelWidth, float panelHeight, int mouseX, int mouseY, float alpha) {
        if (this.isBindPopupActive()) {
            String popupKey = this.getPopupAnimKey();
            float target = this.bindPopupOpen ? 1.0f : 0.0f;
            float popupAlpha = animation.animate01("bind:popup:alpha:" + popupKey, target, 14.0f, 0.0f);
            if (popupAlpha <= 0.01f) {
                if (!this.bindPopupOpen) {
                    this.bindPopupModule = null;
                    this.bindPopupSetting = null;
                }
            } else {
                float[] popupRect = this.getPopupRect(panelX, panelY, panelWidth, panelHeight);
                float x = popupRect[0];
                float y = popupRect[1];
                float w = popupRect[2];
                float h = popupRect[3];
                float a = alpha * popupAlpha;
                RenderUtil.drawRoundRect(context, x, y, w, h, 6.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, a));
                RenderUtil.drawBorder(context, x, y, w, h, 6.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND_OUTLINE, a), 1.0f, 0.0f, 0.5f);
                float titleY = y + 8.5f;
                float titleIconX = x + 8.0f;
                String badgeText = "KEYBIND";
                float badgeTextW = RenderUtil.getTextWidth(badgeText, GFonts.BOLD, 6);
                float badgeW = 15.0f + badgeTextW + 6.0f;
                float badgeX = x + w - 8.0f - badgeW;
                float badgeY = y + 7.0f;
                RenderUtil.drawIcon(context, GIcons.KEYBOARD, titleIconX, titleY, 8.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, a));
                float titleTextX = titleIconX + 12.0f;
                float titleMaxW = Math.max(6.0f, badgeX - 4.0f - titleTextX);
                int titleScX1 = (int)Math.floor(titleTextX);
                int titleScY1 = (int)Math.floor(y - 1.0f);
                int titleScX2 = (int)Math.ceil(titleTextX + titleMaxW);
                int titleScY2 = (int)Math.ceil(y + 16.0f);
                context.method_44379(titleScX1, titleScY1, titleScX2, titleScY2);
                RenderUtil.drawText(context, this.getPopupTitle(), titleTextX, titleY + 1.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, a), GFonts.SEMIBOLD, 9);
                context.method_44380();
                int badgeBg = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, a * 0.3f);
                RenderUtil.drawRoundRect(context, badgeX, badgeY, badgeW, 11.0f, 3.0f, badgeBg);
                RenderUtil.drawImage(context, "textures/gui/icons/property.png", badgeX + 6.0f, badgeY + 2.5f, 6.0f, 6.0f, 0.0f, ColorUtil.multiplyAlpha(ColorUtil.withAlpha(ClientColors.CATEGORY_ACCENT, 200), a), 1.0f);
                RenderUtil.drawText(context, badgeText, badgeX + 15.0f, badgeY + 3.5f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, a), GFonts.BOLD, 6);
                float modeY = y + 32.0f;
                RenderUtil.drawText(context, "Bind mode", x + 8.0f, modeY - 3.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, a), GFonts.SEMIBOLD, 8);
                float segW = 72.0f;
                float segX = x + w - 8.0f - segW;
                float segY = modeY - 7.0f;
                int segBg = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, a);
                int segBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, a);
                RenderUtil.drawRoundRect(context, segX, segY, segW, 14.0f, 3.0f, segBg);
                RenderUtil.drawBorder(context, segX, segY, segW, 14.0f, 3.0f, segBorder, 0.0f, 0.5f, 1.0f);
                float modeAnim = animation.animate01("bind:mode:" + popupKey, this.getActivePopupBind().getMode() == BindMode.TOGGLE ? 1.0f : 0.0f, 22.0f, this.getActivePopupBind().getMode() == BindMode.TOGGLE ? 1.0f : 0.0f);
                float halfW = segW * 0.5f;
                float activeX = segX + halfW * modeAnim;
                RenderUtil.drawRoundRect(context, activeX, segY, halfW, 14.0f, 3.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, a));
                RenderUtil.drawRoundRect(context, segX + halfW - 0.5f, segY + 1.5f, 1.0f, 11.0f, 0.5f, ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, a));
                int holdColor = ColorUtil.multiplyAlpha(ColorUtil.interpolate(ClientColors.TEXT_PRIMARY, ClientColors.CATEGORY_INACTIVE, modeAnim), a);
                int toggleColor = ColorUtil.multiplyAlpha(ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, modeAnim), a);
                this.drawModeText(context, segX + 1.0f, segY - 1.0f, halfW, "HOLD", holdColor);
                this.drawModeText(context, segX + 1.0f + halfW, segY - 1.0f, halfW, "TOGGLE", toggleColor);
                float keyY = y + 52.0f;
                RenderUtil.drawText(context, "Key", x + 8.0f, keyY - 2.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, a), GFonts.SEMIBOLD, 8);
                String keyText = this.bindCapture ? (this.captureSelectedCodes.isEmpty() ? "..." : this.formatBindText(this.captureSelectedCodes)) : this.formatBindText(this.getActivePopupBind().getKeys());
                float keyTargetW = this.getPopupKeyTargetWidth(keyText);
                float keyW = this.animateWidth(animation, this.popupKeyWidths, "popup:key:w:" + popupKey, keyTargetW, 20.0f);
                float keyTextReveal = this.animateTextRevealAfterWidth(animation, this.popupKeyTexts, this.popupKeyTextUnlocked, popupKey, "popup:key:text:reveal:" + popupKey, keyText, keyW, keyTargetW);
                float keyRightX = x + w - 8.0f;
                float keyX = keyRightX - keyW;
                boolean keyHoveredRaw = this.isHovered(mouseX, mouseY, keyX, keyY - 7.0f, keyW, 14.0f);
                float keyHover = animation.animate01("popup:key:hover:" + popupKey, keyHoveredRaw ? 1.0f : 0.0f, 12.0f, 0.0f);
                int keyBgBase = ColorUtil.interpolate(ClientColors.SETTINGS_BACKGROUND, ClientColors.CATEGORY_ACCENT, keyHover * 0.22f);
                int keyBg = ColorUtil.multiplyAlpha(keyBgBase, a);
                int keyBorder = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, a);
                float keyYDraw = keyY - 7.0f;
                RenderUtil.drawRoundRect(context, keyX, keyYDraw, keyW, 14.0f, 3.0f, keyBg);
                RenderUtil.drawBorder(context, keyX, keyYDraw, keyW, 14.0f, 3.0f, keyBorder, 0.0f, 0.5f, 1.0f);
                float keyIconX = keyX + 4.0f;
                float keyIconY = keyY - 7.0f + 3.75f;
                int keyIconColor = ColorUtil.multiplyAlpha(ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, keyHover * 0.55f), a);
                RenderUtil.drawIcon(context, GIcons.KEYBOARD, keyIconX, keyIconY, 6.5f, keyIconColor);
                float keyTextY = keyY + (14.0f + RenderUtil.getTextHeight(GFonts.BOLD, 5)) / 2.0f - 5.1f - 7.0f;
                int keyTextColor = ColorUtil.multiplyAlpha(ColorUtil.interpolate(ClientColors.CATEGORY_INACTIVE, ClientColors.TEXT_PRIMARY, keyHover * 0.6f), a * keyTextReveal);
                RenderUtil.drawText(context, keyText, keyIconX + 6.5f + 3.0f, keyTextY - 0.5f, keyTextColor, GFonts.BOLD, 5);
            }
        }
    }

    private void renderColorPopup(class_332 context, UiAnimationEngine animation, int mouseX, int mouseY, float alpha) {
        if (this.isColorPopupActive()) {
            String popupKey = this.getColorPopupAnimKey();
            float target = this.colorPopupOpen ? 1.0f : 0.0f;
            float popupAlpha = animation.animate01("color:popup:alpha:" + popupKey, target, 14.0f, 0.0f);
            if (popupAlpha <= 0.01f) {
                if (!this.colorPopupOpen) {
                    this.colorPopupModule = null;
                    this.colorPopupSetting = null;
                    this.colorPopupTitleOverride = null;
                    this.colorDragMode = ColorDragMode.NONE;
                }
            } else {
                float[] popupRect = this.getColorPopupRect();
                float x = popupRect[0];
                float y = popupRect[1];
                float w = popupRect[2];
                float h = popupRect[3];
                float a = alpha * popupAlpha;
                ColorPopupLayout layout = this.getColorPopupLayout(x, y);
                context.method_25294(0, 0, class_310.method_1551().method_22683().method_4486(), class_310.method_1551().method_22683().method_4502(), ColorUtil.multiplyAlpha(ColorUtil.rgb(3, 3, 4), a * 0.42f));
                RenderUtil.drawShadow(context, x, y, w, h, 8.0f, ColorUtil.multiplyAlpha(ClientColors.MENU_SHADOW, a), 1.0f, 10.0f, 0.0f, 0.0f);
                RenderUtil.drawRoundRect(context, x, y, w, h, 6.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, a));
                RenderUtil.drawBorder(context, x, y, w, h, 6.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND_OUTLINE, a), 1.0f, 0.0f, 0.5f);
                RenderUtil.drawImage(context, "textures/gui/icons/theme.png", x + 8.0f, y + 7.0f, 7.0f, 7.0f, 0.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, a), 0.0f);
                RenderUtil.drawText(context, this.getColorPopupTitle(), x + 8.0f + 11.0f, y + 6.8f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, a), GFonts.SEMIBOLD, 9);
                this.renderColorPalette(context, layout, a);
                this.renderColorBar(context, layout.hueX, layout.paletteY, layout.barW, layout.paletteSize, a, true);
                this.renderColorBar(context, layout.alphaX, layout.paletteY, layout.barW, layout.paletteSize, a, false);
                this.renderColorHandles(context, layout, a);
                this.renderColorInfo(context, layout, a);
                this.renderColorPresets(context, layout, mouseX, mouseY, a);
                boolean closeHovered = this.isHovered(mouseX, mouseY, layout.closeX, layout.closeY, layout.closeW, layout.closeH);
                float closeHover = animation.animate01("color:popup:close:hover:" + popupKey, closeHovered ? 1.0f : 0.0f, 14.0f, 0.0f);
                int closeBg = ColorUtil.multiplyAlpha(ColorUtil.interpolate(ClientColors.SETTINGS_BACKGROUND, ClientColors.CATEGORY_ACCENT, closeHover * 0.22f), a);
                RenderUtil.drawRoundRect(context, layout.closeX, layout.closeY, layout.closeW, layout.closeH, 4.0f, closeBg);
                RenderUtil.drawBorder(context, layout.closeX, layout.closeY, layout.closeW, layout.closeH, 4.0f, ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, a), 0.0f, 0.6f, 1.0f);
                float closeTextX = layout.closeX + (layout.closeW - RenderUtil.getTextWidth("Close", GFonts.BOLD, 7)) / 2.0f;
                float closeTextY = layout.closeY + (layout.closeH - RenderUtil.getTextHeight(GFonts.BOLD, 7)) / 2.0f + 1.0f;
                RenderUtil.drawText(context, "Close", closeTextX, closeTextY, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, a), GFonts.BOLD, 7);
            }
        }
    }

    private void drawModeText(class_332 context, float x, float y, float width, String text, int color) {
        float textX = x + (width - RenderUtil.getTextWidth(text, GFonts.BOLD, 6)) / 2.0f;
        float textY = y + (14.0f + RenderUtil.getTextHeight(GFonts.BOLD, 6)) / 2.0f - 4.6f;
        RenderUtil.drawText(context, text, textX, textY, color, GFonts.BOLD, 6);
    }

    private float[] getPopupRect(float panelX, float panelY, float panelWidth, float panelHeight) {
        class_310 mc = class_310.method_1551();
        float screenWidth = mc.method_22683().method_4486();
        float screenHeight = mc.method_22683().method_4502();
        float x = Math.min(panelX + panelWidth + 8.0f, screenWidth - 156.0f - 6.0f);
        float y = this.clamp(panelY + (panelHeight - 67.0f) * 0.5f, 6.0f, screenHeight - 67.0f - 6.0f);
        return new float[]{x, y, 156.0f, 67.0f};
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean isBindPopupActive() {
        return this.bindPopupModule != null || this.bindPopupSetting != null;
    }

    private BindData getActivePopupBind() {
        if (this.bindPopupSetting != null) {
            return this.bindPopupSetting.getBind();
        }
        return this.bindPopupModule != null ? this.bindPopupModule.getBind() : null;
    }

    private String getPopupAnimKey() {
        if (this.bindPopupSetting != null && this.bindPopupModule != null) {
            return this.bindPopupModule.getClass().getName() + ":setting:" + this.bindPopupSetting.getName();
        }
        return this.bindPopupModule != null ? this.bindPopupModule.getClass().getName() : "popup:none";
    }

    private String getPopupTitle() {
        if (this.bindPopupSetting != null) {
            return this.bindPopupSetting.getName();
        }
        return this.bindPopupModule == null ? "" : this.bindPopupModule.getName();
    }

    private boolean isColorPopupActive() {
        return this.colorPopupSetting != null;
    }

    private String getColorPopupAnimKey() {
        if (!this.isColorPopupActive()) {
            return "color:none";
        }
        return this.colorPopupModule != null ? this.colorPopupModule.getClass().getName() + ":" + this.colorPopupSetting.getName() : "standalone:" + (this.colorPopupTitleOverride == null ? this.colorPopupSetting.getName() : this.colorPopupTitleOverride);
    }

    private String getColorPopupTitle() {
        if (this.colorPopupTitleOverride != null && !this.colorPopupTitleOverride.isBlank()) {
            return this.colorPopupTitleOverride;
        }
        return this.colorPopupSetting == null ? "" : this.colorPopupSetting.getName();
    }

    private float[] getColorPopupRect() {
        class_310 mc = class_310.method_1551();
        if (mc != null && mc.method_22683() != null) {
            float screenW = mc.method_22683().method_4486();
            float screenH = mc.method_22683().method_4502();
            float x = (screenW - 170.0f) * 0.5f;
            float y = (screenH - 194.0f) * 0.5f;
            return new float[]{x, y, 170.0f, 194.0f};
        }
        return new float[]{this.colorPopupAnchorX, this.colorPopupAnchorY, 170.0f, 194.0f};
    }

    private ColorPopupLayout getColorPopupLayout(float x, float y) {
        float paletteX = x + 8.0f;
        float paletteY = y + 20.0f;
        float hueX = paletteX + 110.0f + 6.0f;
        float alphaX = hueX + 8.0f + 4.0f;
        float infoY = paletteY + 110.0f + 8.0f;
        float presetsY = infoY + 14.0f + 7.0f;
        float closeY = presetsY + 7.0f + 10.0f;
        float infoW = 154.0f;
        float rightInset = 6.0f;
        float iconGap = 9.0f;
        float loadX = x + 170.0f - 8.0f - rightInset - 7.0f;
        float copyX = loadX - iconGap;
        float closeX = x + 8.0f + 3.0f;
        float closeW = infoW - 6.0f;
        return new ColorPopupLayout(paletteX, paletteY, 110.0f, hueX, alphaX, 8.0f, infoY, presetsY, closeX, closeY, closeW, 16.0f, copyX, loadX);
    }

    private void renderColorPalette(class_332 context, ColorPopupLayout layout, float alpha) {
        if (this.colorPopupSetting != null) {
            float hue = this.colorPopupSetting.getHue();
            int pure = 0xFF000000 | Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
            QuadRadiusState radius = new QuadRadiusState(5.0f);
            RenderUtil.drawRoundRectGradientQuad(context, layout.paletteX, layout.paletteY, layout.paletteSize, layout.paletteSize, radius, ColorUtil.multiplyAlpha(ColorUtil.rgb(255, 255, 255), alpha), ColorUtil.multiplyAlpha(ColorUtil.rgb(255, 255, 255), alpha), ColorUtil.multiplyAlpha(pure, alpha), ColorUtil.multiplyAlpha(pure, alpha), 1.0f);
            RenderUtil.drawRoundRectGradientQuad(context, layout.paletteX, layout.paletteY, layout.paletteSize, layout.paletteSize, radius, ColorUtil.multiplyAlpha(ColorUtil.rgba(0, 0, 0, 0), alpha), ColorUtil.multiplyAlpha(ColorUtil.rgb(0, 0, 0), alpha), ColorUtil.multiplyAlpha(ColorUtil.rgb(0, 0, 0), alpha), ColorUtil.multiplyAlpha(ColorUtil.rgba(0, 0, 0, 0), alpha), 1.0f);
            RenderUtil.drawBorder(context, layout.paletteX, layout.paletteY, layout.paletteSize, layout.paletteSize, 5.0f, ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha), 0.0f, 0.5f, 1.0f);
        }
    }

    private void renderColorBar(class_332 context, float x, float y, float width, float height, float alpha, boolean hueBar) {
        RenderUtil.drawRoundRect(context, x, y, width, height, 4.0f, ColorUtil.multiplyAlpha(ColorUtil.rgb(8, 8, 10), alpha));
        float innerX = x + 1.0f;
        float innerY = y + 1.0f;
        float innerW = width - 2.0f;
        float innerH = height - 2.0f;
        if (hueBar) {
            for (int row = 0; row < (int)innerH; ++row) {
                float t = (float)row / Math.max(1.0f, innerH - 1.0f);
                int color = 0xFF000000 | Color.HSBtoRGB(t, 1.0f, 1.0f) & 0xFFFFFF;
                for (int col = 0; col < (int)innerW; ++col) {
                    if (!this.isInsideRoundedRect((float)col + 0.5f, (float)row + 0.5f, innerW, innerH, 3.0f)) continue;
                    context.method_25294(Math.round(innerX + (float)col), Math.round(innerY + (float)row), Math.round(innerX + (float)col + 1.0f), Math.round(innerY + (float)row + 1.0f), ColorUtil.multiplyAlpha(color, alpha));
                }
            }
        } else {
            int rgb = this.colorPopupSetting == null ? ClientColors.CATEGORY_ACCENT : this.colorPopupSetting.getColor() & 0xFFFFFF;
            for (int row = 0; row < (int)innerH; ++row) {
                float t = (float)row / Math.max(1.0f, innerH - 1.0f);
                int color = ColorUtil.interpolate(ColorUtil.withAlpha(rgb, 255), ColorUtil.withAlpha(rgb, 0), t);
                for (int col = 0; col < (int)innerW; ++col) {
                    if (!this.isInsideRoundedRect((float)col + 0.5f, (float)row + 0.5f, innerW, innerH, 3.0f)) continue;
                    context.method_25294(Math.round(innerX + (float)col), Math.round(innerY + (float)row), Math.round(innerX + (float)col + 1.0f), Math.round(innerY + (float)row + 1.0f), ColorUtil.multiplyAlpha(color, alpha));
                }
            }
        }
        RenderUtil.drawBorder(context, x, y, width, height, 4.0f, ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha), 0.0f, 0.5f, 1.0f);
    }

    private void renderColorHandles(class_332 context, ColorPopupLayout layout, float alpha) {
        if (this.colorPopupSetting != null) {
            float paletteInset = 2.0f;
            float paletteInnerSize = layout.paletteSize - paletteInset * 2.0f;
            float paletteHandleRadius = 3.0f;
            float paletteHandleX = layout.paletteX + paletteInset + this.colorPopupSetting.getSaturation() * paletteInnerSize;
            float paletteHandleY = layout.paletteY + paletteInset + (1.0f - this.colorPopupSetting.getBrightness()) * paletteInnerSize;
            paletteHandleX = this.clamp(paletteHandleX, layout.paletteX + paletteInset + paletteHandleRadius, layout.paletteX + layout.paletteSize - paletteInset - paletteHandleRadius);
            paletteHandleY = this.clamp(paletteHandleY, layout.paletteY + paletteInset + paletteHandleRadius, layout.paletteY + layout.paletteSize - paletteInset - paletteHandleRadius);
            RenderUtil.drawBorder(context, paletteHandleX - 3.0f, paletteHandleY - 3.0f, 6.0f, 6.0f, 3.0f, ColorUtil.multiplyAlpha(ColorUtil.rgb(255, 255, 255), alpha), 0.0f, 0.8f, 1.0f);
            RenderUtil.drawRoundRect(context, paletteHandleX - 1.0f, paletteHandleY - 1.0f, 2.0f, 2.0f, 1.0f, ColorUtil.multiplyAlpha(ColorUtil.rgb(255, 255, 255), alpha));
            float barInset = 2.0f;
            float barInnerHeight = layout.paletteSize - barInset * 2.0f;
            float markerHeight = 2.0f;
            float hueHandleY = layout.paletteY + barInset + this.colorPopupSetting.getHue() * barInnerHeight;
            float alphaHandleY = layout.paletteY + barInset + (1.0f - this.colorPopupSetting.getAlpha01()) * barInnerHeight;
            hueHandleY = this.clamp(hueHandleY, layout.paletteY + barInset + markerHeight * 0.5f, layout.paletteY + layout.paletteSize - barInset - markerHeight * 0.5f);
            alphaHandleY = this.clamp(alphaHandleY, layout.paletteY + barInset + markerHeight * 0.5f, layout.paletteY + layout.paletteSize - barInset - markerHeight * 0.5f);
            RenderUtil.drawRoundRect(context, layout.hueX, hueHandleY - markerHeight * 0.5f, layout.barW, markerHeight, 1.0f, ColorUtil.multiplyAlpha(ColorUtil.rgb(255, 255, 255), alpha));
            RenderUtil.drawRoundRect(context, layout.alphaX, alphaHandleY - markerHeight * 0.5f, layout.barW, markerHeight, 1.0f, ColorUtil.multiplyAlpha(ColorUtil.rgb(255, 255, 255), alpha));
        }
    }

    private void renderColorInfo(class_332 context, ColorPopupLayout layout, float alpha) {
        if (this.colorPopupSetting != null) {
            int boxBg = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, alpha);
            int border = ColorUtil.multiplyAlpha(ClientColors.SETTINGS_OUTLINE, alpha);
            RenderUtil.drawRoundRect(context, layout.paletteX, layout.infoY, 146.0f, 14.0f, 4.0f, boxBg);
            RenderUtil.drawBorder(context, layout.paletteX, layout.infoY, 146.0f, 14.0f, 4.0f, border, 0.0f, 0.5f, 1.0f);
            int color = this.colorPopupSetting.getColor();
            String rgba = ColorUtil.red(color) + "  " + ColorUtil.green(color) + "  " + ColorUtil.blue(color) + "  " + Math.round(this.colorPopupSetting.getAlpha01() * 100.0f) + "%";
            RenderUtil.drawImage(context, "textures/gui/icons/test_tube.png", layout.paletteX + 6.0f, layout.infoY + 3.5f, 7.0f, 7.0f, 0.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), 0.0f);
            RenderUtil.drawText(context, "RGBA", layout.paletteX + 16.0f, layout.infoY + 4.5f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.BOLD, 6);
            RenderUtil.drawText(context, rgba, layout.paletteX + 41.0f, layout.infoY + 4.5f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.REGULAR, 6);
            double uiMouseX = class_310.method_1551().field_1729.method_1603() / (double)class_310.method_1551().method_22683().method_4495();
            double uiMouseY = class_310.method_1551().field_1729.method_1604() / (double)class_310.method_1551().method_22683().method_4495();
            boolean copyHovered = this.isHovered(uiMouseX, uiMouseY, layout.copyX - 1.0f, layout.infoY + 2.5f, 9.0f, 9.0f);
            boolean loadHovered = this.isHovered(uiMouseX, uiMouseY, layout.loadX - 1.0f, layout.infoY + 2.5f, 9.0f, 9.0f);
            int copyColor = ColorUtil.multiplyAlpha(copyHovered ? ClientColors.CATEGORY_ACCENT : ClientColors.TEXT_PRIMARY, alpha);
            int loadColor = ColorUtil.multiplyAlpha(loadHovered ? ClientColors.CATEGORY_ACCENT : ClientColors.TEXT_PRIMARY, alpha);
            float iconY = layout.infoY + 3.0f;
            if (copyHovered) {
                RenderUtil.drawRoundRect(context, layout.copyX - 1.5f, iconY - 1.0f, 10.0f, 10.0f, 2.5f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * 0.18f));
            }
            if (loadHovered) {
                RenderUtil.drawRoundRect(context, layout.loadX - 1.5f, iconY - 1.0f, 10.0f, 10.0f, 2.5f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha * 0.18f));
            }
            RenderUtil.drawImage(context, "textures/gui/icons/copy.png", layout.copyX, iconY, 7.0f, 7.0f, 0.0f, copyColor, 0.0f);
            RenderUtil.drawImage(context, "textures/gui/icons/load.png", layout.loadX, iconY, 7.0f, 7.0f, 0.0f, loadColor, 0.0f);
        }
    }

    private void renderColorPresets(class_332 context, ColorPopupLayout layout, int mouseX, int mouseY, float alpha) {
        float startX = layout.paletteX + 2.0f;
        for (int i = 0; i < COLOR_PRESETS.length; ++i) {
            float px = startX + (float)i * 11.0f;
            float py = layout.presetsY;
            boolean hovered = this.isHovered(mouseX, mouseY, px, py, 7.0f, 7.0f);
            float size = hovered ? 7.8f : 7.0f;
            float offset = (7.0f - size) * 0.5f;
            int presetColor = ColorUtil.withAlpha(COLOR_PRESETS[i], 255);
            RenderUtil.drawCircle(context, px + offset, py + offset, size, ColorUtil.multiplyAlpha(presetColor, alpha));
            if (!hovered) continue;
            RenderUtil.drawBorder(context, px + offset - 1.0f, py + offset - 1.0f, size + 2.0f, size + 2.0f, 6.0f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha * 0.75f), 0.0f, 0.6f, 1.0f);
        }
    }

    private int hitColorPresetIndex(ColorPopupLayout layout, double mouseX, double mouseY) {
        float startX = layout.paletteX + 2.0f;
        for (int i = 0; i < COLOR_PRESETS.length; ++i) {
            float px = startX + (float)i * 11.0f;
            float py = layout.presetsY;
            if (!this.isHovered(mouseX, mouseY, px, py, 7.0f, 7.0f)) continue;
            return i;
        }
        return -1;
    }

    private void applyColorPreset(int presetIndex) {
        if (this.colorPopupSetting != null && presetIndex >= 0 && presetIndex < COLOR_PRESETS.length) {
            int nextColor = ColorUtil.withAlpha(COLOR_PRESETS[presetIndex], Math.round(this.colorPopupSetting.getAlpha01() * 255.0f));
            this.colorPopupSetting.setColor(nextColor);
            ClientTheme.syncSetting(this.colorPopupSetting);
        }
    }

    private void updateColorDrag(int mouseX, int mouseY, float panelX, float panelY, float panelWidth, float panelHeight) {
        if (this.isColorPopupActive() && this.colorPopupOpen && this.colorDragMode != ColorDragMode.NONE) {
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.method_22683() != null) {
                long handle = mc.method_22683().method_4490();
                if (GLFW.glfwGetMouseButton((long)handle, (int)0) != 1) {
                    this.colorDragMode = ColorDragMode.NONE;
                } else {
                    float[] popupRect = this.getColorPopupRect();
                    this.updateColorFromMouse(this.colorDragMode, mouseX, mouseY, this.getColorPopupLayout(popupRect[0], popupRect[1]));
                }
            } else {
                this.colorDragMode = ColorDragMode.NONE;
            }
        }
    }

    private void updateColorFromMouse(ColorDragMode dragMode, double mouseX, double mouseY, ColorPopupLayout layout) {
        if (this.colorPopupSetting != null) {
            if (dragMode == ColorDragMode.PALETTE) {
                float saturation = this.clamp((float)((mouseX - (double)layout.paletteX) / (double)layout.paletteSize), 0.0f, 1.0f);
                float brightness = 1.0f - this.clamp((float)((mouseY - (double)layout.paletteY) / (double)layout.paletteSize), 0.0f, 1.0f);
                this.colorPopupSetting.setSaturation(saturation);
                this.colorPopupSetting.setBrightness(brightness);
                ClientTheme.syncSetting(this.colorPopupSetting);
            } else if (dragMode == ColorDragMode.HUE) {
                float hue = this.clamp((float)((mouseY - (double)layout.paletteY) / (double)layout.paletteSize), 0.0f, 1.0f);
                this.colorPopupSetting.setHue(hue);
                ClientTheme.syncSetting(this.colorPopupSetting);
            } else if (dragMode == ColorDragMode.ALPHA) {
                float alpha = 1.0f - this.clamp((float)((mouseY - (double)layout.paletteY) / (double)layout.paletteSize), 0.0f, 1.0f);
                this.colorPopupSetting.setAlpha01(alpha);
                ClientTheme.syncSetting(this.colorPopupSetting);
            }
        }
    }

    private void copyCurrentColor() {
        class_310 mc;
        if (this.colorPopupSetting != null && (mc = class_310.method_1551()) != null && mc.field_1774 != null) {
            mc.field_1774.method_1455(String.format("#%08X", this.colorPopupSetting.getColor()));
        }
    }

    private void pasteCurrentColor() {
        String text;
        Integer parsed;
        class_310 mc;
        if (this.colorPopupSetting != null && (mc = class_310.method_1551()) != null && mc.field_1774 != null && (parsed = this.parseColorText(text = mc.field_1774.method_1460())) != null) {
            this.colorPopupSetting.setColor(parsed);
            ClientTheme.syncSetting(this.colorPopupSetting);
        }
    }

    private Integer parseColorText(String text) {
        if (text == null) {
            return null;
        }
        Object normalized = text.trim().replace("0x", "").replace("0X", "").replace("#", "").replace("_", "").replace(" ", "");
        if (((String)normalized).length() == 6) {
            normalized = "FF" + (String)normalized;
        }
        if (((String)normalized).length() != 8) {
            return null;
        }
        try {
            return (int)Long.parseLong((String)normalized, 16);
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean isInsideRoundedRect(float x, float y, float width, float height, float radius) {
        float r = Math.max(0.0f, Math.min(radius, Math.min(width, height) * 0.5f));
        if (r <= 0.0f) {
            return x >= 0.0f && x <= width && y >= 0.0f && y <= height;
        }
        if (x >= r && x <= width - r) {
            return y >= 0.0f && y <= height;
        }
        if (y >= r && y <= height - r) {
            return x >= 0.0f && x <= width;
        }
        float cx = x < r ? r : width - r;
        float cy = y < r ? r : height - r;
        float dx = x - cx;
        float dy = y - cy;
        return dx * dx + dy * dy <= r * r;
    }

    private void renderCompactRow(class_332 context, UiAnimationEngine animation, Module module, float x, float y, float width, float height, int slot, boolean animate, float alpha) {
        float enabled = module.isEnable() ? 1.0f : 0.0f;
        float progress = animate ? animation.animate01("compact:state:" + module.getClass().getName(), enabled, 12.0f, enabled) : enabled;
        int background = ColorUtil.interpolate(ClientColors.CATEGORY_BACKGROUND, ClientColors.CATEGORY_ACCENT, progress * 0.11f);
        RenderUtil.drawRoundRect(context, x, y, width, height, 5.0f, ColorUtil.multiplyAlpha(background, alpha));
        RenderUtil.drawBorder(context, x, y, width, height, 5.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND_OUTLINE, alpha), 0.0f, 0.5f, 1.0f);
        Object label = module.getName();
        float maxWidth = Math.max(20.0f, width - 65.0f);
        if (RenderUtil.getTextWidth((String)label, GFonts.MEDIUM, 8.0f) > maxWidth) {
            while (((String)label).length() > 1 && RenderUtil.getTextWidth((String)label + "...", GFonts.MEDIUM, 8.0f) > maxWidth) {
                label = ((String)label).substring(0, ((String)label).length() - 1);
            }
            label = (String)label + "...";
        }
        RenderUtil.drawText(context, (String)label, x + 9.0f, y + 9.5f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha), GFonts.MEDIUM, 8.0f);
        RenderUtil.drawText(context, "...", x + width - 43.0f, y + 9.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT_SECOND, alpha), GFonts.BOLD, 8.0f);
        float switchX = x + width - 25.0f;
        float switchY = y + 8.5f;
        RenderUtil.drawRoundRect(context, switchX, switchY, 17.0f, 10.0f, 5.0f, ColorUtil.multiplyAlpha(ColorUtil.interpolate(ClientColors.SETTINGS_BACKGROUND, ClientColors.CATEGORY_ACCENT, progress), alpha));
        RenderUtil.drawRoundRect(context, switchX + 1.5f + 7.0f * progress, switchY + 1.5f, 7.0f, 7.0f, 3.5f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha));
    }

    private float getModuleCardHeight(Module module, float cardWidth, UiAnimationEngine animation) {
        if (this.settingsModule == null || module != this.settingsModule) {
            return 27.0f;
        }
        List<Setting> renderSettings = this.getRenderableSettings(module);
        if (renderSettings.isEmpty()) {
            return 72.0f;
        }
        float titleOffset = (25.0f + RenderUtil.getTextHeight(GFonts.MEDIUM, 9)) / 2.0f - 8.0f;
        float settingXOffset = 8.0f;
        float settingW = Math.max(16.0f, cardWidth - 8.0f - settingXOffset);
        float settingHeight = 0.0f;
        for (int i = 0; i < renderSettings.size(); ++i) {
            Setting setting = renderSettings.get(i);
            if (setting instanceof BooleanSetting) {
                BooleanSetting booleanSetting = (BooleanSetting)setting;
                settingHeight += BooleanSettingRenderer.measureHeight(booleanSetting, settingW);
            } else if (setting instanceof SliderSetting) {
                SliderSetting sliderSetting = (SliderSetting)setting;
                settingHeight += SliderSettingRenderer.measureHeight(sliderSetting, settingW);
            } else if (setting instanceof ColorSetting) {
                ColorSetting colorSetting = (ColorSetting)setting;
                settingHeight += ColorSettingRenderer.measureHeight(colorSetting, settingW);
            } else if (setting instanceof SelectSetting) {
                SelectSetting selectSetting = (SelectSetting)setting;
                settingKey = module.getClass().getName() + ":" + selectSetting.getName();
                expanded = this.selectExpanded.getOrDefault(settingKey, false);
                expandProgress = animation == null ? this.getSelectExpandProgress(settingKey, expanded) : animation.animate01("setting:select:open:" + settingKey, expanded ? 1.0f : 0.0f, 18.0f, expanded ? 1.0f : 0.0f);
                this.selectExpandProgress.put(settingKey, Float.valueOf(expandProgress));
                settingHeight += SelectSettingRenderer.measureHeight(selectSetting, settingW, expandProgress);
            } else if (setting instanceof MultiSelectSetting) {
                MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
                settingKey = module.getClass().getName() + ":" + multiSelectSetting.getName();
                expanded = this.multiSelectExpanded.getOrDefault(settingKey, false);
                expandProgress = animation == null ? this.getMultiSelectExpandProgress(settingKey, expanded) : animation.animate01("setting:multi:open:" + settingKey, expanded ? 1.0f : 0.0f, 18.0f, expanded ? 1.0f : 0.0f);
                this.multiSelectExpandProgress.put(settingKey, Float.valueOf(expandProgress));
                settingHeight += MultiSelectSettingRenderer.measureHeight(multiSelectSetting, settingW, expandProgress);
            }
            if (i >= renderSettings.size() - 1) continue;
            Setting nextSetting = renderSettings.get(i + 1);
            settingHeight += this.getSettingsGap(setting, nextSetting);
        }
        return Math.max(25.0f, titleOffset + titleOffset + 4.0f + settingHeight + 4.0f);
    }

    private float getSettingsGap(Setting current, Setting next) {
        if (next == null) {
            return 0.0f;
        }
        return (current instanceof SelectSetting || current instanceof MultiSelectSetting) && next instanceof BooleanSetting ? 5.0f : 2.0f;
    }

    private float getSelectExpandProgress(String settingKey, boolean expanded) {
        return this.selectExpandProgress.getOrDefault(settingKey, Float.valueOf(expanded ? 1.0f : 0.0f)).floatValue();
    }

    private float getMultiSelectExpandProgress(String settingKey, boolean expanded) {
        return this.multiSelectExpandProgress.getOrDefault(settingKey, Float.valueOf(expanded ? 1.0f : 0.0f)).floatValue();
    }

    private float getModuleBindTargetWidth(String bindText) {
        float bindTextW = RenderUtil.getTextWidth(bindText, GFonts.BOLD, 5);
        return 11.5f + bindTextW + 3.0f - 1.0f;
    }

    private float getPopupKeyTargetWidth(String keyText) {
        float keyTextW = RenderUtil.getTextWidth(keyText, GFonts.BOLD, 5);
        return 13.5f + keyTextW + 5.0f;
    }

    private float animateWidth(UiAnimationEngine animation, Map<String, Float> widths, String key, float targetWidth, float speed) {
        if (!widths.containsKey(key)) {
            animation.snap(key, targetWidth);
            widths.put(key, Float.valueOf(targetWidth));
            return targetWidth;
        }
        float current = widths.getOrDefault(key, Float.valueOf(targetWidth)).floatValue();
        float next = animation.animate(key, targetWidth, speed, current);
        if (Math.abs(next - targetWidth) <= 0.06f) {
            next = targetWidth;
            animation.snap(key, targetWidth);
        }
        widths.put(key, Float.valueOf(next));
        return next;
    }

    private float animateTextRevealAfterWidth(UiAnimationEngine animation, Map<String, String> textMap, Map<String, Boolean> unlockedMap, String stateKey, String alphaKey, String nextText, float currentWidth, float targetWidth) {
        String prevText = textMap.get(stateKey);
        if (prevText == null) {
            textMap.put(stateKey, nextText);
            unlockedMap.put(stateKey, true);
            animation.snap(alphaKey, 1.0f);
            return 1.0f;
        }
        if (!prevText.equals(nextText)) {
            textMap.put(stateKey, nextText);
            unlockedMap.put(stateKey, false);
            animation.snap(alphaKey, 0.0f);
            return 0.0f;
        }
        boolean unlocked = unlockedMap.getOrDefault(stateKey, false);
        if (!unlocked && Math.abs(currentWidth - targetWidth) <= 0.12f) {
            unlocked = true;
            unlockedMap.put(stateKey, true);
        }
        if (!unlocked) {
            animation.snap(alphaKey, 0.0f);
            return 0.0f;
        }
        return animation.animate01(alphaKey, 1.0f, 16.0f, 0.0f);
    }

    private void drawTextWithRightFade(class_332 context, String text, float x, float y, int color, GFonts font, int fontSize, float maxWidth, float fadeWidth, float clipTop, float clipHeight) {
        if (text != null && !text.isEmpty() && !(maxWidth <= 0.0f)) {
            float fullWidth = RenderUtil.getTextWidth(text, font, fontSize);
            if (fullWidth <= maxWidth) {
                RenderUtil.drawText(context, text, x, y - 0.5f, color, font, fontSize);
            } else {
                int scissorTop = (int)Math.floor(clipTop);
                int scissorBottom = (int)Math.ceil(clipTop + clipHeight);
                int x1 = (int)Math.floor(x);
                int x2 = (int)Math.ceil(x + maxWidth);
                context.method_44379(x1, scissorTop, x2, scissorBottom);
                RenderUtil.drawTextFadeRight(context, text, x, y - 0.5f, color, font, fontSize, maxWidth, fadeWidth);
                context.method_44380();
            }
        }
    }

    private void drawTextClipped(class_332 context, String text, float x, float y, int color, GFonts font, int fontSize, float clipX, float clipWidth, float clipTop, float clipHeight) {
        if (text != null && !text.isEmpty() && !(clipWidth <= 0.0f) && ColorUtil.alpha(color) > 0) {
            int x1 = (int)Math.floor(clipX);
            int y1 = (int)Math.floor(clipTop);
            int x2 = (int)Math.ceil(clipX + clipWidth);
            int y2 = (int)Math.ceil(clipTop + clipHeight);
            context.method_44379(x1, y1, x2, y2);
            RenderUtil.drawText(context, text, x, y, color, font, fontSize);
            context.method_44380();
        }
    }

    private void onCapturePress(int code) {
        this.capturePressedCodes.add(code);
        if (this.captureSelectedCodes.size() < 3 || this.captureSelectedCodes.contains(code)) {
            this.captureSelectedCodes.add(code);
        }
    }

    private void onCaptureRelease(int code) {
        this.capturePressedCodes.remove(code);
        if (this.capturePressedCodes.isEmpty() && !this.captureSelectedCodes.isEmpty()) {
            BindData activePopupBind = this.getActivePopupBind();
            if (activePopupBind != null) {
                activePopupBind.setKeys(new LinkedHashSet<Integer>(this.captureSelectedCodes));
            }
            this.bindCapture = false;
            this.clearCaptureState();
        }
    }

    private void clearCaptureState() {
        this.capturePressedCodes.clear();
        this.captureSelectedCodes.clear();
    }

    private float updateModuleTitleShift(UiAnimationEngine animation, String moduleKey, float titleMix, float shiftMax) {
        String animKey = "module:title:shift:" + moduleKey;
        float current = this.moduleTitleScrollValues.getOrDefault(moduleKey, Float.valueOf(0.0f)).floatValue();
        boolean forward = this.moduleTitleScrollForward.getOrDefault(moduleKey, true);
        float edgeEpsilon = 0.0015f;
        if (shiftMax <= 0.5f) {
            this.moduleTitleScrollValues.put(moduleKey, Float.valueOf(0.0f));
            this.moduleTitleScrollForward.put(moduleKey, true);
            animation.snap(animKey, 0.0f);
            return 0.0f;
        }
        if (titleMix <= 0.02f) {
            float back = animation.animate(animKey, 0.0f, 18.0f, current);
            this.moduleTitleScrollValues.put(moduleKey, Float.valueOf(back));
            this.moduleTitleScrollForward.put(moduleKey, true);
            return back;
        }
        float target = forward ? shiftMax : 0.0f;
        float next = animation.animate(animKey, target, 4.8f, current);
        if (forward && next >= shiftMax - 0.0015f) {
            next = shiftMax;
            this.moduleTitleScrollForward.put(moduleKey, false);
        } else if (!forward && next <= 0.0015f) {
            next = 0.0f;
            this.moduleTitleScrollForward.put(moduleKey, true);
        } else {
            this.moduleTitleScrollForward.put(moduleKey, forward);
        }
        this.moduleTitleScrollValues.put(moduleKey, Float.valueOf(next));
        return next;
    }

    private float smootherStep01(float t) {
        float x = ColorUtil.clamp01(t);
        return x * x * x * (x * (x * 6.0f - 15.0f) + 10.0f);
    }

    private float computeMaxScroll(List<Module> modules, float cardWidth, float viewportHeight, UiAnimationEngine animation) {
        if (this.settingsModule != null) {
            return Math.max(0.0f, this.getModuleCardHeight(this.settingsModule, cardWidth, animation) - viewportHeight);
        }
        float[] columnBottom = new float[2];
        for (int c = 0; c < 2; ++c) {
            columnBottom[c] = 0.0f;
        }
        for (int i = 0; i < modules.size(); ++i) {
            int col = i % 2;
            Module module = modules.get(i);
            float moduleCardHeight = this.getModuleCardHeight(module, cardWidth, animation);
            int n = col;
            columnBottom[n] = columnBottom[n] + (moduleCardHeight + 8.0f);
        }
        float contentHeight = 0.0f;
        for (int c = 0; c < 2; ++c) {
            contentHeight = Math.max(contentHeight, columnBottom[c]);
        }
        if (contentHeight > 0.0f) {
            contentHeight -= 8.0f;
        }
        return Math.max(0.0f, contentHeight - viewportHeight);
    }

    private boolean isHovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    private List<Setting> getRenderableSettings(Module module) {
        if (module != this.settingsModule) {
            return List.of();
        }
        ArrayList<Setting> result = new ArrayList<Setting>();
        for (Setting setting : module.getSettings()) {
            if (!setting.isVisible() || !(setting instanceof BooleanSetting) && !(setting instanceof SliderSetting) && !(setting instanceof ColorSetting) && !(setting instanceof SelectSetting) && !(setting instanceof MultiSelectSetting)) continue;
            result.add(setting);
        }
        return result;
    }

    private void updateSliderDrag(int mouseX) {
        if (this.draggingSliderModule != null && this.draggingSliderSetting != null) {
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.method_22683() != null) {
                long handle = mc.method_22683().method_4490();
                if (GLFW.glfwGetMouseButton((long)handle, (int)0) != 1) {
                    this.draggingSliderModule = null;
                    this.draggingSliderSetting = null;
                } else {
                    SliderSettingRenderer.setFromMouse(this.draggingSliderSetting, this.draggingSliderX, this.draggingSliderY, this.draggingSliderW, mouseX);
                }
            } else {
                this.draggingSliderModule = null;
                this.draggingSliderSetting = null;
            }
        }
    }

    private String formatBindText(Set<Integer> keys) {
        if (keys != null && !keys.isEmpty()) {
            ArrayList<String> parts = new ArrayList<String>();
            for (int code : keys) {
                parts.add(this.formatBindCode(code));
                if (parts.size() < 3) continue;
                break;
            }
            return String.join((CharSequence)" + ", parts);
        }
        return "None";
    }

    private String formatBindCode(int code) {
        if (Pyz1k.isMouseBindCode(code)) {
            return "M" + (Pyz1k.bindCodeToMouseButton(code) + 1);
        }
        return switch (code) {
            case 32 -> "SPACE";
            case 256 -> "ESC";
            case 257 -> "ENTER";
            case 258 -> "TAB";
            case 259 -> "BACKSPACE";
            case 261 -> "DELETE";
            case 340 -> "L SHIFT";
            case 341 -> "L CTRL";
            case 342 -> "L ALT";
            case 344 -> "R SHIFT";
            case 345 -> "R CTRL";
            case 346 -> "R ALT";
            default -> {
                String name = GLFW.glfwGetKeyName((int)code, (int)0);
                if (name != null && !name.isBlank()) {
                    yield name.toUpperCase();
                }
                yield "KEY " + code;
            }
        };
    }

    private Category toCategory(CategoryBarSection.TopTab tab) {
        return switch (tab) {
            default -> throw new MatchException(null, null);
            case CategoryBarSection.TopTab.VISUALS -> Category.VISUALS;
            case CategoryBarSection.TopTab.ITEM_RENDER -> Category.ITEM_RENDER;
            case CategoryBarSection.TopTab.HAND_RENDER -> Category.HAND_RENDER;
            case CategoryBarSection.TopTab.ABILITY_BIND, CategoryBarSection.TopTab.CONFIGS, CategoryBarSection.TopTab.THEMES, CategoryBarSection.TopTab.COSMETICS -> null;
        };
    }

    private List<Module> visibleModules(Category category) {
        return Pyz1k.getInstance().getModuleManager().getModules(category);
    }

    private CategoryBarSection.TopTab toTab(Category category) {
        if (category == null) {
            return null;
        }
        return switch (category) {
            default -> throw new MatchException(null, null);
            case Category.VISUALS -> CategoryBarSection.TopTab.VISUALS;
            case Category.ITEM_RENDER -> CategoryBarSection.TopTab.ITEM_RENDER;
            case Category.HAND_RENDER -> CategoryBarSection.TopTab.HAND_RENDER;
        };
    }

    public Map<String, Float> captureTabScrollState() {
        HashMap<String, Float> state = new HashMap<String, Float>();
        for (Map.Entry<CategoryBarSection.TopTab, Float> entry : this.tabScrollTargets.entrySet()) {
            state.put(entry.getKey().name(), entry.getValue());
        }
        return state;
    }

    public Map<String, Boolean> captureSelectExpandedState() {
        return new HashMap<String, Boolean>(this.selectExpanded);
    }

    public Map<String, Boolean> captureMultiSelectExpandedState() {
        return new HashMap<String, Boolean>(this.multiSelectExpanded);
    }

    public void applyUiState(Map<String, Float> scrollByTab, Map<String, Boolean> selectExpandedState, Map<String, Boolean> multiSelectExpandedState) {
        if (scrollByTab != null) {
            this.tabScrollTargets.clear();
            this.tabScrollValues.clear();
            for (Map.Entry<String, Float> entry : scrollByTab.entrySet()) {
                try {
                    CategoryBarSection.TopTab tab = CategoryBarSection.TopTab.valueOf(entry.getKey());
                    float value = Math.max(0.0f, entry.getValue() == null ? 0.0f : entry.getValue().floatValue());
                    this.tabScrollTargets.put(tab, Float.valueOf(value));
                    this.tabScrollValues.put(tab, Float.valueOf(value));
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
        }
        if (selectExpandedState != null) {
            this.selectExpanded.clear();
            this.selectExpanded.putAll(selectExpandedState);
        }
        if (multiSelectExpandedState != null) {
            this.multiSelectExpanded.clear();
            this.multiSelectExpanded.putAll(multiSelectExpandedState);
        }
    }

    private static enum ColorDragMode {
        NONE,
        PALETTE,
        HUE,
        ALPHA;

    }

    private record ScrollbarState(float trackX, float trackY, float trackW, float trackH, float thumbY, float thumbH, float maxScroll) {
    }

    private record ColorPopupLayout(float paletteX, float paletteY, float paletteSize, float hueX, float alphaX, float barW, float infoY, float presetsY, float closeX, float closeY, float closeW, float closeH, float copyX, float loadX) {
    }
}

