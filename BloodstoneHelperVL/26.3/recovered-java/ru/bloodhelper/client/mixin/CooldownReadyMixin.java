/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemCooldowns
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.CombatStateModule;
import ru.bloodhelper.client.modules.CooldownReadyModule;

@Mixin(value={ItemCooldowns.class})
abstract class CooldownReadyMixin {
    @Unique
    private boolean bloodhelper$naturalCooldownTick;

    CooldownReadyMixin() {
    }

    @Inject(method={"addCooldown(Lnet/minecraft/resources/Identifier;I)V"}, at={@At(value="RETURN")})
    private void bloodhelper$observeCooldown(Identifier group, int duration, CallbackInfo ci) {
        CooldownReadyModule.started((ItemCooldowns)this, group, duration);
        CombatStateModule.cooldownAdded((ItemCooldowns)this, group, duration);
    }

    @Inject(method={"removeCooldown"}, at={@At(value="HEAD")})
    private void bloodhelper$cancelCooldown(Identifier group, CallbackInfo ci) {
        CooldownReadyModule.cancelled((ItemCooldowns)this, group);
        CombatStateModule.cooldownRemoved((ItemCooldowns)this, group);
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void bloodhelper$beginTick(CallbackInfo ci) {
        this.bloodhelper$naturalCooldownTick = true;
    }

    @Inject(method={"tick"}, at={@At(value="RETURN")})
    private void bloodhelper$endTick(CallbackInfo ci) {
        this.bloodhelper$naturalCooldownTick = false;
    }

    @Inject(method={"onCooldownEnded"}, at={@At(value="HEAD")})
    private void bloodhelper$naturalEnd(Identifier group, CallbackInfo ci) {
        if (this.bloodhelper$naturalCooldownTick) {
            CooldownReadyModule.naturallyEnded((ItemCooldowns)this, group);
        }
    }
}

