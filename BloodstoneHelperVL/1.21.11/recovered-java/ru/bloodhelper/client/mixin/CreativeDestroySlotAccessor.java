package ru.bloodhelper.client.mixin;

import net.minecraft.class_1735;
import net.minecraft.class_481;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_481.class})
public interface CreativeDestroySlotAccessor {
    @Accessor(value="field_2889")
    public class_1735 bloodhelper$destroySlot();
}

