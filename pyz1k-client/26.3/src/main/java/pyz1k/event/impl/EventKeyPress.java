/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyz1k.event.impl;

import pyz1k.event.Event;

public class EventKeyPress
extends Event {
    int key;
    int action;

    public int getKey() {
        return this.key;
    }

    public int getAction() {
        return this.action;
    }

    public EventKeyPress(int key, int action) {
        this.key = key;
        this.action = action;
    }
}

