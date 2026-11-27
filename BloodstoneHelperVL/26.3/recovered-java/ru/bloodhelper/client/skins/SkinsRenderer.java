/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.player.PlayerModel
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package ru.bloodhelper.client.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.SkinsSettings;
import ru.bloodhelper.client.skins.SharedSkins;
import ru.bloodhelper.client.skins.SkinMesh;
import ru.bloodhelper.client.skins.VeritiAnimation;

public final class SkinsRenderer {
    private static final Map<String, Identifier> TEXTURES = new ConcurrentHashMap<String, Identifier>();

    private static Identifier texture(SkinMesh mesh, SkinMesh.Surface surface) {
        return TEXTURES.computeIfAbsent(mesh.textures()[surface.material()], path -> Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)path));
    }

    private SkinsRenderer() {
    }

    public static SkinMesh selected(Object value) {
        if (!(value instanceof AvatarRenderState)) {
            return null;
        }
        AvatarRenderState state = (AvatarRenderState)value;
        Minecraft mc = Minecraft.getInstance();
        SkinsSettings settings = BloodHelperClient.config().skins;
        if (mc.player == null || state.isSpectator || state.isInvisible) {
            return null;
        }
        if (state.id == mc.player.getId()) {
            return settings.enabled && !mc.options.getCameraType().isFirstPerson() ? SkinMesh.get(settings.character) : null;
        }
        if (!settings.showOthers || mc.level == null) {
            return null;
        }
        Entity entity = mc.level.getEntity(state.id);
        if (!(entity instanceof Player)) {
            return null;
        }
        Player player = (Player)entity;
        String model = SharedSkins.model(player.getUUID(), player.getGameProfile().name());
        return model.isEmpty() ? null : SkinMesh.get(model);
    }

    public static boolean heldItems(Object state) {
        return BloodHelperClient.config().skins.heldItems && SkinsRenderer.selected(state) == SkinMesh.get("RUDY");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void submit(SkinMesh mesh, AvatarRenderState state, PlayerModel model, PoseStack poses, SubmitNodeCollector collector, int light, int overlay, int tint, int outline) {
        model.setupAnim(state);
        boolean veriti = mesh == SkinMesh.get("VERITI");
        VeritiAnimation.Pose motion = veriti ? VeritiAnimation.capture(state.ageInTicks) : null;
        for (int index = 0; index < mesh.bones().length; ++index) {
            SkinMesh.Bone bone;
            if (veriti && index != (motion.mouthOpen() ? 1 : 0) || (bone = mesh.bones()[index]).surfaces().length == 0) continue;
            poses.pushPose();
            try {
                if (veriti) {
                    poses.translate(bone.x(), bone.y() + motion.y(), bone.z());
                    poses.scale(motion.scaleX(), motion.scaleY(), motion.scaleZ());
                    poses.rotate(Axis.XP, motion.rotation());
                    poses.translate(-bone.x(), -bone.y(), -bone.z());
                } else {
                    SkinsRenderer.animate(poses, bone, index, model);
                }
                for (SkinMesh.Surface surface : bone.surfaces()) {
                    Identifier texture = SkinsRenderer.texture(mesh, surface);
                    collector.submitCustomGeometry(poses, RenderTypes.entityCutout((Identifier)texture), (pose, vertices) -> SkinsRenderer.draw(surface, pose, vertices, light, overlay, tint));
                    if (outline == 0) continue;
                    collector.submitCustomGeometry(poses, RenderTypes.outline((Identifier)texture), (pose, vertices) -> SkinsRenderer.draw(surface, pose, vertices, light, overlay, outline));
                }
                continue;
            }
            finally {
                poses.popPose();
            }
        }
    }

    private static ModelPart part(PlayerModel model, int index) {
        return switch (index) {
            case 0 -> model.body;
            case 1 -> model.head;
            case 2 -> model.leftArm;
            case 3 -> model.rightArm;
            case 4 -> model.leftLeg;
            default -> model.rightLeg;
        };
    }

    private static void animate(PoseStack poses, SkinMesh.Bone bone, int index, PlayerModel model) {
        float restX;
        ModelPart part = SkinsRenderer.part(model, index);
        float f = index == 2 ? 5.0f : (index == 3 ? -5.0f : (index == 4 ? 1.9f : (restX = index == 5 ? -1.9f : 0.0f)));
        float restY = index == 2 || index == 3 ? 2.0f : (index >= 4 ? 12.0f : 0.0f);
        poses.translate((part.x - restX) / 16.0f, (part.y - restY) / 16.0f, part.z / 16.0f);
        poses.translate(bone.x(), bone.y(), bone.z());
        if (part.zRot != 0.0f) {
            poses.rotate(Axis.ZP, part.zRot);
        }
        if (part.yRot != 0.0f) {
            poses.rotate(Axis.YP, part.yRot);
        }
        if (part.xRot != 0.0f) {
            poses.rotate(Axis.XP, part.xRot);
        }
        poses.translate(-bone.x(), -bone.y(), -bone.z());
    }

    private static void draw(SkinMesh.Surface surface, PoseStack.Pose pose, VertexConsumer target, int light, int overlay, int tint) {
        float[] v = surface.vertices();
        for (int p = 0; p < v.length; p += 8) {
            target.addVertex(pose, v[p], v[p + 1], v[p + 2]).setColor(tint).setUv(v[p + 6], v[p + 7]).setOverlay(overlay).setLight(light).setNormal(pose, v[p + 3], v[p + 4], v[p + 5]);
        }
    }
}

