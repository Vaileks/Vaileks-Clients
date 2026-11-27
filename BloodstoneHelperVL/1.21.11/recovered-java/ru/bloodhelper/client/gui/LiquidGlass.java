/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1044
 *  net.minecraft.class_10789
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
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
import net.minecraft.class_1044;
import net.minecraft.class_10789;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.RenderQualitySettings;
import ru.bloodhelper.client.gui.EmbeddedReGlass;
import ru.bloodhelper.client.gui.OutlineSurface;
import ru.bloodhelper.client.gui.ReGlassCompat;
import ru.bloodhelper.client.gui.ReGlassGui;
import ru.bloodhelper.client.gui.RenderMetrics;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.SimpleHud;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudRenderer;

public final class LiquidGlass {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"BloodHelper/LiquidGlass");
    private static final class_2960 SNAPSHOT = class_2960.method_60655((String)"bloodhelper", (String)"dynamic/glass_background");
    private static final RenderPipeline HIGH = LiquidGlass.glassPipeline("high", 1, false);
    private static final RenderPipeline BALANCED = LiquidGlass.glassPipeline("balanced", 2, false);
    private static final RenderPipeline LOW = LiquidGlass.glassPipeline("low", 2, true);
    private static final RenderPipeline OVERLAY_HIGH = LiquidGlass.glassPipeline("overlay_high", 1, false);
    private static final RenderPipeline OVERLAY_BALANCED = LiquidGlass.glassPipeline("overlay_balanced", 2, false);
    private static final RenderPipeline OVERLAY_LOW = LiquidGlass.glassPipeline("overlay_low", 2, true);
    private static int capturedGuiKinds;
    private static final RenderPipeline BLUR_HORIZONTAL;
    private static final RenderPipeline BLUR_VERTICAL;
    private static final RenderPipeline BLUR_HORIZONTAL_LOW;
    private static final RenderPipeline BLUR_VERTICAL_LOW;
    private static RenderPipeline framePipeline;
    private static boolean frameLow;
    private static int frameBlurLevel;
    private static class_332 capturedFrame;
    private static SnapshotTexture snapshot;
    private static boolean ready;
    private static boolean blurred;
    private static boolean reportedFailure;
    private static long retryAfter;
    private static long capturedFrames;
    private static long blurPasses;
    private static long blurredPixels;
    private static int lastBlurWidth;
    private static int lastBlurHeight;

    private LiquidGlass() {
    }

    public static void beginFrame(class_332 graphics) {
        if (ReGlassCompat.installed()) {
            ready = false;
            return;
        }
        if (capturedFrame == graphics) {
            return;
        }
        capturedFrame = graphics;
        ready = false;
        blurred = false;
        lastBlurHeight = 0;
        lastBlurWidth = 0;
        RenderQualitySettings quality = RenderQuality.settings();
        frameBlurLevel = quality.blurMipLevel();
        boolean bl = frameLow = !quality.dispersion();
        framePipeline = frameLow ? LOW : (frameBlurLevel == 1 ? HIGH : BALANCED);
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null && !ReGlassGui.active() || !RenderSystem.isOnRenderThread() || System.nanoTime() < retryAfter) {
            return;
        }
        try {
            GpuTexture source = mc.method_1522().method_30277();
            if (source == null || source.isClosed() || source.getWidth(0) < 2 || source.getHeight(0) < 2) {
                return;
            }
            if (!RenderSystem.getDevice().precompilePipeline(framePipeline).isValid()) {
                LiquidGlass.failed("Liquid Glass shader is unavailable; using matte surfaces", null);
                return;
            }
            if (snapshot == null || !snapshot.matches(source, frameBlurLevel)) {
                snapshot = new SnapshotTexture(source, frameBlurLevel);
                mc.method_1531().method_4616(SNAPSHOT, (class_1044)snapshot);
            }
            ready = true;
        }
        catch (RuntimeException exception) {
            LiquidGlass.failed("Unable to copy Liquid Glass background; using matte surfaces", exception);
        }
    }

    public static void surface(class_332 graphics, int x, int y, int width, int height, int radius, int tint, ClientConfig config) {
        LiquidGlass.surface(graphics, x, y, width, height, radius, tint, config, false);
    }

    public static void surfaceMenu(class_332 graphics, int x, int y, int width, int height, int radius, int tint, ClientConfig config) {
        LiquidGlass.surface(graphics, x, y, width, height, radius, tint, config, true);
    }

    private static void surface(class_332 graphics, int x, int y, int width, int height, int radius, int tint, ClientConfig config, boolean menu) {
        if (width <= 0 || height <= 0 || tint >>> 24 == 0) {
            return;
        }
        if (SimpleHud.active()) {
            graphics.method_25294(x, y, x + width, y + height, tint);
            return;
        }
        if ("OUTLINE".equals(config.surfaceStyle)) {
            OutlineSurface.draw(graphics, x, y, width, height, radius, tint >>> 24, config, menu);
            return;
        }
        if ("LIQUID_GLASS".equals(config.surfaceStyle) && LiquidGlass.draw(graphics, x, y, width, height, radius, menu ? tint : UiDraw.alpha(tint, (int)Math.round(255.0 * (double)(tint >>> 24) / (double)Math.max(1, config.backgroundAlpha))), config.glassBlur, config.glassRefraction, menu, menu ? 1.0 : (double)config.backgroundAlpha / 255.0, config)) {
            return;
        }
        if ("LIQUID_GLASS".equals(config.surfaceStyle)) {
            tint = UiDraw.alpha(tint, (int)Math.round((double)(tint >>> 24) * (menu ? 0.68 : 0.42)));
        }
        UiDraw.rounded(graphics, x, y, width, height, radius, tint);
        int opacity = tint >>> 24;
        UiDraw.outline(graphics, x, y, width, height, radius, UiDraw.alpha(11975106, opacity / ("LIQUID_GLASS".equals(config.surfaceStyle) ? 24 : 12)));
    }

    public static boolean draw(class_332 graphics, int x, int y, int width, int height, int radius, int tint, double blur, double refraction) {
        return LiquidGlass.draw(graphics, x, y, width, height, radius, tint, blur, refraction, false, 1.0, BloodHelperClient.config());
    }

    private static boolean draw(class_332 graphics, int x, int y, int width, int height, int radius, int tint, double blur, double refraction, boolean menu, double density, ClientConfig cfg) {
        if (width <= 0 || height <= 0 || width >= 4096 || height >= 4096 || tint >>> 24 == 0) {
            return false;
        }
        if (SimpleHud.active()) {
            graphics.method_25294(x, y, x + width, y + height, tint);
            return true;
        }
        if (ReGlassCompat.installed()) {
            return ReGlassCompat.draw(graphics, x, y, width, height, radius, tint);
        }
        LiquidGlass.beginFrame(graphics);
        if (!ready) {
            return false;
        }
        if (!blurred && blur > 0.0) {
            try {
                RenderPipeline vertical;
                RenderPipeline horizontal = frameLow ? BLUR_HORIZONTAL_LOW : BLUR_HORIZONTAL;
                RenderPipeline renderPipeline = vertical = frameLow ? BLUR_VERTICAL_LOW : BLUR_VERTICAL;
                if (!RenderSystem.getDevice().precompilePipeline(horizontal).isValid() || !RenderSystem.getDevice().precompilePipeline(vertical).isValid()) {
                    ready = false;
                    LiquidGlass.failed("Liquid Glass blur shader is unavailable; using matte surfaces", null);
                    return false;
                }
                blurred = true;
            }
            catch (RuntimeException exception) {
                ready = false;
                LiquidGlass.failed("Unable to blur Liquid Glass background; using matte surfaces", exception);
                return false;
            }
        }
        int corner = Math.clamp((long)radius, 0, Math.min(63, Math.min(width, height) / 2));
        int softening = (int)Math.round(Math.clamp(Double.isFinite(blur) ? blur : 6.0, 0.0, 16.0));
        int bending = (int)Math.round(Math.clamp(Double.isFinite(refraction) ? refraction : 3.0, 0.0, 10.0) * 2.0);
        if (EmbeddedReGlass.active()) {
            bending = 21 + (int)Math.round(Math.clamp(Double.isFinite(refraction) ? refraction : 3.0, 0.0, 10.0));
            EmbeddedReGlass.recordDraw();
        }
        int material = (int)Math.round(Math.clamp(density, 0.0, 1.0) * 31.0);
        int packedX = width + (corner + (menu ? 64 : 0) + material * 128) * 4096;
        int readability = !menu && cfg.hudAutoReadability ? ("LIGHT".equals(cfg.themeMode) ? 2 : 1) : 0;
        int packedY = height + (softening + bending * 32 + readability * 1024) * 4096;
        RenderPipeline panelPipeline = !menu && HudRenderer.screenOverlay() ? (frameLow ? OVERLAY_LOW : (frameBlurLevel == 1 ? OVERLAY_HIGH : OVERLAY_BALANCED)) : framePipeline;
        graphics.method_25293(panelPipeline, SNAPSHOT, x, y, (float)(-packedX), (float)(-packedY), width, height, packedX * 2, packedY * 2, 1, 1, tint);
        return true;
    }

    public static boolean isGlassPipeline(RenderPipeline pipeline) {
        return pipeline == HIGH || pipeline == BALANCED || pipeline == LOW || pipeline == OVERLAY_HIGH || pipeline == OVERLAY_BALANCED || pipeline == OVERLAY_LOW;
    }

    public static void beginGuiRange() {
        capturedGuiKinds = 0;
    }

    public static void captureGuiBackground(RenderPipeline pipeline) {
        int kind;
        int n = kind = pipeline == OVERLAY_HIGH || pipeline == OVERLAY_BALANCED || pipeline == OVERLAY_LOW ? 2 : 1;
        if ((capturedGuiKinds & kind) != 0) {
            return;
        }
        capturedGuiKinds |= kind;
        if (!ready || snapshot == null || System.nanoTime() < retryAfter) {
            return;
        }
        try {
            GpuTexture source = class_310.method_1551().method_1522().method_30277();
            if (source == null || source.isClosed() || !snapshot.matches(source, frameBlurLevel)) {
                return;
            }
            RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(source, snapshot.method_68004(), 0, 0, 0, 0, 0, source.getWidth(0), source.getHeight(0));
            double amount = BloodHelperClient.config().glassBlur;
            snapshot.blur(Math.clamp(Double.isFinite(amount) ? amount : 6.0, 0.0, 16.0), frameLow ? BLUR_HORIZONTAL_LOW : BLUR_HORIZONTAL, frameLow ? BLUR_VERTICAL_LOW : BLUR_VERTICAL);
            lastBlurWidth = LiquidGlass.snapshot.scratch.getWidth(0);
            lastBlurHeight = LiquidGlass.snapshot.scratch.getHeight(0);
            ++capturedFrames;
            blurPasses += 2L;
            RenderMetrics.capture();
            long pixels = 2L * (long)LiquidGlass.snapshot.scratch.getWidth(0) * (long)LiquidGlass.snapshot.scratch.getHeight(0);
            blurredPixels += pixels;
            RenderMetrics.blur(pixels);
        }
        catch (RuntimeException failure) {
            LiquidGlass.failed("Unable to capture composed GUI for Liquid Glass", failure);
        }
    }

    public static long capturedFrames() {
        return capturedFrames;
    }

    public static boolean hasCurrentSnapshot() {
        return ready;
    }

    public static long blurPasses() {
        return blurPasses;
    }

    public static long blurredPixels() {
        return blurredPixels;
    }

    public static int lastBlurWidth() {
        return lastBlurWidth;
    }

    public static int lastBlurHeight() {
        return lastBlurHeight;
    }

    private static void failed(String reason, RuntimeException exception) {
        retryAfter = System.nanoTime() + 5000000000L;
        if (reportedFailure) {
            return;
        }
        reportedFailure = true;
        if (exception == null) {
            LOGGER.warn(reason);
        } else {
            LOGGER.warn(reason, (Throwable)exception);
        }
    }

    private static RenderPipeline glassPipeline(String quality, int mip, boolean low) {
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"bloodhelper", (String)("pipeline/liquid_glass_" + quality))).withVertexShader(class_2960.method_60655((String)"bloodhelper", (String)"core/liquid_glass")).withFragmentShader(class_2960.method_60655((String)"bloodhelper", (String)"core/liquid_glass")).withUniform("DynamicTransforms", class_10789.field_60031).withUniform("Projection", class_10789.field_60031).withSampler("Sampler0").withShaderDefine("GLASS_BLUR_LOD", mip).withVertexFormat(class_290.field_1575, VertexFormat.class_5596.field_27382).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false);
        if (low) {
            builder.withShaderDefine("GLASS_LOW");
        }
        return builder.build();
    }

    private static RenderPipeline blurPipeline(String name, boolean horizontal, boolean low) {
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"bloodhelper", (String)("pipeline/" + name))).withVertexShader(class_2960.method_60655((String)"bloodhelper", (String)"core/glass_blur")).withFragmentShader(class_2960.method_60655((String)"bloodhelper", (String)"core/glass_blur")).withSampler("Sampler0").withUniform("GlassBlur", class_10789.field_60031).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).withoutBlend();
        if (horizontal) {
            builder.withShaderDefine("HORIZONTAL");
        }
        if (low) {
            builder.withShaderDefine("BLUR_LOW");
        }
        return builder.build();
    }

    static {
        BLUR_HORIZONTAL = LiquidGlass.blurPipeline("glass_blur_horizontal", true, false);
        BLUR_VERTICAL = LiquidGlass.blurPipeline("glass_blur_vertical", false, false);
        BLUR_HORIZONTAL_LOW = LiquidGlass.blurPipeline("glass_blur_horizontal_low", true, true);
        BLUR_VERTICAL_LOW = LiquidGlass.blurPipeline("glass_blur_vertical_low", false, true);
    }

    private static final class SnapshotTexture
    extends class_1044 {
        private GpuTexture scratch;
        private GpuTextureView scratchView;
        private GpuTextureView rawView;
        private GpuTextureView blurredView;
        private GpuBuffer blurParameters;
        private final ByteBuffer blurValues = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());
        private final int blurLevel;

        SnapshotTexture(GpuTexture source, int blurLevel) {
            this.blurLevel = blurLevel;
            this.field_56974 = RenderSystem.getDevice().createTexture("BLOODHELPER glass background", 13, source.getFormat(), source.getWidth(0), source.getHeight(0), 1, blurLevel + 1);
            try {
                this.field_60597 = RenderSystem.getDevice().createTextureView(this.field_56974);
                this.rawView = RenderSystem.getDevice().createTextureView(this.field_56974, 0, 1);
                this.blurredView = RenderSystem.getDevice().createTextureView(this.field_56974, blurLevel, 1);
                this.scratch = RenderSystem.getDevice().createTexture("BLOODHELPER glass blur intermediate", 12, source.getFormat(), Math.max(1, source.getWidth(0) >> blurLevel), Math.max(1, source.getHeight(0) >> blurLevel), 1, 1);
                this.scratchView = RenderSystem.getDevice().createTextureView(this.scratch);
                this.blurParameters = RenderSystem.getDevice().createBuffer(() -> "BLOODHELPER glass blur settings", 136, 16L);
                this.field_63613 = RenderSystem.getSamplerCache().method_76520(FilterMode.LINEAR, true);
            }
            catch (RuntimeException failure) {
                this.close();
                throw failure;
            }
        }

        void blur(double amount, RenderPipeline horizontal, RenderPipeline vertical) {
            float pixels = (float)(amount * (double)class_310.method_1551().method_22683().method_4495() * 0.5);
            this.blurValues.clear();
            this.blurValues.putFloat(pixels / (float)this.field_56974.getWidth(0)).putFloat(pixels / (float)this.field_56974.getHeight(0));
            this.blurValues.putFloat(0.0f).putFloat(0.0f).flip();
            CommandEncoder commands = RenderSystem.getDevice().createCommandEncoder();
            commands.writeToBuffer(this.blurParameters.slice(), this.blurValues);
            this.blurPass(this.rawView, this.scratchView, horizontal);
            this.blurPass(this.scratchView, this.blurredView, vertical);
        }

        private void blurPass(GpuTextureView source, GpuTextureView destination, RenderPipeline pipeline) {
            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "BLOODHELPER glass Gaussian blur", destination, OptionalInt.empty());){
                pass.setPipeline(pipeline);
                pass.bindTexture("Sampler0", source, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
                pass.setUniform("GlassBlur", this.blurParameters);
                pass.draw(0, 3);
            }
        }

        public void close() {
            if (this.rawView != null) {
                this.rawView.close();
                this.rawView = null;
            }
            if (this.blurredView != null) {
                this.blurredView.close();
                this.blurredView = null;
            }
            if (this.scratchView != null) {
                this.scratchView.close();
                this.scratchView = null;
            }
            if (this.scratch != null) {
                this.scratch.close();
                this.scratch = null;
            }
            if (this.blurParameters != null) {
                this.blurParameters.close();
                this.blurParameters = null;
            }
            super.close();
        }

        boolean matches(GpuTexture source, int level) {
            return this.blurLevel == level && this.field_56974 != null && !this.field_56974.isClosed() && this.field_56974.getFormat() == source.getFormat() && this.field_56974.getWidth(0) == source.getWidth(0) && this.field_56974.getHeight(0) == source.getHeight(0);
        }
    }
}

