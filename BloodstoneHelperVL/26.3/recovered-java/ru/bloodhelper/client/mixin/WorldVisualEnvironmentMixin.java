package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.SpatialAttributeInterpolator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={EnvironmentAttributeProbe.class})
abstract class WorldVisualEnvironmentMixin {
    @Shadow
    @Final
    private SpatialAttributeInterpolator biomeInterpolator;

    WorldVisualEnvironmentMixin() {
    }

    @ModifyReturnValue(method={"getValue"}, at={@At(value="RETURN")})
    private <T> T bloodhelper$cameraVisuals(T original, EnvironmentAttribute<T> attribute, float partialTick) {
        return WorldVisuals.cameraAttribute((EnvironmentAttributeProbe)this, attribute, partialTick, this.biomeInterpolator, original);
    }
}

