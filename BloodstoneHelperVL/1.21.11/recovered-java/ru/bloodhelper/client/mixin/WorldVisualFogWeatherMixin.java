/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_11398
 *  net.minecraft.class_1937
 *  net.minecraft.class_4184
 *  net.minecraft.class_638
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_11398;
import net.minecraft.class_1937;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={class_11398.class})
abstract class WorldVisualFogWeatherMixin {
    WorldVisualFogWeatherMixin() {
    }

    @ModifyExpressionValue(method={"method_71654"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_638;method_8430(F)F")})
    private float bloodhelper$rainFog(float original, class_638 level) {
        return WorldVisuals.rain((class_1937)level, original);
    }

    @ModifyExpressionValue(method={"method_76304"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_638;method_8430(F)F")})
    private float bloodhelper$rainFogDistance(float original, class_4184 camera, class_638 level) {
        return WorldVisuals.rain((class_1937)level, original);
    }

    @ModifyExpressionValue(method={"method_71654"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_638;method_8478(F)F")})
    private float bloodhelper$thunderFog(float original, class_638 level) {
        return WorldVisuals.thunder((class_1937)level, original);
    }
}

