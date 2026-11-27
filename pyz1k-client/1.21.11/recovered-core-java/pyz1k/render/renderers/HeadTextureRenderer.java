/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_12137
 *  net.minecraft.class_287
 *  net.minecraft.class_332
 *  net.minecraft.class_8030
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix3x2fc
 */
package pyz1k.render.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import me.x150.renderer.mixin.DrawContextAccessor;
import me.x150.renderer.render.SimpleGuiRenderState;
import me.x150.renderer.util.DirectVertexConsumer;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_12137;
import net.minecraft.class_287;
import net.minecraft.class_332;
import net.minecraft.class_8030;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;
import pyz1k.render.builders.states.QuadColorState;
import pyz1k.render.builders.states.QuadRadiusState;
import pyz1k.render.builders.states.SizeState;
import pyz1k.render.renderers.Renderer;
import pyz1k.render.renderers.pipeline.PipelineRegistry;

public record HeadTextureRenderer(SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness, float u, float v, float texWidth, float texHeight, GpuTextureView texture) implements Renderer
{
    private static class_8030 createBounds(class_332 c, float x, float y, float w, float h) {
        Matrix3x2fStack mat = c.method_51448();
        return new class_8030((int)Math.floor(x), (int)Math.floor(y), (int)Math.ceil(w), (int)Math.ceil(h)).method_71523((Matrix3x2fc)mat);
    }

    @Override
    public void render(double x, double y, class_332 ctx) {
        if (PipelineRegistry.HEAD_TEXTURE_PIPELINE != null && this.texture != null) {
            Matrix3x2f guiMatrices = new Matrix3x2f((Matrix3x2fc)ctx.method_51448());
            class_11231 textureSetup = class_11231.method_70900((GpuTextureView)this.texture, (class_12137)RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
            float u0 = 0.0f;
            float v0 = 0.0f;
            float u1 = 1.0f;
            float v1 = 1.0f;
            if (this.texWidth > 0.0f && this.texHeight > 0.0f && this.texture != null) {
                int texW = this.texture.getWidth(0);
                int texH = this.texture.getHeight(0);
                if (texW > 0 && texH > 0) {
                    u0 = this.u / (float)texW;
                    v0 = this.v / (float)texH;
                    u1 = (this.u + this.texWidth) / (float)texW;
                    v1 = (this.v + this.texHeight) / (float)texH;
                }
            }
            float fu0 = u0;
            float fv0 = v0;
            float fu1 = u1;
            float fv1 = v1;
            SimpleGuiRenderState state = new SimpleGuiRenderState(PipelineRegistry.HEAD_TEXTURE_PIPELINE, textureSetup, ctx, HeadTextureRenderer.createBounds(ctx, (float)x, (float)y, this.size.width(), this.size.height()), buffer -> {
                DirectVertexConsumer dvc = new DirectVertexConsumer((class_287)buffer, false);
                int c1 = Renderer.toAbgr(this.color.color1());
                int c2 = Renderer.toAbgr(this.color.color2());
                int c3 = Renderer.toAbgr(this.color.color3());
                int c4 = Renderer.toAbgr(this.color.color4());
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)x, (float)y).method_22913(0.0f, 0.0f).method_39415(c1).method_22913(this.size.width(), this.size.height()).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(fu0, fv0).method_22913(this.smoothness, 0.0f);
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)x, (float)(y + (double)this.size.height())).method_22913(0.0f, 1.0f).method_39415(c2).method_22913(this.size.width(), this.size.height()).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(fu0, fv1).method_22913(this.smoothness, 0.0f);
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)(x + (double)this.size.width()), (float)(y + (double)this.size.height())).method_22913(1.0f, 1.0f).method_39415(c3).method_22913(this.size.width(), this.size.height()).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(fu1, fv1).method_22913(this.smoothness, 0.0f);
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)(x + (double)this.size.width()), (float)y).method_22913(1.0f, 0.0f).method_39415(c4).method_22913(this.size.width(), this.size.height()).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(fu1, fv0).method_22913(this.smoothness, 0.0f);
            });
            ((DrawContextAccessor)ctx).getState().method_70919((class_11244)state);
        }
    }
}

