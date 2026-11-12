/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.alchemy.PotionContents
 */
package ru.bloodhelper.client.modules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import ru.bloodhelper.client.config.DroppedItemFilterSettings;

public final class DroppedItemFilter {
    private static volatile RenderRules rules = new RenderRules(false, Set.of(), Map.of());

    private DroppedItemFilter() {
    }

    public static Rules compile(boolean optimizerEnabled, DroppedItemFilterSettings settings) {
        HashSet<String> items = new HashSet<String>();
        HashSet<String> potions = new HashSet<String>();
        for (DroppedItemFilterSettings.Entry entry : settings.selected) {
            if (entry.potionId.isEmpty()) {
                items.add(entry.itemId);
                continue;
            }
            potions.add(entry.key());
        }
        return new Rules(optimizerEnabled && settings.enabled, Set.copyOf(items), Set.copyOf(potions));
    }

    public static void configure(boolean optimizerEnabled, DroppedItemFilterSettings settings) {
        if (!optimizerEnabled || !settings.enabled || settings.selected.isEmpty()) {
            rules = new RenderRules(false, Set.of(), Map.of());
            return;
        }
        HashSet<Item> items = new HashSet<Item>();
        HashMap<Item, Set> potions = new HashMap<Item, Set>();
        for (DroppedItemFilterSettings.Entry entry : settings.selected) {
            Item item2 = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse((String)entry.itemId));
            if (item2 == null || item2 == Items.AIR) continue;
            if (entry.potionId.isEmpty()) {
                items.add(item2);
                continue;
            }
            potions.computeIfAbsent(item2, ignored -> new HashSet()).add(Identifier.parse((String)entry.potionId));
        }
        potions.replaceAll((item, variants) -> Set.copyOf(variants));
        rules = new RenderRules(true, Set.copyOf(items), Map.copyOf(potions));
    }

    public static boolean hides(ItemStack stack) {
        RenderRules current = rules;
        if (!current.enabled || stack == null || stack.isEmpty()) {
            return false;
        }
        if (current.items.contains(stack.getItem())) {
            return true;
        }
        Set<Identifier> variants = current.potions.get(stack.getItem());
        if (variants == null) {
            return false;
        }
        PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.potion().flatMap(holder -> holder.unwrapKey()).map(key -> variants.contains(key.identifier())).orElse(false) != false;
    }

    public record Rules(boolean enabled, Set<String> items, Set<String> potions) {
        public boolean hides(String item, String potion) {
            return this.enabled && (this.items.contains(item) || potion != null && this.potions.contains(item + "\u0000" + potion));
        }
    }

    private record RenderRules(boolean enabled, Set<Item> items, Map<Item, Set<Identifier>> potions) {
    }
}

