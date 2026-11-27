/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_638
 *  net.minecraft.class_6539
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_2338;
import net.minecraft.class_638;
import net.minecraft.class_6539;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={class_638.class})
abstract class WorldVisualTintMixin {
    WorldVisualTintMixin() {
    }

    @Inject(method={"method_23752"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$visualBiomePalette(class_2338 pos, class_6539 resolver, CallbackInfoReturnable<Integer> cir) {
        Integer color = WorldVisuals.blockTint(pos, resolver);
        if (color != null) {
            cir.setReturnValue((Object)color);
        }
    }
}

