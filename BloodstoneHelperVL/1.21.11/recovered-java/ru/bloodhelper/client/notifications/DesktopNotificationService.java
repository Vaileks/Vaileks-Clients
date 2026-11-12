/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.notifications;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import ru.bloodhelper.client.notifications.DesktopNotification;
import ru.bloodhelper.client.notifications.WindowsNotificationBackend;

public final class DesktopNotificationService
implements AutoCloseable {
    private final Backend backend;
    private final ThreadPoolExecutor worker;
    private volatile Context context = new Context(false, true, 0L, 0L);
    private volatile String status;
    private volatile boolean closed;
    private volatile boolean activated;

    public DesktopNotificationService() {
        this(new WindowsNotificationBackend());
    }

    DesktopNotificationService(Backend backend) {
        this.backend = backend;
        this.status = backend.supported() ? "windows" : "unsupported";
        this.worker = new ThreadPoolExecutor(0, 1, 20L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(4), runnable -> {
            Thread thread = new Thread(runnable, "bloodhelper-desktop-alerts");
            thread.setDaemon(true);
            return thread;
        }, new ThreadPoolExecutor.DiscardPolicy());
    }

    public boolean supported() {
        return this.backend.supported();
    }

    public String status() {
        return this.status;
    }

    public void context(boolean enabled, boolean focused, long session, long window) {
        Context previous = this.context;
        if (previous.enabled == enabled && previous.focused == focused && previous.session == session && previous.window == window) {
            return;
        }
        this.context = new Context(enabled, focused, session, window);
        if (this.closed || !this.supported()) {
            return;
        }
        if (!enabled || focused || previous.session != session || previous.window != window) {
            this.worker.getQueue().clear();
            if (this.activated) {
                this.worker.execute(this::clearQuietly);
            }
        }
    }

    public void submit(DesktopNotification notification) {
        Context submitted = this.context;
        if (this.closed || !this.supported() || !DesktopNotificationService.allowed(submitted)) {
            return;
        }
        this.activated = true;
        this.worker.execute(() -> {
            if (this.closed || this.context != submitted || !DesktopNotificationService.allowed(submitted)) {
                return;
            }
            try {
                if (this.backend.foreground(submitted.window)) {
                    return;
                }
                if (this.context != submitted || this.closed) {
                    return;
                }
                String string = this.status = this.backend.show(submitted.window, notification) ? "submitted" : "unavailable";
                if (this.context != submitted || this.closed) {
                    this.clearQuietly();
                }
            }
            catch (LinkageError | RuntimeException failure) {
                this.status = "unavailable";
                this.clearQuietly();
            }
        });
    }

    private static boolean allowed(Context context) {
        return context.enabled && !context.focused && context.window != 0L;
    }

    private void clearQuietly() {
        try {
            this.backend.clear();
        }
        catch (LinkageError | RuntimeException throwable) {
            // empty catch block
        }
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.worker.getQueue().clear();
        if (this.activated) {
            this.worker.execute(this::clearQuietly);
        }
        this.worker.shutdown();
    }

    static interface Backend {
        public boolean supported();

        public boolean foreground(long var1);

        public boolean show(long var1, DesktopNotification var3);

        public void clear();
    }

    private record Context(boolean enabled, boolean focused, long session, long window) {
    }
}

