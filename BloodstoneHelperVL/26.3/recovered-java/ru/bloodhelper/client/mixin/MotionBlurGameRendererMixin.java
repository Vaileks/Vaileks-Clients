/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GameRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.visuals.MotionBlurRenderer;

@Mixin(value={GameRenderer.class})
abstract class MotionBlurGameRendererMixin {
    MotionBlurGameRendererMixin() {
    }

    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void bloodhelper$resetMotionHistory(CallbackInfo ci) {
        MotionBlurRenderer.beginFrame();
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/GameRenderer;renderLevel()V", shift=At.Shift.AFTER)})
    private void bloodhelper$blurWorld(CallbackInfo ci) {
        MotionBlurRenderer.compositeWorld();
    }

    @Inject(method={"close"}, at={@At(value="HEAD")})
    private void bloodhelper$closeMotionHistory(CallbackInfo ci) {
        MotionBlurRenderer.close();
    }
}

