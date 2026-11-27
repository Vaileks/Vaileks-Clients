/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11603
 *  net.minecraft.class_11719
 *  net.minecraft.class_11719$class_11721
 *  net.minecraft.class_11769
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_327$class_11602
 */
package ru.bloodhelper.client.gui;

import net.minecraft.class_11603;
import net.minecraft.class_11719;
import net.minecraft.class_11769;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import ru.bloodhelper.client.mixin.FontProviderAccessor;

public final class UiFonts {
    private static final class_11719 WORLD_FACE = new class_11719.class_11721(class_2960.method_60655((String)"bloodhelper", (String)"ui"));
    private static class_327 world;
    private static class_327 owner;

    private UiFonts() {
    }

    public static class_327 world() {
        class_327 base = class_310.method_1551().field_1772;
        if (base != owner) {
            final class_327.class_11602 provider = ((FontProviderAccessor)base).bloodhelper$provider();
            world = new class_327(new class_327.class_11602(){

                public class_11603 method_72735(class_11719 requested) {
                    return provider.method_72735(class_11719.field_61972.equals((Object)requested) ? WORLD_FACE : requested);
                }

                public class_11769 method_73366() {
                    return provider.method_73366();
                }
            });
            owner = base;
        }
        return world;
    }
}

