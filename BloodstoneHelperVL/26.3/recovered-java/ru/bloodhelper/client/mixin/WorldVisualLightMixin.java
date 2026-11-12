/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.LightmapRenderStateExtractor
 *  net.minecraft.client.renderer.state.LightmapRenderState
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.config.WorldVisualSettings;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={LightmapRenderStateExtractor.class})
abstract class WorldVisualLightMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private boolean needsUpdate;
    @Unique
    private float bloodhelper$lastBrightness = Float.NaN;

    WorldVisualLightMixin() {
    }

    @Inject(method={"extract"}, at={@At(value="HEAD")})
    private void bloodhelper$refreshBrightness(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        if (this.minecraft.level == null || this.minecraft.player == null) {
            return;
        }
        float strength = WorldVisuals.settings().fullBrightStrength();
        if (Float.compare(strength, this.bloodhelper$lastBrightness) != 0) {
            this.needsUpdate = true;
            this.bloodhelper$lastBrightness = strength;
        }
    }

    @Inject(method={"extract"}, at={@At(value="TAIL")})
    private void bloodhelper$fullBright(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        WorldVisualSettings settings = WorldVisuals.settings();
        if (!state.needsUpdate || settings.fullBrightStrength() <= 0.0f) {
            return;
        }
        Vector3fc ambient = state.ambientColor;
        state.ambientColor = new Vector3f(settings.ambientLight(ambient.x()), settings.ambientLight(ambient.y()), settings.ambientLight(ambient.z()));
    }
}

