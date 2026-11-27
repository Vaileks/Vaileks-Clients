/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.font.FontManager
 *  net.minecraft.client.gui.font.FontSet
 *  net.minecraft.resources.Identifier
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
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

