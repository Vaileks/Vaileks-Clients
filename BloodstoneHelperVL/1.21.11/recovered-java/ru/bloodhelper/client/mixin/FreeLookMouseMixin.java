/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_11910
 *  net.minecraft.class_310
 *  net.minecraft.class_312
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_11910;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.FreeLookModule;
import ru.bloodhelper.client.modules.PlayerMetrics;
import ru.bloodhelper.client.modules.ZoomModule;

@Mixin(value={class_312.class})
public class FreeLookMouseMixin {
    @Inject(method={"method_1601"}, at={@At(value="HEAD")})
    private void bloodhelper$click(long window, class_11910 info, int action, CallbackInfo ci) {
        if (window == class_310.method_1551().method_22683().method_4490()) {
            PlayerMetrics.click(info.comp_4801(), action);
        }
    }

    @WrapOperation(method={"method_1606"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_5872(DD)V")}, require=1)
    private void bloodhelper$mouseTurn(class_746 player, double dx, double dy, Operation<Void> original) {
        double scale = ZoomModule.sensitivityScale();
        if (!FreeLookModule.turn(dx *= scale, dy *= scale)) {
            original.call(new Object[]{player, dx, dy});
        }
    }
}

