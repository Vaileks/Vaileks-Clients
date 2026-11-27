/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.PlayerMetrics;
import ru.bloodhelper.client.skins.VeritiAnimation;
import ru.bloodhelper.client.visuals.AbilityTracker;

@Mixin(value={LivingEntity.class})
public class LegendaryDamageMixin {
    @Inject(method={"handleDamageEvent"}, at={@At(value="HEAD")})
    private void bloodhelper$onDamage(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)this;
        if (self.level().isClientSide()) {
            AbilityTracker.onDamage((Entity)self, source);
            VeritiAnimation.damage((Entity)self, source);
            PlayerMetrics.damage((Entity)self, source);
        }
    }
}

