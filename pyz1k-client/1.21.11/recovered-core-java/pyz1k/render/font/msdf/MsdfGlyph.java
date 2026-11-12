/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4588
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package pyz1k.render.font.msdf;

import net.minecraft.class_4588;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import pyz1k.render.font.msdf.FontData;

public final class MsdfGlyph {
    private final float minU;
    private final float maxU;
    private final float minV;
    private final float maxV;
    private final float advance;
    private final float topPosition;
    private final float width;
    private final float height;

    public MsdfGlyph(FontData.GlyphData data, float atlasWidth, float atlasHeight) {
        this.advance = data.advance();
        FontData.BoundsData atlasBounds = data.atlasBounds();
        if (atlasBounds != null) {
            this.minU = atlasBounds.left() / atlasWidth;
            this.maxU = atlasBounds.right() / atlasWidth;
            this.minV = 1.0f - atlasBounds.top() / atlasHeight;
            this.maxV = 1.0f - atlasBounds.bottom() / atlasHeight;
        } else {
            this.minU = 0.0f;
            this.maxU = 0.0f;
            this.minV = 0.0f;
            this.maxV = 0.0f;
        }
        FontData.BoundsData planeBounds = data.planeBounds();
        if (planeBounds != null) {
            this.width = planeBounds.right() - planeBounds.left();
            this.height = planeBounds.top() - planeBounds.bottom();
            this.topPosition = planeBounds.top();
        } else {
            this.width = 0.0f;
            this.height = 0.0f;
            this.topPosition = 0.0f;
        }
    }

    public float apply(Matrix4f matrix, class_4588 consumer, float size, float x, float y, float z, int color) {
        float w = this.width * size;
        float h = this.height * size;
        consumer.method_22918((Matrix4fc)matrix, x, y -= this.topPosition * size, z).method_22913(this.minU, this.minV).method_39415(color);
        consumer.method_22918((Matrix4fc)matrix, x, y + h, z).method_22913(this.minU, this.maxV).method_39415(color);
        consumer.method_22918((Matrix4fc)matrix, x + w, y + h, z).method_22913(this.maxU, this.maxV).method_39415(color);
        consumer.method_22918((Matrix4fc)matrix, x + w, y, z).method_22913(this.maxU, this.minV).method_39415(color);
        return this.advance * size;
    }

    public float getAdvance(float size) {
        return this.advance * size;
    }

    public float minU() {
        return this.minU;
    }

    public float maxU() {
        return this.maxU;
    }

    public float minV() {
        return this.minV;
    }

    public float maxV() {
        return this.maxV;
    }

    public float topPosition() {
        return this.topPosition;
    }

    public float width() {
        return this.width;
    }

    public float height() {
        return this.height;
    }
}

