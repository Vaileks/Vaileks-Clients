/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10017
 *  net.minecraft.class_1937
 *  net.minecraft.class_310
 *  net.minecraft.class_897
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_10017;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.OptimizerModule;

@Mixin(value={class_897.class})
public abstract class OptimizerShadowMixin {
    @Inject(method={"method_72979"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$entityShadow(class_10017 state, class_310 client, class_1937 level, CallbackInfo ci) {
        if (OptimizerModule.disableEntityShadows()) {
            state.field_61823.clear();
            ci.cancel();
        }
    }
}

