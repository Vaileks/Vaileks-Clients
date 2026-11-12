/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.class_4184
 *  net.minecraft.class_757
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.class_4184;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.modules.ZoomModule;

@Mixin(value={class_757.class})
abstract class ZoomGameRendererMixin {
    ZoomGameRendererMixin() {
    }

    @ModifyReturnValue(method={"method_3196"}, at={@At(value="RETURN")})
    private float bloodhelper$zoomFov(float original, class_4184 camera, float partialTick, boolean worldProjection) {
        return worldProjection || BloodHelperClient.config().zoom.affectHandFov ? ZoomModule.scaleFov(original) : original;
    }

    @ModifyExpressionValue(method={"method_3186"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_11902;method_74204(F)F")})
    private float bloodhelper$relativeViewBobbing(float original) {
        return (float)((double)original * ZoomModule.bobbingScale());
    }
}

