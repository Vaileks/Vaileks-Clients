/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyz1k.event;

import lombok.Generated;

public class Event {
    private boolean canceled;

    public void cancel() {
        this.canceled = true;
    }

    @Generated
    public boolean isCanceled() {
        return this.canceled;
    }
}

