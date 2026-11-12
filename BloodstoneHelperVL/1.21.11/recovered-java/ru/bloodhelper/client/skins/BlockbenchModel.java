/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package ru.bloodhelper.client.skins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import ru.bloodhelper.client.skins.SkinMesh;

public final class BlockbenchModel {
    public static final int MAX_BYTES = 0x800000;
    private final JsonObject model;
    private final String id;
    private final List<byte[]> images = new ArrayList<byte[]>();
    private final Map<String, JsonObject> elements = new LinkedHashMap<String, JsonObject>();
    private final Map<String, JsonObject> groups = new HashMap<String, JsonObject>();
    private final Set<String> visited = new HashSet<String>();
    private final List<List<List<Float>>> surfaces = new ArrayList<List<List<Float>>>();
    private final Vector3f[] pivots = new Vector3f[]{new Vector3f(0.0f, 0.75f, 0.0f), new Vector3f(), new Vector3f(0.3125f, 0.125f, 0.0f), new Vector3f(-0.3125f, 0.125f, 0.0f), new Vector3f(0.11875f, 0.75f, 0.0f), new Vector3f(-0.11875f, 0.75f, 0.0f)};
    private int vertices;
    private float uvWidth;
    private float uvHeight;

    private BlockbenchModel(String id, byte[] bytes) {
        if (!id.matches("CUSTOM_[a-f0-9]{64}") || bytes.length > 0x800000) {
            throw new IllegalArgumentException("Invalid custom model");
        }
        this.id = id;
        this.model = JsonParser.parseString((String)new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    public static Result parse(String id, byte[] bytes) {
        return new BlockbenchModel(id, bytes).parse();
    }

    private Result parse() {
        JsonArray textures = this.model.getAsJsonArray("textures");
        if (textures == null || textures.isEmpty() || textures.size() > 4) {
            throw new IllegalArgumentException("Use one to four embedded PNG textures");
        }
        for (JsonElement value : textures) {
            String data = value.getAsJsonObject().get("source").getAsString();
            if (!data.startsWith("data:image/png;base64,") || data.length() > 1400000) {
                throw new IllegalArgumentException("Embed PNG textures inside the bbmodel");
            }
            byte[] png = Base64.getDecoder().decode(data.substring(22));
            if (png.length < 24 || png.length > 0x100000 || png[0] != -119 || png[1] != 80 || png[2] != 78 || png[3] != 71) {
                throw new IllegalArgumentException("Invalid PNG texture");
            }
            ByteBuffer header = ByteBuffer.wrap(png);
            int w = header.getInt(16);
            int h = header.getInt(20);
            if (w < 1 || w > 512 || h < 1 || h > 512) {
                throw new IllegalArgumentException("Maximum texture size is 512 x 512");
            }
            this.images.add(png);
        }
        JsonObject resolution = this.model.has("resolution") ? this.model.getAsJsonObject("resolution") : new JsonObject();
        this.uvWidth = BlockbenchModel.positive(resolution, "width", 16.0f);
        this.uvHeight = BlockbenchModel.positive(resolution, "height", 16.0f);
        JsonArray source = this.model.getAsJsonArray("elements");
        if (source == null || source.isEmpty() || source.size() > 1024) {
            throw new IllegalArgumentException("Use 1 to 1024 model elements");
        }
        for (JsonElement value : source) {
            JsonObject e = value.getAsJsonObject();
            String uuid = e.get("uuid").getAsString();
            if (uuid.length() <= 80 && this.elements.put(uuid, e) == null) continue;
            throw new IllegalArgumentException("Duplicate model element");
        }
        for (int b = 0; b < 6; ++b) {
            ArrayList materials = new ArrayList();
            for (int m = 0; m < this.images.size(); ++m) {
                materials.add(new ArrayList());
            }
            this.surfaces.add(materials);
        }
        if (this.model.has("groups")) {
            JsonArray sourceGroups = this.model.getAsJsonArray("groups");
            if (sourceGroups.size() > 2048) {
                throw new IllegalArgumentException("Too many model groups");
            }
            for (JsonElement item : sourceGroups) {
                JsonObject group = item.getAsJsonObject();
                this.groups.put(group.get("uuid").getAsString(), group);
            }
        }
        if (this.model.has("outliner")) {
            this.walk(this.model.getAsJsonArray("outliner"), new Matrix4f(), 0, 0, true);
        }
        for (String uuid : this.elements.keySet()) {
            if (this.visited.contains(uuid)) continue;
            this.element(uuid, new Matrix4f(), 0, true);
        }
        if (this.vertices == 0) {
            throw new IllegalArgumentException("Model has no textured faces");
        }
        SkinMesh.Bone[] bones = new SkinMesh.Bone[6];
        for (int b = 0; b < 6; ++b) {
            ArrayList<SkinMesh.Surface> list = new ArrayList<SkinMesh.Surface>();
            for (int m = 0; m < this.images.size(); ++m) {
                List<Float> data = this.surfaces.get(b).get(m);
                if (data.isEmpty()) continue;
                float[] array = new float[data.size()];
                for (int i = 0; i < array.length; ++i) {
                    array[i] = data.get(i).floatValue();
                }
                list.add(new SkinMesh.Surface(m, array));
            }
            Vector3f p = this.pivots[b];
            bones[b] = new SkinMesh.Bone(p.x, p.y, p.z, (SkinMesh.Surface[])list.toArray(SkinMesh.Surface[]::new));
        }
        String[] paths = new String[this.images.size()];
        for (int i = 0; i < paths.length; ++i) {
            paths[i] = "textures/skins/custom_" + this.id.substring(7) + "_" + i + ".png";
        }
        return new Result(new SkinMesh(bones, paths), List.copyOf(this.images));
    }

    private void walk(JsonArray nodes, Matrix4f parent, int bone, int depth, boolean visible) {
        if (depth > 32 || nodes.size() > 2048) {
            throw new IllegalArgumentException("Model hierarchy too deep");
        }
        for (JsonElement node : nodes) {
            if (node.isJsonPrimitive()) {
                this.element(node.getAsString(), parent, bone, visible);
                continue;
            }
            JsonObject group = node.getAsJsonObject().deepCopy();
            if (group.has("uuid") && this.groups.containsKey(group.get("uuid").getAsString())) {
                JsonObject template = this.groups.get(group.get("uuid").getAsString());
                for (Map.Entry field : template.entrySet()) {
                    if (group.has((String)field.getKey())) continue;
                    group.add((String)field.getKey(), (JsonElement)field.getValue());
                }
            }
            int selected = BlockbenchModel.bone(group.has("name") ? group.get("name").getAsString() : "", bone);
            Matrix4f matrix = BlockbenchModel.transform(parent, group);
            if (selected != bone && group.has("origin")) {
                this.pivots[selected] = BlockbenchModel.position(matrix, new Vector3f((Vector3fc)BlockbenchModel.vector(group, "origin")));
            }
            if (!group.has("children")) continue;
            this.walk(group.getAsJsonArray("children"), matrix, selected, depth + 1, visible && BlockbenchModel.flag(group, "visibility") && BlockbenchModel.flag(group, "export"));
        }
    }

    private void element(String uuid, Matrix4f parent, int bone, boolean visible) {
        JsonObject e = this.elements.get(uuid);
        if (e == null || !this.visited.add(uuid)) {
            return;
        }
        if (!(visible && BlockbenchModel.flag(e, "visibility") && BlockbenchModel.flag(e, "export"))) {
            return;
        }
        Matrix4f matrix = BlockbenchModel.transform(parent, e);
        String type = e.has("type") ? e.get("type").getAsString() : "cube";
        JsonObject faces = e.getAsJsonObject("faces");
        if (faces == null) {
            return;
        }
        if (type.equals("mesh")) {
            JsonObject points = e.getAsJsonObject("vertices");
            if (points == null || points.size() > 8192) {
                throw new IllegalArgumentException("Invalid mesh vertices");
            }
            Vector3f origin = BlockbenchModel.vector(e, "origin");
            for (Map.Entry entry : faces.entrySet()) {
                JsonObject face = ((JsonElement)entry.getValue()).getAsJsonObject();
                int material = this.material(face);
                if (material < 0) continue;
                JsonArray keys = face.getAsJsonArray("vertices");
                JsonObject uv = face.getAsJsonObject("uv");
                if (keys == null || keys.size() < 3 || keys.size() > 64 || uv == null) {
                    throw new IllegalArgumentException("Invalid mesh face");
                }
                for (int i = 1; i < keys.size() - 1; ++i) {
                    String[] names = new String[]{keys.get(0).getAsString(), keys.get(i).getAsString(), keys.get(i + 1).getAsString(), keys.get(i + 1).getAsString()};
                    Vector3f[] v = new Vector3f[4];
                    float[][] u = new float[4][];
                    for (int k = 0; k < 4; ++k) {
                        v[k] = BlockbenchModel.position(matrix, BlockbenchModel.array(points.getAsJsonArray(names[k])).add((Vector3fc)origin));
                        u[k] = this.uv(uv.getAsJsonArray(names[k]));
                    }
                    this.emit(bone, material, v, u);
                }
            }
        } else if (type.equals("cube")) {
            Vector3f a = BlockbenchModel.vector(e, "from");
            Vector3f b = BlockbenchModel.vector(e, "to");
            float inflate = e.has("inflate") ? BlockbenchModel.finite(e.get("inflate").getAsFloat()) : 0.0f;
            a.sub(inflate, inflate, inflate);
            b.add(inflate, inflate, inflate);
            for (Map.Entry entry : faces.entrySet()) {
                float[][] fArrayArray;
                JsonObject face = ((JsonElement)entry.getValue()).getAsJsonObject();
                int material = this.material(face);
                if (material < 0) continue;
                float x = a.x;
                float y = a.y;
                float z = a.z;
                float X = b.x;
                float Y = b.y;
                float Z = b.z;
                switch ((String)entry.getKey()) {
                    case "north": {
                        float[][] fArrayArray2 = new float[4][];
                        fArrayArray2[0] = new float[]{X, y, z};
                        fArrayArray2[1] = new float[]{x, y, z};
                        fArrayArray2[2] = new float[]{x, Y, z};
                        fArrayArray = fArrayArray2;
                        fArrayArray2[3] = new float[]{X, Y, z};
                        break;
                    }
                    case "south": {
                        float[][] fArrayArray3 = new float[4][];
                        fArrayArray3[0] = new float[]{x, y, Z};
                        fArrayArray3[1] = new float[]{X, y, Z};
                        fArrayArray3[2] = new float[]{X, Y, Z};
                        fArrayArray = fArrayArray3;
                        fArrayArray3[3] = new float[]{x, Y, Z};
                        break;
                    }
                    case "west": {
                        float[][] fArrayArray4 = new float[4][];
                        fArrayArray4[0] = new float[]{x, y, z};
                        fArrayArray4[1] = new float[]{x, y, Z};
                        fArrayArray4[2] = new float[]{x, Y, Z};
                        fArrayArray = fArrayArray4;
                        fArrayArray4[3] = new float[]{x, Y, z};
                        break;
                    }
                    case "east": {
                        float[][] fArrayArray5 = new float[4][];
                        fArrayArray5[0] = new float[]{X, y, Z};
                        fArrayArray5[1] = new float[]{X, y, z};
                        fArrayArray5[2] = new float[]{X, Y, z};
                        fArrayArray = fArrayArray5;
                        fArrayArray5[3] = new float[]{X, Y, Z};
                        break;
                    }
                    case "up": {
                        float[][] fArrayArray6 = new float[4][];
                        fArrayArray6[0] = new float[]{x, Y, Z};
                        fArrayArray6[1] = new float[]{X, Y, Z};
                        fArrayArray6[2] = new float[]{X, Y, z};
                        fArrayArray = fArrayArray6;
                        fArrayArray6[3] = new float[]{x, Y, z};
                        break;
                    }
                    case "down": {
                        float[][] fArrayArray7 = new float[4][];
                        fArrayArray7[0] = new float[]{x, y, z};
                        fArrayArray7[1] = new float[]{X, y, z};
                        fArrayArray7[2] = new float[]{X, y, Z};
                        fArrayArray = fArrayArray7;
                        fArrayArray7[3] = new float[]{x, y, Z};
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unknown cube face");
                    }
                }
                float[][] points = fArrayArray;
                JsonArray rect = face.getAsJsonArray("uv");
                if (rect == null || rect.size() != 4) {
                    throw new IllegalArgumentException("Missing cube face UV");
                }
                float u0 = BlockbenchModel.finite(rect.get(0).getAsFloat()) / this.uvWidth;
                float v0 = BlockbenchModel.finite(rect.get(1).getAsFloat()) / this.uvHeight;
                float u1 = BlockbenchModel.finite(rect.get(2).getAsFloat()) / this.uvWidth;
                float v1 = BlockbenchModel.finite(rect.get(3).getAsFloat()) / this.uvHeight;
                float[][] uv = new float[][]{{u0, v1}, {u1, v1}, {u1, v0}, {u0, v0}};
                float[][] rotated = new float[4][];
                int rotation = face.has("rotation") ? Math.floorMod(face.get("rotation").getAsInt() / 90, 4) : 0;
                Vector3f[] v = new Vector3f[4];
                for (int i = 0; i < 4; ++i) {
                    v[i] = BlockbenchModel.position(matrix, new Vector3f(points[i]));
                    rotated[i] = uv[(i + rotation) % 4];
                }
                this.emit(bone, material, v, rotated);
            }
        } else {
            throw new IllegalArgumentException("Only cube and mesh elements are supported");
        }
    }

    private void emit(int bone, int material, Vector3f[] v, float[][] uv) {
        if ((this.vertices += 4) > 65536) {
            throw new IllegalArgumentException("Custom model exceeds 65536 vertices");
        }
        Vector3f normal = new Vector3f((Vector3fc)v[1]).sub((Vector3fc)v[0]).cross((Vector3fc)new Vector3f((Vector3fc)v[2]).sub((Vector3fc)v[0]));
        if (normal.lengthSquared() < 1.0E-10f) {
            return;
        }
        normal.normalize();
        List<Float> list = this.surfaces.get(bone).get(material);
        for (int i = 0; i < 4; ++i) {
            for (float n : new float[]{v[i].x, v[i].y, v[i].z, normal.x, normal.y, normal.z, uv[i][0], uv[i][1]}) {
                if (!Float.isFinite(n) || Math.abs(n) > 8.0f) {
                    throw new IllegalArgumentException("Model extends outside supported bounds");
                }
                list.add(Float.valueOf(n));
            }
        }
    }

    private int material(JsonObject face) {
        int value;
        if (!face.has("texture") || face.get("texture").isJsonNull()) {
            return -1;
        }
        String s = face.get("texture").getAsString();
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        if ((value = Integer.parseInt(s)) < 0 || value >= this.images.size()) {
            throw new IllegalArgumentException("Invalid texture index");
        }
        return value;
    }

    private float[] uv(JsonArray a) {
        if (a == null || a.size() != 2) {
            throw new IllegalArgumentException("Invalid mesh UV");
        }
        return new float[]{BlockbenchModel.finite(a.get(0).getAsFloat()) / this.uvWidth, BlockbenchModel.finite(a.get(1).getAsFloat()) / this.uvHeight};
    }

    private static Matrix4f transform(Matrix4f parent, JsonObject o) {
        Vector3f origin = BlockbenchModel.vector(o, "origin");
        Vector3f r = BlockbenchModel.vector(o, "rotation");
        return new Matrix4f((Matrix4fc)parent).translate((Vector3fc)origin).rotateZYX((float)Math.toRadians(r.z), (float)Math.toRadians(r.y), (float)Math.toRadians(r.x)).translate(-origin.x, -origin.y, -origin.z);
    }

    private static Vector3f position(Matrix4f transform, Vector3f point) {
        transform.transformPosition(point);
        return point.set(point.x / 16.0f, 1.5f - point.y / 16.0f, point.z / 16.0f);
    }

    private static Vector3f vector(JsonObject o, String key) {
        return o.has(key) ? BlockbenchModel.array(o.getAsJsonArray(key)) : new Vector3f();
    }

    private static Vector3f array(JsonArray a) {
        if (a == null || a.size() != 3) {
            throw new IllegalArgumentException("Invalid position");
        }
        return new Vector3f(BlockbenchModel.finite(a.get(0).getAsFloat()), BlockbenchModel.finite(a.get(1).getAsFloat()), BlockbenchModel.finite(a.get(2).getAsFloat()));
    }

    private static float finite(float f) {
        if (!Float.isFinite(f) || Math.abs(f) > 4096.0f) {
            throw new IllegalArgumentException("Invalid numeric value");
        }
        return f;
    }

    private static float positive(JsonObject o, String key, float fallback) {
        float f;
        float f2 = f = o.has(key) ? BlockbenchModel.finite(o.get(key).getAsFloat()) : fallback;
        if (f <= 0.0f) {
            throw new IllegalArgumentException("Invalid texture resolution");
        }
        return f;
    }

    private static boolean flag(JsonObject o, String key) {
        return !o.has(key) || o.get(key).getAsBoolean();
    }

    private static int bone(String name, int fallback) {
        return switch (name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "")) {
            case "body", "torso", "chest" -> 0;
            case "head" -> 1;
            case "leftarm", "armleft" -> 2;
            case "rightarm", "armright" -> 3;
            case "leftleg", "legleft" -> 4;
            case "rightleg", "legright" -> 5;
            default -> fallback;
        };
    }

    public record Result(SkinMesh mesh, List<byte[]> textures) {
    }
}

