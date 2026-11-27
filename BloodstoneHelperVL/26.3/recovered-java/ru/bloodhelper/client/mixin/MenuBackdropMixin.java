/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.renderer.GameRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.MenuBackdrop;
import ru.bloodhelper.client.gui.ReGlassGui;

@Mixin(value={GameRenderer.class})
public abstract class MenuBackdropMixin {
    @Inject(method={"extract"}, at={@At(value="HEAD")})
    private void bloodhelper$beginBackdropFrame(DeltaTracker delta, boolean renderLevel, CallbackInfo ci) {
        MenuBackdrop.beginFrame();
        LiquidGlass.beginExtraction();
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/render/GuiRenderer;render()V")})
    private void bloodhelper$captureCompletedWorld(CallbackInfo ci) {
        LiquidGlass.captureScheduledWorld();
    }

    @Inject(method={"processBlurEffect"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$blurCompletedHud(CallbackInfo ci) {
        if (ReGlassGui.active() || MenuBackdrop.processBlur()) {
            ci.cancel();
        }
    }
}

