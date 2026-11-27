/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_5250
 *  net.minecraft.class_5251
 *  net.minecraft.class_5481
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.bloodhelper.client.modules;

import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;
import net.minecraft.class_5251;
import net.minecraft.class_5481;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.NameVisibilitySettings;

public final class BetterNameVisibilityModule {
    public static final int FULL_BRIGHT = 0xF000F0;

    private BetterNameVisibilityModule() {
    }

    public static boolean active() {
        return BloodHelperClient.config().betterNameVisibilityEnabled();
    }

    public static NameVisibilitySettings settings() {
        return BloodHelperClient.config().betterNameVisibility;
    }

    public static Matrix4f scalePose(Matrix4fc original, double exponent) {
        double distance = Math.sqrt((double)original.m30() * (double)original.m30() + (double)original.m31() * (double)original.m31() + (double)original.m32() * (double)original.m32());
        float factor = NameVisibilitySettings.scale(distance, exponent);
        return new Matrix4f(original).translate(0.0f, 9.0f, 0.0f).scale(factor).translate(0.0f, -9.0f, 0.0f);
    }

    public static class_2561 withoutTeamColors(class_2561 original) {
        class_5250 copy = class_2561.method_43473();
        original.method_27658((style, text) -> {
            copy.method_10852((class_2561)class_2561.method_43470((String)text).method_10862(style.method_27703((class_5251)null)));
            return Optional.empty();
        }, class_2583.field_24360);
        return copy;
    }

    public static class_5481 withoutTeamColors(class_5481 original) {
        return sink -> original.accept((index, style, point) -> sink.accept(index, style.method_27703((class_5251)null), point));
    }
}

