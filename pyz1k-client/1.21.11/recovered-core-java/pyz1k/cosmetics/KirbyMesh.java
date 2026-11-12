/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_10444
 *  net.minecraft.class_11659
 *  net.minecraft.class_12249
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
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;

public final class KirbyMesh {
    private static final class_2960 TEXTURE = class_2960.method_60655((String)"pyz1k", (String)"models/kirby/kirby.png");
    private static final int IDLE = 0;
    private static final int RUN = 1;
    private static final int WALK = 2;
    private static final int RIGHT_HAND = 3;
    private static final int LEFT_HAND = 4;
    private static Model model;

    private KirbyMesh() {
    }

    public static boolean isAvailable() {
        return KirbyMesh.geometry().vertices.length > 0;
    }

    private static synchronized Model geometry() {
        if (model != null) {
            return model;
        }
        try (InputStream raw = KirbyMesh.class.getResourceAsStream("/assets/pyz1k/models/kirby/kirby_mesh.bin");){
            if (raw == null) {
                throw new IOException("Missing Kirby geometry");
            }
            try (DataInputStream in = new DataInputStream(raw);){
                if (in.readInt() != 1263682137) {
                    throw new IOException("Invalid Kirby geometry");
                }
                int vertexCount = in.readInt();
                int groupCount = in.readInt();
                if (vertexCount < 1 || vertexCount > 100000 || groupCount != 5) {
                    throw new IOException("Invalid Kirby mesh size");
                }
                float[] vertices = new float[vertexCount * 8];
                byte[] joints = new byte[vertexCount * 4];
                float[] weights = new float[vertexCount * 4];
                for (int v = 0; v < vertexCount; ++v) {
                    int k;
                    for (k = 0; k < 8; ++k) {
                        vertices[v * 8 + k] = in.readFloat();
                    }
                    in.readFully(joints, v * 4, 4);
                    for (k = 0; k < 4; ++k) {
                        weights[v * 4 + k] = in.readFloat();
                    }
                }
                int[][] indices = new int[groupCount][];
                for (int group = 0; group < groupCount; ++group) {
                    int count = in.readInt();
                    if (count < 0 || count > 150000 || count % 3 != 0) {
                        throw new IOException("Invalid Kirby index count");
                    }
                    indices[group] = new int[count];
                    for (int k = 0; k < count; ++k) {
                        indices[group][k] = in.readInt();
                        if (indices[group][k] >= 0 && indices[group][k] < vertexCount) continue;
                        throw new IOException("Invalid Kirby index");
                    }
                }
                int jointCount = in.readInt();
                int clipCount = in.readInt();
                if (jointCount != 18 || clipCount != 3) {
                    throw new IOException("Invalid Kirby animation data");
                }
                float[] handAnchors = new float[6];
                for (int k = 0; k < handAnchors.length; ++k) {
                    handAnchors[k] = in.readFloat();
                }
                Clip[] clips = new Clip[clipCount];
                for (int c = 0; c < clipCount; ++c) {
                    float[][] frames;
                    float duration = in.readFloat();
                    int frameCount = in.readInt();
                    if (duration <= 0.0f || frameCount < 1 || frameCount > 300) {
                        throw new IOException("Invalid Kirby animation length");
                    }
                    for (float[] frame : frames = new float[frameCount][jointCount * 12]) {
                        for (int k = 0; k < frame.length; ++k) {
                            frame[k] = in.readFloat();
                        }
                    }
                    clips[c] = new Clip(duration, frames);
                }
                model = new Model(vertices, joints, weights, indices, clips, handAnchors);
            }
        }
        catch (IOException exception) {
            System.err.println("Pyz1k: failed to load Kirby: " + String.valueOf(exception));
            model = new Model(new float[0], new byte[0], new float[0], new int[0][], new Clip[0], new float[0]);
        }
        return model;
    }

    public static void submit(class_10055 state, class_4587 pose, class_11659 collector, int light, int overlay) {
        Model data = KirbyMesh.geometry();
        if (data.vertices.length == 0) {
            return;
        }
        float speed = state.field_53451;
        int animation = speed < 0.06f ? 0 : (speed > 0.75f ? 1 : 2);
        float time = animation == 0 ? (float)(System.currentTimeMillis() % 2000L) / 1000.0f : state.field_53450 * 0.1f;
        float[] transforms = KirbyMesh.sample(data.clips[animation], time);
        float[] skinned = KirbyMesh.skin(data, transforms);
        collector.method_73483(pose, class_12249.method_75994((class_2960)TEXTURE), (matrix, consumer) -> {
            for (int group = 0; group < 3; ++group) {
                int[] indices = data.indices[group];
                for (int triangle = 0; triangle < indices.length; triangle += 3) {
                    KirbyMesh.vertex(matrix, consumer, data.vertices, skinned, indices[triangle], light, overlay);
                    KirbyMesh.vertex(matrix, consumer, data.vertices, skinned, indices[triangle + 1], light, overlay);
                    KirbyMesh.vertex(matrix, consumer, data.vertices, skinned, indices[triangle + 2], light, overlay);
                    KirbyMesh.vertex(matrix, consumer, data.vertices, skinned, indices[triangle + 2], light, overlay);
                }
            }
        });
        KirbyMesh.submitItem(state.field_55305, pose, collector, light, state.field_61821, data.handAnchors, transforms, true);
        KirbyMesh.submitItem(state.field_55307, pose, collector, light, state.field_61821, data.handAnchors, transforms, false);
    }

    public static void submitArm(class_4587 pose, class_11659 collector, int light, boolean right) {
        Model data = KirbyMesh.geometry();
        if (data.vertices.length == 0) {
            return;
        }
        float[] transforms = KirbyMesh.sample(data.clips[0], 0.0f);
        float[] skinned = KirbyMesh.skin(data, transforms);
        float[] point = KirbyMesh.handPoint(data.handAnchors, transforms, right);
        pose.method_22903();
        pose.method_46416(-point[0], -point[1], -point[2]);
        collector.method_73483(pose, class_12249.method_75994((class_2960)TEXTURE), (matrix, consumer) -> {
            int[] indices = data.indices[right ? 3 : 4];
            for (int triangle = 0; triangle < indices.length; triangle += 3) {
                KirbyMesh.vertex(matrix, consumer, data.vertices, skinned, indices[triangle], light, class_4608.field_21444);
                KirbyMesh.vertex(matrix, consumer, data.vertices, skinned, indices[triangle + 1], light, class_4608.field_21444);
                KirbyMesh.vertex(matrix, consumer, data.vertices, skinned, indices[triangle + 2], light, class_4608.field_21444);
                KirbyMesh.vertex(matrix, consumer, data.vertices, skinned, indices[triangle + 2], light, class_4608.field_21444);
            }
        });
        pose.method_22909();
    }

    private static float[] sample(Clip clip, float time) {
        float position = (time % clip.duration + clip.duration) % clip.duration * (float)clip.frames.length / clip.duration;
        int first = (int)position % clip.frames.length;
        int second = (first + 1) % clip.frames.length;
        float factor = position - (float)((int)position);
        float[] result = new float[clip.frames[first].length];
        for (int k = 0; k < result.length; ++k) {
            result[k] = clip.frames[first][k] + (clip.frames[second][k] - clip.frames[first][k]) * factor;
        }
        return result;
    }

    private static float[] skin(Model data, float[] transforms) {
        int count = data.vertices.length / 8;
        float[] result = new float[count * 6];
        for (int i = 0; i < count; ++i) {
            int start = i * 8;
            float x = data.vertices[start];
            float y = data.vertices[start + 1];
            float z = data.vertices[start + 2];
            float nx = data.vertices[start + 3];
            float ny = data.vertices[start + 4];
            float nz = data.vertices[start + 5];
            float px = 0.0f;
            float py = 0.0f;
            float pz = 0.0f;
            float vx = 0.0f;
            float vy = 0.0f;
            float vz = 0.0f;
            for (int slot = 0; slot < 4; ++slot) {
                float weight = data.weights[i * 4 + slot];
                if (weight <= 0.0f) continue;
                int m = (data.joints[i * 4 + slot] & 0xFF) * 12;
                px += weight * (transforms[m] * x + transforms[m + 1] * y + transforms[m + 2] * z + transforms[m + 3]);
                py += weight * (transforms[m + 4] * x + transforms[m + 5] * y + transforms[m + 6] * z + transforms[m + 7]);
                pz += weight * (transforms[m + 8] * x + transforms[m + 9] * y + transforms[m + 10] * z + transforms[m + 11]);
                vx += weight * (transforms[m] * nx + transforms[m + 1] * ny + transforms[m + 2] * nz);
                vy += weight * (transforms[m + 4] * nx + transforms[m + 5] * ny + transforms[m + 6] * nz);
                vz += weight * (transforms[m + 8] * nx + transforms[m + 9] * ny + transforms[m + 10] * nz);
            }
            int dst = i * 6;
            result[dst] = px;
            result[dst + 1] = py;
            result[dst + 2] = pz;
            float inverseLength = (float)(1.0 / Math.sqrt(vx * vx + vy * vy + vz * vz + 1.0E-12f));
            result[dst + 3] = vx * inverseLength;
            result[dst + 4] = vy * inverseLength;
            result[dst + 5] = vz * inverseLength;
        }
        return result;
    }

    private static float[] handPoint(float[] anchors, float[] transforms, boolean right) {
        int start = right ? 0 : 3;
        int matrix = (right ? 5 : 8) * 12;
        float x = anchors[start];
        float y = anchors[start + 1];
        float z = anchors[start + 2];
        return new float[]{transforms[matrix] * x + transforms[matrix + 1] * y + transforms[matrix + 2] * z + transforms[matrix + 3], transforms[matrix + 4] * x + transforms[matrix + 5] * y + transforms[matrix + 6] * z + transforms[matrix + 7], transforms[matrix + 8] * x + transforms[matrix + 9] * y + transforms[matrix + 10] * z + transforms[matrix + 11]};
    }

    private static void submitItem(class_10444 item, class_4587 pose, class_11659 collector, int light, int outlineColor, float[] anchors, float[] transforms, boolean right) {
        if (item.method_65606()) {
            return;
        }
        float[] point = KirbyMesh.handPoint(anchors, transforms, right);
        pose.method_22903();
        pose.method_46416(point[0] + (right ? -0.15f : 0.15f), point[1], point[2] - 0.06f);
        pose.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(-90.0f));
        pose.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(180.0f));
        item.method_65604(pose, collector, light, class_4608.field_21444, outlineColor);
        pose.method_22909();
    }

    private static void vertex(class_4587.class_4665 pose, class_4588 consumer, float[] source, float[] skinned, int index, int light, int overlay) {
        int p = index * 6;
        int uv = index * 8;
        consumer.method_56824(pose, skinned[p], skinned[p + 1], skinned[p + 2]).method_39415(-1).method_22913(source[uv + 6], source[uv + 7]).method_22922(overlay).method_60803(light).method_60831(pose, skinned[p + 3], skinned[p + 4], skinned[p + 5]);
    }

    private record Model(float[] vertices, byte[] joints, float[] weights, int[][] indices, Clip[] clips, float[] handAnchors) {
    }

    private record Clip(float duration, float[][] frames) {
    }
}

