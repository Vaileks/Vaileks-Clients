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

final class RoundedShape {
    private static final class_2960 WHITE = class_2960.method_60655((String)"bloodhelper", (String)"textures/gui/shape.png");
    private static final RenderPipeline PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"bloodhelper", (String)"pipeline/rounded_shape")).withVertexShader(class_2960.method_60655((String)"bloodhelper", (String)"core/rounded_shape")).withFragmentShader(class_2960.method_60655((String)"bloodhelper", (String)"core/rounded_shape")).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withSampler("Sampler0").withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build();

    private RoundedShape() {
    }

    static void draw(class_332 graphics, int x, int y, int width, int height, int radius, int color, boolean outline) {
        if (width <= 0 || height <= 0 || color >>> 24 == 0) {
            return;
        }
        int r = Math.clamp((long)radius, 0, Math.min(2047, Math.min(width, height) / 2));
        if (width >= 4096 || height >= 4096) {
            if (outline) {
                graphics.method_25294(x, y, x + width, y + 1, color);
                graphics.method_25294(x, y + height - 1, x + width, y + height, color);
                graphics.method_25294(x, y + 1, x + 1, y + height - 1, color);
                graphics.method_25294(x + width - 1, y + 1, x + width, y + height - 1, color);
            } else {
                graphics.method_25294(x, y, x + width, y + height, color);
            }
            return;
        }
        int packedX = width + r * 4096;
        int packedY = height + (outline ? 4096 : 0);
        graphics.method_25293(PIPELINE, WHITE, x, y, (float)(-packedX), (float)(-packedY), width, height, packedX * 2, packedY * 2, 1, 1, color);
    }
}

