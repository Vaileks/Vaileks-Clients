/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_1937
 *  net.minecraft.class_638
 *  net.minecraft.class_9975
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_1937;
import net.minecraft.class_638;
import net.minecraft.class_9975;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={class_9975.class})
abstract class WorldVisualSkyWeatherMixin {
    WorldVisualSkyWeatherMixin() {
    }

    @ModifyExpressionValue(method={"method_74926"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_638;method_8430(F)F")})
    private float bloodhelper$rainBrightness(float original, class_638 level) {
        return WorldVisuals.rain((class_1937)level, original);
    }
}

