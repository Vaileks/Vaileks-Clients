/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.renderpearl.api.textures.FilterMode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.Util
 *  org.lwjgl.sdl.SDLMisc
 */
package ru.bloodhelper.client.modules;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.sdl.SDLMisc;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.DeathScreenshotFiles;
import ru.bloodhelper.client.modules.DeathScreenshotModule;
import ru.bloodhelper.client.modules.ModuleDetailScreen;
import ru.bloodhelper.client.modules.StreamerMode;

public final class DeathScreenshotViewer
extends ModuleDetailScreen {
    private final String filename;
    private final Identifier texture = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)("death_preview/" + String.valueOf(UUID.randomUUID())));
    private int generation;
    private int imageWidth;
    private int imageHeight;
    private boolean ready;
    private boolean loading;
    private boolean failed;

    @Override
    protected int preferredPanelHeight() {
        return this.ready && !StreamerMode.enabled() ? 410 : 194;
    }

    public DeathScreenshotViewer(Screen parent, String filename) {
        super(parent, HudStyle.tr("\u0421\u043a\u0440\u0438\u043d\u0448\u043e\u0442 \u0441\u043c\u0435\u0440\u0442\u0438", "Death Screenshot"));
        this.filename = filename;
    }

    protected void init() {
        this.load();
    }

    private void load() {
        if (StreamerMode.enabled() || this.ready || this.loading) {
            return;
        }
        int token = ++this.generation;
        this.loading = true;
        this.failed = false;
        Minecraft client = this.minecraft;
        Path gameDirectory = client.gameDirectory.toPath();
        Util.ioPool().execute(() -> {
            NativeImage preview = null;
            try {
                Path path = DeathScreenshotFiles.readable(gameDirectory, this.filename);
                try (InputStream stream = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS);
                     NativeImage original = NativeImage.read((InputStream)stream);){
                    double factor = Math.min(1.0, Math.min(1600.0 / (double)original.getWidth(), 900.0 / (double)original.getHeight()));
                    int w = Math.max(1, (int)Math.round((double)original.getWidth() * factor));
                    int h = Math.max(1, (int)Math.round((double)original.getHeight() * factor));
                    preview = new NativeImage(w, h, false);
                    original.resizeSubRectTo(0, 0, original.getWidth(), original.getHeight(), preview);
                }
            }
            catch (Exception ignored) {
                if (preview != null) {
                    preview.close();
                }
                preview = null;
            }
            NativeImage result = preview;
            client.execute(() -> {
                if (token != this.generation) {
                    if (result != null) {
                        result.close();
                    }
                    return;
                }
                this.loading = false;
                if (result == null) {
                    this.failed = true;
                    return;
                }
                try {
                    this.imageWidth = result.getWidth();
                    this.imageHeight = result.getHeight();
                    client.getTextureManager().register(this.texture, (AbstractTexture)new PreviewTexture(result));
                    this.ready = true;
                }
                catch (RuntimeException error) {
                    result.close();
                    this.failed = true;
                }
            });
        });
    }

    public void removed() {
        ++this.generation;
        this.loading = false;
        if (this.ready) {
            this.minecraft.getTextureManager().release(this.texture);
            this.ready = false;
        }
    }

    private void openOriginal() {
        if (StreamerMode.enabled()) {
            return;
        }
        Path gameDirectory = this.minecraft.gameDirectory.toPath();
        Util.ioPool().execute(() -> {
            try {
                Path file = DeathScreenshotFiles.readable(gameDirectory, this.filename);
                this.minecraft.execute(() -> {
                    if (!StreamerMode.enabled()) {
                        SDLMisc.SDL_OpenURL((CharSequence)file.toUri().toString());
                    }
                });
            }
            catch (Exception ignored) {
                this.minecraft.execute(() -> {
                    this.failed = true;
                });
            }
        });
    }

    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        this.frame(g, HudStyle.tr("\u0421\u043a\u0440\u0438\u043d\u0448\u043e\u0442 \u0441\u043c\u0435\u0440\u0442\u0438", "Death Screenshot"), mouseX, mouseY);
        if (StreamerMode.enabled()) {
            String label = HudStyle.tr("\u0418\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u0441\u043a\u0440\u044b\u0442\u043e \u0432 Streamer Mode: \u043d\u0430 \u0441\u0442\u0430\u0440\u043e\u043c \u0441\u043a\u0440\u0438\u043d\u0448\u043e\u0442\u0435 \u043c\u043e\u0433\u0443\u0442 \u043e\u0441\u0442\u0430\u0442\u044c\u0441\u044f \u0447\u0430\u0442, \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b \u0438 \u043d\u0438\u043a\u0438.", "Image hidden by Streamer Mode: an older screenshot may contain chat, coordinates and player names.");
            int y = this.bodyTop + 20;
            for (String line : DeathScreenshotViewer.wrap(label, this.panelWidth - 40)) {
                this.text(g, line, this.left + 20, y, -7105633);
                y += 16;
            }
            this.footer(g, HudStyle.tr("\u041e\u0442\u043a\u043b\u044e\u0447\u0438 Streamer Mode, \u0447\u0442\u043e\u0431\u044b \u043f\u043e\u0441\u043c\u043e\u0442\u0440\u0435\u0442\u044c \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435", "Disable Streamer Mode to view the image"));
            return;
        }
        if (!(this.ready || this.loading || this.failed)) {
            this.load();
        }
        this.button(g, this.left + 14, this.top + 47, 138, HudStyle.tr("\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u043e\u0440\u0438\u0433\u0438\u043d\u0430\u043b", "Open original"), mouseX, mouseY, this::openOriginal);
        if (this.failed) {
            this.button(g, this.left + 160, this.top + 47, 94, HudStyle.tr("\u041f\u043e\u0432\u0442\u043e\u0440\u0438\u0442\u044c", "Retry"), mouseX, mouseY, this::load);
        }
        this.bodyTop = this.top + 80;
        if (this.ready) {
            float factor = Math.min(((float)this.panelWidth - 28.0f) / (float)this.imageWidth, Math.max(1.0f, (float)(this.bodyBottom - this.bodyTop)) / (float)this.imageHeight);
            int w = Math.max(1, Math.round((float)this.imageWidth * factor));
            int h = Math.max(1, Math.round((float)this.imageHeight * factor));
            int x = this.left + (this.panelWidth - w) / 2;
            int y = this.bodyTop + (this.bodyBottom - this.bodyTop - h) / 2;
            g.blit(RenderPipelines.GUI_TEXTURED, this.texture, x, y, 0.0f, 0.0f, w, h, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        } else {
            boolean saving;
            DeathScreenshotModule.Capture capture = DeathScreenshotModule.lastCapture();
            boolean bl = saving = capture != null && capture.filename().equals(this.filename) && capture.state() == DeathScreenshotModule.State.SAVING;
            String label = saving ? HudStyle.tr("\u0418\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u0435\u0449\u0451 \u0441\u043e\u0445\u0440\u0430\u043d\u044f\u0435\u0442\u0441\u044f. \u041d\u0430\u0436\u043c\u0438 \u00ab\u041f\u043e\u0432\u0442\u043e\u0440\u0438\u0442\u044c\u00bb \u0447\u0435\u0440\u0435\u0437 \u0441\u0435\u043a\u0443\u043d\u0434\u0443.", "The image is still saving. Press Retry in a moment.") : (this.loading ? HudStyle.tr("\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0438\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u044f\u2026", "Loading the image\u2026") : HudStyle.tr("\u0418\u0437\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0438\u043b\u0438 \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0435\u0433\u043e \u043f\u0440\u043e\u0447\u0438\u0442\u0430\u0442\u044c.", "The image is missing or could not be read."));
            int y = this.bodyTop + 20;
            for (String line : DeathScreenshotViewer.wrap(label, this.panelWidth - 40)) {
                this.text(g, line, this.left + 20, y, -7105633);
                y += 16;
            }
        }
        this.footer(g, HudStyle.tr("\u041b\u043e\u043a\u0430\u043b\u044c\u043d\u044b\u0439 PNG \u00b7 \u0411\u0435\u0437 \u043e\u0442\u043f\u0440\u0430\u0432\u043a\u0438 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440", "Local PNG \u00b7 Never uploaded"));
    }

    private static final class PreviewTexture
    extends DynamicTexture {
        private PreviewTexture(NativeImage image) {
            super(() -> "BloodHelper death screenshot preview", image);
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        }
    }
}

