/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11659
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  net.minecraft.class_4587
 *  net.minecraft.class_759
 *  net.minecraft.class_811
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import net.minecraft.class_11659;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_759;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.handrender.HandRenderManager;
import pyz1k.handrender.HandRenderRule;

@Mixin(value={class_759.class})
public class ItemInHandRendererMixin {
    @Inject(method={"method_3233"}, at={@At(value="HEAD")})
    private void pyz1k$pushHandTransform(class_1309 entity, class_1799 stack, class_811 displayContext, class_4587 poseStack, class_11659 collector, int light, CallbackInfo ci) {
        poseStack.method_22903();
        if (displayContext != class_811.field_4321 && displayContext != class_811.field_4322) {
            return;
        }
        HandRenderRule rule = HandRenderManager.getInstance().findMatching(stack);
        if (rule == null) {
            return;
        }
        float scale = HandRenderManager.clampScale(rule.handScale);
        poseStack.method_46416(HandRenderManager.clampOffset(rule.offsetX), HandRenderManager.clampOffset(rule.offsetY), HandRenderManager.clampOffset(rule.offsetZ));
        poseStack.method_22905(scale, scale, scale);
    }

    @Inject(method={"method_3233"}, at={@At(value="RETURN")})
    private void pyz1k$popHandTransform(class_1309 entity, class_1799 stack, class_811 displayContext, class_4587 poseStack, class_11659 collector, int light, CallbackInfo ci) {
        poseStack.method_22909();
    }
}

