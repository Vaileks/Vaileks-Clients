package pyz1k.extensions.mixin;

import net.minecraft.entity.player.ItemCooldownManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.entity.player.ItemCooldownManager$Entry")
public interface ItemCooldownEntryAccessor {
    @Accessor("startTick")
    int bloodSwordCooldown$getStartTick();

    @Accessor("endTick")
    int bloodSwordCooldown$getEndTick();
}
