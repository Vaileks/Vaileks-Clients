/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ItemLike
 */
package ru.bloodhelper.client.hud;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import ru.bloodhelper.client.config.BlastLeggingsSettings;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.CombatStateModule;

public final class BlastLeggingsWidget {
    private BlastLeggingsWidget() {
    }

    public static int blastLevel(ItemStack item) {
        if (item.isEmpty()) {
            return 0;
        }
        for (Object2IntMap.Entry entry : item.getEnchantments().entrySet()) {
            if (!((Holder)entry.getKey()).is(Enchantments.BLAST_PROTECTION)) continue;
            return entry.getIntValue();
        }
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(GuiGraphicsExtractor g, ClientConfig config, boolean preview) {
        int level;
        Minecraft mc = Minecraft.getInstance();
        BlastLeggingsSettings settings = config.combatHud.blastLeggings;
        if (!preview && (mc.gui.screen() != null || mc.player == null || !mc.player.isAlive() || settings.onlyInCombat && !CombatStateModule.active())) {
            return;
        }
        ItemStack item = preview ? new ItemStack((ItemLike)Items.DIAMOND_LEGGINGS) : mc.player.getItemBySlot(EquipmentSlot.LEGS);
        int n = level = preview ? 4 : BlastLeggingsWidget.blastLevel(item);
        if (level <= 0) {
            return;
        }
        if (preview) {
            item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, (Object)true);
        }
        HudRenderer.begin(g, "blastLeggings", settings.position, "LEFT", false, 20, settings.showLevel ? 29 : 20);
        try {
            g.item(item, 2, 2);
            if (settings.showLevel) {
                String text = Integer.toString(level);
                HudStyle.label(g, text, (20 - HudStyle.labelWidth(text)) / 2, 20, -921101, true);
            }
        }
        finally {
            g.pose().popMatrix();
        }
    }
}

