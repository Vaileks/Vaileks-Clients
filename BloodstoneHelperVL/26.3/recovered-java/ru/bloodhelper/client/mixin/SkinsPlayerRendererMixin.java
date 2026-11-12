/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.Model
 *  net.minecraft.client.model.player.PlayerModel
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.layers.ItemInHandLayer
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.entity.state.EntityRenderState
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.texture.UvMapping
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.skins.SkinMesh;
import ru.bloodhelper.client.skins.SkinsRenderer;

@Mixin(value={LivingEntityRenderer.class})
abstract class SkinsPlayerRendererMixin {
    @Shadow
    protected EntityModel<?> model;

    SkinsPlayerRendererMixin() {
    }

    @WrapOperation(method={"submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V")})
    private void bloodhelper$skin(SubmitNodeCollector collector, Model<?> model, Object state, PoseStack poses, RenderType type, int light, int overlay, int tint, UvMapping sprite, int outline, Operation<Void> original) {
        SkinMesh mesh = SkinsRenderer.selected(state);
        if (mesh != null && model instanceof PlayerModel) {
            PlayerModel playerModel = (PlayerModel)model;
            SkinsRenderer.submit(mesh, (AvatarRenderState)state, playerModel, poses, collector, light, overlay, tint, outline);
        } else {
            original.call(new Object[]{collector, model, state, poses, type, light, overlay, tint, sprite, outline});
        }
    }

    @WrapOperation(method={"submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/layers/RenderLayer;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V")})
    private void bloodhelper$skinLayers(RenderLayer<?, ?> layer, PoseStack poses, SubmitNodeCollector collector, int light, EntityRenderState state, float yaw, float pitch, Operation<Void> original) {
        if (!(this.model instanceof PlayerModel) || SkinsRenderer.selected(state) == null || layer instanceof ItemInHandLayer && SkinsRenderer.heldItems(state)) {
            original.call(new Object[]{layer, poses, collector, light, state, Float.valueOf(yaw), Float.valueOf(pitch)});
        }
    }
}

