/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_332
 *  net.minecraft.class_4588
 *  net.minecraft.class_8030
 */
package me.x150.renderer.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.function.Consumer;
import me.x150.renderer.render.GuiScissorTracker;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_332;
import net.minecraft.class_4588;
import net.minecraft.class_8030;

public final class SimpleGuiRenderState
extends Record
implements class_11244 {
    private final RenderPipeline pipeline;
    private final class_11231 textureSetup;
    private final class_8030 scissorArea;
    private final class_8030 bounds;
    private final Consumer<class_4588> vertices;

    public SimpleGuiRenderState(RenderPipeline pipeline, class_11231 textureSetup, class_332 context, class_8030 bounds, Consumer<class_4588> vertices) {
        this(pipeline, textureSetup, SimpleGuiRenderState.currentScissor(context), SimpleGuiRenderState.clippedBounds(context, bounds), SimpleGuiRenderState.visibleVertices(context, bounds, vertices));
    }

    public SimpleGuiRenderState(RenderPipeline pipeline, class_11231 textureSetup, class_8030 scissorArea, class_8030 bounds, Consumer<class_4588> vertices) {
        this.pipeline = pipeline;
        this.textureSetup = textureSetup;
        this.scissorArea = scissorArea;
        this.bounds = bounds;
        this.vertices = vertices;
    }

    private static class_8030 currentScissor(class_332 context) {
        class_8030 class_80302;
        if (context instanceof GuiScissorTracker) {
            GuiScissorTracker tracker = (GuiScissorTracker)context;
            class_80302 = tracker.pyz1k$currentScissor();
        } else {
            class_80302 = null;
        }
        return class_80302;
    }

    private static class_8030 clippedBounds(class_332 context, class_8030 bounds) {
        class_8030 scissor = SimpleGuiRenderState.currentScissor(context);
        if (scissor != null && bounds != null) {
            class_8030 clipped = scissor.method_49701(bounds);
            return clipped != null ? clipped : new class_8030(0, 0, 0, 0);
        }
        return bounds;
    }

    private static Consumer<class_4588> visibleVertices(class_332 context, class_8030 bounds, Consumer<class_4588> vertices) {
        class_8030 scissor = SimpleGuiRenderState.currentScissor(context);
        if (scissor != null && bounds != null) {
            class_8030 clipped = scissor.method_49701(bounds);
            return clipped != null && clipped.comp_1196() > 0 && clipped.comp_1197() > 0 ? vertices : ignored -> {};
        }
        return vertices;
    }

    public void method_70917(class_4588 vertices) {
        this.vertices.accept(vertices);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{SimpleGuiRenderState.class, "pipeline;textureSetup;scissorArea;bounds;vertices", "pipeline", "textureSetup", "scissorArea", "bounds", "vertices"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SimpleGuiRenderState.class, "pipeline;textureSetup;scissorArea;bounds;vertices", "pipeline", "textureSetup", "scissorArea", "bounds", "vertices"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SimpleGuiRenderState.class, "pipeline;textureSetup;scissorArea;bounds;vertices", "pipeline", "textureSetup", "scissorArea", "bounds", "vertices"}, this, o);
    }

    public RenderPipeline comp_4055() {
        return this.pipeline;
    }

    public class_11231 comp_4056() {
        return this.textureSetup;
    }

    public class_8030 comp_4069() {
        return this.scissorArea;
    }

    public class_8030 comp_4274() {
        return this.bounds;
    }

    public Consumer<class_4588> vertices() {
        return this.vertices;
    }
}

