/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.BlockHitResult
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.visuals.OrbitalTracker;

@Mixin(value={MultiPlayerGameMode.class})
public abstract class OrbitalUseMixin {
    @Inject(method={"useItem"}, at={@At(value="HEAD")})
    private void bloodhelper$stabAir(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        OrbitalTracker.onUse(player, hand);
    }

    @Inject(method={"useItemOn"}, at={@At(value="HEAD")})
    private void bloodhelper$stabBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> ci) {
        OrbitalTracker.onUse((Player)player, hand);
    }
}

