/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.renderpearl.api.buffers.GpuBuffer
 *  com.mojang.renderpearl.api.commands.RenderPass
 *  com.mojang.renderpearl.api.pipeline.BindGroupLayout
 *  com.mojang.renderpearl.api.pipeline.ColorTargetState
 *  com.mojang.renderpearl.api.pipeline.PrimitiveTopology
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline$Builder
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline$Snippet
 *  com.mojang.renderpearl.api.pipeline.UniformType
 *  com.mojang.renderpearl.api.textures.FilterMode
 *  com.mojang.renderpearl.api.textures.GpuTexture
 *  com.mojang.renderpearl.api.textures.GpuTextureView
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.TextureManager
 *  net.minecraft.resources.Identifier
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
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
    private static final Identifier TARGET = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"dynamic/menu_backdrop");
    private static final RenderPipeline HORIZONTAL = MenuBackdrop.pipeline("horizontal", true, false);
    private static final RenderPipeline VERTICAL = MenuBackdrop.pipeline("vertical", false, false);
    private static final RenderPipeline HORIZONTAL_LOW = MenuBackdrop.pipeline("horizontal_low", true, true);
    private static final RenderPipeline VERTICAL_LOW = MenuBackdrop.pipeline("vertical_low", false, true);
    private static final RenderPipeline COMPOSITE = MenuBackdrop.compositePipeline();
    private static GuiGraphicsExtractor scheduledFrame;
    private static Screen scheduledScreen;
    private static Object scheduledWorld;
    private static double scheduledAmount;
    private static int scheduledMip;
    private static boolean scheduledLow;
    private static boolean pending;
    private static BackdropTexture target;
    private static TextureManager targetOwner;
    private static long retryAfter;
    private static long processedFrames;
    private static long blurPasses;
    private static long compositePasses;
    private static long failureCount;
    private static boolean reportedFailure;

    private MenuBackdrop() {
    }

    public static boolean handlesScreen(Screen screen) {
        return screen instanceof BloodHelperScreen || screen instanceof BackgroundMenuScreen;
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (ReGlassCompat.installed() || EmbeddedReGlass.active()) {
            return;
        }
        if (scheduledFrame == graphics) {
            return;
        }
        scheduledFrame = graphics;
        pending = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !MenuBackdrop.handlesScreen(mc.gui.screen()) || !RenderSystem.isOnRenderThread()) {
            return;
        }
        double amount = BloodHelperClient.config().glassBlur;
        if (!Double.isFinite(amount) || amount <= 0.0 || System.nanoTime() < retryAfter) {
            return;
        }
        RenderQualitySettings quality = RenderQuality.settings();
        scheduledScreen = mc.gui.screen();
        scheduledWorld = mc.level;
        scheduledAmount = Math.clamp(amount, 0.0, 16.0);
        scheduledMip = quality.blurMipLevel();
        scheduledLow = !quality.dispersion();
        graphics.nextStratum();
        graphics.blurBeforeThisStratum();
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
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() != scheduledScreen || mc.level != scheduledWorld || mc.level == null || !MenuBackdrop.handlesScreen(mc.gui.screen()) || !RenderSystem.isOnRenderThread() || System.nanoTime() < retryAfter) {
            return true;
        }
        try {
            RenderPipeline vertical;
            RenderTarget main = mc.gameRenderer.mainRenderTarget();
            GpuTexture source = main.getColorTexture();
            GpuTextureView sourceView = main.getColorTextureView();
            if (source == null || source.isClosed() || sourceView == null || sourceView.isClosed() || source.getWidth(0) < 2 || source.getHeight(0) < 2) {
                return true;
            }
            RenderPipeline horizontal = scheduledLow ? HORIZONTAL_LOW : HORIZONTAL;
            RenderPipeline renderPipeline = vertical = scheduledLow ? VERTICAL_LOW : VERTICAL;
            if (RenderSystem.getCompiledPipelineNullable((RenderPipeline)horizontal) == null || RenderSystem.getCompiledPipelineNullable((RenderPipeline)vertical) == null || RenderSystem.getCompiledPipelineNullable((RenderPipeline)COMPOSITE) == null) {
                MenuBackdrop.failed("Menu blur shader is unavailable; keeping the HUD and world sharp", null);
                return true;
            }
            TextureManager manager = mc.getTextureManager();
            if (targetOwner != manager || target == null || !target.matches(source, scheduledMip)) {
                BackdropTexture replacement = new BackdropTexture(source, scheduledMip);
                try {
                    manager.register(TARGET, (AbstractTexture)replacement);
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
            RenderMetrics.blur(2L * (long)target.getTexture().getWidth(0) * (long)target.getTexture().getHeight(0));
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
        RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)("pipeline/menu_backdrop_" + name))).withVertexShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/glass_blur")).withFragmentShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/menu_backdrop_blur")).withBindGroupLayout(BindGroupLayout.builder().withUniform("Sampler0", UniformType.COMBINED_IMAGE_SAMPLER).withUniform("GlassBlur", UniformType.UNIFORM_BUFFER).build()).withPrimitiveTopology(PrimitiveTopology.TRIANGLES).withDepthStencilState(Optional.empty()).withCull(false).withColorTargetState(ColorTargetState.DEFAULT);
        if (horizontal) {
            builder.withShaderDefine("HORIZONTAL");
        }
        if (low) {
            builder.withShaderDefine("BLUR_LOW");
        }
        return builder.build();
    }

    private static RenderPipeline compositePipeline() {
        return RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"pipeline/menu_backdrop_composite")).withVertexShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/glass_blur")).withFragmentShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/menu_backdrop_blit")).withBindGroupLayout(BindGroupLayout.builder().withUniform("Sampler0", UniformType.COMBINED_IMAGE_SAMPLER).build()).withPrimitiveTopology(PrimitiveTopology.TRIANGLES).withDepthStencilState(Optional.empty()).withCull(false).withColorTargetState(ColorTargetState.DEFAULT).build();
    }

    private static final class BackdropTexture
    extends AbstractTexture {
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
                this.texture = RenderSystem.getDevice().createTexture("BLOODHELPER menu backdrop intermediate", 12, source.getFormat(), Math.max(1, this.sourceWidth >> mip), Math.max(1, this.sourceHeight >> mip), 1, 1);
                this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
                this.blurredTexture = RenderSystem.getDevice().createTexture("BLOODHELPER menu backdrop blurred", 12, source.getFormat(), this.texture.getWidth(0), this.texture.getHeight(0), 1, 1);
                this.blurredView = RenderSystem.getDevice().createTextureView(this.blurredTexture);
                this.parameters = RenderSystem.getDevice().createBuffer(() -> "BLOODHELPER menu backdrop settings", 136, 16L);
                this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
            }
            catch (RuntimeException failure) {
                this.close();
                throw failure;
            }
        }

        boolean matches(GpuTexture source, int level) {
            return this.mip == level && this.sourceWidth == source.getWidth(0) && this.sourceHeight == source.getHeight(0) && this.texture != null && !this.texture.isClosed() && this.texture.getFormat() == source.getFormat() && this.textureView != null && !this.textureView.isClosed() && this.parameters != null && !this.parameters.isClosed() && this.blurredTexture != null && !this.blurredTexture.isClosed() && this.blurredView != null && !this.blurredView.isClosed();
        }

        void blur(GpuTexture source, GpuTextureView sourceView, double amount, RenderPipeline horizontal, RenderPipeline vertical) {
            float pixels = (float)(amount * (double)Minecraft.getInstance().getWindow().getGuiScale() * 0.5);
            this.values.clear();
            this.values.putFloat(pixels / (float)source.getWidth(0)).putFloat(pixels / (float)source.getHeight(0));
            this.values.putFloat(1.0f / (float)this.texture.getWidth(0)).putFloat(1.0f / (float)this.texture.getHeight(0)).flip();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.parameters.slice(), this.values);
            this.pass(sourceView, this.textureView, horizontal);
            this.pass(this.textureView, this.blurredView, vertical);
            this.pass(this.blurredView, sourceView, COMPOSITE);
        }

        private void pass(GpuTextureView source, GpuTextureView destination, RenderPipeline pipeline) {
            try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "BLOODHELPER menu backdrop blur", destination, Optional.empty());){
                pass.setPipeline(RenderSystem.getCompiledPipeline((RenderPipeline)pipeline));
                pass.setUniform("Sampler0", source, this.sampler);
                if (pipeline != COMPOSITE) {
                    pass.setUniform("GlassBlur", this.parameters);
                }
                pass.draw(3, 1, 0, 0);
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
            if (this.textureView != null) {
                this.textureView.close();
                this.textureView = null;
            }
            if (this.parameters != null) {
                this.parameters.close();
                this.parameters = null;
            }
            super.close();
        }
    }
}

