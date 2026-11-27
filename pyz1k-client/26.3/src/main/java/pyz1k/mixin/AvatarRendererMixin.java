/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.player.AvatarRenderer
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Avatar
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package pyz1k.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pyz1k.cosmetics.ChickenMesh;
import pyz1k.cosmetics.CosmeticsManager;
import pyz1k.cosmetics.KirbyMesh;

@Mixin(value={AvatarRenderer.class})
public abstract class AvatarRendererMixin {
    @Inject(method={"extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V"}, at={@At(value="TAIL")})
    private void pyz1k$applyLocalCosmeticHat(Avatar entity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        ((pyz1k.cosmetics.AvatarAttackState)(Object)state).pyz1k$setAttack(entity.getSwingAnimation(partialTick), entity.getCurrentSwing() != null && entity.getCurrentSwing().hand() == net.minecraft.world.InteractionHand.OFF_HAND ? entity.getMainArm().getOpposite() : entity.getMainArm());
        boolean hasHelmet;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || entity != minecraft.player) {
            return;
        }
        CosmeticsManager cosmetics = CosmeticsManager.getInstance();
        if (cosmetics.isCustomModelAvailable()) {
            return;
        }
        ItemStack cosmetic = cosmetics.getSelectedStack();
        if (cosmetic.isEmpty()) {
            return;
        }
        boolean bl = hasHelmet = !state.headEquipment.isEmpty();
        if (cosmetics.isOnlyWithHelmet() && !hasHelmet) {
            return;
        }
        state.wornHeadType = null;
        state.wornHeadProfile = null;
        minecraft.getItemModelResolver().updateForLiving(state.headItem, cosmetic, ItemDisplayContext.HEAD, (LivingEntity)entity);
    }

    @Inject(method={"shouldRenderLayers(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Z"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void pyz1k$hideLayersForChicken(AvatarRenderState state, CallbackInfoReturnable<Boolean> result) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && state.id == minecraft.player.getId() && CosmeticsManager.getInstance().isCustomModelAvailable()) {
            result.setReturnValue(false);
        }
    }

    @Inject(method={"renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void pyz1k$chickenRightHand(PoseStack pose, SubmitNodeCollector collector, int light, Identifier skin, boolean sleeve, CallbackInfo ci) {
        if (CosmeticsManager.getInstance().isKirbySelected() && KirbyMesh.isAvailable()) {
            KirbyMesh.submitArm(pose, collector, light, true);
            ci.cancel();
        } else if (CosmeticsManager.getInstance().isChickenSelected() && ChickenMesh.isAvailable()) {
            ChickenMesh.submitArm(pose, collector, light, true);
            ci.cancel();
        }
    }

    @Inject(method={"renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"}, at={@At(value="HEAD")}, cancellable=true, require=1)
    private void pyz1k$chickenLeftHand(PoseStack pose, SubmitNodeCollector collector, int light, Identifier skin, boolean sleeve, CallbackInfo ci) {
        if (CosmeticsManager.getInstance().isKirbySelected() && KirbyMesh.isAvailable()) {
            KirbyMesh.submitArm(pose, collector, light, false);
            ci.cancel();
        } else if (CosmeticsManager.getInstance().isChickenSelected() && ChickenMesh.isAvailable()) {
            ChickenMesh.submitArm(pose, collector, light, false);
            ci.cancel();
        }
    }
}

