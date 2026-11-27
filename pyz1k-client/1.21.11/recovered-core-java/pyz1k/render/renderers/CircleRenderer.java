/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_287
 *  net.minecraft.class_332
 *  net.minecraft.class_8030
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix3x2fc
 */
package pyz1k.render.renderers;

import me.x150.renderer.mixin.DrawContextAccessor;
import me.x150.renderer.render.SimpleGuiRenderState;
import me.x150.renderer.util.DirectVertexConsumer;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_287;
import net.minecraft.class_332;
import net.minecraft.class_8030;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;
import pyz1k.render.renderers.Renderer;
import pyz1k.render.renderers.pipeline.PipelineRegistry;

public record CircleRenderer(float diameter, int color, float smoothness) implements Renderer
{
    private static class_8030 createBounds(class_332 c, float x, float y, float w, float h) {
        Matrix3x2fStack mat = c.method_51448();
        return new class_8030((int)Math.floor(x), (int)Math.floor(y), (int)Math.ceil(w), (int)Math.ceil(h)).method_71523((Matrix3x2fc)mat);
    }

    @Override
    public void render(double x, double y, class_332 ctx) {
        if (PipelineRegistry.CIRCLE_PIPELINE != null) {
            Matrix3x2f guiMatrices = new Matrix3x2f((Matrix3x2fc)ctx.method_51448());
            int vertexColor = Renderer.toAbgr(this.color);
            SimpleGuiRenderState state = new SimpleGuiRenderState(PipelineRegistry.CIRCLE_PIPELINE, class_11231.method_70899(), ctx, CircleRenderer.createBounds(ctx, (float)x, (float)y, this.diameter, this.diameter), buffer -> {
                DirectVertexConsumer dvc = new DirectVertexConsumer((class_287)buffer, false);
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)x, (float)y).method_22913(0.0f, 0.0f).method_39415(vertexColor).method_22913(this.diameter, this.diameter).method_22913(0.0f, 0.0f).method_22913(0.0f, 0.0f).method_22913(this.smoothness, 0.0f);
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)x, (float)(y + (double)this.diameter)).method_22913(0.0f, 1.0f).method_39415(vertexColor).method_22913(this.diameter, this.diameter).method_22913(0.0f, 0.0f).method_22913(0.0f, 0.0f).method_22913(this.smoothness, 0.0f);
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)(x + (double)this.diameter), (float)(y + (double)this.diameter)).method_22913(1.0f, 1.0f).method_39415(vertexColor).method_22913(this.diameter, this.diameter).method_22913(0.0f, 0.0f).method_22913(0.0f, 0.0f).method_22913(this.smoothness, 0.0f);
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)(x + (double)this.diameter), (float)y).method_22913(1.0f, 0.0f).method_39415(vertexColor).method_22913(this.diameter, this.diameter).method_22913(0.0f, 0.0f).method_22913(0.0f, 0.0f).method_22913(this.smoothness, 0.0f);
            });
            ((DrawContextAccessor)ctx).getState().method_70919((class_11244)state);
        }
    }
}

