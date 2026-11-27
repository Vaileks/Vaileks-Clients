/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 */
package ru.bloodhelper.client.gui;

import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;

public final class ThemeColorPicker {
    public static final int X = 224;
    public static final int Y = 82;
    public static final int WIDTH = 352;
    public static final int HEIGHT = 342;
    private static final int SV_X = 240;
    private static final int SV_Y = 130;
    private static final int SV_W = 228;
    private static final int SV_H = 176;
    private static final int HUE_Y = 318;
    private static final int HUE_H = 14;
    private final boolean russian;
    private final IntConsumer changed;
    private final Runnable closed;
    private final GuiTextField hex = new GuiTextField(282, 351, 128, 20, "#RRGGBB");
    private final GuiTextField red = ThemeColorPicker.channel(210);
    private final GuiTextField green = ThemeColorPicker.channel(244);
    private final GuiTextField blue = ThemeColorPicker.channel(278);
    private final List<GuiTextField> fields = List.of(this.hex, this.red, this.green, this.blue);
    private GuiTextField focused;
    private double hue;
    private double saturation;
    private double value;
    private int color;
    private boolean syncing;
    private int drag;

    public ThemeColorPicker(int color, boolean russian, IntConsumer changed, Runnable closed) {
        this.russian = russian;
        this.changed = changed;
        this.closed = closed;
        this.hex.setMaxLength(7);
        this.hex.setTextSize(12.0f);
        this.hex.setFilter(v -> v.matches("#?[0-9a-fA-F]{0,6}"));
        this.hex.setResponder(v -> {
            if (!this.syncing) {
                ThemeColors.parseHex(v).ifPresent(c -> this.setColor(c, this.hex));
            }
        });
        this.red.setResponder(v -> this.applyChannels());
        this.green.setResponder(v -> this.applyChannels());
        this.blue.setResponder(v -> this.applyChannels());
        this.setColor(color, null);
    }

    private static GuiTextField channel(int y) {
        GuiTextField field = new GuiTextField(508, y, 48, 20, "0");
        field.setTextSize(12.0f);
        field.setMaxLength(3);
        field.setFilter(v -> v.matches("[0-9]{0,3}"));
        return field;
    }

    private String tr(String ru, String en) {
        return this.russian ? ru : en;
    }

    private void setColor(int next, GuiTextField preserve) {
        this.color = next | 0xFF000000;
        float[] hsv = ThemeColors.hsv(this.color);
        if (hsv[1] > 1.0E-4f && hsv[2] > 1.0E-4f) {
            this.hue = hsv[0];
        }
        this.saturation = hsv[1];
        this.value = hsv[2];
        this.refreshFields(preserve);
        this.changed.accept(this.color);
    }

    private void refreshFields(GuiTextField preserve) {
        this.syncing = true;
        try {
            if (preserve != this.hex) {
                this.hex.setValue(ThemeColors.hex(this.color));
            }
            if (preserve != this.red) {
                this.red.setValue(Integer.toString(this.color >>> 16 & 0xFF));
            }
            if (preserve != this.green) {
                this.green.setValue(Integer.toString(this.color >>> 8 & 0xFF));
            }
            if (preserve != this.blue) {
                this.blue.setValue(Integer.toString(this.color & 0xFF));
            }
        }
        finally {
            this.syncing = false;
        }
    }

    private void applyChannels() {
        if (this.syncing) {
            return;
        }
        try {
            int r = Integer.parseInt(this.red.getValue());
            int g = Integer.parseInt(this.green.getValue());
            int b = Integer.parseInt(this.blue.getValue());
            if (r > 255 || g > 255 || b > 255) {
                return;
            }
            this.setColor(0xFF000000 | r << 16 | g << 8 | b, this.focused);
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
    }

    public void render(GuiGraphicsExtractor g, double mouseX, double mouseY) {
        g.fill(0, 0, 800, 500, -1342177280);
        UiDraw.rounded(g, 224, 86, 352, 342, 8, 0x70000000);
        UiDraw.border(g, 224, 82, 352, 342, 8, ThemeColors.surface(-13618631), ThemeColors.surface(-6907484));
        CustomFont.medium().draw(g, this.tr("\u0426\u0432\u0435\u0442 \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u044f", "Theme colour"), 240.0f, 96.0f, 15.0f, -855308);
        UiIcons.draw(g, "close", 547, 96, 13, -2236444);
        for (int xx = 0; xx < 228; xx += 2) {
            int top = ThemeColors.hsv(this.hue, (double)xx / 227.0, 1.0);
            g.fillGradient(240 + xx, 130, 240 + Math.min(228, xx + 2), 306, top, -16777216);
        }
        UiDraw.outline(g, 239, 129, 230, 178, 1, -10131600);
        int markerX = 240 + (int)Math.round(this.saturation * 227.0);
        int markerY = 130 + (int)Math.round((1.0 - this.value) * 175.0);
        UiDraw.outline(g, markerX - 4, markerY - 4, 9, 9, 4, -16777216);
        UiDraw.outline(g, markerX - 3, markerY - 3, 7, 7, 3, -1);
        for (int xx = 0; xx < 228; xx += 2) {
            g.fill(240 + xx, 318, 240 + Math.min(228, xx + 2), 332, ThemeColors.hsv((double)xx / 227.0, 1.0, 1.0));
        }
        int hueX = 240 + (int)Math.round(this.hue * 227.0);
        UiDraw.border(g, hueX - 3, 315, 7, 20, 2, ThemeColors.hsv(this.hue, 1.0, 1.0), -1);
        UiDraw.rounded(g, 484, 130, 76, 44, 4, this.color);
        ThemeColorPicker.label(g, this.tr("\u041f\u0440\u043e\u0441\u043c\u043e\u0442\u0440", "Preview"), 484.0f, 184.0f, 11.0f, -4210231);
        this.field(g, this.red, "R", mouseX, mouseY);
        this.field(g, this.green, "G", mouseX, mouseY);
        this.field(g, this.blue, "B", mouseX, mouseY);
        ThemeColorPicker.label(g, "HEX", 240.0f, 355.0f, 12.0f, -2828837);
        this.input(g, this.hex, mouseX, mouseY, ThemeColors.parseHex(this.hex.getValue()).isPresent());
        boolean hover = ThemeColorPicker.inside(mouseX, mouseY, 442, 346, 118, 30);
        UiDraw.border(g, 442, 346, 118, 30, 4, ThemeColors.surface(hover ? -9144445 : -12434098), ThemeColors.surface(-6907484));
        String reset = this.tr("\u0421\u0431\u0440\u043e\u0441\u0438\u0442\u044c", "Reset");
        ThemeColorPicker.label(g, reset, 442.0f + (118.0f - CustomFont.regular().width(reset, 12.0f)) / 2.0f, 353.0f, 12.0f, -1973529);
        ThemeColorPicker.label(g, this.tr("\u041b\u041a\u041c \u0438 \u043f\u0435\u0440\u0435\u0442\u0430\u0441\u043a\u0438\u0432\u0430\u043d\u0438\u0435 \u2014 \u0432\u044b\u0431\u043e\u0440 \u0446\u0432\u0435\u0442\u0430", "Click or drag to pick a colour"), 240.0f, 392.0f, 11.0f, -4538939);
        ThemeColorPicker.label(g, this.tr("Esc \u2014 \u0437\u0430\u043a\u0440\u044b\u0442\u044c", "Esc \u2014 close"), 474.0f, 392.0f, 10.0f, -4538939);
    }

    private void field(GuiGraphicsExtractor g, GuiTextField field, String name, double mx, double my) {
        boolean valid;
        ThemeColorPicker.label(g, name, 484.0f, field.getY() + 4, 12.0f, -2236443);
        try {
            valid = Integer.parseInt(field.getValue()) <= 255;
        }
        catch (NumberFormatException ignored) {
            valid = false;
        }
        this.input(g, field, mx, my, valid);
    }

    private void input(GuiGraphicsExtractor g, GuiTextField field, double mx, double my, boolean valid) {
        int edge = !valid ? -1541754 : (field == this.focused ? ThemeColors.icon(-1973530) : ThemeColors.surface(-6907484));
        UiDraw.border(g, field.getX() - 4, field.getY() - 3, field.getWidth() + 8, field.getHeight() + 6, 3, ThemeColors.surface(-15197664), edge);
        field.render(g, (int)mx, (int)my, 0.0f);
    }

    private static void label(GuiGraphicsExtractor g, String text, float x, float y, float size, int argb) {
        CustomFont.regular().draw(g, text, x, y, size, argb);
    }

    private void focus(GuiTextField next) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }
        this.focused = next;
        if (next != null) {
            next.setFocused(true);
        }
    }

    private void updatePointer(double x, double y) {
        if (this.drag == 1) {
            this.saturation = Math.clamp((x - 240.0) / 227.0, 0.0, 1.0);
            this.value = 1.0 - Math.clamp((y - 130.0) / 175.0, 0.0, 1.0);
        } else if (this.drag == 2) {
            this.hue = Math.clamp((x - 240.0) / 227.0, 0.0, 1.0);
        }
        this.color = ThemeColors.hsv(this.hue, this.saturation, this.value);
        this.refreshFields(null);
        this.changed.accept(this.color);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double y;
        if (event.button() != 1) {
            return true;
        }
        double x = event.x();
        if (!ThemeColorPicker.inside(x, y = event.y(), 224, 82, 352, 342) || ThemeColorPicker.inside(x, y, 538, 87, 32, 30)) {
            this.close();
            return true;
        }
        if (ThemeColorPicker.inside(x, y, 240, 130, 228, 176)) {
            this.focus(null);
            this.drag = 1;
            this.updatePointer(x, y);
            return true;
        }
        if (ThemeColorPicker.inside(x, y, 236, 313, 236, 24)) {
            this.focus(null);
            this.drag = 2;
            this.updatePointer(x, y);
            return true;
        }
        if (ThemeColorPicker.inside(x, y, 442, 346, 118, 30)) {
            this.focus(null);
            this.setColor(ThemeColors.defaultAccent(), null);
            return true;
        }
        for (GuiTextField field : this.fields) {
            if (!ThemeColorPicker.inside(x, y, field.getX() - 4, field.getY() - 3, field.getWidth() + 8, field.getHeight() + 6)) continue;
            this.focus(field);
            field.mouseClicked(new MouseButtonEvent(Math.clamp(x, (double)field.getX(), (double)(field.getX() + field.getWidth()) - 0.01), Math.clamp(y, (double)field.getY(), (double)(field.getY() + field.getHeight()) - 0.01), event.buttonInfo()), doubled);
            return true;
        }
        this.focus(null);
        return true;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.drag != 0) {
            this.updatePointer(event.x(), event.y());
        } else if (this.focused != null) {
            this.focused.mouseDragged(event, dx, dy);
        }
        return true;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        this.drag = 0;
        if (this.focused != null) {
            this.focused.mouseReleased(event);
        }
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 41 || event.key() == 40) {
            this.close();
            return true;
        }
        if (event.key() == 43) {
            int current;
            int n = current = this.focused == null ? -1 : this.fields.indexOf(this.focused);
            int next = current < 0 ? (event.hasShiftDown() ? this.fields.size() - 1 : 0) : Math.floorMod(current + (event.hasShiftDown() ? -1 : 1), this.fields.size());
            this.focus(this.fields.get(next));
            return true;
        }
        if (this.focused != null) {
            this.focused.keyPressed(event);
        }
        return true;
    }

    public boolean charTyped(CharacterEvent event) {
        if (this.focused != null) {
            this.focused.charTyped(event);
        }
        return true;
    }

    private void close() {
        this.focus(null);
        this.drag = 0;
        this.closed.run();
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= (double)left && x < (double)(left + width) && y >= (double)top && y < (double)(top + height);
    }
}

