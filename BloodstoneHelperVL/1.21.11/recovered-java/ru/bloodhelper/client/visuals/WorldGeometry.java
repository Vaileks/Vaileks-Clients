/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBuffer$MappedView
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.systems.RenderSystem$class_5590
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1044
 *  net.minecraft.class_10789
 *  net.minecraft.class_11285
 *  net.minecraft.class_243
 *  net.minecraft.class_276
 *  net.minecraft.class_287
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4588
 *  net.minecraft.class_9799
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.lwjgl.system.MemoryUtil
 */
package ru.bloodhelper.client.visuals;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_1044;
import net.minecraft.class_10789;
import net.minecraft.class_11285;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4588;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.FontGlyphRun;
import ru.bloodhelper.client.visuals.WorldProjection;

public final class WorldGeometry {
    private static final RenderPipeline LINES = WorldGeometry.pipeline("world_line", false);
    private static final RenderPipeline RINGS = WorldGeometry.pipeline("world_ring", true);
    private static final RenderPipeline TEXT = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"bloodhelper", (String)"pipeline/world_text")).withVertexShader(class_2960.method_60655((String)"bloodhelper", (String)"core/world_overlay")).withFragmentShader(class_2960.method_60655((String)"bloodhelper", (String)"core/world_overlay")).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withSampler("Sampler0").withShaderDefine("TEXT").withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withCull(false).build();
    private static final class_9799 arena = new class_9799(262144);
    private static class_11285 gpu;
    private static int drawnLines;
    private static int drawnRings;
    private static long draws;

    private WorldGeometry() {
    }

    private static RenderPipeline pipeline(String name, boolean ring) {
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"bloodhelper", (String)("pipeline/" + name))).withVertexShader(class_2960.method_60655((String)"bloodhelper", (String)"core/world_overlay")).withFragmentShader(class_2960.method_60655((String)"bloodhelper", (String)"core/world_overlay")).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withCull(false);
        if (ring) {
            builder.withShaderDefine("RING");
        }
        return builder.build();
    }

    public static void draw(List<Line> lines, List<Ring> rings) {
        class_287 buffer;
        drawnRings = 0;
        drawnLines = 0;
        if (!WorldProjection.valid()) {
            return;
        }
        class_243 camera = WorldProjection.camera();
        if (!rings.isEmpty()) {
            buffer = new class_287(arena, VertexFormat.class_5596.field_27382, class_290.field_1575);
            for (Ring ring : rings) {
                if (!(ring.radius > 0.0) || ring.color >>> 24 == 0) continue;
                class_243 c = ring.center.method_1020(camera);
                double r = ring.radius / 0.94;
                WorldGeometry.vertex(buffer, c.method_1031(-r, 0.0, -r), -1.0f, -1.0f, ring.color);
                WorldGeometry.vertex(buffer, c.method_1031(-r, 0.0, r), -1.0f, 1.0f, ring.color);
                WorldGeometry.vertex(buffer, c.method_1031(r, 0.0, r), 1.0f, 1.0f, ring.color);
                WorldGeometry.vertex(buffer, c.method_1031(r, 0.0, -r), 1.0f, -1.0f, ring.color);
                ++drawnRings;
            }
            WorldGeometry.upload(buffer, RINGS);
        }
        if (!lines.isEmpty()) {
            buffer = new class_287(arena, VertexFormat.class_5596.field_27382, class_290.field_1575);
            for (Line line : lines) {
                class_243 side;
                class_243 a = line.from.method_1020(camera);
                class_243 b = line.to.method_1020(camera);
                class_243 direction = b.method_1020(a);
                if (direction.method_1027() < 1.0E-8 || line.color >>> 24 == 0 || (side = direction.method_1036(a.method_1019(b).method_1021(0.5)).method_1029()).method_1027() < 0.01) continue;
                double widthA = WorldProjection.worldUnitsPerPixel(line.from) * (double)line.pixels * 1.8;
                double widthB = WorldProjection.worldUnitsPerPixel(line.to) * (double)line.pixels * 1.8;
                class_243 sa = side.method_1021(Math.clamp(widthA, 0.001, 1.0));
                class_243 sb = side.method_1021(Math.clamp(widthB, 0.001, 1.0));
                WorldGeometry.vertex(buffer, a.method_1020(sa), 0.0f, -1.0f, line.color);
                WorldGeometry.vertex(buffer, a.method_1019(sa), 0.0f, 1.0f, line.color);
                WorldGeometry.vertex(buffer, b.method_1019(sb), 1.0f, 1.0f, line.color);
                WorldGeometry.vertex(buffer, b.method_1020(sb), 1.0f, -1.0f, line.color);
                ++drawnLines;
            }
            WorldGeometry.upload(buffer, LINES);
        }
    }

    private static void vertex(class_287 b, class_243 p, float u, float v, int color) {
        b.method_22912((float)p.field_1352, (float)p.field_1351, (float)p.field_1350).method_22913(u, v).method_39415(color);
    }

    private static void upload(class_287 builder, RenderPipeline pipeline) {
        WorldGeometry.upload(builder, pipeline, null);
    }

    public static void text(List<Text> text) {
        record Part(FontGlyphRun glyphs, Matrix4f pose, float x, float y, int color) {
        }
        if (text.isEmpty() || !WorldProjection.valid()) {
            return;
        }
        CustomFont font = CustomFont.medium();
        LinkedHashMap<class_2960, List> batches = new LinkedHashMap<class_2960, List>();
        for (Text text2 : text) {
            class_243 p = text2.center.method_1020(WorldProjection.camera());
            Matrix4f pose = new Matrix4f().translation((float)p.field_1352, (float)p.field_1351, (float)p.field_1350).rotate((Quaternionfc)WorldProjection.rotation()).scale(text2.height, -text2.height, text2.height);
            float x = -font.width(text2.text, 1.0f) / 2.0f + text2.xOffset;
            float y = -0.5f + text2.yOffset;
            for (CustomFont.WorldRun run : font.worldRuns(text2.text)) {
                batches.computeIfAbsent(run.texture(), k -> new ArrayList()).add(new Part(run.glyphs(), pose, x, y, text2.color));
            }
        }
        for (Map.Entry entry : batches.entrySet()) {
            class_287 buffer = new class_287(arena, VertexFormat.class_5596.field_27382, class_290.field_1575);
            for (Part part : (List)entry.getValue()) {
                part.glyphs.buildWorld((class_4588)buffer, (Matrix4fc)part.pose, part.x, part.y, part.color);
            }
            WorldGeometry.upload(buffer, TEXT, (class_2960)entry.getKey());
        }
    }

    private static void upload(class_287 builder, RenderPipeline pipeline, class_2960 texture) {
        class_9801 mesh = builder.method_60794();
        if (mesh == null) {
            return;
        }
        try (class_9801 class_98012 = mesh;){
            int size = mesh.method_60818().remaining();
            if (gpu == null || gpu.method_71312() < size) {
                if (gpu != null) {
                    gpu.close();
                }
                gpu = new class_11285(() -> "BloodHelper world overlays", 34, Math.max(262144, size));
            }
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            try (GpuBuffer.MappedView mapped = encoder.mapBuffer(gpu.method_71119().slice(0L, (long)size), false, true);){
                MemoryUtil.memCopy((ByteBuffer)mesh.method_60818(), (ByteBuffer)mapped.data());
            }
            RenderSystem.class_5590 sequential = RenderSystem.getSequentialBuffer((VertexFormat.class_5596)VertexFormat.class_5596.field_27382);
            GpuBuffer indices = sequential.method_68274(mesh.method_60822().comp_751());
            GpuBufferSlice transform = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)WorldProjection.view(), (Vector4fc)new Vector4f(1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
            class_276 target = class_310.method_1551().method_1522();
            try (RenderPass pass = encoder.createRenderPass(() -> "BloodHelper world overlays", target.method_71639(), OptionalInt.empty(), target.method_71640(), OptionalDouble.empty());){
                pass.setPipeline(pipeline);
                RenderSystem.bindDefaultUniforms((RenderPass)pass);
                pass.setUniform("DynamicTransforms", transform);
                if (texture != null) {
                    class_1044 atlas = class_310.method_1551().method_1531().method_4619(texture);
                    pass.bindTexture("Sampler0", atlas.method_71659(), atlas.method_75484());
                }
                pass.setVertexBuffer(0, gpu.method_71119());
                pass.setIndexBuffer(indices, sequential.method_31924());
                pass.drawIndexed(0, 0, mesh.method_60822().comp_751(), 1);
                ++draws;
            }
            gpu.method_71121();
        }
    }

    public static int drawnLines() {
        return drawnLines;
    }

    public static int drawnRings() {
        return drawnRings;
    }

    public static long draws() {
        return draws;
    }

    public static void clearCounters() {
        drawnRings = 0;
        drawnLines = 0;
    }

    public static void close() {
        if (gpu != null) {
            gpu.close();
            gpu = null;
        }
        arena.close();
    }

    public record Ring(class_243 center, double radius, int color) {
    }

    public record Line(class_243 from, class_243 to, int color, float pixels) {
    }

    public record Text(class_243 center, String text, float height, int color, float xOffset, float yOffset) {
    }
}

