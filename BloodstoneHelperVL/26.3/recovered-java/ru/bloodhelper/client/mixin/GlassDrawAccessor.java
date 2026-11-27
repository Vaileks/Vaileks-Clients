package ru.bloodhelper.client.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net.minecraft.client.gui.render.GuiRenderer$Draw"})
public interface GlassDrawAccessor {
    @Accessor(value="pipeline")
    public RenderPipeline bloodhelper$pipeline();
}

