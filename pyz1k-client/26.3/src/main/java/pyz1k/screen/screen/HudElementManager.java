/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.input.KeyEvent
 *  org.lwjgl.glfw.GLFW
 */
package pyz1k.screen.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import pyz1k.compat.LegacyInput;
import pyz1k.module.setting.BooleanSetting;
import pyz1k.module.setting.InputSetting;
import pyz1k.module.setting.MultiSelectSetting;
import pyz1k.module.setting.SelectSetting;
import pyz1k.module.setting.Setting;
import pyz1k.module.setting.SliderSetting;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.menu.MenuScreen;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.screen.menu.sections.settings.BooleanSettingRenderer;
import pyz1k.screen.menu.sections.settings.InputSettingRenderer;
import pyz1k.screen.menu.sections.settings.MultiSelectSettingRenderer;
import pyz1k.screen.menu.sections.settings.SelectSettingRenderer;
import pyz1k.screen.menu.sections.settings.SliderSettingRenderer;
import pyz1k.screen.screen.HudElement;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.animation.Animator;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class HudElementManager {
    private static final float MENU_PANEL_WIDTH = 500.0f;
    private static final float MENU_PANEL_HEIGHT = 300.0f;
    private static final float POPUP_WIDTH = 114.0f;
    private static final float POPUP_PAD = 6.0f;
    private static final float POPUP_HEADER_H = 16.0f;
    private static final float POPUP_SETTINGS_GAP = 2.0f;
    private static final float GRID_SNAP_THRESHOLD = 5.0f;
    private static final float SELECT_EXPAND_SPEED = 18.0f;
    private static final float MULTI_SELECT_EXPAND_SPEED = 18.0f;
    private final Map<String, HudElement> elements = new LinkedHashMap<String, HudElement>();
    private final Animator animator = new Animator();
    private final UiAnimationEngine settingUiAnimation = new UiAnimationEngine();
    private final Map<String, Boolean> selectExpanded = new HashMap<String, Boolean>();
    private final Map<String, Float> selectExpandProgress = new HashMap<String, Float>();
    private final Map<String, Boolean> multiExpanded = new HashMap<String, Boolean>();
    private final Map<String, Float> multiExpandProgress = new HashMap<String, Float>();
    private HudElement draggingElement;
    private float dragOffsetX;
    private float dragOffsetY;
    private SliderSetting draggingSlider;
    private float sliderX;
    private float sliderY;
    private float sliderW;
    private HudElement popupElement;
    private HudElement popupRenderElement;
    private PopupLayout lastPopupLayout;
    private boolean hasRenderableElements;
    private InputSetting focusedInputSetting;
    private String focusedInputKey;
    private int focusedInputCaret;
    private boolean prevLmbDown;
    private boolean prevRmbDown;
    private boolean prevEscDown;

    public void register(HudElement element) {
        if (element != null) {
            this.elements.put(element.getId(), element);
        }
    }

    public List<HudElement> getElements() {
        return new ArrayList<HudElement>(this.elements.values());
    }

    public List<Setting> collectAllSettings() {
        ArrayList<Setting> out = new ArrayList<Setting>();
        for (HudElement element : this.elements.values()) {
            out.addAll(element.getSettings());
        }
        return out;
    }

    public boolean hasRenderableElements() {
        return this.hasRenderableElements;
    }

    public void render(GuiGraphicsExtractor context, float tickDelta, Set<String> enabledNames, boolean editorEnabled) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getWindow() != null) {
            this.animator.beginFrame(System.nanoTime());
            int fps = mc.getFps();
            if (fps <= 0) {
                fps = 60;
            }
            this.settingUiAnimation.beginFrame(fps);
            int screenW = mc.getWindow().getGuiScaledWidth();
            int screenH = mc.getWindow().getGuiScaledHeight();
            long window = mc.getWindow().handle();
            float mouseX = (float)(mc.mouseHandler.xpos() * (double)screenW / (double)mc.getWindow().getScreenWidth());
            float mouseY = (float)(mc.mouseHandler.ypos() * (double)screenH / (double)mc.getWindow().getScreenHeight());
            boolean lmbDown = LegacyInput.glfwGetMouseButton((long)window, (int)0) == 1;
            boolean rmbDown = LegacyInput.glfwGetMouseButton((long)window, (int)1) == 1;
            boolean ctrlDown = LegacyInput.glfwGetKey((long)window, (int)341) == 1 || LegacyInput.glfwGetKey((long)window, (int)345) == 1;
            boolean escDown = LegacyInput.glfwGetKey((long)window, (int)256) == 1;
            boolean chatOpen = mc.gui.screen() instanceof ChatScreen;
            boolean lmbPressed = lmbDown && !this.prevLmbDown;
            boolean rmbPressed = rmbDown && !this.prevRmbDown;
            boolean escPressed = escDown && !this.prevEscDown;
            ArrayList<HudElement> visible = editorEnabled ? new ArrayList<HudElement>() : null;
            this.hasRenderableElements = false;
            for (HudElement element : this.elements.values()) {
                boolean enabled = enabledNames == null || enabledNames.contains(element.getDisplayName());
                element.setActive(enabled);
                if (!enabled && !element.shouldRenderWhenInactive()) continue;
                this.clampElementToScreen(element, screenW, screenH);
                element.render(context, screenW, screenH, tickDelta);
                if (element.getWidth() <= 1.0f || element.getHeight() <= 1.0f) continue;
                this.hasRenderableElements = true;
                if (visible == null) continue;
                visible.add(element);
            }
            if (!editorEnabled) {
                this.draggingElement = null;
                this.draggingSlider = null;
                this.closePopup();
                this.prevLmbDown = lmbDown;
                this.prevRmbDown = rmbDown;
                this.prevEscDown = escDown;
            } else {
                boolean popupHovered;
                boolean mouseOverMenuPanel = this.isMouseOverMenuPanel(mouseX, mouseY, screenW, screenH, mc);
                HudElement hoveredElement = this.getTopHovered(visible, mouseX, mouseY, mc, mouseOverMenuPanel);
                for (HudElement element : visible) {
                    if (!element.canInteractInEditor(mc)) continue;
                    boolean hovered = element == hoveredElement;
                    String string = "hud:element:hover:" + element.getId();
                    float f = hovered ? 1.0f : 0.0f;
                    float hover = this.animator.animate(string, f, 7.0f);
                    if (!(hover > 0.01f)) continue;
                    float eased = hover * hover * (3.0f - 2.0f * hover);
                    int fadeOverlay = ColorUtil.rgba(0, 0, 0, Math.round(120.0f * eased));
                    RenderUtil.drawRoundRect(context, element.getVisualX(), element.getVisualY(), element.getWidth(), element.getHeight(), 4.0f, fadeOverlay);
                }
                if (this.draggingElement != null && lmbDown) {
                    this.drawSnapGrid(context, screenW, screenH);
                    float targetX = mouseX - this.dragOffsetX;
                    float targetY = mouseY - this.dragOffsetY;
                    if (!this.draggingElement.allowHorizontalDrag()) {
                        targetX = this.draggingElement.getVisualX();
                    }
                    if (!this.draggingElement.allowVerticalDrag()) {
                        targetY = this.draggingElement.getVisualY();
                    }
                    targetX = this.clamp(targetX, 0.0f, Math.max(0.0f, (float)screenW - this.draggingElement.getWidth()));
                    targetY = this.clamp(targetY, 0.0f, Math.max(0.0f, (float)screenH - this.draggingElement.getHeight()));
                    if (!ctrlDown) {
                        targetX = this.snapElementToQuarterGuides(targetX, this.draggingElement.getWidth(), screenW, 5.0f);
                        targetY = this.snapElementToQuarterGuides(targetY, this.draggingElement.getHeight(), screenH, 5.0f);
                        targetX = this.clamp(targetX, 0.0f, Math.max(0.0f, (float)screenW - this.draggingElement.getWidth()));
                        targetY = this.clamp(targetY, 0.0f, Math.max(0.0f, (float)screenH - this.draggingElement.getHeight()));
                    }
                    this.draggingElement.setVisualPosition(targetX, targetY);
                }
                if (!lmbDown) {
                    this.draggingElement = null;
                }
                if (this.draggingSlider != null && lmbDown) {
                    SliderSettingRenderer.setFromMouse(this.draggingSlider, this.sliderX, this.sliderY, this.sliderW, mouseX);
                }
                if (!lmbDown) {
                    this.draggingSlider = null;
                }
                boolean bl = popupHovered = this.lastPopupLayout != null && this.isHovered(mouseX, mouseY, this.lastPopupLayout.x, this.lastPopupLayout.y, this.lastPopupLayout.width, this.lastPopupLayout.height);
                if (lmbPressed) {
                    boolean consumedPopup = false;
                    if (popupHovered && this.lastPopupLayout != null && !(consumedPopup = this.handlePopupLeftClick(mouseX, mouseY))) {
                        consumedPopup = true;
                    }
                    if (!consumedPopup) {
                        if (hoveredElement != null && hoveredElement.canInteractInEditor(mc)) {
                            if (!hoveredElement.handleLeftClick(mc, mouseX, mouseY, screenW, screenH)) {
                                this.draggingElement = hoveredElement;
                                this.dragOffsetX = mouseX - hoveredElement.getVisualX();
                                this.dragOffsetY = mouseY - hoveredElement.getVisualY();
                            }
                        } else if (!popupHovered) {
                            this.closePopup();
                        }
                    }
                }
                if (rmbPressed) {
                    if (hoveredElement != null && hoveredElement.canInteractInEditor(mc)) {
                        if (hoveredElement.getSettings().isEmpty()) {
                            this.closePopup();
                        } else if (this.popupElement == hoveredElement) {
                            this.closePopup();
                        } else {
                            this.popupElement = hoveredElement;
                            this.popupRenderElement = hoveredElement;
                        }
                    } else if (!popupHovered) {
                        this.closePopup();
                    }
                }
                if (escPressed) {
                    this.closePopup();
                }
                this.renderPopup(context, mouseX, mouseY);
                this.prevLmbDown = lmbDown;
                this.prevRmbDown = rmbDown;
                this.prevEscDown = escDown;
            }
        }
    }

    private void renderPopup(GuiGraphicsExtractor context, float mouseX, float mouseY) {
        HudElement activeElement;
        HudElement hudElement = activeElement = this.popupElement != null ? this.popupElement : this.popupRenderElement;
        if (activeElement == null) {
            this.lastPopupLayout = null;
        } else {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getWindow() != null) {
                PopupSettingLayout last;
                int screenW = mc.getWindow().getGuiScaledWidth();
                int screenH = mc.getWindow().getGuiScaledHeight();
                float popupX = activeElement.getVisualX() + activeElement.getWidth() + 6.0f;
                float popupY = activeElement.getVisualY();
                List<PopupSettingLayout> settingLayouts = this.buildSettingLayouts(activeElement, popupX, popupY);
                float popupHeight = 28.0f;
                if (!settingLayouts.isEmpty()) {
                    last = settingLayouts.get(settingLayouts.size() - 1);
                    popupHeight = last.y + last.height - popupY + 6.0f;
                }
                if (!(settingLayouts = this.buildSettingLayouts(activeElement, popupX = this.clamp(popupX, 4.0f, Math.max(4.0f, (float)screenW - 114.0f - 4.0f)), popupY = this.clamp(popupY, 4.0f, Math.max(4.0f, (float)screenH - popupHeight - 4.0f)))).isEmpty()) {
                    last = settingLayouts.get(settingLayouts.size() - 1);
                    popupHeight = last.y + last.height - popupY + 6.0f;
                }
                float popupTarget = this.popupElement != null ? 1.0f : 0.0f;
                float popupAlpha = this.animator.animate("hud:popup:alpha:" + activeElement.getId(), popupTarget, 18.0f);
                if (popupTarget <= 0.0f && popupAlpha <= 0.01f) {
                    this.popupRenderElement = null;
                    this.lastPopupLayout = null;
                } else {
                    this.lastPopupLayout = new PopupLayout(popupX, popupY, 114.0f, popupHeight, settingLayouts);
                    if (!(popupAlpha <= 0.01f)) {
                        int text = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, popupAlpha);
                        HudVisualStyle.drawPanel(context, popupX, popupY, 114.0f, popupHeight, popupAlpha);
                        RenderUtil.drawText(context, activeElement.getDisplayName() + " Settings", popupX + 6.0f, popupY + 5.0f, text, GFonts.MEDIUM, 7);
                        int scX1 = (int)Math.floor(popupX);
                        int scY1 = (int)Math.floor(popupY);
                        int scX2 = (int)Math.ceil(popupX + 114.0f);
                        int scY2 = (int)Math.ceil(popupY + popupHeight);
                        context.enableScissor(scX1, scY1, scX2, scY2);
                        for (PopupSettingLayout layout : settingLayouts) {
                            Setting setting = layout.setting;
                            if (setting instanceof BooleanSetting) {
                                BooleanSetting booleanSetting = (BooleanSetting)setting;
                                BooleanSettingRenderer.render(context, this.settingUiAnimation, layout.key, booleanSetting, layout.x, layout.y, layout.width, (int)mouseX, (int)mouseY, popupAlpha, false);
                                continue;
                            }
                            if (setting instanceof SliderSetting) {
                                SliderSetting sliderSetting = (SliderSetting)setting;
                                SliderSettingRenderer.render(context, this.settingUiAnimation, layout.key, sliderSetting, layout.x, layout.y, layout.width, (int)mouseX, (int)mouseY, popupAlpha);
                                continue;
                            }
                            if (setting instanceof SelectSetting) {
                                SelectSetting selectSetting = (SelectSetting)setting;
                                SelectSettingRenderer.render(context, this.settingUiAnimation, layout.key, selectSetting, layout.x, layout.y, layout.width, (int)mouseX, (int)mouseY, popupAlpha, layout.expandProgress);
                                continue;
                            }
                            if (setting instanceof MultiSelectSetting) {
                                MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
                                MultiSelectSettingRenderer.render(context, this.settingUiAnimation, layout.key, multiSelectSetting, layout.x, layout.y, layout.width, (int)mouseX, (int)mouseY, popupAlpha, layout.expandProgress);
                                continue;
                            }
                            if (!(setting instanceof InputSetting)) continue;
                            InputSetting inputSetting = (InputSetting)setting;
                            boolean focused = inputSetting == this.focusedInputSetting && layout.key.equals(this.focusedInputKey);
                            InputSettingRenderer.render(context, layout.key, inputSetting, layout.x, layout.y, layout.width, (int)mouseX, (int)mouseY, popupAlpha, focused, this.focusedInputCaret);
                        }
                        context.disableScissor();
                    }
                }
            } else {
                this.lastPopupLayout = null;
            }
        }
    }

    private List<PopupSettingLayout> buildSettingLayouts(HudElement element, float popupX, float popupY) {
        ArrayList<PopupSettingLayout> layouts = new ArrayList<PopupSettingLayout>();
        float settingX = popupX + 6.0f;
        float settingW = 102.0f;
        float settingY = popupY + 16.0f + 6.0f;
        for (Setting setting : element.getSettings()) {
            float h;
            float progress;
            if (setting == null || !setting.isVisible()) continue;
            String key = this.settingKey(element, setting);
            if (setting instanceof BooleanSetting) {
                BooleanSetting booleanSetting = (BooleanSetting)setting;
                float h2 = BooleanSettingRenderer.measureHeight(booleanSetting, settingW);
                layouts.add(new PopupSettingLayout(setting, key, settingX, settingY, settingW, h2, 0.0f));
                settingY += h2 + 2.0f;
                continue;
            }
            if (setting instanceof SliderSetting) {
                SliderSetting sliderSetting = (SliderSetting)setting;
                float h3 = SliderSettingRenderer.measureHeight(sliderSetting, settingW);
                layouts.add(new PopupSettingLayout(setting, key, settingX, settingY, settingW, h3, 0.0f));
                settingY += h3 + 2.0f;
                continue;
            }
            if (setting instanceof SelectSetting) {
                SelectSetting selectSetting = (SelectSetting)setting;
                boolean expanded = this.selectExpanded.getOrDefault(key, false);
                progress = this.animator.animate("hud:popup:select:open:" + key, expanded ? 1.0f : 0.0f, 18.0f);
                this.selectExpandProgress.put(key, Float.valueOf(progress));
                h = SelectSettingRenderer.measureHeight(selectSetting, settingW, progress);
                layouts.add(new PopupSettingLayout(setting, key, settingX, settingY, settingW, h, progress));
                settingY += h + 2.0f;
                continue;
            }
            if (setting instanceof MultiSelectSetting) {
                MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
                boolean expanded = this.multiExpanded.getOrDefault(key, false);
                progress = this.animator.animate("hud:popup:multi:open:" + key, expanded ? 1.0f : 0.0f, 18.0f);
                this.multiExpandProgress.put(key, Float.valueOf(progress));
                h = MultiSelectSettingRenderer.measureHeight(multiSelectSetting, settingW, progress);
                layouts.add(new PopupSettingLayout(setting, key, settingX, settingY, settingW, h, progress));
                settingY += h + 2.0f;
                continue;
            }
            if (!(setting instanceof InputSetting)) continue;
            InputSetting inputSetting = (InputSetting)setting;
            float h4 = InputSettingRenderer.measureHeight(inputSetting, settingW);
            layouts.add(new PopupSettingLayout(setting, key, settingX, settingY, settingW, h4, 0.0f));
            settingY += h4 + 2.0f;
        }
        return layouts;
    }

    private boolean handlePopupLeftClick(float mouseX, float mouseY) {
        if (this.lastPopupLayout != null && this.popupElement != null) {
            for (PopupSettingLayout layout : this.lastPopupLayout.settings) {
                int optionIndex;
                boolean expanded;
                Setting setting = layout.setting;
                if (setting instanceof BooleanSetting) {
                    BooleanSetting booleanSetting = (BooleanSetting)setting;
                    if (!BooleanSettingRenderer.hitCheckbox(booleanSetting, layout.x, layout.y, layout.width, mouseX, mouseY, false)) continue;
                    booleanSetting.toggle();
                    return true;
                }
                if (setting instanceof SliderSetting) {
                    SliderSetting sliderSetting = (SliderSetting)setting;
                    if (!SliderSettingRenderer.hitTrack(sliderSetting, layout.x, layout.y, layout.width, mouseX, mouseY)) continue;
                    SliderSettingRenderer.setFromMouse(sliderSetting, layout.x, layout.y, layout.width, mouseX);
                    this.draggingSlider = sliderSetting;
                    this.sliderX = layout.x;
                    this.sliderY = layout.y;
                    this.sliderW = layout.width;
                    return true;
                }
                if (setting instanceof SelectSetting) {
                    SelectSetting selectSetting = (SelectSetting)setting;
                    expanded = this.selectExpanded.getOrDefault(layout.key, false);
                    if (SelectSettingRenderer.hitSelectBox(selectSetting, layout.x, layout.y, layout.width, mouseX, mouseY)) {
                        this.selectExpanded.put(layout.key, !expanded);
                        return true;
                    }
                    optionIndex = SelectSettingRenderer.hitOptionIndex(selectSetting, layout.x, layout.y, layout.width, mouseX, mouseY, layout.expandProgress);
                    if (optionIndex < 0 || optionIndex >= selectSetting.getOptions().size()) continue;
                    selectSetting.setIndex(optionIndex);
                    return true;
                }
                if (setting instanceof MultiSelectSetting) {
                    MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
                    expanded = this.multiExpanded.getOrDefault(layout.key, false);
                    if (MultiSelectSettingRenderer.hitSelectBox(multiSelectSetting, layout.x, layout.y, layout.width, mouseX, mouseY)) {
                        this.multiExpanded.put(layout.key, !expanded);
                        return true;
                    }
                    optionIndex = MultiSelectSettingRenderer.hitOptionIndex(multiSelectSetting, layout.x, layout.y, layout.width, mouseX, mouseY, layout.expandProgress);
                    if (optionIndex < 0 || optionIndex >= multiSelectSetting.getOptions().size()) continue;
                    multiSelectSetting.toggle(multiSelectSetting.getOptions().get(optionIndex));
                    return true;
                }
                if (!(setting instanceof InputSetting)) continue;
                InputSetting inputSetting = (InputSetting)setting;
                if (!InputSettingRenderer.hitInputBox(layout.x, layout.y, layout.width, mouseX, mouseY)) continue;
                this.focusedInputSetting = inputSetting;
                this.focusedInputKey = layout.key;
                String value = inputSetting.getValue();
                this.focusedInputCaret = value == null ? 0 : value.length();
                return true;
            }
            this.focusedInputSetting = null;
            this.focusedInputKey = null;
            this.focusedInputCaret = 0;
            return false;
        }
        return false;
    }

    private void clampElementToScreen(HudElement element, int screenW, int screenH) {
        if (!Float.isFinite(element.getVisualX()) || !Float.isFinite(element.getVisualY())) {
            element.setVisualPosition(0.0f, 0.0f);
        }
        float visualX = element.getVisualX();
        float visualY = element.getVisualY();
        float clampedX = this.clamp(visualX, 0.0f, Math.max(0.0f, (float)screenW - element.getWidth()));
        float clampedY = this.clamp(visualY, 0.0f, Math.max(0.0f, (float)screenH - element.getHeight()));
        if (Math.abs(clampedX - visualX) > 0.01f || Math.abs(clampedY - visualY) > 0.01f) {
            element.setVisualPosition(clampedX, clampedY);
        }
    }

    private HudElement getTopHovered(List<HudElement> visible, float mouseX, float mouseY, Minecraft mc, boolean mouseOverMenuPanel) {
        if (mouseOverMenuPanel) {
            return null;
        }
        for (int i = visible.size() - 1; i >= 0; --i) {
            HudElement element = visible.get(i);
            if (!element.canInteractInEditor(mc) || !this.isHovered(mouseX, mouseY, element.getVisualX(), element.getVisualY(), element.getWidth(), element.getHeight())) continue;
            return element;
        }
        return null;
    }

    private boolean isMouseOverMenuPanel(float mouseX, float mouseY, int screenW, int screenH, Minecraft mc) {
        if (mc != null && mc.gui.screen() instanceof MenuScreen) {
            float panelWidth = Math.min((float)screenW - 48.0f, 500.0f);
            float panelHeight = Math.min((float)screenH - 56.0f, 300.0f);
            float panelX = ((float)screenW - panelWidth) * 0.5f;
            float panelY = ((float)screenH - panelHeight) * 0.5f;
            return this.isHovered(mouseX, mouseY, panelX, panelY, panelWidth, panelHeight);
        }
        return false;
    }

    private String settingKey(HudElement element, Setting setting) {
        return element.getId() + ":" + setting.getName();
    }

    private void closePopup() {
        if (this.popupElement != null) {
            this.popupRenderElement = this.popupElement;
        }
        this.popupElement = null;
        this.focusedInputSetting = null;
        this.focusedInputKey = null;
        this.focusedInputCaret = 0;
    }

    public boolean handleKeyPressed(KeyEvent key) {
        if (this.popupElement == null || this.focusedInputSetting == null || this.focusedInputKey == null || key == null) {
            return false;
        }
        if (!this.focusedInputSetting.isVisible()) {
            this.focusedInputSetting = null;
            this.focusedInputKey = null;
            this.focusedInputCaret = 0;
            return false;
        }
        String value = this.focusedInputSetting.getValue();
        if (value == null) {
            value = "";
        }
        int code = pyz1k.compat.LegacyInput.fromNativeKey(key.key());
        boolean ctrl = this.isCtrlDown();
        switch (code) {
            case 86: {
                if (ctrl) {
                    String clip = this.getClipboard();
                    if (clip != null && !clip.isEmpty()) {
                        this.insertIntoFocused(clip.replaceAll("[\\r\\n\\t]", " "));
                    }
                    return true;
                }
            }
            default: {
                return false;
            }
            case 256: {
                this.focusedInputSetting = null;
                this.focusedInputKey = null;
                this.focusedInputCaret = 0;
                return true;
            }
            case 257: 
            case 335: {
                this.focusedInputSetting = null;
                this.focusedInputKey = null;
                this.focusedInputCaret = 0;
                return true;
            }
            case 259: {
                if (this.focusedInputCaret > 0) {
                    this.focusedInputSetting.setValue(value.substring(0, this.focusedInputCaret - 1) + value.substring(this.focusedInputCaret));
                    --this.focusedInputCaret;
                }
                return true;
            }
            case 261: {
                if (this.focusedInputCaret < value.length()) {
                    this.focusedInputSetting.setValue(value.substring(0, this.focusedInputCaret) + value.substring(this.focusedInputCaret + 1));
                }
                return true;
            }
            case 262: {
                this.focusedInputCaret = Math.min(value.length(), this.focusedInputCaret + 1);
                return true;
            }
            case 263: {
                this.focusedInputCaret = Math.max(0, this.focusedInputCaret - 1);
                return true;
            }
            case 268: {
                this.focusedInputCaret = 0;
                return true;
            }
            case 269: 
        }
        this.focusedInputCaret = value.length();
        return true;
    }

    public boolean handleCharTyped(int codePoint) {
        if (this.popupElement == null || this.focusedInputSetting == null || this.focusedInputKey == null) {
            return false;
        }
        if (!this.focusedInputSetting.isVisible()) {
            this.focusedInputSetting = null;
            this.focusedInputKey = null;
            this.focusedInputCaret = 0;
            return false;
        }
        if (!Character.isValidCodePoint(codePoint)) {
            return false;
        }
        char chr = (char)codePoint;
        if (chr >= ' ' && chr != '\u007f') {
            this.insertIntoFocused(String.valueOf(chr));
            return true;
        }
        return false;
    }

    private void insertIntoFocused(String text) {
        if (this.focusedInputSetting != null && text != null && !text.isEmpty()) {
            String value = this.focusedInputSetting.getValue();
            if (value == null) {
                value = "";
            }
            int caret = Math.max(0, Math.min(this.focusedInputCaret, value.length()));
            String updated = value.substring(0, caret) + text + value.substring(caret);
            this.focusedInputSetting.setValue(updated);
            this.focusedInputCaret = Math.min(this.focusedInputSetting.getValue().length(), caret + text.length());
        }
    }

    private boolean isCtrlDown() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getWindow() != null) {
            long handle = mc.getWindow().handle();
            return LegacyInput.glfwGetKey((long)handle, (int)341) == 1 || LegacyInput.glfwGetKey((long)handle, (int)345) == 1;
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

    private float snapElementToQuarterGuides(float start, float size, int screenSize, float threshold) {
        float[] guides = this.quarterGuides(screenSize);
        float best = start;
        float bestDist = Float.MAX_VALUE;
        float left = start;
        float center = start + size * 0.5f;
        float right = start + size;
        for (float guide : guides) {
            float distRight;
            float distCenter;
            float distLeft = Math.abs(left - guide);
            if (distLeft < bestDist) {
                bestDist = distLeft;
                best = guide;
            }
            if ((distCenter = Math.abs(center - guide)) < bestDist) {
                bestDist = distCenter;
                best = guide - size * 0.5f;
            }
            if (!((distRight = Math.abs(right - guide)) < bestDist)) continue;
            bestDist = distRight;
            best = guide - size;
        }
        return bestDist <= threshold ? best : start;
    }

    private void drawSnapGrid(GuiGraphicsExtractor context, int screenW, int screenH) {
        int color;
        float[] xGuides = this.quarterGuides(screenW);
        float[] yGuides = this.quarterGuides(screenH);
        int quarterColor = ColorUtil.rgba(255, 255, 255, 95);
        int centerColor = ColorUtil.rgba(255, 255, 255, 135);
        for (float x : xGuides) {
            color = Math.abs(x - (float)screenW * 0.5f) < 0.5f ? centerColor : quarterColor;
            RenderUtil.drawRoundRect(context, x, 0.0f, 1.0f, (float)screenH, 0.0f, color);
        }
        for (float y : yGuides) {
            color = Math.abs(y - (float)screenH * 0.5f) < 0.5f ? centerColor : quarterColor;
            RenderUtil.drawRoundRect(context, 0.0f, y, (float)screenW, 1.0f, 0.0f, color);
        }
    }

    private float[] quarterGuides(int size) {
        float q = (float)size * 0.25f;
        return new float[]{0.0f, q, q * 2.0f, q * 3.0f, size};
    }

    private float clamp(float value, float min, float max) {
        return !Float.isFinite(value) ? min : Math.max(min, Math.min(max, value));
    }

    private boolean isHovered(float mouseX, float mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private record PopupLayout(float x, float y, float width, float height, List<PopupSettingLayout> settings) {
    }

    private record PopupSettingLayout(Setting setting, String key, float x, float y, float width, float height, float expandProgress) {
    }
}

