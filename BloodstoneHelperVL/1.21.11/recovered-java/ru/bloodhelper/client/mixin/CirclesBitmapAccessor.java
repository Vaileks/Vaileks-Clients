/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1011;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net/minecraft/class_386$class_388"})
public interface CirclesBitmapAccessor {
    @Accessor(value="comp_604")
    public class_1011 bloodhelper$image();

    @Accessor(value="comp_605")
    public int bloodhelper$x();

    @Accessor(value="comp_606")
    public int bloodhelper$y();

    @Accessor(value="comp_607")
    public int bloodhelper$width();

    @Accessor(value="comp_608")
    public int bloodhelper$height();
}

