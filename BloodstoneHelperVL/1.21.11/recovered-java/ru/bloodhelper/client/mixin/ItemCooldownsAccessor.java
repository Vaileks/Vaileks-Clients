package ru.bloodhelper.client.mixin;

import java.util.Map;
import net.minecraft.class_1796;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_1796.class})
public interface ItemCooldownsAccessor {
    @Accessor(value="field_8024")
    public Map<class_2960, Object> bloodhelper$cooldowns();

    @Accessor(value="field_8025")
    public int bloodhelper$tickCount();
}

