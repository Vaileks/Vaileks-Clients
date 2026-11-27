package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={CreativeModeInventoryScreen.class})
public interface CreativeDestroySlotAccessor {
    @Accessor(value="destroyItemSlot")
    public Slot bloodhelper$destroySlot();
}

