/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.debug.DebugRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.VisualHitboxes;

@Mixin(value={DebugRenderer.class})
public class VisualHitboxesMixin {
    @Inject(method={"emitGizmos"}, at={@At(value="TAIL")})
    private void bloodhelper$hitboxes(Frustum frustum, double x, double y, double z, float partial, CallbackInfo ci) {
        VisualHitboxes.render(frustum, partial);
    }
}

