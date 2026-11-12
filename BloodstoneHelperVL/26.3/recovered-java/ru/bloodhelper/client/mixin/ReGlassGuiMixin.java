/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.resources.Identifier
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.ReGlassGui;

@Mixin(value={GuiGraphicsExtractor.class})
public abstract class ReGlassGuiMixin {
    @Inject(method={"blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$glassSprite(RenderPipeline pipeline, Identifier id, int x, int y, int w, int h, int color, CallbackInfo ci) {
        if (ReGlassGui.sprite((GuiGraphicsExtractor)this, id, x, y, w, h, color)) {
            ci.cancel();
        }
    }

    @Inject(method={"blit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIIII)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$glassTexture(RenderPipeline pipeline, Identifier id, int x, int y, float u, float v, int w, int h, int sw, int sh, int tw, int th, int color, CallbackInfo ci) {
        if (ReGlassGui.texture((GuiGraphicsExtractor)this, id, x, y, w, h)) {
            ci.cancel();
        }
    }

    @Inject(method={"fill(IIIII)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$glassFill(int x1, int y1, int x2, int y2, int color, CallbackInfo ci) {
        if (ReGlassGui.fill((GuiGraphicsExtractor)this, x1, y1, x2, y2, color)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method={"text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V", "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V"}, at={@At(value="HEAD")}, argsOnly=true, ordinal=2)
    private int bloodhelper$labelColor(int color) {
        return ReGlassGui.labelColor(color);
    }
}

