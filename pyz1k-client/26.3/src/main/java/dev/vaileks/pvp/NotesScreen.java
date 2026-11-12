/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package pyz1k.extensions.pvp;

import pyz1k.extensions.pvp.Config;
import pyz1k.extensions.pvp.Core;
import pyz1k.extensions.pvp.Feature;
import pyz1k.extensions.pvp.ReportScreen;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class NotesScreen
extends Screen {
    final Screen parent;
    EditBox custom;

    public NotesScreen(Screen p) {
        super((Component)Component.literal((String)"Duel Notes"));
        this.parent = p;
    }

    protected void init() {
        Config cfg = Core.config(Feature.DUEL_NOTES);
        String[] tags = new String[]{"\u041f\u043b\u043e\u0445\u043e\u0439 \u0440\u0435\u0444\u0438\u043b\u043b", "\u0417\u0430\u0431\u044b\u043b \u044d\u0444\u0444\u0435\u043a\u0442", "\u0421\u043b\u043e\u043c\u0430\u043b\u0430\u0441\u044c \u0431\u0440\u043e\u043d\u044f", "\u041e\u0448\u0438\u0431\u043a\u0430 \u0440\u0430\u0441\u043a\u043b\u0430\u0434\u043a\u0438", "\u041d\u0435 \u0445\u0432\u0430\u0442\u0438\u043b\u043e \u0440\u0435\u0441\u0443\u0440\u0441\u043e\u0432"};
        for (int i = 0; i < tags.length; ++i) {
            String tag = tags[i];
            this.addRenderableWidget(Button.builder((Component)Component.literal((String)(tag + " \u00b7 " + String.valueOf(cfg.notes.getOrDefault(tag, 0)))), b -> this.note(tag)).bounds(this.width / 2 - 150, 40 + i * 23, 300, 20).build());
        }
        this.custom = (EditBox)this.addRenderableWidget(new EditBox(this.font, this.width / 2 - 150, 158, 220, 20, (Component)Component.literal((String)"\u0421\u0432\u043e\u044f \u0437\u0430\u043c\u0435\u0442\u043a\u0430")));
        this.custom.setMaxLength(80);
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c"), b -> {
            if (!this.custom.getValue().isBlank()) {
                this.note(this.custom.getValue().trim());
            }
        }).bounds(this.width / 2 + 75, 158, 75, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u0421\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0430"), b -> {
            ArrayList<String> lines = new ArrayList<String>();
            cfg.notes.entrySet().stream().sorted((a, v) -> Integer.compare((Integer)v.getValue(), (Integer)a.getValue())).forEach(e -> lines.add((String)e.getKey() + ": " + String.valueOf(e.getValue())));
            this.minecraft.gui.setScreen((Screen)new ReportScreen(this, "\u041f\u043e\u0432\u0442\u043e\u0440\u044f\u044e\u0449\u0438\u0435\u0441\u044f \u043e\u0448\u0438\u0431\u043a\u0438", lines));
        }).bounds(this.width / 2 - 150, 185, 145, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"\u041d\u0430\u0437\u0430\u0434"), b -> this.onClose()).bounds(this.width / 2 + 5, 185, 145, 20).build());
    }

    void note(String tag) {
        Config cfg = Core.config(Feature.DUEL_NOTES);
        cfg.notes.merge(tag, 1, Integer::sum);
        Core.save(Feature.DUEL_NOTES, cfg);
        Core.notice("\u0417\u0430\u043c\u0435\u0442\u043a\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0430: " + tag);
        this.rebuildWidgets();
    }

    public void extractRenderState(GuiGraphicsExtractor c, int x, int y, float d) {
        c.fill(0, 0, this.width, this.height, -266855646);
        super.extractRenderState(c, x, y, d);
        c.centeredText(this.font, this.title, this.width / 2, 15, -1);
    }

    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

