/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11719
 *  net.minecraft.class_11719$class_11721
 *  net.minecraft.class_2583
 *  net.minecraft.class_310
 *  net.minecraft.class_377
 *  net.minecraft.class_378
 */
package ru.bloodhelper.client.circles;

import net.minecraft.class_11719;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_377;
import net.minecraft.class_378;
import ru.bloodhelper.client.circles.DominantColor;
import ru.bloodhelper.client.circles.GlyphColorSource;
import ru.bloodhelper.client.mixin.CirclesFontManagerAccessor;
import ru.bloodhelper.client.mixin.CirclesMinecraftAccessor;

public final class LoadedGlyphColors {
    private LoadedGlyphColors() {
    }

    public static int resolve(class_2583 style, int point) {
        class_11719 class_117192 = style.method_27708();
        if (!(class_117192 instanceof class_11719.class_11721)) {
            return -1;
        }
        class_11719.class_11721 font = (class_11719.class_11721)class_117192;
        class_378 manager = ((CirclesMinecraftAccessor)class_310.method_1551()).bloodhelper$fontManager();
        class_377 storage = ((CirclesFontManagerAccessor)manager).bloodhelper$font(font.comp_4590());
        int rgb = ((GlyphColorSource)storage).bloodstonecircles$getColor(point);
        return rgb == -1 || style.method_10973() == null ? rgb : DominantColor.tint(rgb, style.method_10973().method_27716());
    }
}

