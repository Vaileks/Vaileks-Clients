/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1542
 *  net.minecraft.class_4604
 *  net.minecraft.class_897
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_4604;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.DroppedItemFilter;

@Mixin(value={class_897.class})
public abstract class DroppedItemFilterMixin {
    @Inject(method={"method_3933"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hideSelectedDrop(class_1297 entity, class_4604 frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        class_1542 item;
        if (entity instanceof class_1542 && DroppedItemFilter.hides((item = (class_1542)entity).method_6983())) {
            cir.setReturnValue((Object)false);
        }
    }
}

