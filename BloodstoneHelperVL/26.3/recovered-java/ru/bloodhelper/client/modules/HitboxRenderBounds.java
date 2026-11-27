/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package ru.bloodhelper.client.modules;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class HitboxRenderBounds {
    private HitboxRenderBounds() {
    }

    public static AABB interpolated(AABB box, Vec3 currentPosition, Vec3 renderPosition) {
        return box.move(renderPosition.subtract(currentPosition));
    }

    public static AABB culling(AABB box, Vec3 eye, Vec3 directionEnd) {
        AABB visible = directionEnd == null ? box : box.minmax(new AABB(eye, directionEnd));
        return visible.inflate(0.05);
    }
}

