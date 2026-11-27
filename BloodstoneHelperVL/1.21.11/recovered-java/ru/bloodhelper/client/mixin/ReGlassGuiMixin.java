/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.ReGlassGui;

@Mixin(value={class_332.class})
public abstract class ReGlassGuiMixin {
    @Inject(method={"method_52707(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIIII)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$glassSprite(RenderPipeline pipeline, class_2960 id, int x, int y, int w, int h, int color, CallbackInfo ci) {
        if (ReGlassGui.sprite((class_332)this, id, x, y, w, h, color)) {
            ci.cancel();
        }
    }

    @Inject(method={"method_25293(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIFFIIIIIII)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$glassTexture(RenderPipeline pipeline, class_2960 id, int x, int y, float u, float v, int w, int h, int sw, int sh, int tw, int th, int color, CallbackInfo ci) {
        if (ReGlassGui.texture((class_332)this, id, x, y, w, h)) {
            ci.cancel();
        }
    }

    @Inject(method={"method_25294(IIIII)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$glassFill(int x1, int y1, int x2, int y2, int color, CallbackInfo ci) {
        if (ReGlassGui.fill((class_332)this, x1, y1, x2, y2, color)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method={"method_51433(Lnet/minecraft/class_327;Ljava/lang/String;IIIZ)V", "method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)V", "method_51430(Lnet/minecraft/class_327;Lnet/minecraft/class_5481;IIIZ)V"}, at=@At(value="HEAD"), argsOnly=true, ordinal=2)
    private int bloodhelper$labelColor(int color) {
        return ReGlassGui.labelColor(color);
    }
}

