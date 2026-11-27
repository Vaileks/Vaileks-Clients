/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_757
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.bloodhelper.client.modules.HudItemControlsModule;

@Mixin(value={class_757.class})
abstract class HudItemControlsProjectionMixin {
    HudItemControlsProjectionMixin() {
    }

    @ModifyArg(method={"method_3188"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_11279;method_71095(IIF)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"), index=2)
    private float bloodhelper$hudProjection(float fov) {
        HudItemControlsModule.hudFov = fov;
        return fov;
    }
}

