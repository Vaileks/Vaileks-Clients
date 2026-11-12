/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4604
 *  net.minecraft.class_863
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_4604;
import net.minecraft.class_863;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.VisualHitboxes;

@Mixin(value={class_863.class})
public class VisualHitboxesMixin {
    @Inject(method={"method_23099"}, at={@At(value="TAIL")})
    private void bloodhelper$hitboxes(class_4604 frustum, double x, double y, double z, float partial, CallbackInfo ci) {
        VisualHitboxes.render(frustum, partial);
    }
}

