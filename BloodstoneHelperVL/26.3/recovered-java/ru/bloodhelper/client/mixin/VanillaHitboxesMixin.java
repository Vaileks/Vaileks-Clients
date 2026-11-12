/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.BloodHelperClient;

@Mixin(value={EntityHitboxDebugRenderer.class})
abstract class VanillaHitboxesMixin {
    VanillaHitboxesMixin() {
    }

    @Inject(method={"emitGizmos"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$replaceVanilla(CallbackInfo ci) {
        if (BloodHelperClient.config().hitboxes.enabled) {
            ci.cancel();
        }
    }
}

