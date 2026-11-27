/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package pyz1k.screen.screen;

import net.minecraft.class_332;
import pyz1k.module.impl.visuals.Interface;
import pyz1k.render.RenderUtil;
import pyz1k.render.builders.states.QuadRadiusState;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class HudVisualStyle {
    public static final float RADIUS = 5.0f;
    public static int PRIMARY_TEXT = ColorUtil.rgba(244, 244, 247, 255);
    public static int SECONDARY_TEXT = ColorUtil.rgba(155, 155, 164, 255);
    public static int MUTED_TEXT = ColorUtil.rgba(101, 102, 112, 255);
    private static boolean lightMode;

    private HudVisualStyle() {
    }

    public static void applyLightMode(boolean light) {
        HudVisualStyle.applyThemePalette(light);
    }

    public static void applyThemePalette(boolean light) {
        lightMode = light;
        PRIMARY_TEXT = ClientColors.HUD_TEXT;
        SECONDARY_TEXT = ClientColors.HUD_SECONDARY_TEXT;
        MUTED_TEXT = ColorUtil.interpolate(ClientColors.HUD_SECONDARY_TEXT, ClientColors.HUD_BACKGROUND, 0.32f);
    }

    public static void drawPanel(class_332 context, float x, float y, float width, float height, float alpha) {
        HudVisualStyle.drawPanel(context, x, y, width, height, alpha, 5.0f);
    }

    public static void drawPanel(class_332 context, float x, float y, float width, float height, float alpha, float radius) {
        float a = ColorUtil.clamp01(alpha);
        if (!(a <= 0.01f || width <= 1.0f || height <= 1.0f)) {
            float safeRadius = Math.max(0.0f, Math.min(radius, Math.min(width, height) * 0.5f));
            if (Interface.isHudShadowEnabled()) {
                RenderUtil.drawGlow(context, x + 0.5f, y + 0.5f, width - 1.0f, height - 1.0f, safeRadius, ColorUtil.multiplyAlpha(ClientColors.HUD_SHADOW, a * Interface.getHudShadowOpacity()), 1.0f, Interface.getHudShadowSize());
            }
            RenderUtil.drawRoundRect(context, x, y, width, height, safeRadius, ColorUtil.multiplyAlpha(ClientColors.HUD_BACKGROUND, a));
            RenderUtil.drawBorder(context, x, y, width, height, safeRadius, ColorUtil.multiplyAlpha(ClientColors.HUD_OUTLINE, a), 0.7f, 0.0f, 0.5f);
            float inset = 0.8f;
            RenderUtil.drawRoundRect(context, x + inset, y + inset, width - inset * 2.0f, height - inset * 2.0f, Math.max(0.0f, safeRadius - inset), ColorUtil.multiplyAlpha(ClientColors.HUD_INSET, a));
            RenderUtil.drawBorder(context, x + inset, y + inset, width - inset * 2.0f, height - inset * 2.0f, Math.max(0.0f, safeRadius - inset), ColorUtil.multiplyAlpha(ClientColors.HUD_OUTLINE, a * 0.55f), 0.55f, 0.0f, 0.5f);
        }
    }

    public static void drawGlassPanel(class_332 context, float x, float y, float width, float height, float alpha) {
        HudVisualStyle.drawGlassPanel(context, x, y, width, height, alpha, ClientColors.HUD_BACKGROUND);
    }

    public static void drawGlassPanel(class_332 context, float x, float y, float width, float height, float alpha, int backgroundColor) {
        float a = ColorUtil.clamp01(alpha);
        if (!(a <= 0.01f || width <= 1.0f || height <= 1.0f)) {
            if (Interface.isHudShadowEnabled()) {
                RenderUtil.drawGlow(context, x + 0.5f, y + 0.5f, width - 1.0f, height - 1.0f, 5.0f, ColorUtil.multiplyAlpha(ClientColors.HUD_SHADOW, a * Interface.getHudShadowOpacity()), 1.0f, Interface.getHudShadowSize());
            }
            RenderUtil.drawRoundRect(context, x, y, width, height, 5.0f, ColorUtil.multiplyAlpha(backgroundColor, a));
            RenderUtil.drawBorder(context, x, y, width, height, 5.0f, ColorUtil.multiplyAlpha(ClientColors.HUD_OUTLINE, a * 0.86f), 0.65f, 0.0f, 0.5f);
        }
    }

    public static void drawMenuPanel(class_332 context, float x, float y, float width, float height, float sidebarWidth, float alpha) {
        float a = ColorUtil.clamp01(alpha);
        if (!(a <= 0.01f || width <= 1.0f || height <= 1.0f)) {
            if (Interface.isHudShadowEnabled()) {
                RenderUtil.drawShadow(context, x, y + 0.5f, width, height, 5.0f, ColorUtil.multiplyAlpha(ColorUtil.rgba(3, 3, 5, 104), a), 1.0f, 10.0f, 0.0f, 1.0f);
            }
            RenderUtil.drawRoundRect(context, x, y, width, height, 5.0f, ColorUtil.multiplyAlpha(ClientColors.MENU_BACKGROUND, a));
            float split = Math.max(0.0f, Math.min(width, sidebarWidth));
            if (split < width - 1.0f) {
                RenderUtil.drawRoundRect(context, x + split, y, width - split, height, new QuadRadiusState(0.0f, 0.0f, 5.0f, 5.0f), ColorUtil.multiplyAlpha(ClientColors.SETTINGS_BACKGROUND, a));
            }
            RenderUtil.drawBorder(context, x, y, width, height, 5.0f, ColorUtil.multiplyAlpha(ColorUtil.rgba(255, 255, 255, 20), a), 0.7f, 0.0f, 0.5f);
            RenderUtil.drawRoundRect(context, x + 6.0f, y + 0.9f, Math.max(8.0f, width - 12.0f), 0.55f, 0.3f, ColorUtil.multiplyAlpha(ColorUtil.rgba(255, 255, 255, 13), a));
        }
    }

    public static void drawSignalRail(class_332 context, float x, float y, float length, int color, float alpha) {
        float a = ColorUtil.clamp01(alpha);
        RenderUtil.drawRoundRect(context, x, y, 1.25f, length, 0.65f, ColorUtil.multiplyAlpha(ColorUtil.rgba(255, 255, 255, 18), a));
        RenderUtil.drawRoundRect(context, x, y, 1.25f, Math.max(2.0f, length * 0.42f), 0.65f, ColorUtil.multiplyAlpha(color, a));
    }

    public static void drawProminentSignalRail(class_332 context, float x, float y, float length, int color, float alpha) {
        float a = ColorUtil.clamp01(alpha);
        if (!(length <= 0.0f) && !(a <= 0.01f)) {
            RenderUtil.drawRoundRect(context, x - 0.55f, y - 1.0f, 2.4f, length + 2.0f, 1.2f, ColorUtil.multiplyAlpha(color, a * 0.16f));
            RenderUtil.drawRoundRect(context, x, y, 1.4f, length, 0.7f, ColorUtil.multiplyAlpha(color, a * 0.92f));
            RenderUtil.drawRoundRect(context, x + 0.25f, y + 0.7f, 0.55f, Math.max(1.0f, length - 1.4f), 0.3f, ColorUtil.multiplyAlpha(ColorUtil.rgba(255, 255, 255, 150), a));
        }
    }

    public static void drawHairline(class_332 context, float x, float y, float width, int color, float alpha) {
        float a = ColorUtil.clamp01(alpha);
        if (!(width <= 0.0f) && !(a <= 0.01f)) {
            RenderUtil.drawRoundRect(context, x, y, width, 0.65f, 0.35f, ColorUtil.multiplyAlpha(color, a));
        }
    }

    public static float easeOutCubic(float value) {
        float v = ColorUtil.clamp01(value);
        float inv = 1.0f - v;
        return 1.0f - inv * inv * inv;
    }

    public static void drawInset(class_332 context, float x, float y, float width, float height, float alpha) {
        float a = ColorUtil.clamp01(alpha);
        RenderUtil.drawRoundRect(context, x, y, width, height, 2.5f, ColorUtil.multiplyAlpha(ClientColors.HUD_INSET, a));
        RenderUtil.drawBorder(context, x, y, width, height, 2.5f, ColorUtil.multiplyAlpha(ClientColors.HUD_OUTLINE, a * 0.65f), 0.55f, 0.0f, 0.5f);
    }
}

