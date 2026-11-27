/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_1297
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_4184
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector4f
 */
package ru.bloodhelper.client.visuals;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_1041;
import net.minecraft.class_1297;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4184;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector4f;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.modules.WaypointProjection;

public final class WorldProjection {
    private static final Matrix4f matrix = new Matrix4f();
    private static final Matrix4f viewMatrix = new Matrix4f();
    private static final Quaternionf rotation = new Quaternionf();
    private static class_243 camera = class_243.field_1353;
    private static Object level;
    private static long captured;
    private static float focal;
    private static final Map<class_243, Visibility> visibility;
    private static int rays;

    private WorldProjection() {
    }

    public static void capture(class_4184 source, Matrix4f view, Matrix4f projection) {
        boolean lyrics;
        ClientConfig c = BloodHelperClient.config();
        boolean bl = lyrics = c.music.enabled && ("WORLD".equals(c.music.lyricsMode) || "BOTH".equals(c.music.lyricsMode));
        if (!c.legendaryVisualsEnabled() && !lyrics) {
            level = null;
            return;
        }
        if (level != class_310.method_1551().field_1687) {
            visibility.clear();
        }
        rays = 0;
        viewMatrix.set((Matrix4fc)view);
        matrix.set((Matrix4fc)projection).mul((Matrix4fc)view);
        camera = source.method_71156();
        rotation.set((Quaternionfc)source.method_23767());
        focal = Math.abs(projection.m11());
        level = class_310.method_1551().field_1687;
        captured = System.nanoTime();
    }

    public static boolean valid() {
        return level != null && level == class_310.method_1551().field_1687;
    }

    public static Matrix4f view() {
        return viewMatrix;
    }

    public static double worldUnitsPerPixel(class_243 at) {
        return 2.0 * Math.max(0.05, (double)Math.abs(WorldProjection.clip((class_243)at).w)) / (double)Math.max(1.0f, focal * (float)class_310.method_1551().method_22683().method_4506());
    }

    public static class_243 camera() {
        return camera;
    }

    public static Quaternionf rotation() {
        return new Quaternionf((Quaternionfc)rotation);
    }

    public static float pixelHeight(class_243 at, double meters) {
        Vector4f clip = WorldProjection.clip(at);
        return clip.w <= 0.05f ? 0.0f : (float)(meters * (double)focal * (double)class_310.method_1551().method_22683().method_4502() / (double)(2.0f * clip.w));
    }

    private static Vector4f clip(class_243 p) {
        return matrix.transform(new Vector4f((float)(p.field_1352 - WorldProjection.camera.field_1352), (float)(p.field_1351 - WorldProjection.camera.field_1351), (float)(p.field_1350 - WorldProjection.camera.field_1350), 1.0f));
    }

    public static WaypointProjection.Point projectUnbounded(class_243 p) {
        Vector4f clip = WorldProjection.clip(p);
        if (clip.w <= 0.05f) {
            return null;
        }
        class_1041 window = class_310.method_1551().method_22683();
        return new WaypointProjection.Point((clip.x / clip.w * 0.5f + 0.5f) * (float)window.method_4486(), (0.5f - clip.y / clip.w * 0.5f) * (float)window.method_4502());
    }

    public static WaypointProjection.Point project(class_243 p) {
        class_1041 w = class_310.method_1551().method_22683();
        return WaypointProjection.project(matrix, p.field_1352 - WorldProjection.camera.field_1352, p.field_1351 - WorldProjection.camera.field_1351, p.field_1350 - WorldProjection.camera.field_1350, w.method_4486(), w.method_4502());
    }

    public static boolean visible(class_243 p) {
        boolean result;
        class_310 mc = class_310.method_1551();
        if (!WorldProjection.valid() || mc.field_1724 == null) {
            return false;
        }
        long now = System.nanoTime();
        Visibility cached = visibility.get(p);
        if (cached != null && now - cached.at < 100000000L && cached.camera.method_1025(camera) < 0.02) {
            return cached.visible;
        }
        if (rays++ >= 128) {
            return false;
        }
        class_3965 hit = mc.field_1687.method_17742(new class_3959(camera, p, class_3959.class_3960.field_23142, class_3959.class_242.field_1348, (class_1297)mc.field_1724));
        boolean bl = result = hit.method_17783() == class_239.class_240.field_1333 || hit.method_17784().method_1025(p) < 0.08;
        if (visibility.size() >= 512) {
            visibility.remove(visibility.keySet().iterator().next());
        }
        visibility.put(p, new Visibility(camera, now, result));
        return result;
    }

    static {
        visibility = new LinkedHashMap<class_243, Visibility>();
    }

    private record Visibility(class_243 camera, long at, boolean visible) {
    }
}

