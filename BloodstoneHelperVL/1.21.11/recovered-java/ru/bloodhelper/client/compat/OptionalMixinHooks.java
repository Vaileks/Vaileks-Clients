/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.compat;

import java.util.function.BooleanSupplier;

public final class OptionalMixinHooks {
    public static final int REQUIRE = 1;
    static final Hook MOUSE_TURN = new Hook();
    static final Hook CAMERA_ROTATION = new Hook();
    static final Hook CROSSHAIR_SPRITE = new Hook();
    static final Hook CROSSHAIR_PERSPECTIVE = new Hook();
    static final Hook NAME_DISTANCE = new Hook();

    private OptionalMixinHooks() {
    }

    public static boolean freeLook() {
        return true;
    }

    public static boolean crosshair() {
        return true;
    }

    public static boolean nameDistance() {
        return true;
    }

    public static boolean crosshairPerspective() {
        return true;
    }

    static final class Hook {
        private BooleanSupplier check;
        private boolean available;

        Hook() {
        }

        synchronized void register(BooleanSupplier check) {
            this.check = check;
            this.available = false;
        }

        synchronized boolean available() {
            if (this.check != null) {
                this.available = this.check.getAsBoolean();
                this.check = null;
            }
            return this.available;
        }
    }
}

