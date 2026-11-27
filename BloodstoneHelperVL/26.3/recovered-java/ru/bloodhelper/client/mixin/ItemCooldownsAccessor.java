package ru.bloodhelper.client.mixin;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemCooldowns.class})
public interface ItemCooldownsAccessor {
    @Accessor(value="cooldowns")
    public Map<Identifier, Object> bloodhelper$cooldowns();

    @Accessor(value="tickCount")
    public int bloodhelper$tickCount();
}

