/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.SkyRenderer
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={SkyRenderer.class})
abstract class WorldVisualSkyWeatherMixin {
    WorldVisualSkyWeatherMixin() {
    }

    @ModifyExpressionValue(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")})
    private float bloodhelper$rainBrightness(float original, ClientLevel level) {
        return WorldVisuals.rain((Level)level, original);
    }
}

