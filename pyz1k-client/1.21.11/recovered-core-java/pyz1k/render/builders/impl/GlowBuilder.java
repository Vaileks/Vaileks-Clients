/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.render.builders.impl;

import pyz1k.render.builders.AbstractBuilder;
import pyz1k.render.builders.states.QuadColorState;
import pyz1k.render.builders.states.QuadRadiusState;
import pyz1k.render.builders.states.SizeState;
import pyz1k.render.renderers.GlowRenderer;

public final class GlowBuilder
extends AbstractBuilder<GlowRenderer> {
    private SizeState size;
    private QuadRadiusState radius;
    private QuadColorState color;
    private float smoothness;
    private float spread;

    public GlowBuilder size(SizeState size) {
        this.size = size;
        return this;
    }

    public GlowBuilder radius(QuadRadiusState radius) {
        this.radius = radius;
        return this;
    }

    public GlowBuilder color(QuadColorState color) {
        this.color = color;
        return this;
    }

    public GlowBuilder smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public GlowBuilder spread(float spread) {
        this.spread = spread;
        return this;
    }

    @Override
    protected GlowRenderer _build() {
        return new GlowRenderer(this.size, this.radius, this.color, this.smoothness, this.spread);
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = QuadRadiusState.NO_ROUND;
        this.color = QuadColorState.TRANSPARENT;
        this.smoothness = 1.0f;
        this.spread = 0.0f;
    }
}

