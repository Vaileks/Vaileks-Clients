/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.font.providers.BitmapProvider$ImageDataHolder
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.font.providers.BitmapProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net.minecraft.client.gui.font.providers.BitmapProvider$Glyph"})
public interface CirclesBitmapAccessor {
    @Accessor(value="imageData")
    public BitmapProvider.ImageDataHolder bloodhelper$data();

    @Accessor(value="offsetX")
    public int bloodhelper$x();

    @Accessor(value="offsetY")
    public int bloodhelper$y();

    @Accessor(value="width")
    public int bloodhelper$width();

    @Accessor(value="height")
    public int bloodhelper$height();
}

