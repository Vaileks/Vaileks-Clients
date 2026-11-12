/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.renderpearl.api.pipeline.BindGroupLayout
 *  com.mojang.renderpearl.api.pipeline.BlendFunction
 *  com.mojang.renderpearl.api.pipeline.ColorTargetState
 *  com.mojang.renderpearl.api.pipeline.PrimitiveTopology
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline$Snippet
 *  com.mojang.renderpearl.api.pipeline.UniformType
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.resources.Identifier
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

final class RoundedShape {
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"textures/gui/shape.png");
    private static final RenderPipeline PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"pipeline/rounded_shape")).withVertexShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/rounded_shape")).withFragmentShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/rounded_shape")).withBindGroupLayout(BindGroupLayout.builder().withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).withUniform("Sampler0", UniformType.COMBINED_IMAGE_SAMPLER).build()).withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withDepthStencilState(Optional.empty()).withCull(false).build();

    private RoundedShape() {
    }

    static void draw(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color, boolean outline) {
        if (width <= 0 || height <= 0 || color >>> 24 == 0) {
            return;
        }
        int r = Math.clamp((long)radius, 0, Math.min(2047, Math.min(width, height) / 2));
        if (width >= 4096 || height >= 4096) {
            if (outline) {
                graphics.fill(x, y, x + width, y + 1, color);
                graphics.fill(x, y + height - 1, x + width, y + height, color);
                graphics.fill(x, y + 1, x + 1, y + height - 1, color);
                graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
            } else {
                graphics.fill(x, y, x + width, y + height, color);
            }
            return;
        }
        int packedX = width + r * 4096;
        int packedY = height + (outline ? 4096 : 0);
        graphics.blit(PIPELINE, WHITE, x, y, (float)(-packedX), (float)(-packedY), width, height, packedX * 2, packedY * 2, 1, 1, color);
    }
}

