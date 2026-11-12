/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1703
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_310
 *  net.minecraft.class_465
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_310;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.DropGuardModule;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={class_465.class})
abstract class DropGuardContainerMixin {
    @Shadow
    @Final
    protected class_1703 field_2797;

    DropGuardContainerMixin() {
    }

    @Inject(method={"method_2383"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$guardSlot(class_1735 slot, int slotId, int button, class_1713 type, CallbackInfo ci) {
        if (SlotLockModule.block(this.field_2797, slot, button, type) || DropGuardModule.blockContainerDrop(class_310.method_1551(), this.field_2797, slot, slotId, button, type)) {
            ci.cancel();
        }
    }
}

