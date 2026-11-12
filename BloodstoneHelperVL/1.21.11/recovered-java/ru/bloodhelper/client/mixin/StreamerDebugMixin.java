/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_340
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_340;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={class_340.class})
public abstract class StreamerDebugMixin {
    @Inject(method={"method_1846"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hideDebug(CallbackInfo callback) {
        if (StreamerMode.hideDebug()) {
            callback.cancel();
        }
    }
}

