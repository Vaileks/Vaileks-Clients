package pyz1k.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net.minecraft.world.item.ItemCooldowns$CooldownInstance"})
public interface CooldownInstanceAccessor {
    @Accessor(value="startTime")
    public int pyz1k$getStartTime();

    @Accessor(value="endTime")
    public int pyz1k$getEndTime();
}

