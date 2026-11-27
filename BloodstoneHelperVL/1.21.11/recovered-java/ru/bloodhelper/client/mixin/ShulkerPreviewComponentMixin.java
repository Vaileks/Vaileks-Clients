/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_5632
 *  net.minecraft.class_5684
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_5632;
import net.minecraft.class_5684;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.gui.ShulkerPreviewTooltip;
import ru.bloodhelper.client.modules.ShulkerPreview;

@Mixin(value={class_5684.class})
public interface ShulkerPreviewComponentMixin {
    @Inject(method={"method_32663(Lnet/minecraft/class_5632;)Lnet/minecraft/class_5684;"}, at={@At(value="HEAD")}, cancellable=true)
    private static void bloodhelper$createShulkerPreview(class_5632 data, CallbackInfoReturnable<class_5684> result) {
        if (data instanceof ShulkerPreview.Payload) {
            ShulkerPreview.Payload payload = (ShulkerPreview.Payload)data;
            result.setReturnValue((Object)new ShulkerPreviewTooltip(payload));
        }
    }
}

