/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_10444
 *  net.minecraft.class_11659
 *  net.minecraft.class_12249
 *  net.minecraft.class_1268
 *  net.minecraft.class_1306
 *  net.minecraft.class_2960
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4608
 *  net.minecraft.class_7833
 *  org.joml.Quaternionfc
 */
package pyz1k.cosmetics;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.class_10055;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;

public final class ChickenMesh {
    private static final String[] PARTS = new String[]{"body", "head", "right_arm", "left_arm", "right_leg", "left_leg"};
    private static final class_2960[] TEXTURES = new class_2960[]{class_2960.method_60655((String)"pyz1k", (String)"models/chicken/rudy_sc_1.png"), class_2960.method_60655((String)"pyz1k", (String)"models/chicken/bruce_col.png")};
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

    public static void submit(class_10055 state, class_4587 pose, class_11659 collector, int light, int overlay) {
        float[][][] data = ChickenMesh.geometry();
        float stride = Math.min(state.field_53451, 1.0f);
        float swing = (float)Math.cos(state.field_53450 * 0.6662f) * 1.2f * stride;
        float rightArm = -swing;
        float leftArm = swing;
        if (state.field_63604 > 0.0f) {
            float hit = (float)Math.sin(Math.sqrt(state.field_63604) * Math.PI) * 1.3f;
            if (state.field_53408 == class_1306.field_6183) {
                rightArm -= hit;
            } else {
                leftArm -= hit;
            }
        }
        if (state.field_53414) {
            boolean rightHanded;
            boolean mainHand = state.field_53409 == class_1268.field_5808;
            boolean bl = rightHanded = state.field_55303 == class_1306.field_6183;
            if (mainHand == rightHanded) {
                rightArm = -0.8f;
            } else {
                leftArm = -0.8f;
            }
        }
        float[] angles = new float[]{0.0f, 0.0f, rightArm, leftArm, swing, -swing};
        for (int part = 0; part < PARTS.length; ++part) {
            pose.method_22903();
            ChickenMesh.rotatePart(pose, part, angles[part]);
            for (int material = 0; material < TEXTURES.length; ++material) {
                float[] triangles = data[part][material];
                if (triangles.length == 0) continue;
                collector.method_73483(pose, class_12249.method_75994((class_2960)TEXTURES[material]), (matrix, consumer) -> {
                    for (int triangle = 0; triangle < triangles.length; triangle += 24) {
                        ChickenMesh.vertex(matrix, consumer, triangles, triangle, light, overlay);
                        ChickenMesh.vertex(matrix, consumer, triangles, triangle + 8, light, overlay);
                        ChickenMesh.vertex(matrix, consumer, triangles, triangle + 16, light, overlay);
                        ChickenMesh.vertex(matrix, consumer, triangles, triangle + 16, light, overlay);
                    }
                });
            }
            pose.method_22909();
        }
        ChickenMesh.submitItem(state.field_55305, pose, collector, light, state.field_61821, 2, rightArm);
        ChickenMesh.submitItem(state.field_55307, pose, collector, light, state.field_61821, 3, leftArm);
    }

    public static void submitArm(class_4587 pose, class_11659 collector, int light, boolean right) {
        float[][][] data = ChickenMesh.geometry();
        int part = right ? 2 : 3;
        float[] pivot = PIVOTS[part];
        pose.method_22903();
        pose.method_46416(-pivot[0], -pivot[1], -pivot[2]);
        for (int material = 0; material < TEXTURES.length; ++material) {
            float[] triangles = data[part][material];
            if (triangles.length == 0) continue;
            collector.method_73483(pose, class_12249.method_75994((class_2960)TEXTURES[material]), (matrix, consumer) -> {
                for (int triangle = 0; triangle < triangles.length; triangle += 24) {
                    ChickenMesh.vertex(matrix, consumer, triangles, triangle, light, class_4608.field_21444);
                    ChickenMesh.vertex(matrix, consumer, triangles, triangle + 8, light, class_4608.field_21444);
                    ChickenMesh.vertex(matrix, consumer, triangles, triangle + 16, light, class_4608.field_21444);
                    ChickenMesh.vertex(matrix, consumer, triangles, triangle + 16, light, class_4608.field_21444);
                }
            });
        }
        pose.method_22909();
    }

    private static void rotatePart(class_4587 pose, int part, float angle) {
        if (angle == 0.0f) {
            return;
        }
        float[] pivot = PIVOTS[part];
        pose.method_46416(pivot[0], pivot[1], pivot[2]);
        pose.method_22907((Quaternionfc)class_7833.field_40714.rotation(angle));
        pose.method_46416(-pivot[0], -pivot[1], -pivot[2]);
    }

    private static void submitItem(class_10444 item, class_4587 pose, class_11659 collector, int light, int outlineColor, int arm, float angle) {
        if (item.method_65606()) {
            return;
        }
        pose.method_22903();
        ChickenMesh.rotatePart(pose, arm, angle);
        pose.method_46416(arm == 2 ? -0.57f : 0.57f, 0.92f, -0.02f);
        pose.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(-90.0f));
        pose.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(180.0f));
        pose.method_46416(arm == 2 ? 0.0625f : -0.0625f, 0.125f, 0.0f);
        item.method_65604(pose, collector, light, class_4608.field_21444, outlineColor);
        pose.method_22909();
    }

    private static void vertex(class_4587.class_4665 pose, class_4588 consumer, float[] data, int i, int light, int overlay) {
        consumer.method_56824(pose, data[i], data[i + 1], data[i + 2]).method_39415(-1).method_22913(data[i + 3], data[i + 4]).method_22922(overlay).method_60803(light).method_60831(pose, data[i + 5], data[i + 6], data[i + 7]);
    }
}

