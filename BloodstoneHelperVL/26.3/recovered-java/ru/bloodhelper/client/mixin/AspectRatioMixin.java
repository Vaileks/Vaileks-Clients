/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.bloodhelper.client.BloodHelperClient;

@Mixin(value={Camera.class})
abstract class AspectRatioMixin {
    AspectRatioMixin() {
    }

    @ModifyArgs(method={"setupPerspective"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/Projection;setupPerspective(FFFFF)V")})
    private void bloodhelper$aspect(Args args) {
        float width = ((Float)args.get(3)).floatValue();
        float height = ((Float)args.get(4)).floatValue();
        if (height > 0.0f) {
            args.set(3, (Object)Float.valueOf(BloodHelperClient.config().aspectRatio.ratio(width / height) * height));
        }
    }
}

