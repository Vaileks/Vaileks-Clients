/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10042
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_811
 *  net.minecraft.class_922
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import net.minecraft.class_10042;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_811;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.cosmetics.CosmeticsManager;

@Mixin(value={class_922.class})
public abstract class LivingEntityRendererMixin {
    @Inject(method={"method_62355(Lnet/minecraft/class_1309;Lnet/minecraft/class_10042;F)V"}, at={@At(value="TAIL")})
    private void pyz1k$applyLocalCosmeticHat(class_1309 entity, class_10042 state, float partialTick, CallbackInfo ci) {
        class_310 minecraft = class_310.method_1551();
        if (minecraft.field_1724 == null || entity != minecraft.field_1724) {
            return;
        }
        class_1799 stack = CosmeticsManager.getInstance().getSelectedStack();
        if (stack.method_7960()) {
            return;
        }
        state.field_55315 = null;
        state.field_55316 = null;
        minecraft.method_65386().method_65597(state.field_53467, stack, class_811.field_4316, entity);
    }
}

