/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.CrosshairSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.CrosshairPattern;
import ru.bloodhelper.client.modules.CrosshairRenderer;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class CrosshairScreen
extends ModuleDetailScreen {
    private static final String[] RU = new String[]{"\u041a\u0440\u0435\u0441\u0442", "\u0422\u043e\u0447\u043a\u0430", "\u041a\u0440\u0443\u0433", "\u041a\u0440\u0443\u0433 \u0441 \u0442\u043e\u0447\u043a\u043e\u0439", "\u041a\u0432\u0430\u0434\u0440\u0430\u0442", "\u0420\u043e\u043c\u0431", "\u041a\u043e\u0441\u043e\u0439 \u043a\u0440\u0435\u0441\u0442", "T-\u043e\u0431\u0440\u0430\u0437\u043d\u044b\u0439", "\u0421\u043a\u043e\u0431\u043a\u0438", "\u041a\u0440\u0435\u0441\u0442 \u0441 \u0442\u043e\u0447\u043a\u043e\u0439", "\u0423\u0433\u043e\u043b\u043e\u043a", "\u0417\u0432\u0435\u0437\u0434\u0430", "\u0421\u0435\u0440\u0434\u0446\u0435", "\u0421\u043c\u0430\u0439\u043b\u0438\u043a"};
    private static final String[] EN = new String[]{"Cross", "Dot", "Circle", "Circle + dot", "Square", "Diamond", "X", "T shape", "Brackets", "Cross + dot", "Chevron", "Star", "Heart", "Smile"};
    private static final String[] MODES = new String[]{"SIMPLE", "PRESET", "CUSTOM"};
    private final List<CrosshairPattern.Mask> thumbnails = new ArrayList<CrosshairPattern.Mask>();
    private int background;
    private int previewTarget;
    private int gridX;
    private int gridY;
    private int cell;
    private int gridSide;
    private int paintButton = -1;
    private int lastX = -1;
    private int lastY = -1;
    private boolean dirty;
    private String notice = "";
    private long noticeUntil;

    public CrosshairScreen(class_437 parent) {
        super(parent, "Crosshair");
        for (String name : CrosshairPattern.PRESETS) {
            CrosshairSettings c = new CrosshairSettings();
            c.mode = "PRESET";
            c.preset = name;
            c.dot = false;
            c.size = 5;
            c.gap = 2;
            this.thumbnails.add(CrosshairPattern.create(c));
        }
    }

    private CrosshairSettings config() {
        return ConfigStore.get().crosshair;
    }

    @Override
    protected int preferredPanelWidth() {
        return 720;
    }

    @Override
    protected int preferredPanelHeight() {
        return 450;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        CrosshairSettings c = this.config();
        this.frame(g, "Crosshair", mx, my);
        int tab = Math.max(45, (this.panelWidth - 130) / 3);
        String[] names = new String[]{HudStyle.tr("\u041e\u0431\u044b\u0447\u043d\u044b\u0439", "Simple"), HudStyle.tr("\u0413\u043e\u0442\u043e\u0432\u044b\u0435", "Presets"), HudStyle.tr("\u0421\u0432\u043e\u0439 \u0440\u0438\u0441\u0443\u043d\u043e\u043a", "Custom")};
        for (int i = 0; i < 3; ++i) {
            int index = i;
            int x = this.left + 14 + i * (tab + 4);
            this.button(g, x, this.bodyTop, tab, names[i], mx, my, () -> {
                this.finishPaint();
                c.mode = MODES[index];
                this.save();
            });
            if (!c.mode.equals(MODES[i])) continue;
            UiDraw.outline(g, x, this.bodyTop, tab, 21, 6, ThemeColors.accent());
        }
        this.button(g, this.left + this.panelWidth - 94, this.bodyTop, 80, c.enabled ? HudStyle.tr("\u0412\u043a\u043b\u044e\u0447\u0451\u043d", "Enabled") : HudStyle.tr("\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d", "Disabled"), mx, my, () -> {
            c.enabled = !c.enabled;
            this.save();
        });
        this.bodyTop += 29;
        this.scrollbar(g, 330);
        int x = this.left + 14;
        int y = this.bodyTop - this.scroll;
        int lw = (this.panelWidth - 44) * 53 / 100;
        int rx = x + lw + 16;
        int rw = this.panelWidth - 42 - lw;
        this.gridSide = 0;
        g.method_44379(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            int cy;
            if (c.mode.equals("CUSTOM")) {
                this.text(g, CustomFont.regular().ellipsize(HudStyle.tr("\u041b\u041a\u041c \u2014 \u0440\u0438\u0441\u043e\u0432\u0430\u0442\u044c \u00b7 \u041f\u041a\u041c \u2014 \u0441\u0442\u0438\u0440\u0430\u0442\u044c", "Left: draw \u00b7 Right: erase"), 11.0f, lw), x, y + 2, -7105633);
                this.cell = Math.max(2, Math.min(8, (lw - 6) / c.gridSize));
                this.gridSide = this.cell * c.gridSize;
                this.gridX = x + (lw - this.gridSide) / 2;
                this.gridY = y + 22;
                g.method_25294(this.gridX - 1, this.gridY - 1, this.gridX + this.gridSide + 1, this.gridY + this.gridSide + 1, -10461076);
                for (int yy = 0; yy < c.gridSize; ++yy) {
                    for (int xx = 0; xx < c.gridSize; ++xx) {
                        int color = (c.pixels[yy] & 1 << xx) != 0 ? c.color : ((xx + yy) % 2 == 0 ? -14671834 : -13619144);
                        g.method_25294(this.gridX + xx * this.cell, this.gridY + yy * this.cell, this.gridX + (xx + 1) * this.cell - 1, this.gridY + (yy + 1) * this.cell - 1, color);
                    }
                }
                int gy = this.gridY + this.gridSide + 9;
                this.control(g, x, gy, lw / 2 - 3, HudStyle.tr("\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c", "Clear"), mx, my, () -> {
                    Arrays.fill(c.pixels, 0);
                    this.save();
                });
                this.control(g, x + lw / 2 + 3, gy, lw / 2 - 3, c.gridSize + " \u00d7 " + c.gridSize, mx, my, () -> {
                    CrosshairPattern.resize(c, c.gridSize == 31 ? 17 : (c.gridSize == 17 ? 25 : 31));
                    this.save();
                });
            } else if (c.mode.equals("PRESET")) {
                int w = (lw - 6) / 2;
                for (int i = 0; i < this.thumbnails.size(); ++i) {
                    int index = i;
                    int px = x + i % 2 * (w + 6);
                    int py = y + i / 2 * 39;
                    UiDraw.rounded(g, px, py, w, 36, 5, ThemeColors.surface(-13619143));
                    this.text(g, CustomFont.regular().ellipsize(HudStyle.tr(RU[i], EN[i]), 10.0f, w - 10), px + 5, py + 3, -1381908);
                    if (py >= this.bodyTop && py + 36 <= this.bodyBottom) {
                        this.actions.add(new ModuleDetailScreen.Action(px, py, w, 36, () -> {
                            c.preset = CrosshairPattern.PRESETS.get(index);
                            c.dot = false;
                            this.save();
                        }));
                    }
                    CrosshairRenderer.paint(g, this.thumbnails.get(i), px + w / 2, py + 29, 0.65, -1052684, false);
                    if (!c.preset.equals(CrosshairPattern.PRESETS.get(i))) continue;
                    UiDraw.outline(g, px, py, w, 36, 5, ThemeColors.accent());
                }
            } else {
                this.number(g, x, y, lw, HudStyle.tr("\u0414\u043b\u0438\u043d\u0430 \u043b\u0438\u043d\u0438\u0439", "Line length"), c.size, 1, 12, v -> {
                    c.size = v;
                }, mx, my);
                this.number(g, x, y + 36, lw, HudStyle.tr("\u0422\u043e\u043b\u0449\u0438\u043d\u0430", "Thickness"), c.thickness, 1, 6, v -> {
                    c.thickness = v;
                }, mx, my);
                this.number(g, x, y + 72, lw, HudStyle.tr("\u0417\u0430\u0437\u043e\u0440", "Gap"), c.gap, 0, 10, v -> {
                    c.gap = v;
                }, mx, my);
                this.control(g, x, y + 111, lw, HudStyle.tr("\u0422\u043e\u0447\u043a\u0430 \u0432 \u0446\u0435\u043d\u0442\u0440\u0435: ", "Center dot: ") + (c.dot ? HudStyle.tr("\u0434\u0430", "on") : HudStyle.tr("\u043d\u0435\u0442", "off")), mx, my, () -> {
                    c.dot = !c.dot;
                    this.save();
                });
                this.number(g, x, y + 144, lw, HudStyle.tr("\u0420\u0430\u0437\u043c\u0435\u0440 \u0442\u043e\u0447\u043a\u0438", "Dot size"), c.dotSize, 1, 6, v -> {
                    c.dotSize = v;
                }, mx, my);
                this.number(g, x, y + 180, lw, HudStyle.tr("\u041e\u0431\u0432\u043e\u0434\u043a\u0430", "Outline"), c.outline, 0, 3, v -> {
                    c.outline = v;
                }, mx, my);
                this.control(g, x, y + 222, lw, HudStyle.tr("\u0420\u0435\u0434\u0430\u043a\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u043f\u043e \u043f\u0438\u043a\u0441\u0435\u043b\u044f\u043c", "Edit as pixels"), mx, my, () -> {
                    CrosshairPattern.editCurrent(c);
                    this.save();
                });
                this.text(g, CustomFont.regular().ellipsize(HudStyle.tr("\u0426\u0432\u0435\u0442\u0430 \u0438 \u043c\u0430\u0441\u0448\u0442\u0430\u0431 \u2014 \u0432 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445 \u043c\u043e\u0434\u0443\u043b\u044f.", "Colors and scale are in module settings."), 11.0f, lw), x, y + 256, -7105633);
            }
            int ph = 146;
            this.previewBackground(g, rx, y, rw, ph);
            boolean inside = mx >= rx && mx < rx + rw && my >= Math.max(y, this.bodyTop) && my < Math.min(y + ph, this.bodyBottom);
            int cx = inside ? Math.clamp((long)mx, rx + 10, rx + rw - 10) : rx + rw / 2;
            int n = cy = inside ? Math.clamp((long)my, y + 10, y + ph - 10) : y + ph / 2;
            if (y + ph > this.bodyTop && y < this.bodyBottom) {
                g.method_44379(rx, Math.max(this.bodyTop, y), rx + rw, Math.min(this.bodyBottom, y + ph));
                CrosshairRenderer.draw(g, cx, cy, c, this.previewTarget, true);
                g.method_44380();
            }
            for (int i = 0; i < 4; ++i) {
                int value = i;
                int bw = (rw - 12) / 4;
                this.control(g, rx + i * (bw + 4), y + 153, bw, Integer.toString(i + 1), mx, my, () -> {
                    this.background = value;
                });
            }
            this.control(g, rx, y + 182, rw, HudStyle.tr("\u0426\u0435\u043b\u044c: ", "Target: ") + (this.previewTarget == 1 ? HudStyle.tr("\u0432\u0440\u0430\u0433", "enemy") : (this.previewTarget == 2 ? HudStyle.tr("\u0441\u043e\u044e\u0437\u043d\u0438\u043a", "friendly") : HudStyle.tr("\u043d\u0435\u0442", "none"))), mx, my, () -> {
                this.previewTarget = (this.previewTarget + 1) % 3;
            });
            this.control(g, rx, y + 211, rw, HudStyle.tr("\u0421\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u043a\u043e\u0434 \u0444\u043e\u0440\u043c\u044b", "Copy shape code"), mx, my, () -> {
                try {
                    this.field_22787.field_1774.method_1455(CrosshairPattern.encode(c));
                    this.message(HudStyle.tr("\u041a\u043e\u0434 BHCH-1 \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d", "BHCH-1 code copied"));
                }
                catch (IllegalArgumentException e) {
                    this.message(HudStyle.tr("\u0414\u043b\u044f \u043a\u043e\u0434\u0430 \u0443\u043c\u0435\u043d\u044c\u0448\u0438 \u043f\u0440\u0438\u0446\u0435\u043b \u0434\u043e 31 \u00d7 31", "Reduce the drawing to 31 x 31 to share"));
                }
            });
            this.control(g, rx, y + 240, rw, HudStyle.tr("\u0412\u0441\u0442\u0430\u0432\u0438\u0442\u044c \u043a\u043e\u0434 \u0444\u043e\u0440\u043c\u044b", "Paste shape code"), mx, my, () -> {
                try {
                    CrosshairPattern.decode(this.field_22787.field_1774.method_1460(), c);
                    this.save();
                    this.message(HudStyle.tr("\u041f\u0440\u0438\u0446\u0435\u043b \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d", "Crosshair imported"));
                }
                catch (IllegalArgumentException e) {
                    this.message(HudStyle.tr("\u041d\u0443\u0436\u0435\u043d \u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u044b\u0439 \u043a\u043e\u0434 BloodHelper BHCH-1", "A valid BloodHelper BHCH-1 code is required"));
                }
            });
            if (!c.mode.equals("CUSTOM")) {
                this.control(g, rx, y + 269, rw, HudStyle.tr("\u0418\u0437 \u044d\u0442\u043e\u0439 \u0444\u043e\u0440\u043c\u044b \u2014 \u0441\u0432\u043e\u0439 \u0440\u0438\u0441\u0443\u043d\u043e\u043a", "Make this shape editable"), mx, my, () -> {
                    CrosshairPattern.editCurrent(c);
                    this.save();
                });
            }
        }
        finally {
            g.method_44380();
        }
        this.footer(g, System.nanoTime() < this.noticeUntil ? this.notice : HudStyle.tr("\u0410\u0432\u0442\u043e\u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u0435 \u00b7 Delete \u2014 \u043e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u0440\u0438\u0441\u0443\u043d\u043e\u043a \u00b7 \u0446\u0432\u0435\u0442\u0430 \u0438 \u043c\u0430\u0441\u0448\u0442\u0430\u0431 \u0432 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445 \u043c\u043e\u0434\u0443\u043b\u044f", "Autosaved \u00b7 Delete clears drawing \u00b7 colors and scale in module settings"));
    }

    private void previewBackground(class_332 g, int x, int y, int w, int h) {
        int[] sky = new int[]{-8078878, -4932929, -12774119, -15656139};
        int[] ground = new int[]{-12095175, -11183263, -9493463, -14669274};
        g.method_25294(x, y, x + w, y + h, sky[this.background]);
        g.method_25294(x, y + h / 2, x + w, y + h, ground[this.background]);
        for (int yy = y + h / 2; yy < y + h; yy += 14) {
            for (int xx = x; xx < x + w; xx += 18) {
                if (((xx - x) / 18 + (yy - y) / 14) % 2 != 0) continue;
                g.method_25294(xx, yy, Math.min(xx + 17, x + w), Math.min(yy + 13, y + h), UiDraw.alpha(0xFFFFFF, 18));
            }
        }
    }

    private void control(class_332 g, int x, int y, int w, String label, int mx, int my, Runnable action) {
        if (y < this.bodyTop || y + 21 > this.bodyBottom) {
            return;
        }
        this.button(g, x, y, w, label, mx, my, action);
    }

    private void number(class_332 g, int x, int y, int w, String label, int value, int min, int max, IntConsumer setter, int mx, int my) {
        this.text(g, CustomFont.regular().ellipsize(label + ": " + value, 11.0f, Math.max(1, w - 65)), x, y + 7, -1381908);
        this.control(g, x + w - 58, y, 26, "\u2212", mx, my, () -> {
            setter.accept(Math.max(min, value - 1));
            this.save();
        });
        this.control(g, x + w - 28, y, 26, "+", mx, my, () -> {
            setter.accept(Math.min(max, value + 1));
            this.save();
        });
    }

    private boolean paint(double mx, double my) {
        CrosshairSettings c = this.config();
        if (!c.mode.equals("CUSTOM") || this.gridSide <= 0 || mx < (double)this.gridX || mx >= (double)(this.gridX + this.gridSide) || my < (double)this.gridY || my >= (double)(this.gridY + this.gridSide) || my < (double)this.bodyTop || my >= (double)this.bodyBottom) {
            this.lastY = -1;
            this.lastX = -1;
            return false;
        }
        int x = (int)(mx - (double)this.gridX) / this.cell;
        int y = (int)(my - (double)this.gridY) / this.cell;
        int fromX = this.lastX < 0 ? x : this.lastX;
        int fromY = this.lastY < 0 ? y : this.lastY;
        int steps = Math.max(Math.abs(x - fromX), Math.abs(y - fromY));
        for (int i = 0; i <= steps; ++i) {
            int px = steps == 0 ? x : fromX + (int)Math.round((double)((x - fromX) * i) / (double)steps);
            int py = steps == 0 ? y : fromY + (int)Math.round((double)((y - fromY) * i) / (double)steps);
            int before = c.pixels[py];
            c.pixels[py] = this.paintButton == 0 ? before | 1 << px : before & ~(1 << px);
            this.dirty |= before != c.pixels[py];
        }
        this.lastX = x;
        this.lastY = y;
        return true;
    }

    @Override
    protected boolean panelMouseClicked(class_11909 e, boolean doubled) {
        if (e.method_74245() == 0 || e.method_74245() == 1) {
            this.paintButton = e.method_74245();
            if (this.paint(e.comp_4798(), e.comp_4799())) {
                return true;
            }
            this.paintButton = -1;
        }
        return super.panelMouseClicked(e, doubled);
    }

    @Override
    protected boolean panelMouseDragged(class_11909 e, double dx, double dy) {
        if (this.paintButton >= 0) {
            this.paint(e.comp_4798(), e.comp_4799());
            return true;
        }
        return super.panelMouseDragged(e, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(class_11909 e) {
        if (this.paintButton >= 0) {
            this.finishPaint();
            return true;
        }
        return super.panelMouseReleased(e);
    }

    public boolean method_25404(class_11908 e) {
        if (e.comp_4795() == 261 && this.config().mode.equals("CUSTOM")) {
            this.finishPaint();
            Arrays.fill(this.config().pixels, 0);
            this.save();
            return true;
        }
        return super.method_25404(e);
    }

    public void method_25432() {
        this.finishPaint();
        super.method_25432();
    }

    @Override
    public void method_25419() {
        this.finishPaint();
        super.method_25419();
    }

    private void finishPaint() {
        this.paintButton = -1;
        this.lastY = -1;
        this.lastX = -1;
        if (this.dirty) {
            this.dirty = false;
            this.save();
        }
    }

    private void save() {
        BloodHelperClient.saveConfig();
    }

    private void message(String text) {
        this.notice = text;
        this.noticeUntil = System.nanoTime() + 4000000000L;
    }
}

