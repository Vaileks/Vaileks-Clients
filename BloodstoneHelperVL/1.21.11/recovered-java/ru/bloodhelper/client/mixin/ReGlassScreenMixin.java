/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.ReGlassGui;
import ru.bloodhelper.client.gui.ReGlassResources;

@Mixin(value={class_437.class})
public abstract class ReGlassScreenMixin {
    @Inject(method={"method_57737"}, at={@At(value="HEAD")}, cancellable=true)
    private static void bloodhelper$background(class_332 g, class_2960 id, int x, int y, float u, float v, int w, int h, CallbackInfo ci) {
        if (ReGlassGui.active() && ReGlassResources.vanillaTexture(id) && !ReGlassResources.customContainer() && ReGlassGui.panel(g, x, y, w, h, 0, -1071634636)) {
            ci.cancel();
        }
    }
}

