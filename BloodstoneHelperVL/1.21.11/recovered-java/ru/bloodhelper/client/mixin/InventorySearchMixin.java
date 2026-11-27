/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1735
 *  net.minecraft.class_332
 *  net.minecraft.class_465
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1735;
import net.minecraft.class_332;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.InventorySearch;
import ru.bloodhelper.client.modules.SlotLockModule;

@Mixin(value={class_465.class})
public abstract class InventorySearchMixin {
    @Inject(method={"method_2385"}, at={@At(value="TAIL")})
    private void bloodhelper$searchSlot(class_332 g, class_1735 slot, int mouseX, int mouseY, CallbackInfo ci) {
        InventorySearch.slot(g, slot);
        SlotLockModule.renderSlot(g, slot);
    }
}

