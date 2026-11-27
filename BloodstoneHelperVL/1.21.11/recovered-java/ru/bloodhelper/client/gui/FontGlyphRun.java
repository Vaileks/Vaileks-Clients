/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4588
 *  org.joml.Matrix3x2fc
 *  org.joml.Matrix4fc
 */
package ru.bloodhelper.client.gui;

import net.minecraft.class_4588;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4fc;

public final class FontGlyphRun {
    private static final int STRIDE = 8;
    private final float[] quads;
    private final float left;
    private final float top;
    private final float right;
    private final float bottom;

    FontGlyphRun(float[] quads) {
        float maxX;
        float minX;
        if (quads.length == 0 || quads.length % 8 != 0) {
            throw new IllegalArgumentException("quads");
        }
        this.quads = (float[])quads.clone();
        float minY = minX = Float.POSITIVE_INFINITY;
        float maxY = maxX = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < quads.length; i += 8) {
            for (int j = 0; j < 8; ++j) {
                if (Float.isFinite(quads[i + j])) continue;
                throw new IllegalArgumentException("Non-finite glyph");
            }
            minX = Math.min(minX, quads[i]);
            minY = Math.min(minY, quads[i + 1]);
            maxX = Math.max(maxX, quads[i + 2]);
            maxY = Math.max(maxY, quads[i + 3]);
        }
        this.left = minX;
        this.top = minY;
        this.right = maxX;
        this.bottom = maxY;
    }

    public int glyphCount() {
        return this.quads.length / 8;
    }

    public void buildWorld(class_4588 output, Matrix4fc pose, float x, float y, int color) {
        for (int i = 0; i < this.quads.length; i += 8) {
            float x0 = x + this.quads[i];
            float y0 = y + this.quads[i + 1];
            float x1 = x + this.quads[i + 2];
            float y1 = y + this.quads[i + 3];
            output.method_22918(pose, x0, y0, 0.0f).method_22913(this.quads[i + 4], this.quads[i + 5]).method_39415(color);
            output.method_22918(pose, x0, y1, 0.0f).method_22913(this.quads[i + 4], this.quads[i + 7]).method_39415(color);
            output.method_22918(pose, x1, y1, 0.0f).method_22913(this.quads[i + 6], this.quads[i + 7]).method_39415(color);
            output.method_22918(pose, x1, y0, 0.0f).method_22913(this.quads[i + 6], this.quads[i + 5]).method_39415(color);
        }
    }

    public int left(float x, float size) {
        return (int)Math.floor(x + this.left * size);
    }

    public int top(float y, float size) {
        return (int)Math.floor(y + this.top * size);
    }

    public int right(float x, float size) {
        return (int)Math.ceil(x + this.right * size);
    }

    public int bottom(float y, float size) {
        return (int)Math.ceil(y + this.bottom * size);
    }

    void build(class_4588 output, Matrix3x2fc pose, float x, float y, float size, int color) {
        for (int i = 0; i < this.quads.length; i += 8) {
            float x0 = x + this.quads[i] * size;
            float y0 = y + this.quads[i + 1] * size;
            float x1 = x + this.quads[i + 2] * size;
            float y1 = y + this.quads[i + 3] * size;
            float u0 = this.quads[i + 4];
            float v0 = this.quads[i + 5];
            float u1 = this.quads[i + 6];
            float v1 = this.quads[i + 7];
            output.method_70815(pose, x0, y0).method_22913(u0, v0).method_39415(color);
            output.method_70815(pose, x0, y1).method_22913(u0, v1).method_39415(color);
            output.method_70815(pose, x1, y1).method_22913(u1, v1).method_39415(color);
            output.method_70815(pose, x1, y0).method_22913(u1, v0).method_39415(color);
        }
    }
}

