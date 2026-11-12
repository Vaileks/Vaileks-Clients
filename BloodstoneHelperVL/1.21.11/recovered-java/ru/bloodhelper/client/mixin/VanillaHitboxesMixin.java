/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_12155
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_12155;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.BloodHelperClient;

@Mixin(value={class_12155.class})
abstract class VanillaHitboxesMixin {
    VanillaHitboxesMixin() {
    }

    @Inject(method={"method_23109"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$replaceVanilla(CallbackInfo ci) {
        if (BloodHelperClient.config().hitboxes.enabled) {
            ci.cancel();
        }
    }
}

