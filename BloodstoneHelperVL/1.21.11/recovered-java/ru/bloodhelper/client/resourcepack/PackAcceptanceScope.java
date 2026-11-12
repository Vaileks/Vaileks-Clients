/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.resourcepack;

import java.util.Objects;
import java.util.UUID;

public final class PackAcceptanceScope {
    private static final ThreadLocal<UUID> ACCEPTING = new ThreadLocal();

    private PackAcceptanceScope() {
    }

    public static void accepting(UUID id, Runnable action) {
        Objects.requireNonNull(id, "pack id");
        Objects.requireNonNull(action, "pack push");
        UUID previous = ACCEPTING.get();
        ACCEPTING.set(id);
        try {
            action.run();
        }
        finally {
            if (previous == null) {
                ACCEPTING.remove();
            } else {
                ACCEPTING.set(previous);
            }
        }
    }

    public static boolean matches(UUID id) {
        UUID current = ACCEPTING.get();
        return current != null && current.equals(id);
    }
}

