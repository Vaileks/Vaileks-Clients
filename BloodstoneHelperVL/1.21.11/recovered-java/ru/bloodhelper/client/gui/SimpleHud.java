/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

public final class SimpleHud {
    private static boolean active;

    private SimpleHud() {
    }

    public static boolean active() {
        return active;
    }

    public static boolean enter(boolean value) {
        boolean previous = active;
        active = value;
        return previous;
    }

    public static void restore(boolean previous) {
        active = previous;
    }
}

