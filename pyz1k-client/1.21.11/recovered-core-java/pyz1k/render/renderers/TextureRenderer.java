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
import org.joml.Matrix3x2fc;
import pyz1k.render.builders.states.QuadColorState;
import pyz1k.render.builders.states.QuadRadiusState;
import pyz1k.render.builders.states.SizeState;
import pyz1k.render.renderers.Renderer;
import pyz1k.render.renderers.pipeline.PipelineRegistry;

public record TextureRenderer(SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness, float rotation, GpuTextureView texture) implements Renderer
{
    private static class_8030 createBounds(class_332 context, float x, float y, float width, float height) {
        return new class_8030((int)Math.floor(x), (int)Math.floor(y), (int)Math.ceil(width), (int)Math.ceil(height)).method_71523((Matrix3x2fc)context.method_51448());
    }

    @Override
    public void render(double x, double y, class_332 context) {
        if (PipelineRegistry.TEXTURE_PIPELINE != null && this.texture != null) {
            float boundsHeight;
            float boundsWidth;
            float y4;
            float x4;
            float y3;
            float x3;
            float y2;
            float x2;
            float y1;
            float x1;
            Matrix3x2f matrices = new Matrix3x2f((Matrix3x2fc)context.method_51448());
            class_11231 textureSetup = class_11231.method_70900((GpuTextureView)this.texture, (class_12137)RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
            float width = this.size.width();
            float height = this.size.height();
            float centerX = (float)x + width * 0.5f;
            float centerY = (float)y + height * 0.5f;
            if (Math.abs(this.rotation) < 0.001f) {
                x1 = (float)x;
                y1 = (float)y;
                x2 = (float)x;
                y2 = (float)y + height;
                x3 = (float)x + width;
                y3 = (float)y + height;
                x4 = (float)x + width;
                y4 = (float)y;
                boundsWidth = width;
                boundsHeight = height;
            } else {
                float radians = (float)Math.toRadians(this.rotation);
                float cos = (float)Math.cos(radians);
                float sin = (float)Math.sin(radians);
                float halfWidth = width * 0.5f;
                float halfHeight = height * 0.5f;
                x1 = centerX - halfWidth * cos + halfHeight * sin;
                y1 = centerY - halfWidth * sin - halfHeight * cos;
                x2 = centerX - halfWidth * cos - halfHeight * sin;
                y2 = centerY - halfWidth * sin + halfHeight * cos;
                x3 = centerX + halfWidth * cos - halfHeight * sin;
                y3 = centerY + halfWidth * sin + halfHeight * cos;
                x4 = centerX + halfWidth * cos + halfHeight * sin;
                y4 = centerY + halfWidth * sin - halfHeight * cos;
                boundsWidth = Math.abs(width * cos) + Math.abs(height * sin);
                boundsHeight = Math.abs(width * sin) + Math.abs(height * cos);
            }
            int color1 = Renderer.toAbgr(this.color.color1());
            int color2 = Renderer.toAbgr(this.color.color2());
            int color3 = Renderer.toAbgr(this.color.color3());
            int color4 = Renderer.toAbgr(this.color.color4());
            SimpleGuiRenderState state = new SimpleGuiRenderState(PipelineRegistry.TEXTURE_PIPELINE, textureSetup, context, TextureRenderer.createBounds(context, centerX - boundsWidth * 0.5f, centerY - boundsHeight * 0.5f, boundsWidth, boundsHeight), buffer -> {
                DirectVertexConsumer consumer = new DirectVertexConsumer((class_287)buffer, false);
                consumer.method_70815((Matrix3x2fc)matrices, x1, y1).method_22913(0.0f, 0.0f).method_39415(color1).method_22913(width, height).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(this.smoothness, 0.0f);
                consumer.method_70815((Matrix3x2fc)matrices, x2, y2).method_22913(0.0f, 1.0f).method_39415(color2).method_22913(width, height).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(this.smoothness, 0.0f);
                consumer.method_70815((Matrix3x2fc)matrices, x3, y3).method_22913(1.0f, 1.0f).method_39415(color3).method_22913(width, height).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(this.smoothness, 0.0f);
                consumer.method_70815((Matrix3x2fc)matrices, x4, y4).method_22913(1.0f, 0.0f).method_39415(color4).method_22913(width, height).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(this.smoothness, 0.0f);
            });
            ((DrawContextAccessor)context).getState().method_70919((class_11244)state);
        }
    }
}

