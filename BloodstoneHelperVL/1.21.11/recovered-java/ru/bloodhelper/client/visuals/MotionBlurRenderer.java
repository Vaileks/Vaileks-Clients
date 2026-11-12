/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_243
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.visuals;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
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
        if (!MotionBlurRenderer.available(class_310.method_1551())) {
            MotionBlurRenderer.close();
        }
    }

    private static boolean available(class_310 mc) {
        MotionBlurSettings settings = BloodHelperClient.config().motionBlur;
        return settings.enabled && settings.strength > 0.0 && mc.field_1687 != null && mc.field_1724 != null && mc.field_1755 == null && mc.method_18506() == null && !mc.method_1493() && mc.method_1569() && (settings.duringZoom || ZoomModule.scaleFov(1.0f) >= 0.999f);
    }

    public static void compositeWorld() {
        block15: {
            class_310 mc = class_310.method_1551();
            if (!MotionBlurRenderer.available(mc) || System.nanoTime() < retryAfter) {
                HISTORY.reset();
                return;
            }
            try {
                class_276 target = mc.method_1522();
                GpuTexture source = target.method_30277();
                if (source == null || source.isClosed() || source.getWidth(0) < 2 || source.getHeight(0) < 2) {
                    MotionBlurRenderer.close();
                    return;
                }
                if (!RenderSystem.getDevice().precompilePipeline(PIPELINE).isValid()) {
                    throw new IllegalStateException("Motion Blur shader unavailable");
                }
                if (buffers == null || !buffers.matches(source)) {
                    MotionBlurRenderer.close();
                    buffers = new Buffers(source);
                }
                MotionBlurSettings settings = BloodHelperClient.config().motionBlur;
                double aspect = BloodHelperClient.config().aspectRatio.ratio((float)source.getWidth(0) / (float)source.getHeight(0));
                double fov = ((Integer)mc.field_1690.method_41808().method_41753()).intValue();
                if (aspect != observedAspect || fov != observedFov) {
                    HISTORY.reset();
                }
                observedAspect = aspect;
                observedFov = fov;
                class_243 position = mc.field_1773.method_19418().method_71156();
                double weight = HISTORY.advance(System.nanoTime(), mc.field_1687, mc.method_1560(), mc.field_1690.method_31044(), settings, source.getWidth(0), source.getHeight(0), position.field_1352, position.field_1351, position.field_1350, settings.strength, settings.durationMs);
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
                try (RenderPass pass = commands.createRenderPass(() -> "BLOODHELPER world Motion Blur", target.method_71639(), OptionalInt.empty());){
                    pass.setPipeline(PIPELINE);
                    pass.bindTexture("CurrentFrame", MotionBlurRenderer.buffers.currentView, RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
                    pass.bindTexture("HistoryFrame", MotionBlurRenderer.buffers.historyView, RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
                    pass.setUniform("MotionBlur", MotionBlurRenderer.buffers.uniform);
                    pass.draw(0, 3);
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
        return RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"bloodhelper", (String)"pipeline/motion_blur")).withVertexShader(class_2960.method_60655((String)"bloodhelper", (String)"core/motion_blur")).withFragmentShader(class_2960.method_60655((String)"bloodhelper", (String)"core/motion_blur")).withSampler("CurrentFrame").withSampler("HistoryFrame").withUniform("MotionBlur", class_10789.field_60031).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).withoutBlend().build();
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

