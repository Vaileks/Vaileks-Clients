/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={MultiPlayerGameMode.class})
abstract class SlotLockTransactionMixin {
    SlotLockTransactionMixin() {
    }

    @Inject(method={"handleContainerInput"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$lockTransaction(int menuId, int slotId, int button, ContainerInput type, Player player, CallbackInfo ci) {
        Slot slot;
        if (player != Minecraft.getInstance().player || menuId != player.containerMenu.containerId) {
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        Slot slot2 = slot = slotId >= 0 && slotId < menu.slots.size() ? menu.getSlot(slotId) : null;
        if (SlotLockModule.block(menu, slot, button, type)) {
            ci.cancel();
        }
    }
}

