/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.alchemy.PotionContents
 *  net.minecraft.world.level.ItemLike
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;
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
    private List<ItemStack> pending;

    private void refreshSelection() {
        this.selectedKeys = this.settings().selected.stream().map(DroppedItemFilterSettings.Entry::key).collect(Collectors.toSet());
    }

    public DroppedItemFilterScreen(Screen parent) {
        super(parent, "Dropped items");
        this.search.setMaxLength(100);
        this.search.setTextSize(11.0f);
        this.search.setResponder(v -> {
            this.query = null;
            this.scroll = 0;
        });
    }

    protected void init() {
        this.pending = null;
        this.translations = null;
        this.refreshSelection();
    }

    public void tick() {
        Object token = UiLanguage.cacheToken();
        if (this.pending == null || this.translations != token || !Objects.equals(this.catalogLanguage, BloodHelperClient.config().language)) {
            this.translations = token;
            this.catalogLanguage = BloodHelperClient.config().language;
            this.beginCatalog();
        }
        int before = this.buildIndex;
        int limit = Math.min(this.pending.size(), this.buildIndex + 96);
        while (this.buildIndex < limit) {
            ItemStack stack = this.pending.get(this.buildIndex);
            String item = BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).toString();
            PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
            String potion = contents == null ? "" : contents.potion().flatMap(h -> h.unwrapKey()).map(k -> k.identifier().toString()).orElse("");
            Object name = UiLanguage.text(stack.getHoverName());
            if (potion.isEmpty() && (stack.is((Object)Items.POTION) || stack.is((Object)Items.SPLASH_POTION) || stack.is((Object)Items.LINGERING_POTION))) {
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
        this.pending = new ArrayList<ItemStack>();
        this.catalog.clear();
        this.buildIndex = 0;
        this.query = null;
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            this.pending.add(new ItemStack((ItemLike)item));
        }
        for (Holder.Reference potion : BuiltInRegistries.POTION.listElements().toList()) {
            for (Item item : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW)) {
                this.pending.add(PotionContents.createItemStack((Item)item, (Holder)potion));
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
    protected void renderPanel(GuiGraphicsExtractor g, int mx, int my, float delta) {
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
        g.enableScissor(this.left + 10, this.bodyTop, this.left + this.panelWidth - 10, this.bodyBottom);
        try {
            for (int i = Math.max(0, this.scroll / 35); i < this.filtered.size(); ++i) {
                int y = this.bodyTop + i * 35 - this.scroll;
                if (y >= this.bodyBottom) {
                    break;
                }
                Row row = this.filtered.get(i);
                boolean selected = this.selected(row);
                g.item(row.icon, this.left + 22, y + 6);
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
            g.disableScissor();
        }
        this.footer(g, (this.pending != null && this.buildIndex < this.pending.size() ? HudStyle.tr("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u043a\u0430\u0442\u0430\u043b\u043e\u0433\u0430\u2026 \u00b7 ", "Loading catalog\u2026 \u00b7 ") : "") + HudStyle.tr("\u0412\u044b\u0431\u0440\u0430\u043d\u043e: ", "Selected: ") + this.settings().selected.size() + (BloodHelperClient.config().optimizer.enabled ? "" : HudStyle.tr(" \u00b7 Optimizer \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d", " \u00b7 Optimizer is off")));
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

    private record Row(ItemStack icon, String itemId, String potionId, String name, String search) {
        String key() {
            return this.itemId + "\u0000" + this.potionId;
        }
    }
}

