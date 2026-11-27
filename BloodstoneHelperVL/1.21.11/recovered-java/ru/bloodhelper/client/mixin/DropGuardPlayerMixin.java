/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_310;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.DropGuardModule;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={class_746.class})
abstract class DropGuardPlayerMixin {
    DropGuardPlayerMixin() {
    }

    @Inject(method={"method_7290(Z)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$guardHeldItem(boolean wholeStack, CallbackInfoReturnable<Boolean> cir) {
        if (this == class_310.method_1551().field_1724 && (SlotLockModule.selectedLocked() || DropGuardModule.blockSelectedDrop(class_310.method_1551(), wholeStack))) {
            cir.setReturnValue((Object)false);
        }
    }
}

