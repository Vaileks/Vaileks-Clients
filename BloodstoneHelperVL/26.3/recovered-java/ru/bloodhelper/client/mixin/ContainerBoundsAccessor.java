package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={AbstractContainerScreen.class})
public interface ContainerBoundsAccessor {
    @Invoker(value="slotClicked")
    public void bloodhelper$clickSlot(Slot var1, int var2, int var3, ContainerInput var4);

    @Accessor(value="leftPos")
    public int bloodhelper$left();

    @Accessor(value="topPos")
    public int bloodhelper$top();

    @Accessor(value="imageWidth")
    public int bloodhelper$width();

    @Accessor(value="imageHeight")
    public int bloodhelper$height();
}

