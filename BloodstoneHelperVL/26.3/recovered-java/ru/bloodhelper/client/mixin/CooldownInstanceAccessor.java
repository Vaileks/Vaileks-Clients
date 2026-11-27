package ru.bloodhelper.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net.minecraft.world.item.ItemCooldowns$CooldownInstance"})
public interface CooldownInstanceAccessor {
    @Accessor(value="startTime")
    public int bloodhelper$startTime();

    @Accessor(value="endTime")
    public int bloodhelper$endTime();
}

