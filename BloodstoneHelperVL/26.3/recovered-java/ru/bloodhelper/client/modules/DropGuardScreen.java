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
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.DropGuardSettings;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.DropGuardModule;
import ru.bloodhelper.client.modules.ModuleDetailScreen;

public final class DropGuardScreen
extends ModuleDetailScreen {
    private final GuiTextField search = new GuiTextField(0, 0, 100, 21, "");
    private boolean inventory;
    private List<Row> cachedRows;
    private int refreshTicks;

    @Override
    protected int preferredPanelWidth() {
        return 520;
    }

    @Override
    protected int preferredPanelHeight() {
        return DropGuardScreen.listPanelHeight(105, this.rows().size(), 42, 64);
    }

    public DropGuardScreen(Screen parent) {
        super(parent, "Drop Guard");
        this.search.setMaxLength(80);
        this.search.setTextSize(11.0f);
        this.search.setResponder(value -> {
            this.scroll = 0;
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mx, int my, float delta) {
        this.frame(g, HudStyle.tr("Drop Guard \u00b7 \u0437\u0430\u0449\u0438\u0449\u0451\u043d\u043d\u044b\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b", "Drop Guard \u00b7 protected items"), mx, my);
        int tabWidth = Math.max(65, Math.min(140, (this.panelWidth - 38) / 3));
        this.button(g, this.left + 14, this.bodyTop, tabWidth, HudStyle.tr("\u0417\u0430\u0449\u0438\u0449\u0451\u043d\u043d\u044b\u0435", "Protected"), mx, my, () -> {
            this.inventory = false;
            this.scroll = 0;
            this.cachedRows = null;
        });
        this.button(g, this.left + 20 + tabWidth, this.bodyTop, tabWidth, HudStyle.tr("\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c", "Inventory"), mx, my, () -> {
            this.inventory = true;
            this.scroll = 0;
            this.cachedRows = null;
        });
        this.button(g, this.left + this.panelWidth - tabWidth - 14, this.bodyTop, tabWidth, HudStyle.tr("\u041f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u0440\u0443\u043a\u0435 +", "Add held item"), mx, my, () -> {
            if (this.minecraft.player != null) {
                DropGuardModule.add(this.minecraft.player.getMainHandItem());
            }
            this.cachedRows = null;
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
        String query = this.search.getValue().strip().toLowerCase(Locale.ROOT);
        List<Row> rows = this.rows().stream().filter(r -> query.isEmpty() || r.entry.name.toLowerCase(Locale.ROOT).contains(query) || r.entry.itemId.contains(query)).toList();
        this.scrollbar(g, Math.max(36, rows.size() * 42));
        g.enableScissor(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            if (rows.isEmpty()) {
                String line = this.inventory && this.minecraft.player == null ? HudStyle.tr("\u041e\u0442\u043a\u0440\u043e\u0439\u0442\u0435 \u043c\u0438\u0440, \u0447\u0442\u043e\u0431\u044b \u0432\u044b\u0431\u0440\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b.", "Join a world to select inventory items.") : HudStyle.tr("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u043d\u0435\u0442. \u0414\u043e\u0431\u0430\u0432\u044c\u0442\u0435 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432 \u0440\u0443\u043a\u0435 \u0438\u043b\u0438 \u0438\u0437 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f.", "No items. Add your held item or select one from inventory.");
                int y = this.bodyTop + 8;
                for (String wrapped : DropGuardScreen.wrap(line, this.panelWidth - 40)) {
                    this.text(g, wrapped, this.left + 20, y, -7105633);
                    y += 15;
                }
            }
            for (int index = 0; index < rows.size(); ++index) {
                Row row = rows.get(index);
                int y = this.bodyTop + index * 42 - this.scroll;
                if (y + 38 <= this.bodyTop || y >= this.bodyBottom) continue;
                UiDraw.rounded(g, this.left + 14, y, this.panelWidth - 28, 38, 7, ThemeColors.surface(-1339677139));
                g.item(row.icon, this.left + 23, y + 10);
                int textWidth = Math.max(20, this.panelWidth - 166);
                this.text(g, CustomFont.regular().ellipsize(row.entry.name, 11.0f, textWidth), this.left + 47, y + 6, -1381908);
                String detail = row.entry.itemId + (row.entry.customName.isEmpty() && row.entry.customModel.isEmpty() ? "" : HudStyle.tr(" \u00b7 \u0441\u0432\u043e\u0439 \u0432\u0430\u0440\u0438\u0430\u043d\u0442", " \u00b7 custom variant"));
                CustomFont.regular().draw(g, CustomFont.regular().ellipsize(detail, 9.0f, textWidth), this.left + 47, y + 22, 9.0f, -7105633);
                if (y < this.bodyTop || y + 32 > this.bodyBottom) continue;
                boolean protectedItem = BloodHelperClient.config().dropGuard.protectedItems.stream().anyMatch(e -> e.key().equals(row.entry.key()));
                this.button(g, this.left + this.panelWidth - 103, y + 8, 80, this.inventory ? (protectedItem ? HudStyle.tr("\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u043e", "Protected") : HudStyle.tr("\u0417\u0430\u0449\u0438\u0442\u0438\u0442\u044c", "Protect")) : HudStyle.tr("\u0423\u0431\u0440\u0430\u0442\u044c", "Remove"), mx, my, () -> {
                    if (this.inventory) {
                        DropGuardModule.add(row.icon);
                        this.cachedRows = null;
                    } else {
                        BloodHelperClient.config().dropGuard.protectedItems.removeIf(e -> e.key().equals(row.entry.key()));
                        DropGuardModule.reset();
                        BloodHelperClient.saveConfig();
                        this.cachedRows = null;
                    }
                });
            }
        }
        finally {
            g.disableScissor();
        }
        this.footer(g, HudStyle.tr("\u041f\u043e\u0432\u0442\u043e\u0440\u043d\u043e\u0435 \u043d\u0430\u0436\u0430\u0442\u0438\u0435 \u0437\u0430 2 \u0441 \u00b7 \u0443\u0434\u0435\u0440\u0436\u0430\u043d\u0438\u0435 \u043d\u0435 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0430\u0435\u0442 \u00b7 ", "Press again within 2s \u00b7 holding never confirms \u00b7 ") + BloodHelperClient.config().dropGuard.protectedItems.size() + "/128");
    }

    public void tick() {
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
        LinkedHashMap<String, Row> current = new LinkedHashMap<String, Row>();
        if (this.minecraft.player != null) {
            Inventory items = this.minecraft.player.getInventory();
            for (int slot = 0; slot < items.getContainerSize(); ++slot) {
                ItemStack stack = items.getItem(slot);
                if (stack.isEmpty()) continue;
                DropGuardSettings.Entry entry = DropGuardModule.capture(stack);
                current.putIfAbsent(entry.key(), new Row(entry, stack.copy()));
            }
        }
        if (this.inventory) {
            return new ArrayList<Row>(current.values());
        }
        ArrayList<Row> rows = new ArrayList<Row>();
        for (DropGuardSettings.Entry entry : BloodHelperClient.config().dropGuard.protectedItems) {
            Row actual = (Row)current.get(entry.key());
            Identifier id = Identifier.tryParse((String)entry.itemId);
            ItemStack fallback = id == null ? ItemStack.EMPTY : new ItemStack((ItemLike)BuiltInRegistries.ITEM.getValue(id));
            rows.add(actual == null ? new Row(entry, fallback) : new Row(entry, actual.icon));
        }
        return rows;
    }

    @Override
    protected boolean panelMouseClicked(MouseButtonEvent e, boolean doubled) {
        return this.search.mouseClicked(e, doubled) || super.panelMouseClicked(e, doubled);
    }

    @Override
    protected boolean panelMouseDragged(MouseButtonEvent e, double dx, double dy) {
        return this.search.mouseDragged(e, dx, dy) || super.panelMouseDragged(e, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(MouseButtonEvent e) {
        return this.search.mouseReleased(e) || super.panelMouseReleased(e);
    }

    public boolean keyPressed(KeyEvent e) {
        return this.search.keyPressed(e) || super.keyPressed(e);
    }

    public boolean charTyped(CharacterEvent e) {
        return this.search.charTyped(e) || super.charTyped(e);
    }

    private record Row(DropGuardSettings.Entry entry, ItemStack icon) {
    }
}

