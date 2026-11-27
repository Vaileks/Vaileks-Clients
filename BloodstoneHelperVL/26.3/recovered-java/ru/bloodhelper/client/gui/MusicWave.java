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
import ru.bloodhelper.client.gui.MusicWaveShape;

final class MusicWave {
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"textures/gui/shape.png");
    private static final RenderPipeline PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"pipeline/music_wave")).withVertexShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/music_wave")).withFragmentShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/music_wave")).withBindGroupLayout(BindGroupLayout.builder().withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).withUniform("Sampler0", UniformType.COMBINED_IMAGE_SAMPLER).build()).withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withDepthStencilState(Optional.empty()).withCull(false).build();

    private MusicWave() {
    }

    static void draw(GuiGraphicsExtractor g, float x, float y, float width, float height, double phase, double energy, int color) {
        if (width <= 0.0f || height <= 0.0f || color >>> 24 == 0) {
            return;
        }
        int w = Math.clamp((long)((int)Math.ceil(width)), 1, 251);
        int h = Math.clamp((long)((int)Math.ceil(height)), 1, 251);
        int u = MusicWaveShape.x(w, phase);
        int v = MusicWaveShape.y(h, phase, energy);
        g.pose().pushMatrix();
        g.pose().translate(x - 2.0f, y - 2.0f);
        g.blit(PIPELINE, WHITE, 0, 0, (float)(-u), (float)(-v), w + 4, h + 4, u * 2, v * 2, 1, 1, color);
        g.pose().popMatrix();
    }
}

