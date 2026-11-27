/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_329
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_329;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={class_329.class})
public abstract class StreamerOverlayMixin {
    @Inject(method={"method_55803", "method_55804", "method_55801", "method_55800", "method_70837"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hideServerOverlays(CallbackInfo callback) {
        if (StreamerMode.hideServerOverlays()) {
            callback.cancel();
        }
    }
}

