/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl
 *  net.minecraft.world.phys.HitResult
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.extensions.pvp.mixin;

import pyz1k.extensions.pvp.Core;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ThrownEnderpearl.class})
abstract class PearlMixin {
    PearlMixin() {
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void pvp$track(CallbackInfo ci) {
        Core.pearlTick((ThrownEnderpearl)(Object)this);
    }

    @Inject(method={"onHit"}, at={@At(value="HEAD")})
    private void pvp$land(HitResult hit, CallbackInfo ci) {
        Core.pearlLand((ThrownEnderpearl)(Object)this);
    }
}

