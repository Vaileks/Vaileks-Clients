/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Vector4f
 */
package ru.bloodhelper.client.modules;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class WaypointProjection {
    private WaypointProjection() {
    }

    public static Point project(Matrix4f viewProjection, double x, double y, double z, int width, int height) {
        if (!(width > 0 && height > 0 && Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z))) {
            return null;
        }
        Vector4f clip = viewProjection.transform(new Vector4f((float)x, (float)y, (float)z, 1.0f));
        if (!Float.isFinite(clip.w) || clip.w <= 0.05f || !Float.isFinite(clip.x) || !Float.isFinite(clip.y) || !Float.isFinite(clip.z) || clip.z < -clip.w) {
            return null;
        }
        float nx = clip.x / clip.w;
        float ny = clip.y / clip.w;
        if (nx < -1.0f || nx > 1.0f || ny < -1.0f || ny > 1.0f) {
            return null;
        }
        return new Point((nx + 1.0f) * (float)width / 2.0f, (1.0f - ny) * (float)height / 2.0f);
    }

    public record Point(float x, float y) {
    }
}

