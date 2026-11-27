/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

public final class ReGlassChatContext {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private ReGlassChatContext() {
    }

    public static void enter() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void exit() {
        int depth = DEPTH.get() - 1;
        if (depth <= 0) {
            DEPTH.remove();
        } else {
            DEPTH.set(depth);
        }
    }

    public static boolean drawing() {
        return DEPTH.get() > 0;
    }
}

