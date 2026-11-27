/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.util.FormattedCharSequence
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.bloodhelper.client.modules;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
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

    public static Component withoutTeamColors(Component original) {
        MutableComponent copy = Component.empty();
        original.visit((style, text) -> {
            copy.append((Component)Component.literal((String)text).setStyle(style.withColor((TextColor)null)));
            return Optional.empty();
        }, Style.EMPTY);
        return copy;
    }

    public static FormattedCharSequence withoutTeamColors(FormattedCharSequence original) {
        return sink -> original.accept((index, style, point) -> sink.accept(index, style.withColor((TextColor)null), point));
    }
}

