/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.skins;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.skins.CustomModels;

public record SkinMesh(Bone[] bones, String[] textures) {
    private static final Map<String, Optional<SkinMesh>> CACHE = new ConcurrentHashMap<String, Optional<SkinMesh>>();
    private static final Set<String> CHARACTERS = Set.of("RUDY", "VERITI", "JENNY", "MIMI_TYPH");

    public static SkinMesh get(String character) {
        if (CustomModels.validId(character)) {
            return CustomModels.get(character);
        }
        if (!CHARACTERS.contains(character == null ? "" : character)) {
            return null;
        }
        return CACHE.computeIfAbsent(character, SkinMesh::load).orElse(null);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static Optional<SkinMesh> load(String character) {
        String path = "/assets/bloodhelper/skins/" + character.toLowerCase(Locale.ROOT) + ".bhs";
        try (InputStream stream = SkinMesh.class.getResourceAsStream(path);){
            Optional<SkinMesh> optional;
            if (stream == null) {
                throw new IOException("Missing " + path);
            }
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(stream));){
                if (in.readInt() != 1112036146) {
                    throw new IOException("Invalid skin mesh header");
                }
                int materialCount = in.readInt();
                if (materialCount < 1 || materialCount > 4) {
                    throw new IOException("Invalid material count");
                }
                String[] textures = new String[materialCount];
                for (int i = 0; i < materialCount; ++i) {
                    textures[i] = in.readUTF();
                    if (textures[i].matches("textures/skins/[a-z_]{1,32}\\.png")) continue;
                    throw new IOException("Invalid skin texture path");
                }
                if (in.readInt() != 6) {
                    throw new IOException("Invalid skin bone count");
                }
                Bone[] bones = new Bone[6];
                int total = 0;
                for (int b = 0; b < bones.length; ++b) {
                    float x = SkinMesh.finite(in.readFloat());
                    float y = SkinMesh.finite(in.readFloat());
                    float z = SkinMesh.finite(in.readFloat());
                    int surfaceCount = in.readInt();
                    if (surfaceCount < 0 || surfaceCount > 4) {
                        throw new IOException("Invalid skin surface count");
                    }
                    Surface[] surfaces = new Surface[surfaceCount];
                    for (int s = 0; s < surfaceCount; ++s) {
                        int material = in.readInt();
                        int count = in.readInt();
                        if (material < 0 || material >= materialCount || count < 0 || count % 4 != 0 || count > 100000 || (total += count) > 150000) {
                            throw new IOException("Invalid skin surface");
                        }
                        float[] vertices = new float[count * 8];
                        for (int i = 0; i < vertices.length; ++i) {
                            vertices[i] = SkinMesh.finite(in.readFloat());
                        }
                        surfaces[s] = new Surface(material, vertices);
                    }
                    bones[b] = new Bone(x, y, z, surfaces);
                }
                if (in.read() != -1) {
                    throw new IOException("Unexpected skin mesh data");
                }
                optional = Optional.of(new SkinMesh(bones, textures));
            }
            return optional;
        }
        catch (IOException error) {
            LoggerFactory.getLogger((String)"BloodHelper/Skins").warn("Cannot load cosmetic {}", (Object)character, (Object)error);
            return Optional.empty();
        }
    }

    private static float finite(float value) throws IOException {
        if (!Float.isFinite(value) || Math.abs(value) > 8.0f) {
            throw new IOException("Invalid skin vertex");
        }
        return value;
    }

    public record Bone(float x, float y, float z, Surface[] surfaces) {
    }

    public record Surface(int material, float[] vertices) {
    }
}

