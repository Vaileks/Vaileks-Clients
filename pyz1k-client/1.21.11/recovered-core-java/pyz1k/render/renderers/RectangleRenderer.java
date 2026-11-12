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
import pyz1k.render.builders.states.QuadColorState;
import pyz1k.render.builders.states.QuadRadiusState;
import pyz1k.render.builders.states.SizeState;
import pyz1k.render.renderers.Renderer;
import pyz1k.render.renderers.pipeline.PipelineRegistry;

public record RectangleRenderer(SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness) implements Renderer
{
    private static class_8030 createBounds(class_332 c, float x, float y, float w, float h) {
        Matrix3x2fStack mat = c.method_51448();
        return new class_8030((int)Math.floor(x), (int)Math.floor(y), (int)Math.ceil(w), (int)Math.ceil(h)).method_71523((Matrix3x2fc)mat);
    }

    @Override
    public void render(double x, double y, class_332 ctx) {
        if (PipelineRegistry.RECTANGLE_PIPELINE != null) {
            Matrix3x2f guiMatrices = new Matrix3x2f((Matrix3x2fc)ctx.method_51448());
            SimpleGuiRenderState state = new SimpleGuiRenderState(PipelineRegistry.RECTANGLE_PIPELINE, class_11231.method_70899(), ctx, RectangleRenderer.createBounds(ctx, (float)x, (float)y, this.size.width(), this.size.height()), buffer -> {
                DirectVertexConsumer dvc = new DirectVertexConsumer((class_287)buffer, false);
                int c1 = Renderer.toAbgr(this.color.color1());
                int c2 = Renderer.toAbgr(this.color.color2());
                int c3 = Renderer.toAbgr(this.color.color3());
                int c4 = Renderer.toAbgr(this.color.color4());
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)x, (float)y).method_22913(0.0f, 0.0f).method_39415(c1).method_22913(this.size.width(), this.size.height()).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(this.smoothness, 0.0f);
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)x, (float)(y + (double)this.size.height())).method_22913(0.0f, 1.0f).method_39415(c2).method_22913(this.size.width(), this.size.height()).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(this.smoothness, 0.0f);
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)(x + (double)this.size.width()), (float)(y + (double)this.size.height())).method_22913(1.0f, 1.0f).method_39415(c3).method_22913(this.size.width(), this.size.height()).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(this.smoothness, 0.0f);
                dvc.method_70815((Matrix3x2fc)guiMatrices, (float)(x + (double)this.size.width()), (float)y).method_22913(1.0f, 0.0f).method_39415(c4).method_22913(this.size.width(), this.size.height()).method_22913(this.radius.radius1(), this.radius.radius2()).method_22913(this.radius.radius3(), this.radius.radius4()).method_22913(this.smoothness, 0.0f);
            });
            ((DrawContextAccessor)ctx).getState().method_70919((class_11244)state);
        }
    }
}

