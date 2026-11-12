/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ru.bloodhelper.client.compat.OptionalMixinHooks$Hook
 */
package ru.bloodhelper.client.compat;

import ru.bloodhelper.client.compat.OptionalMixinHooks;

public final class OptionalMixinHooks {
    public static final int REQUIRE = 0;
    static final Hook MOUSE_TURN = new Hook();
    static final Hook CAMERA_ROTATION = new Hook();
    static final Hook CROSSHAIR_SPRITE = new Hook();
    static final Hook CROSSHAIR_PERSPECTIVE = new Hook();
    static final Hook NAME_DISTANCE = new Hook();

    private OptionalMixinHooks() {
    }

    public static boolean freeLook() {
        return MOUSE_TURN.available() && CAMERA_ROTATION.available();
    }

    public static boolean crosshair() {
        return CROSSHAIR_SPRITE.available();
    }

    public static boolean nameDistance() {
        return NAME_DISTANCE.available();
    }

    public static boolean crosshairPerspective() {
        return CROSSHAIR_SPRITE.available() && CROSSHAIR_PERSPECTIVE.available();
    }
}

