/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package pyz1k.extensions.pvp.mixin;

import pyz1k.extensions.pvp.Core;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={MultiPlayerGameMode.class})
abstract class UseMixin {
    @Unique
    private ItemStack pvp$before = ItemStack.EMPTY;

    UseMixin() {
    }

    @Inject(method={"useItem"}, at={@At(value="HEAD")})
    private void pvp$before(Player p, InteractionHand h, CallbackInfoReturnable<InteractionResult> ci) {
        this.pvp$before = Core.recording() ? p.getItemInHand(h).copy() : ItemStack.EMPTY;
    }

    @Inject(method={"useItem"}, at={@At(value="RETURN")})
    private void pvp$after(Player p, InteractionHand h, CallbackInfoReturnable<InteractionResult> ci) {
        if (!this.pvp$before.isEmpty() && ((InteractionResult)ci.getReturnValue()).consumesAction()) {
            Core.used(this.pvp$before, false);
        }
    }
}

