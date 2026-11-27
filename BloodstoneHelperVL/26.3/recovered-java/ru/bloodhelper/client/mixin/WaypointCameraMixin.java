/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.state.GameRenderState
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.hud.WorldWaypoints;
import ru.bloodhelper.client.modules.LunarNameTags;

@Mixin(value={GameRenderer.class})
public abstract class WaypointCameraMixin {
    @Shadow
    @Final
    private GameRenderState gameRenderState;

    @Inject(method={"extractCamera"}, at={@At(value="HEAD")})
    private void bloodhelper$beginNames(CallbackInfo ci) {
        LunarNameTags.beginFrame();
    }

    @Inject(method={"extractCamera"}, at={@At(value="TAIL")})
    private void bloodhelper$currentCamera(CallbackInfo ci) {
        CameraRenderState source = this.gameRenderState.levelRenderState.cameraRenderState;
        if (source != null) {
            WorldWaypoints.capture(source, source.viewRotationMatrix, source.projectionMatrix);
        }
    }
}

