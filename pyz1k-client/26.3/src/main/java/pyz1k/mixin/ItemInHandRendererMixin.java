package pyz1k.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.handrender.HandRenderManager;
import pyz1k.handrender.HandRenderRule;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method="submitArmWithItem", at=@At("HEAD"))
    private void pyz1k$pushHandTransform(PlayerRenderState player, FirstPersonHandsAndItemsRenderState hands, float partialTick, float pitch, InteractionHand hand, float swing, ItemStack stack, float height, PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        pose.pushPose();
        HandRenderRule rule = HandRenderManager.getInstance().findMatching(stack);
        if (rule != null) {
            float scale = HandRenderManager.clampScale(rule.handScale);
            pose.translate(HandRenderManager.clampOffset(rule.offsetX), HandRenderManager.clampOffset(rule.offsetY), HandRenderManager.clampOffset(rule.offsetZ));
            pose.scale(scale, scale, scale);
        }
    }
    @Inject(method="submitArmWithItem", at=@At("RETURN"))
    private void pyz1k$popHandTransform(PlayerRenderState player, FirstPersonHandsAndItemsRenderState hands, float partialTick, float pitch, InteractionHand hand, float swing, ItemStack stack, float height, PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci) { pose.popPose(); }
}
