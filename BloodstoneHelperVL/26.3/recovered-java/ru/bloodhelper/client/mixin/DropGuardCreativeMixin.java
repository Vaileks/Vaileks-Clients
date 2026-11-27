/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.DropGuardModule;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={CreativeModeInventoryScreen.class})
abstract class DropGuardCreativeMixin {
    DropGuardCreativeMixin() {
    }

    @Inject(method={"slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$guardCreativeSlot(Slot slot, int slotId, int button, ContainerInput type, CallbackInfo ci) {
        CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen)this;
        if (SlotLockModule.block(screen.getMenu(), slot, button, type) || DropGuardModule.blockContainerDrop(Minecraft.getInstance(), screen.getMenu(), slot, slotId, button, type)) {
            ci.cancel();
        }
    }
}

