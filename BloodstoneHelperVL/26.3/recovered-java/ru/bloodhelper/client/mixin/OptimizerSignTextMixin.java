package ru.bloodhelper.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.OptimizerModule;

@Mixin(value={AbstractSignRenderer.class})
public abstract class OptimizerSignTextMixin {
    @Inject(method={"submitSignText"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$distantText(SignRenderState state, PoseStack pose, SubmitNodeCollector collector, SignText text, CallbackInfo ci) {
        if (OptimizerModule.hideSignText(state.blockPos)) {
            ci.cancel();
        }
    }
}

