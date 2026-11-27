/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.client.Camera
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.modules.ZoomModule;

@Mixin(value={Camera.class})
abstract class ZoomCameraMixin {
    ZoomCameraMixin() {
    }

    @ModifyReturnValue(method={"calculateFov"}, at={@At(value="RETURN")})
    private float bloodhelper$worldFov(float original) {
        return ZoomModule.scaleFov(original);
    }

    @ModifyReturnValue(method={"calculateHudFov"}, at={@At(value="RETURN")})
    private float bloodhelper$handFov(float original) {
        return BloodHelperClient.config().zoom.affectHandFov ? ZoomModule.scaleFov(original) : original;
    }
}

