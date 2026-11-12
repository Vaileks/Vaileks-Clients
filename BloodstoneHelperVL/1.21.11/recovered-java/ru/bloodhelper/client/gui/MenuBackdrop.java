/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Builder
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1044
 *  net.minecraft.class_1060
 *  net.minecraft.class_10789
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
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
import net.minecraft.class_1060;
import net.minecraft.class_10789;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.RenderQualitySettings;
import ru.bloodhelper.client.gui.BackgroundMenuScreen;
import ru.bloodhelper.client.gui.BloodHelperScreen;
import ru.bloodhelper.client.gui.EmbeddedReGlass;
import ru.bloodhelper.client.gui.ReGlassCompat;
import ru.bloodhelper.client.gui.RenderMetrics;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.hud.HeldCooldownWidget;
import ru.bloodhelper.client.hud.WorldWaypoints;

public final class MenuBackdrop {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"BloodHelper/MenuBackdrop");
    private static final class_2960 TARGET = class_2960.method_60655((String)"bloodhelper", (String)"dynamic/menu_backdrop");
    private static final RenderPipeline HORIZONTAL = MenuBackdrop.pipeline("horizontal", true, false);
    private static final RenderPipeline VERTICAL = MenuBackdrop.pipeline("vertical", false, false);
    private static final RenderPipeline HORIZONTAL_LOW = MenuBackdrop.pipeline("horizontal_low", true, true);
    private static final RenderPipeline VERTICAL_LOW = MenuBackdrop.pipeline("vertical_low", false, true);
    private static final RenderPipeline COMPOSITE = MenuBackdrop.compositePipeline();
    private static class_332 scheduledFrame;
    private static class_437 scheduledScreen;
    private static Object scheduledWorld;
    private static double scheduledAmount;
    private static int scheduledMip;
    private static boolean scheduledLow;
    private static boolean pending;
    private static BackdropTexture target;
    private static class_1060 targetOwner;
    private static long retryAfter;
    private static long processedFrames;
    private static long blurPasses;
    private static long compositePasses;
    private static long failureCount;
    private static boolean reportedFailure;

    private MenuBackdrop() {
    }

    public static boolean handlesScreen(class_437 screen) {
        return screen instanceof BloodHelperScreen || screen instanceof BackgroundMenuScreen;
    }

    public static void render(class_332 graphics) {
        if (ReGlassCompat.installed() || EmbeddedReGlass.active()) {
            return;
        }
        if (scheduledFrame == graphics) {
            return;
        }
        scheduledFrame = graphics;
        pending = false;
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || !MenuBackdrop.handlesScreen(mc.field_1755) || !RenderSystem.isOnRenderThread()) {
            return;
        }
        double amount = BloodHelperClient.config().glassBlur;
        if (!Double.isFinite(amount) || amount <= 0.0 || System.nanoTime() < retryAfter) {
            return;
        }
        RenderQualitySettings quality = RenderQuality.settings();
        scheduledScreen = mc.field_1755;
        scheduledWorld = mc.field_1687;
        scheduledAmount = Math.clamp(amount, 0.0, 16.0);
        scheduledMip = quality.blurMipLevel();
        scheduledLow = !quality.dispersion();
        graphics.method_71048();
        graphics.method_71278();
        pending = true;
    }

    public static void beginFrame() {
        ReGlassCompat.beginFrame();
        HeldCooldownWidget.clear();
        WorldWaypoints.beginFrame();
        pending = false;
        scheduledFrame = null;
        scheduledScreen = null;
        scheduledWorld = null;
    }

    public static boolean processBlur() {
        if (!pending) {
            return false;
        }
        pending = false;
        class_310 mc = class_310.method_1551();
        if (mc.field_1755 != scheduledScreen || mc.field_1687 != scheduledWorld || mc.field_1687 == null || !MenuBackdrop.handlesScreen(mc.field_1755) || !RenderSystem.isOnRenderThread() || System.nanoTime() < retryAfter) {
            return true;
        }
        try {
            RenderPipeline vertical;
            class_276 main = mc.method_1522();
            GpuTexture source = main.method_30277();
            GpuTextureView sourceView = main.method_71639();
            if (source == null || source.isClosed() || sourceView == null || sourceView.isClosed() || source.getWidth(0) < 2 || source.getHeight(0) < 2) {
                return true;
            }
            RenderPipeline horizontal = scheduledLow ? HORIZONTAL_LOW : HORIZONTAL;
            RenderPipeline renderPipeline = vertical = scheduledLow ? VERTICAL_LOW : VERTICAL;
            if (!(RenderSystem.getDevice().precompilePipeline(horizontal).isValid() && RenderSystem.getDevice().precompilePipeline(vertical).isValid() && RenderSystem.getDevice().precompilePipeline(COMPOSITE).isValid())) {
                MenuBackdrop.failed("Menu blur shader is unavailable; keeping the HUD and world sharp", null);
                return true;
            }
            class_1060 manager = mc.method_1531();
            if (targetOwner != manager || target == null || !target.matches(source, scheduledMip)) {
                BackdropTexture replacement = new BackdropTexture(source, scheduledMip);
                try {
                    manager.method_4616(TARGET, (class_1044)replacement);
                }
                catch (RuntimeException failure) {
                    replacement.close();
                    throw failure;
                }
                target = replacement;
                targetOwner = manager;
            }
            target.blur(source, sourceView, scheduledAmount, horizontal, vertical);
            ++processedFrames;
            blurPasses += 2L;
            ++compositePasses;
            RenderMetrics.blur(2L * (long)target.method_68004().getWidth(0) * (long)target.method_68004().getHeight(0));
        }
        catch (RuntimeException failure) {
            MenuBackdrop.failed("Unable to blur the menu backdrop; keeping the HUD and world sharp", failure);
        }
        return true;
    }

    public static long processedFrames() {
        return processedFrames;
    }

    public static long blurPasses() {
        return blurPasses;
    }

    public static long compositePasses() {
        return compositePasses;
    }

    public static long failureCount() {
        return failureCount;
    }

    private static void failed(String message, RuntimeException failure) {
        ++failureCount;
        retryAfter = System.nanoTime() + 5000000000L;
        if (reportedFailure) {
            return;
        }
        reportedFailure = true;
        if (failure == null) {
            LOGGER.warn(message);
        } else {
            LOGGER.warn(message, (Throwable)failure);
        }
    }

    private static RenderPipeline pipeline(String name, boolean horizontal, boolean low) {
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"bloodhelper", (String)("pipeline/menu_backdrop_" + name))).withVertexShader(class_2960.method_60655((String)"bloodhelper", (String)"core/glass_blur")).withFragmentShader(class_2960.method_60655((String)"bloodhelper", (String)"core/menu_backdrop_blur")).withSampler("Sampler0").withUniform("GlassBlur", class_10789.field_60031).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).withoutBlend();
        if (horizontal) {
            builder.withShaderDefine("HORIZONTAL");
        }
        if (low) {
            builder.withShaderDefine("BLUR_LOW");
        }
        return builder.build();
    }

    private static RenderPipeline compositePipeline() {
        return RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"bloodhelper", (String)"pipeline/menu_backdrop_composite")).withVertexShader(class_2960.method_60655((String)"bloodhelper", (String)"core/glass_blur")).withFragmentShader(class_2960.method_60655((String)"bloodhelper", (String)"core/menu_backdrop_blit")).withSampler("Sampler0").withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).withoutBlend().build();
    }

    private static final class BackdropTexture
    extends class_1044 {
        private final int sourceWidth;
        private final int sourceHeight;
        private final int mip;
        private final ByteBuffer values = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());
        private GpuBuffer parameters;
        private GpuTexture blurredTexture;
        private GpuTextureView blurredView;

        BackdropTexture(GpuTexture source, int mip) {
            this.sourceWidth = source.getWidth(0);
            this.sourceHeight = source.getHeight(0);
            this.mip = mip;
            try {
                this.field_56974 = RenderSystem.getDevice().createTexture("BLOODHELPER menu backdrop intermediate", 12, source.getFormat(), Math.max(1, this.sourceWidth >> mip), Math.max(1, this.sourceHeight >> mip), 1, 1);
                this.field_60597 = RenderSystem.getDevice().createTextureView(this.field_56974);
                this.blurredTexture = RenderSystem.getDevice().createTexture("BLOODHELPER menu backdrop blurred", 12, source.getFormat(), this.field_56974.getWidth(0), this.field_56974.getHeight(0), 1, 1);
                this.blurredView = RenderSystem.getDevice().createTextureView(this.blurredTexture);
                this.parameters = RenderSystem.getDevice().createBuffer(() -> "BLOODHELPER menu backdrop settings", 136, 16L);
                this.field_63613 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
            }
            catch (RuntimeException failure) {
                this.close();
                throw failure;
            }
        }

        boolean matches(GpuTexture source, int level) {
            return this.mip == level && this.sourceWidth == source.getWidth(0) && this.sourceHeight == source.getHeight(0) && this.field_56974 != null && !this.field_56974.isClosed() && this.field_56974.getFormat() == source.getFormat() && this.field_60597 != null && !this.field_60597.isClosed() && this.parameters != null && !this.parameters.isClosed() && this.blurredTexture != null && !this.blurredTexture.isClosed() && this.blurredView != null && !this.blurredView.isClosed();
        }

        void blur(GpuTexture source, GpuTextureView sourceView, double amount, RenderPipeline horizontal, RenderPipeline vertical) {
            float pixels = (float)(amount * (double)class_310.method_1551().method_22683().method_4495() * 0.5);
            this.values.clear();
            this.values.putFloat(pixels / (float)source.getWidth(0)).putFloat(pixels / (float)source.getHeight(0));
            this.values.putFloat(1.0f / (float)this.field_56974.getWidth(0)).putFloat(1.0f / (float)this.field_56974.getHeight(0)).flip();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.parameters.slice(), this.values);
            this.pass(sourceView, this.field_60597, horizontal);
            this.pass(this.field_60597, this.blurredView, vertical);
            this.pass(this.blurredView, sourceView, COMPOSITE);
        }

        private void pass(GpuTextureView source, GpuTextureView destination, RenderPipeline pipeline) {
            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "BLOODHELPER menu backdrop blur", destination, OptionalInt.empty());){
                pass.setPipeline(pipeline);
                pass.bindTexture("Sampler0", source, this.field_63613);
                if (pipeline != COMPOSITE) {
                    pass.setUniform("GlassBlur", this.parameters);
                }
                pass.draw(0, 3);
            }
        }

        public void close() {
            if (this.blurredView != null) {
                this.blurredView.close();
                this.blurredView = null;
            }
            if (this.blurredTexture != null) {
                this.blurredTexture.close();
                this.blurredTexture = null;
            }
            if (this.field_60597 != null) {
                this.field_60597.close();
                this.field_60597 = null;
            }
            if (this.parameters != null) {
                this.parameters.close();
                this.parameters = null;
            }
            super.close();
        }
    }
}

