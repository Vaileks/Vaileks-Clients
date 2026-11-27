/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemCooldowns
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package pyz1k.mixin;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemCooldowns.class})
public interface ItemCooldownsAccessor {
    @Accessor(value="cooldowns")
    public Map<Identifier, Object> pyz1k$getCooldowns();

    @Accessor(value="tickCount")
    public int pyz1k$getTickCount();
}

