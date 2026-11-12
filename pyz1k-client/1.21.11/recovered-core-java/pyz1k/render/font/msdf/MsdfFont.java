/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_1044
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4588
 *  org.joml.Matrix4f
 */
package pyz1k.render.font.msdf;

import com.google.gson.Gson;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4588;
import org.joml.Matrix4f;
import pyz1k.render.font.msdf.FontData;
import pyz1k.render.font.msdf.MsdfGlyph;

public final class MsdfFont {
    private static final Gson GSON = new Gson();
    private final class_1044 texture;
    private final FontData.AtlasData atlas;
    private final FontData.MetricsData metrics;
    private final Map<Integer, MsdfGlyph> glyphs;
    private final Map<Integer, Map<Integer, Float>> kernings;
    private final ConcurrentHashMap<Long, Float> widthCache = new ConcurrentHashMap();

    private MsdfFont(class_1044 texture, FontData.AtlasData atlas, FontData.MetricsData metrics, Map<Integer, MsdfGlyph> glyphs, Map<Integer, Map<Integer, Float>> kernings) {
        this.texture = texture;
        this.atlas = atlas;
        this.metrics = metrics;
        this.glyphs = glyphs;
        this.kernings = kernings;
    }

    public static MsdfFont load(class_2960 jsonId, class_2960 atlasId) {
        try {
            FontData data;
            class_310 client = class_310.method_1551();
            if (client == null) {
                throw new IllegalStateException("MinecraftClient is not initialized");
            }
            try (InputStream in = client.method_1478().open(jsonId);
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);){
                data = (FontData)GSON.fromJson((Reader)reader, FontData.class);
            }
            if (data != null && data.atlas() != null && data.metrics() != null) {
                class_1044 texture = client.method_1531().method_4619(atlasId);
                float atlasW = data.atlas().width();
                float atlasH = data.atlas().height();
                HashMap<Integer, MsdfGlyph> glyphs = new HashMap<Integer, MsdfGlyph>();
                if (data.glyphs() != null) {
                    for (FontData.GlyphData glyphData : data.glyphs()) {
                        glyphs.put(glyphData.unicode(), new MsdfGlyph(glyphData, atlasW, atlasH));
                    }
                }
                HashMap<Integer, Map<Integer, Float>> kernings = new HashMap<Integer, Map<Integer, Float>>();
                if (data.kernings() != null) {
                    for (FontData.KerningData kerning : data.kernings()) {
                        kernings.computeIfAbsent(kerning.leftChar(), k -> new HashMap()).put(kerning.rightChar(), Float.valueOf(kerning.advance()));
                    }
                }
                return new MsdfFont(texture, data.atlas(), data.metrics(), glyphs, kernings);
            }
            throw new IllegalStateException("Invalid MSDF data: " + String.valueOf(jsonId));
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load MSDF font: " + String.valueOf(jsonId) + " / " + String.valueOf(atlasId), e);
        }
    }

    public GpuTextureView textureView() {
        return this.texture.method_71659();
    }

    public float range() {
        return this.atlas.range();
    }

    public float lineHeight(float size) {
        return this.metrics.lineHeight() * size;
    }

    public float getWidth(String text, float size) {
        if (text != null && !text.isEmpty()) {
            long key = (long)text.hashCode() & 0xFFFFFFFFL ^ (long)Float.floatToIntBits(size) << 32;
            Float cached = this.widthCache.get(key);
            if (cached != null) {
                return cached.floatValue();
            }
            float width = this.computeWidth(text, size);
            this.widthCache.put(key, Float.valueOf(width));
            return width;
        }
        return 0.0f;
    }

    public void applyGlyphs(Matrix4f matrix, class_4588 consumer, String text, float size, float thickness, float spacing, float x, float y, float z, int color) {
        if (text != null && !text.isEmpty()) {
            int prevCodePoint = -1;
            int i = 0;
            while (i < text.length()) {
                int codePoint = text.codePointAt(i);
                i += Character.charCount(codePoint);
                MsdfGlyph glyph = this.glyphs.get(codePoint);
                if (glyph == null) continue;
                Map<Integer, Float> kerning = this.kernings.get(prevCodePoint);
                if (kerning != null) {
                    x += kerning.getOrDefault(codePoint, Float.valueOf(0.0f)).floatValue() * size;
                }
                x += glyph.apply(matrix, consumer, size, x, y, z, color) + thickness + spacing;
                prevCodePoint = codePoint;
            }
        }
    }

    public MsdfGlyph getGlyph(int codePoint) {
        return this.glyphs.get(codePoint);
    }

    public float getKerningAdvance(int leftCodePoint, int rightCodePoint, float size) {
        Map<Integer, Float> kerning = this.kernings.get(leftCodePoint);
        return kerning == null ? 0.0f : kerning.getOrDefault(rightCodePoint, Float.valueOf(0.0f)).floatValue() * size;
    }

    private float computeWidth(String text, float size) {
        int prevCodePoint = -1;
        float width = 0.0f;
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            MsdfGlyph glyph = this.glyphs.get(codePoint);
            if (glyph == null) continue;
            Map<Integer, Float> kerning = this.kernings.get(prevCodePoint);
            if (kerning != null) {
                width += kerning.getOrDefault(codePoint, Float.valueOf(0.0f)).floatValue() * size;
            }
            width += glyph.getAdvance(size) + 0.25f;
            prevCodePoint = codePoint;
        }
        return width;
    }
}

