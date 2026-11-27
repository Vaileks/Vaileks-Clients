/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.client.player.LocalPlayer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.DropGuardModule;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={MultiPlayerGameMode.class})
abstract class DropGuardPlayerMixin {
    DropGuardPlayerMixin() {
    }

    @Inject(method={"dropItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$guardHeldItem(LocalPlayer player, boolean wholeStack, CallbackInfo ci) {
        if (player == Minecraft.getInstance().player && (SlotLockModule.selectedLocked() || DropGuardModule.blockSelectedDrop(Minecraft.getInstance(), wholeStack))) {
            ci.cancel();
        }
    }
}

