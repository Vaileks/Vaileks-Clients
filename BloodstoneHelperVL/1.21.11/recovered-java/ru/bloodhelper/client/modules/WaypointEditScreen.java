/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.util.List;
import java.util.OptionalInt;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.config.WaypointSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ModuleDetailScreen;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.modules.WaypointFrame;
import ru.bloodhelper.client.modules.WaypointScreen;
import ru.bloodhelper.client.modules.WaypointsModule;

public final class WaypointEditScreen
extends WaypointFrame {
    private final WaypointSettings.Entry entry;
    private final boolean create;
    private final GuiTextField name = WaypointEditScreen.field(48);
    private final GuiTextField x = WaypointEditScreen.field(10);
    private final GuiTextField y = WaypointEditScreen.field(10);
    private final GuiTextField z = WaypointEditScreen.field(10);
    private final GuiTextField color = WaypointEditScreen.field(7);
    private final List<GuiTextField> fields = List.of(this.name, this.x, this.y, this.z, this.color);
    private String error = "";
    private static final int[] COLORS = new int[]{-1618842, -18340, -860568, -8989031, -10173713, -5203473};

    @Override
    protected int preferredPanelWidth() {
        return 440;
    }

    @Override
    protected int preferredPanelHeight() {
        return 344;
    }

    public WaypointEditScreen(class_437 parent, WaypointSettings.Entry original, boolean create) {
        super(parent, HudStyle.tr("\u041c\u0435\u0442\u043a\u0430", "Waypoint"));
        this.entry = original.copy();
        this.create = create;
        this.name.setValue(this.entry.name);
        this.x.setValue(Integer.toString(this.entry.x));
        this.y.setValue(Integer.toString(this.entry.y));
        this.z.setValue(Integer.toString(this.entry.z));
        this.color.setValue(ThemeColors.hex(this.entry.color));
        this.name.setFocused(!StreamerMode.hideCoordinates());
    }

    private static GuiTextField field(int maximum) {
        GuiTextField field = new GuiTextField(0, 0, 100, 24, "");
        field.setMaxLength(maximum);
        field.setTextSize(11.0f);
        return field;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, this.create ? HudStyle.tr("\u041d\u043e\u0432\u0430\u044f \u043c\u0435\u0442\u043a\u0430", "New waypoint") : HudStyle.tr("\u0418\u0437\u043c\u0435\u043d\u0438\u0442\u044c \u043c\u0435\u0442\u043a\u0443", "Edit waypoint"), mx, my);
        this.bodyTop = this.top + 47;
        this.maximumScroll = Math.max(0, 271 - (this.bodyBottom - this.bodyTop));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        int start = this.bodyTop - this.scroll;
        boolean privateLocation = StreamerMode.hideCoordinates();
        if (privateLocation) {
            for (GuiTextField guiTextField : this.fields) {
                if (guiTextField == this.color) continue;
                guiTextField.setFocused(false);
            }
        }
        g.method_44379(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            if (!privateLocation) {
                this.input(g, this.name, HudStyle.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435", "Name"), this.left + 20, start + 8, this.panelWidth - 40, mx, my, delta);
                int cell = Math.max(1, (this.panelWidth - 56) / 3);
                this.input(g, this.x, "X", this.left + 20, start + 61, cell, mx, my, delta);
                this.input(g, this.y, "Y", this.left + 28 + cell, start + 61, cell, mx, my, delta);
                this.input(g, this.z, "Z", this.left + 36 + 2 * cell, start + 61, cell, mx, my, delta);
                this.text(g, CustomFont.regular().ellipsize(WaypointScreen.dimension(this.entry.dimension), 11.0f, this.panelWidth - 40), this.left + 20, start + 110, -7105633);
            } else {
                int textY = start + 25;
                for (String line : WaypointEditScreen.wrap(HudStyle.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0438 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b \u0441\u043a\u0440\u044b\u0442\u044b \u0440\u0435\u0436\u0438\u043c\u043e\u043c \u0441\u0442\u0440\u0438\u043c\u0435\u0440\u0430. \u041c\u0435\u0442\u043a\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u0441\u044f \u0441 \u043f\u0440\u0435\u0436\u043d\u0438\u043c\u0438 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u044f\u043c\u0438.", "Name and coordinates are hidden by Streamer Mode. Their values are preserved when you save."), this.panelWidth - 40)) {
                    this.text(g, line, this.left + 20, textY, -7105633);
                    textY += 15;
                }
            }
            this.input(g, this.color, HudStyle.tr("\u0426\u0432\u0435\u0442 \u00b7 HEX", "Colour \u00b7 HEX"), this.left + 20, start + 139, 96, mx, my, delta);
            for (int i = 0; i < COLORS.length; ++i) {
                int n = this.left + 128 + i % 3 * 29;
                int py = start + 148 + i / 3 * 24;
                UiDraw.rounded(g, n, py, 22, 18, 5, COLORS[i]);
                if (py < this.bodyTop || py + 18 > this.bodyBottom) continue;
                int selected = COLORS[i];
                this.actions.add(new ModuleDetailScreen.Action(n, py, 22, 18, () -> this.color.setValue(ThemeColors.hex(selected))));
            }
            if (start + 201 >= this.bodyTop && start + 222 <= this.bodyBottom) {
                this.button(g, this.left + 20, start + 201, 130, HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c", "Save waypoint"), mx, my, this::save);
            }
            if (!this.create && start + 201 >= this.bodyTop && start + 222 <= this.bodyBottom) {
                this.button(g, this.left + this.panelWidth - 108, start + 201, 88, HudStyle.tr("\u0423\u0434\u0430\u043b\u0438\u0442\u044c", "Delete"), mx, my, () -> {
                    if (WaypointsModule.removeSaved(this.entry.id, this.entry.scope)) {
                        this.method_25419();
                    } else {
                        this.error = HudStyle.tr("\u041c\u0435\u0442\u043a\u0430 \u0443\u0436\u0435 \u0443\u0434\u0430\u043b\u0435\u043d\u0430 \u0438\u043b\u0438 \u0441\u043c\u0435\u043d\u0438\u043b\u0441\u044f \u0441\u0435\u0440\u0432\u0435\u0440.", "Waypoint removed or server changed.");
                    }
                });
            }
            if (!this.error.isEmpty()) {
                this.text(g, CustomFont.regular().ellipsize(this.error, 11.0f, this.panelWidth - 40), this.left + 20, start + 234, -882038);
            }
        }
        finally {
            g.method_44380();
        }
        this.scrollbar(g, 271);
        this.footer(g, HudStyle.tr("Enter \u2014 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u00b7 Escape \u2014 \u043e\u0442\u043c\u0435\u043d\u0438\u0442\u044c", "Enter to save \u00b7 Escape to cancel"));
    }

    private void input(class_332 g, GuiTextField field, String label, int px, int py, int w, int mx, int my, float delta) {
        this.text(g, label, px, py, -7105633);
        UiDraw.border(g, px, py + 16, w, 27, 5, ThemeColors.surface(-15395556), field.isFocused() ? ThemeColors.accent() : ThemeColors.surface(-7829352));
        field.setX(px + 7);
        field.setY(py + 17);
        field.setWidth(w - 14);
        field.setHeight(25);
        field.render(g, mx, my, delta);
    }

    private void save() {
        try {
            this.entry.x = Integer.parseInt(this.x.getValue().strip());
            this.entry.y = Integer.parseInt(this.y.getValue().strip());
            this.entry.z = Integer.parseInt(this.z.getValue().strip());
            if (!WaypointSettings.validCoordinates(this.entry.x, this.entry.y, this.entry.z)) {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException invalid) {
            this.error = HudStyle.tr("\u041f\u0440\u043e\u0432\u0435\u0440\u044c \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b: \u043d\u0443\u0436\u043d\u044b \u0446\u0435\u043b\u044b\u0435 \u0447\u0438\u0441\u043b\u0430 \u0432 \u043f\u0440\u0435\u0434\u0435\u043b\u0430\u0445 \u043c\u0438\u0440\u0430.", "Use whole-number coordinates inside the world bounds.");
            return;
        }
        OptionalInt parsed = ThemeColors.parseHex(this.color.getValue().strip());
        if (parsed.isEmpty()) {
            this.error = HudStyle.tr("\u0426\u0432\u0435\u0442 \u043d\u0443\u0436\u0435\u043d \u0432 \u0444\u043e\u0440\u043c\u0430\u0442\u0435 #RRGGBB.", "Enter a colour as #RRGGBB.");
            return;
        }
        this.entry.color = parsed.getAsInt();
        this.entry.name = WaypointSettings.cleanName(this.name.getValue());
        if (WaypointsModule.save(this.entry, this.create)) {
            this.method_25419();
        } else {
            this.error = HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c: \u0441\u043c\u0435\u043d\u0438\u043b\u0441\u044f \u0441\u0435\u0440\u0432\u0435\u0440 \u0438\u043b\u0438 \u0434\u043e\u0441\u0442\u0438\u0433\u043d\u0443\u0442 \u043b\u0438\u043c\u0438\u0442 \u043c\u0435\u0442\u043e\u043a.", "Cannot save: the server changed or the waypoint limit was reached.");
        }
    }

    @Override
    protected boolean panelMouseClicked(class_11909 event, boolean doubled) {
        for (GuiTextField field : this.fields) {
            field.setFocused(false);
        }
        if (event.comp_4799() >= (double)this.bodyTop && event.comp_4799() < (double)this.bodyBottom) {
            for (GuiTextField field : this.fields) {
                if (StreamerMode.hideCoordinates() && field != this.color || !field.mouseClicked(event, doubled)) continue;
                return true;
            }
        }
        return super.panelMouseClicked(event, doubled);
    }

    public boolean method_25404(class_11908 event) {
        if (event.comp_4795() == 257 || event.comp_4795() == 335) {
            this.save();
            return true;
        }
        if (event.comp_4795() == 258) {
            List<GuiTextField> available = StreamerMode.hideCoordinates() ? List.of(this.color) : this.fields;
            int current = -1;
            for (int i = 0; i < available.size(); ++i) {
                if (!available.get(i).isFocused()) continue;
                current = i;
            }
            for (GuiTextField field : this.fields) {
                field.setFocused(false);
            }
            available.get(Math.floorMod(current + (event.method_74239() ? -1 : 1), available.size())).setFocused(true);
            return true;
        }
        for (GuiTextField field : this.fields) {
            if (!field.keyPressed(event)) continue;
            return true;
        }
        return super.method_25404(event);
    }

    public boolean method_25400(class_11905 event) {
        for (GuiTextField field : this.fields) {
            if (!field.charTyped(event)) continue;
            return true;
        }
        return super.method_25400(event);
    }

    @Override
    protected boolean panelMouseDragged(class_11909 event, double dx, double dy) {
        for (GuiTextField field : this.fields) {
            if (!field.mouseDragged(event, dx, dy)) continue;
            return true;
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

