/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_897
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  ru.bloodhelper.client.modules.BetterNameVisibilityModule
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.bloodhelper.client.modules.BetterNameVisibilityModule;

@Mixin(value={class_897.class})
public abstract class BetterNameDistanceMixin {
    @ModifyConstant(method={"method_62354"}, constant={@Constant(doubleValue=4096.0)}, require=0)
    private double bloodhelper$nameDistance(double original) {
        return BetterNameVisibilityModule.active() && BetterNameVisibilityModule.settings().unlimitedDistance ? Double.MAX_VALUE : original;
    }
}

