/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.renderpearl.api.buffers.GpuBuffer
 *  com.mojang.renderpearl.api.commands.CommandEncoder
 *  com.mojang.renderpearl.api.commands.RenderPass
 *  com.mojang.renderpearl.api.pipeline.BindGroupLayout
 *  com.mojang.renderpearl.api.pipeline.ColorTargetState
 *  com.mojang.renderpearl.api.pipeline.PrimitiveTopology
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline$Snippet
 *  com.mojang.renderpearl.api.pipeline.UniformType
 *  com.mojang.renderpearl.api.textures.FilterMode
 *  com.mojang.renderpearl.api.textures.GpuTexture
 *  com.mojang.renderpearl.api.textures.GpuTextureView
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.phys.Vec3
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.visuals;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.MotionBlurSettings;
import ru.bloodhelper.client.modules.ZoomModule;
import ru.bloodhelper.client.visuals.MotionBlurHistory;

public final class MotionBlurRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"BloodHelper/MotionBlur");
    private static final MotionBlurHistory HISTORY = new MotionBlurHistory();
    private static final RenderPipeline PIPELINE = MotionBlurRenderer.pipeline();
    private static Buffers buffers;
    private static long processedFrames;
    private static long seededFrames;
    private static long retryAfter;
    private static boolean reportedFailure;
    private static double observedAspect;
    private static double observedFov;

    private MotionBlurRenderer() {
    }

    public static void beginFrame() {
        if (!MotionBlurRenderer.available(Minecraft.getInstance())) {
            MotionBlurRenderer.close();
        }
    }

    private static boolean available(Minecraft mc) {
        MotionBlurSettings settings = BloodHelperClient.config().motionBlur;
        return settings.enabled && settings.strength > 0.0 && mc.level != null && mc.player != null && mc.gui.screen() == null && mc.gui.overlay() == null && !mc.isPaused() && mc.isWindowActive() && (settings.duringZoom || ZoomModule.scaleFov(1.0f) >= 0.999f);
    }

    public static void compositeWorld() {
        block15: {
            Minecraft mc = Minecraft.getInstance();
            if (!MotionBlurRenderer.available(mc) || System.nanoTime() < retryAfter) {
                HISTORY.reset();
                return;
            }
            try {
                RenderTarget target = mc.gameRenderer.mainRenderTarget();
                GpuTexture source = target.getColorTexture();
                if (source == null || source.isClosed() || source.getWidth(0) < 2 || source.getHeight(0) < 2) {
                    MotionBlurRenderer.close();
                    return;
                }
                if (RenderSystem.getCompiledPipelineNullable((RenderPipeline)PIPELINE) == null) {
                    throw new IllegalStateException("Motion Blur shader unavailable");
                }
                if (buffers == null || !buffers.matches(source)) {
                    MotionBlurRenderer.close();
                    buffers = new Buffers(source);
                }
                MotionBlurSettings settings = BloodHelperClient.config().motionBlur;
                double aspect = BloodHelperClient.config().aspectRatio.ratio((float)source.getWidth(0) / (float)source.getHeight(0));
                double fov = ((Integer)mc.options.fov().get()).intValue();
                if (aspect != observedAspect || fov != observedFov) {
                    HISTORY.reset();
                }
                observedAspect = aspect;
                observedFov = fov;
                Vec3 position = mc.gameRenderer.mainCamera().position();
                double weight = HISTORY.advance(System.nanoTime(), mc.level, mc.getCameraEntity(), mc.options.getCameraType(), settings, source.getWidth(0), source.getHeight(0), position.x, position.y, position.z, settings.strength, settings.durationMs);
                CommandEncoder commands = RenderSystem.getDevice().createCommandEncoder();
                if (weight <= 0.0) {
                    commands.copyTextureToTexture(source, MotionBlurRenderer.buffers.history, 0, 0, 0, 0, 0, source.getWidth(0), source.getHeight(0));
                    ++seededFrames;
                    return;
                }
                commands.copyTextureToTexture(source, MotionBlurRenderer.buffers.current, 0, 0, 0, 0, 0, source.getWidth(0), source.getHeight(0));
                MotionBlurRenderer.buffers.parameters.clear();
                MotionBlurRenderer.buffers.parameters.putFloat((float)weight).putFloat(0.0f).putFloat(0.0f).putFloat(0.0f).flip();
                commands.writeToBuffer(MotionBlurRenderer.buffers.uniform.slice(), MotionBlurRenderer.buffers.parameters);
                try (RenderPass pass = commands.createRenderPass(() -> "BLOODHELPER world Motion Blur", target.getColorTextureView(), Optional.empty());){
                    pass.setPipeline(RenderSystem.getCompiledPipeline((RenderPipeline)PIPELINE));
                    pass.setUniform("CurrentFrame", MotionBlurRenderer.buffers.currentView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                    pass.setUniform("HistoryFrame", MotionBlurRenderer.buffers.historyView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                    pass.setUniform("MotionBlur", MotionBlurRenderer.buffers.uniform);
                    pass.draw(3, 1, 0, 0);
                }
                commands.copyTextureToTexture(source, MotionBlurRenderer.buffers.history, 0, 0, 0, 0, 0, source.getWidth(0), source.getHeight(0));
                ++processedFrames;
            }
            catch (RuntimeException failure) {
                MotionBlurRenderer.close();
                retryAfter = System.nanoTime() + 5000000000L;
                if (reportedFailure) break block15;
                reportedFailure = true;
                LOGGER.warn("Motion Blur unavailable; leaving world unchanged", (Throwable)failure);
            }
        }
    }

    public static void close() {
        HISTORY.reset();
        if (buffers != null) {
            buffers.close();
            buffers = null;
        }
    }

    public static long processedFrames() {
        return processedFrames;
    }

    public static long seededFrames() {
        return seededFrames;
    }

    public static boolean hasHistory() {
        return HISTORY.valid() && buffers != null;
    }

    private static RenderPipeline pipeline() {
        return RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"pipeline/motion_blur")).withVertexShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/motion_blur")).withFragmentShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/motion_blur")).withBindGroupLayout(BindGroupLayout.builder().withUniform("CurrentFrame", UniformType.COMBINED_IMAGE_SAMPLER).withUniform("HistoryFrame", UniformType.COMBINED_IMAGE_SAMPLER).withUniform("MotionBlur", UniformType.UNIFORM_BUFFER).build()).withPrimitiveTopology(PrimitiveTopology.TRIANGLES).withDepthStencilState(Optional.empty()).withCull(false).withColorTargetState(ColorTargetState.DEFAULT).build();
    }

    private static final class Buffers
    implements AutoCloseable {
        private GpuTexture current;
        private GpuTexture history;
        private GpuTextureView currentView;
        private GpuTextureView historyView;
        private GpuBuffer uniform;
        private final ByteBuffer parameters = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());

        Buffers(GpuTexture source) {
            try {
                int usage = 5;
                this.current = RenderSystem.getDevice().createTexture("BLOODHELPER motion current", usage, source.getFormat(), source.getWidth(0), source.getHeight(0), 1, 1);
                this.history = RenderSystem.getDevice().createTexture("BLOODHELPER motion history", usage, source.getFormat(), source.getWidth(0), source.getHeight(0), 1, 1);
                this.currentView = RenderSystem.getDevice().createTextureView(this.current);
                this.historyView = RenderSystem.getDevice().createTextureView(this.history);
                this.uniform = RenderSystem.getDevice().createBuffer(() -> "BLOODHELPER Motion Blur settings", 136, 16L);
            }
            catch (RuntimeException failure) {
                this.close();
                throw failure;
            }
        }

        boolean matches(GpuTexture source) {
            return this.current != null && !this.current.isClosed() && this.history != null && !this.history.isClosed() && this.current.getFormat() == source.getFormat() && this.current.getWidth(0) == source.getWidth(0) && this.current.getHeight(0) == source.getHeight(0);
        }

        @Override
        public void close() {
            if (this.currentView != null) {
                this.currentView.close();
                this.currentView = null;
            }
            if (this.historyView != null) {
                this.historyView.close();
                this.historyView = null;
            }
            if (this.current != null) {
                this.current.close();
                this.current = null;
            }
            if (this.history != null) {
                this.history.close();
                this.history = null;
            }
            if (this.uniform != null) {
                this.uniform.close();
                this.uniform = null;
            }
        }
    }
}

