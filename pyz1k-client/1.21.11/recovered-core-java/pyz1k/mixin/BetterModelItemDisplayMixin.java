/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10038
 *  net.minecraft.class_8113$class_8122
 *  net.minecraft.class_8138$class_8140
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import net.minecraft.class_10038;
import net.minecraft.class_8113;
import net.minecraft.class_8138;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.killeffects.ServerKillEffectFilter;

@Mixin(value={class_8138.class_8140.class})
public abstract class BetterModelItemDisplayMixin {
    @Inject(method={"method_62408(Lnet/minecraft/class_8113$class_8122;Lnet/minecraft/class_10038;F)V"}, at={@At(value="TAIL")})
    private void pyz1k$hideServerKillEffects(class_8113.class_8122 entity, class_10038 state, float partialTick, CallbackInfo ci) {
        if (ServerKillEffectFilter.shouldHide(entity)) {
            state.field_55313.method_65605();
        }
    }
}

