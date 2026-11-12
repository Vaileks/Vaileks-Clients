/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.Camera
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={AtmosphericFogEnvironment.class})
abstract class WorldVisualFogWeatherMixin {
    WorldVisualFogWeatherMixin() {
    }

    @ModifyExpressionValue(method={"getBaseColor"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")})
    private float bloodhelper$rainFog(float original, ClientLevel level) {
        return WorldVisuals.rain((Level)level, original);
    }

    @ModifyExpressionValue(method={"updateRainFogState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")})
    private float bloodhelper$rainFogDistance(float original, Camera camera, ClientLevel level) {
        return WorldVisuals.rain((Level)level, original);
    }

    @ModifyExpressionValue(method={"getBaseColor"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F")})
    private float bloodhelper$thunderFog(float original, ClientLevel level) {
        return WorldVisuals.thunder((Level)level, original);
    }
}

