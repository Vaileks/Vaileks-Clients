/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 */
package ru.bloodhelper.client.modules;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.CooldownReadySettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.CooldownReadyModule;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class CooldownReadyScreen
extends ModuleDetailScreen {
    private final GuiTextField search = new GuiTextField(0, 0, 100, 23, "");
    private List<CooldownReadyModule.Group> rows = List.of();
    private int seenRevision = -1;
    private String query = null;
    private String language;
    private CooldownReadySettings source;
    private String message = "";
    private int groupCount;

    @Override
    protected int preferredPanelWidth() {
        return 620;
    }

    @Override
    protected int preferredPanelHeight() {
        return CooldownReadyScreen.listPanelHeight(101, this.groupCount, 49, 82);
    }

    public CooldownReadyScreen(Screen parent) {
        super(parent, "Cooldown Ready");
        this.search.setTextSize(11.0f);
        this.search.setMaxLength(128);
        this.search.setResponder(value -> {
            this.scroll = 0;
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mx, int my, float delta) {
        ClientConfig config = BloodHelperClient.config();
        CooldownReadySettings settings = config.cooldownReady;
        String nextQuery = this.search.getValue().strip().toLowerCase(Locale.ROOT);
        if (this.source != settings || this.seenRevision != CooldownReadyModule.knownRevision() || !nextQuery.equals(this.query) || !config.language.equals(this.language)) {
            this.source = settings;
            this.seenRevision = CooldownReadyModule.knownRevision();
            this.query = nextQuery;
            this.language = config.language;
            List<CooldownReadyModule.Group> groups = CooldownReadyModule.groups();
            this.groupCount = groups.size();
            this.rows = groups.stream().filter(group -> this.query.isEmpty() || group.label().toLowerCase(Locale.ROOT).contains(this.query) || group.id().contains(this.query)).toList();
        }
        this.frame(g, HudStyle.tr("\u0413\u043e\u0442\u043e\u0432\u043d\u043e\u0441\u0442\u044c \u043f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u0438", "Cooldown Ready"), mx, my);
        this.button(g, this.left + 14, this.top + 46, 166, settings.onlySelected ? HudStyle.tr("\u0422\u043e\u043b\u044c\u043a\u043e \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0435", "Selected groups only") : HudStyle.tr("\u0412\u0441\u0435 \u043f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u0438", "All cooldown groups"), mx, my, () -> {
            settings.onlySelected = !settings.onlySelected;
            BloodHelperClient.saveConfig();
        });
        this.search.setX(this.left + 197);
        this.search.setY(this.top + 46);
        this.search.setWidth(Math.max(1, this.panelWidth - 218));
        this.search.setHint(HudStyle.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0438\u043b\u0438 \u0433\u0440\u0443\u043f\u043f\u0430", "Item or group"));
        UiDraw.rounded(g, this.left + 190, this.top + 44, Math.max(1, this.panelWidth - 205), 25, 6, ThemeColors.surface(-15460579));
        this.search.render(g, mx, my, delta);
        this.text(g, HudStyle.tr("\u0412\u044b\u0431\u0435\u0440\u0438 \u0433\u0440\u0443\u043f\u043f\u044b \u0434\u043b\u044f \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0439. \u041e\u0431\u0449\u0430\u044f \u0433\u0440\u0443\u043f\u043f\u0430 \u043e\u0437\u043d\u0430\u0447\u0430\u0435\u0442 \u043e\u0431\u0449\u0435\u0435 \u041a\u0414.", "Select groups for alerts. Items in the same group share a cooldown."), this.left + 16, this.top + 79, -7105633);
        this.bodyTop = this.top + 101;
        this.maximumScroll = Math.max(0, this.rows.size() * 49 - (this.bodyBottom - this.bodyTop));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        g.enableScissor(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            int y = this.bodyTop - this.scroll;
            if (this.rows.isEmpty()) {
                String empty = this.query.isEmpty() ? HudStyle.tr("\u0412\u043a\u043b\u044e\u0447\u0438 \u043c\u043e\u0434\u0443\u043b\u044c \u0438 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0441 \u043f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u043e\u0439. \u0415\u0433\u043e \u0433\u0440\u0443\u043f\u043f\u0430 \u043f\u043e\u044f\u0432\u0438\u0442\u0441\u044f \u0437\u0434\u0435\u0441\u044c. \u0423\u0436\u0435 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0435 \u0433\u0440\u0443\u043f\u043f\u044b \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u044e\u0442\u0441\u044f \u043f\u043e\u0441\u043b\u0435 \u0441\u043c\u0435\u043d\u044b \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 \u0438 \u0432\u044b\u0445\u043e\u0434\u0430 \u0438\u0437 \u0438\u0433\u0440\u044b.", "Enable this module and use an item with a cooldown to discover its group here. Selected groups stay saved after changing items or leaving the game.") : HudStyle.tr("\u041d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e.", "No matching groups.");
                for (String line : CooldownReadyScreen.wrap(empty, this.panelWidth - 44)) {
                    this.text(g, line, this.left + 20, y + 12, -7105633);
                    y += 16;
                }
            }
            for (CooldownReadyModule.Group group2 : this.rows) {
                if (y + 45 > this.bodyTop && y < this.bodyBottom) {
                    boolean selected = settings.selectedGroups.contains(group2.id());
                    UiDraw.rounded(g, this.left + 14, y, this.panelWidth - 30, 44, 7, ThemeColors.surface(-14079183));
                    int boxX = this.left + 25;
                    int boxY = y + 14;
                    UiDraw.rounded(g, boxX, boxY, 16, 16, 5, selected ? UiDraw.alpha(config.accent, 75) : ThemeColors.surface(-15394784));
                    UiDraw.outline(g, boxX, boxY, 16, 16, 5, selected ? config.accent : ThemeColors.surface(-10066319));
                    if (selected) {
                        UiIcons.draw(g, "check", boxX, boxY, 16, config.accent);
                    }
                    this.text(g, CustomFont.regular().ellipsize(group2.label(), 11.0f, this.panelWidth - 83), this.left + 52, y + 7, -1381908);
                    CustomFont.regular().draw(g, CustomFont.regular().ellipsize(group2.id(), 9.0f, this.panelWidth - 83), this.left + 52, y + 26, 9.0f, -7105633);
                    if (y >= this.bodyTop && y + 44 <= this.bodyBottom) {
                        this.actions.add(new ModuleDetailScreen.Action(this.left + 14, y, this.panelWidth - 30, 44, () -> this.select(group2.id())));
                    }
                }
                y += 49;
            }
        }
        finally {
            g.disableScissor();
        }
        this.scrollbar(g, this.rows.size() * 49);
        this.footer(g, (String)(!this.message.isEmpty() ? this.message : HudStyle.tr("\u0412\u044b\u0431\u0440\u0430\u043d\u043e: ", "Selected: ") + settings.selectedGroups.size() + HudStyle.tr(" \u00b7 PvP \u043d\u0435 \u0443\u0447\u0438\u0442\u044b\u0432\u0430\u0435\u0442\u0441\u044f \u00b7 \u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f \u043d\u0435 \u0437\u0430\u0432\u0438\u0441\u044f\u0442 \u043e\u0442 \u043c\u043e\u0434\u0443\u043b\u044f Notifications", " \u00b7 PvP excluded \u00b7 Independent of the Notifications module")));
    }

    private void select(String id) {
        CooldownReadySettings settings = BloodHelperClient.config().cooldownReady;
        if (settings.selectedGroups.contains(id)) {
            settings.selectedGroups.remove(id);
        } else if (settings.selectedGroups.size() < 128) {
            settings.selectedGroups.add(id);
        } else {
            this.message = HudStyle.tr("\u041c\u043e\u0436\u043d\u043e \u0432\u044b\u0431\u0440\u0430\u0442\u044c \u043d\u0435 \u0431\u043e\u043b\u0435\u0435 128 \u0433\u0440\u0443\u043f\u043f.", "You can select up to 128 groups.");
            return;
        }
        this.message = "";
        this.source = null;
        BloodHelperClient.saveConfig();
    }

    @Override
    protected boolean panelMouseClicked(MouseButtonEvent event, boolean doubled) {
        return this.search.mouseClicked(event, doubled) || super.panelMouseClicked(event, doubled);
    }

    @Override
    protected boolean panelMouseDragged(MouseButtonEvent event, double dx, double dy) {
        return this.search.mouseDragged(event, dx, dy) || super.panelMouseDragged(event, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(MouseButtonEvent event) {
        return this.search.mouseReleased(event) || super.panelMouseReleased(event);
    }

    public boolean keyPressed(KeyEvent event) {
        return this.search.keyPressed(event) || super.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        return this.search.charTyped(event) || super.charTyped(event);
    }
}

