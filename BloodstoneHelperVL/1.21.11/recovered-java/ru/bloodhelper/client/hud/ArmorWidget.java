/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1304
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.hud;

import java.util.List;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_332;
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
    public static void render(class_332 g, ClientConfig c, boolean preview) {
        int rowHeight;
        int cellWidth;
        ClientConfig.ArmorSettings settings = c.armor;
        List<InventorySnapshot.ArmorPiece> armor = preview ? ArmorWidget.previewArmor() : InventorySnapshot.capture().armor();
        long equipped = armor.stream().filter(piece -> !piece.item().method_7960()).count();
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
            g.method_51448().popMatrix();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderSeparate(class_332 g, ClientConfig c, List<InventorySnapshot.ArmorPiece> armor) {
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
                g.method_51448().popMatrix();
            }
        }
    }

    private static void renderCell(class_332 g, ClientConfig c, InventorySnapshot.ArmorPiece piece, int x, int y, int width) {
        InventorySnapshot.Durability durability;
        ClientConfig.ArmorSettings settings = c.armor;
        if (!piece.item().method_7960()) {
            g.method_51427(piece.item(), x + (width - 16) / 2, y + 3);
        } else {
            UiIcons.draw(g, "shield", x + (width - 12) / 2, y + 5, 12, -7763050);
        }
        if (settings.showNames) {
            String name = HudStyle.fit(piece.item().method_7960() ? ArmorWidget.emptySlot(piece.slot()) : piece.displayName(), width - 12);
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

    private static void renderRow(class_332 g, ClientConfig c, InventorySnapshot.ArmorPiece piece, int y, int width) {
        ClientConfig.ArmorSettings settings = c.armor;
        if (!piece.item().method_7960()) {
            g.method_51427(piece.item(), 5, y + 2);
        } else {
            UiIcons.draw(g, "shield", 7, y + 4, 12, -7763050);
        }
        InventorySnapshot.Durability durability = piece.durability();
        String value = HudStyle.fitStrong(durability.display(settings.durabilityMode), width - 36);
        int valueX = width - 7 - HudStyle.strongWidth(value);
        if (settings.showNames) {
            String name = piece.item().method_7960() ? ArmorWidget.emptySlot(piece.slot()) : piece.displayName();
            HudStyle.text(g, HudStyle.fit(name, valueX - 32), 27, y + 3, -921101, settings.shadow);
        }
        int color = durability.low(settings.warnPercent) ? -39568 : -4802625;
        HudStyle.strong(g, value, valueX, y + 3, color, settings.shadow);
        if (durability.damageable()) {
            HudStyle.progress(g, "armor." + String.valueOf(piece.slot()), 27, y + 18, width - 34, durability.fraction(), color == -39568 ? color : -6907228);
        }
    }

    private static String slotKey(class_1304 slot) {
        return switch (slot) {
            case class_1304.field_6169 -> "head";
            case class_1304.field_6174 -> "chest";
            case class_1304.field_6172 -> "legs";
            default -> "feet";
        };
    }

    private static String emptySlot(class_1304 slot) {
        return switch (slot) {
            case class_1304.field_6169 -> HudStyle.tr("\u041d\u0435\u0442 \u0448\u043b\u0435\u043c\u0430", "No helmet");
            case class_1304.field_6174 -> HudStyle.tr("\u041d\u0435\u0442 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a\u0430", "No chestplate");
            case class_1304.field_6172 -> HudStyle.tr("\u041d\u0435\u0442 \u043f\u043e\u043d\u043e\u0436\u0435\u0439", "No leggings");
            default -> HudStyle.tr("\u041d\u0435\u0442 \u0431\u043e\u0442\u0438\u043d\u043e\u043a", "No boots");
        };
    }

    private static List<InventorySnapshot.ArmorPiece> previewArmor() {
        class_1799 helmet = new class_1799((class_1935)class_1802.field_22027);
        class_1799 chest = new class_1799((class_1935)class_1802.field_22028);
        class_1799 legs = new class_1799((class_1935)class_1802.field_22029);
        class_1799 boots = new class_1799((class_1935)class_1802.field_22030);
        helmet.method_7974(Math.round((float)helmet.method_7936() * 0.16f));
        chest.method_7974(Math.round((float)chest.method_7936() * 0.83f));
        legs.method_7974(Math.round((float)legs.method_7936() * 0.42f));
        boots.method_7974(Math.round((float)boots.method_7936() * 0.07f));
        return List.of(InventorySnapshot.armorPiece(class_1304.field_6169, helmet), InventorySnapshot.armorPiece(class_1304.field_6174, chest), InventorySnapshot.armorPiece(class_1304.field_6172, legs), InventorySnapshot.armorPiece(class_1304.field_6166, boots));
    }
}

