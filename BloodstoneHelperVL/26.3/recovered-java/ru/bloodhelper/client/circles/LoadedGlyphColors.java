/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.font.FontManager
 *  net.minecraft.client.gui.font.FontSet
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.FontDescription$Resource
 *  net.minecraft.network.chat.Style
 */
package ru.bloodhelper.client.circles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import ru.bloodhelper.client.circles.DominantColor;
import ru.bloodhelper.client.circles.GlyphColorSource;
import ru.bloodhelper.client.mixin.CirclesFontManagerAccessor;
import ru.bloodhelper.client.mixin.CirclesMinecraftAccessor;

public final class LoadedGlyphColors {
    private LoadedGlyphColors() {
    }

    public static int resolve(Style style, int point) {
        FontDescription fontDescription = style.getFont();
        if (!(fontDescription instanceof FontDescription.Resource)) {
            return -1;
        }
        FontDescription.Resource font = (FontDescription.Resource)fontDescription;
        FontManager manager = ((CirclesMinecraftAccessor)Minecraft.getInstance()).bloodhelper$fontManager();
        FontSet storage = ((CirclesFontManagerAccessor)manager).bloodhelper$font(font.id());
        int rgb = ((GlyphColorSource)storage).bloodstonecircles$getColor(point);
        return rgb == -1 || style.getColor() == null ? rgb : DominantColor.tint(rgb, style.getColor().getValue());
    }
}

