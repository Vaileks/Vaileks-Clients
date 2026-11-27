/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.LoggerFactory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.BloodHelperClient;

@Pseudo
@Mixin(targets={"dev.tr7zw.exordium.components.BufferInstance"}, remap=false)
public abstract class ExordiumGlassCompatMixin {
    @Unique
    private boolean bloodhelper$glassCompatibilityReported;

    @Inject(method={"enabled()Z"}, at={@At(value="HEAD")}, cancellable=true, require=0, remap=false)
    private void bloodhelper$liveGlass(CallbackInfoReturnable<Boolean> callback) {
        if (!"LIQUID_GLASS".equals(BloodHelperClient.config().surfaceStyle)) {
            return;
        }
        callback.setReturnValue((Object)false);
        if (!this.bloodhelper$glassCompatibilityReported) {
            this.bloodhelper$glassCompatibilityReported = true;
            LoggerFactory.getLogger((String)"BloodHelper/LiquidGlass").info("Using live HUD rendering for Liquid Glass with Exordium; its transparent HUD cache cannot provide the scene background");
        }
    }
}

