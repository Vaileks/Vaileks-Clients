/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.ItemEntityRenderer
 *  net.minecraft.client.renderer.entity.state.ItemEntityRenderState
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  net.minecraft.world.entity.item.ItemEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.Pyz1k;
import pyz1k.itemrender.ItemRenderManager;
import pyz1k.itemrender.ItemRenderState;

@Mixin(value={ItemEntityRenderer.class})
public class ItemEntityRendererMixin {
    @Inject(method={"extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V"}, at={@At(value="TAIL")})
    private void pyz1k$captureGroundScale(ItemEntity entity, ItemEntityRenderState state, float partialTick, CallbackInfo ci) {
        ItemRenderManager manager;
        Pyz1k pyz1k = Pyz1k.getInstance();
        float scale = 1.0f;
        if (pyz1k != null && (manager = ItemRenderManager.getInstance()) != null) {
            scale = manager.getScale(entity);
        }
        ((ItemRenderState)state).pyz1k$setGroundScale(scale);
    }

    @Inject(method={"submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;submitMultipleFromCount(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/phys/AABB;)V", shift=At.Shift.BEFORE)})
    private void pyz1k$scaleDroppedItem(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        float scale = ((ItemRenderState)state).pyz1k$getGroundScale();
        if (Math.abs(scale - 1.0f) > 0.001f) {
            poseStack.scale(scale, scale, scale);
        }
    }
}

