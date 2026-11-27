package ru.bloodhelper.client.mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.OptimizerModule;

@Mixin(value={BlockEntityRenderDispatcher.class})
public abstract class OptimizerSignMixin {
    @Inject(method={"tryExtractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$distantSigns(BlockEntity entity, float partial, ModelFeatureRenderer.CrumblingOverlay overlay, boolean outline, CallbackInfoReturnable<BlockEntityRenderState> cir) {
        if (entity instanceof SignBlockEntity && OptimizerModule.hideSign(entity.getBlockPos())) {
            cir.setReturnValue(null);
        }
    }
}

