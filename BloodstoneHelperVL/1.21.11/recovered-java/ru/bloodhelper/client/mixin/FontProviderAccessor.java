package ru.bloodhelper.client.mixin;

import net.minecraft.class_327;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_327.class})
public interface FontProviderAccessor {
    @Accessor(value="field_61529")
    public class_327.class_11602 bloodhelper$provider();
}

