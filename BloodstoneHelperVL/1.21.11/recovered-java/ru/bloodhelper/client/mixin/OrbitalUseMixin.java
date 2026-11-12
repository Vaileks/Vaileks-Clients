/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1269
 *  net.minecraft.class_1657
 *  net.minecraft.class_3965
 *  net.minecraft.class_636
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_3965;
import net.minecraft.class_636;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.visuals.OrbitalTracker;

@Mixin(value={class_636.class})
public abstract class OrbitalUseMixin {
    @Inject(method={"method_2919"}, at={@At(value="HEAD")})
    private void bloodhelper$stabAir(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> ci) {
        OrbitalTracker.onUse(player, hand);
    }

    @Inject(method={"method_2896"}, at={@At(value="HEAD")})
    private void bloodhelper$stabBlock(class_746 player, class_1268 hand, class_3965 hit, CallbackInfoReturnable<class_1269> ci) {
        OrbitalTracker.onUse((class_1657)player, hand);
    }
}

