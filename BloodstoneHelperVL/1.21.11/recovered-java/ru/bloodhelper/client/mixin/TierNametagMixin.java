/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10017
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_897
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import java.util.Optional;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.circles.BloodstoneCircles;
import ru.bloodhelper.client.cosmetic.CosmeticNameText;
import ru.bloodhelper.client.cosmetic.CosmeticNames;
import ru.bloodhelper.client.cosmetic.NicknamePalette;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.tiers.TierDisplay;

@Mixin(value={class_897.class})
public class TierNametagMixin {
    @Inject(method={"method_62354"}, at={@At(value="TAIL")})
    private void bloodhelper$tiers(class_1297 entity, class_10017 state, float partial, CallbackInfo ci) {
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657)entity;
            if (state.field_53337 != null) {
                String name = player.method_7334().name();
                state.field_53337 = TierDisplay.decorate(name, state.field_53337, false);
                Optional<NicknamePalette> palette = CosmeticNames.palette(player.method_5667(), name, StreamerMode.hideNickname() || StreamerMode.hideNametags());
                state.field_53337 = BloodstoneCircles.rewrite(state.field_53337, false);
                if (palette.isPresent()) {
                    state.field_53337 = CosmeticNameText.color(state.field_53337, name, palette.get(), System.currentTimeMillis());
                }
            }
        }
    }
}

