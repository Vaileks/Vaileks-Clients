/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Builder
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  com.mojang.blaze3d.vertex.VertexFormatElement
 *  com.mojang.blaze3d.vertex.VertexFormatElement$Type
 *  com.mojang.blaze3d.vertex.VertexFormatElement$Usage
 *  net.minecraft.class_10789
 *  net.minecraft.class_2960
 */
package pyz1k.render.renderers.pipeline;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.class_10789;
import net.minecraft.class_2960;

public final class PipelineRegistry {
    private static final VertexFormatElement[] EXTRA_UV_ELEMENTS = PipelineRegistry.createExtraUvElements(5);
    public static RenderPipeline RECTANGLE_PIPELINE;
    public static RenderPipeline BORDER_PIPELINE;
    public static RenderPipeline TEXTURE_PIPELINE;
    public static RenderPipeline HEAD_TEXTURE_PIPELINE;
    public static RenderPipeline BLUR_PIPELINE;
    public static RenderPipeline GLOW_PIPELINE;
    public static RenderPipeline CIRCLE_PIPELINE;
    public static RenderPipeline METANOISE_PIPELINE;
    public static RenderPipeline RING_PROGRESS_PIPELINE;
    public static RenderPipeline JUMP_CIRCLE_PIPELINE;
    public static RenderPipeline MSDF_FONT_PIPELINE;

    private PipelineRegistry() {
    }

    public static void init() {
        RECTANGLE_PIPELINE = PipelineRegistry.create("rectangle", false, PipelineRegistry.format("UV1", "RadiusXY", "RadiusZW", "ExtraData1"));
        BORDER_PIPELINE = PipelineRegistry.create("border", false, PipelineRegistry.format("UV1", "RadiusXY", "RadiusZW", "BorderData1", "BorderData2"));
        TEXTURE_PIPELINE = PipelineRegistry.create("texture", true, PipelineRegistry.format("UV1", "RadiusXY", "RadiusZW", "ExtraData1"));
        HEAD_TEXTURE_PIPELINE = PipelineRegistry.create("head_texture", true, PipelineRegistry.format("UV1", "RadiusXY", "RadiusZW", "ExtraData1", "ExtraData2"));
        BLUR_PIPELINE = PipelineRegistry.create("blur", true, PipelineRegistry.format("UV1", "RadiusXY", "RadiusZW", "BlurData"));
        GLOW_PIPELINE = PipelineRegistry.create("glow", false, PipelineRegistry.format("UV1", "RadiusXY", "RadiusZW", "GlowData"));
        CIRCLE_PIPELINE = PipelineRegistry.create("circle", false, PipelineRegistry.format("UV1", "RadiusXY", "RadiusZW", "ExtraData1"));
        METANOISE_PIPELINE = PipelineRegistry.create("metanoise", false, PipelineRegistry.format("UV1", "RadiusXY", "RadiusZW", "ExtraData1"));
        RING_PROGRESS_PIPELINE = PipelineRegistry.create("ring_progress", false, PipelineRegistry.format("UV1", "RadiusXY", "RadiusZW", "ExtraData1", "ExtraData2"));
        MSDF_FONT_PIPELINE = PipelineRegistry.create("msdf_font", true, PipelineRegistry.baseFormat());
    }

    private static RenderPipeline create(String shader, boolean textured, VertexFormat format) {
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withLocation(class_2960.method_60655((String)"pyz1k", (String)("pipeline/" + shader))).withVertexShader(class_2960.method_60655((String)"pyz1k", (String)("core/" + shader))).withFragmentShader(class_2960.method_60655((String)"pyz1k", (String)("core/" + shader))).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).withVertexFormat(format, VertexFormat.class_5596.field_27382);
        if (textured) {
            builder.withSampler("Sampler0");
        }
        builder.withUsePipelineDrawModeForGui(true);
        return builder.build();
    }

    private static VertexFormat baseFormat() {
        return VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("UV0", VertexFormatElement.UV0).add("Color", VertexFormatElement.COLOR).build();
    }

    private static VertexFormat format(String ... extraAttributes) {
        if (extraAttributes.length > EXTRA_UV_ELEMENTS.length) {
            throw new IllegalArgumentException("Too many custom vertex attributes: " + extraAttributes.length);
        }
        VertexFormat.Builder builder = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("UV0", VertexFormatElement.UV0).add("Color", VertexFormatElement.COLOR);
        for (int i = 0; i < extraAttributes.length; ++i) {
            builder.add(extraAttributes[i], EXTRA_UV_ELEMENTS[i]);
        }
        return builder.build();
    }

    private static VertexFormatElement[] createExtraUvElements(int count) {
        VertexFormatElement[] result = new VertexFormatElement[count];
        for (int i = 0; i < count; ++i) {
            int id = PipelineRegistry.findFreeElementId();
            result[i] = VertexFormatElement.register((int)id, (int)(3 + i), (VertexFormatElement.Type)VertexFormatElement.Type.FLOAT, (VertexFormatElement.Usage)VertexFormatElement.Usage.UV, (int)2);
        }
        return result;
    }

    private static int findFreeElementId() {
        for (int id = 31; id >= 7; --id) {
            if (VertexFormatElement.byId((int)id) != null) continue;
            return id;
        }
        throw new IllegalStateException("No free VertexFormatElement IDs for Pyz1k custom GUI pipeline");
    }
}

