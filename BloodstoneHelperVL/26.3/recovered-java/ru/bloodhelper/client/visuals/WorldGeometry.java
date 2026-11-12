/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.renderpearl.api.buffers.GpuBuffer
 *  com.mojang.renderpearl.api.buffers.GpuBufferSlice
 *  com.mojang.renderpearl.api.buffers.GpuBufferSlice$MappedView
 *  com.mojang.renderpearl.api.commands.CommandEncoder
 *  com.mojang.renderpearl.api.commands.RenderPass
 *  com.mojang.renderpearl.api.pipeline.BindGroupLayout
 *  com.mojang.renderpearl.api.pipeline.BlendFunction
 *  com.mojang.renderpearl.api.pipeline.ColorTargetState
 *  com.mojang.renderpearl.api.pipeline.DepthStencilState
 *  com.mojang.renderpearl.api.pipeline.PrimitiveTopology
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline$Builder
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline$Snippet
 *  com.mojang.renderpearl.api.pipeline.UniformType
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MappableRingBuffer
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 *  org.lwjgl.system.MemoryUtil
 */
package ru.bloodhelper.client.visuals;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.FontGlyphRun;
import ru.bloodhelper.client.visuals.WorldProjection;

public final class WorldGeometry {
    private static final RenderPipeline LINES = WorldGeometry.pipeline("world_line", false);
    private static final RenderPipeline RINGS = WorldGeometry.pipeline("world_ring", true);
    private static final RenderPipeline TEXT = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"pipeline/world_text")).withVertexShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/world_overlay")).withFragmentShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/world_overlay")).withBindGroupLayout(BindGroupLayout.builder().withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).withUniform("Sampler0", UniformType.COMBINED_IMAGE_SAMPLER).build()).withShaderDefine("TEXT").withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withDepthStencilState(new DepthStencilState(DepthStencilState.DEFAULT.depthTest(), false)).withCull(false).build();
    private static final ByteBufferBuilder arena = new ByteBufferBuilder(262144);
    private static MappableRingBuffer gpu;
    private static int drawnLines;
    private static int drawnRings;
    private static long draws;

    private WorldGeometry() {
    }

    private static RenderPipeline pipeline(String name, boolean ring) {
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)("pipeline/" + name))).withVertexShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/world_overlay")).withFragmentShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/world_overlay")).withBindGroupLayout(BindGroupLayout.builder().withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).build()).withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withDepthStencilState(new DepthStencilState(DepthStencilState.DEFAULT.depthTest(), false)).withCull(false);
        if (ring) {
            builder.withShaderDefine("RING");
        }
        return builder.build();
    }

    public static void draw(List<Line> lines, List<Ring> rings) {
        BufferBuilder buffer;
        drawnRings = 0;
        drawnLines = 0;
        if (!WorldProjection.valid()) {
            return;
        }
        Vec3 camera = WorldProjection.camera();
        if (!rings.isEmpty()) {
            buffer = new BufferBuilder(arena, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (Ring ring : rings) {
                if (!(ring.radius > 0.0) || ring.color >>> 24 == 0) continue;
                Vec3 c = ring.center.subtract(camera);
                double r = ring.radius / 0.94;
                WorldGeometry.vertex(buffer, c.add(-r, 0.0, -r), -1.0f, -1.0f, ring.color);
                WorldGeometry.vertex(buffer, c.add(-r, 0.0, r), -1.0f, 1.0f, ring.color);
                WorldGeometry.vertex(buffer, c.add(r, 0.0, r), 1.0f, 1.0f, ring.color);
                WorldGeometry.vertex(buffer, c.add(r, 0.0, -r), 1.0f, -1.0f, ring.color);
                ++drawnRings;
            }
            WorldGeometry.upload(buffer, RINGS);
        }
        if (!lines.isEmpty()) {
            buffer = new BufferBuilder(arena, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (Line line : lines) {
                Vec3 side;
                Vec3 a = line.from.subtract(camera);
                Vec3 b = line.to.subtract(camera);
                Vec3 direction = b.subtract(a);
                if (direction.lengthSqr() < 1.0E-8 || line.color >>> 24 == 0 || (side = direction.cross(a.add(b).scale(0.5)).normalize()).lengthSqr() < 0.01) continue;
                double widthA = WorldProjection.worldUnitsPerPixel(line.from) * (double)line.pixels * 1.8;
                double widthB = WorldProjection.worldUnitsPerPixel(line.to) * (double)line.pixels * 1.8;
                Vec3 sa = side.scale(Math.clamp(widthA, 0.001, 1.0));
                Vec3 sb = side.scale(Math.clamp(widthB, 0.001, 1.0));
                WorldGeometry.vertex(buffer, a.subtract(sa), 0.0f, -1.0f, line.color);
                WorldGeometry.vertex(buffer, a.add(sa), 0.0f, 1.0f, line.color);
                WorldGeometry.vertex(buffer, b.add(sb), 1.0f, 1.0f, line.color);
                WorldGeometry.vertex(buffer, b.subtract(sb), 1.0f, -1.0f, line.color);
                ++drawnLines;
            }
            WorldGeometry.upload(buffer, LINES);
        }
    }

    private static void vertex(BufferBuilder b, Vec3 p, float u, float v, int color) {
        b.addVertex((float)p.x, (float)p.y, (float)p.z).setUv(u, v).setColor(color);
    }

    private static void upload(BufferBuilder builder, RenderPipeline pipeline) {
        WorldGeometry.upload(builder, pipeline, null);
    }

    public static void text(List<Text> text) {
        record Part(FontGlyphRun glyphs, Matrix4f pose, float x, float y, int color) {
        }
        if (text.isEmpty() || !WorldProjection.valid()) {
            return;
        }
        CustomFont font = CustomFont.medium();
        LinkedHashMap<Identifier, List> batches = new LinkedHashMap<Identifier, List>();
        for (Text text2 : text) {
            Vec3 p = text2.center.subtract(WorldProjection.camera());
            Matrix4f pose = new Matrix4f().translation((float)p.x, (float)p.y, (float)p.z).rotate((Quaternionfc)WorldProjection.rotation()).scale(text2.height, -text2.height, text2.height);
            float x = -font.width(text2.text, 1.0f) / 2.0f + text2.xOffset;
            float y = -0.5f + text2.yOffset;
            for (CustomFont.WorldRun run : font.worldRuns(text2.text)) {
                batches.computeIfAbsent(run.texture(), k -> new ArrayList()).add(new Part(run.glyphs(), pose, x, y, text2.color));
            }
        }
        for (Map.Entry entry : batches.entrySet()) {
            BufferBuilder buffer = new BufferBuilder(arena, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (Part part : (List)entry.getValue()) {
                part.glyphs.buildWorld((VertexConsumer)buffer, (Matrix4fc)part.pose, part.x, part.y, part.color);
            }
            WorldGeometry.upload(buffer, TEXT, (Identifier)entry.getKey());
        }
    }

    private static void upload(BufferBuilder builder, RenderPipeline pipeline, Identifier texture) {
        MeshData mesh = builder.build();
        if (mesh == null) {
            return;
        }
        try (MeshData meshData = mesh;){
            int size = mesh.vertexBuffer().remaining();
            if (gpu == null || gpu.size() < size) {
                if (gpu != null) {
                    gpu.close();
                }
                gpu = new MappableRingBuffer(() -> "BloodHelper world overlays", 34, Math.max(262144, size));
            }
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            try (GpuBufferSlice.MappedView mapped = gpu.currentBuffer().slice(0L, (long)size).map(false, true);){
                MemoryUtil.memCopy((ByteBuffer)mesh.vertexBuffer(), (ByteBuffer)mapped.data());
            }
            RenderSystem.AutoStorageIndexBuffer sequential = RenderSystem.getSequentialBuffer((PrimitiveTopology)PrimitiveTopology.QUADS);
            GpuBuffer indices = sequential.getBuffer(mesh.drawState().indexCount());
            GpuBufferSlice transform = RenderSystem.getDynamicUniforms().writeTransform(WorldProjection.view(), new Vector4f(1.0f), new Vector3f(), new Matrix4f());
            RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            try (RenderPass pass = encoder.createRenderPass(() -> "BloodHelper world overlays", target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty());){
                pass.setPipeline(RenderSystem.getCompiledPipeline((RenderPipeline)pipeline));
                RenderSystem.bindDefaultUniforms((RenderPass)pass);
                pass.setUniform("DynamicTransforms", transform);
                if (texture != null) {
                    AbstractTexture atlas = Minecraft.getInstance().getTextureManager().getTexture(texture);
                    pass.setUniform("Sampler0", atlas.getTextureView(), atlas.getSampler());
                }
                pass.setVertexBuffer(0, gpu.currentBuffer().slice());
                pass.setIndexBuffer(indices, sequential.type());
                pass.drawIndexed(mesh.drawState().indexCount(), 1, 0, 0, 0);
                ++draws;
            }
            gpu.rotate();
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

    public record Ring(Vec3 center, double radius, int color) {
    }

    public record Line(Vec3 from, Vec3 to, int color, float pixels) {
    }

    public record Text(Vec3 center, String text, float height, int color, float xOffset, float yOffset) {
    }
}

