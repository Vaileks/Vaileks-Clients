/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.ColorResolver
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.WorldVisuals;

@Mixin(value={ClientLevel.class})
abstract class WorldVisualTintMixin {
    WorldVisualTintMixin() {
    }

    @Inject(method={"getBlockTint"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$visualBiomePalette(BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> cir) {
        Integer color = WorldVisuals.blockTint(pos, resolver);
        if (color != null) {
            cir.setReturnValue((Object)color);
        }
    }
}

