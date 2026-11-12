/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1735
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1735;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net/minecraft/class_481$class_484"})
public interface CreativeSlotTargetAccessor {
    @Accessor(value="field_2898")
    public class_1735 bloodhelper$target();
}

