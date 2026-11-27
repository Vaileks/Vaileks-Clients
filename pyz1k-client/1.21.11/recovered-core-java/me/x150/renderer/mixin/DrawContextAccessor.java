package me.x150.renderer.mixin;

import net.minecraft.class_11246;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_332.class})
public interface DrawContextAccessor {
    @Accessor(value="field_59826")
    public class_11246 getState();
}

