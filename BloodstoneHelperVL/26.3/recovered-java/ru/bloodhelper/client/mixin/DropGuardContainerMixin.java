/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.DropGuardModule;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={AbstractContainerScreen.class})
abstract class DropGuardContainerMixin {
    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    DropGuardContainerMixin() {
    }

    @Inject(method={"slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$guardSlot(Slot slot, int slotId, int button, ContainerInput type, CallbackInfo ci) {
        if (SlotLockModule.block(this.menu, slot, button, type) || DropGuardModule.blockContainerDrop(Minecraft.getInstance(), this.menu, slot, slotId, button, type)) {
            ci.cancel();
        }
    }
}

