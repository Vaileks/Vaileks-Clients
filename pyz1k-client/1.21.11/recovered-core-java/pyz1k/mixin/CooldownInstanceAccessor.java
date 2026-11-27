/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package pyz1k.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net/minecraft/class_1796$class_1797"})
public interface CooldownInstanceAccessor {
    @Accessor(value="comp_3083")
    public int pyz1k$getStartTime();

    @Accessor(value="comp_3084")
    public int pyz1k$getEndTime();
}

