package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Font.class})
public interface FontProviderAccessor {
    @Accessor(value="provider")
    public Font.Provider bloodhelper$provider();
}

