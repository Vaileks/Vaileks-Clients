/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.util.Optional;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.QuickCommandsSettings;
import ru.bloodhelper.client.gui.BackgroundMenuScreen;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.MenuBackdrop;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.QuickCommandAction;
import ru.bloodhelper.client.modules.QuickCommandsModule;
import ru.bloodhelper.client.modules.StreamerMode;

public final class QuickCommandsEditorScreen
extends class_437
implements BackgroundMenuScreen {
    private final class_437 parent;
    private final GuiTextField[] fields = new GuiTextField[8];
    private float scale = 1.0f;
    private int left;
    private int top;
    private static final int PANEL_WIDTH = 568;
    private static final int PANEL_HEIGHT = 356;
    private static final String[] RU_POSITION = new String[]{"\u0421\u0432\u0435\u0440\u0445\u0443", "\u0421\u043f\u0440\u0430\u0432\u0430", "\u0421\u043d\u0438\u0437\u0443", "\u0421\u043b\u0435\u0432\u0430"};
    private static final String[] EN_POSITION = new String[]{"Top", "Right", "Bottom", "Left"};

    public QuickCommandsEditorScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"Quick Commands"));
        this.parent = parent;
        QuickCommandsSettings settings = BloodHelperClient.config().quickCommands;
        settings.sanitize();
        for (int i = 0; i < 4; ++i) {
            QuickCommandsSettings.Slot slot = settings.slots[i];
            this.fields[i * 2] = QuickCommandsEditorScreen.input(32, 90 + 52 * i, 312, slot.value, 256);
            this.fields[i * 2 + 1] = QuickCommandsEditorScreen.input(370, 90 + 52 * i, 166, slot.label, 28);
        }
        this.fields[0].setFocused(true);
    }

    private static GuiTextField input(int x, int y, int width, String value, int limit) {
        GuiTextField field = new GuiTextField(x, y, width, 24, "");
        field.setTextSize(12.0f);
        field.setMaxLength(limit);
        field.setValue(value);
        return field;
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25420(class_332 g, int mx, int my, float delta) {
        if (this.field_22787.field_1687 == null) {
            this.method_57728(g, delta);
        }
        MenuBackdrop.render(g);
        this.field_22787.field_1705.method_72738();
    }

    public void method_25419() {
        this.field_22787.method_1507(this.parent);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void method_25394(class_332 g, int mouseX, int mouseY, float delta) {
        this.scale = Math.min(0.7f, Math.min(((float)this.field_22789 - 16.0f) / 568.0f, ((float)this.field_22790 - 16.0f) / 356.0f));
        this.left = Math.round(((float)this.field_22789 - 568.0f * this.scale) / 2.0f);
        this.top = Math.round(((float)this.field_22790 - 356.0f * this.scale) / 2.0f);
        int mx = (int)((float)(mouseX - this.left) / this.scale);
        int my = (int)((float)(mouseY - this.top) / this.scale);
        g.method_25294(0, 0, this.field_22789, this.field_22790, -1610020340);
        g.method_51448().pushMatrix();
        try {
            g.method_51448().translate((float)this.left, (float)this.top);
            g.method_51448().scale(this.scale, this.scale);
            LiquidGlass.surfaceMenu(g, 0, 0, 568, 356, 15, ThemeColors.surface(-149218526), BloodHelperClient.config());
            QuickCommandsEditorScreen.text(g, "Quick Commands", 24.0f, 18.0f, 18.0f, -921099);
            QuickCommandsEditorScreen.text(g, HudStyle.tr("\u0427\u0435\u0442\u044b\u0440\u0435 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f \u0432 \u043a\u0440\u0443\u0433\u043e\u0432\u043e\u043c \u043c\u0435\u043d\u044e \u00b7 ", "Four actions in the wheel \u00b7 ") + QuickCommandsModule.bindingName(), 24.0f, 43.0f, 11.0f, -5592395);
            if (StreamerMode.hideChat()) {
                for (GuiTextField field : this.fields) {
                    field.setFocused(false);
                }
                QuickCommandsEditorScreen.text(g, "Streamer Mode", 24.0f, 94.0f, 17.0f, -921099);
                QuickCommandsEditorScreen.text(g, HudStyle.tr("\u0422\u0435\u043a\u0441\u0442\u044b \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0439 \u0441\u043a\u0440\u044b\u0442\u044b \u0432\u043c\u0435\u0441\u0442\u0435 \u0441 \u0447\u0430\u0442\u043e\u043c.", "Shortcut texts are hidden along with chat."), 24.0f, 126.0f, 12.0f, -4539708);
                QuickCommandsEditorScreen.text(g, HudStyle.tr("\u0414\u043b\u044f \u0440\u0435\u0434\u0430\u043a\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0438 \u0441\u043a\u0440\u044b\u0442\u0438\u0435 \u0447\u0430\u0442\u0430 \u0432 Streamer Mode.", "Turn off Hide chat in Streamer Mode to edit these shortcuts."), 24.0f, 148.0f, 12.0f, -4539708);
                this.button(g, 426, 312, 118, HudStyle.tr("\u041d\u0430\u0437\u0430\u0434", "Back"), mx, my, false);
                return;
            }
            for (int i = 0; i < 4; ++i) {
                int y = 72 + i * 52;
                String position = i + 1 + "  " + HudStyle.tr(RU_POSITION[i], EN_POSITION[i]);
                QuickCommandsEditorScreen.text(g, position, 24.0f, y, 10.0f, ThemeColors.text(-4539708));
                Optional<QuickCommandAction> parsed = QuickCommandAction.parse(this.fields[i * 2].getValue());
                String kind = parsed.isEmpty() ? HudStyle.tr("\u041f\u0443\u0441\u0442\u043e\u0439 \u0441\u043b\u043e\u0442", "Empty slot") : (parsed.get().command() ? HudStyle.tr("\u041a\u043e\u043c\u0430\u043d\u0434\u0430", "Command") : HudStyle.tr("\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u0432 \u0447\u0430\u0442", "Chat message"));
                QuickCommandsEditorScreen.text(g, kind, 150.0f, y, 10.0f, -6710874);
                QuickCommandsEditorScreen.text(g, HudStyle.tr("\u041f\u043e\u0434\u043f\u0438\u0441\u044c (\u043d\u0435\u043e\u0431\u044f\u0437\u0430\u0442\u0435\u043b\u044c\u043d\u043e)", "Label (optional)"), 362.0f, y, 10.0f, -6710874);
                for (int j = 0; j < 2; ++j) {
                    GuiTextField field = this.fields[i * 2 + j];
                    field.setHint(j == 1 ? HudStyle.tr("\u041a\u0430\u043a \u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0432 \u043a\u0440\u0443\u0433\u0435", "Name in the wheel") : HudStyle.tr("\u0422\u0435\u043a\u0441\u0442 \u0438\u043b\u0438 /\u043a\u043e\u043c\u0430\u043d\u0434\u0430", "Text or /command"));
                    UiDraw.border(g, field.getX() - 8, field.getY() - 2, field.getWidth() + 16, 28, 6, ThemeColors.surface(-284159976), field.isFocused() ? ThemeColors.accent() : ThemeColors.surface(-7303013));
                    field.render(g, mx, my, delta);
                }
            }
            QuickCommandsEditorScreen.text(g, HudStyle.tr("GG \u2192 \u0432 \u0447\u0430\u0442. /spawn \u2192 \u043a\u043e\u043c\u0430\u043d\u0434\u0430. \u041f\u0443\u0441\u0442\u044b\u0435 \u0441\u043b\u043e\u0442\u044b \u043d\u0435\u0430\u043a\u0442\u0438\u0432\u043d\u044b.", "GG \u2192 chat. /spawn \u2192 command. Empty slots are disabled."), 24.0f, 287.0f, 11.0f, -5592395);
            this.button(g, 322, 312, 96, HudStyle.tr("\u041e\u0442\u043c\u0435\u043d\u0430", "Cancel"), mx, my, false);
            this.button(g, 426, 312, 118, HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c", "Save"), mx, my, true);
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    private void button(class_332 g, int x, int y, int w, String label, int mx, int my, boolean primary) {
        boolean hover = QuickCommandsEditorScreen.inside(mx, my, x, y, w, 28);
        int fill = primary ? UiDraw.blend(ThemeColors.accent(), 0xFFFFFF, hover ? 0.15 : 0.0) : ThemeColors.surface(hover ? -9408390 : -12303280);
        UiDraw.rounded(g, x, y, w, 28, 7, fill);
        float textX = (float)x + ((float)w - CustomFont.medium().width(label, 12.0f)) / 2.0f;
        if (primary) {
            double luminance = 0.2126 * QuickCommandsEditorScreen.linear(fill >>> 16 & 0xFF) + 0.7152 * QuickCommandsEditorScreen.linear(fill >>> 8 & 0xFF) + 0.0722 * QuickCommandsEditorScreen.linear(fill & 0xFF);
            CustomFont.medium().drawRaw(g, label, textX, y + 7, 12.0f, luminance > 0.179 ? -16777216 : -1);
        } else {
            QuickCommandsEditorScreen.text(g, label, textX, y + 7, 12.0f, -1);
        }
    }

    private static double linear(int channel) {
        double value = (double)channel / 255.0;
        return value <= 0.04045 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
    }

    private static void text(class_332 g, String value, float x, float y, float size, int color) {
        CustomFont.medium().draw(g, value, x, y, size, color);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
    }

    private class_11909 local(class_11909 e) {
        return new class_11909((e.comp_4798() - (double)this.left) / (double)this.scale, (e.comp_4799() - (double)this.top) / (double)this.scale, e.comp_4800());
    }

    private void save() {
        if (StreamerMode.hideChat()) {
            return;
        }
        QuickCommandsSettings settings = BloodHelperClient.config().quickCommands;
        settings.sanitize();
        for (int i = 0; i < 4; ++i) {
            settings.slots[i] = new QuickCommandsSettings.Slot(this.fields[i * 2 + 1].getValue(), this.fields[i * 2].getValue());
        }
        settings.sanitize();
        ConfigStore.requestSave();
        this.method_25419();
    }

    public boolean method_25402(class_11909 event, boolean doubled) {
        class_11909 e = this.local(event);
        if (StreamerMode.hideChat()) {
            if (event.method_74245() == 0 && QuickCommandsEditorScreen.inside(e.comp_4798(), e.comp_4799(), 426, 312, 118, 28)) {
                this.method_25419();
            }
            return true;
        }
        boolean used = false;
        for (GuiTextField field : this.fields) {
            used |= field.mouseClicked(e, doubled);
        }
        if (event.method_74245() == 0) {
            if (QuickCommandsEditorScreen.inside(e.comp_4798(), e.comp_4799(), 426, 312, 118, 28)) {
                this.save();
                return true;
            }
            if (QuickCommandsEditorScreen.inside(e.comp_4798(), e.comp_4799(), 322, 312, 96, 28)) {
                this.method_25419();
                return true;
            }
        }
        return used || super.method_25402(event, doubled);
    }

    public boolean method_25403(class_11909 event, double dx, double dy) {
        if (StreamerMode.hideChat()) {
            return true;
        }
        for (GuiTextField field : this.fields) {
            if (!field.mouseDragged(this.local(event), dx / (double)this.scale, dy / (double)this.scale)) continue;
            return true;
        }
        return false;
    }

    public boolean method_25406(class_11909 event) {
        if (StreamerMode.hideChat()) {
            return true;
        }
        boolean used = false;
        for (GuiTextField field : this.fields) {
            used |= field.mouseReleased(this.local(event));
        }
        return used;
    }

    public boolean method_25404(class_11908 event) {
        if (StreamerMode.hideChat()) {
            if (event.comp_4795() == 256) {
                this.method_25419();
            }
            return true;
        }
        if (event.comp_4795() == 258) {
            int selected = 0;
            for (int i = 0; i < this.fields.length; ++i) {
                if (!this.fields[i].isFocused()) continue;
                selected = i;
            }
            int next = Math.floorMod(selected + (event.method_74239() ? -1 : 1), this.fields.length);
            for (int i = 0; i < this.fields.length; ++i) {
                this.fields[i].setFocused(i == next);
            }
            return true;
        }
        if (event.comp_4795() == 257 || event.comp_4795() == 335) {
            this.save();
            return true;
        }
        for (GuiTextField field : this.fields) {
            if (!field.keyPressed(event)) continue;
            return true;
        }
        return super.method_25404(event);
    }

    public boolean method_25400(class_11905 event) {
        if (StreamerMode.hideChat()) {
            return true;
        }
        for (GuiTextField field : this.fields) {
            if (!field.charTyped(event)) continue;
            return true;
        }
        return super.method_25400(event);
    }
}

