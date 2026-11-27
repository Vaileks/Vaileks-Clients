/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_1007
 *  net.minecraft.class_11659
 *  net.minecraft.class_11890
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_811
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package pyz1k.mixin;

import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11659;
import net.minecraft.class_11890;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyz1k.cosmetics.ChickenMesh;
import pyz1k.cosmetics.CosmeticsManager;
import pyz1k.cosmetics.KirbyMesh;

@Mixin(value={class_1007.class})
public abstract class AvatarRendererMixin {
    @Inject(method={"method_62604(Lnet/minecraft/class_11890;Lnet/minecraft/class_10055;F)V"}, at={@At(value="TAIL")})
    private void pyz1k$applyLocalCosmeticHat(class_11890 entity, class_10055 state, float partialTick, CallbackInfo ci) {
        boolean hasHelmet;
        class_310 minecraft = class_310.method_1551();
        if (minecraft.field_1724 == null || entity != minecraft.field_1724) {
            return;
        }
        CosmeticsManager cosmetics = CosmeticsManager.getInstance();
        if (cosmetics.isCustomModelAvailable()) {
            return;
        }
        class_1799 cosmetic = cosmetics.getSelectedStack();
        if (cosmetic.method_7960()) {
            return;
        }
        boolean bl = hasHelmet = !state.field_55309.method_7960();
        if (cosmetics.isOnlyWithHelmet() && !hasHelmet) {
            return;
        }
        state.field_55315 = null;
        state.field_55316 = null;
        minecraft.method_65386().method_65597(state.field_53467, cosmetic, class_811.field_4316, (class_1309)entity);
    }

    @Inject(method={"method_62606(Lnet/minecraft/class_10055;)Z"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void pyz1k$hideLayersForChicken(class_10055 state, CallbackInfoReturnable<Boolean> result) {
        class_310 minecraft = class_310.method_1551();
        if (minecraft.field_1724 != null && state.field_53528 == minecraft.field_1724.method_5628() && CosmeticsManager.getInstance().isCustomModelAvailable()) {
            result.setReturnValue((Object)false);
        }
    }

    @Inject(method={"method_4220(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_2960;Z)V"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void pyz1k$chickenRightHand(class_4587 pose, class_11659 collector, int light, class_2960 skin, boolean sleeve, CallbackInfo ci) {
        if (CosmeticsManager.getInstance().isKirbySelected() && KirbyMesh.isAvailable()) {
            KirbyMesh.submitArm(pose, collector, light, true);
            ci.cancel();
        } else if (CosmeticsManager.getInstance().isChickenSelected() && ChickenMesh.isAvailable()) {
            ChickenMesh.submitArm(pose, collector, light, true);
            ci.cancel();
        }
    }

    @Inject(method={"method_4221(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_2960;Z)V"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void pyz1k$chickenLeftHand(class_4587 pose, class_11659 collector, int light, class_2960 skin, boolean sleeve, CallbackInfo ci) {
        if (CosmeticsManager.getInstance().isKirbySelected() && KirbyMesh.isAvailable()) {
            KirbyMesh.submitArm(pose, collector, light, false);
            ci.cancel();
        } else if (CosmeticsManager.getInstance().isChickenSelected() && ChickenMesh.isAvailable()) {
            ChickenMesh.submitArm(pose, collector, light, false);
            ci.cancel();
        }
    }
}

