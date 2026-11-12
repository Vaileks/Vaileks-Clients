/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.HudItemControlsSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class HudItemControlsScreen
extends ModuleDetailScreen {
    private Page page;
    private int slot;
    private int rowY;
    private int mouseX;
    private int mouseY;
    private final List<Slider> sliders = new ArrayList<Slider>();
    private Slider dragging;

    public HudItemControlsScreen(class_437 parent) {
        this(parent, Page.HOTBAR);
    }

    public HudItemControlsScreen(class_437 parent, Page page) {
        super(parent, "HUD controls");
        this.page = page;
    }

    @Override
    protected int preferredPanelWidth() {
        return 620;
    }

    @Override
    protected int preferredPanelHeight() {
        return 465;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("HUD \u00b7 \u0440\u0430\u0441\u043f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435 \u0438 \u0432\u043d\u0435\u0448\u043d\u0438\u0439 \u0432\u0438\u0434", "HUD \u00b7 placement and appearance"), mx, my);
        this.mouseX = mx;
        this.mouseY = my;
        this.sliders.clear();
        tabWidth = Math.max(1, (this.panelWidth - 34) / 6);
        names = new String[]{HudStyle.tr("\u0425\u043e\u0442\u0431\u0430\u0440", "Hotbar"), HudStyle.tr("\u0422\u043e\u0442\u0435\u043c", "Totem"), HudStyle.tr("\u0422\u0430\u0431\u043b\u043e", "Scoreboard"), HudStyle.tr("\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435", "Health"), HudStyle.tr("\u0417\u0430\u0433\u043e\u043b\u043e\u0432\u043a\u0438", "Titles"), "Actionbar"};
        for (Page p : Page.values()) {
            x = this.left + 14 + p.ordinal() * tabWidth;
            this.button(g, x, this.bodyTop, tabWidth - 3, names[p.ordinal()], mx, my, (Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$renderPanel$0(ru.bloodhelper.client.modules.HudItemControlsScreen$Page ), ()V)((HudItemControlsScreen)this, (Page)p));
            if (p != this.page) continue;
            UiDraw.outline(g, x, this.bodyTop, tabWidth - 3, 21, 6, ThemeColors.accent());
        }
        this.bodyTop += 31;
        s = ConfigStore.get().hudItemControls;
        switch (this.page.ordinal()) {
            case 0: {
                v0 = 15;
                break;
            }
            case 1: {
                v0 = 7;
                break;
            }
            case 2: {
                v0 = 10;
                break;
            }
            case 3: {
                v0 = 7;
                break;
            }
            default: {
                v0 = 5;
            }
        }
        rows = v0;
        this.scrollbar(g, rows * 31 + 10);
        this.rowY = this.bodyTop - this.scroll;
        g.method_44379(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            switch (this.page.ordinal()) {
                case 0: {
                    this.hotbar(g, s.hotbar);
                    ** break;
lbl39:
                    // 1 sources

                    break;
                }
                case 1: {
                    this.placement(g, s.totem);
                    this.slider(g, HudStyle.tr("\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c, \u0441\u0435\u043a\u0443\u043d\u0434\u044b", "Duration, seconds"), (DoubleSupplier)LambdaMetafactory.metafactory(null, null, null, ()D, lambda$renderPanel$1(ru.bloodhelper.client.config.HudItemControlsSettings ), ()D)((HudItemControlsSettings)s), 0.25, 6.0, 0.05, (DoubleConsumer)LambdaMetafactory.metafactory(null, null, null, (D)V, lambda$renderPanel$2(ru.bloodhelper.client.config.HudItemControlsSettings double ), (D)V)((HudItemControlsSettings)s));
                    this.action(g, HudStyle.tr("\u041b\u043e\u043a\u0430\u043b\u044c\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440", "Local preview"), HudStyle.tr("\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0442\u043e\u0442\u0435\u043c", "Show totem"), (Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$renderPanel$3(), ()V)((HudItemControlsScreen)this));
                    ** break;
lbl45:
                    // 1 sources

                    break;
                }
                case 2: {
                    this.placement(g, s.scoreboard);
                    this.slider(g, HudStyle.tr("\u041d\u0435\u043f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0444\u043e\u043d\u0430", "Background opacity"), (DoubleSupplier)LambdaMetafactory.metafactory(null, null, null, ()D, lambda$renderPanel$4(ru.bloodhelper.client.config.HudItemControlsSettings ), ()D)((HudItemControlsSettings)s), 0.0, 100.0, 1.0, (DoubleConsumer)LambdaMetafactory.metafactory(null, null, null, (D)V, lambda$renderPanel$5(ru.bloodhelper.client.config.HudItemControlsSettings double ), (D)V)((HudItemControlsSettings)s));
                    this.slider(g, HudStyle.tr("\u041e\u0442\u0441\u0442\u0443\u043f \u043c\u0435\u0436\u0434\u0443 \u0441\u0442\u0440\u043e\u043a\u0430\u043c\u0438", "Line spacing"), (DoubleSupplier)LambdaMetafactory.metafactory(null, null, null, ()D, lambda$renderPanel$6(ru.bloodhelper.client.config.HudItemControlsSettings ), ()D)((HudItemControlsSettings)s), 0.0, 12.0, 1.0, (DoubleConsumer)LambdaMetafactory.metafactory(null, null, null, (D)V, lambda$renderPanel$7(ru.bloodhelper.client.config.HudItemControlsSettings double ), (D)V)((HudItemControlsSettings)s));
                    this.color(g, (DoubleSupplier)LambdaMetafactory.metafactory(null, null, null, ()D, lambda$renderPanel$8(ru.bloodhelper.client.config.HudItemControlsSettings ), ()D)((HudItemControlsSettings)s), (DoubleConsumer)LambdaMetafactory.metafactory(null, null, null, (D)V, lambda$renderPanel$9(ru.bloodhelper.client.config.HudItemControlsSettings double ), (D)V)((HudItemControlsSettings)s));
                    ** break;
lbl52:
                    // 1 sources

                    break;
                }
                case 3: {
                    this.placement(g, s.health);
                    v1 = HudStyle.tr("\u0412\u0438\u0434 \u043f\u0430\u043d\u0435\u043b\u0438", "Display style");
                    switch (1.$SwitchMap$ru$bloodhelper$client$config$HudItemControlsSettings$HealthStyle[s.health.style.ordinal()]) {
                        default: {
                            throw new MatchException(null, null);
                        }
                        case 1: {
                            v2 = HudStyle.tr("\u0421\u0435\u0440\u0434\u0446\u0430", "Hearts");
                            break;
                        }
                        case 2: {
                            v2 = HudStyle.tr("\u041f\u043e\u043b\u043e\u0441\u043a\u0430", "Bar");
                            break;
                        }
                        case 3: {
                            v2 = HudStyle.tr("\u0427\u0438\u0441\u043b\u043e", "Number");
                        }
                    }
                    this.action(g, v1, v2, (Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$renderPanel$10(ru.bloodhelper.client.config.HudItemControlsSettings ), ()V)((HudItemControlsSettings)s));
                    this.toggle(g, HudStyle.tr("\u0417\u0430\u043c\u0435\u043d\u0438\u0442\u044c \u0432\u0430\u043d\u0438\u043b\u044c\u043d\u044b\u0435 \u0441\u0435\u0440\u0434\u0446\u0430", "Replace vanilla hearts"), (BooleanSupplier)LambdaMetafactory.metafactory(null, null, null, ()Z, lambda$renderPanel$11(ru.bloodhelper.client.config.HudItemControlsSettings ), ()Z)((HudItemControlsSettings)s), (Runnable)LambdaMetafactory.metafactory(null, null, null, ()V, lambda$renderPanel$12(ru.bloodhelper.client.config.HudItemControlsSettings ), ()V)((HudItemControlsSettings)s));
                    ** break;
lbl70:
                    // 1 sources

                    break;
                }
                case 4: {
                    this.placement(g, s.titles);
                    ** break;
lbl74:
                    // 1 sources

                    break;
                }
                case 5: {
                    this.placement(g, s.actionbar);
                    break;
                }
                ** default:
lbl79:
                // 1 sources

                break;
            }
        }
        finally {
            g.method_44380();
        }
        this.footer(g, HudStyle.tr("\u0418\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u044f \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u044e\u0442\u0441\u044f \u0441\u0440\u0430\u0437\u0443 \u00b7 X/Y \u0432 \u043f\u0440\u043e\u0446\u0435\u043d\u0442\u0430\u0445 \u043e\u043a\u043d\u0430 \u00b7 \u0437\u0434\u043e\u0440\u043e\u0432\u044c\u0435 \u0441 \u0437\u043e\u043b\u043e\u0442\u044b\u043c \u043f\u043e\u0433\u043b\u043e\u0449\u0435\u043d\u0438\u0435\u043c", "Saved immediately \u00b7 X/Y as window percentages \u00b7 health includes golden absorption"));
    }

    private void placement(class_332 g, HudItemControlsSettings.Placement p) {
        this.toggle(g, HudStyle.tr("\u0412\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0443", "Enable customization"), () -> p.enabled, () -> {
            p.enabled = !p.enabled;
        });
        this.slider(g, HudStyle.tr("\u0420\u0430\u0437\u043c\u0435\u0440, %", "Scale, %"), () -> p.scale * 100.0, 25.0, 300.0, 5.0, v -> {
            p.scale = v / 100.0;
        });
        this.slider(g, HudStyle.tr("\u041f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435 X, %", "Position X, %"), () -> p.x * 100.0, 0.0, 100.0, 1.0, v -> {
            p.x = v / 100.0;
        });
        this.slider(g, HudStyle.tr("\u041f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435 Y, %", "Position Y, %"), () -> p.y * 100.0, 0.0, 100.0, 1.0, v -> {
            p.y = v / 100.0;
        });
        this.action(g, HudStyle.tr("\u0411\u044b\u0441\u0442\u0440\u043e\u0435 \u0440\u0430\u0441\u043f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435", "Placement preset"), HudStyle.tr("\u0426\u0435\u043d\u0442\u0440 / \u043d\u0438\u0436\u0435", "Center / lower"), () -> {
            p.x = 0.5;
            p.y = p.y < 0.7 ? 0.8 : 0.5;
            HudItemControlsScreen.save();
        });
    }

    private void hotbar(class_332 g, HudItemControlsSettings.Hotbar h) {
        this.toggle(g, HudStyle.tr("\u0426\u0432\u0435\u0442\u043e\u0432\u044b\u0435 \u043c\u0435\u0442\u043a\u0438", "Colored markers"), () -> h.markersEnabled, () -> {
            h.markersEnabled = !h.markersEnabled;
        });
        int y = this.rowY;
        this.rowY += 31;
        if (this.visible(y)) {
            int w = Math.max(1, (this.panelWidth - 40) / 9);
            for (int i = 0; i < 9; ++i) {
                int index = i;
                int x = this.left + 16 + i * w;
                this.button(g, x, y, w - 4, Integer.toString(i + 1), this.mouseX, this.mouseY, () -> {
                    this.slot = index;
                    this.finishDrag();
                });
                g.method_25294(x + 3, y + 18, x + w - 7, y + 21, 0xFF000000 | h.colors[i]);
                if (this.slot != i) continue;
                UiDraw.outline(g, x, y, w - 4, 21, 5, ThemeColors.accent());
            }
        }
        this.toggle(g, HudStyle.tr("\u041c\u0435\u0442\u043a\u0430 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0433\u043e \u0441\u043b\u043e\u0442\u0430", "Selected slot marker"), () -> h.marked[this.slot], () -> {
            h.marked[this.slot] = !h.marked[this.slot];
        });
        this.color(g, () -> h.colors[this.slot], v -> {
            h.colors[this.slot] = (int)v;
        });
        this.slider(g, HudStyle.tr("\u041d\u0435\u043f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u043c\u0435\u0442\u043e\u043a, %", "Marker opacity, %"), () -> h.markerOpacity * 100.0, 0.0, 100.0, 1.0, v -> {
            h.markerOpacity = v / 100.0;
        });
        this.action(g, HudStyle.tr("\u041f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435 \u043c\u0435\u0442\u043e\u043a", "Marker position"), HudItemControlsScreen.edge(h.markerEdge), () -> {
            h.markerEdge = HudItemControlsScreen.opposite(h.markerEdge);
            HudItemControlsScreen.save();
        });
        this.toggle(g, HudStyle.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u044f \u043d\u0430\u0437\u043d\u0430\u0447\u0435\u043d\u043d\u044b\u0445 \u043a\u043b\u0430\u0432\u0438\u0448", "Actual assigned keys"), () -> h.keysEnabled, () -> {
            h.keysEnabled = !h.keysEnabled;
        });
        this.slider(g, HudStyle.tr("\u0420\u0430\u0437\u043c\u0435\u0440 \u043f\u043e\u0434\u043f\u0438\u0441\u0435\u0439, %", "Key label size, %"), () -> h.keyScale * 100.0, 35.0, 150.0, 5.0, v -> {
            h.keyScale = v / 100.0;
        });
        this.slider(g, HudStyle.tr("\u041d\u0435\u043f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u043f\u043e\u0434\u043f\u0438\u0441\u0435\u0439, %", "Key label opacity, %"), () -> h.keyOpacity * 100.0, 0.0, 100.0, 1.0, v -> {
            h.keyOpacity = v / 100.0;
        });
        this.action(g, HudStyle.tr("\u041f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u0435 \u043f\u043e\u0434\u043f\u0438\u0441\u0435\u0439", "Key label position"), HudItemControlsScreen.edge(h.keyEdge), () -> {
            h.keyEdge = HudItemControlsScreen.opposite(h.keyEdge);
            HudItemControlsScreen.save();
        });
        this.slider(g, HudStyle.tr("\u0421\u0434\u0432\u0438\u0433 \u043f\u043e\u0434\u043f\u0438\u0441\u0435\u0439 \u043f\u043e Y", "Key label Y offset"), () -> h.keyOffset, -20.0, 20.0, 1.0, v -> {
            h.keyOffset = (int)v;
        });
        this.action(g, HudStyle.tr("\u0412\u0435\u0440\u043d\u0443\u0442\u044c \u0446\u0432\u0435\u0442\u0430 \u0432\u0441\u0435\u0445 \u0441\u043b\u043e\u0442\u043e\u0432", "Restore all slot colors"), HudStyle.tr("\u0421\u0431\u0440\u043e\u0441\u0438\u0442\u044c \u0446\u0432\u0435\u0442\u0430", "Reset colors"), () -> {
            h.colors = new HudItemControlsSettings.Hotbar().colors;
            HudItemControlsScreen.save();
        });
    }

    private static HudItemControlsSettings.Edge opposite(HudItemControlsSettings.Edge edge) {
        return edge == HudItemControlsSettings.Edge.TOP ? HudItemControlsSettings.Edge.BOTTOM : HudItemControlsSettings.Edge.TOP;
    }

    private static String edge(HudItemControlsSettings.Edge edge) {
        return edge == HudItemControlsSettings.Edge.TOP ? HudStyle.tr("\u0421\u0432\u0435\u0440\u0445\u0443", "Top") : HudStyle.tr("\u0421\u043d\u0438\u0437\u0443", "Bottom");
    }

    private void color(class_332 g, DoubleSupplier value, DoubleConsumer update) {
        String[] labels = new String[]{HudStyle.tr("\u041a\u0440\u0430\u0441\u043d\u044b\u0439", "Red"), HudStyle.tr("\u0417\u0435\u043b\u0451\u043d\u044b\u0439", "Green"), HudStyle.tr("\u0421\u0438\u043d\u0438\u0439", "Blue")};
        for (int i = 0; i < 3; ++i) {
            int shift = (2 - i) * 8;
            this.slider(g, labels[i], () -> (int)value.getAsDouble() >> shift & 0xFF, 0.0, 255.0, 1.0, v -> update.accept((int)value.getAsDouble() & ~(255 << shift) | (int)v << shift));
        }
    }

    private boolean visible(int y) {
        return y >= this.bodyTop && y + 26 <= this.bodyBottom;
    }

    private void toggle(class_332 g, String label, BooleanSupplier value, Runnable change) {
        this.action(g, label, value.getAsBoolean() ? HudStyle.tr("\u0412\u043a\u043b\u044e\u0447\u0435\u043d\u043e", "Enabled") : HudStyle.tr("\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u043e", "Disabled"), () -> {
            change.run();
            HudItemControlsScreen.save();
        });
    }

    private void action(class_332 g, String label, String value, Runnable action) {
        int y = this.rowY;
        this.rowY += 31;
        if (!this.visible(y)) {
            return;
        }
        this.text(g, CustomFont.regular().ellipsize(label, 11.0f, Math.max(30, this.panelWidth - 205)), this.left + 17, y + 6, -1381908);
        this.button(g, this.left + this.panelWidth - 176, y + 1, 155, value, this.mouseX, this.mouseY, action);
    }

    private void slider(class_332 g, String label, DoubleSupplier value, double min, double max, double step, DoubleConsumer change) {
        int y = this.rowY;
        this.rowY += 31;
        if (!this.visible(y)) {
            return;
        }
        int width = Math.max(40, Math.min(210, this.panelWidth / 3));
        int x = this.left + this.panelWidth - width - 65;
        this.text(g, CustomFont.regular().ellipsize(label, 11.0f, Math.max(30, x - this.left - 32)), this.left + 17, y + 6, -1381908);
        double v = Math.clamp(value.getAsDouble(), min, max);
        double fraction = (v - min) / (max - min);
        UiDraw.rounded(g, x, y + 13, width, 3, 1, ThemeColors.surface(-11184801));
        UiDraw.rounded(g, x, y + 13, (int)Math.round((double)width * fraction), 3, 1, ThemeColors.accent());
        UiDraw.rounded(g, x + (int)Math.round((double)width * fraction) - 4, y + 9, 8, 11, 4, -1381908);
        String formatted = step < 1.0 ? String.format(Locale.ROOT, "%.2f", v) : Integer.toString((int)Math.round(v));
        this.text(g, formatted, x + width + 10, y + 6, -7105633);
        this.sliders.add(new Slider(x, y + 2, width, min, max, step, change));
    }

    private void change(double x) {
        if (this.dragging != null) {
            Slider s = this.dragging;
            double v = s.min + Math.clamp((x - (double)s.x) / (double)s.width, 0.0, 1.0) * (s.max - s.min);
            s.update.accept(Math.clamp((double)Math.round(v / s.step) * s.step, s.min, s.max));
        }
    }

    private void finishDrag() {
        if (this.dragging != null) {
            this.dragging = null;
            HudItemControlsScreen.save();
        }
    }

    private static void save() {
        ConfigStore.get().hudItemControls.sanitize();
        BloodHelperClient.saveConfig();
    }

    @Override
    protected boolean panelMouseClicked(class_11909 e, boolean doubled) {
        if (e.method_74245() == 0) {
            for (Slider s : this.sliders) {
                if (!(e.comp_4798() >= (double)(s.x - 5)) || !(e.comp_4798() <= (double)(s.x + s.width + 5)) || !(e.comp_4799() >= (double)s.y) || !(e.comp_4799() < (double)(s.y + 23))) continue;
                this.dragging = s;
                this.change(e.comp_4798());
                return true;
            }
        }
        return super.panelMouseClicked(e, doubled);
    }

    @Override
    protected boolean panelMouseDragged(class_11909 e, double dx, double dy) {
        if (this.dragging != null) {
            this.change(e.comp_4798());
            return true;
        }
        return super.panelMouseDragged(e, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(class_11909 e) {
        if (this.dragging != null) {
            this.finishDrag();
            return true;
        }
        return super.panelMouseReleased(e);
    }

    public void method_25432() {
        this.finishDrag();
    }

    private static /* synthetic */ void lambda$renderPanel$12(HudItemControlsSettings s) {
        s.health.replaceVanilla = !s.health.replaceVanilla;
    }

    private static /* synthetic */ boolean lambda$renderPanel$11(HudItemControlsSettings s) {
        return s.health.replaceVanilla;
    }

    private static /* synthetic */ void lambda$renderPanel$10(HudItemControlsSettings s) {
        HudItemControlsSettings.HealthStyle[] values = HudItemControlsSettings.HealthStyle.values();
        s.health.style = values[(s.health.style.ordinal() + 1) % values.length];
        HudItemControlsScreen.save();
    }

    private static /* synthetic */ void lambda$renderPanel$9(HudItemControlsSettings s, double v) {
        s.scoreboard.backgroundColor = (int)v;
    }

    private static /* synthetic */ double lambda$renderPanel$8(HudItemControlsSettings s) {
        return s.scoreboard.backgroundColor;
    }

    private static /* synthetic */ void lambda$renderPanel$7(HudItemControlsSettings s, double v) {
        s.scoreboard.lineSpacing = (int)v;
    }

    private static /* synthetic */ double lambda$renderPanel$6(HudItemControlsSettings s) {
        return s.scoreboard.lineSpacing;
    }

    private static /* synthetic */ void lambda$renderPanel$5(HudItemControlsSettings s, double v) {
        s.scoreboard.backgroundOpacity = v / 100.0;
    }

    private static /* synthetic */ double lambda$renderPanel$4(HudItemControlsSettings s) {
        return s.scoreboard.backgroundOpacity * 100.0;
    }

    private /* synthetic */ void lambda$renderPanel$3() {
        if (this.field_22787.field_1724 != null) {
            this.field_22787.method_1507(null);
            this.field_22787.field_1773.method_3189(new class_1799((class_1935)class_1802.field_8288));
        }
    }

    private static /* synthetic */ void lambda$renderPanel$2(HudItemControlsSettings s, double v) {
        s.totem.durationTicks = (int)Math.round(v * 20.0);
    }

    private static /* synthetic */ double lambda$renderPanel$1(HudItemControlsSettings s) {
        return (double)s.totem.durationTicks / 20.0;
    }

    private /* synthetic */ void lambda$renderPanel$0(Page p) {
        this.finishDrag();
        this.page = p;
        this.scroll = 0;
    }

    public static enum Page {
        HOTBAR,
        TOTEM,
        SCOREBOARD,
        HEALTH,
        TITLES,
        ACTIONBAR;

    }

    private record Slider(int x, int y, int width, double min, double max, double step, DoubleConsumer update) {
    }
}

