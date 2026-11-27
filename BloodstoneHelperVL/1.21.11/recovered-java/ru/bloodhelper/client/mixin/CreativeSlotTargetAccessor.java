package ru.bloodhelper.client.mixin;

import net.minecraft.class_1735;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net/minecraft/class_481$class_484"})
public interface CreativeSlotTargetAccessor {
    @Accessor(value="field_2898")
    public class_1735 bloodhelper$target();
}

