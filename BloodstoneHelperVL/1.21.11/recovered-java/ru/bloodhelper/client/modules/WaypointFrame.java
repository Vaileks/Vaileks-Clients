/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

abstract class WaypointFrame
extends ModuleDetailScreen {
    private final long opened = System.nanoTime();

    WaypointFrame(class_437 parent, String title) {
        super(parent, title);
    }

    @Override
    protected void frame(class_332 g, String title, int mx, int my) {
        this.layoutFrame();
        double progress = BloodHelperClient.config().animations ? Math.clamp((double)(System.nanoTime() - this.opened) / (RenderQuality.animationMillis(180.0) * 1000000.0), 0.0, 1.0) : 1.0;
        double remaining = Math.pow(1.0 - progress, 3.0);
        this.top += (int)(remaining * 8.0);
        this.bodyTop = this.top + 48;
        this.bodyBottom = this.top + this.panelHeight - 26;
        this.actions.clear();
        LiquidGlass.surfaceMenu(g, this.left, this.top, this.panelWidth, this.panelHeight, 10, ThemeColors.surface(-182707166), BloodHelperClient.config());
        CustomFont.medium().draw(g, CustomFont.medium().ellipsize(title, 14.0f, Math.max(0, this.panelWidth - 112)), this.left + 14, this.top + 12, 14.0f, -1381908);
        this.button(g, this.left + this.panelWidth - 76, this.top + 10, 62, HudStyle.tr("\u041d\u0430\u0437\u0430\u0434", "Back"), mx, my, this::method_25419);
    }

    @Override
    protected void button(class_332 g, int x, int y, int w, String label, int mx, int my, Runnable callback) {
        ModuleDetailScreen.Action action = new ModuleDetailScreen.Action(x, y, w, 21, callback);
        this.actions.add(action);
        boolean hovered = action.contains(mx, my);
        UiDraw.border(g, x, y, w, 21, 5, ThemeColors.surface(hovered ? -11184803 : -13421764), hovered ? ThemeColors.accent() : ThemeColors.surface(-6710870));
        String visible = CustomFont.medium().ellipsize(label, 10.0f, Math.max(0, w - 10));
        CustomFont.medium().draw(g, visible, (float)x + ((float)w - CustomFont.medium().width(visible, 10.0f)) / 2.0f, y + 5, 10.0f, -1381908);
    }
}

