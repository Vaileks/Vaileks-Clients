package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={ClientLevel.class})
abstract class WorldVisualWeatherParticlesMixin {
    WorldVisualWeatherParticlesMixin() {
    }

    @ModifyExpressionValue(method={"tickWeatherEffects"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F")})
    private float bloodhelper$rainSplashes(float original) {
        return WorldVisuals.rain((Level)((ClientLevel)this), original);
    }

    @WrapOperation(method={"tickWeatherEffects"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;")})
    private Biome.Precipitation bloodhelper$splashType(ClientLevel level, BlockPos pos, Operation<Biome.Precipitation> original) {
        Biome.Precipitation replacement = WorldVisuals.precipitation((Level)level, pos);
        return replacement == null ? (Biome.Precipitation)original.call(new Object[]{level, pos}) : replacement;
    }
}

