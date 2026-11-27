package ru.bloodhelper.client.mixin;

import net.minecraft.class_342;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_408.class})
public interface LoginGuardChatAccessor {
    @Accessor(value="field_2382")
    public class_342 bloodhelper$loginInput();
}

