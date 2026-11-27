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

