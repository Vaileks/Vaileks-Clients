/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.inventory.tooltip.TooltipComponent
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 */
package ru.bloodhelper.client.modules;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ShulkerPreviewSettings;
import ru.bloodhelper.client.modules.ShulkerPreviewLayout;

public final class ShulkerPreview {
    private static ItemContainerContents cachedContents;
    private static Payload cachedPayload;
    private static Optional<TooltipComponent> cachedTooltip;

    private ShulkerPreview() {
    }

    public static Optional<TooltipComponent> forTooltip(ItemStack stack, Optional<TooltipComponent> original, int screenWidth, int screenHeight) {
        List<Object> slots;
        BlockItem item;
        Item item2;
        ShulkerPreviewSettings settings = BloodHelperClient.config().shulkerPreview;
        if (!(settings.enabled && original.isEmpty() && !stack.isEmpty() && (item2 = stack.getItem()) instanceof BlockItem && (item = (BlockItem)item2).getBlock() instanceof ShulkerBoxBlock)) {
            return original;
        }
        ItemContainerContents contents = (ItemContainerContents)stack.getOrDefault(DataComponents.CONTAINER, (Object)ItemContainerContents.EMPTY);
        ShulkerPreviewLayout layout = ShulkerPreviewLayout.fit(settings.scale, screenWidth, screenHeight);
        if (contents == cachedContents && cachedPayload != null && cachedPayload.layout().equals(layout)) {
            return cachedTooltip;
        }
        if (contents == cachedContents && cachedPayload != null) {
            slots = cachedPayload.slots();
        } else {
            NonNullList copied = NonNullList.withSize((int)27, (Object)ItemStack.EMPTY);
            contents.copyInto(copied);
            slots = List.copyOf(copied);
        }
        cachedContents = contents;
        cachedPayload = new Payload(slots, layout);
        cachedTooltip = Optional.of(cachedPayload);
        return cachedTooltip;
    }

    static {
        cachedTooltip = Optional.empty();
    }

    public record Payload(List<ItemStack> slots, ShulkerPreviewLayout layout) implements TooltipComponent
    {
    }
}

