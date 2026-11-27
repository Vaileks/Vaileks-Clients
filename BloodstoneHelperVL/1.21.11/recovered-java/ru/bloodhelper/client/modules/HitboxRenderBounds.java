/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_238;
import net.minecraft.class_243;

public final class HitboxRenderBounds {
    private HitboxRenderBounds() {
    }

    public static class_238 interpolated(class_238 box, class_243 currentPosition, class_243 renderPosition) {
        return box.method_997(renderPosition.method_1020(currentPosition));
    }

    public static class_238 culling(class_238 box, class_243 eye, class_243 directionEnd) {
        class_238 visible = directionEnd == null ? box : box.method_991(new class_238(eye, directionEnd));
        return visible.method_1014(0.05);
    }
}

