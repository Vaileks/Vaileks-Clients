/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.WeatherEffectRenderer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.biome.Biome$Precipitation
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={WeatherEffectRenderer.class})
abstract class WorldVisualWeatherMixin {
    WorldVisualWeatherMixin() {
    }

    @ModifyExpressionValue(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")})
    private float bloodhelper$rainColumns(float original, ClientLevel level) {
        return WorldVisuals.rain((Level)level, original);
    }

    @WrapOperation(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;")})
    private Biome.Precipitation bloodhelper$precipitation(ClientLevel level, BlockPos pos, Operation<Biome.Precipitation> original) {
        Biome.Precipitation replacement = WorldVisuals.precipitation((Level)level, pos);
        return replacement == null ? (Biome.Precipitation)original.call(new Object[]{level, pos}) : replacement;
    }
}

