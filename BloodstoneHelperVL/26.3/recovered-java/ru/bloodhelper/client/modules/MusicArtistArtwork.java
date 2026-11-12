/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 */
package ru.bloodhelper.client.modules;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;

final class MusicArtistArtwork
implements AutoCloseable {
    private static final AtomicLong SEQUENCE = new AtomicLong();
    private final Identifier texture = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)("music-search/artist-" + SEQUENCE.incrementAndGet()));
    private byte[] observed;
    private int width;
    private int height;
    private long loadedAt;

    MusicArtistArtwork() {
    }

    void draw(GuiGraphicsExtractor g, byte[] bytes, String name, int x, int y, int size) {
        if (bytes != this.observed) {
            this.close();
            this.observed = bytes;
            if (bytes != null) {
                try {
                    NativeImage image = NativeImage.read((InputStream)new ByteArrayInputStream(bytes));
                    if (image.getWidth() > 512 || image.getHeight() > 512) {
                        image.close();
                    } else {
                        this.width = image.getWidth();
                        this.height = image.getHeight();
                        this.loadedAt = System.nanoTime();
                        Minecraft.getInstance().getTextureManager().register(this.texture, (AbstractTexture)new DynamicTexture(() -> "BloodHelper artist portrait", image));
                    }
                }
                catch (Exception ignored) {
                    this.height = 0;
                    this.width = 0;
                }
            }
        }
        int background = ThemeColors.surface(-14079438);
        UiDraw.rounded(g, x, y, size, size, 12, background);
        if (this.width > 0) {
            double fade;
            double fit = ((double)size - 6.0) / (double)Math.max(this.width, this.height);
            int w = Math.max(1, (int)((double)this.width * fit));
            int h = Math.max(1, (int)((double)this.height * fit));
            g.blit(RenderPipelines.GUI_TEXTURED, this.texture, x + (size - w) / 2, y + (size - h) / 2, 0.0f, 0.0f, w, h, this.width, this.height, this.width, this.height);
            double d = fade = BloodHelperClient.config().animations ? Math.clamp((double)(System.nanoTime() - this.loadedAt) / 2.4E8, 0.0, 1.0) : 1.0;
            if (fade < 1.0) {
                g.fill(x + 3, y + 3, x + size - 3, y + size - 3, UiDraw.alpha(background, (int)((1.0 - fade) * 255.0)));
            }
        } else {
            String initial = name.isBlank() ? "\u266a" : name.substring(0, name.offsetByCodePoints(0, 1)).toUpperCase(Locale.ROOT);
            CustomFont font = CustomFont.medium();
            int textSize = Math.min(28, Math.max(10, size / 2));
            font.draw(g, initial, (float)x + ((float)size - font.width(initial, textSize)) / 2.0f, y + (size - textSize - 4) / 2, textSize, ThemeColors.accent());
        }
    }

    @Override
    public void close() {
        if (this.width > 0) {
            Minecraft.getInstance().getTextureManager().release(this.texture);
        }
        this.height = 0;
        this.width = 0;
        this.observed = null;
    }
}

