/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.class_1937
 *  net.minecraft.class_1959$class_1963
 *  net.minecraft.class_2338
 *  net.minecraft.class_638
 *  net.minecraft.class_9976
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_638;
import net.minecraft.class_9976;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={class_9976.class})
abstract class WorldVisualWeatherMixin {
    WorldVisualWeatherMixin() {
    }

    @ModifyExpressionValue(method={"method_62315"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1937;method_8430(F)F")})
    private float bloodhelper$rainColumns(float original, class_1937 level) {
        return WorldVisuals.rain(level, original);
    }

    @ModifyExpressionValue(method={"method_62319"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_638;method_8430(F)F")})
    private float bloodhelper$rainSplashes(float original, class_638 level) {
        return WorldVisuals.rain((class_1937)level, original);
    }

    @ModifyReturnValue(method={"method_62317"}, at={@At(value="RETURN")})
    private class_1959.class_1963 bloodhelper$precipitationTexture(class_1959.class_1963 original, class_1937 level, class_2338 pos) {
        class_1959.class_1963 replacement = WorldVisuals.precipitation(level, pos);
        return replacement == null ? original : replacement;
    }
}

