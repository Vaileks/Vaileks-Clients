/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.util.List;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.WaypointSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.modules.WaypointFrame;
import ru.bloodhelper.client.modules.WaypointScreen;
import ru.bloodhelper.client.modules.WaypointsModule;

public final class CoordinateLinkScreen
extends WaypointFrame {
    private final WaypointSettings.Entry entry;
    private final Object connection;
    private final GuiTextField name = CoordinateLinkScreen.field(48);
    private final GuiTextField dimension = CoordinateLinkScreen.field(128);
    private final GuiTextField x = CoordinateLinkScreen.field(10);
    private final GuiTextField y = CoordinateLinkScreen.field(10);
    private final GuiTextField z = CoordinateLinkScreen.field(10);
    private final List<GuiTextField> fields = List.of(this.name, this.x, this.y, this.z, this.dimension);
    private String error = "";

    @Override
    protected int preferredPanelWidth() {
        return 440;
    }

    @Override
    protected int preferredPanelHeight() {
        return 363;
    }

    public CoordinateLinkScreen(class_437 parent, WaypointSettings.Entry original, Object connection) {
        super(parent, HudStyle.tr("\u041c\u0435\u0442\u043a\u0430 \u0438\u0437 \u0447\u0430\u0442\u0430", "Chat waypoint"));
        this.entry = original.copy();
        this.connection = connection;
        this.name.setValue(this.entry.name);
        this.dimension.setValue(this.entry.dimension);
        this.x.setValue(Integer.toString(this.entry.x));
        this.y.setValue(Integer.toString(this.entry.y));
        this.z.setValue(Integer.toString(this.entry.z));
        this.name.setFocused(true);
    }

    private static GuiTextField field(int max) {
        GuiTextField field = new GuiTextField(0, 0, 100, 25, "");
        field.setMaxLength(max);
        field.setTextSize(11.0f);
        return field;
    }

    private boolean allowed() {
        return this.connection == this.field_22787.method_1562() && this.field_22787.field_1724 != null && this.field_22787.field_1687 != null && BloodHelperClient.config().coordinateLinks.enabled && !StreamerMode.hideCoordinates() && !StreamerMode.hideChat();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u0421\u043e\u0437\u0434\u0430\u0442\u044c \u043c\u0435\u0442\u043a\u0443", "Create waypoint"), mx, my);
        this.bodyTop = this.top + 47;
        if (!this.allowed()) {
            this.fields.forEach(field -> field.setFocused(false));
            this.text(g, HudStyle.tr("\u0421\u0441\u044b\u043b\u043a\u0430 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430", "Link unavailable"), this.left + 20, this.bodyTop + 15, -7105633);
            return;
        }
        int totalHeight = 290;
        this.maximumScroll = Math.max(0, totalHeight - (this.bodyBottom - this.bodyTop));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        int start = this.bodyTop - this.scroll;
        g.method_44379(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            this.input(g, this.name, HudStyle.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435", "Name"), this.left + 20, start + 5, this.panelWidth - 40, mx, my, delta);
            int cell = Math.max(1, (this.panelWidth - 56) / 3);
            this.input(g, this.x, "X", this.left + 20, start + 55, cell, mx, my, delta);
            this.input(g, this.y, "Y", this.left + 28 + cell, start + 55, cell, mx, my, delta);
            this.input(g, this.z, "Z", this.left + 36 + 2 * cell, start + 55, cell, mx, my, delta);
            this.input(g, this.dimension, HudStyle.tr("\u0418\u0437\u043c\u0435\u0440\u0435\u043d\u0438\u0435", "Dimension"), this.left + 20, start + 105, this.panelWidth - 40, mx, my, delta);
            if (start + 155 >= this.bodyTop && start + 176 <= this.bodyBottom) {
                int cellWidth = Math.max(1, (this.panelWidth - 56) / 3);
                String[] ids = new String[]{"minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"};
                for (int i = 0; i < ids.length; ++i) {
                    String id = ids[i];
                    this.button(g, this.left + 20 + i * (cellWidth + 8), start + 155, cellWidth, WaypointScreen.dimension(id), mx, my, () -> this.dimension.setValue(id));
                }
            }
            String note = BloodHelperClient.config().waypoints.enabled ? HudStyle.tr("\u041f\u0440\u043e\u0432\u0435\u0440\u044c \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0438 \u0438\u0437\u043c\u0435\u0440\u0435\u043d\u0438\u0435 \u043f\u0435\u0440\u0435\u0434 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u0435\u043c.", "Check the name and dimension before saving.") : HudStyle.tr("\u041c\u0435\u0442\u043a\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u0441\u044f. \u0427\u0442\u043e\u0431\u044b \u0432\u0438\u0434\u0435\u0442\u044c \u0435\u0451 \u0432 \u0438\u0433\u0440\u0435, \u0432\u043a\u043b\u044e\u0447\u0438 Waypoints.", "The waypoint will be saved. Enable Waypoints to show it in game.");
            int noteY = start + 185;
            for (String line : CoordinateLinkScreen.wrap(note, this.panelWidth - 40)) {
                this.text(g, line, this.left + 20, noteY, -7105633);
                noteY += 15;
            }
            if (start + 230 >= this.bodyTop && start + 251 <= this.bodyBottom) {
                this.button(g, this.left + 20, start + 230, 150, HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u043c\u0435\u0442\u043a\u0443", "Save waypoint"), mx, my, this::save);
            }
            if (!this.error.isEmpty()) {
                this.text(g, CustomFont.regular().ellipsize(this.error, 11.0f, this.panelWidth - 40), this.left + 20, start + 262, -882038);
            }
        }
        finally {
            g.method_44380();
        }
        this.scrollbar(g, totalHeight);
        this.footer(g, HudStyle.tr("Enter \u2014 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u00b7 Escape \u2014 \u043e\u0442\u043c\u0435\u043d\u0438\u0442\u044c", "Enter to save \u00b7 Escape to cancel"));
    }

    private void input(class_332 g, GuiTextField field, String label, int px, int py, int w, int mx, int my, float delta) {
        this.text(g, label, px, py, -7105633);
        UiDraw.border(g, px, py + 15, w, 27, 5, ThemeColors.surface(-15395556), field.isFocused() ? ThemeColors.accent() : ThemeColors.surface(-7829352));
        field.setX(px + 7);
        field.setY(py + 16);
        field.setWidth(w - 14);
        field.setHeight(25);
        field.render(g, mx, my, delta);
    }

    private void save() {
        if (!this.allowed()) {
            return;
        }
        try {
            this.entry.x = Integer.parseInt(this.x.getValue().strip());
            this.entry.y = Integer.parseInt(this.y.getValue().strip());
            this.entry.z = Integer.parseInt(this.z.getValue().strip());
            if (!WaypointSettings.validCoordinates(this.entry.x, this.entry.y, this.entry.z)) {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException invalid) {
            this.error = HudStyle.tr("\u041f\u0440\u043e\u0432\u0435\u0440\u044c \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b.", "Check the coordinates.");
            return;
        }
        class_2960 id = class_2960.method_12829((String)this.dimension.getValue().strip());
        if (id == null || !this.dimension.getValue().contains(":")) {
            this.error = HudStyle.tr("\u041f\u0440\u0438\u043c\u0435\u0440 \u0438\u0437\u043c\u0435\u0440\u0435\u043d\u0438\u044f: minecraft:overworld", "Dimension example: minecraft:overworld");
            return;
        }
        this.entry.dimension = id.toString();
        this.entry.name = WaypointSettings.cleanName(this.name.getValue());
        if (WaypointsModule.save(this.entry, true)) {
            this.method_25419();
        } else {
            this.error = HudStyle.tr("\u0421\u0435\u0440\u0432\u0435\u0440 \u0441\u043c\u0435\u043d\u0438\u043b\u0441\u044f \u0438\u043b\u0438 \u0434\u043e\u0441\u0442\u0438\u0433\u043d\u0443\u0442 \u043b\u0438\u043c\u0438\u0442 \u043c\u0435\u0442\u043e\u043a.", "The server changed or the waypoint limit was reached.");
        }
    }

    @Override
    public void method_25419() {
        this.field_22787.method_1507(this.connection == this.field_22787.method_1562() ? this.parent : null);
    }

    @Override
    protected boolean panelMouseClicked(class_11909 event, boolean doubled) {
        this.fields.forEach(field -> field.setFocused(false));
        if (this.allowed() && event.comp_4799() >= (double)this.bodyTop && event.comp_4799() < (double)this.bodyBottom) {
            for (GuiTextField field2 : this.fields) {
                if (!field2.mouseClicked(event, doubled)) continue;
                return true;
            }
        }
        return super.panelMouseClicked(event, doubled);
    }

    public boolean method_25404(class_11908 event) {
        if (!this.allowed()) {
            return super.method_25404(event);
        }
        if (event.comp_4795() == 257 || event.comp_4795() == 335) {
            this.save();
            return true;
        }
        if (event.comp_4795() == 258) {
            int index = -1;
            for (int i = 0; i < this.fields.size(); ++i) {
                if (!this.fields.get(i).isFocused()) continue;
                index = i;
            }
            this.fields.forEach(field -> field.setFocused(false));
            this.fields.get(Math.floorMod(index + (event.method_74239() ? -1 : 1), this.fields.size())).setFocused(true);
            return true;
        }
        for (GuiTextField field2 : this.fields) {
            if (!field2.keyPressed(event)) continue;
            return true;
        }
        return super.method_25404(event);
    }

    public boolean method_25400(class_11905 event) {
        if (this.allowed()) {
            for (GuiTextField field : this.fields) {
                if (!field.charTyped(event)) continue;
                return true;
            }
        }
        return super.method_25400(event);
    }

    @Override
    protected boolean panelMouseDragged(class_11909 event, double dx, double dy) {
        if (this.allowed()) {
            for (GuiTextField field : this.fields) {
                if (!field.mouseDragged(event, dx, dy)) continue;
                return true;
            }
        }
        return super.panelMouseDragged(event, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(class_11909 event) {
        for (GuiTextField field : this.fields) {
            if (!field.mouseReleased(event)) continue;
            return true;
        }
        return super.panelMouseReleased(event);
    }
}

