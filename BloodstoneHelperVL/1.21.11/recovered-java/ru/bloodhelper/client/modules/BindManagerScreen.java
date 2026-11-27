/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.BindManagerModel;
import ru.bloodhelper.client.modules.WaypointFrame;

public final class BindManagerScreen
extends WaypointFrame {
    private final GuiTextField search = new GuiTextField(0, 0, 200, 25, "");
    private final Map<String, class_304> mappings = new LinkedHashMap<String, class_304>();
    private List<BindManagerModel.Binding> all = List.of();
    private List<BindManagerModel.Binding> visible = List.of();
    private Map<String, List<String>> conflicts = Map.of();
    private Map<String, String> conflictLabels = Map.of();
    private BindManagerModel.Filter filter = BindManagerModel.Filter.BLOODHELPER;
    private class_304 capturing;
    private Object languageToken;
    private String language = "";
    private String status = "";
    private int pollTicks;
    private boolean suppressCharacter;
    private class_3675.class_306 capturedPress;

    public BindManagerScreen(class_437 parent) {
        super(parent, HudStyle.tr("\u0411\u0438\u043d\u0434\u044b", "Bind Manager"));
        this.search.setMaxLength(96);
        this.search.setTextSize(11.0f);
        this.search.setFocused(true);
        this.search.setResponder(value -> {
            this.scroll = 0;
            this.applyFilter();
            this.status = "";
        });
    }

    public static void open(class_437 parent) {
        class_310.method_1551().method_1507((class_437)new BindManagerScreen(parent));
    }

    public boolean isCapturing() {
        return this.capturing != null;
    }

    @Override
    protected int preferredPanelWidth() {
        return 620;
    }

    @Override
    protected int preferredPanelHeight() {
        return BindManagerScreen.listPanelHeight(130, this.all.size(), 46, 64);
    }

    protected void method_25426() {
        this.drainInputs();
        this.rebuild();
    }

    public void method_25393() {
        if (++this.pollTicks % 10 == 0 && this.changed()) {
            this.rebuild();
        }
    }

    private boolean changed() {
        if (!this.language.equals(BloodHelperClient.config().language) || this.languageToken != UiLanguage.cacheToken() || this.field_22787.field_1690.field_1839.length != this.mappings.size()) {
            return true;
        }
        for (BindManagerModel.Binding row : this.all) {
            class_304 mapping = this.mappings.get(row.id());
            if (mapping != null && mapping.method_1428().equals(row.key())) continue;
            return true;
        }
        return false;
    }

    private void rebuild() {
        this.language = BloodHelperClient.config().language;
        this.languageToken = UiLanguage.cacheToken();
        this.mappings.clear();
        ArrayList<BindManagerModel.Binding> rows = new ArrayList<BindManagerModel.Binding>();
        for (class_304 mapping : this.field_22787.field_1690.field_1839) {
            if (mapping == null || this.mappings.putIfAbsent(mapping.method_1431(), mapping) != null) continue;
            rows.add(new BindManagerModel.Binding(mapping.method_1431(), UiLanguage.text((class_2561)class_2561.method_43471((String)mapping.method_1431())), UiLanguage.text(mapping.method_1423().method_74697()), mapping.method_1428(), mapping.method_1429().method_1441(), this.keyLabel(mapping), mapping.method_1431().startsWith("key.bloodhelper.") || mapping.method_1423().comp_4879().method_12836().equals("bloodhelper"), mapping.method_1415()));
        }
        rows.sort(Comparator.comparing(row -> !row.bloodhelper()).thenComparing(BindManagerModel.Binding::category, String.CASE_INSENSITIVE_ORDER).thenComparing(BindManagerModel.Binding::label, String.CASE_INSENSITIVE_ORDER));
        this.all = List.copyOf(rows);
        this.conflicts = BindManagerModel.conflicts(this.all);
        LinkedHashMap<String, String> labels = new LinkedHashMap<String, String>();
        for (Map.Entry<String, List<String>> entry : this.conflicts.entrySet()) {
            labels.put(entry.getKey(), entry.getValue().stream().map(id -> UiLanguage.text((class_2561)class_2561.method_43471((String)id))).collect(Collectors.joining(", ")));
        }
        this.conflictLabels = Map.copyOf(labels);
        if (this.capturing != null && !this.mappings.containsValue(this.capturing)) {
            this.capturing = null;
        }
        this.applyFilter();
    }

    private void applyFilter() {
        this.visible = BindManagerModel.filter(this.all, this.conflicts, this.filter, this.search.getValue());
    }

    private void selectFilter(BindManagerModel.Filter next) {
        this.filter = next;
        this.scroll = 0;
        this.applyFilter();
    }

    private String keyLabel(class_304 mapping) {
        if (mapping.method_1415()) {
            return "None";
        }
        String key = UiLanguage.text(mapping.method_16007());
        if (BindManagerModel.debugChord(mapping.method_1431())) {
            class_304 modifier = this.field_22787.field_1690.field_63471;
            return (modifier.method_1415() ? "None" : UiLanguage.text(modifier.method_16007())) + " + " + key;
        }
        return key;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        String hint;
        this.frame(g, HudStyle.tr("\u0411\u0438\u043d\u0434\u044b", "Bind Manager"), mx, my);
        int fieldWidth = Math.max(60, this.panelWidth - 28);
        UiDraw.border(g, this.left + 14, this.top + 44, fieldWidth, 27, 5, ThemeColors.surface(-15395556), this.search.isFocused() ? ThemeColors.accent() : ThemeColors.surface(-8947833));
        this.search.setX(this.left + 21);
        this.search.setY(this.top + 45);
        this.search.setWidth(fieldWidth - 14);
        this.search.setHeight(25);
        this.search.setHint(HudStyle.tr("\u041f\u043e\u0438\u0441\u043a \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f \u0438\u043b\u0438 \u043a\u043b\u0430\u0432\u0438\u0448\u0438", "Search actions or keys"));
        this.search.render(g, mx, my, delta);
        int tabWidth = Math.max(55, Math.min(155, (this.panelWidth - 40) / 3));
        String[] labels = new String[]{"BloodstoneHelperVL", HudStyle.tr("\u0412\u0441\u0435", "All"), HudStyle.tr("\u041a\u043e\u043d\u0444\u043b\u0438\u043a\u0442\u044b", "Conflicts") + " \u00b7 " + this.conflicts.size()};
        BindManagerModel.Filter[] filters = BindManagerModel.Filter.values();
        for (int i = 0; i < filters.length; ++i) {
            BindManagerModel.Filter selected = filters[i];
            int tabX = this.left + 14 + i * (tabWidth + 6);
            this.button(g, tabX, this.top + 79, tabWidth, labels[i], mx, my, () -> this.selectFilter(selected));
            if (this.filter != selected) continue;
            UiDraw.rounded(g, tabX + 6, this.top + 101, tabWidth - 12, 2, 1, ThemeColors.accent());
        }
        String string = hint = this.capturing == null ? this.status : HudStyle.tr("\u041d\u0430\u0436\u043c\u0438 \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u0438\u043b\u0438 \u043a\u043d\u043e\u043f\u043a\u0443 \u043c\u044b\u0448\u0438 \u00b7 Esc \u2014 \u043e\u0442\u043c\u0435\u043d\u0430", "Press a key or mouse button \u00b7 Esc cancels");
        if (!hint.isEmpty()) {
            CustomFont.regular().draw(g, CustomFont.regular().ellipsize(hint, 10.0f, this.panelWidth - 28), this.left + 14, this.top + 111, 10.0f, -7105633);
        }
        this.bodyTop = this.top + 130;
        int rowHeight = 46;
        int contentHeight = this.visible.size() * rowHeight;
        this.maximumScroll = Math.max(0, contentHeight - Math.max(1, this.bodyBottom - this.bodyTop));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        String hoveredConflict = null;
        g.method_44379(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            if (this.visible.isEmpty()) {
                this.text(g, this.filter == BindManagerModel.Filter.CONFLICTS && this.search.getValue().isBlank() ? HudStyle.tr("\u041a\u043e\u043d\u0444\u043b\u0438\u043a\u0442\u043e\u0432 \u043d\u0435\u0442", "No conflicts") : HudStyle.tr("\u041d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e", "No matches"), this.left + 20, this.bodyTop + 15, -7105633);
            }
            for (int i = Math.max(0, this.scroll / rowHeight); i < this.visible.size(); ++i) {
                int y = this.bodyTop + i * rowHeight - this.scroll;
                if (y >= this.bodyBottom) {
                    break;
                }
                BindManagerModel.Binding row = this.visible.get(i);
                class_304 mapping = this.mappings.get(row.id());
                boolean collision = this.conflicts.containsKey(row.id());
                UiDraw.rounded(g, this.left + 14, y, this.panelWidth - 28, 41, 5, ThemeColors.surface(-14342868));
                if (this.capturing == mapping) {
                    UiDraw.outline(g, this.left + 14, y, this.panelWidth - 28, 41, 5, ThemeColors.accent());
                }
                int keyWidth = Math.max(48, Math.min(112, this.panelWidth / 5));
                int resetWidth = Math.max(38, Math.min(60, this.panelWidth / 10));
                int clearWidth = 39;
                int gap = 5;
                int resetX = this.left + this.panelWidth - 23 - resetWidth;
                int clearX = resetX - gap - clearWidth;
                int keyX = clearX - gap - keyWidth;
                int textWidth = Math.max(8, keyX - this.left - 33);
                CustomFont.medium().draw(g, CustomFont.medium().ellipsize(row.label(), 11.0f, textWidth), this.left + 23, y + 6, 11.0f, -1381908);
                String subtitle = collision ? HudStyle.tr("\u0421\u043e\u0432\u043f\u0430\u0434\u0430\u0435\u0442: ", "Also: ") + this.conflictLabels.get(row.id()) : row.category();
                CustomFont.regular().draw(g, CustomFont.regular().ellipsize(subtitle, 9.0f, textWidth), this.left + 23, y + 24, 9.0f, -7105633);
                if (collision) {
                    UiDraw.rounded(g, this.left + 15, y + 8, 2, 25, 1, ThemeColors.accent());
                }
                if (y + 10 >= this.bodyTop && y + 31 <= this.bodyBottom) {
                    this.button(g, keyX, y + 10, keyWidth, this.capturing == mapping ? HudStyle.tr("\u0416\u0434\u0443\u2026", "Waiting\u2026") : row.keyLabel(), mx, my, () -> this.beginCapture(mapping));
                    this.button(g, clearX, y + 10, clearWidth, "None", mx, my, () -> this.assign(mapping, class_3675.field_16237));
                    if (!mapping.method_1427()) {
                        this.button(g, resetX, y + 10, resetWidth, HudStyle.tr("\u0421\u0431\u0440\u043e\u0441", "Reset"), mx, my, () -> this.assign(mapping, mapping.method_1429()));
                    }
                }
                if (!collision || mx < this.left + 14 || mx >= this.left + this.panelWidth - 14 || my < Math.max(this.bodyTop, y) || my >= Math.min(this.bodyBottom, y + 41)) continue;
                hoveredConflict = row.label() + " \u00b7 " + HudStyle.tr("\u0421\u043e\u0432\u043f\u0430\u0434\u0430\u0435\u0442: ", "Also: ") + this.conflictLabels.get(row.id());
            }
        }
        finally {
            g.method_44380();
        }
        this.scrollbar(g, contentHeight);
        this.footer(g, this.capturing == null ? HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u044f\u0435\u0442\u0441\u044f \u0432 \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0438 Minecraft", "Saved in Minecraft Controls") : HudStyle.tr("Delete / Backspace \u2014 \u0443\u0431\u0440\u0430\u0442\u044c \u0431\u0438\u043d\u0434", "Delete / Backspace clears the bind"));
        if (hoveredConflict != null && this.capturing == null) {
            this.tooltip(g, hoveredConflict, mx, my);
        }
    }

    private void tooltip(class_332 g, String label, int mx, int my) {
        int maxWidth = Math.max(30, Math.min(360, this.field_22789 - 28));
        List<String> lines = CustomFont.regular().wrap(label, 10.0f, maxWidth - 18);
        int w = 20;
        for (String line : lines) {
            w = Math.max(w, (int)Math.ceil(CustomFont.regular().width(line, 10.0f)) + 18);
        }
        int h = lines.size() * 13 + 14;
        int x = Math.clamp((long)(mx + 10), 8, Math.max(8, this.field_22789 - w - 8));
        int y = Math.max(8, Math.min(my + 14, this.field_22790 - h - 8));
        UiDraw.border(g, x, y, w, h, 6, ThemeColors.surface(-14540247), ThemeColors.surface(-8947833));
        for (int i = 0; i < lines.size(); ++i) {
            CustomFont.regular().draw(g, lines.get(i), x + 9, y + 7 + i * 13, 10.0f, -1381908);
        }
    }

    private void beginCapture(class_304 mapping) {
        this.capturing = mapping;
        this.status = "";
        this.search.setFocused(false);
        this.suppressCharacter = false;
        this.drainInputs();
    }

    private void assign(class_304 mapping, class_3675.class_306 key) {
        if (mapping == null) {
            return;
        }
        mapping.method_1422(key);
        class_304.method_1426();
        this.capturing = null;
        this.suppressCharacter = true;
        this.drainInputs();
        this.field_22787.field_1690.method_1640();
        this.status = UiLanguage.text((class_2561)class_2561.method_43471((String)mapping.method_1431())) + " \u00b7 " + (mapping.method_1415() ? "None" : UiLanguage.text(mapping.method_16007()));
        this.rebuild();
    }

    public static boolean captureKeyboard(class_310 client, long window, int action, class_11908 event) {
        class_437 class_4372;
        if (window != client.method_22683().method_4490() || !((class_4372 = client.field_1755) instanceof BindManagerScreen)) {
            return false;
        }
        BindManagerScreen screen = (BindManagerScreen)class_4372;
        class_3675.class_306 physical = class_3675.method_15985((class_11908)event);
        if (screen.capturedPress != null && screen.capturedPress.equals((Object)physical) && action != 1) {
            if (action == 0) {
                screen.capturedPress = null;
            }
            return true;
        }
        if (!screen.isCapturing()) {
            return false;
        }
        if (action == 1) {
            screen.capturedPress = physical;
            screen.method_25404(event);
        }
        return true;
    }

    public boolean method_25404(class_11908 event) {
        if (this.capturing != null) {
            if (event.comp_4795() == 256) {
                this.capturing = null;
                this.status = "";
                this.drainInputs();
            } else {
                this.assign(this.capturing, event.comp_4795() == 261 || event.comp_4795() == 259 ? class_3675.field_16237 : class_3675.method_15985((class_11908)event));
            }
            return true;
        }
        this.suppressCharacter = false;
        if (this.search.keyPressed(event)) {
            return true;
        }
        return super.method_25404(event);
    }

    public boolean method_25400(class_11905 event) {
        return this.suppressCharacter || this.capturing != null || this.search.charTyped(event) || super.method_25400(event);
    }

    @Override
    protected boolean panelMouseClicked(class_11909 event, boolean doubled) {
        if (this.capturing != null) {
            this.assign(this.capturing, class_3675.class_307.field_1672.method_1447(event.method_74245()));
            return true;
        }
        this.suppressCharacter = false;
        if (this.search.mouseClicked(event, doubled)) {
            return true;
        }
        return super.panelMouseClicked(event, doubled);
    }

    @Override
    protected boolean panelMouseDragged(class_11909 event, double dx, double dy) {
        return this.capturing != null || this.search.mouseDragged(event, dx, dy) || super.panelMouseDragged(event, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(class_11909 event) {
        return this.search.mouseReleased(event) || super.panelMouseReleased(event);
    }

    @Override
    protected boolean panelMouseScrolled(double x, double y, double horizontal, double vertical) {
        return this.capturing != null || super.panelMouseScrolled(x, y, horizontal, vertical);
    }

    public void method_25432() {
        this.capturing = null;
        this.capturedPress = null;
        this.drainInputs();
        super.method_25432();
    }

    @Override
    public void method_25419() {
        this.capturing = null;
        this.drainInputs();
        super.method_25419();
    }

    private void drainInputs() {
        if (this.field_22787.field_1690 == null) {
            return;
        }
        for (class_304 mapping : this.field_22787.field_1690.field_1839) {
            mapping.method_23481(false);
            while (mapping.method_1436()) {
            }
        }
    }
}

