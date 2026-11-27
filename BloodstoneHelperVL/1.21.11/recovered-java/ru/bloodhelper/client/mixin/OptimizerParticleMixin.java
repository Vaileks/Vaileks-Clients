/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2394
 *  net.minecraft.class_702
 *  net.minecraft.class_703
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_2394;
import net.minecraft.class_702;
import net.minecraft.class_703;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.OptimizerModule;

@Mixin(value={class_702.class})
public abstract class OptimizerParticleMixin {
    @Inject(method={"method_3056"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$ambientParticles(class_2394 options, double x, double y, double z, double dx, double dy, double dz, CallbackInfoReturnable<class_703> cir) {
        if (OptimizerModule.skipParticle(options)) {
            cir.setReturnValue(null);
        }
    }
}

