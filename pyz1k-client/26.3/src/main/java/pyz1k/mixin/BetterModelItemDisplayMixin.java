package pyz1k.mixin;

import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.state.ItemDisplayEntityRenderState;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.killeffects.ServerKillEffectFilter;

@Mixin(value={DisplayRenderer.ItemDisplayRenderer.class})
public abstract class BetterModelItemDisplayMixin {
    @Inject(method={"extractRenderState(Lnet/minecraft/world/entity/Display$ItemDisplay;Lnet/minecraft/client/renderer/entity/state/ItemDisplayEntityRenderState;F)V"}, at={@At(value="TAIL")})
    private void pyz1k$hideServerKillEffects(Display.ItemDisplay entity, ItemDisplayEntityRenderState state, float partialTick, CallbackInfo ci) {
        if (ServerKillEffectFilter.shouldHide(entity)) {
            state.item.clear();
        }
    }
}

