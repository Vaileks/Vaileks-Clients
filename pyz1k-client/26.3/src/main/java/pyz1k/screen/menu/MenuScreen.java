/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  org.joml.Matrix3x2fStack
 *  org.lwjgl.glfw.GLFW
 */
package pyz1k.screen.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;
import pyz1k.compat.LegacyInput;
import pyz1k.Pyz1k;
import pyz1k.abilitybind.AbilityBindManager;
import pyz1k.config.ConfigManager;
import pyz1k.cosmetics.CosmeticsManager;
import pyz1k.handrender.HandRenderManager;
import pyz1k.itemrender.ItemRenderManager;
import pyz1k.module.base.Module;
import pyz1k.module.impl.visuals.Interface;
import pyz1k.module.impl.visuals.Menu;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.menu.MenuCloseAnimation;
import pyz1k.screen.menu.UiAnimationEngine;
import pyz1k.screen.menu.sections.AbilityBindSection;
import pyz1k.screen.menu.sections.CategoryBarSection;
import pyz1k.screen.menu.sections.ConfigsSection;
import pyz1k.screen.menu.sections.CosmeticsSection;
import pyz1k.screen.menu.sections.HandRenderSection;
import pyz1k.screen.menu.sections.ItemRenderSection;
import pyz1k.screen.menu.sections.ModuleGridSection;
import pyz1k.screen.menu.sections.ThemeSection;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.animation.AnimatedValue;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ClientTheme;
import pyz1k.utilites.color.ColorUtil;

public final class MenuScreen
extends Screen {
    private static final float PANEL_WIDTH = 400.0f;
    private static final float PANEL_HEIGHT = 307.0f;
    private final Screen parentScreen;
    private final UiAnimationEngine animation = new UiAnimationEngine();
    private final CategoryBarSection categoryBarSection = new CategoryBarSection();
    private final ModuleGridSection moduleGridSection = new ModuleGridSection();
    private final ConfigsSection configsSection = new ConfigsSection();
    private final ItemRenderSection itemRenderSection = new ItemRenderSection();
    private final HandRenderSection handRenderSection = new HandRenderSection();
    private final AbilityBindSection abilityBindSection = new AbilityBindSection();
    private final ThemeSection themeSection = new ThemeSection();
    private final CosmeticsSection cosmeticsSection = new CosmeticsSection();
    private boolean closing;
    private boolean closeApplied;
    private boolean searchOpen;
    private boolean searchFocused;
    private String searchQuery = "";
    private final AnimatedValue searchAnim = new AnimatedValue();

    public MenuScreen(Screen parentScreen) {
        super((Component)Component.literal((String)"Pyz1k Menu"));
        this.parentScreen = parentScreen;
        this.applySavedMenuState();
    }

    public void requestClose() {
        if (this.moduleGridSection.isPopupOpen()) {
            this.moduleGridSection.requestClosePopup();
        } else if (this.moduleGridSection.isSettingsOpen()) {
            this.moduleGridSection.closeSettings();
        } else if (!this.closing && this.parentScreen == null && this.minecraft != null && this.minecraft.player != null) {
            MenuCloseAnimation.start(this);
            this.applyClose();
            this.minecraft.mouseHandler.grabMouse();
        } else {
            this.closing = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderSpatialAfterimage(GuiGraphicsExtractor context, float alpha, float yawDelta, float pitchDelta) {
        if (!(alpha <= 0.01f) && this.minecraft != null) {
            int fps = Math.max(1, this.minecraft.getFps());
            this.animation.beginFrame(fps);
            float guiScale = this.getInterfaceScale();
            float angle = Math.min(1.0f, Math.abs(yawDelta) / 105.0f + Math.abs(pitchDelta) / 80.0f);
            float perspectiveScale = 1.0f - angle * 0.11f;
            float panelWidth = this.getPanelWidth();
            float panelHeight = this.getPanelHeight();
            float spatialScale = guiScale * perspectiveScale;
            float maxShiftX = Math.max(0.0f, ((float)this.width - panelWidth * spatialScale) * 0.5f - 1.0f);
            float maxShiftY = Math.max(0.0f, ((float)this.height - panelHeight * spatialScale) * 0.5f - 1.0f);
            float shiftX = Math.clamp(-yawDelta * 4.25f, -maxShiftX, maxShiftX);
            float shiftY = Math.clamp(-pitchDelta * 3.65f, -maxShiftY, maxShiftY);
            Matrix3x2fStack matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate((float)this.width * 0.5f + shiftX, (float)this.height * 0.5f + shiftY);
            matrices.scale(spatialScale, spatialScale);
            matrices.translate((float)(-this.width) * 0.5f, (float)(-this.height) * 0.5f);
            try {
                float panelX = this.getPanelX(panelWidth);
                float panelY = ((float)this.height - panelHeight) * 0.5f;
                float contentX = panelX;
                float contentY = panelY + 43.0f;
                float contentHeight = panelHeight - 43.0f;
                float contentWidth = panelWidth;
                HudVisualStyle.drawMenuPanel(context, panelX, panelY, panelWidth, panelHeight, panelWidth, alpha);
                boolean itemRenderActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ITEM_RENDER;
                boolean handRenderActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.HAND_RENDER;
                boolean abilityBindActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ABILITY_BIND;
                boolean configsActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.CONFIGS;
                boolean themesActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.THEMES;
                boolean cosmeticsActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.COSMETICS;
                this.configsSection.setActive(configsActive);
                if (itemRenderActive) {
                    this.itemRenderSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, -10000, -10000, alpha);
                } else if (handRenderActive) {
                    this.handRenderSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, -10000, -10000, alpha);
                } else if (abilityBindActive) {
                    this.abilityBindSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, -10000, -10000, alpha);
                } else if (configsActive) {
                    this.configsSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, -10000, -10000, alpha);
                } else if (themesActive) {
                    this.themeSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, -10000, -10000, alpha);
                } else if (cosmeticsActive) {
                    this.cosmeticsSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, -10000, -10000, alpha);
                } else {
                    this.moduleGridSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, -10000, -10000, alpha, this.categoryBarSection.getActiveTab());
                }
                this.categoryBarSection.render(context, this.animation, panelX, panelY, panelWidth, panelHeight, -10000, -10000, alpha);
                this.renderHeaderActions(context, panelX, panelY, panelWidth, alpha);
                if (!(configsActive || itemRenderActive || handRenderActive || abilityBindActive)) {
                    this.moduleGridSection.renderOverlay(context, this.animation, -10000, -10000, alpha);
                }
            }
            finally {
                matrices.popMatrix();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int fps;
        ClientTheme.updateDynamicColors();
        int n = fps = this.minecraft != null ? this.minecraft.getFps() : 60;
        if (fps <= 0) {
            fps = 60;
        }
        this.animation.beginFrame(fps);
        if (this.minecraft != null && this.minecraft.level == null) {
            this.extractPanorama(context, delta);
        }
        float guiScale = this.getInterfaceScale();
        int logicalMouseX = (int)Math.round(this.toLogicalX(mouseX, guiScale));
        int logicalMouseY = (int)Math.round(this.toLogicalY(mouseY, guiScale));
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate((float)this.width * 0.5f, (float)this.height * 0.5f);
        matrices.scale(guiScale, guiScale);
        matrices.translate((float)(-this.width) * 0.5f, (float)(-this.height) * 0.5f);
        try {
            float panelVisibility;
            float categoriesVisibility;
            float visibility = this.animation.animate01("menu:visibility", this.closing ? 0.0f : 1.0f, 12.0f, this.closing ? 1.0f : 0.0f);
            if (this.closing) {
                float closeProgress = 1.0f - visibility;
                categoriesVisibility = 1.0f - this.clamp01(closeProgress / 0.62f);
                panelVisibility = 1.0f - this.clamp01((closeProgress - 0.52f) / 0.48f);
            } else {
                float openProgress = visibility;
                panelVisibility = this.clamp01(openProgress / 0.48f);
                categoriesVisibility = this.clamp01((openProgress - 0.32f) / 0.68f);
            }
            panelVisibility = this.smoothStep(panelVisibility);
            categoriesVisibility = this.smoothStep(categoriesVisibility);
            if (!this.closing || this.closeApplied || !(panelVisibility <= 0.02f)) {
                float panelWidth = this.getPanelWidth();
                float panelHeight = this.getPanelHeight();
                float panelX = this.getPanelX(panelWidth);
                float panelYBase = ((float)this.height - panelHeight) / 2.0f;
                float panelYOffset = (1.0f - panelVisibility) * 14.0f;
                float panelY = panelYBase + panelYOffset;
                float contentX = panelX;
                float contentY = panelY + 43.0f;
                float contentHeight = panelHeight - 43.0f;
                float contentWidth = panelWidth;
                HudVisualStyle.drawMenuPanel(context, panelX, panelY, panelWidth, panelHeight, panelWidth, panelVisibility);
                boolean itemRenderActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ITEM_RENDER;
                boolean handRenderActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.HAND_RENDER;
                boolean abilityBindActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ABILITY_BIND;
                boolean configsActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.CONFIGS;
                boolean themesActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.THEMES;
                boolean cosmeticsActive = this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.COSMETICS;
                this.configsSection.setActive(configsActive);
                if (itemRenderActive) {
                    this.itemRenderSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, logicalMouseX, logicalMouseY, categoriesVisibility);
                } else if (handRenderActive) {
                    this.handRenderSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, logicalMouseX, logicalMouseY, categoriesVisibility);
                } else if (abilityBindActive) {
                    this.abilityBindSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, logicalMouseX, logicalMouseY, categoriesVisibility);
                } else if (configsActive) {
                    this.configsSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, logicalMouseX, logicalMouseY, categoriesVisibility);
                } else if (themesActive) {
                    this.themeSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, logicalMouseX, logicalMouseY, categoriesVisibility);
                } else if (cosmeticsActive) {
                    this.cosmeticsSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, logicalMouseX, logicalMouseY, categoriesVisibility);
                } else {
                    this.moduleGridSection.render(context, this.animation, contentX, contentY, contentWidth, contentHeight, logicalMouseX, logicalMouseY, categoriesVisibility, this.categoryBarSection.getActiveTab());
                }
                this.categoryBarSection.render(context, this.animation, panelX, panelY, panelWidth, panelHeight, logicalMouseX, logicalMouseY, categoriesVisibility);
                this.renderHeaderActions(context, panelX, panelY, panelWidth, categoriesVisibility);
                if (!(configsActive || itemRenderActive || handRenderActive || abilityBindActive)) {
                    this.moduleGridSection.renderOverlay(context, this.animation, logicalMouseX, logicalMouseY, categoriesVisibility);
                }
                this.renderSearchHint(context, panelX, panelWidth, panelY + panelHeight, categoriesVisibility);
                this.renderSearchOverlay(context);
                this.persistMenuState();
                Pyz1k.getInstance().getConfigManager().flushAutoSave();
                ItemRenderManager.getInstance().flushAutoSave();
                HandRenderManager.getInstance().flushAutoSave();
                AbilityBindManager.getInstance().flushAutoSave();
                CosmeticsManager.getInstance().flushAutoSave();
                return;
            }
            this.applyClose();
        }
        finally {
            matrices.popMatrix();
        }
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        MouseButtonEvent logicalClick = this.toLogicalClick(click);
        if (this.searchOpen) {
            return this.handleSearchClick(logicalClick);
        }
        float panelWidth = this.getPanelWidth();
        float panelHeight = this.getPanelHeight();
        float panelX = this.getPanelX(panelWidth);
        float panelY = ((float)this.height - panelHeight) / 2.0f;
        float contentX = panelX;
        float contentY = panelY + 43.0f;
        float contentHeight = panelHeight - 43.0f;
        float contentWidth = panelWidth;
        if (pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button()) == 0 && logicalClick.y() >= (double)(panelY + 9.0f) && logicalClick.y() <= (double)(panelY + 34.0f)) {
            if (logicalClick.x() >= (double)(panelX + panelWidth - 33.0f) && logicalClick.x() <= (double)(panelX + panelWidth - 9.0f)) {
                this.requestClose();
                return true;
            }
            if (logicalClick.x() >= (double)(panelX + panelWidth - 64.0f) && logicalClick.x() <= (double)(panelX + panelWidth - 40.0f)) {
                this.openSearch();
                return true;
            }
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ITEM_RENDER) {
            if (this.itemRenderSection.handleClick(logicalClick, contentX, contentY, contentWidth, contentHeight)) {
                return true;
            }
        } else if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.HAND_RENDER) {
            if (this.handRenderSection.handleClick(logicalClick, contentX, contentY, contentWidth, contentHeight)) {
                return true;
            }
        } else if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ABILITY_BIND) {
            if (this.abilityBindSection.handleClick(logicalClick, contentX, contentY, contentWidth, contentHeight)) {
                return true;
            }
        } else if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.CONFIGS) {
            if (this.configsSection.handleClick(logicalClick, contentX, contentY, contentWidth, contentHeight)) {
                return true;
            }
        } else if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.THEMES) {
            if (this.moduleGridSection.handleClick(logicalClick, contentX, contentY, contentWidth, contentHeight, this.categoryBarSection.getActiveTab())) {
                return true;
            }
            if (this.themeSection.handleClick(logicalClick, contentX, contentY, contentWidth, contentHeight, this.moduleGridSection)) {
                return true;
            }
        } else if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.COSMETICS ? this.cosmeticsSection.handleClick(logicalClick, contentX, contentY, contentWidth, contentHeight) : this.moduleGridSection.handleClick(logicalClick, contentX, contentY, contentWidth, contentHeight, this.categoryBarSection.getActiveTab())) {
            return true;
        }
        CategoryBarSection.TopTab previousTab = this.categoryBarSection.getActiveTab();
        if (this.categoryBarSection.handleClick(logicalClick, panelX, panelY, panelWidth, panelHeight)) {
            this.moduleGridSection.closeSettings();
            CategoryBarSection.TopTab nextTab = this.categoryBarSection.getActiveTab();
            if (previousTab != nextTab) {
                if (previousTab == CategoryBarSection.TopTab.ITEM_RENDER) {
                    this.itemRenderSection.commitEdit();
                } else if (previousTab == CategoryBarSection.TopTab.HAND_RENDER) {
                    this.handRenderSection.commitEdit();
                } else if (previousTab == CategoryBarSection.TopTab.ABILITY_BIND) {
                    this.abilityBindSection.commitEdit();
                }
            }
            return true;
        }
        return pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button()) == 0 ? true : super.mouseClicked(click, doubleClick);
    }

    public boolean keyPressed(KeyEvent key) {
        if (this.isCtrlDown() && pyz1k.compat.LegacyInput.fromNativeKey(key.key()) == 70) {
            this.openSearch();
            return true;
        }
        if (this.searchOpen) {
            return this.handleSearchKeyPressed(key);
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ITEM_RENDER) {
            return this.itemRenderSection.handleKeyPressed(key) ? true : super.keyPressed(key);
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.HAND_RENDER) {
            return this.handRenderSection.handleKeyPressed(key) ? true : super.keyPressed(key);
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ABILITY_BIND) {
            return this.abilityBindSection.handleKeyPressed(key) ? true : super.keyPressed(key);
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.COSMETICS) {
            return this.cosmeticsSection.handleKeyPressed(key) ? true : super.keyPressed(key);
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.THEMES && this.themeSection.handleKeyPressed(key)) {
            return true;
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.CONFIGS) {
            return this.configsSection.handleKeyPressed(key) ? true : super.keyPressed(key);
        }
        Interface interfaceModule = Pyz1k.getInstance().getModuleManager().getModule(Interface.class);
        if (interfaceModule != null && interfaceModule.handleHudEditorKeyPressed(key)) {
            return true;
        }
        return this.moduleGridSection.handleKeyPressed(key) ? true : super.keyPressed(key);
    }

    public boolean keyReleased(KeyEvent key) {
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ITEM_RENDER || this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.HAND_RENDER || this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ABILITY_BIND) {
            return super.keyReleased(key);
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.CONFIGS) {
            return super.keyReleased(key);
        }
        return this.moduleGridSection.handleKeyReleased(key) ? true : super.keyReleased(key);
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        MouseButtonEvent logicalClick = this.toLogicalClick(click);
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ITEM_RENDER) {
            return this.itemRenderSection.handleMouseReleased(pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button())) ? true : super.mouseReleased(click);
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.HAND_RENDER) {
            return this.handRenderSection.handleMouseReleased(pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button())) ? true : super.mouseReleased(click);
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ABILITY_BIND) {
            return this.abilityBindSection.handleMouseReleased(pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button())) ? true : super.mouseReleased(click);
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.COSMETICS) {
            return this.cosmeticsSection.handleMouseReleased(pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button())) ? true : super.mouseReleased(click);
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.CONFIGS) {
            return this.configsSection.handleMouseReleased(pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button())) ? true : super.mouseReleased(click);
        }
        return this.moduleGridSection.handleMouseReleased(pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button())) ? true : super.mouseReleased(click);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float scale = this.getInterfaceScale();
        double logicalMouseX = this.toLogicalX(mouseX, scale);
        double logicalMouseY = this.toLogicalY(mouseY, scale);
        float panelWidth = this.getPanelWidth();
        float panelHeight = this.getPanelHeight();
        float panelX = this.getPanelX(panelWidth);
        float panelY = ((float)this.height - panelHeight) / 2.0f;
        float contentX = panelX;
        float contentY = panelY + 43.0f;
        float contentHeight = panelHeight - 43.0f;
        float contentWidth = panelWidth;
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ITEM_RENDER ? this.itemRenderSection.handleScroll(logicalMouseX, logicalMouseY, verticalAmount, contentX, contentY, contentWidth, contentHeight) : (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.HAND_RENDER ? this.handRenderSection.handleScroll(logicalMouseX, logicalMouseY, verticalAmount, contentX, contentY, contentWidth, contentHeight) : (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ABILITY_BIND ? this.abilityBindSection.handleScroll(logicalMouseX, logicalMouseY, verticalAmount, contentX, contentY, contentWidth, contentHeight) : (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.CONFIGS ? this.configsSection.handleScroll(logicalMouseX, logicalMouseY, verticalAmount, contentX, contentY, contentWidth, contentHeight) : (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.THEMES ? this.themeSection.handleScroll(logicalMouseX, logicalMouseY, verticalAmount, contentX, contentY, contentWidth, contentHeight) : (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.COSMETICS ? this.cosmeticsSection.handleScroll(logicalMouseX, logicalMouseY, verticalAmount, contentX, contentY, contentWidth, contentHeight) : this.moduleGridSection.handleScroll(logicalMouseX, logicalMouseY, verticalAmount, contentX, contentY, contentWidth, contentHeight, this.categoryBarSection.getActiveTab()))))))) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean charTyped(CharacterEvent input) {
        int codePoint = this.resolveCharCodePoint(input);
        if (this.searchOpen) {
            if (this.searchFocused && codePoint >= 32 && codePoint != 127) {
                this.searchQuery = this.searchQuery + new String(Character.toChars(codePoint));
            }
            return true;
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ITEM_RENDER && this.itemRenderSection.handleCharTyped(codePoint)) {
            return true;
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.HAND_RENDER && this.handRenderSection.handleCharTyped(codePoint)) {
            return true;
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ABILITY_BIND && this.abilityBindSection.handleCharTyped(codePoint)) {
            return true;
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.COSMETICS && this.cosmeticsSection.handleCharTyped(codePoint)) {
            return true;
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.THEMES && this.themeSection.handleCharTyped(codePoint)) {
            return true;
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.CONFIGS && this.configsSection.handleCharTyped(codePoint)) {
            return true;
        }
        Interface interfaceModule = Pyz1k.getInstance().getModuleManager().getModule(Interface.class);
        return interfaceModule != null && interfaceModule.handleHudEditorCharTyped(codePoint) ? true : super.charTyped(input);
    }

    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        MouseButtonEvent logicalClick = this.toLogicalClick(click);
        float panelWidth = this.getPanelWidth();
        float panelHeight = this.getPanelHeight();
        float panelX = this.getPanelX(panelWidth);
        float panelY = ((float)this.height - panelHeight) / 2.0f;
        float contentX = panelX;
        float contentY = panelY + 43.0f;
        float contentHeight = panelHeight - 43.0f;
        float contentWidth = panelWidth;
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ITEM_RENDER && this.itemRenderSection.handleMouseDragged(logicalClick.x(), logicalClick.y(), pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button()), contentX, contentY, contentWidth, contentHeight)) {
            return true;
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.HAND_RENDER && this.handRenderSection.handleMouseDragged(logicalClick.x(), logicalClick.y(), pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button()), contentX, contentY, contentWidth, contentHeight)) {
            return true;
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.ABILITY_BIND && this.abilityBindSection.handleMouseDragged(logicalClick.x(), logicalClick.y(), pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button()), contentX, contentY, contentWidth, contentHeight)) {
            return true;
        }
        if (this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.COSMETICS && this.cosmeticsSection.handleMouseDragged(logicalClick.x(), logicalClick.y(), pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button()), contentX, contentY, contentWidth, contentHeight)) {
            return true;
        }
        return this.categoryBarSection.getActiveTab() == CategoryBarSection.TopTab.CONFIGS && this.configsSection.handleMouseDragged(logicalClick.x(), logicalClick.y(), pyz1k.compat.LegacyInput.fromNativeMouse(logicalClick.button()), contentX, contentY, contentWidth, contentHeight) ? true : super.mouseDragged(click, deltaX, deltaY);
    }

    private void applySavedMenuState() {
        ConfigManager configManager = Pyz1k.getInstance().getConfigManager();
        ConfigManager.MenuState state = configManager.getMenuState();
        if (state.activeTopTab != null) {
            try {
                this.categoryBarSection.setActiveTab(CategoryBarSection.TopTab.valueOf(state.activeTopTab));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        this.moduleGridSection.applyUiState(state.moduleTabScroll, state.selectExpanded, state.multiSelectExpanded);
        this.configsSection.applyUiState(state.configsSelectedConfig, state.configsScroll);
    }

    private void persistMenuState() {
        ConfigManager configManager = Pyz1k.getInstance().getConfigManager();
        ConfigManager.MenuState state = new ConfigManager.MenuState();
        state.activeTopTab = this.categoryBarSection.getActiveTab().name();
        state.configsSelectedConfig = this.configsSection.getSelectedConfigName();
        state.configsScroll = this.configsSection.getScrollTarget();
        state.moduleTabScroll = this.moduleGridSection.captureTabScrollState();
        state.selectExpanded = this.moduleGridSection.captureSelectExpandedState();
        state.multiSelectExpanded = this.moduleGridSection.captureMultiSelectExpandedState();
        configManager.updateMenuState(state);
    }

    private float clamp01(float value) {
        return Math.clamp(value, 0.0f, 1.0f);
    }

    private int resolveCharCodePoint(CharacterEvent input) {
        return input == null ? 0 : input.codepoint();
    }

    private float smoothStep(float value) {
        float x = this.clamp01(value);
        return x * x * (3.0f - 2.0f * x);
    }

    private boolean handleSearchKeyPressed(KeyEvent key) {
        int code = pyz1k.compat.LegacyInput.fromNativeKey(key.key());
        if (code == 256) {
            this.searchOpen = false;
            this.searchFocused = false;
            this.searchQuery = "";
            return true;
        }
        if (!this.searchFocused) {
            return true;
        }
        if (code == 259) {
            if (!this.searchQuery.isEmpty()) {
                this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
            }
            return true;
        }
        if (code != 257 && code != 335) {
            return true;
        }
        List<Module> matches = this.collectSearchMatches();
        if (!matches.isEmpty()) {
            this.focusSearchResult(matches.get(0));
        }
        return true;
    }

    private boolean handleSearchClick(MouseButtonEvent click) {
        if (pyz1k.compat.LegacyInput.fromNativeMouse(click.button()) != 0) {
            return true;
        }
        float panelWidth = this.getPanelWidth();
        float panelHeight = this.getPanelHeight();
        float panelY = ((float)this.height - panelHeight) / 2.0f;
        float boxW = Math.min(236.0f, panelWidth - 44.0f);
        float panelX = this.getPanelX(panelWidth);
        float boxX = panelX + (panelWidth - boxW) * 0.5f;
        float boxY = panelY + 42.0f;
        float rowY = boxY + 28.0f;
        float rowH = 17.0f;
        List<Module> matches = this.collectSearchMatches();
        int rows = Math.min(7, matches.size());
        if (click.x() >= (double)boxX && click.x() <= (double)(boxX + boxW) && click.y() >= (double)boxY && click.y() <= (double)(boxY + 22.0f)) {
            this.searchFocused = true;
            return true;
        }
        for (int i = 0; i < rows; ++i) {
            float y = rowY + (float)i * (rowH + 2.5f);
            if (!(click.x() >= (double)boxX) || !(click.x() <= (double)(boxX + boxW)) || !(click.y() >= (double)y) || !(click.y() <= (double)(y + rowH))) continue;
            this.focusSearchResult(matches.get(i));
            return true;
        }
        if (!(click.x() < (double)boxX || click.x() > (double)(boxX + boxW) || click.y() < (double)boxY || click.y() > (double)(rowY + (float)rows * (rowH + 3.0f)))) {
            this.searchFocused = false;
        } else {
            this.searchOpen = false;
            this.searchFocused = false;
            this.searchQuery = "";
        }
        return true;
    }

    private void renderSearchOverlay(GuiGraphicsExtractor context) {
        if (this.searchOpen) {
            int textColor;
            float searchVisibility = this.searchAnim.update(1.0f, 0.08f, System.nanoTime());
            float eased = this.smoothStep(searchVisibility);
            float panelWidth = this.getPanelWidth();
            float panelHeight = this.getPanelHeight();
            float panelX = this.getPanelX(panelWidth);
            float panelY = ((float)this.height - panelHeight) / 2.0f;
            RenderUtil.drawRoundRect(context, panelX, panelY, panelWidth, panelHeight, 6.0f, ColorUtil.rgba(0, 0, 0, Math.round(132.0f * eased)));
            float boxW = Math.min(236.0f, panelWidth - 44.0f);
            float boxX = panelX + (panelWidth - boxW) * 0.5f;
            float boxY = panelY + 42.0f - (1.0f - eased) * 8.0f;
            float inputH = 22.0f;
            float radius = 4.0f;
            RenderUtil.drawShadow(context, boxX, boxY, boxW, inputH, radius, ColorUtil.multiplyAlpha(ClientColors.MENU_SHADOW, 0.78f * eased), 1.0f, 8.0f, 0.0f, 0.0f);
            RenderUtil.drawRoundRect(context, boxX, boxY, boxW, inputH, radius, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, 0.98f * eased));
            RenderUtil.drawBorder(context, boxX, boxY, boxW, inputH, radius, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND_OUTLINE, eased), 1.0f, 0.0f, 0.5f);
            boolean showPlaceholder = this.searchQuery.isEmpty() && !this.searchFocused;
            String label = showPlaceholder ? "Search modules..." : this.searchQuery;
            int n = textColor = showPlaceholder ? ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, eased) : ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, eased);
            if (!label.isEmpty()) {
                RenderUtil.drawText(context, label, boxX + 11.0f, boxY + 7.2f, textColor, GFonts.MEDIUM, 7);
            }
            if (this.searchFocused && (System.currentTimeMillis() / 520L & 1L) == 0L) {
                float caretBaseX = boxX + 11.0f;
                float caretX = caretBaseX + (this.searchQuery.isEmpty() ? 0.0f : RenderUtil.getTextWidth(this.searchQuery, GFonts.MEDIUM, 7)) + 1.5f;
                RenderUtil.drawRoundRect(context, caretX, boxY + 6.0f, 1.0f, inputH - 12.0f, 0.5f, ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, eased * 0.86f));
            }
            List<Module> matches = this.collectSearchMatches();
            float rowY = boxY + 28.0f;
            float rowH = 17.0f;
            int rows = Math.min(7, matches.size());
            for (int i = 0; i < rows; ++i) {
                Module module = matches.get(i);
                float rowEase = this.smoothStep(Math.clamp((eased - (float)i * 0.045f) / 0.82f, 0.0f, 1.0f));
                float y = rowY + (float)i * (rowH + 2.5f) + (1.0f - rowEase) * 5.0f;
                float appear = (1.0f - (float)i * 0.055f) * rowEase;
                int bg = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, appear * 0.96f);
                int border = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND_OUTLINE, appear);
                RenderUtil.drawRoundRect(context, boxX, y, boxW, rowH, 3.5f, bg);
                RenderUtil.drawBorder(context, boxX, y, boxW, rowH, 3.5f, border, 1.0f, 0.0f, 0.5f);
                this.drawHighlightedModuleName(context, module.getName(), this.searchQuery, boxX + 8.0f, y + 5.0f, appear);
                RenderUtil.drawText(context, module.getCategory().name(), boxX + boxW - 54.0f, y + 5.3f, ColorUtil.rgba(132, 136, 154, Math.round(205.0f * appear)), GFonts.MEDIUM, 6);
            }
            if (rows == 0 && !this.searchQuery.isEmpty()) {
                RenderUtil.drawRoundRect(context, boxX, rowY, boxW, rowH, 3.5f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, 0.92f));
                RenderUtil.drawText(context, "No modules found", boxX + 8.0f, rowY + 5.0f, ColorUtil.rgba(160, 164, 180, 230), GFonts.MEDIUM, 7);
            }
        }
    }

    private void renderSearchHint(GuiGraphicsExtractor context, float panelX, float panelWidth, float panelBottomY, float alpha) {
        String hint = "CTRL + F - открыть поиск модулей";
        float textSize = 6.0f;
        float textW = RenderUtil.getTextWidth(hint, GFonts.MEDIUM, textSize);
        float x = panelX + (panelWidth - textW) * 0.5f;
        float y = panelBottomY + 6.0f;
        RenderUtil.drawText(context, hint, x, y, ColorUtil.rgba(255, 255, 255, Math.round(185.0f * alpha)), GFonts.MEDIUM, textSize);
    }

    private List<Module> collectSearchMatches() {
        List<Module> modules = Pyz1k.getInstance().getModuleManager().getModules();
        String query = this.searchQuery.trim().toLowerCase(Locale.ROOT);
        ArrayList<Module> matches = new ArrayList<Module>();
        if (query.isEmpty()) {
            return matches;
        }
        for (Module module : modules) {
            String name = module.getName();
            if (!name.toLowerCase(Locale.ROOT).contains(query)) continue;
            matches.add(module);
        }
        matches.sort((a, b) -> {
            boolean bp;
            String an = a.getName().toLowerCase(Locale.ROOT);
            String bn = b.getName().toLowerCase(Locale.ROOT);
            boolean ap = !query.isEmpty() && an.startsWith(query);
            boolean bl = bp = !query.isEmpty() && bn.startsWith(query);
            if (ap != bp) {
                return ap ? -1 : 1;
            }
            return an.compareTo(bn);
        });
        return matches;
    }

    private void drawHighlightedModuleName(GuiGraphicsExtractor context, String name, String query, float x, float y, float alpha) {
        String normalizedQuery = query == null ? "" : query.trim();
        int baseColor = ColorUtil.multiplyAlpha(ClientColors.TEXT_PRIMARY, alpha);
        if (normalizedQuery.isEmpty()) {
            RenderUtil.drawText(context, name, x, y, baseColor, GFonts.MEDIUM, 7);
        } else {
            String lowerQuery;
            String lowerName = name.toLowerCase(Locale.ROOT);
            int start = lowerName.indexOf(lowerQuery = normalizedQuery.toLowerCase(Locale.ROOT));
            if (start < 0) {
                RenderUtil.drawText(context, name, x, y, baseColor, GFonts.MEDIUM, 7);
            } else {
                String before = name.substring(0, start);
                String match = name.substring(start, start + normalizedQuery.length());
                String after = name.substring(start + normalizedQuery.length());
                float cursor = x;
                if (!before.isEmpty()) {
                    RenderUtil.drawText(context, before, cursor, y, baseColor, GFonts.MEDIUM, 7);
                    cursor += RenderUtil.getTextWidth(before, GFonts.MEDIUM, 7);
                }
                int accentColor = ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT, alpha);
                RenderUtil.drawText(context, match, cursor, y, accentColor, GFonts.MEDIUM, 7);
                cursor += RenderUtil.getTextWidth(match, GFonts.MEDIUM, 7);
                if (!after.isEmpty()) {
                    RenderUtil.drawText(context, after, cursor, y, baseColor, GFonts.MEDIUM, 7);
                }
            }
        }
    }

    private void focusSearchResult(Module module) {
        float panelHeight;
        float panelWidth = this.getPanelWidth();
        float contentWidth = panelWidth;
        CategoryBarSection.TopTab tab = this.moduleGridSection.focusModule(module, contentWidth, (panelHeight = this.getPanelHeight()) - 43.0f);
        if (tab != null) {
            this.categoryBarSection.setActiveTab(tab);
            this.moduleGridSection.openSettings(module);
        }
        this.searchOpen = false;
        this.searchFocused = false;
        this.searchQuery = "";
        this.searchAnim.setValue(0.0f);
    }

    private boolean isCtrlDown() {
        if (this.minecraft != null && this.minecraft.getWindow() != null) {
            long handle = this.minecraft.getWindow().handle();
            return LegacyInput.glfwGetKey((long)handle, (int)341) == 1 || LegacyInput.glfwGetKey((long)handle, (int)345) == 1;
        }
        return false;
    }

    private void openSearch() {
        this.searchOpen = true;
        this.searchFocused = false;
        this.searchQuery = "";
        this.searchAnim.setValue(0.0f);
    }

    private void renderHeaderActions(GuiGraphicsExtractor context, float panelX, float panelY, float panelWidth, float alpha) {
        float searchX = panelX + panelWidth - 64.0f;
        float closeX = panelX + panelWidth - 33.0f;
        float y = panelY + 9.0f;
        RenderUtil.drawRoundRect(context, searchX, y, 24.0f, 25.0f, 4.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha));
        RenderUtil.drawImage(context, "textures/gui/icons/search.png", searchX + 8.0f, y + 8.0f, 8.0f, 8.0f, 0.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT_SECOND, alpha), 1.0f);
        RenderUtil.drawRoundRect(context, closeX, y, 24.0f, 25.0f, 4.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_BACKGROUND, alpha));
        RenderUtil.drawText(context, "×", closeX + 8.0f, y + 5.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_INACTIVE, alpha), GFonts.MEDIUM, 13.0f);
    }

    private float getPanelWidth() {
        float scale = Math.max(0.01f, this.getInterfaceScale());
        return Math.min(((float)this.width - 32.0f) / scale, 400.0f);
    }

    private float getPanelX(float panelWidth) {
        return ((float)this.width - panelWidth) * 0.5f;
    }

    private float getPanelHeight() {
        float scale = Math.max(0.01f, this.getInterfaceScale());
        return Math.min(((float)this.height - 48.0f) / scale, 307.0f);
    }

    private MouseButtonEvent toLogicalClick(MouseButtonEvent click) {
        float scale = this.getInterfaceScale();
        return new MouseButtonEvent(this.toLogicalX(click.x(), scale), this.toLogicalY(click.y(), scale), click.buttonInfo());
    }

    private double toLogicalX(double value, float scale) {
        return (double)this.width * 0.5 + (value - (double)this.width * 0.5) / (double)Math.max(0.01f, scale);
    }

    private double toLogicalY(double value, float scale) {
        return (double)this.height * 0.5 + (value - (double)this.height * 0.5) / (double)Math.max(0.01f, scale);
    }

    private float getInterfaceScale() {
        Menu menu = Pyz1k.getInstance().getModuleManager().getModule(Menu.class);
        return menu == null ? 1.0f : menu.getInterfaceScale();
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }

    public void onClose() {
        this.requestClose();
    }

    private void applyClose() {
        if (!this.closeApplied) {
            this.itemRenderSection.commitEdit();
            this.handRenderSection.commitEdit();
            this.abilityBindSection.commitEdit();
            ItemRenderManager.getInstance().saveNow();
            HandRenderManager.getInstance().saveNow();
            AbilityBindManager.getInstance().saveNow();
            CosmeticsManager.getInstance().saveNow();
            this.closeApplied = true;
            Menu menuModule = Pyz1k.getInstance().getModuleManager().getModule(Menu.class);
            if (menuModule != null) {
                menuModule.setEnable(false);
            }
            if (this.minecraft != null) {
                this.minecraft.gui.setScreen(this.parentScreen);
            }
        }
    }

    public boolean isPauseScreen() {
        return false;
    }
}

