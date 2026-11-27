package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={FontManager.class})
public interface CirclesFontManagerAccessor {
    @Invoker(value="getFontSetRaw")
    public FontSet bloodhelper$font(Identifier var1);
}

