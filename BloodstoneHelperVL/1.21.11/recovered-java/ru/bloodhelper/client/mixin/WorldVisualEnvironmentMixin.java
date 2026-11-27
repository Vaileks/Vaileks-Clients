/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.class_12197
 *  net.minecraft.class_12202
 *  net.minecraft.class_12211
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.class_12197;
import net.minecraft.class_12202;
import net.minecraft.class_12211;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={class_12202.class})
abstract class WorldVisualEnvironmentMixin {
    @Shadow
    @Final
    private class_12211 field_63734;

    WorldVisualEnvironmentMixin() {
    }

    @ModifyReturnValue(method={"method_75689"}, at={@At(value="RETURN")})
    private <T> T bloodhelper$cameraVisuals(T original, class_12197<T> attribute, float partialTick) {
        return WorldVisuals.cameraAttribute((class_12202)this, attribute, partialTick, this.field_63734, original);
    }
}

