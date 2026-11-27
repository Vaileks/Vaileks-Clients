/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_765
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_310;
import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={class_765.class})
abstract class WorldVisualLightMixin {
    @Shadow
    @Final
    private class_310 field_4137;
    @Shadow
    private boolean field_4135;
    @Unique
    private float bloodhelper$lastBrightness = Float.NaN;

    WorldVisualLightMixin() {
    }

    @Inject(method={"method_3313"}, at={@At(value="HEAD")})
    private void bloodhelper$refreshBrightness(float partialTick, CallbackInfo ci) {
        if (this.field_4137.field_1687 == null || this.field_4137.field_1724 == null) {
            return;
        }
        float strength = WorldVisuals.settings().fullBrightStrength();
        if (Float.compare(strength, this.bloodhelper$lastBrightness) != 0) {
            this.field_4135 = true;
            this.bloodhelper$lastBrightness = strength;
        }
    }

    @ModifyArg(method={"method_3313"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;", ordinal=3), index=0)
    private float bloodhelper$fullBright(float original) {
        return WorldVisuals.settings().nightVision(original);
    }
}

