/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11910
 *  net.minecraft.class_310
 *  net.minecraft.class_312
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_11910;
import net.minecraft.class_310;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.ZoomModule;

@Mixin(value={class_312.class})
abstract class ZoomMouseMixin {
    ZoomMouseMixin() {
    }

    @Inject(method={"method_1601"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$zoomMousePress(long window, class_11910 info, int action, CallbackInfo ci) {
        if (ZoomModule.mouseAction(class_310.method_1551(), window, info, action)) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1598"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$zoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ZoomModule.scroll(class_310.method_1551(), window, vertical)) {
            ci.cancel();
        }
    }
}

