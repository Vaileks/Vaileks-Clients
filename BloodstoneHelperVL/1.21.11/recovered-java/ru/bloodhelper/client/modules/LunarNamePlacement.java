/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  org.joml.Matrix4f
 *  org.joml.Quaternionfc
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_243;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;

public final class LunarNamePlacement {
    private LunarNamePlacement() {
    }

    public static double anchorHeight(double attachmentHeight, double bodyHeight) {
        return Math.max(attachmentHeight, bodyHeight) + 0.5;
    }

    public static Matrix4f pose(class_243 anchor, Quaternionfc cameraRotation, float scale, int lineHeight) {
        return new Matrix4f().translation((float)anchor.field_1352, (float)anchor.field_1351, (float)anchor.field_1350).rotate(cameraRotation).scale(scale, -scale, scale).translate(0.0f, (float)(-(lineHeight + 1)), 0.0f);
    }
}

