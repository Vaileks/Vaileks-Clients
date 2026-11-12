/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_10789;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import ru.bloodhelper.client.gui.MusicWaveShape;

final class MusicWave {
    private static final class_2960 WHITE = class_2960.method_60655((String)"bloodhelper", (String)"textures/gui/shape.png");
    private static final RenderPipeline PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"bloodhelper", (String)"pipeline/music_wave")).withVertexShader(class_2960.method_60655((String)"bloodhelper", (String)"core/music_wave")).withFragmentShader(class_2960.method_60655((String)"bloodhelper", (String)"core/music_wave")).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withSampler("Sampler0").withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build();

    private MusicWave() {
    }

    static void draw(class_332 g, float x, float y, float width, float height, double phase, double energy, int color) {
        if (width <= 0.0f || height <= 0.0f || color >>> 24 == 0) {
            return;
        }
        int w = Math.clamp((long)((int)Math.ceil(width)), 1, 251);
        int h = Math.clamp((long)((int)Math.ceil(height)), 1, 251);
        int u = MusicWaveShape.x(w, phase);
        int v = MusicWaveShape.y(h, phase, energy);
        g.method_51448().pushMatrix();
        g.method_51448().translate(x - 2.0f, y - 2.0f);
        g.method_25293(PIPELINE, WHITE, 0, 0, (float)(-u), (float)(-v), w + 4, h + 4, u * 2, v * 2, 1, 1, color);
        g.method_51448().popMatrix();
    }
}

