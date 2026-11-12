/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  net.minecraft.class_11231
 *  net.minecraft.class_11241
 *  net.minecraft.class_11244
 *  net.minecraft.class_4588
 *  net.minecraft.class_8030
 *  org.joml.Matrix3x2fc
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.class_11231;
import net.minecraft.class_11241;
import net.minecraft.class_11244;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import org.joml.Matrix3x2fc;
import ru.bloodhelper.client.gui.FontGlyphRun;

public final class FontGlyphBatchState
implements class_11244 {
    private static long submittedRuns;
    private static long submittedGlyphs;
    private final class_11241 context;
    private final FontGlyphRun run;
    private final float x;
    private final float y;
    private final float size;

    public FontGlyphBatchState(class_11241 context, FontGlyphRun run, float x, float y, float size) {
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

    public void method_70917(class_4588 output) {
        this.run.build(output, (Matrix3x2fc)this.context.comp_4057(), this.x, this.y, this.size, this.context.comp_4067());
    }

    public RenderPipeline comp_4055() {
        return this.context.comp_4055();
    }

    public class_11231 comp_4056() {
        return this.context.comp_4056();
    }

    public class_8030 comp_4069() {
        return this.context.comp_4069();
    }

    public class_8030 comp_4274() {
        return this.context.comp_4274();
    }
}

