/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.MouseHandler
 *  net.minecraft.client.input.MouseButtonInfo
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.bloodhelper.client.modules.ZoomModule;

@Mixin(value={MouseHandler.class})
abstract class ZoomMouseMixin {
    ZoomMouseMixin() {
    }

    @Inject(method={"onButton"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$zoomMousePress(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (ZoomModule.mouseAction(Minecraft.getInstance(), window, info, action)) {
            ci.cancel();
        }
    }

    @Inject(method={"onScroll"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$zoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ZoomModule.scroll(Minecraft.getInstance(), window, vertical)) {
            ci.cancel();
        }
    }

    @ModifyArgs(method={"turnPlayer"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;turn(DD)V")})
    private void bloodhelper$zoomSensitivity(Args args) {
        double multiplier = ZoomModule.sensitivityScale();
        if (multiplier == 1.0) {
            return;
        }
        args.set(0, (Object)((Double)args.get(0) * multiplier));
        args.set(1, (Object)((Double)args.get(1) * multiplier));
    }
}

