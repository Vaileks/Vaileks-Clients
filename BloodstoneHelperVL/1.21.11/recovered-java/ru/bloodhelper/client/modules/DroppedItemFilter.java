/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1844
 *  net.minecraft.class_2960
 *  net.minecraft.class_7923
 *  net.minecraft.class_9334
 */
package ru.bloodhelper.client.modules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import net.minecraft.class_9334;
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
        HashSet<class_1792> items = new HashSet<class_1792>();
        HashMap<class_1792, Set> potions = new HashMap<class_1792, Set>();
        for (DroppedItemFilterSettings.Entry entry : settings.selected) {
            class_1792 item2 = (class_1792)class_7923.field_41178.method_63535(class_2960.method_60654((String)entry.itemId));
            if (item2 == null || item2 == class_1802.field_8162) continue;
            if (entry.potionId.isEmpty()) {
                items.add(item2);
                continue;
            }
            potions.computeIfAbsent(item2, ignored -> new HashSet()).add(class_2960.method_60654((String)entry.potionId));
        }
        potions.replaceAll((item, variants) -> Set.copyOf(variants));
        rules = new RenderRules(true, Set.copyOf(items), Map.copyOf(potions));
    }

    public static boolean hides(class_1799 stack) {
        RenderRules current = rules;
        if (!current.enabled || stack == null || stack.method_7960()) {
            return false;
        }
        if (current.items.contains(stack.method_7909())) {
            return true;
        }
        Set<class_2960> variants = current.potions.get(stack.method_7909());
        if (variants == null) {
            return false;
        }
        class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
        return contents != null && contents.comp_2378().flatMap(holder -> holder.method_40230()).map(key -> variants.contains(key.method_29177())).orElse(false) != false;
    }

    public record Rules(boolean enabled, Set<String> items, Set<String> potions) {
        public boolean hides(String item, String potion) {
            return this.enabled && (this.items.contains(item) || potion != null && this.potions.contains(item + "\u0000" + potion));
        }
    }

    private record RenderRules(boolean enabled, Set<class_1792> items, Map<class_1792, Set<class_2960>> potions) {
    }
}

