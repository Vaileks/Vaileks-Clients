/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.network.chat.Component
 */
package pyz1k.extensions.pvp;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public final class ReportScreen
extends Screen {
    final Screen parent;
    final List<String> lines;
    int offset;

    public ReportScreen(Screen parent, String title, List<String> lines) {
        super((Component)Component.literal((String)title));
        this.parent = parent;
        this.lines = List.copyOf(lines);
    }

    protected void init() {
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u041d\u0430\u0437\u0430\u0434"), b -> this.onClose()).bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
    }

    int visible() {
        return Math.max(1, (this.height - 90) / 14);
    }

    void scroll(int dy) {
        this.offset = Math.clamp((long)(this.offset + dy), 0, Math.max(0, this.lines.size() - this.visible()));
    }

    public void extractRenderState(GuiGraphicsExtractor c, int x, int y, float d) {
        c.fill(0, 0, this.width, this.height, -266855646);
        super.extractRenderState(c, x, y, d);
        c.centeredText(this.font, this.title, this.width / 2, 14, -1);
        c.centeredText(this.font, "\u041a\u043e\u043b\u0435\u0441\u043e / \u2191\u2193 \u00b7 " + this.lines.size() + " \u0441\u0442\u0440\u043e\u043a", this.width / 2, 29, -8856321);
        c.enableScissor(12, 45, this.width - 12, this.height - 36);
        for (int i = this.offset; i < Math.min(this.lines.size(), this.offset + this.visible()); ++i) {
            c.text(this.font, this.lines.get(i), 18, 48 + (i - this.offset) * 14, -1840395);
        }
        c.disableScissor();
    }

    public boolean mouseScrolled(double x, double y, double h, double v) {
        this.scroll((int)(-Math.signum(v)) * 3);
        return true;
    }

    public boolean keyPressed(KeyEvent k) {
        if (pyz1k.compat.LegacyInput.fromNativeKey(k.key()) == 264) {
            this.scroll(1);
            return true;
        }
        if (pyz1k.compat.LegacyInput.fromNativeKey(k.key()) == 265) {
            this.scroll(-1);
            return true;
        }
        return super.keyPressed(k);
    }

    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

