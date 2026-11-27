/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  net.minecraft.class_4184
 *  net.minecraft.class_761
 *  net.minecraft.class_9779
 *  net.minecraft.class_9922
 *  org.joml.Matrix4f
 *  org.joml.Vector4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.class_4184;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.hud.WorldWaypoints;
import ru.bloodhelper.client.visuals.WorldProjection;

@Mixin(value={class_761.class})
public abstract class WaypointProjectionMixin {
    @Inject(method={"method_22710"}, at={@At(value="HEAD")})
    private void bloodhelper$waypointProjection(class_9922 allocator, class_9779 delta, boolean outline, class_4184 camera, Matrix4f view, Matrix4f projection, Matrix4f cullingProjection, GpuBufferSlice fog, Vector4f fogColor, boolean sky, CallbackInfo ci) {
        WorldWaypoints.capture(camera, view, projection);
        WorldProjection.capture(camera, view, projection);
    }
}

