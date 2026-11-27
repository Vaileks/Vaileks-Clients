/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.gui;

import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class BrandLogo {
    private static final class_2960 LOGO = class_2960.method_60655((String)"bloodhelper", (String)"textures/logo.png");

    private BrandLogo() {
    }

    public static void draw(class_332 g, int x, int y, int w) {
        g.method_25302(class_10799.field_56883, LOGO, x, y, 0.0f, 0.0f, w, Math.round((float)w * 722.0f / 1105.0f), 1105, 722, 1105, 722);
    }
}

