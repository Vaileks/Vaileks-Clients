/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 */
package pyz1k.extensions;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public enum BloodSwordForm {
    SWORD("Меч", -1525686),
    TRANSITION("Переход", -1021087),
    MACE("Булава", -1754827),
    HARPOON("Гарпун", -34722);

    public final String label;
    public final int color;

    private BloodSwordForm(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public static BloodSwordForm from(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        Identifier model = (Identifier)stack.get(DataComponents.ITEM_MODEL);
        if (model == null || !model.getNamespace().equals("bloodstone") || !model.getPath().matches("tools/bloodsword/[^/]+/(sword|block|mace|harpoon)")) {
            return null;
        }
        return switch (model.getPath().substring(model.getPath().lastIndexOf(47) + 1)) {
            case "sword" -> SWORD;
            case "block" -> TRANSITION;
            case "mace" -> MACE;
            case "harpoon" -> HARPOON;
            default -> null;
        };
    }
}

