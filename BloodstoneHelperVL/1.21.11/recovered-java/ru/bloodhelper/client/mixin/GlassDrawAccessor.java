package ru.bloodhelper.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net/minecraft/class_11228$class_11230"})
public interface GlassDrawAccessor {
    @Accessor(value="comp_4049")
    public RenderPipeline bloodhelper$pipeline();
}

