/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10529
 *  net.minecraft.class_11659
 *  net.minecraft.class_11971
 *  net.minecraft.class_4587
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_10529;
import net.minecraft.class_11659;
import net.minecraft.class_11971;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.OptimizerModule;

@Mixin(value={class_10529.class})
public abstract class OptimizerSignTextMixin {
    @Inject(method={"method_65828"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$distantText(class_11971 state, class_4587 pose, class_11659 collector, boolean front, CallbackInfo ci) {
        if (OptimizerModule.hideSignText(state.field_62673)) {
            ci.cancel();
        }
    }
}

