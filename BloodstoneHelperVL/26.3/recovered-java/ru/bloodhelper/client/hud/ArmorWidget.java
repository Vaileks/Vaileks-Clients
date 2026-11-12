/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package ru.bloodhelper.client.hud;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.InventorySnapshot;

public final class ArmorWidget {
    private static final int WARNING = -39568;
    private static final int HEADER_HEIGHT = 21;

    private ArmorWidget() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(GuiGraphicsExtractor g, ClientConfig c, boolean preview) {
        int rowHeight;
        int cellWidth;
        ClientConfig.ArmorSettings settings = c.armor;
        List<InventorySnapshot.ArmorPiece> armor = preview ? ArmorWidget.previewArmor() : InventorySnapshot.capture().armor();
        long equipped = armor.stream().filter(piece -> !piece.item().isEmpty()).count();
        if (!preview && (armor.isEmpty() || settings.hideWhenEmpty && equipped == 0L)) {
            return;
        }
        if ("SEPARATE".equals(settings.layout)) {
            ArmorWidget.renderSeparate(g, c, armor);
            return;
        }
        boolean vertical = "VERTICAL".equals(settings.orientation);
        int n = cellWidth = settings.showNames ? 98 : 40;
        int n2 = vertical ? 26 : (rowHeight = settings.showNames ? 61 : 45);
        int width = vertical ? (settings.showNames ? 188 : 104) : cellWidth * armor.size();
        int headerHeight = c.hudTitle("armor") ? 21 : 0;
        int height = headerHeight + rowHeight * (vertical ? armor.size() : 1);
        HudRenderer.begin(g, "armor", settings.position, settings.alignment, settings.bottom, width, height);
        try {
            if (settings.background) {
                HudStyle.panel(g, width, height, c);
            }
            if (c.hudTitle("armor")) {
                HudStyle.divider(g, 6, 19, width - 12);
                UiIcons.draw(g, "armor", 5, 4, 12, -4802625);
                HudStyle.strong(g, HudStyle.tr(c, "\u0411\u0440\u043e\u043d\u044f", "Armor"), 25, 3, -921101, settings.shadow);
                String count = equipped + "/4";
                HudStyle.label(g, count, width - 7 - HudStyle.labelWidth(count), 4, -7763050, settings.shadow);
            }
            for (int i = 0; i < armor.size(); ++i) {
                InventorySnapshot.ArmorPiece piece2 = armor.get(i);
                if (vertical) {
                    int y = headerHeight + i * rowHeight;
                    if (c.hudTitle("armor") && i > 0) {
                        HudStyle.divider(g, 7, y - 1, width - 14);
                    }
                    ArmorWidget.renderRow(g, c, piece2, y, width);
                    continue;
                }
                int x = i * cellWidth;
                if (c.hudTitle("armor") && i > 0) {
                    HudStyle.separator(g, x, headerHeight + 3, rowHeight - 8);
                }
                ArmorWidget.renderCell(g, c, piece2, x, headerHeight, cellWidth);
            }
        }
        finally {
            g.pose().popMatrix();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderSeparate(GuiGraphicsExtractor g, ClientConfig c, List<InventorySnapshot.ArmorPiece> armor) {
        ClientConfig.ArmorSettings settings = c.armor;
        int width = settings.showNames ? 188 : 40;
        int height = settings.showNames ? 28 : 45;
        for (InventorySnapshot.ArmorPiece piece : armor) {
            String slot = ArmorWidget.slotKey(piece.slot());
            HudRenderer.begin(g, "armor." + slot, settings.partPosition(slot), "LEFT", false, width, height);
            try {
                if (settings.background) {
                    HudStyle.panel(g, width, height, c);
                }
                if (settings.showNames) {
                    ArmorWidget.renderRow(g, c, piece, 2, width);
                    continue;
                }
                ArmorWidget.renderCell(g, c, piece, 0, 0, width);
            }
            finally {
                g.pose().popMatrix();
            }
        }
    }

    private static void renderCell(GuiGraphicsExtractor g, ClientConfig c, InventorySnapshot.ArmorPiece piece, int x, int y, int width) {
        InventorySnapshot.Durability durability;
        ClientConfig.ArmorSettings settings = c.armor;
        if (!piece.item().isEmpty()) {
            g.item(piece.item(), x + (width - 16) / 2, y + 3);
        } else {
            UiIcons.draw(g, "shield", x + (width - 12) / 2, y + 5, 12, -7763050);
        }
        if (settings.showNames) {
            String name = HudStyle.fit(piece.item().isEmpty() ? ArmorWidget.emptySlot(piece.slot()) : piece.displayName(), width - 12);
            HudStyle.text(g, name, x + (width - HudStyle.width(name)) / 2, y + 23, -4802625, settings.shadow);
        }
        int color = (durability = piece.durability()).low(settings.warnPercent) ? -39568 : -4802625;
        String value = HudStyle.fitStrong(durability.display(settings.durabilityMode), width - 6);
        int valueY = settings.showNames ? 38 : 23;
        HudStyle.strong(g, value, x + (width - HudStyle.strongWidth(value)) / 2, y + valueY, color, settings.shadow);
        if (durability.damageable()) {
            HudStyle.progress(g, "armor." + String.valueOf(piece.slot()), x + 5, y + valueY + 17, width - 10, durability.fraction(), color == -39568 ? color : -6907228);
        }
    }

    private static void renderRow(GuiGraphicsExtractor g, ClientConfig c, InventorySnapshot.ArmorPiece piece, int y, int width) {
        ClientConfig.ArmorSettings settings = c.armor;
        if (!piece.item().isEmpty()) {
            g.item(piece.item(), 5, y + 2);
        } else {
            UiIcons.draw(g, "shield", 7, y + 4, 12, -7763050);
        }
        InventorySnapshot.Durability durability = piece.durability();
        String value = HudStyle.fitStrong(durability.display(settings.durabilityMode), width - 36);
        int valueX = width - 7 - HudStyle.strongWidth(value);
        if (settings.showNames) {
            String name = piece.item().isEmpty() ? ArmorWidget.emptySlot(piece.slot()) : piece.displayName();
            HudStyle.text(g, HudStyle.fit(name, valueX - 32), 27, y + 3, -921101, settings.shadow);
        }
        int color = durability.low(settings.warnPercent) ? -39568 : -4802625;
        HudStyle.strong(g, value, valueX, y + 3, color, settings.shadow);
        if (durability.damageable()) {
            HudStyle.progress(g, "armor." + String.valueOf(piece.slot()), 27, y + 18, width - 34, durability.fraction(), color == -39568 ? color : -6907228);
        }
    }

    private static String slotKey(EquipmentSlot slot) {
        return switch (slot) {
            case EquipmentSlot.HEAD -> "head";
            case EquipmentSlot.CHEST -> "chest";
            case EquipmentSlot.LEGS -> "legs";
            default -> "feet";
        };
    }

    private static String emptySlot(EquipmentSlot slot) {
        return switch (slot) {
            case EquipmentSlot.HEAD -> HudStyle.tr("\u041d\u0435\u0442 \u0448\u043b\u0435\u043c\u0430", "No helmet");
            case EquipmentSlot.CHEST -> HudStyle.tr("\u041d\u0435\u0442 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a\u0430", "No chestplate");
            case EquipmentSlot.LEGS -> HudStyle.tr("\u041d\u0435\u0442 \u043f\u043e\u043d\u043e\u0436\u0435\u0439", "No leggings");
            default -> HudStyle.tr("\u041d\u0435\u0442 \u0431\u043e\u0442\u0438\u043d\u043e\u043a", "No boots");
        };
    }

    private static List<InventorySnapshot.ArmorPiece> previewArmor() {
        ItemStack helmet = new ItemStack((ItemLike)Items.NETHERITE_HELMET);
        ItemStack chest = new ItemStack((ItemLike)Items.NETHERITE_CHESTPLATE);
        ItemStack legs = new ItemStack((ItemLike)Items.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack((ItemLike)Items.NETHERITE_BOOTS);
        helmet.setDamageValue(Math.round((float)helmet.getMaxDamage() * 0.16f));
        chest.setDamageValue(Math.round((float)chest.getMaxDamage() * 0.83f));
        legs.setDamageValue(Math.round((float)legs.getMaxDamage() * 0.42f));
        boots.setDamageValue(Math.round((float)boots.getMaxDamage() * 0.07f));
        return List.of(InventorySnapshot.armorPiece(EquipmentSlot.HEAD, helmet), InventorySnapshot.armorPiece(EquipmentSlot.CHEST, chest), InventorySnapshot.armorPiece(EquipmentSlot.LEGS, legs), InventorySnapshot.armorPiece(EquipmentSlot.FEET, boots));
    }
}

