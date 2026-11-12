/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package pyz1k.extensions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net.minecraft.world.item.ItemCooldowns$CooldownInstance"})
public interface ItemCooldownEntryAccessor {
    @Accessor(value="startTime")
    public int bloodSwordCooldown$getStartTick();

    @Accessor(value="endTime")
    public int bloodSwordCooldown$getEndTick();
}

