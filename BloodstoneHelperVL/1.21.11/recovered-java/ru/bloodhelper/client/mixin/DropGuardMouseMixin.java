/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11910
 *  net.minecraft.class_310
 *  net.minecraft.class_312
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_11910;
import net.minecraft.class_310;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.DropGuardModule;

@Mixin(value={class_312.class})
abstract class DropGuardMouseMixin {
    DropGuardMouseMixin() {
    }

    @Inject(method={"method_1601"}, at={@At(value="HEAD")})
    private void bloodhelper$dropPhysicalMouse(long window, class_11910 info, int action, CallbackInfo ci) {
        DropGuardModule.mouseInput(class_310.method_1551(), window, info, action);
    }
}

