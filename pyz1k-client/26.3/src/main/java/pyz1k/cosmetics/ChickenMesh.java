/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.item.ItemStackRenderState
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  org.joml.Quaternionfc
 */
package pyz1k.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionfc;

public final class ChickenMesh {
    private static final String[] PARTS = new String[]{"body", "head", "right_arm", "left_arm", "right_leg", "left_leg"};
    private static final Identifier[] TEXTURES = new Identifier[]{Identifier.fromNamespaceAndPath((String)"pyz1k", (String)"models/chicken/rudy_sc_1.png"), Identifier.fromNamespaceAndPath((String)"pyz1k", (String)"models/chicken/bruce_col.png")};
    private static final float[][] PIVOTS = new float[][]{{0.0f, 0.0f, 0.0f}, {0.0f, 0.55f, 0.0f}, {-0.52f, 0.55f, 0.0f}, {0.52f, 0.55f, 0.0f}, {-0.18f, 1.12f, 0.0f}, {0.18f, 1.12f, 0.0f}};
    private static float[][][] vertices;

    private ChickenMesh() {
    }

    public static boolean isAvailable() {
        float[][][] data = ChickenMesh.geometry();
        return data[0][0].length > 0 && data[1][0].length > 0 && data[2][0].length > 0 && data[3][0].length > 0 && data[4][1].length > 0 && data[5][1].length > 0;
    }

    private static float[][][] geometry() {
        if (vertices == null) {
            float[][][] all = new float[PARTS.length][TEXTURES.length][];
            for (int part = 0; part < PARTS.length; ++part) {
                for (int material = 0; material < TEXTURES.length; ++material) {
                    all[part][material] = ChickenMesh.load(PARTS[part], material);
                }
            }
            vertices = all;
        }
        return vertices;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static float[] load(String part, int material) {
        String path = "/assets/pyz1k/models/chicken/mesh_" + part + "_" + material + ".bin";
        try (InputStream raw = ChickenMesh.class.getResourceAsStream(path);){
            float[] fArray;
            if (raw == null) {
                float[] fArray2 = new float[]{};
                return fArray2;
            }
            try (DataInputStream in = new DataInputStream(raw);){
                int count = in.readInt();
                if (count < 0 || count > 1000000 || count % 3 != 0) {
                    throw new IOException("Invalid vertex count");
                }
                float[] data = new float[count * 8];
                for (int i = 0; i < data.length; ++i) {
                    data[i] = in.readFloat();
                }
                fArray = data;
            }
            return fArray;
        }
        catch (IOException | RuntimeException exception) {
            System.err.println("Pyz1k: failed to load " + path + ": " + String.valueOf(exception));
            return new float[0];
        }
    }

    public static void submit(AvatarRenderState state, PoseStack pose, SubmitNodeCollector collector, int light, int overlay) {
        float[][][] data = ChickenMesh.geometry();
        float stride = Math.min(state.walkAnimationSpeed, 1.0f);
        float swing = (float)Math.cos(state.walkAnimationPos * 0.6662f) * 1.2f * stride;
        float rightArm = -swing;
        float leftArm = swing;
        if (((AvatarAttackState)(Object)state).pyz1k$attackTime() > 0.0f) {
            float hit = (float)Math.sin(Math.sqrt(((AvatarAttackState)(Object)state).pyz1k$attackTime()) * Math.PI) * 1.3f;
            if (((AvatarAttackState)(Object)state).pyz1k$attackArm() == HumanoidArm.RIGHT) {
                rightArm -= hit;
            } else {
                leftArm -= hit;
            }
        }
        if (state.isUsingItem) {
            boolean rightHanded;
            boolean mainHand = state.useItemHand == InteractionHand.MAIN_HAND;
            boolean bl = rightHanded = state.mainArm == HumanoidArm.RIGHT;
            if (mainHand == rightHanded) {
                rightArm = -0.8f;
            } else {
                leftArm = -0.8f;
            }
        }
        float[] angles = new float[]{0.0f, 0.0f, rightArm, leftArm, swing, -swing};
        for (int part = 0; part < PARTS.length; ++part) {
            pose.pushPose();
            ChickenMesh.rotatePart(pose, part, angles[part]);
            for (int material = 0; material < TEXTURES.length; ++material) {
                float[] triangles = data[part][material];
                if (triangles.length == 0) continue;
                collector.submitCustomGeometry(pose, RenderTypes.entityCutout((Identifier)TEXTURES[material]), (matrix, consumer) -> {
                    for (int triangle = 0; triangle < triangles.length; triangle += 24) {
                        ChickenMesh.vertex(matrix, consumer, triangles, triangle, light, overlay);
                        ChickenMesh.vertex(matrix, consumer, triangles, triangle + 8, light, overlay);
                        ChickenMesh.vertex(matrix, consumer, triangles, triangle + 16, light, overlay);
                        ChickenMesh.vertex(matrix, consumer, triangles, triangle + 16, light, overlay);
                    }
                });
            }
            pose.popPose();
        }
        ChickenMesh.submitItem(state.rightHandItemState, pose, collector, light, state.outlineColor, 2, rightArm);
        ChickenMesh.submitItem(state.leftHandItemState, pose, collector, light, state.outlineColor, 3, leftArm);
    }

    public static void submitArm(PoseStack pose, SubmitNodeCollector collector, int light, boolean right) {
        float[][][] data = ChickenMesh.geometry();
        int part = right ? 2 : 3;
        float[] pivot = PIVOTS[part];
        pose.pushPose();
        pose.translate(-pivot[0], -pivot[1], -pivot[2]);
        for (int material = 0; material < TEXTURES.length; ++material) {
            float[] triangles = data[part][material];
            if (triangles.length == 0) continue;
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout((Identifier)TEXTURES[material]), (matrix, consumer) -> {
                for (int triangle = 0; triangle < triangles.length; triangle += 24) {
                    ChickenMesh.vertex(matrix, consumer, triangles, triangle, light, OverlayTexture.NO_OVERLAY);
                    ChickenMesh.vertex(matrix, consumer, triangles, triangle + 8, light, OverlayTexture.NO_OVERLAY);
                    ChickenMesh.vertex(matrix, consumer, triangles, triangle + 16, light, OverlayTexture.NO_OVERLAY);
                    ChickenMesh.vertex(matrix, consumer, triangles, triangle + 16, light, OverlayTexture.NO_OVERLAY);
                }
            });
        }
        pose.popPose();
    }

    private static void rotatePart(PoseStack pose, int part, float angle) {
        if (angle == 0.0f) {
            return;
        }
        float[] pivot = PIVOTS[part];
        pose.translate(pivot[0], pivot[1], pivot[2]);
        pose.rotate((Quaternionfc)Axis.XP.rotation(angle));
        pose.translate(-pivot[0], -pivot[1], -pivot[2]);
    }

    private static void submitItem(ItemStackRenderState item, PoseStack pose, SubmitNodeCollector collector, int light, int outlineColor, int arm, float angle) {
        if (item.isEmpty()) {
            return;
        }
        pose.pushPose();
        ChickenMesh.rotatePart(pose, arm, angle);
        pose.translate(arm == 2 ? -0.57f : 0.57f, 0.92f, -0.02f);
        pose.rotate((Quaternionfc)Axis.XP.rotationDegrees(-90.0f));
        pose.rotate((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
        pose.translate(arm == 2 ? 0.0625f : -0.0625f, 0.125f, 0.0f);
        item.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, outlineColor);
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, float[] data, int i, int light, int overlay) {
        consumer.addVertex(pose, data[i], data[i + 1], data[i + 2]).setColor(-1).setUv(data[i + 3], data[i + 4]).setOverlay(overlay).setLight(light).setNormal(pose, data[i + 5], data[i + 6], data[i + 7]);
    }
}

