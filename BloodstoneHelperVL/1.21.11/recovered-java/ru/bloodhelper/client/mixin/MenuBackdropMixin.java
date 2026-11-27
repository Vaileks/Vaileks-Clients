/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_757
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.MenuBackdrop;
import ru.bloodhelper.client.gui.ReGlassGui;

@Mixin(value={class_757.class})
public abstract class MenuBackdropMixin {
    @Inject(method={"method_3192"}, at={@At(value="HEAD")})
    private void bloodhelper$beginBackdropFrame(class_9779 delta, boolean renderLevel, CallbackInfo ci) {
        MenuBackdrop.beginFrame();
    }

    @Inject(method={"method_57796"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$blurCompletedHud(CallbackInfo ci) {
        if (ReGlassGui.active() || MenuBackdrop.processBlur()) {
            ci.cancel();
        }
    }
}

