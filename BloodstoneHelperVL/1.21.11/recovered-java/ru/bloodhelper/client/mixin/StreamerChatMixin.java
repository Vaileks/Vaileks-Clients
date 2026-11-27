/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_338
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_338;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={class_338.class})
public abstract class StreamerChatMixin {
    @Inject(method={"method_23677"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hideChat(CallbackInfoReturnable<Boolean> callback) {
        if (StreamerMode.hideChat()) {
            callback.setReturnValue((Object)true);
        }
    }
}

