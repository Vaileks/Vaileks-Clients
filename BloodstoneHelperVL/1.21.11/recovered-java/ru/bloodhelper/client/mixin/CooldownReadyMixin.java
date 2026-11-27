/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1796
 *  net.minecraft.class_2960
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1796;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.CombatStateModule;
import ru.bloodhelper.client.modules.CooldownReadyModule;

@Mixin(value={class_1796.class})
abstract class CooldownReadyMixin {
    @Unique
    private boolean bloodhelper$naturalCooldownTick;

    CooldownReadyMixin() {
    }

    @Inject(method={"method_7906(Lnet/minecraft/class_2960;I)V"}, at={@At(value="RETURN")})
    private void bloodhelper$observeCooldown(class_2960 group, int duration, CallbackInfo ci) {
        CooldownReadyModule.started((class_1796)this, group, duration);
        CombatStateModule.cooldownAdded((class_1796)this, group, duration);
    }

    @Inject(method={"method_7900"}, at={@At(value="HEAD")})
    private void bloodhelper$cancelCooldown(class_2960 group, CallbackInfo ci) {
        CooldownReadyModule.cancelled((class_1796)this, group);
        CombatStateModule.cooldownRemoved((class_1796)this, group);
    }

    @Inject(method={"method_7903"}, at={@At(value="HEAD")})
    private void bloodhelper$beginTick(CallbackInfo ci) {
        this.bloodhelper$naturalCooldownTick = true;
    }

    @Inject(method={"method_7903"}, at={@At(value="RETURN")})
    private void bloodhelper$endTick(CallbackInfo ci) {
        this.bloodhelper$naturalCooldownTick = false;
    }

    @Inject(method={"method_7901"}, at={@At(value="HEAD")})
    private void bloodhelper$naturalEnd(class_2960 group, CallbackInfo ci) {
        if (this.bloodhelper$naturalCooldownTick) {
            CooldownReadyModule.naturallyEnded((class_1796)this, group);
        }
    }
}

