/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.bloodhelper.client.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net.minecraft.client.gui.render.GuiRenderer$Draw"})
public interface GlassDrawAccessor {
    @Accessor(value="pipeline")
    public RenderPipeline bloodhelper$pipeline();
}

