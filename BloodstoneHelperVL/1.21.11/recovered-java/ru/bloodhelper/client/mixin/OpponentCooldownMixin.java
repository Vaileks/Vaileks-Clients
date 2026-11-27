/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_1007
 *  net.minecraft.class_11659
 *  net.minecraft.class_11890
 *  net.minecraft.class_12075
 *  net.minecraft.class_4587
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11659;
import net.minecraft.class_11890;
import net.minecraft.class_12075;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.OpponentCooldownRenderer;

@Mixin(value={class_1007.class})
public abstract class OpponentCooldownMixin {
    @Inject(method={"method_62604(Lnet/minecraft/class_11890;Lnet/minecraft/class_10055;F)V"}, at={@At(value="TAIL")})
    private void bloodhelper$extractCooldowns(class_11890 player, class_10055 state, float partialTick, CallbackInfo ci) {
        OpponentCooldownRenderer.extract(player, state, partialTick);
    }

    @Inject(method={"method_4213(Lnet/minecraft/class_10055;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, at={@At(value="TAIL")})
    private void bloodhelper$submitCooldowns(class_10055 state, class_4587 pose, class_11659 collector, class_12075 camera, CallbackInfo ci) {
        OpponentCooldownRenderer.submit(state, pose, collector, camera);
    }
}

