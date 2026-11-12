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

public record GlowRenderer(SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness, float spread) implements Renderer
{
    private static final float SHADOW_GAP_COMPENSATION = 1.0f;
    private static final float SHADOW_INSET = 1.0f;

    private static class_8030 createBounds(class_332 c, float x, float y, float w, float h) {
        Matrix3x2fStack mat = c.method_51448();
        return new class_8030((int)Math.floor(x), (int)Math.floor(y), (int)Math.ceil(w), (int)Math.ceil(h)).method_71523((Matrix3x2fc)mat);
    }

    @Override
    public void render(double x, double y, class_332 ctx) {
        if (PipelineRegistry.GLOW_PIPELINE != null) {
            float safeSpread = Math.max(0.0f, this.spread - 1.0f);
            float baseX = (float)x + 1.0f;
            float baseY = (float)y + 1.0f;
            float baseWidth = Math.max(0.0f, this.size.width() - 2.0f);
            float baseHeight = Math.max(0.0f, this.size.height() - 2.0f);
            float radius1 = Math.max(0.0f, this.radius.radius1() - 1.0f);
            float radius2 = Math.max(0.0f, this.radius.radius2() - 1.0f);
            float radius3 = Math.max(0.0f, this.radius.radius3() - 1.0f);
            float radius4 = Math.max(0.0f, this.radius.radius4() - 1.0f);
            float expandedX = baseX - safeSpread;
            float expandedY = baseY - safeSpread;
            float expandedWidth = baseWidth + safeSpread * 2.0f;
            float expandedHeight = baseHeight + safeSpread * 2.0f;
            Matrix3x2f guiMatrices = new Matrix3x2f((Matrix3x2fc)ctx.method_51448());
            SimpleGuiRenderState state = new SimpleGuiRenderState(PipelineRegistry.GLOW_PIPELINE, class_11231.method_70899(), ctx, GlowRenderer.createBounds(ctx, expandedX, expandedY, expandedWidth, expandedHeight), buffer -> {
                DirectVertexConsumer dvc = new DirectVertexConsumer((class_287)buffer, false);
                int c1 = Renderer.toAbgr(this.color.color1());
                int c2 = Renderer.toAbgr(this.color.color2());
                int c3 = Renderer.toAbgr(this.color.color3());
                int c4 = Renderer.toAbgr(this.color.color4());
                dvc.method_70815((Matrix3x2fc)guiMatrices, expandedX, expandedY).method_22913(0.0f, 0.0f).method_39415(c1).method_22913(expandedWidth, expandedHeight).method_22913(radius1, radius2).method_22913(radius3, radius4).method_22913(this.smoothness, safeSpread);
                dvc.method_70815((Matrix3x2fc)guiMatrices, expandedX, expandedY + expandedHeight).method_22913(0.0f, 1.0f).method_39415(c2).method_22913(expandedWidth, expandedHeight).method_22913(radius1, radius2).method_22913(radius3, radius4).method_22913(this.smoothness, safeSpread);
                dvc.method_70815((Matrix3x2fc)guiMatrices, expandedX + expandedWidth, expandedY + expandedHeight).method_22913(1.0f, 1.0f).method_39415(c3).method_22913(expandedWidth, expandedHeight).method_22913(radius1, radius2).method_22913(radius3, radius4).method_22913(this.smoothness, safeSpread);
                dvc.method_70815((Matrix3x2fc)guiMatrices, expandedX + expandedWidth, expandedY).method_22913(1.0f, 0.0f).method_39415(c4).method_22913(expandedWidth, expandedHeight).method_22913(radius1, radius2).method_22913(radius3, radius4).method_22913(this.smoothness, safeSpread);
            });
            ((DrawContextAccessor)ctx).getState().method_70919((class_11244)state);
        }
    }
}

