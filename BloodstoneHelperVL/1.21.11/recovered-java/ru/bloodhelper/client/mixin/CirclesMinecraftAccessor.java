package ru.bloodhelper.client.mixin;

import net.minecraft.class_310;
import net.minecraft.class_378;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_310.class})
public interface CirclesMinecraftAccessor {
    @Accessor(value="field_1708")
    public class_378 bloodhelper$fontManager();
}

