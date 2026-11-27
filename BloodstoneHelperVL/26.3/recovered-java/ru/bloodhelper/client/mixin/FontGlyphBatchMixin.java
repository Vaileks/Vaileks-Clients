/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  com.mojang.renderpearl.api.textures.GpuSampler
 *  com.mojang.renderpearl.api.textures.GpuTextureView
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.state.gui.BlitRenderState
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.resources.Identifier
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.gen.Invoker
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package ru.bloodhelper.client.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.bloodhelper.client.gui.FontBatchAccess;
import ru.bloodhelper.client.gui.FontGlyphBatchState;
import ru.bloodhelper.client.gui.FontGlyphRun;

@Mixin(value={GuiGraphicsExtractor.class})
public abstract class FontGlyphBatchMixin
implements FontBatchAccess {
    @Unique
    private FontGlyphRun bloodhelper$pendingGlyphs;
    @Unique
    private float bloodhelper$fontX;
    @Unique
    private float bloodhelper$fontY;
    @Unique
    private float bloodhelper$fontSize;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void bloodhelper$submitGlyphRun(FontGlyphRun run, Identifier texture, float x, float y, float size, int color) {
        if (this.bloodhelper$pendingGlyphs != null) {
            throw new IllegalStateException("Nested custom font submission");
        }
        int left = run.left(x, size);
        int top = run.top(y, size);
        int width = run.right(x, size) - left;
        int height = run.bottom(y, size) - top;
        if (width <= 0 || height <= 0) {
            return;
        }
        this.bloodhelper$pendingGlyphs = run;
        this.bloodhelper$fontX = x;
        this.bloodhelper$fontY = y;
        this.bloodhelper$fontSize = size;
        try {
            AbstractTexture atlas = Minecraft.getInstance().getTextureManager().getTexture(texture);
            this.bloodhelper$invokeSubmitBlit(RenderPipelines.GUI_TEXTURED, atlas.getTextureView(), atlas.getSampler(), left, top, left + width, top + height, 0.0f, 1.0f, 0.0f, 1.0f, color);
        }
        finally {
            this.bloodhelper$pendingGlyphs = null;
        }
    }

    @Invoker(value="innerBlit")
    public abstract void bloodhelper$invokeSubmitBlit(RenderPipeline var1, GpuTextureView var2, GpuSampler var3, int var4, int var5, int var6, int var7, float var8, float var9, float var10, float var11, int var12);

    @ModifyArg(method={"innerBlit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuSampler;IIIIFFFFI)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/state/gui/GuiRenderState;addGuiElement(Lnet/minecraft/client/renderer/state/gui/GuiElementRenderState;)V")}, index=0)
    private GuiElementRenderState bloodhelper$batchOwnGlyphs(GuiElementRenderState original) {
        if (this.bloodhelper$pendingGlyphs == null) {
            return original;
        }
        if (!(original instanceof BlitRenderState)) {
            throw new IllegalStateException("Unexpected custom-font blit context");
        }
        BlitRenderState blit = (BlitRenderState)original;
        return new FontGlyphBatchState(blit, this.bloodhelper$pendingGlyphs, this.bloodhelper$fontX, this.bloodhelper$fontY, this.bloodhelper$fontSize);
    }
}

