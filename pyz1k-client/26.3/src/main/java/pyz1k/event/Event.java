package pyz1k.event;


public class Event {
    private boolean canceled;

    public void cancel() {
        this.canceled = true;
    }

    public boolean isCanceled() {
        return this.canceled;
    }
}

