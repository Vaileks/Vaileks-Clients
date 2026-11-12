/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
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

