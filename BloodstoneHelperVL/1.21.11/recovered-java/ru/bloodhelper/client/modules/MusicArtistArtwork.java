/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_10799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.modules;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;

final class MusicArtistArtwork
implements AutoCloseable {
    private static final AtomicLong SEQUENCE = new AtomicLong();
    private final class_2960 texture = class_2960.method_60655((String)"bloodhelper", (String)("music-search/artist-" + SEQUENCE.incrementAndGet()));
    private byte[] observed;
    private int width;
    private int height;
    private long loadedAt;

    MusicArtistArtwork() {
    }

    void draw(class_332 g, byte[] bytes, String name, int x, int y, int size) {
        if (bytes != this.observed) {
            this.close();
            this.observed = bytes;
            if (bytes != null) {
                try {
                    class_1011 image = class_1011.method_4309((InputStream)new ByteArrayInputStream(bytes));
                    if (image.method_4307() > 512 || image.method_4323() > 512) {
                        image.close();
                    } else {
                        this.width = image.method_4307();
                        this.height = image.method_4323();
                        this.loadedAt = System.nanoTime();
                        class_310.method_1551().method_1531().method_4616(this.texture, (class_1044)new class_1043(() -> "BloodHelper artist portrait", image));
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
            g.method_25302(class_10799.field_56883, this.texture, x + (size - w) / 2, y + (size - h) / 2, 0.0f, 0.0f, w, h, this.width, this.height, this.width, this.height);
            double d = fade = BloodHelperClient.config().animations ? Math.clamp((double)(System.nanoTime() - this.loadedAt) / 2.4E8, 0.0, 1.0) : 1.0;
            if (fade < 1.0) {
                g.method_25294(x + 3, y + 3, x + size - 3, y + size - 3, UiDraw.alpha(background, (int)((1.0 - fade) * 255.0)));
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
            class_310.method_1551().method_1531().method_4615(this.texture);
        }
        this.height = 0;
        this.width = 0;
        this.observed = null;
    }
}

