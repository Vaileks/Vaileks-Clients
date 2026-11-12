/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_11631
 *  net.minecraft.class_11633
 *  net.minecraft.class_11635
 *  net.minecraft.class_2960
 *  net.minecraft.class_309
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_11631;
import net.minecraft.class_11633;
import net.minecraft.class_11635;
import net.minecraft.class_2960;
import net.minecraft.class_309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.modules.VisualHitboxes;

@Mixin(value={class_309.class})
abstract class HitboxesKeyboardMixin {
    HitboxesKeyboardMixin() {
    }

    @WrapOperation(method={"method_1468"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_11633;method_72775(Lnet/minecraft/class_2960;)Z")})
    private boolean bloodhelper$toggleHitboxes(class_11633 entries, class_2960 entry, Operation<Boolean> original) {
        if (class_11631.field_61581.equals((Object)entry) && BloodHelperClient.config().hitboxes.enabled) {
            entries.method_72767(entry, class_11635.field_61595);
            return VisualHitboxes.toggleVisibility();
        }
        return (Boolean)original.call(new Object[]{entries, entry});
    }
}

