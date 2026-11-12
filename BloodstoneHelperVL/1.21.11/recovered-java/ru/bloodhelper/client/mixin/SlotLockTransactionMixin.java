/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1703
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_310
 *  net.minecraft.class_636
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_310;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={class_636.class})
abstract class SlotLockTransactionMixin {
    SlotLockTransactionMixin() {
    }

    @Inject(method={"method_2906"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$lockTransaction(int menuId, int slotId, int button, class_1713 type, class_1657 player, CallbackInfo ci) {
        class_1735 slot;
        if (player != class_310.method_1551().field_1724 || menuId != player.field_7512.field_7763) {
            return;
        }
        class_1703 menu = player.field_7512;
        class_1735 class_17352 = slot = slotId >= 0 && slotId < menu.field_7761.size() ? menu.method_7611(slotId) : null;
        if (SlotLockModule.block(menu, slot, button, type)) {
            ci.cancel();
        }
    }
}

