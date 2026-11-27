/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemCooldowns
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package pyz1k.utilites.cooldown;

import java.util.Collections;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import pyz1k.mixin.CooldownInstanceAccessor;
import pyz1k.mixin.ItemCooldownsAccessor;

public final class CooldownHudUtil {
    public static final Identifier PVP_COOLDOWN = Identifier.parse((String)"minecraft:trident");

    private CooldownHudUtil() {
    }

    public static Map<Identifier, Object> getCooldowns(Minecraft mc) {
        if (mc == null || mc.player == null) {
            return Collections.emptyMap();
        }
        ItemCooldowns cooldowns = mc.player.getCooldowns();
        return ((ItemCooldownsAccessor)cooldowns).pyz1k$getCooldowns();
    }

    public static float getRemainingSeconds(Minecraft mc, Object cooldownInstance) {
        if (mc == null || mc.player == null || cooldownInstance == null) {
            return 0.0f;
        }
        ItemCooldownsAccessor cooldowns = (ItemCooldownsAccessor)mc.player.getCooldowns();
        CooldownInstanceAccessor cooldown = (CooldownInstanceAccessor)cooldownInstance;
        return Math.max(0.0f, (float)(cooldown.pyz1k$getEndTime() - cooldowns.pyz1k$getTickCount()) / 20.0f);
    }

    public static ItemStack findStackForCooldown(Minecraft mc, Identifier cooldownId) {
        if (mc == null || mc.player == null || cooldownId == null) {
            return ItemStack.EMPTY;
        }
        for (ItemStack stack : mc.player.getInventory().getNonEquipmentItems()) {
            if (stack.isEmpty() || !stack.getComponents().toString().contains(cooldownId.toString())) continue;
            return stack;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(cooldownId).orElse(null);
        return item == null ? ItemStack.EMPTY : pyz1k.compat.PreviewStacks.of((ItemLike)item);
    }
}

