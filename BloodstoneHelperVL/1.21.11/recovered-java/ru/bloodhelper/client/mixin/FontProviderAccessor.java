/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_327
 *  net.minecraft.class_327$class_11602
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_327;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_327.class})
public interface FontProviderAccessor {
    @Accessor(value="field_61529")
    public class_327.class_11602 bloodhelper$provider();
}

