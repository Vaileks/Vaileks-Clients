/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_12137
 *  net.minecraft.class_287
 *  net.minecraft.class_332
 *  net.minecraft.class_8030
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix3x2fc
 */
package pyz1k.render.font.msdf;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import me.x150.renderer.mixin.DrawContextAccessor;
import me.x150.renderer.render.SimpleGuiRenderState;
import me.x150.renderer.util.DirectVertexConsumer;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_12137;
import net.minecraft.class_287;
import net.minecraft.class_332;
import net.minecraft.class_8030;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;
import pyz1k.render.font.msdf.MsdfFont;
import pyz1k.render.font.msdf.MsdfGlyph;
import pyz1k.render.renderers.pipeline.PipelineRegistry;

public final class MsdfRenderer {
    private MsdfRenderer() {
    }

    public static void drawString(class_332 context, MsdfFont font, String text, float size, int color, float x, float y) {
        if (text != null && !text.isEmpty() && PipelineRegistry.MSDF_FONT_PIPELINE != null) {
            float textWidth = font.getWidth(text, size);
            float textHeight = font.lineHeight(size);
            if (!(textWidth <= 0.0f) && !(textHeight <= 0.0f)) {
                GpuTextureView textureView = font.textureView();
                Matrix3x2f guiMatrices = new Matrix3x2f((Matrix3x2fc)context.method_51448());
                SimpleGuiRenderState state = new SimpleGuiRenderState(PipelineRegistry.MSDF_FONT_PIPELINE, class_11231.method_70900((GpuTextureView)textureView, (class_12137)RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR)), context, MsdfRenderer.createBounds(context, x, y, textWidth + 2.0f, textHeight + 2.0f), buffer -> {
                    DirectVertexConsumer dvc = new DirectVertexConsumer((class_287)buffer, false);
                    float thickness = 0.05f;
                    float spacing = 0.0f;
                    float cursorX = x - 0.75f;
                    float baseY = y + size * 0.7f;
                    int prevCodePoint = -1;
                    int outColor = MsdfRenderer.toAbgr(color);
                    int i = 0;
                    while (i < text.length()) {
                        int codePoint = text.codePointAt(i);
                        i += Character.charCount(codePoint);
                        MsdfGlyph glyph = font.getGlyph(codePoint);
                        if (glyph == null) continue;
                        float gy = baseY - glyph.topPosition() * size;
                        float gw = glyph.width() * size;
                        float gh = glyph.height() * size;
                        dvc.method_70815((Matrix3x2fc)guiMatrices, cursorX += font.getKerningAdvance(prevCodePoint, codePoint, size), gy).method_22913(glyph.minU(), glyph.minV()).method_39415(outColor);
                        dvc.method_70815((Matrix3x2fc)guiMatrices, cursorX, gy + gh).method_22913(glyph.minU(), glyph.maxV()).method_39415(outColor);
                        dvc.method_70815((Matrix3x2fc)guiMatrices, cursorX + gw, gy + gh).method_22913(glyph.maxU(), glyph.maxV()).method_39415(outColor);
                        dvc.method_70815((Matrix3x2fc)guiMatrices, cursorX + gw, gy).method_22913(glyph.maxU(), glyph.minV()).method_39415(outColor);
                        cursorX += glyph.getAdvance(size) + thickness * 0.5f * size + spacing;
                        prevCodePoint = codePoint;
                    }
                });
                ((DrawContextAccessor)context).getState().method_70919((class_11244)state);
            }
        }
    }

    public static void drawStringFadeRight(class_332 context, MsdfFont font, String text, float size, int color, float x, float y, float maxWidth, float fadeWidth) {
        if (text != null && !text.isEmpty() && PipelineRegistry.MSDF_FONT_PIPELINE != null && !(maxWidth <= 0.0f)) {
            float textWidth = font.getWidth(text, size);
            float textHeight = font.lineHeight(size);
            if (!(textWidth <= 0.0f) && !(textHeight <= 0.0f)) {
                float fadeSpan = Math.max(1.0f, Math.min(maxWidth, fadeWidth));
                float visibleEndX = x + maxWidth;
                float fadeStartX = visibleEndX - fadeSpan;
                int baseAlpha = color >>> 24 & 0xFF;
                int rgb = color & 0xFFFFFF;
                GpuTextureView textureView = font.textureView();
                Matrix3x2f guiMatrices = new Matrix3x2f((Matrix3x2fc)context.method_51448());
                SimpleGuiRenderState state = new SimpleGuiRenderState(PipelineRegistry.MSDF_FONT_PIPELINE, class_11231.method_70900((GpuTextureView)textureView, (class_12137)RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR)), context, MsdfRenderer.createBounds(context, x, y, Math.min(textWidth, maxWidth) + 2.0f, textHeight + 2.0f), buffer -> {
                    DirectVertexConsumer dvc = new DirectVertexConsumer((class_287)buffer, false);
                    float thickness = 0.05f;
                    float spacing = 0.0f;
                    float cursorX = x - 0.75f;
                    float baseY = y + size * 0.7f;
                    int prevCodePoint = -1;
                    int i = 0;
                    while (i < text.length()) {
                        int outA;
                        int codePoint = text.codePointAt(i);
                        i += Character.charCount(codePoint);
                        MsdfGlyph glyph = font.getGlyph(codePoint);
                        if (glyph == null) continue;
                        float gy = baseY - glyph.topPosition() * size;
                        float gw = glyph.width() * size;
                        float gh = glyph.height() * size;
                        float glyphCenter = (cursorX += font.getKerningAdvance(prevCodePoint, codePoint, size)) + gw * 0.5f;
                        float alphaMul = 1.0f;
                        if (glyphCenter > fadeStartX) {
                            float t = MsdfRenderer.clamp01((glyphCenter - fadeStartX) / fadeSpan);
                            alphaMul = 1.0f - MsdfRenderer.smootherStep01(t);
                        }
                        if ((outA = Math.round((float)baseAlpha * alphaMul)) > 0) {
                            int glyphColor = MsdfRenderer.toAbgr((outA & 0xFF) << 24 | rgb);
                            dvc.method_70815((Matrix3x2fc)guiMatrices, cursorX, gy).method_22913(glyph.minU(), glyph.minV()).method_39415(glyphColor);
                            dvc.method_70815((Matrix3x2fc)guiMatrices, cursorX, gy + gh).method_22913(glyph.minU(), glyph.maxV()).method_39415(glyphColor);
                            dvc.method_70815((Matrix3x2fc)guiMatrices, cursorX + gw, gy + gh).method_22913(glyph.maxU(), glyph.maxV()).method_39415(glyphColor);
                            dvc.method_70815((Matrix3x2fc)guiMatrices, cursorX + gw, gy).method_22913(glyph.maxU(), glyph.minV()).method_39415(glyphColor);
                        }
                        cursorX += glyph.getAdvance(size) + thickness * 0.5f * size + spacing;
                        prevCodePoint = codePoint;
                        if (!(cursorX > visibleEndX + 2.0f)) continue;
                        break;
                    }
                });
                ((DrawContextAccessor)context).getState().method_70919((class_11244)state);
            }
        }
    }

    private static class_8030 createBounds(class_332 context, float x, float y, float w, float h) {
        Matrix3x2fStack mat = context.method_51448();
        return new class_8030((int)Math.floor(x), (int)Math.floor(y), (int)Math.ceil(w), (int)Math.ceil(h)).method_71523((Matrix3x2fc)mat);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float smootherStep01(float t) {
        float x = MsdfRenderer.clamp01(t);
        return x * x * x * (x * (x * 6.0f - 15.0f) + 10.0f);
    }

    private static int toAbgr(int argb) {
        int a = argb >>> 24 & 0xFF;
        int r = argb >>> 16 & 0xFF;
        int g = argb >>> 8 & 0xFF;
        int b = argb & 0xFF;
        return (a & 0xFF) << 24 | (b & 0xFF) << 16 | (g & 0xFF) << 8 | r & 0xFF;
    }
}

