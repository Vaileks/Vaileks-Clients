/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_1799
 *  net.minecraft.class_1935
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_7923
 */
package ru.bloodhelper.client.gui;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_7923;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.HudInteraction;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.HudWidgets;
import ru.bloodhelper.client.modules.CombatHudModule;

public final class HudEditorScreen
extends class_437 {
    private final class_437 parent;
    private final boolean combatLayout;
    private final HudInteraction interaction = new HudInteraction();
    private HudRenderer.Bounds toolbar;
    private HudRenderer.Bounds done;
    private HudRenderer.Bounds adaptiveButton;
    private HudRenderer.Bounds guidesButton;
    private HudRenderer.Bounds recoverButton;
    private Map<String, HudRenderer.Bounds> toolbarLayout = Map.of();
    private HudRenderer.Bounds cachedToolbar;
    private int toolbarScreenWidth;
    private int toolbarScreenHeight;

    public HudEditorScreen(class_437 parent) {
        this(parent, false);
    }

    public HudEditorScreen(class_437 parent, boolean combatLayout) {
        super((class_2561)class_2561.method_43470((String)("BLOODHELPER \u2022 " + (combatLayout ? "Combat HUD" : HudStyle.tr("\u0420\u0435\u0434\u0430\u043a\u0442\u043e\u0440 HUD", "HUD Editor")))));
        this.parent = parent;
        this.combatLayout = combatLayout;
    }

    public boolean combatLayout() {
        return this.combatLayout;
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25420(class_332 g, int mouseX, int mouseY, float delta) {
        if (this.field_22787.field_1687 == null) {
            super.method_25420(g, mouseX, mouseY, delta);
        } else {
            this.field_22787.field_1705.method_72738();
        }
    }

    public void method_25394(class_332 g, int mouseX, int mouseY, float delta) {
        g.method_25294(0, 0, this.field_22789, this.field_22790, 1611401482);
        for (int x = this.field_22789 / 2 % 40; x < this.field_22789; x += 40) {
            g.method_25294(x, 0, x + 1, this.field_22790, 0x7FFFFFF);
        }
        for (int y = this.field_22790 / 2 % 40; y < this.field_22790; y += 40) {
            g.method_25294(0, y, this.field_22789, y + 1, 0x7FFFFFF);
        }
        HudRenderer.renderCombatPreview(g);
        this.interaction.render(g, mouseX, mouseY, false);
        if (HudRenderer.bounds().isEmpty()) {
            String empty = HudStyle.tr("\u0412\u043a\u043b\u044e\u0447\u0438\u0442\u0435 HUD-\u043c\u043e\u0434\u0443\u043b\u0438 \u0432 \u043c\u0435\u043d\u044e.", "Enable HUD modules in the menu.");
            CustomFont font = CustomFont.regular();
            font.draw(g, font.ellipsize(empty, 10.0f, this.field_22789 - 30), ((float)this.field_22789 - Math.min((float)(this.field_22789 - 30), font.width(empty, 10.0f))) / 2.0f, this.field_22790 / 2 - 5, 10.0f, -4802625);
        }
        if (!this.interaction.active()) {
            this.drawToolbar(g, mouseX, mouseY);
        } else {
            this.toolbar = null;
            this.done = null;
            this.recoverButton = null;
            this.guidesButton = null;
            this.adaptiveButton = null;
        }
    }

    private void drawToolbar(class_332 g, int mouseX, int mouseY) {
        int panelWidth = Math.min(344, Math.max(1, this.field_22789 - 20));
        int panelHeight = Math.min(68, Math.max(1, this.field_22790 - 12));
        this.toolbar = this.freeToolbar(panelWidth, panelHeight);
        int x = this.toolbar.x();
        int y = this.toolbar.y();
        UiDraw.border(g, x, y, panelWidth, panelHeight, 6, ThemeColors.surface(-182904548), ThemeColors.surface(-13290181));
        int accent = BloodHelperClient.config().accent;
        if (panelWidth > 2 && panelHeight > 12) {
            g.method_25294(x + 1, y + 6, x + 2, y + panelHeight - 6, UiDraw.alpha(accent, 190));
        }
        g.method_44379(x, y, x + panelWidth, y + panelHeight);
        CustomFont regular = CustomFont.regular();
        CustomFont medium = CustomFont.medium();
        int inset = Math.min(6, Math.max(0, (panelWidth - 1) / 2));
        int buttonWidth = Math.min(44, Math.max(1, panelWidth - inset * 2));
        int buttonHeight = Math.min(16, Math.max(1, panelHeight - 8));
        this.done = new HudRenderer.Bounds(x + panelWidth - buttonWidth - inset, y + Math.min(4, panelHeight - buttonHeight), buttonWidth, buttonHeight);
        boolean overDone = this.done.contains(mouseX, mouseY);
        UiDraw.border(g, this.done.x(), this.done.y(), this.done.width(), this.done.height(), 2, ThemeColors.surface(overDone ? -13818584 : -14342872), overDone ? UiDraw.alpha(accent, 160) : ThemeColors.surface(-12829630));
        String finish = HudStyle.tr("\u0413\u043e\u0442\u043e\u0432\u043e", "Done");
        finish = medium.ellipsize(finish, 8.0f, Math.max(0, buttonWidth - 6));
        medium.draw(g, finish, (float)this.done.x() + ((float)this.done.width() - medium.width(finish, 8.0f)) / 2.0f, this.done.y() + 3, 8.0f, -921101);
        float titleWidth = Math.max(0, this.done.x() - x - 17);
        Object title = this.interaction.selectedCount() > 1 ? HudStyle.tr("\u0412\u044b\u0431\u0440\u0430\u043d\u043e: ", "Selected: ") + this.interaction.selectedCount() : (this.interaction.selected() == null ? (this.combatLayout ? HudStyle.tr("\u0411\u043e\u0435\u0432\u043e\u0439 HUD", "Combat HUD") : HudStyle.tr("\u041e\u0431\u044b\u0447\u043d\u044b\u0439 HUD", "Normal HUD")) : this.selectionLabel(titleWidth));
        medium.draw(g, medium.ellipsize((String)title, 8.5f, titleWidth), x + 9, y + 6, 8.5f, -921101);
        this.recoverButton = null;
        this.guidesButton = null;
        this.adaptiveButton = null;
        if (panelWidth >= 250 && panelHeight >= 62) {
            int width = (panelWidth - 26) / 3;
            this.adaptiveButton = this.toolbarButton(g, x + 8, y + 27, width, HudStyle.tr("\u0410\u0434\u0430\u043f\u0442\u0430\u0446\u0438\u044f: ", "Adaptive: ") + (BloodHelperClient.config().hudEditor.adaptive ? HudStyle.tr("\u0434\u0430", "on") : HudStyle.tr("\u043d\u0435\u0442", "off")), mouseX, mouseY);
            this.guidesButton = this.toolbarButton(g, x + 13 + width, y + 27, width, HudStyle.tr("\u041d\u0430\u043f\u0440\u0430\u0432\u043b\u044f\u044e\u0449\u0438\u0435: ", "Guides: ") + (BloodHelperClient.config().hudEditor.guides ? HudStyle.tr("\u0434\u0430", "on") : HudStyle.tr("\u043d\u0435\u0442", "off")), mouseX, mouseY);
            this.recoverButton = this.toolbarButton(g, x + 18 + width * 2, y + 27, width, HudStyle.tr("\u0412\u0435\u0440\u043d\u0443\u0442\u044c \u043d\u0430 \u044d\u043a\u0440\u0430\u043d", "Return to screen"), mouseX, mouseY);
        }
        String hint = HudStyle.tr("Shift: \u0432\u044b\u0431\u043e\u0440 \u00b7 \u041a\u0440\u0430\u044f: \u0440\u0430\u0437\u043c\u0435\u0440 \u00b7 Ctrl+Z: \u043e\u0442\u043c\u0435\u043d\u0430 \u00b7 Alt: \u0431\u0435\u0437 \u043c\u0430\u0433\u043d\u0438\u0442\u0430", "Shift: select \u00b7 Edges: resize \u00b7 Ctrl+Z: undo \u00b7 Alt: free move");
        regular.draw(g, regular.ellipsize(hint, 7.5f, Math.max(0, panelWidth - 18)), x + 9, y + Math.max(28, panelHeight - 15), 7.5f, -4802625);
        g.method_44380();
    }

    private HudRenderer.Bounds toolbarButton(class_332 g, int x, int y, int width, String text, int mx, int my) {
        HudRenderer.Bounds box = new HudRenderer.Bounds(x, y, width, 17);
        boolean hover = box.contains(mx, my);
        UiDraw.border(g, x, y, width, 17, 3, ThemeColors.surface(hover ? -13291463 : -14145490), ThemeColors.surface(-12237492));
        CustomFont font = CustomFont.regular();
        String label = font.ellipsize(text, 7.5f, width - 8);
        font.draw(g, label, (float)x + ((float)width - font.width(label, 7.5f)) / 2.0f, y + 4, 7.5f, -921101);
        return box;
    }

    private String selectionLabel(float availableWidth) {
        class_2960 id;
        String selected = this.interaction.selected();
        String name = HudWidgets.get(selected, BloodHelperClient.config()).title();
        if (selected.startsWith("itemCounter.") && (id = class_2960.method_12829((String)selected.substring("itemCounter.".length()))) != null && class_7923.field_41178.method_10250(id)) {
            name = UiLanguage.text(new class_1799((class_1935)class_7923.field_41178.method_63535(id)).method_7964());
        }
        String scale = " \u00b7 " + String.format(Locale.ROOT, "%.0f%%", this.position((String)selected).scale / 0.65 * 100.0);
        CustomFont font = CustomFont.medium();
        return font.ellipsize(name, 8.5f, Math.max(0.0f, availableWidth - font.width(scale, 8.5f))) + scale;
    }

    private HudRenderer.Bounds freeToolbar(int w, int h) {
        if (this.cachedToolbar != null && this.cachedToolbar.width() == w && this.cachedToolbar.height() == h && this.toolbarScreenWidth == this.field_22789 && this.toolbarScreenHeight == this.field_22790 && this.toolbarLayout.equals(HudRenderer.bounds())) {
            return this.cachedToolbar;
        }
        this.toolbarLayout = Map.copyOf(HudRenderer.bounds());
        this.toolbarScreenWidth = this.field_22789;
        this.toolbarScreenHeight = this.field_22790;
        this.cachedToolbar = this.findFreeToolbar(w, h);
        return this.cachedToolbar;
    }

    private HudRenderer.Bounds findFreeToolbar(int w, int h) {
        int left = Math.min(10, Math.max(0, this.field_22789 - w));
        int right = Math.max(0, this.field_22789 - w - 10);
        int top = Math.min(10, Math.max(0, this.field_22790 - h));
        int bottom = Math.max(0, this.field_22790 - h - 10);
        int center = Math.max(0, (this.field_22789 - w) / 2);
        LinkedHashSet<Integer> columns = new LinkedHashSet<Integer>();
        columns.add(center);
        columns.add(left);
        columns.add(right);
        LinkedHashSet<Integer> rows = new LinkedHashSet<Integer>();
        rows.add(bottom);
        rows.add(top);
        for (HudRenderer.Bounds box : HudRenderer.bounds().values()) {
            if (columns.size() >= 32 || rows.size() >= 32) break;
            columns.add(Math.clamp((long)(box.x() - w - 8), 0, Math.max(0, this.field_22789 - w)));
            columns.add(Math.clamp((long)(box.x() + box.width() + 8), 0, Math.max(0, this.field_22789 - w)));
            rows.add(Math.clamp((long)(box.y() - h - 8), 0, Math.max(0, this.field_22790 - h)));
            rows.add(Math.clamp((long)(box.y() + box.height() + 8), 0, Math.max(0, this.field_22790 - h)));
        }
        rows.add(Math.max(0, (this.field_22790 - h) / 2));
        HudRenderer.Bounds best = new HudRenderer.Bounds(center, bottom, w, h);
        long score = Long.MAX_VALUE;
        Iterator iterator = rows.iterator();
        while (iterator.hasNext()) {
            int y = (Integer)iterator.next();
            Iterator iterator2 = columns.iterator();
            while (iterator2.hasNext()) {
                int x = (Integer)iterator2.next();
                HudRenderer.Bounds candidate = new HudRenderer.Bounds(x, y, w, h);
                long overlap = 0L;
                for (HudRenderer.Bounds box : HudRenderer.bounds().values()) {
                    int overlapWidth = Math.max(0, Math.min(candidate.x() + w, box.x() + box.width() + 6) - Math.max(candidate.x(), box.x() - 6));
                    int overlapHeight = Math.max(0, Math.min(candidate.y() + h, box.y() + box.height() + 6) - Math.max(candidate.y(), box.y() - 6));
                    overlap += (long)overlapWidth * (long)overlapHeight;
                }
                if (overlap < score) {
                    score = overlap;
                    best = candidate;
                }
                if (score != 0L) continue;
                return best;
            }
        }
        return best;
    }

    public boolean method_25402(class_11909 event, boolean doubled) {
        if (event.method_74245() == 0) {
            if (this.done != null && this.done.contains(event.comp_4798(), event.comp_4799())) {
                this.method_25419();
                return true;
            }
            if (this.adaptiveButton != null && this.adaptiveButton.contains(event.comp_4798(), event.comp_4799())) {
                this.interaction.toggleAdaptive();
                return true;
            }
            if (this.guidesButton != null && this.guidesButton.contains(event.comp_4798(), event.comp_4799())) {
                this.interaction.toggleGuides();
                return true;
            }
            if (this.recoverButton != null && this.recoverButton.contains(event.comp_4798(), event.comp_4799())) {
                this.interaction.recoverPositions();
                return true;
            }
            if (this.toolbar != null && this.toolbar.contains(event.comp_4798(), event.comp_4799())) {
                return true;
            }
        }
        return this.interaction.click(event, false) || super.method_25402(event, doubled);
    }

    public boolean method_25403(class_11909 event, double dx, double dy) {
        return this.interaction.drag(event, this.field_22789, this.field_22790) || super.method_25403(event, dx, dy);
    }

    public boolean method_25406(class_11909 event) {
        return this.interaction.release(event) || super.method_25406(event);
    }

    public boolean method_25401(double x, double y, double horizontal, double vertical) {
        if (this.toolbar != null && this.toolbar.contains(x, y)) {
            return false;
        }
        return this.interaction.scroll(x, y, vertical);
    }

    public boolean method_25404(class_11908 event) {
        if (this.interaction.keyPressed(event, false)) {
            return true;
        }
        return super.method_25404(event);
    }

    public void method_25419() {
        this.interaction.finish();
        BloodHelperClient.saveConfig();
        this.field_22787.method_1507(this.parent);
    }

    public void method_25432() {
        this.interaction.finish();
    }

    private ClientConfig.HudPosition position(String key) {
        ClientConfig config = BloodHelperClient.config();
        return CombatHudModule.editableWidget(key, config).position();
    }
}

