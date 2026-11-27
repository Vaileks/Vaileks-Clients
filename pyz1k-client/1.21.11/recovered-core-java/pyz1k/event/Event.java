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

