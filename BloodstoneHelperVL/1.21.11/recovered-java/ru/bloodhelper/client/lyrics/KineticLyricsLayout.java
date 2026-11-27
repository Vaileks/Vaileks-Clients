/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaterniond
 *  org.joml.Quaterniondc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3d
 */
package ru.bloodhelper.client.lyrics;

import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionfc;
import org.joml.Vector3d;

final class KineticLyricsLayout {
    private KineticLyricsLayout() {
    }

    static Vector3d offset(Quaternionfc camera, double distance, double angle, double height) {
        return new Vector3d(Math.sin(angle) * distance, height, -Math.cos(angle) * distance).rotate((Quaterniondc)new Quaterniond(camera));
    }

    static double visibleFraction(double requestedDistance, double hitDistance) {
        if (requestedDistance <= 0.0) {
            return 1.0;
        }
        return Math.clamp((hitDistance - 0.16) / requestedDistance, 0.03, 1.0);
    }
}

