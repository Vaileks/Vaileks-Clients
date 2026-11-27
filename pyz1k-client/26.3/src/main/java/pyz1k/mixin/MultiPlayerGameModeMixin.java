/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package pyz1k.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyz1k.abilitybind.AbilityBindManager;
import pyz1k.killeffects.KillEffectEngine;

@Mixin(value={MultiPlayerGameMode.class})
public abstract class MultiPlayerGameModeMixin {
    @Inject(method={"attack"}, at={@At(value="HEAD")})
    private void pyz1k$recordKillEffectTarget(Player player, Entity target, CallbackInfo ci) {
        KillEffectEngine.recordAttack(target);
    }

    @Inject(method={"useItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$abilityBindUseItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        this.pyz1k$blockProtectedMainHand(player, hand, cir);
    }

    @Inject(method={"useItemOn"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$abilityBindUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        this.pyz1k$blockProtectedMainHand((Player)player, hand, cir);
        if (!cir.isCancelled()) {
            KillEffectEngine.recordExplosiveUse((Player)player, hand, hit);
        }
    }

    @Inject(method={"interact"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$abilityBindInteract(Player player, Entity target, EntityHitResult hit, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        this.pyz1k$blockProtectedMainHand(player, hand, cir);
    }

    private void pyz1k$blockProtectedMainHand(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (AbilityBindManager.getInstance().shouldBlockUse(player, hand)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}

