/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.entity.state.EntityRenderState
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.extensions.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.extensions.FeatureModules;

@Mixin(value={EntityRenderer.class})
public abstract class NamesMixin {
    @Inject(method={"extractNameTags(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V"}, at={@At(value="TAIL")})
    private void vaileks$label(Entity entity, EntityRenderState state, float delta, CallbackInfo ci) {
        FeatureModules.BetterNames m = FeatureModules.names;
        Minecraft mc = Minecraft.getInstance();
        if (m == null || !m.isEnable() || !(entity instanceof Player) || entity == mc.player || entity.isInvisible()) {
            return;
        }
        float distance = ((Float)m.distance.getValue()).floatValue();
        if (state.distanceToCameraSq > (double)(distance * distance)) {
            state.nameTag = null;
            return;
        }
        state.nameTag = entity.getDisplayName();
        state.nameTagAttachment = new Vec3(0.0, (double)entity.getBbHeight() + 0.35, 0.0);
    }

    @Inject(method={"submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void vaileks$draw(EntityRenderState state, PoseStack pose, SubmitNodeCollector queue, CameraRenderState camera, int yOffset, CallbackInfo ci) {
        FeatureModules.BetterNames m = FeatureModules.names;
        if (m == null || !m.isEnable() || !(state instanceof AvatarRenderState) || state.nameTag == null || state.nameTagAttachment == null) {
            return;
        }
        float distance = ((Float)m.distance.getValue()).floatValue();
        if (state.distanceToCameraSq > (double)(distance * distance)) {
            ci.cancel();
            return;
        }
        Font font = Minecraft.getInstance().font;
        pose.pushPose();
        pose.translate(state.nameTagAttachment);
        pose.rotate((Quaternionfc)camera.orientation);
        float scale = 0.025f * ((Float)m.scale.getValue()).floatValue();
        pose.scale(scale, -scale, scale);
        float labelY = yOffset;
        if (state.scoreText != null) {
            queue.submitText(pose, -font.width(state.scoreText) * 0.5f, labelY, state.scoreText.getVisualOrderText(), true, m.throughWalls.isValue() ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0xF000F0, m.color.getColor(), m.background.isValue() ? -1727526644 : 0, 0);
            labelY -= font.lineHeight * 1.15f;
        }
        queue.submitText(pose, (float)(-font.width((FormattedText)state.nameTag)) * 0.5f, labelY, state.nameTag.getVisualOrderText(), true, m.throughWalls.isValue() ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0xF000F0, m.color.getColor(), m.background.isValue() ? -1727526644 : 0, 0);
        pose.popPose();
        ci.cancel();
    }
}

