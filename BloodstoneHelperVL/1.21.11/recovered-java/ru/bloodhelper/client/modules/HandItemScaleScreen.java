/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_1661
 *  net.minecraft.class_1799
 *  net.minecraft.class_1935
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_7923
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_7923;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.DropGuardSettings;
import ru.bloodhelper.client.config.LegendarySizeSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.DropGuardModule;
import ru.bloodhelper.client.modules.LegendarySizeModule;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class HandItemScaleScreen
extends ModuleDetailScreen {
    private final GuiTextField search = new GuiTextField(0, 0, 100, 21, "");
    private boolean inventory;
    private boolean ground;
    private static final int ROW_HEIGHT = 79;
    private List<Row> cachedRows;
    private int refreshTicks;
    private final List<Knob> knobs = new ArrayList<Knob>();
    private Knob dragging;

    public HandItemScaleScreen(class_437 parent) {
        super(parent, "Item size");
        this.search.setMaxLength(80);
        this.search.setTextSize(11.0f);
        this.search.setResponder(value -> {
            this.finishDrag();
            this.scroll = 0;
        });
    }

    @Override
    protected int preferredPanelHeight() {
        return HandItemScaleScreen.listPanelHeight(136, this.rows().size(), 79, 74);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u0420\u0430\u0437\u043c\u0435\u0440 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u00b7 \u0432\u044b\u0431\u043e\u0440 \u0438 \u043f\u0440\u043e\u0446\u0435\u043d\u0442\u044b", "Item size \u00b7 selection and percentages"), mx, my);
        int tabWidth = Math.max(65, Math.min(150, (this.panelWidth - 38) / 3));
        this.button(g, this.left + 14, this.bodyTop, tabWidth, HudStyle.tr("\u0412\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0435", "Selected"), mx, my, () -> this.tab(false));
        this.button(g, this.left + 20 + tabWidth, this.bodyTop, tabWidth, HudStyle.tr("\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c", "Inventory"), mx, my, () -> this.tab(true));
        this.button(g, this.left + this.panelWidth - tabWidth - 14, this.bodyTop, tabWidth, HudStyle.tr("\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u0440\u0443\u043a\u0435 +", "Add held item"), mx, my, () -> {
            if (this.field_22787.field_1724 != null) {
                LegendarySizeModule.addSelected(this.field_22787.field_1724.method_6047());
            }
            this.tab(false);
        });
        UiDraw.outline(g, this.left + 14 + (this.inventory ? tabWidth + 6 : 0), this.bodyTop, tabWidth, 21, 6, ThemeColors.accent());
        this.bodyTop += 27;
        UiDraw.rounded(g, this.left + 14, this.bodyTop, this.panelWidth - 28, 23, 6, ThemeColors.surface(-14211282));
        this.search.setX(this.left + 21);
        this.search.setY(this.bodyTop + 1);
        this.search.setWidth(this.panelWidth - 42);
        this.search.setHeight(21);
        this.search.setHint(HudStyle.tr("\u041f\u043e\u0438\u0441\u043a \u043f\u043e \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u044e \u0438\u043b\u0438 ID", "Search name or item ID"));
        this.search.render(g, mx, my, delta);
        this.bodyTop += 30;
        this.knobs.clear();
        this.button(g, this.left + 14, this.bodyTop, Math.max(60, (this.panelWidth - 34) / 2), HudStyle.tr("\u0412 \u0440\u0443\u043a\u0430\u0445", "In hands"), mx, my, () -> {
            this.finishDrag();
            this.ground = false;
        });
        this.button(g, this.left + 20 + (this.panelWidth - 34) / 2, this.bodyTop, Math.max(60, (this.panelWidth - 34) / 2), HudStyle.tr("\u041d\u0430 \u0437\u0435\u043c\u043b\u0435", "Dropped items"), mx, my, () -> {
            this.finishDrag();
            this.ground = true;
        });
        UiDraw.outline(g, this.left + 14 + (this.ground ? 6 + (this.panelWidth - 34) / 2 : 0), this.bodyTop, Math.max(60, (this.panelWidth - 34) / 2), 21, 6, ThemeColors.accent());
        this.bodyTop += 29;
        String query = this.search.getValue().strip().toLowerCase(Locale.ROOT);
        List<Row> rows = this.rows().stream().filter(r -> query.isEmpty() || r.entry.name.toLowerCase(Locale.ROOT).contains(query) || r.entry.itemId.contains(query)).toList();
        this.scrollbar(g, Math.max(50, rows.size() * 79));
        LegendarySizeSettings settings = BloodHelperClient.config().legendarySize;
        g.method_44379(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            if (rows.isEmpty()) {
                int y = this.bodyTop + 8;
                String message = this.field_22787.field_1724 == null ? HudStyle.tr("\u041e\u0442\u043a\u0440\u043e\u0439\u0442\u0435 \u043c\u0438\u0440, \u0447\u0442\u043e\u0431\u044b \u0432\u044b\u0431\u0440\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0438\u0437 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f.", "Join a world to select inventory items.") : HudStyle.tr("\u0414\u043e\u0431\u0430\u0432\u044c\u0442\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0438\u0437 \u0432\u043a\u043b\u0430\u0434\u043a\u0438 \u00ab\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c\u00bb \u0438\u043b\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u0440\u0443\u043a\u0435.", "Add an item from Inventory or your held item.");
                for (String line : HandItemScaleScreen.wrap(message, this.panelWidth - 40)) {
                    this.text(g, line, this.left + 20, y, -7105633);
                    y += 15;
                }
            }
            for (int i = Math.max(0, this.scroll / 79); i < rows.size(); ++i) {
                int y = this.bodyTop + i * 79 - this.scroll;
                if (y >= this.bodyBottom) {
                    break;
                }
                Row row = rows.get(i);
                LegendarySizeSettings.SelectedItem selected = settings.selected(row.entry.key());
                UiDraw.rounded(g, this.left + 14, y, this.panelWidth - 28, 74, 7, ThemeColors.surface(-1339677139));
                g.method_51427(row.icon, this.left + 23, y + 9);
                this.text(g, CustomFont.regular().ellipsize(row.entry.name, 11.0f, this.panelWidth - 170), this.left + 47, y + 7, -1381908);
                if (selected != null) {
                    int x = this.left + 49;
                    int width = Math.max(25, this.panelWidth - 139);
                    int percent = this.ground ? selected.droppedPercent : selected.percent;
                    float fraction = (float)(Math.clamp((long)percent, 10, 500) - 10) / 490.0f;
                    if (y >= this.bodyTop && y + 49 <= this.bodyBottom) {
                        this.button(g, this.left + 47, y + 25, Math.min(185, this.panelWidth - 135), (this.ground ? selected.droppedEnabled : selected.handEnabled) ? HudStyle.tr("\u041c\u0430\u0441\u0448\u0442\u0430\u0431 \u0432\u043a\u043b\u044e\u0447\u0451\u043d", "Scale enabled") : HudStyle.tr("\u041c\u0430\u0441\u0448\u0442\u0430\u0431 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d", "Scale disabled"), mx, my, () -> {
                            this.finishDrag();
                            if (this.ground) {
                                selected.droppedEnabled = !selected.droppedEnabled;
                            } else {
                                selected.handEnabled = !selected.handEnabled;
                            }
                            BloodHelperClient.saveConfig();
                        });
                    }
                    UiDraw.rounded(g, x, y + 59, width, 3, 1, ThemeColors.surface(-11184801));
                    UiDraw.rounded(g, x, y + 59, Math.round((float)width * fraction), 3, 1, ThemeColors.accent());
                    UiDraw.rounded(g, x + Math.round((float)width * fraction) - 4, y + 56, 8, 9, 4, -1381908);
                    this.text(g, percent + "%", this.left + this.panelWidth - 72, y + 54, -1381908);
                    this.knobs.add(new Knob(row.entry.key(), x, width, Math.max(this.bodyTop, y + 51), Math.min(this.bodyBottom, y + 72)));
                } else {
                    CustomFont.regular().draw(g, CustomFont.regular().ellipsize(row.entry.itemId, 9.0f, this.panelWidth - 75), this.left + 47, y + 29, 9.0f, -7105633);
                }
                if (y < this.bodyTop || y + 28 > this.bodyBottom) continue;
                this.button(g, this.left + this.panelWidth - 103, y + 3, 80, selected == null ? HudStyle.tr("\u0412\u044b\u0431\u0440\u0430\u0442\u044c", "Select") : HudStyle.tr("\u0423\u0431\u0440\u0430\u0442\u044c", "Remove"), mx, my, () -> {
                    this.finishDrag();
                    if (selected == null) {
                        LegendarySizeModule.addSelected(row.icon);
                    } else {
                        settings.selectedItems.removeIf(e -> e.item.key().equals(row.entry.key()));
                        BloodHelperClient.saveConfig();
                    }
                    this.cachedRows = null;
                });
            }
        }
        finally {
            g.method_44380();
        }
        this.footer(g, settings.enabled ? HudStyle.tr("10\u2013500% \u00b7 \u0440\u0443\u043a\u0438 \u0438 \u0437\u0435\u043c\u043b\u044f \u043d\u0435\u0437\u0430\u0432\u0438\u0441\u0438\u043c\u044b \u00b7 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0439 \u043c\u0430\u0441\u0448\u0442\u0430\u0431 \u0432\u0430\u0436\u043d\u0435\u0435 \u043e\u0431\u0449\u0435\u0433\u043e", "10\u2013500% \u00b7 hands and ground independent \u00b7 selected scales override global scale") : HudStyle.tr("\u041c\u043e\u0434\u0443\u043b\u044c \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d \u00b7 \u0432\u043a\u043b\u044e\u0447\u0438\u0442\u0435 \u00ab\u0420\u0430\u0437\u043c\u0435\u0440 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432\u00bb \u0432 \u043c\u0435\u043d\u044e", "Module off \u00b7 enable Item size in the menu"));
    }

    private void tab(boolean value) {
        this.finishDrag();
        this.inventory = value;
        this.scroll = 0;
        this.cachedRows = null;
    }

    public void method_25393() {
        if (++this.refreshTicks >= 10) {
            this.refreshTicks = 0;
            this.cachedRows = null;
        }
    }

    private List<Row> rows() {
        if (this.cachedRows == null) {
            this.cachedRows = this.collectRows();
        }
        return this.cachedRows;
    }

    private List<Row> collectRows() {
        DropGuardSettings.Entry e;
        LinkedHashMap<String, Row> current = new LinkedHashMap<String, Row>();
        if (this.field_22787 != null && this.field_22787.field_1724 != null) {
            class_1661 items = this.field_22787.field_1724.method_31548();
            for (int slot = 0; slot < items.method_5439(); ++slot) {
                class_1799 stack = items.method_5438(slot);
                if (stack.method_7960()) continue;
                e = DropGuardModule.capture(stack);
                current.putIfAbsent(e.key(), new Row(e, stack.method_7972()));
            }
        }
        if (this.inventory) {
            return new ArrayList<Row>(current.values());
        }
        ArrayList<Row> result = new ArrayList<Row>();
        for (LegendarySizeSettings.SelectedItem selected : BloodHelperClient.config().legendarySize.selectedItems) {
            e = selected.item;
            Row actual = (Row)current.get(e.key());
            class_2960 id = class_2960.method_12829((String)e.itemId);
            class_1799 fallback = id == null ? class_1799.field_8037 : new class_1799((class_1935)class_7923.field_41178.method_63535(id));
            result.add(actual == null ? new Row(e, fallback) : new Row(e, actual.icon));
        }
        return result;
    }

    private void change(double x) {
        if (this.dragging == null) {
            return;
        }
        LegendarySizeSettings.SelectedItem selected = BloodHelperClient.config().legendarySize.selected(this.dragging.key);
        if (selected != null) {
            int percent = (int)Math.round(10.0 + 490.0 * Math.clamp((x - (double)this.dragging.x) / (double)this.dragging.width, 0.0, 1.0));
            if (this.ground) {
                selected.droppedPercent = percent;
            } else {
                selected.percent = percent;
            }
        }
    }

    private void finishDrag() {
        if (this.dragging != null) {
            this.dragging = null;
            BloodHelperClient.saveConfig();
        }
    }

    @Override
    protected boolean panelMouseClicked(class_11909 e, boolean doubled) {
        if (this.search.mouseClicked(e, doubled)) {
            return true;
        }
        if (e.method_74245() == 0) {
            for (Knob knob : this.knobs) {
                if (!(e.comp_4798() >= (double)(knob.x - 5)) || !(e.comp_4798() <= (double)(knob.x + knob.width + 5)) || !(e.comp_4799() >= (double)knob.top) || !(e.comp_4799() < (double)knob.bottom)) continue;
                this.dragging = knob;
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
        return this.search.mouseDragged(e, dx, dy) || super.panelMouseDragged(e, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(class_11909 e) {
        if (this.dragging != null) {
            this.finishDrag();
            return true;
        }
        return this.search.mouseReleased(e) || super.panelMouseReleased(e);
    }

    public void method_25432() {
        this.finishDrag();
    }

    public boolean method_25404(class_11908 e) {
        return this.search.keyPressed(e) || super.method_25404(e);
    }

    public boolean method_25400(class_11905 e) {
        return this.search.charTyped(e) || super.method_25400(e);
    }

    private record Row(DropGuardSettings.Entry entry, class_1799 icon) {
    }

    private record Knob(String key, int x, int width, int top, int bottom) {
    }
}

