/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1657
 *  net.minecraft.class_636
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.PlayerMetrics;
import ru.bloodhelper.client.skins.VeritiAnimation;

@Mixin(value={class_636.class})
public class PlayerMetricsAttackMixin {
    @Inject(method={"method_2918"}, at={@At(value="HEAD")})
    private void bloodhelper$attack(class_1657 player, class_1297 target, CallbackInfo ci) {
        VeritiAnimation.attack(target);
        PlayerMetrics.attack(target);
    }
}

