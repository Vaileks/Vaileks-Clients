/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_757
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.visuals.MotionBlurRenderer;

@Mixin(value={class_757.class})
abstract class MotionBlurGameRendererMixin {
    MotionBlurGameRendererMixin() {
    }

    @Inject(method={"method_3192"}, at={@At(value="HEAD")})
    private void bloodhelper$resetMotionHistory(CallbackInfo ci) {
        MotionBlurRenderer.beginFrame();
    }

    @Inject(method={"method_3192"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_757;method_3188(Lnet/minecraft/class_9779;)V", shift=At.Shift.AFTER)})
    private void bloodhelper$blurWorld(CallbackInfo ci) {
        MotionBlurRenderer.compositeWorld();
    }

    @Inject(method={"close"}, at={@At(value="HEAD")})
    private void bloodhelper$closeMotionHistory(CallbackInfo ci) {
        MotionBlurRenderer.close();
    }
}

