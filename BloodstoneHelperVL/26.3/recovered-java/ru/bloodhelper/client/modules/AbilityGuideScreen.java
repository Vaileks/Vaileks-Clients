/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.AbilityGuideSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.AbilityGuideModule;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class AbilityGuideScreen
extends ModuleDetailScreen {
    private final GuiTextField search = new GuiTextField(0, 0, 100, 23, "");
    private final GuiTextField notes = new GuiTextField(0, 0, 100, 23, "");
    private AbilityGuideSettings.Entry selected;
    private List<String> wrappedDescription = List.of();
    private int wrapWidth = -1;
    private String message = "";
    private List<AbilityGuideSettings.Entry> librarySource;
    private List<AbilityGuideSettings.Entry> libraryRows = List.of();
    private String libraryQuery = "";

    @Override
    protected int preferredPanelHeight() {
        return this.selected == null ? AbilityGuideScreen.listPanelHeight(80, BloodHelperClient.config().abilityGuide.entries.size(), 42, 64) : 410;
    }

    public AbilityGuideScreen(Screen parent, AbilityGuideSettings.Entry entry) {
        super(parent, "Ability Guide");
        this.search.setTextSize(11.0f);
        this.search.setMaxLength(128);
        this.search.setResponder(v -> {
            this.scroll = 0;
        });
        this.notes.setTextSize(11.0f);
        this.notes.setMaxLength(384);
        this.select(entry);
    }

    private void select(AbilityGuideSettings.Entry entry) {
        this.selected = entry;
        this.scroll = 0;
        this.wrapWidth = -1;
        this.notes.setValue(entry == null ? "" : entry.notes);
        this.notes.setFocused(false);
        this.search.setFocused(false);
    }

    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("\u0421\u043f\u0440\u0430\u0432\u043e\u0447\u043d\u0438\u043a \u0441\u043f\u043e\u0441\u043e\u0431\u043d\u043e\u0441\u0442\u0435\u0439", "Ability Guide"), mx, my);
        int rowY = this.top + 46;
        this.button(g, this.left + 14, rowY, 96, HudStyle.tr("\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u0440\u0443\u043a\u0435", "Held item"), mx, my, () -> {
            AbilityGuideSettings.Entry captured = AbilityGuideModule.captureHeld();
            if (captured == null) {
                this.message = HudStyle.tr("\u0412\u043a\u043b\u044e\u0447\u0438 \u043c\u043e\u0434\u0443\u043b\u044c \u0438 \u0432\u043e\u0437\u044c\u043c\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0441 \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u0435\u043c.", "Enable the module and hold an item with a description.");
            } else {
                this.select(captured);
                this.message = "";
            }
        });
        this.button(g, this.left + 116, rowY, 80, HudStyle.tr("\u0412\u0441\u0435 \u0437\u0430\u043f\u0438\u0441\u0438", "All entries"), mx, my, () -> this.select(null));
        this.bodyTop = this.top + 80;
        if (this.selected == null) {
            this.renderLibrary(g, mx, my, delta);
        } else {
            this.renderEntry(g, mx, my, delta);
        }
        this.footer(g, this.message.isEmpty() ? HudStyle.tr("\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u044f \u043e\u0442 \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u00b7 \u0417\u0430\u043c\u0435\u0442\u043a\u0438 \u0445\u0440\u0430\u043d\u044f\u0442\u0441\u044f \u0442\u043e\u043b\u044c\u043a\u043e \u0443 \u0442\u0435\u0431\u044f", "Server-provided descriptions \u00b7 Notes stay on your device") : this.message);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderLibrary(GuiGraphicsExtractor g, int mx, int my, float delta) {
        this.search.setHint(HudStyle.tr("\u041f\u043e\u0438\u0441\u043a \u043f\u043e \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u044e \u0438\u043b\u0438 \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u044e", "Search names or descriptions"));
        AbilityGuideScreen.field(g, this.search, this.left + 222, this.top + 45, Math.max(20, this.panelWidth - 238), mx, my, delta);
        String query = this.search.getValue().strip().toLowerCase(Locale.ROOT);
        List<AbilityGuideSettings.Entry> source = BloodHelperClient.config().abilityGuide.entries;
        if (this.librarySource != source || !this.libraryQuery.equals(query)) {
            this.librarySource = source;
            this.libraryQuery = query;
            this.libraryRows = source.stream().filter(e -> query.isBlank() || e.name.toLowerCase(Locale.ROOT).contains(query) || e.description.stream().anyMatch(line -> line.toLowerCase(Locale.ROOT).contains(query))).toList();
        }
        List<AbilityGuideSettings.Entry> rows = this.libraryRows;
        this.maximumScroll = Math.max(0, rows.size() * 42 - (this.bodyBottom - this.bodyTop));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        g.enableScissor(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            int y = this.bodyTop - this.scroll;
            if (rows.isEmpty()) {
                for (String line : AbilityGuideScreen.wrap(HudStyle.tr("\u0412\u043e\u0437\u044c\u043c\u0438 \u043b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043a\u0443 \u0432 \u0440\u0443\u043a\u0443 \u0438 \u043d\u0430\u0436\u043c\u0438 \u00ab\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u0440\u0443\u043a\u0435\u00bb. \u0417\u0434\u0435\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u0442\u0441\u044f \u0435\u0451 \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u0435 \u0438 \u0442\u0432\u043e\u0438 \u0437\u0430\u043c\u0435\u0442\u043a\u0438.", "Hold a legendary item and press Held item to save its description and your notes here."), this.panelWidth - 44)) {
                    this.text(g, line, this.left + 20, y + 10, -7105633);
                    y += 16;
                }
            }
            for (AbilityGuideSettings.Entry entry : rows) {
                if (y + 40 > this.bodyTop && y < this.bodyBottom) {
                    UiDraw.rounded(g, this.left + 14, y, this.panelWidth - 30, 37, 7, ThemeColors.surface(-14079183));
                    Identifier id = Identifier.tryParse((String)entry.itemId);
                    if (id != null) {
                        g.item(new ItemStack((ItemLike)BuiltInRegistries.ITEM.getValue(id)), this.left + 22, y + 10);
                    }
                    this.text(g, CustomFont.regular().ellipsize(entry.name, 11.0f, Math.max(1, this.panelWidth - 155)), this.left + 47, y + 12, -1381908);
                    if (y >= this.bodyTop && y + 37 <= this.bodyBottom) {
                        this.button(g, this.left + this.panelWidth - 89, y + 8, 61, HudStyle.tr("\u041e\u0442\u043a\u0440\u044b\u0442\u044c", "Open"), mx, my, () -> this.select(entry));
                    }
                }
                y += 42;
            }
        }
        finally {
            g.disableScissor();
        }
        this.scrollbar(g, rows.size() * 42);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderEntry(GuiGraphicsExtractor g, int mx, int my, float delta) {
        if (this.wrapWidth != this.panelWidth) {
            ArrayList<String> lines = new ArrayList<String>();
            for (String value : this.selected.description) {
                lines.addAll(AbilityGuideScreen.wrap(value, Math.max(1, this.panelWidth - 44)));
            }
            if (lines.isEmpty()) {
                lines.addAll(AbilityGuideScreen.wrap(HudStyle.tr("\u0423 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 \u043d\u0435\u0442 \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u044f \u0441\u043f\u043e\u0441\u043e\u0431\u043d\u043e\u0441\u0442\u0435\u0439. \u041c\u043e\u0436\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0441\u0432\u043e\u044e \u0437\u0430\u043c\u0435\u0442\u043a\u0443 \u043d\u0438\u0436\u0435.", "This item has no ability description. You can add a note below."), Math.max(1, this.panelWidth - 44)));
            }
            this.wrappedDescription = List.copyOf(lines);
            this.wrapWidth = this.panelWidth;
        }
        int noteTop = this.bodyBottom - 57;
        int viewportBottom = Math.max(this.bodyTop + 1, noteTop - 9);
        this.maximumScroll = Math.max(0, 32 + this.wrappedDescription.size() * 16 - (viewportBottom - this.bodyTop));
        this.scroll = Math.clamp((long)this.scroll, 0, this.maximumScroll);
        g.enableScissor(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, viewportBottom);
        try {
            int y = this.bodyTop - this.scroll;
            CustomFont.medium().draw(g, CustomFont.medium().ellipsize(this.selected.name, 14.0f, this.panelWidth - 44), this.left + 20, y, 14.0f, -1381908);
            y += 30;
            for (String line : this.wrappedDescription) {
                if (y + 16 > this.bodyTop && y < viewportBottom) {
                    this.text(g, line, this.left + 20, y, -7105633);
                }
                y += 16;
            }
        }
        finally {
            g.disableScissor();
        }
        this.text(g, HudStyle.tr("\u041c\u043e\u044f \u0437\u0430\u043c\u0435\u0442\u043a\u0430", "My note"), this.left + 18, noteTop, -7105633);
        this.notes.setHint(HudStyle.tr("\u041d\u0430\u043f\u0440\u0438\u043c\u0435\u0440: \u0443\u0434\u043e\u0431\u043d\u0435\u0435 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u043f\u043e\u0441\u043b\u0435 \u0440\u044b\u0432\u043a\u0430", "Your own reminder"));
        AbilityGuideScreen.field(g, this.notes, this.left + 14, noteTop + 17, Math.max(20, this.panelWidth - 126), mx, my, delta);
        this.button(g, this.left + this.panelWidth - 103, noteTop + 18, 88, HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c", "Save note"), mx, my, () -> {
            this.selected.notes = AbilityGuideSettings.clean(this.notes.getValue(), 384);
            BloodHelperClient.saveConfig();
            this.message = HudStyle.tr("\u0417\u0430\u043c\u0435\u0442\u043a\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0430", "Note saved");
            this.notes.setFocused(false);
        });
        this.button(g, this.left + this.panelWidth - 104, this.top + 46, 90, HudStyle.tr("\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0437\u0430\u043f\u0438\u0441\u044c", "Delete entry"), mx, my, () -> {
            BloodHelperClient.config().abilityGuide.entries.removeIf(e -> e.id.equals(this.selected.id));
            BloodHelperClient.saveConfig();
            this.select(null);
        });
    }

    private static void field(GuiGraphicsExtractor g, GuiTextField field, int x, int y, int w, int mx, int my, float delta) {
        field.setX(x + 8);
        field.setY(y + 1);
        field.setWidth(Math.max(1, w - 16));
        UiDraw.rounded(g, x, y, w, 25, 6, ThemeColors.surface(-15460579));
        field.render(g, mx, my, delta);
    }

    @Override
    protected boolean panelMouseClicked(MouseButtonEvent e, boolean doubled) {
        GuiTextField input;
        GuiTextField guiTextField = input = this.selected == null ? this.search : this.notes;
        if (input.mouseClicked(e, doubled)) {
            return true;
        }
        return super.panelMouseClicked(e, doubled);
    }

    @Override
    protected boolean panelMouseDragged(MouseButtonEvent e, double dx, double dy) {
        return (this.selected == null ? this.search : this.notes).mouseDragged(e, dx, dy) || super.panelMouseDragged(e, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(MouseButtonEvent e) {
        return (this.selected == null ? this.search : this.notes).mouseReleased(e) || super.panelMouseReleased(e);
    }

    public boolean keyPressed(KeyEvent e) {
        return (this.selected == null ? this.search : this.notes).keyPressed(e) || super.keyPressed(e);
    }

    public boolean charTyped(CharacterEvent e) {
        return (this.selected == null ? this.search : this.notes).charTyped(e) || super.charTyped(e);
    }
}

