/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.render.builders;

import pyz1k.render.builders.impl.BlurBuilder;
import pyz1k.render.builders.impl.BorderBuilder;
import pyz1k.render.builders.impl.GlowBuilder;
import pyz1k.render.builders.impl.HeadTextureBuilder;
import pyz1k.render.builders.impl.RectangleBuilder;
import pyz1k.render.builders.impl.TextureBuilder;

public final class Builder {
    private static final RectangleBuilder RECTANGLE_BUILDER = new RectangleBuilder();
    private static final BorderBuilder BORDER_BUILDER = new BorderBuilder();
    private static final TextureBuilder TEXTURE_BUILDER = new TextureBuilder();
    private static final HeadTextureBuilder HEAD_TEXTURE_BUILDER = new HeadTextureBuilder();
    private static final BlurBuilder BLUR_BUILDER = new BlurBuilder();
    private static final GlowBuilder GLOW_BUILDER = new GlowBuilder();

    public static RectangleBuilder rectangle() {
        return RECTANGLE_BUILDER;
    }

    public static BorderBuilder border() {
        return BORDER_BUILDER;
    }

    public static TextureBuilder texture() {
        return TEXTURE_BUILDER;
    }

    public static HeadTextureBuilder headTexture() {
        return HEAD_TEXTURE_BUILDER;
    }

    public static BlurBuilder blur() {
        return BLUR_BUILDER;
    }

    public static GlowBuilder glow() {
        return GLOW_BUILDER;
    }
}

