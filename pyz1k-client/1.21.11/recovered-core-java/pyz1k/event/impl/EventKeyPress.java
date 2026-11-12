/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyz1k.event.impl;

import lombok.Generated;
import pyz1k.event.Event;

public class EventKeyPress
extends Event {
    int key;
    int action;

    @Generated
    public int getKey() {
        return this.key;
    }

    @Generated
    public int getAction() {
        return this.action;
    }

    @Generated
    public EventKeyPress(int key, int action) {
        this.key = key;
        this.action = action;
    }
}

