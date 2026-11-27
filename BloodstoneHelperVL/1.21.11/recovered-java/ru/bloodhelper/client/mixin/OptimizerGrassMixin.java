/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2464
 *  net.minecraft.class_4970$class_4971
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_2464;
import net.minecraft.class_4970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.OptimizerModule;

@Mixin(value={class_4970.class_4971.class})
public abstract class OptimizerGrassMixin {
    @Inject(method={"method_26217"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$grassGeometry(CallbackInfoReturnable<class_2464> cir) {
        if (OptimizerModule.hideGrass(((class_4970.class_4971)this).method_26204())) {
            cir.setReturnValue((Object)class_2464.field_11455);
        }
    }
}

