/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_1044
 */
package pyz1k.render.builders.impl;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.class_1044;
import pyz1k.render.builders.AbstractBuilder;
import pyz1k.render.builders.states.QuadColorState;
import pyz1k.render.builders.states.QuadRadiusState;
import pyz1k.render.builders.states.SizeState;
import pyz1k.render.renderers.HeadTextureRenderer;

public final class HeadTextureBuilder
extends AbstractBuilder<HeadTextureRenderer> {
    private SizeState size;
    private QuadRadiusState radius;
    private QuadColorState color;
    private float smoothness;
    private float u;
    private float v;
    private float texWidth;
    private float texHeight;
    private GpuTextureView textureId;

    public HeadTextureBuilder size(SizeState size) {
        this.size = size;
        return this;
    }

    public HeadTextureBuilder radius(QuadRadiusState radius) {
        this.radius = radius;
        return this;
    }

    public HeadTextureBuilder color(QuadColorState color) {
        this.color = color;
        return this;
    }

    public HeadTextureBuilder smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public HeadTextureBuilder texture(float u, float v, float texWidth, float texHeight, class_1044 texture) {
        return this.texture(u, v, texWidth, texHeight, texture.method_71659());
    }

    public HeadTextureBuilder texture(float u, float v, float texWidth, float texHeight, GpuTextureView texture) {
        this.u = u;
        this.v = v;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.textureId = texture;
        return this;
    }

    @Override
    protected HeadTextureRenderer _build() {
        return new HeadTextureRenderer(this.size, this.radius, this.color, this.smoothness, this.u, this.v, this.texWidth, this.texHeight, this.textureId);
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = QuadRadiusState.NO_ROUND;
        this.color = QuadColorState.WHITE;
        this.smoothness = 1.0f;
        this.u = 0.0f;
        this.v = 0.0f;
        this.texWidth = 0.0f;
        this.texHeight = 0.0f;
        this.textureId = null;
    }
}

