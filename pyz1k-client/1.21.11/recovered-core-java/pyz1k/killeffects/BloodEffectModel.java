/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
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
package pyz1k.killeffects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;

public final class BloodEffectModel {
    private final List<Object> roots;
    private final class_2960[] textures;
    private final float[][] textureSizes;
    private final float duration;

    private BloodEffectModel(List<Object> roots, class_2960[] textures, float[][] textureSizes, float duration) {
        this.roots = roots;
        this.textures = textures;
        this.textureSizes = textureSizes;
        this.duration = duration;
    }

    public float duration() {
        return this.duration;
    }

    public static BloodEffectModel load(String name, float fallbackDuration) {
        BloodEffectModel bloodEffectModel;
        block12: {
            String path = "/assets/pyz1k/killeffects/" + name + ".bbmodel";
            InputStream stream = BloodEffectModel.class.getResourceAsStream(path);
            try {
                JsonObject value;
                if (stream == null) {
                    throw new IllegalStateException("Missing " + path);
                }
                JsonObject data = JsonParser.parseReader((Reader)new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                JsonObject animation = data.getAsJsonArray("animations").get(0).getAsJsonObject();
                float duration = BloodEffectModel.number(animation, "length", fallbackDuration);
                JsonObject animators = animation.getAsJsonObject("animators");
                HashMap<String, Bone> bones = new HashMap<String, Bone>();
                HashMap<String, Cube> cubes = new HashMap<String, Cube>();
                JsonObject resolution = data.getAsJsonObject("resolution");
                float textureWidth = BloodEffectModel.number(resolution, "width", 128.0f);
                float textureHeight = BloodEffectModel.number(resolution, "height", 128.0f);
                JsonArray sources = data.getAsJsonArray("textures");
                int count = sources == null ? 1 : Math.max(1, sources.size());
                class_2960[] textures = new class_2960[count];
                float[][] sizes = new float[count][2];
                for (int i = 0; i < count; ++i) {
                    textures[i] = class_2960.method_60655((String)"pyz1k", (String)("killeffects/" + name + (String)(count == 1 ? "" : "_" + i) + ".png"));
                    JsonObject source = sources == null ? resolution : sources.get(i).getAsJsonObject();
                    sizes[i] = new float[]{BloodEffectModel.number(source, "uv_width", textureWidth), BloodEffectModel.number(source, "uv_height", textureHeight)};
                }
                for (JsonElement entry : data.getAsJsonArray("groups")) {
                    value = entry.getAsJsonObject();
                    String id = value.get("uuid").getAsString();
                    bones.put(id, new Bone(BloodEffectModel.vec(value.getAsJsonArray("origin"), 0.0f), BloodEffectModel.vec(value.getAsJsonArray("rotation"), 0.0f), BloodEffectModel.tracks(animators == null ? null : animators.getAsJsonObject(id))));
                }
                for (JsonElement entry : data.getAsJsonArray("elements")) {
                    value = entry.getAsJsonObject();
                    if (!"cube".equals(value.get("type").getAsString()) || count == 1 && !"cube".equals(value.get("name").getAsString())) continue;
                    Cube cube = new Cube(value, sizes);
                    if (cube.textureIndex < 0) continue;
                    cubes.put(value.get("uuid").getAsString(), cube);
                }
                List<Object> roots = BloodEffectModel.resolve(data.getAsJsonArray("outliner"), bones, cubes);
                bloodEffectModel = new BloodEffectModel(roots, textures, sizes, duration);
                if (stream == null) break block12;
            }
            catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception exception) {
                    System.err.println("Pyz1k: cannot load kill effect " + name + ": " + String.valueOf(exception));
                    return new BloodEffectModel(List.of(), new class_2960[0], new float[0][], fallbackDuration);
                }
            }
            stream.close();
        }
        return bloodEffectModel;
    }

    public void submit(class_4587 pose, class_11659 collector, float time, float scale, class_2960 victimSkin) {
        pose.method_22903();
        pose.method_22905(scale, scale, scale);
        for (Object node : this.roots) {
            this.submitNode(node, pose, collector, time, victimSkin);
        }
        pose.method_22909();
    }

    private void submitNode(Object node, class_4587 pose, class_11659 collector, float time, class_2960 victimSkin) {
        if (node instanceof Bone) {
            Bone bone = (Bone)node;
            pose.method_22903();
            float[] position = BloodEffectModel.sample(bone.tracks.get("position"), time, 0.0f);
            float[] rotation = BloodEffectModel.sample(bone.tracks.get("rotation"), time, 0.0f);
            float[] scale = BloodEffectModel.sample(bone.tracks.get("scale"), time, 1.0f);
            pose.method_46416((bone.origin[0] + position[0]) / 16.0f, (bone.origin[1] + position[1]) / 16.0f, (bone.origin[2] + position[2]) / 16.0f);
            BloodEffectModel.rotate(pose, bone.rotation[0] + rotation[0], bone.rotation[1] + rotation[1], bone.rotation[2] + rotation[2]);
            pose.method_22905(scale[0], scale[1], scale[2]);
            pose.method_46416(-bone.origin[0] / 16.0f, -bone.origin[1] / 16.0f, -bone.origin[2] / 16.0f);
            for (Object child : bone.children) {
                this.submitNode(child, pose, collector, time, victimSkin);
            }
            pose.method_22909();
        } else if (node instanceof Cube) {
            Cube cube = (Cube)node;
            pose.method_22903();
            if (cube.rotation != null) {
                pose.method_46416(cube.origin[0] / 16.0f, cube.origin[1] / 16.0f, cube.origin[2] / 16.0f);
                BloodEffectModel.rotate(pose, cube.rotation[0], cube.rotation[1], cube.rotation[2]);
                pose.method_46416(-cube.origin[0] / 16.0f, -cube.origin[1] / 16.0f, -cube.origin[2] / 16.0f);
            }
            class_2960 texture = cube.textureIndex == 0 && victimSkin != null && this.textures.length > 1 ? victimSkin : this.textures[cube.textureIndex];
            collector.method_73483(pose, class_12249.method_75994((class_2960)texture), (matrix, consumer) -> cube.submit(matrix, consumer));
            pose.method_22909();
        }
    }

    private static void rotate(class_4587 pose, float x, float y, float z) {
        if (z != 0.0f) {
            pose.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(z));
        }
        if (y != 0.0f) {
            pose.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(y));
        }
        if (x != 0.0f) {
            pose.method_22907((Quaternionfc)class_7833.field_40714.rotationDegrees(x));
        }
    }

    private static List<Object> resolve(JsonArray entries, Map<String, Bone> bones, Map<String, Cube> cubes) {
        ArrayList<Object> result = new ArrayList<Object>();
        for (JsonElement entry : entries) {
            JsonObject group;
            Bone bone;
            if (entry.isJsonPrimitive()) {
                Cube cube = cubes.get(entry.getAsString());
                if (cube == null) continue;
                result.add(cube);
                continue;
            }
            if (!entry.isJsonObject() || (bone = bones.get((group = entry.getAsJsonObject()).get("uuid").getAsString())) == null) continue;
            bone.children.addAll(BloodEffectModel.resolve(group.getAsJsonArray("children"), bones, cubes));
            result.add(bone);
        }
        return result;
    }

    private static Map<String, List<Key>> tracks(JsonObject animator) {
        HashMap<String, List<Key>> result = new HashMap<String, List<Key>>();
        if (animator == null || !animator.has("keyframes")) {
            return result;
        }
        for (JsonElement entry : animator.getAsJsonArray("keyframes")) {
            JsonArray points;
            JsonObject key = entry.getAsJsonObject();
            String channel = key.get("channel").getAsString();
            if (!channel.equals("position") && !channel.equals("rotation") && !channel.equals("scale") || (points = key.getAsJsonArray("data_points")) == null || points.isEmpty()) continue;
            JsonObject values = points.get(0).getAsJsonObject();
            float[] xyz = new float[]{BloodEffectModel.number(values, "x", channel.equals("scale") ? 1.0f : 0.0f), BloodEffectModel.number(values, "y", channel.equals("scale") ? 1.0f : 0.0f), BloodEffectModel.number(values, "z", channel.equals("scale") ? 1.0f : 0.0f)};
            result.computeIfAbsent(channel, ignored -> new ArrayList()).add(new Key(BloodEffectModel.number(key, "time", 0.0f), xyz, key.has("interpolation") ? key.get("interpolation").getAsString() : "linear"));
        }
        for (List keys : result.values()) {
            keys.sort(Comparator.comparingDouble(k -> k.time));
        }
        return result;
    }

    private static float[] sample(List<Key> keys, float time, float fallback) {
        if (keys == null || keys.isEmpty()) {
            return new float[]{fallback, fallback, fallback};
        }
        if (time <= keys.get((int)0).time) {
            return keys.get((int)0).value;
        }
        for (int i = 1; i < keys.size(); ++i) {
            Key next = keys.get(i);
            if (time > next.time) continue;
            Key prior = keys.get(i - 1);
            if ("step".equals(prior.interpolation)) {
                return prior.value;
            }
            float span = Math.max(1.0E-4f, next.time - prior.time);
            float t = Math.max(0.0f, Math.min(1.0f, (time - prior.time) / span));
            float[] value = new float[3];
            for (int axis = 0; axis < 3; ++axis) {
                if ("catmullrom".equals(prior.interpolation) || "catmullrom".equals(next.interpolation)) {
                    float a = keys.get((int)Math.max((int)0, (int)(i - 2))).value[axis];
                    float b = prior.value[axis];
                    float c = next.value[axis];
                    float d = keys.get((int)Math.min((int)(keys.size() - 1), (int)(i + 1))).value[axis];
                    value[axis] = 0.5f * (2.0f * b + (c - a) * t + (2.0f * a - 5.0f * b + 4.0f * c - d) * t * t + (3.0f * b - a - 3.0f * c + d) * t * t * t);
                    continue;
                }
                value[axis] = prior.value[axis] + (next.value[axis] - prior.value[axis]) * t;
            }
            return value;
        }
        return keys.get((int)(keys.size() - 1)).value;
    }

    private static float[] vec(JsonArray array, float fallback) {
        float[] result = new float[]{fallback, fallback, fallback};
        if (array != null) {
            for (int i = 0; i < Math.min(3, array.size()); ++i) {
                result[i] = array.get(i).getAsFloat();
            }
        }
        return result;
    }

    private static float number(JsonObject data, String key, float fallback) {
        try {
            return data.has(key) ? Float.parseFloat(data.get(key).getAsString()) : fallback;
        }
        catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static final class Bone {
        final float[] origin;
        final float[] rotation;
        final Map<String, List<Key>> tracks;
        final List<Object> children = new ArrayList<Object>();

        Bone(float[] origin, float[] rotation, Map<String, List<Key>> tracks) {
            this.origin = origin;
            this.rotation = rotation;
            this.tracks = tracks;
        }
    }

    private static final class Cube {
        final float[] from;
        final float[] to;
        final float[] origin;
        final float[] rotation;
        final float[][] uv = new float[6][];
        final float textureWidth;
        final float textureHeight;
        final int textureIndex;

        Cube(JsonObject data, float[][] textureSizes) {
            JsonObject face;
            JsonObject faces = data.getAsJsonObject("faces");
            int index = -1;
            if (faces != null) {
                for (Map.Entry entry : faces.entrySet()) {
                    face = ((JsonElement)entry.getValue()).getAsJsonObject();
                    if (!face.has("texture") || face.get("texture").isJsonNull()) continue;
                    index = face.get("texture").getAsInt();
                    break;
                }
            }
            this.textureIndex = index >= 0 && index < textureSizes.length ? index : -1;
            this.textureWidth = this.textureIndex < 0 ? 64.0f : textureSizes[this.textureIndex][0];
            this.textureHeight = this.textureIndex < 0 ? 64.0f : textureSizes[this.textureIndex][1];
            this.from = BloodEffectModel.vec(data.getAsJsonArray("from"), 0.0f);
            this.to = BloodEffectModel.vec(data.getAsJsonArray("to"), 0.0f);
            this.origin = BloodEffectModel.vec(data.getAsJsonArray("origin"), 0.0f);
            this.rotation = data.has("rotation") ? BloodEffectModel.vec(data.getAsJsonArray("rotation"), 0.0f) : null;
            String[] sides = new String[]{"north", "south", "west", "east", "up", "down"};
            for (int side = 0; side < 6; ++side) {
                JsonObject jsonObject = face = faces == null ? null : faces.getAsJsonObject(sides[side]);
                if (face == null || !face.has("texture") || face.get("texture").isJsonNull() || face.get("texture").getAsInt() != this.textureIndex || !face.has("uv")) continue;
                this.uv[side] = new float[]{face.getAsJsonArray("uv").get(0).getAsFloat(), face.getAsJsonArray("uv").get(1).getAsFloat(), face.getAsJsonArray("uv").get(2).getAsFloat(), face.getAsJsonArray("uv").get(3).getAsFloat()};
            }
        }

        void submit(class_4587.class_4665 matrix, class_4588 consumer) {
            float x0 = this.from[0] / 16.0f;
            float y0 = this.from[1] / 16.0f;
            float z0 = this.from[2] / 16.0f;
            float x1 = this.to[0] / 16.0f;
            float y1 = this.to[1] / 16.0f;
            float z1 = this.to[2] / 16.0f;
            float[][][] faces = new float[][][]{new float[][]{{x1, y0, z0}, {x0, y0, z0}, {x0, y1, z0}, {x1, y1, z0}}, new float[][]{{x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}}, new float[][]{{x0, y0, z0}, {x0, y0, z1}, {x0, y1, z1}, {x0, y1, z0}}, new float[][]{{x1, y0, z1}, {x1, y0, z0}, {x1, y1, z0}, {x1, y1, z1}}, new float[][]{{x0, y1, z0}, {x0, y1, z1}, {x1, y1, z1}, {x1, y1, z0}}, new float[][]{{x0, y0, z1}, {x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}}};
            float[][] normals = new float[][]{{0.0f, 0.0f, -1.0f}, {0.0f, 0.0f, 1.0f}, {-1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, -1.0f, 0.0f}};
            for (int side = 0; side < 6; ++side) {
                float[] rect = this.uv[side];
                if (rect == null) continue;
                float[][] corners = new float[][]{{rect[0], rect[3]}, {rect[2], rect[3]}, {rect[2], rect[1]}, {rect[0], rect[1]}};
                for (int i = 0; i < 4; ++i) {
                    float[] point = faces[side][i];
                    consumer.method_56824(matrix, point[0], point[1], point[2]).method_39415(-1).method_22913(corners[i][0] / this.textureWidth, corners[i][1] / this.textureHeight).method_22922(class_4608.field_21444).method_60803(0xF000F0).method_60831(matrix, normals[side][0], normals[side][1], normals[side][2]);
                }
            }
        }
    }

    private record Key(float time, float[] value, String interpolation) {
    }
}

