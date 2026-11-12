/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.Model
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.feature.ModelFeatureRenderer$CrumblingOverlay
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package pyz1k.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pyz1k.cosmetics.ChickenMesh;
import pyz1k.cosmetics.CosmeticsManager;
import pyz1k.cosmetics.KirbyMesh;

@Mixin(value={LivingEntityRenderer.class})
public abstract class ChickenPlayerRenderMixin {
    @Redirect(method={"submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"), require=1)
    private void pyz1k$renderChicken(SubmitNodeCollector collector, Model model, Object state, PoseStack pose, RenderType renderType, int light, int overlay, int color, UvMapping sprite, int outlineColor) {
        Minecraft minecraft = Minecraft.getInstance();
        if (state instanceof AvatarRenderState) {
            AvatarRenderState avatar = (AvatarRenderState)state;
            if (minecraft.player != null && avatar.id == minecraft.player.getId()) {
                CosmeticsManager cosmetics = CosmeticsManager.getInstance();
                if (cosmetics.isKirbySelected() && KirbyMesh.isAvailable()) {
                    KirbyMesh.submit(avatar, pose, collector, light, overlay);
                    return;
                }
                if (cosmetics.isChickenSelected() && ChickenMesh.isAvailable()) {
                    ChickenMesh.submit(avatar, pose, collector, light, overlay);
                    return;
                }
            }
        }
        collector.submitModel(model, state, pose, renderType, light, overlay, color, sprite, outlineColor);
    }
}

