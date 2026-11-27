/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_5251
 */
package pyz1k.render;

import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5251;
import pyz1k.render.ScissorUtil;
import pyz1k.render.builders.Builder;
import pyz1k.render.builders.states.QuadColorState;
import pyz1k.render.builders.states.QuadRadiusState;
import pyz1k.render.builders.states.SizeState;
import pyz1k.render.font.GFonts;
import pyz1k.render.font.GIcons;
import pyz1k.render.font.msdf.MsdfFont;
import pyz1k.render.font.msdf.MsdfFonts;
import pyz1k.render.font.msdf.MsdfRenderer;
import pyz1k.render.renderers.BlurRenderer;
import pyz1k.render.renderers.BorderRenderer;
import pyz1k.render.renderers.CircleRenderer;
import pyz1k.render.renderers.GlowRenderer;
import pyz1k.render.renderers.HeadTextureRenderer;
import pyz1k.render.renderers.RectangleRenderer;
import pyz1k.render.renderers.RingProgressRenderer;
import pyz1k.render.renderers.TextureRenderer;

public final class RenderUtil {
    private static MsdfFont iconMsdfFont;
    private static final Map<String, class_2960> TEXTURE_IDENTIFIERS;

    private RenderUtil() {
    }

    public static void drawRoundRect(class_332 ctx, float x, float y, float w, float h, float radius, int color) {
        RenderUtil.drawRoundRect(ctx, x, y, w, h, radius, color, 1.0f);
    }

    public static void drawRoundRect(class_332 ctx, float x, float y, float w, float h, float radius, int color, float smoothness) {
        ((RectangleRenderer)Builder.rectangle().size(new SizeState(w, h)).radius(new QuadRadiusState(radius)).color(new QuadColorState(color)).smoothness(smoothness).build()).render(x, y, ctx);
    }

    public static void drawRoundRect(class_332 ctx, float x, float y, float w, float h, QuadRadiusState radiusState, int color) {
        RenderUtil.drawRoundRect(ctx, x, y, w, h, radiusState, color, 1.0f);
    }

    public static void drawRoundRect(class_332 ctx, float x, float y, float w, float h, QuadRadiusState radiusState, int color, float smoothness) {
        ((RectangleRenderer)Builder.rectangle().size(new SizeState(w, h)).radius(radiusState).color(new QuadColorState(color)).smoothness(smoothness).build()).render(x, y, ctx);
    }

    public static void drawRoundRectGradient(class_332 ctx, float x, float y, float w, float h, float radius, int leftColor, int rightColor) {
        RenderUtil.drawRoundRectGradient(ctx, x, y, w, h, radius, leftColor, rightColor, 1.0f);
    }

    public static void drawRoundRectGradient(class_332 ctx, float x, float y, float w, float h, float radius, int leftColor, int rightColor, float smoothness) {
        ((RectangleRenderer)Builder.rectangle().size(new SizeState(w, h)).radius(new QuadRadiusState(radius)).color(new QuadColorState(leftColor, rightColor, rightColor, leftColor)).smoothness(smoothness).build()).render(x, y, ctx);
    }

    public static void drawRoundRectGradientQuad(class_332 ctx, float x, float y, float w, float h, float radius, int topLeft, int bottomLeft, int bottomRight, int topRight) {
        RenderUtil.drawRoundRectGradientQuad(ctx, x, y, w, h, new QuadRadiusState(radius), topLeft, bottomLeft, bottomRight, topRight, 1.0f);
    }

    public static void drawRoundRectGradientQuad(class_332 ctx, float x, float y, float w, float h, QuadRadiusState radius, int topLeft, int bottomLeft, int bottomRight, int topRight, float smoothness) {
        ((RectangleRenderer)Builder.rectangle().size(new SizeState(w, h)).radius(radius).color(new QuadColorState(topLeft, bottomLeft, bottomRight, topRight)).smoothness(smoothness).build()).render(x, y, ctx);
    }

    public static void drawBorder(class_332 ctx, float x, float y, float w, float h, float radius, int color, float thickness, float internalSmoothness, float externalSmoothness) {
        int adjustedColor = RenderUtil.accelerateBorderFade(color);
        ((BorderRenderer)Builder.border().size(new SizeState(w, h)).radius(new QuadRadiusState(radius)).color(new QuadColorState(adjustedColor)).thickness(thickness).smoothness(internalSmoothness, externalSmoothness).build()).render(x, y, ctx);
    }

    public static void drawBorder(class_332 ctx, float x, float y, float w, float h, QuadRadiusState radius, int color, float thickness, float internalSmoothness, float externalSmoothness) {
        int adjustedColor = RenderUtil.accelerateBorderFade(color);
        ((BorderRenderer)Builder.border().size(new SizeState(w, h)).radius(radius).color(new QuadColorState(adjustedColor)).thickness(thickness).smoothness(internalSmoothness, externalSmoothness).build()).render(x, y, ctx);
    }

    public static void drawBlur(class_332 ctx, float x, float y, float w, float h, float radius, int color, float smoothness, float blurRadius) {
        ((BlurRenderer)Builder.blur().size(new SizeState(w, h)).radius(new QuadRadiusState(radius)).color(new QuadColorState(color)).smoothness(smoothness).blurRadius(blurRadius).build()).render(x, y, ctx);
    }

    public static void drawGlow(class_332 ctx, float x, float y, float w, float h, float radius, int color, float smoothness, float spread) {
        ((GlowRenderer)Builder.glow().size(new SizeState(w, h)).radius(new QuadRadiusState(radius)).color(new QuadColorState(color)).smoothness(smoothness).spread(spread).build()).render(x, y, ctx);
    }

    public static void drawGlow(class_332 ctx, float x, float y, float w, float h, QuadRadiusState radiusState, int color, float smoothness, float spread) {
        ((GlowRenderer)Builder.glow().size(new SizeState(w, h)).radius(radiusState).color(new QuadColorState(color)).smoothness(smoothness).spread(spread).build()).render(x, y, ctx);
    }

    public static void drawShadow(class_332 ctx, float x, float y, float w, float h, float radius, int color, float smoothness, float spread, float offsetX, float offsetY) {
        RenderUtil.drawGlow(ctx, x + offsetX + 0.5f, y + offsetY + 0.5f, w - 1.0f, h - 1.0f, radius, color, smoothness, spread);
    }

    public static void drawShadow(class_332 ctx, float x, float y, float w, float h, QuadRadiusState radiusState, int color, float smoothness, float spread, float offsetX, float offsetY) {
        RenderUtil.drawGlow(ctx, x + offsetX + 0.5f, y + offsetY + 0.5f, w - 1.0f, h - 1.0f, radiusState, color, smoothness, spread);
    }

    public static void drawImage(class_332 ctx, GpuTextureView texture, float x, float y, float w, float h, float radius, int color, float smoothness) {
        RenderUtil.drawRotatedImage(ctx, texture, x, y, w, h, 0.0f, radius, color, smoothness);
    }

    public static void drawRotatedImage(class_332 ctx, GpuTextureView texture, float x, float y, float w, float h, float rotation, float radius, int color, float smoothness) {
        ((TextureRenderer)Builder.texture().size(new SizeState(w, h)).radius(new QuadRadiusState(radius)).color(new QuadColorState(color)).smoothness(smoothness).rotation(rotation).texture(0.0f, 0.0f, 0.0f, 0.0f, texture).build()).render(x, y, ctx);
    }

    public static void drawImage(class_332 ctx, class_2960 texture, float x, float y, float w, float h, float radius, int color, float smoothness) {
        RenderUtil.drawRotatedImage(ctx, texture, x, y, w, h, 0.0f, radius, color, smoothness);
    }

    public static void drawRotatedImage(class_332 ctx, class_2960 texture, float x, float y, float w, float h, float rotation, float radius, int color, float smoothness) {
        class_310 client = class_310.method_1551();
        if (client != null) {
            RenderUtil.drawRotatedImage(ctx, client.method_1531().method_4619(texture).method_71659(), x, y, w, h, rotation, radius, color, smoothness);
        }
    }

    public static void drawImage(class_332 ctx, String texturePath, float x, float y, float w, float h, float radius, int color, float smoothness) {
        RenderUtil.drawRotatedImage(ctx, texturePath, x, y, w, h, 0.0f, radius, color, smoothness);
    }

    public static void drawRotatedImage(class_332 ctx, String texturePath, float x, float y, float w, float h, float rotation, float radius, int color, float smoothness) {
        class_2960 identifier = TEXTURE_IDENTIFIERS.computeIfAbsent(texturePath, path -> class_2960.method_60655((String)"pyz1k", (String)path));
        RenderUtil.drawRotatedImage(ctx, identifier, x, y, w, h, rotation, radius, color, smoothness);
    }

    public static void drawImageRegion(class_332 ctx, GpuTextureView texture, float x, float y, float w, float h, float u, float v, float texW, float texH, float radius, int color, float smoothness) {
        ((TextureRenderer)Builder.texture().size(new SizeState(w, h)).radius(new QuadRadiusState(radius)).color(new QuadColorState(color)).smoothness(smoothness).texture(u, v, texW, texH, texture).build()).render(x, y, ctx);
    }

    public static void drawHeadImageRegion(class_332 ctx, GpuTextureView texture, float x, float y, float w, float h, float u, float v, float texW, float texH, float radius, int color, float smoothness) {
        ((HeadTextureRenderer)Builder.headTexture().size(new SizeState(w, h)).radius(new QuadRadiusState(radius)).color(new QuadColorState(color)).smoothness(smoothness).texture(u, v, texW, texH, texture).build()).render(x, y, ctx);
    }

    public static void drawText(class_332 ctx, String text, float x, float y, int color, GFonts font, int size) {
        MsdfRenderer.drawString(ctx, MsdfFonts.get(font), text, size, color, x, y);
    }

    public static void drawText(class_332 ctx, String text, float x, float y, int color, GFonts font, float size) {
        MsdfRenderer.drawString(ctx, MsdfFonts.get(font), text, size, color, x, y);
    }

    public static void drawTextFadeRight(class_332 ctx, String text, float x, float y, int color, GFonts font, int size, float maxWidth, float fadeWidth) {
        MsdfRenderer.drawStringFadeRight(ctx, MsdfFonts.get(font), text, size, color, x, y, maxWidth, fadeWidth);
    }

    public static void drawTextFadeRight(class_332 ctx, String text, float x, float y, int color, GFonts font, float size, float maxWidth, float fadeWidth) {
        MsdfRenderer.drawStringFadeRight(ctx, MsdfFonts.get(font), text, size, color, x, y, maxWidth, fadeWidth);
    }

    public static void drawIcon(class_332 ctx, GIcons icon, float x, float y, float size, int color) {
        if (icon != null) {
            String texturePath = switch (icon) {
                default -> throw new MatchException(null, null);
                case GIcons.FRAME -> "textures/gui/icons/frame.png";
                case GIcons.KEYBOARD -> "textures/gui/icons/keyboard.png";
                case GIcons.CHECKMARK -> "textures/gui/icons/checkmark.png";
                case GIcons.LOGO -> "textures/gui/icons/logo.png";
            };
            RenderUtil.drawImage(ctx, texturePath, x, y, size, size, 0.0f, color, 0.0f);
        }
    }

    public static void drawIconGlyph(class_332 ctx, GIcons icon, float x, float y, float size, int color) {
        if (icon != null) {
            MsdfFont font = RenderUtil.getIconMsdfFont();
            if (font == null) {
                RenderUtil.drawIcon(ctx, icon, x, y, size, color);
            } else {
                MsdfRenderer.drawString(ctx, font, String.valueOf(icon.glyph()), size, color, x, y - size * 0.08f);
            }
        }
    }

    public static float getIconGlyphWidth(GIcons icon, float size) {
        if (icon == null) {
            return 0.0f;
        }
        MsdfFont font = RenderUtil.getIconMsdfFont();
        return font == null ? size : font.getWidth(String.valueOf(icon.glyph()), size);
    }

    public static void drawText(class_332 ctx, class_2561 text, float x, float y, int color, GFonts font, int size) {
        if (text != null) {
            float[] cursor = new float[]{x};
            int baseAlpha = color >>> 24 & 0xFF;
            text.method_27658((style, segment) -> {
                if (segment != null && !segment.isEmpty()) {
                    GFonts segmentFont = style != null && style.method_10984() ? GFonts.BOLD : font;
                    class_5251 styleColor = style == null ? null : style.method_10973();
                    int rgb = styleColor == null ? color & 0xFFFFFF : styleColor.method_27716();
                    int segmentColor = baseAlpha << 24 | rgb & 0xFFFFFF;
                    MsdfRenderer.drawString(ctx, MsdfFonts.get(segmentFont), segment, size, segmentColor, cursor[0], y);
                    cursor[0] = cursor[0] + RenderUtil.getTextWidth(segment, segmentFont, size);
                    return Optional.empty();
                }
                return Optional.empty();
            }, class_2583.field_24360);
        }
    }

    public static void drawCircle(class_332 ctx, float x, float y, float diameter, int color) {
        RenderUtil.drawCircle(ctx, x, y, diameter, color, 1.0f);
    }

    public static void drawCircle(class_332 ctx, float x, float y, float diameter, int color, float smoothness) {
        new CircleRenderer(diameter, color, smoothness).render(x, y, ctx);
    }

    public static void drawRingProgress(class_332 ctx, float x, float y, float diameter, float progress, float thickness, int color, float smoothness) {
        new RingProgressRenderer(diameter, color, progress, thickness, smoothness).render(x, y, ctx);
    }

    public static void pushScissor(class_332 ctx, float x, float y, float w, float h) {
        ScissorUtil.push(ctx, x, y, w, h);
    }

    public static void popScissor() {
        ScissorUtil.pop();
    }

    public static void clearScissor() {
        ScissorUtil.clear();
    }

    public static float getTextWidth(String text, GFonts font, int size) {
        MsdfFont msdfFont = MsdfFonts.get(font);
        return msdfFont.getWidth(text, size);
    }

    public static float getTextWidth(String text, GFonts font, float size) {
        MsdfFont msdfFont = MsdfFonts.get(font);
        return msdfFont.getWidth(text, size);
    }

    public static float getTextHeight(GFonts font, int size) {
        MsdfFont msdfFont = MsdfFonts.get(font);
        return msdfFont.lineHeight(size);
    }

    public static float getTextHeight(GFonts font, float size) {
        MsdfFont msdfFont = MsdfFonts.get(font);
        return msdfFont.lineHeight(size);
    }

    public static float getTextHeight(String text, GFonts font, int size) {
        return RenderUtil.getTextHeight(font, size);
    }

    public static float getTextHeight(String text, GFonts font, float size) {
        return RenderUtil.getTextHeight(font, size);
    }

    public static float getTextWidth(class_2561 text, GFonts font, int size) {
        if (text == null) {
            return 0.0f;
        }
        float[] width = new float[]{0.0f};
        text.method_27658((style, segment) -> {
            if (segment != null && !segment.isEmpty()) {
                GFonts segmentFont = style != null && style.method_10984() ? GFonts.BOLD : font;
                width[0] = width[0] + RenderUtil.getTextWidth(segment, segmentFont, size);
                return Optional.empty();
            }
            return Optional.empty();
        }, class_2583.field_24360);
        return width[0];
    }

    public static float getTextHeight(class_2561 text, GFonts font, int size) {
        return RenderUtil.getTextHeight(font, size);
    }

    private static MsdfFont getIconMsdfFont() {
        if (iconMsdfFont != null) {
            return iconMsdfFont;
        }
        try {
            iconMsdfFont = MsdfFont.load(class_2960.method_60655((String)"pyz1k", (String)"fonts/msdf/icons.json"), class_2960.method_60655((String)"pyz1k", (String)"fonts/msdf/icons.png"));
            return iconMsdfFont;
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private static int accelerateBorderFade(int argb) {
        int a = argb >>> 24 & 0xFF;
        int rgb = argb & 0xFFFFFF;
        int fasterA = a * a * a / 65025;
        return (fasterA & 0xFF) << 24 | rgb;
    }

    static {
        TEXTURE_IDENTIFIERS = new HashMap<String, class_2960>();
    }
}

