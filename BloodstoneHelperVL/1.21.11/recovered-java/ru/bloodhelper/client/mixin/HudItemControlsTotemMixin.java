/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_1041
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_4603
 *  net.minecraft.class_5819
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1041;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4603;
import net.minecraft.class_5819;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.config.HudItemControlsSettings;
import ru.bloodhelper.client.modules.HudItemControlsModule;

@Mixin(value={class_4603.class})
abstract class HudItemControlsTotemMixin {
    @Shadow
    private class_1799 field_59972;
    @Shadow
    private int field_59973;
    @Unique
    private int bloodhelper$duration = 40;

    HudItemControlsTotemMixin() {
    }

    @Inject(method={"method_70938"}, at={@At(value="TAIL")})
    private void bloodhelper$duration(class_1799 stack, class_5819 random, CallbackInfo ci) {
        this.field_59973 = this.bloodhelper$duration = this.bloodhelper$customTotem() ? HudItemControlsModule.settings().totem.durationTicks : 40;
    }

    @Unique
    private boolean bloodhelper$customTotem() {
        return HudItemControlsModule.settings().totem.enabled && this.field_59972 != null && this.field_59972.method_31574(class_1802.field_8288);
    }

    @ModifyConstant(method={"method_70939"}, constant={@Constant(intValue=40)})
    private int bloodhelper$elapsed(int original) {
        return this.bloodhelper$duration;
    }

    @ModifyConstant(method={"method_70939"}, constant={@Constant(floatValue=40.0f)})
    private float bloodhelper$progress(float original) {
        return this.bloodhelper$duration;
    }

    @WrapOperation(method={"method_70939"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_4587;method_46416(FFF)V")})
    private void bloodhelper$position(class_4587 pose, float x, float y, float z, Operation<Void> original) {
        if (this.bloodhelper$customTotem()) {
            HudItemControlsSettings.Totem s = HudItemControlsModule.settings().totem;
            class_1041 window = class_310.method_1551().method_22683();
            double halfHeight = (double)(-z) * Math.tan(Math.toRadians(HudItemControlsModule.hudFov / 2.0f));
            x += (float)((2.0 * s.x - 1.0) * halfHeight * (double)window.method_4489() / Math.max(1.0, (double)window.method_4506()));
            y += (float)((1.0 - 2.0 * s.y) * halfHeight);
        }
        original.call(new Object[]{pose, Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
    }

    @WrapOperation(method={"method_70939"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_4587;method_22905(FFF)V")})
    private void bloodhelper$size(class_4587 pose, float x, float y, float z, Operation<Void> original) {
        float scale = this.bloodhelper$customTotem() ? (float)HudItemControlsModule.settings().totem.scale : 1.0f;
        original.call(new Object[]{pose, Float.valueOf(x * scale), Float.valueOf(y * scale), Float.valueOf(z * scale)});
    }
}

