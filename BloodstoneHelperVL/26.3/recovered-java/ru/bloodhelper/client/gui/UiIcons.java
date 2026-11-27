/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.renderpearl.api.pipeline.BindGroupLayout
 *  com.mojang.renderpearl.api.pipeline.BlendFunction
 *  com.mojang.renderpearl.api.pipeline.ColorTargetState
 *  com.mojang.renderpearl.api.pipeline.PrimitiveTopology
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline$Snippet
 *  com.mojang.renderpearl.api.pipeline.UniformType
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.resources.Identifier
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import ru.bloodhelper.client.gui.MusicWave;
import ru.bloodhelper.client.gui.ThemeColors;

public final class UiIcons {
    private static final Identifier VECTOR_TEXTURE = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"textures/gui/shape.png");
    private static final Identifier ATLAS = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"textures/gui/icons_sdf.png");
    private static final int CELL = 64;
    private static final int COLUMNS = 6;
    private static final int ATLAS_WIDTH = 384;
    private static final int ATLAS_HEIGHT = 384;
    private static final RenderPipeline PIPELINE = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"pipeline/gui_icons")).withVertexShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/gui_icons")).withFragmentShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/gui_icons")).withBindGroupLayout(BindGroupLayout.builder().withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).withUniform("Sampler0", UniformType.COMBINED_IMAGE_SAMPLER).build()).withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withDepthStencilState(Optional.empty()).withCull(false).build();
    private static final RenderPipeline[] MEDIA_VECTORS = new RenderPipeline[]{UiIcons.mediaPipeline(0), UiIcons.mediaPipeline(1), UiIcons.mediaPipeline(2), UiIcons.mediaPipeline(3), UiIcons.mediaPipeline(4), UiIcons.mediaPipeline(5), UiIcons.mediaPipeline(6), UiIcons.mediaPipeline(7), UiIcons.mediaPipeline(8), UiIcons.mediaPipeline(9), UiIcons.mediaPipeline(10), UiIcons.mediaPipeline(11), UiIcons.mediaPipeline(12), UiIcons.mediaPipeline(13)};

    private static RenderPipeline mediaPipeline(int shape) {
        return RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)("pipeline/media_vector_" + shape))).withVertexShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/media_vector")).withFragmentShader(Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"core/media_vector")).withShaderDefine("ICON_ID", shape).withBindGroupLayout(BindGroupLayout.builder().withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER).withUniform("Sampler0", UniformType.COMBINED_IMAGE_SAMPLER).build()).withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withDepthStencilState(Optional.empty()).withCull(false).build();
    }

    private UiIcons() {
    }

    public static void draw(GuiGraphicsExtractor graphics, String name, int x, int y, int size, int color) {
        int mediaShape;
        if (size <= 0 || color >>> 24 == 0) {
            return;
        }
        color = ThemeColors.icon(color);
        if ("heads".equals(name)) {
            int inset = Math.max(1, size / 6);
            int line = Math.max(1, size / 12);
            int l = x + inset;
            int t = y + inset;
            int r = x + size - inset;
            int b = y + size - inset;
            graphics.fill(l, t, r, t + line, color);
            graphics.fill(l, b - line, r, b, color);
            graphics.fill(l, t, l + line, b, color);
            graphics.fill(r - line, t, r, b, color);
            graphics.fill(l + line * 2, t + line * 3, l + line * 4, t + line * 5, color);
            graphics.fill(r - line * 4, t + line * 3, r - line * 2, t + line * 5, color);
            graphics.fill(l + line * 3, b - line * 3, r - line * 3, b - line * 2, color);
            return;
        }
        switch (name) {
            case "play": {
                int n = 0;
                break;
            }
            case "pause": {
                int n = 1;
                break;
            }
            case "previous": {
                int n = 2;
                break;
            }
            case "next": {
                int n = 3;
                break;
            }
            case "hotbar": {
                int n = 6;
                break;
            }
            case "keyboard": {
                int n = 7;
                break;
            }
            case "totem": {
                int n = 8;
                break;
            }
            case "scoreboard": {
                int n = 9;
                break;
            }
            case "title": {
                int n = 10;
                break;
            }
            case "actionbar": {
                int n = 11;
                break;
            }
            case "weather": {
                int n = 12;
                break;
            }
            case "ambience": {
                int n = 13;
                break;
            }
            default: {
                int n = mediaShape = -1;
            }
        }
        if (mediaShape >= 0) {
            UiIcons.vector(graphics, mediaShape, x, y, size, size, color);
            return;
        }
        int cell = switch (name) {
            case "modules" -> 0;
            case "palette", "appearance" -> 1;
            case "profiles" -> 2;
            case "about", "info" -> 3;
            case "music" -> 4;
            case "clock", "cooldowns" -> 5;
            case "shield", "pvp" -> 6;
            case "search" -> 7;
            case "close" -> 8;
            case "chevron" -> 9;
            case "back" -> 10;
            case "reset" -> 11;
            case "edit" -> 12;
            case "previous" -> 13;
            case "play" -> 14;
            case "pause" -> 15;
            case "next" -> 16;
            case "check" -> 17;
            case "headphones" -> 18;
            case "grid", "hud" -> 19;
            case "heart" -> 20;
            case "volume" -> 21;
            case "external" -> 22;
            case "sun" -> 23;
            case "armor" -> 24;
            case "bell", "notifications" -> 25;
            case "performance" -> 26;
            case "stats", "session" -> 27;
            case "swords" -> 28;
            case "chat" -> 30;
            case "death" -> 31;
            case "effects" -> 32;
            default -> 3;
        };
        graphics.blit(PIPELINE, ATLAS, x, y, (float)(cell % 6 * 64), (float)(cell / 6 * 64), size, size, 64, 64, 384, 384, color);
    }

    public static void wave(GuiGraphicsExtractor g, float x, float y, float width, float height, double phase, double energy, int color) {
        MusicWave.draw(g, x, y, width, height, phase, energy, ThemeColors.icon(color));
    }

    private static void vector(GuiGraphicsExtractor graphics, int shape, int x, int y, int width, int height, int color) {
        graphics.blit(MEDIA_VECTORS[shape], VECTOR_TEXTURE, x, y, 0.0f, 0.0f, width, height, 1, 1, 1, 1, color);
    }
}

