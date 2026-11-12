/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 */
package ru.bloodhelper.client.modules;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import ru.bloodhelper.client.config.WaypointSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ModuleDetailScreen;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.modules.WaypointEditScreen;
import ru.bloodhelper.client.modules.WaypointFrame;
import ru.bloodhelper.client.modules.WaypointsModule;

public final class WaypointScreen
extends WaypointFrame {
    private int entryCount;

    public WaypointScreen(Screen parent) {
        super(parent, HudStyle.tr("\u041c\u0435\u0442\u043a\u0438", "Waypoints"));
    }

    @Override
    protected int preferredPanelWidth() {
        return 460;
    }

    @Override
    protected int preferredPanelHeight() {
        return WaypointScreen.listPanelHeight(77, this.entryCount, 65, 70);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        boolean connected;
        List<WaypointSettings.Entry> entries = WaypointsModule.currentEntries();
        this.entryCount = entries.size();
        this.frame(g, HudStyle.tr("\u041c\u0435\u0442\u043a\u0438", "Waypoints"), mouseX, mouseY);
        boolean bl = connected = !WaypointsModule.currentScope().isEmpty() && this.minecraft.player != null;
        if (connected) {
            this.button(g, this.left + 14, this.top + 45, 130, HudStyle.tr("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0437\u0434\u0435\u0441\u044c", "Add here"), mouseX, mouseY, () -> WaypointsModule.createAtPlayer(this));
        }
        this.text(g, entries.size() + " / 256", this.left + this.panelWidth - 73, this.top + 51, -7105633);
        this.bodyTop = this.top + 77;
        int totalHeight = entries.isEmpty() ? 70 : entries.size() * 65;
        this.maximumScroll = Math.max(0, totalHeight - Math.max(1, this.bodyBottom - this.bodyTop));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        g.enableScissor(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            if (entries.isEmpty()) {
                int y = this.bodyTop + 10;
                for (String line : WaypointScreen.wrap(connected ? HudStyle.tr("\u0421\u043e\u0437\u0434\u0430\u0439 \u043c\u0435\u0442\u043a\u0443 \u0437\u0434\u0435\u0441\u044c \u0438\u043b\u0438 \u043d\u0430\u0437\u043d\u0430\u0447\u044c \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u0432 \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0438 Minecraft. \u0423 \u043a\u0430\u0436\u0434\u043e\u0439 \u043c\u0435\u0442\u043a\u0438 \u0441\u0432\u043e\u0438 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435, \u0446\u0432\u0435\u0442 \u0438 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b.", "Add a waypoint here or assign its key in Minecraft Controls. Each place has its own name, colour and coordinates.") : HudStyle.tr("\u0417\u0430\u0439\u0434\u0438 \u0432 \u043c\u0438\u0440 \u0438\u043b\u0438 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440, \u0447\u0442\u043e\u0431\u044b \u0443\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u0435\u0433\u043e \u043c\u0435\u0442\u043a\u0430\u043c\u0438.", "Join a world or server to manage its waypoints."), this.panelWidth - 40)) {
                    this.text(g, line, this.left + 20, y, -7105633);
                    y += 15;
                }
            }
            for (int index = 0; index < entries.size(); ++index) {
                WaypointSettings.Entry entry = entries.get(index);
                int y = this.bodyTop + index * 65 - this.scroll;
                if (y + 62 < this.bodyTop || y >= this.bodyBottom) continue;
                UiDraw.rounded(g, this.left + 14, y, this.panelWidth - 28, 59, 5, ThemeColors.surface(-14342868));
                UiDraw.rounded(g, this.left + 23, y + 10, 6, 6, 3, entry.color);
                String title = StreamerMode.hideCoordinates() ? HudStyle.tr("\u0421\u043a\u0440\u044b\u0442\u0430\u044f \u043c\u0435\u0442\u043a\u0430", "Hidden waypoint") : entry.name;
                this.text(g, CustomFont.regular().ellipsize(title, 11.0f, this.panelWidth - 60), this.left + 36, y + 7, -1381908);
                if (!StreamerMode.hideCoordinates()) {
                    String location = entry.x + "  " + entry.y + "  " + entry.z + "  \u00b7  " + WaypointScreen.dimension(entry.dimension);
                    CustomFont.regular().draw(g, CustomFont.regular().ellipsize(location, 9.0f, this.panelWidth - 60), this.left + 23, y + 24, 9.0f, -7105633);
                }
                int hitTop = Math.max(y, this.bodyTop);
                int hitBottom = Math.min(y + 33, this.bodyBottom);
                if (hitBottom > hitTop) {
                    this.actions.add(new ModuleDetailScreen.Action(this.left + 14, hitTop, this.panelWidth - 28, hitBottom - hitTop, () -> WaypointsModule.editSaved(entry.id, entry.scope, this)));
                }
                if (y + 35 < this.bodyTop || y + 56 > this.bodyBottom) continue;
                int x = this.left + this.panelWidth - 205;
                this.button(g, x, y + 35, 57, entry.visible ? HudStyle.tr("\u0412\u0438\u0434\u0435\u043d", "Visible") : HudStyle.tr("\u0421\u043a\u0440\u044b\u0442", "Hidden"), mouseX, mouseY, () -> {
                    entry.visible = !entry.visible;
                    WaypointsModule.changed();
                });
                this.button(g, x + 63, y + 35, 57, HudStyle.tr("\u0418\u0437\u043c\u0435\u043d\u0438\u0442\u044c", "Edit"), mouseX, mouseY, () -> this.minecraft.gui.setScreen((Screen)new WaypointEditScreen(this, entry, false)));
                this.button(g, x + 126, y + 35, 63, HudStyle.tr("\u0423\u0434\u0430\u043b\u0438\u0442\u044c", "Delete"), mouseX, mouseY, () -> WaypointsModule.removeSaved(entry.id, entry.scope));
            }
        }
        finally {
            g.disableScissor();
        }
        this.scrollbar(g, totalHeight);
        this.footer(g, HudStyle.tr("\u041e\u0442\u0434\u0435\u043b\u044c\u043d\u043e \u0434\u043b\u044f \u043a\u0430\u0436\u0434\u043e\u0433\u043e \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u0438 \u043c\u0438\u0440\u0430 \u00b7 HUD \u043f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0442\u0435\u043a\u0443\u0449\u0435\u0435 \u0438\u0437\u043c\u0435\u0440\u0435\u043d\u0438\u0435", "Separate per server and world \u00b7 HUD shows the current dimension"));
    }

    static String dimension(String id) {
        return switch (id) {
            case "minecraft:overworld" -> HudStyle.tr("\u0412\u0435\u0440\u0445\u043d\u0438\u0439 \u043c\u0438\u0440", "Overworld");
            case "minecraft:the_nether" -> HudStyle.tr("\u041d\u0435\u0437\u0435\u0440", "Nether");
            case "minecraft:the_end" -> HudStyle.tr("\u042d\u043d\u0434", "The End");
            default -> id;
        };
    }
}

