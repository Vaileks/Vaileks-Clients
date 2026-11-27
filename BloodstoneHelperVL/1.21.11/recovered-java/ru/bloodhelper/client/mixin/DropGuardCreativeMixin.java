/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_310
 *  net.minecraft.class_481
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_310;
import net.minecraft.class_481;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.DropGuardModule;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={class_481.class})
abstract class DropGuardCreativeMixin {
    DropGuardCreativeMixin() {
    }

    @Inject(method={"method_2383"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$guardCreativeSlot(class_1735 slot, int slotId, int button, class_1713 type, CallbackInfo ci) {
        class_481 screen = (class_481)this;
        if (SlotLockModule.block(screen.method_17577(), slot, button, type) || DropGuardModule.blockContainerDrop(class_310.method_1551(), screen.method_17577(), slot, slotId, button, type)) {
            ci.cancel();
        }
    }
}

