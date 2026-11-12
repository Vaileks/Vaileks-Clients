/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package pyz1k.extensions.pvp;

import pyz1k.extensions.pvp.Core;
import pyz1k.extensions.pvp.Feature;
import pyz1k.extensions.pvp.SettingsScreen;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HubScreen
extends Screen {
    final Screen parent;
    int page;

    public HubScreen(Screen p) {
        super((Component)Component.literal((String)"PvP Mods"));
        this.parent = p;
    }

    protected void init() {
        ArrayList<Feature> features = new ArrayList<Feature>(Core.roots.keySet());
        int capacity = Math.max(1, (this.height - 90) / 24);
        int pages = Math.max(1, (features.size() + capacity - 1) / capacity);
        this.page = Math.min(this.page, pages - 1);
        for (int i = this.page * capacity; i < Math.min(features.size(), (this.page + 1) * capacity); ++i) {
            Feature f = (Feature)((Object)features.get(i));
            this.addRenderableWidget(Button.builder((Component)Component.literal((String)(f.title + "  \u00b7  " + (Core.on(f) ? "\u0412\u043a\u043b." : "\u0412\u044b\u043a\u043b."))), b -> this.minecraft.gui.setScreen((Screen)new SettingsScreen(this, f))).bounds(this.width / 2 - 160, 48 + (i - this.page * capacity) * 24, 320, 20).build());
        }
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u2190"), b -> {
            this.page = Math.floorMod(this.page - 1, pages);
            this.rebuildWidgets();
        }).bounds(this.width / 2 - 120, this.height - 29, 40, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u0413\u043e\u0442\u043e\u0432\u043e"), b -> this.onClose()).bounds(this.width / 2 - 70, this.height - 29, 140, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u2192"), b -> {
            this.page = (this.page + 1) % pages;
            this.rebuildWidgets();
        }).bounds(this.width / 2 + 80, this.height - 29, 40, 20).build());
    }

    public void extractRenderState(GuiGraphicsExtractor c, int x, int y, float d) {
        c.fill(0, 0, this.width, this.height, -283632862);
        super.extractRenderState(c, x, y, d);
        c.centeredText(this.font, this.title, this.width / 2, 14, -1);
        c.centeredText(this.font, "PvPTools \u00b7 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u043c\u043e\u0434\u0443\u043b\u0435\u0439 \u043a\u043b\u0438\u0435\u043d\u0442\u0430", this.width / 2, 29, -8856321);
    }

    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

