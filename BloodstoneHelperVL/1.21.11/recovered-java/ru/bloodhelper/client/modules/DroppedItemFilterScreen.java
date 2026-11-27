/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1844
 *  net.minecraft.class_1935
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_6880
 *  net.minecraft.class_6880$class_6883
 *  net.minecraft.class_7923
 *  net.minecraft.class_9334
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_1935;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_9334;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.DroppedItemFilterSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class DroppedItemFilterScreen
extends ModuleDetailScreen {
    private final GuiTextField search = new GuiTextField(0, 0, 100, 21, "");
    private final List<Row> catalog = new ArrayList<Row>();
    private List<Row> filtered = List.of();
    private String query;
    private Object translations;
    private String catalogLanguage;
    private boolean selectedOnly;
    private Set<String> selectedKeys = Set.of();
    private int buildIndex;
    private List<class_1799> pending;

    private void refreshSelection() {
        this.selectedKeys = this.settings().selected.stream().map(DroppedItemFilterSettings.Entry::key).collect(Collectors.toSet());
    }

    public DroppedItemFilterScreen(class_437 parent) {
        super(parent, "Dropped items");
        this.search.setMaxLength(100);
        this.search.setTextSize(11.0f);
        this.search.setResponder(v -> {
            this.query = null;
            this.scroll = 0;
        });
    }

    protected void method_25426() {
        this.pending = null;
        this.translations = null;
        this.refreshSelection();
    }

    public void method_25393() {
        Object token = UiLanguage.cacheToken();
        if (this.pending == null || this.translations != token || !Objects.equals(this.catalogLanguage, BloodHelperClient.config().language)) {
            this.translations = token;
            this.catalogLanguage = BloodHelperClient.config().language;
            this.beginCatalog();
        }
        int before = this.buildIndex;
        int limit = Math.min(this.pending.size(), this.buildIndex + 96);
        while (this.buildIndex < limit) {
            class_1799 stack = this.pending.get(this.buildIndex);
            String item = class_7923.field_41178.method_10221((Object)stack.method_7909()).toString();
            class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
            String potion = contents == null ? "" : contents.comp_2378().flatMap(h -> h.method_40230()).map(k -> k.method_29177().toString()).orElse("");
            Object name = UiLanguage.text(stack.method_7964());
            if (potion.isEmpty() && (stack.method_31574(class_1802.field_8574) || stack.method_31574(class_1802.field_8436) || stack.method_31574(class_1802.field_8150))) {
                name = (String)name + HudStyle.tr(" \u00b7 \u0432\u0441\u0435 \u0432\u0430\u0440\u0438\u0430\u043d\u0442\u044b", " \u00b7 all variants");
            }
            this.catalog.add(new Row(stack, item, potion, (String)name, ((String)name + " " + item + " " + potion).toLowerCase(Locale.ROOT)));
            ++this.buildIndex;
        }
        if (this.query == null || this.buildIndex != before) {
            this.rebuild();
        }
    }

    private void beginCatalog() {
        this.pending = new ArrayList<class_1799>();
        this.catalog.clear();
        this.buildIndex = 0;
        this.query = null;
        for (class_1792 item : class_7923.field_41178) {
            if (item == class_1802.field_8162) continue;
            this.pending.add(new class_1799((class_1935)item));
        }
        for (class_6880.class_6883 potion : class_7923.field_41179.method_42017().toList()) {
            for (class_1792 item : List.of(class_1802.field_8574, class_1802.field_8436, class_1802.field_8150, class_1802.field_8087)) {
                this.pending.add(class_1844.method_57400((class_1792)item, (class_6880)potion));
            }
        }
    }

    private DroppedItemFilterSettings settings() {
        return BloodHelperClient.config().optimizer.droppedItems;
    }

    private boolean selected(Row row) {
        return this.selectedKeys.contains(row.key());
    }

    private void rebuild() {
        this.query = this.search.getValue().strip().toLowerCase(Locale.ROOT);
        this.filtered = this.catalog.stream().filter(r -> !(this.selectedOnly && !this.selected((Row)r) || !this.query.isEmpty() && !r.search.contains(this.query))).toList();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u0423\u0431\u0438\u0440\u0430\u0442\u044c \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u043d\u0430 \u043f\u043e\u043b\u0443", "Hide selected dropped items"), mx, my);
        this.button(g, this.left + 14, this.bodyTop, 170, this.settings().enabled ? HudStyle.tr("\u0424\u0438\u043b\u044c\u0442\u0440 \u0432\u043a\u043b\u044e\u0447\u0451\u043d", "Filter enabled") : HudStyle.tr("\u0424\u0438\u043b\u044c\u0442\u0440 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d", "Filter disabled"), mx, my, () -> {
            this.settings().enabled = !this.settings().enabled;
            BloodHelperClient.saveConfig();
        });
        this.button(g, this.left + 192, this.bodyTop, 150, this.selectedOnly ? HudStyle.tr("\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0432\u0441\u0435", "Show all") : HudStyle.tr("\u0422\u043e\u043b\u044c\u043a\u043e \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0435", "Selected only"), mx, my, () -> {
            this.selectedOnly = !this.selectedOnly;
            this.query = null;
            this.scroll = 0;
        });
        this.button(g, this.left + this.panelWidth - 136, this.bodyTop, 122, HudStyle.tr("\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u0432\u044b\u0431\u043e\u0440", "Clear selection"), mx, my, () -> {
            this.settings().selected.clear();
            this.refreshSelection();
            BloodHelperClient.saveConfig();
            this.query = null;
        });
        this.bodyTop += 29;
        UiDraw.rounded(g, this.left + 14, this.bodyTop, this.panelWidth - 28, 24, 6, ThemeColors.surface(-14211282));
        this.search.setX(this.left + 21);
        this.search.setY(this.bodyTop + 2);
        this.search.setWidth(this.panelWidth - 42);
        this.search.setHeight(20);
        this.search.setHint(HudStyle.tr("\u041f\u043e\u0438\u0441\u043a \u043f\u043e \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u044e, ID \u0438\u043b\u0438 \u0437\u0435\u043b\u044c\u044e", "Search name, item ID or potion"));
        this.search.render(g, mx, my, delta);
        this.bodyTop += 31;
        if (this.query == null) {
            this.rebuild();
        }
        this.scrollbar(g, this.filtered.size() * 35);
        g.method_44379(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            for (int i = Math.max(0, this.scroll / 35); i < this.filtered.size(); ++i) {
                int y = this.bodyTop + i * 35 - this.scroll;
                if (y >= this.bodyBottom) {
                    break;
                }
                Row row = this.filtered.get(i);
                boolean selected = this.selected(row);
                g.method_51427(row.icon, this.left + 22, y + 6);
                this.text(g, CustomFont.regular().ellipsize(row.name, 11.0f, this.panelWidth - 164), this.left + 46, y + 3, -1381908);
                CustomFont.regular().draw(g, CustomFont.regular().ellipsize(row.potionId.isEmpty() ? row.itemId : row.potionId, 8.0f, this.panelWidth - 164), this.left + 46, y + 18, 8.0f, -7105633);
                if (y < this.bodyTop || y + 27 > this.bodyBottom) continue;
                this.button(g, this.left + this.panelWidth - 110, y + 3, 89, selected ? HudStyle.tr("\u0421\u043a\u0440\u044b\u0432\u0430\u0435\u0442\u0441\u044f", "Hidden") : HudStyle.tr("\u0421\u043a\u0440\u044b\u0432\u0430\u0442\u044c", "Hide"), mx, my, () -> {
                    if (selected) {
                        this.settings().selected.removeIf(e -> e.key().equals(row.key()));
                    } else if (this.settings().selected.size() < 4096) {
                        this.settings().selected.add(new DroppedItemFilterSettings.Entry(row.itemId, row.potionId));
                    }
                    this.refreshSelection();
                    BloodHelperClient.saveConfig();
                    this.query = null;
                });
            }
        }
        finally {
            g.method_44380();
        }
        this.footer(g, (this.pending != null && this.buildIndex < this.pending.size() ? HudStyle.tr("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u043a\u0430\u0442\u0430\u043b\u043e\u0433\u0430\u2026 \u00b7 ", "Loading catalog\u2026 \u00b7 ") : "") + HudStyle.tr("\u0412\u044b\u0431\u0440\u0430\u043d\u043e: ", "Selected: ") + this.settings().selected.size() + (BloodHelperClient.config().optimizer.enabled ? "" : HudStyle.tr(" \u00b7 Optimizer \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d", " \u00b7 Optimizer is off")));
    }

    @Override
    protected boolean panelMouseClicked(class_11909 e, boolean doubled) {
        return this.search.mouseClicked(e, doubled) || super.panelMouseClicked(e, doubled);
    }

    @Override
    protected boolean panelMouseDragged(class_11909 e, double dx, double dy) {
        return this.search.mouseDragged(e, dx, dy) || super.panelMouseDragged(e, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(class_11909 e) {
        return this.search.mouseReleased(e) || super.panelMouseReleased(e);
    }

    public boolean method_25404(class_11908 e) {
        return this.search.keyPressed(e) || super.method_25404(e);
    }

    public boolean method_25400(class_11905 e) {
        return this.search.charTyped(e) || super.method_25400(e);
    }

    private record Row(class_1799 icon, String itemId, String potionId, String name, String search) {
        String key() {
            return this.itemId + "\u0000" + this.potionId;
        }
    }
}

