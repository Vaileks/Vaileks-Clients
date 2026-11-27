/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_3965
 *  net.minecraft.class_3966
 *  net.minecraft.class_636
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package pyz1k.mixin;

import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_636;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyz1k.abilitybind.AbilityBindManager;
import pyz1k.killeffects.KillEffectEngine;

@Mixin(value={class_636.class})
public abstract class MultiPlayerGameModeMixin {
    @Inject(method={"method_2918"}, at={@At(value="HEAD")})
    private void pyz1k$recordKillEffectTarget(class_1657 player, class_1297 target, CallbackInfo ci) {
        KillEffectEngine.recordAttack(target);
    }

    @Inject(method={"method_2919"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$abilityBindUseItem(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        this.pyz1k$blockProtectedMainHand(player, hand, cir);
    }

    @Inject(method={"method_2896"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$abilityBindUseItemOn(class_746 player, class_1268 hand, class_3965 hit, CallbackInfoReturnable<class_1269> cir) {
        this.pyz1k$blockProtectedMainHand((class_1657)player, hand, cir);
        if (!cir.isCancelled()) {
            KillEffectEngine.recordExplosiveUse((class_1657)player, hand, hit);
        }
    }

    @Inject(method={"method_2905"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$abilityBindInteract(class_1657 player, class_1297 target, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        this.pyz1k$blockProtectedMainHand(player, hand, cir);
    }

    @Inject(method={"method_2917"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$abilityBindInteractAt(class_1657 player, class_1297 target, class_3966 hit, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        this.pyz1k$blockProtectedMainHand(player, hand, cir);
    }

    private void pyz1k$blockProtectedMainHand(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        if (AbilityBindManager.getInstance().shouldBlockUse(player, hand)) {
            cir.setReturnValue((Object)class_1269.field_5811);
        }
    }
}

