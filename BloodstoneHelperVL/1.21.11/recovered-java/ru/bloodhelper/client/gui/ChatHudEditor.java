/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.gui;

import net.minecraft.class_1041;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_310;
import net.minecraft.class_332;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.HudInteraction;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.MusicWidget;
import ru.bloodhelper.client.hud.WaypointsWidget;
import ru.bloodhelper.client.inventory.InventoryUiDrag;
import ru.bloodhelper.client.modules.CombatHudModule;

public final class ChatHudEditor {
    private final HudInteraction interaction = new HudInteraction();
    private final InventoryUiDrag panelDrag = new InventoryUiDrag();
    private final long opened = System.nanoTime();
    private long frame;
    private boolean editing;
    private boolean toggleHeld;
    private double enabledMotion;
    private double hoverMotion;
    private HudRenderer.Bounds toggleBounds;

    public ChatHudEditor() {
        this.editing = BloodHelperClient.config().hudEditor.showChatEditor && BloodHelperClient.config().hudEditor.chatEditingEnabled;
    }

    public boolean isEditing() {
        this.syncVisibility();
        return this.editing;
    }

    public HudRenderer.Bounds toggleBounds() {
        this.syncVisibility();
        return this.toggleBounds;
    }

    private void syncVisibility() {
        if (BloodHelperClient.config().hudEditor.showChatEditor) {
            return;
        }
        if (this.editing || this.toggleHeld || this.toggleBounds != null) {
            this.resized();
        }
        this.editing = false;
    }

    private boolean available() {
        class_310 mc = class_310.method_1551();
        return mc.field_1724 != null && !mc.field_1690.field_1842;
    }

    public void render(class_332 g, int mouseX, int mouseY) {
        this.syncVisibility();
        if (!this.available()) {
            this.resized();
            return;
        }
        g.method_71048();
        HudRenderer.renderChat(g);
        if (!BloodHelperClient.config().hudEditor.showChatEditor) {
            return;
        }
        if (this.editing) {
            this.interaction.render(g, mouseX, mouseY, true);
        }
        g.method_71048();
        this.drawToggle(g, mouseX, mouseY);
    }

    private void drawToggle(class_332 g, int mouseX, int mouseY) {
        ClientConfig c = BloodHelperClient.config();
        long now = System.nanoTime();
        double dt = this.frame == 0L ? 0.0 : Math.clamp((double)(now - this.frame) / 1.0E9, 0.0, 0.1);
        this.frame = now;
        double blend = c.animations ? 1.0 - Math.exp(-dt * 22.0 * (240.0 / RenderQuality.animationMillis(240.0))) : 1.0;
        this.enabledMotion += ((double)(this.editing ? 1 : 0) - this.enabledMotion) * blend;
        double enter = c.animations ? 1.0 - Math.pow(1.0 - Math.clamp((double)(now - this.opened) / (RenderQuality.animationMillis(240.0) * 1000000.0), 0.0, 1.0), 3.0) : 1.0;
        int opacity = (int)Math.round(255.0 * enter);
        String message = CombatHudModule.combatLayout(c) ? HudStyle.tr("\u0420\u0435\u0434\u0430\u043a\u0442\u043e\u0440 \u0431\u043e\u0435\u0432\u043e\u0433\u043e HUD", "Combat HUD editor") : HudStyle.tr("\u0420\u0435\u0434\u0430\u043a\u0442\u043e\u0440 HUD", "HUD editor");
        CustomFont font = CustomFont.medium();
        int width = Math.min(Math.max(90, g.method_51421() - 24), (int)Math.ceil(font.width(message, 8.0f)) + 42);
        int height = 20;
        int x = (g.method_51421() - width) / 2;
        int y = Math.min(Math.max(0, g.method_51443() - height - 16), g.method_51443() / 2 + Math.max(30, (int)Math.round((double)g.method_51443() * 0.06)) + (int)Math.round((1.0 - enter) * 7.0));
        x = c.hudEditor.chatPanel.x(x, g.method_51421(), width);
        y = c.hudEditor.chatPanel.y(y, g.method_51443(), height);
        this.toggleBounds = new HudRenderer.Bounds(x, y, width, height);
        boolean hovered = this.toggleBounds.contains(mouseX, mouseY);
        this.hoverMotion += ((double)(hovered ? 1 : 0) - this.hoverMotion) * blend;
        int surface = ThemeColors.surface(-14407890);
        int edge = UiDraw.blend(-9867137, ThemeColors.icon(c.accent), this.hoverMotion * 0.5 + this.enabledMotion * 0.25);
        if (RenderQuality.shadows()) {
            UiDraw.rounded(g, x, y + 2, width, height, 5, UiDraw.alpha(0, opacity / 7));
        }
        LiquidGlass.surfaceMenu(g, x, y, width, height, 5, UiDraw.alpha(surface, (int)((double)opacity * 0.93)), c);
        if (!"OUTLINE".equals(c.surfaceStyle)) {
            UiDraw.outline(g, x, y, width, height, 5, UiDraw.alpha(edge, (int)((double)opacity * (0.35 + this.hoverMotion * 0.15))));
        }
        String label = font.ellipsize(message, 8.0f, Math.max(1, width - 42));
        font.draw(g, label, x + 8, (float)y + ((float)height - font.lineHeight(8.0f)) / 2.0f, 8.0f, UiDraw.alpha(-921101, opacity));
        int switchX = x + width - 29;
        int switchY = y + 4;
        int track = UiDraw.blend(-13618115, c.accent, this.enabledMotion);
        int border = UiDraw.blend(-10788242, UiDraw.blend(c.accent, -16250354, 0.18), this.enabledMotion);
        UiDraw.border(g, switchX, switchY, 22, 12, 6, UiDraw.alpha(track, opacity), UiDraw.alpha(border, opacity));
        double brightness = (0.2126 * (double)(c.accent >>> 16 & 0xFF) + 0.7152 * (double)(c.accent >>> 8 & 0xFF) + 0.0722 * (double)(c.accent & 0xFF)) / 255.0;
        int knob = UiDraw.blend(-4538165, brightness > 0.55 ? -15261652 : -526086, this.enabledMotion);
        g.method_51448().pushMatrix();
        g.method_51448().translate((float)((double)(switchX + 6) + this.enabledMotion * 10.0), (float)(switchY + 6));
        UiDraw.rounded(g, -4, -4, 8, 8, 4, UiDraw.alpha(knob, opacity));
        g.method_51448().popMatrix();
    }

    public boolean click(class_11909 event) {
        this.syncVisibility();
        if (!this.available()) {
            return false;
        }
        if (event.method_74245() == 0 && this.toggleBounds != null && this.toggleBounds.contains(event.comp_4798(), event.comp_4799())) {
            this.interaction.finish();
            this.interaction.reset();
            MusicWidget.cancelInteraction();
            if (this.editing && event.comp_4798() < (double)(this.toggleBounds.x() + this.toggleBounds.width() - 33)) {
                this.panelDrag.begin(BloodHelperClient.config().hudEditor.chatPanel, event.comp_4798(), event.comp_4799(), this.toggleBounds.x(), this.toggleBounds.y(), this.toggleBounds.width(), this.toggleBounds.height());
                return true;
            }
            BloodHelperClient.config().hudEditor.chatEditingEnabled = this.editing = !this.editing;
            BloodHelperClient.saveConfig();
            this.toggleHeld = true;
            return true;
        }
        return this.editing ? this.interaction.click(event, true) : WaypointsWidget.click(event.comp_4798(), event.comp_4799(), event.method_74245()) || HudRenderer.clickMusic(event.comp_4798(), event.comp_4799(), event.method_74245());
    }

    public boolean drag(class_11909 event) {
        this.syncVisibility();
        if (!this.available()) {
            this.resized();
            return false;
        }
        if (this.panelDrag.active()) {
            class_1041 window = class_310.method_1551().method_22683();
            return this.panelDrag.move(event.comp_4798(), event.comp_4799(), window.method_4486(), window.method_4502());
        }
        if (this.toggleHeld && event.method_74245() == 0) {
            return true;
        }
        if (MusicWidget.drag(event.comp_4798(), event.comp_4799(), event.method_74245())) {
            return true;
        }
        class_310 mc = class_310.method_1551();
        return this.editing && this.interaction.drag(event, mc.method_22683().method_4486(), mc.method_22683().method_4502());
    }

    public boolean release(class_11909 event) {
        this.syncVisibility();
        if (this.panelDrag.end()) {
            BloodHelperClient.saveConfig();
            return true;
        }
        if (this.toggleHeld && event.method_74245() == 0) {
            this.toggleHeld = false;
            return true;
        }
        if (!this.available()) {
            this.resized();
            return false;
        }
        return MusicWidget.release(event.method_74245()) || this.editing && this.interaction.release(event);
    }

    public boolean scroll(double x, double y, double amount) {
        this.syncVisibility();
        if (!this.available()) {
            return false;
        }
        if (this.toggleBounds != null && this.toggleBounds.contains(x, y)) {
            return true;
        }
        return MusicWidget.capturesScroll(x, y) || this.editing && this.interaction.scroll(x, y, amount);
    }

    public boolean keyPressed(class_11908 event) {
        this.syncVisibility();
        return this.available() && this.editing && this.interaction.keyPressed(event, true);
    }

    public void resized() {
        this.interaction.finish();
        this.interaction.reset();
        MusicWidget.cancelInteraction();
        this.toggleHeld = false;
        this.toggleBounds = null;
        if (this.panelDrag.end()) {
            BloodHelperClient.saveConfig();
        }
        this.frame = 0L;
    }

    public void close() {
        this.resized();
        this.editing = false;
        this.enabledMotion = 0.0;
        this.hoverMotion = 0.0;
    }
}

