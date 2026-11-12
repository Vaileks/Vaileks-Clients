/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.OptimizerModule;

@Mixin(value={BlockBehaviour.BlockStateBase.class})
public abstract class OptimizerGrassMixin {
    @Inject(method={"getRenderShape"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$grassGeometry(CallbackInfoReturnable<RenderShape> cir) {
        if (OptimizerModule.hideGrass(((BlockBehaviour.BlockStateBase)this).getBlock())) {
            cir.setReturnValue((Object)RenderShape.INVISIBLE);
        }
    }
}

