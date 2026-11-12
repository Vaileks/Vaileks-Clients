/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_355
 *  net.minecraft.class_640
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_2561;
import net.minecraft.class_355;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.circles.BloodstoneCircles;

@Mixin(value={class_355.class})
public abstract class CirclesTabMixin {
    @Inject(method={"method_1918"}, at={@At(value="RETURN")}, cancellable=true)
    private void bloodhelper$circles(class_640 info, CallbackInfoReturnable<class_2561> cir) {
        cir.setReturnValue((Object)BloodstoneCircles.rewrite((class_2561)cir.getReturnValue(), true));
    }
}

