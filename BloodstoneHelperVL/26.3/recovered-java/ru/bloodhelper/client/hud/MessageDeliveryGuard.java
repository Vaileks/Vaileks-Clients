/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;

final class MessageDeliveryGuard {
    private final IdentityHashMap<Object, Boolean> seen = new IdentityHashMap();
    private final ArrayDeque<Object> order = new ArrayDeque();

    MessageDeliveryGuard() {
    }

    boolean accept(Object message) {
        if (message == null || this.seen.put(message, Boolean.TRUE) != null) {
            return false;
        }
        this.order.addLast(message);
        if (this.order.size() > 128) {
            this.seen.remove(this.order.removeFirst());
        }
        return true;
    }

    void clear() {
        this.seen.clear();
        this.order.clear();
    }
}

