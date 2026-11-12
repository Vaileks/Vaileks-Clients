/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.gui.render.TextureSetup
 *  net.minecraft.client.renderer.state.gui.BlitRenderState
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import ru.bloodhelper.client.gui.FontGlyphRun;

public final class FontGlyphBatchState
implements GuiElementRenderState {
    private static long submittedRuns;
    private static long submittedGlyphs;
    private final BlitRenderState context;
    private final FontGlyphRun run;
    private final float x;
    private final float y;
    private final float size;

    public FontGlyphBatchState(BlitRenderState context, FontGlyphRun run, float x, float y, float size) {
        this.context = context;
        this.run = run;
        this.x = x;
        this.y = y;
        this.size = size;
        ++submittedRuns;
        submittedGlyphs += (long)run.glyphCount();
    }

    public static long submittedRuns() {
        return submittedRuns;
    }

    public static long submittedGlyphs() {
        return submittedGlyphs;
    }

    public void buildVertices(VertexConsumer output) {
        this.run.build(output, this.context.pose(), this.x, this.y, this.size, this.context.color());
    }

    public RenderPipeline pipeline() {
        return this.context.pipeline();
    }

    public TextureSetup textureSetup() {
        return this.context.textureSetup();
    }

    public ScreenRectangle scissorArea() {
        return this.context.scissorArea();
    }

    public ScreenRectangle bounds() {
        return this.context.bounds();
    }
}

