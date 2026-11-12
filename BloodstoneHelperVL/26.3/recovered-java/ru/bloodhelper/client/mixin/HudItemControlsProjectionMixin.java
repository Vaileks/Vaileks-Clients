/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GameRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.bloodhelper.client.modules.HudItemControlsModule;

@Mixin(value={GameRenderer.class})
abstract class HudItemControlsProjectionMixin {
    HudItemControlsProjectionMixin() {
    }

    @ModifyArg(method={"render3dHud"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/Projection;setupPerspective(FFFFF)V")}, index=2)
    private float bloodhelper$hudProjection(float fov) {
        HudItemControlsModule.hudFov = fov;
        return fov;
    }
}

