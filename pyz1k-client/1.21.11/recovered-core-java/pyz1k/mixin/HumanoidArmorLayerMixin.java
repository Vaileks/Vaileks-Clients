/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10034
 *  net.minecraft.class_10055
 *  net.minecraft.class_11659
 *  net.minecraft.class_1304
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_970
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import net.minecraft.class_10034;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.cosmetics.CosmeticsManager;

@Mixin(value={class_970.class})
public abstract class HumanoidArmorLayerMixin {
    @Inject(method={"method_4169"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$hideLocalHelmetForCosmetic(class_4587 poseStack, class_11659 collector, class_1799 stack, class_1304 slot, int packedLight, class_10034 state, CallbackInfo ci) {
        boolean cosmeticVisible;
        if (!(state instanceof class_10055)) {
            return;
        }
        class_10055 avatarState = (class_10055)state;
        class_310 minecraft = class_310.method_1551();
        if (minecraft.field_1724 == null || avatarState.field_53528 != minecraft.field_1724.method_5628()) {
            return;
        }
        CosmeticsManager cosmetics = CosmeticsManager.getInstance();
        if (cosmetics.isCustomModelAvailable()) {
            ci.cancel();
            return;
        }
        if (slot != class_1304.field_6169) {
            return;
        }
        if (cosmetics.getSelected() == null) {
            return;
        }
        boolean bl = cosmeticVisible = !cosmetics.isOnlyWithHelmet() || !stack.method_7960();
        if (cosmeticVisible) {
            ci.cancel();
        }
    }
}

