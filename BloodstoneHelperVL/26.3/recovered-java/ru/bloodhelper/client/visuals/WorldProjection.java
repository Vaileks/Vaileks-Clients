/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector4f
 */
package ru.bloodhelper.client.visuals;

import com.mojang.blaze3d.platform.Window;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
    private static Vec3 camera = Vec3.ZERO;
    private static Object level;
    private static long captured;
    private static float focal;
    private static final Map<Vec3, Visibility> visibility;
    private static int rays;

    private WorldProjection() {
    }

    public static void capture(CameraRenderState source, Matrix4f view, Matrix4f projection) {
        boolean lyrics;
        ClientConfig c = BloodHelperClient.config();
        boolean bl = lyrics = c.music.enabled && ("WORLD".equals(c.music.lyricsMode) || "BOTH".equals(c.music.lyricsMode));
        if (!c.legendaryVisualsEnabled() && !lyrics) {
            level = null;
            return;
        }
        if (level != Minecraft.getInstance().level) {
            visibility.clear();
        }
        rays = 0;
        viewMatrix.set((Matrix4fc)view);
        matrix.set((Matrix4fc)projection).mul((Matrix4fc)view);
        camera = source.pos;
        rotation.set((Quaternionfc)source.orientation);
        focal = Math.abs(projection.m11());
        level = Minecraft.getInstance().level;
        captured = System.nanoTime();
    }

    public static boolean valid() {
        return level != null && level == Minecraft.getInstance().level;
    }

    public static Matrix4f view() {
        return viewMatrix;
    }

    public static double worldUnitsPerPixel(Vec3 at) {
        return 2.0 * Math.max(0.05, (double)Math.abs(WorldProjection.clip((Vec3)at).w)) / (double)Math.max(1.0f, focal * (float)Minecraft.getInstance().getWindow().getHeight());
    }

    public static Vec3 camera() {
        return camera;
    }

    public static Quaternionf rotation() {
        return new Quaternionf((Quaternionfc)rotation);
    }

    public static float pixelHeight(Vec3 at, double meters) {
        Vector4f clip = WorldProjection.clip(at);
        return clip.w <= 0.05f ? 0.0f : (float)(meters * (double)focal * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)(2.0f * clip.w));
    }

    private static Vector4f clip(Vec3 p) {
        return matrix.transform(new Vector4f((float)(p.x - WorldProjection.camera.x), (float)(p.y - WorldProjection.camera.y), (float)(p.z - WorldProjection.camera.z), 1.0f));
    }

    public static WaypointProjection.Point projectUnbounded(Vec3 p) {
        Vector4f clip = WorldProjection.clip(p);
        if (clip.w <= 0.05f) {
            return null;
        }
        Window window = Minecraft.getInstance().getWindow();
        return new WaypointProjection.Point((clip.x / clip.w * 0.5f + 0.5f) * (float)window.getGuiScaledWidth(), (0.5f - clip.y / clip.w * 0.5f) * (float)window.getGuiScaledHeight());
    }

    public static WaypointProjection.Point project(Vec3 p) {
        Window w = Minecraft.getInstance().getWindow();
        return WaypointProjection.project(matrix, p.x - WorldProjection.camera.x, p.y - WorldProjection.camera.y, p.z - WorldProjection.camera.z, w.getGuiScaledWidth(), w.getGuiScaledHeight());
    }

    public static boolean visible(Vec3 p) {
        boolean result;
        Minecraft mc = Minecraft.getInstance();
        if (!WorldProjection.valid() || mc.player == null) {
            return false;
        }
        long now = System.nanoTime();
        Visibility cached = visibility.get(p);
        if (cached != null && now - cached.at < 100000000L && cached.camera.distanceToSqr(camera) < 0.02) {
            return cached.visible;
        }
        if (rays++ >= 128) {
            return false;
        }
        BlockHitResult hit = mc.level.clip(new ClipContext(camera, p, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, (Entity)mc.player));
        boolean bl = result = hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(p) < 0.08;
        if (visibility.size() >= 512) {
            visibility.remove(visibility.keySet().iterator().next());
        }
        visibility.put(p, new Visibility(camera, now, result));
        return result;
    }

    static {
        visibility = new LinkedHashMap<Vec3, Visibility>();
    }

    private record Visibility(Vec3 camera, long at, boolean visible) {
    }
}

