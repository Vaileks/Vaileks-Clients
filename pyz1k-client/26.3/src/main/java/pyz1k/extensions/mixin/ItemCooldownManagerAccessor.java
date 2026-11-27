package pyz1k.extensions.mixin;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemCooldowns.class})
public interface ItemCooldownManagerAccessor {
    @Accessor(value="cooldowns")
    public Map<Identifier, Object> bloodSwordCooldown$getEntries();

    @Accessor(value="tickCount")
    public int bloodSwordCooldown$getTick();
}

