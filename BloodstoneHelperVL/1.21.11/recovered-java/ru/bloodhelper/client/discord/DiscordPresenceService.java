/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.discord;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import ru.bloodhelper.client.discord.DiscordPresence;
import ru.bloodhelper.client.discord.DiscordRpcSession;
import ru.bloodhelper.client.discord.LocalDiscordIpc;

final class DiscordPresenceService
implements AutoCloseable {
    private final DiscordRpcSession session;
    private ScheduledExecutorService worker;
    private volatile DiscordRpcSession.Request desired = new DiscordRpcSession.Request(0L, "", null, DiscordRpcSession.Status.OFF);
    private boolean stopped;

    DiscordPresenceService() {
        this(new DiscordRpcSession(LocalDiscordIpc::open, ProcessHandle.current().pid()));
    }

    DiscordPresenceService(DiscordRpcSession session) {
        this.session = session;
    }

    synchronized void update(DiscordPresence presence, DiscordRpcSession.Status inactive) {
        if (this.stopped) {
            return;
        }
        DiscordRpcSession.Request old = this.desired;
        String effectiveId = presence == null ? "" : "1560978569197396022";
        long generation = old.generation() + (long)(!old.applicationId().equals(effectiveId) || old.presence() == null != (presence == null) ? 1 : 0);
        this.desired = new DiscordRpcSession.Request(generation, effectiveId, presence, inactive);
        if (this.worker == null && this.desired.active()) {
            this.worker = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "BLOODHELPER Discord IPC");
                thread.setDaemon(true);
                return thread;
            });
            this.worker.scheduleWithFixedDelay(() -> this.session.step(() -> this.desired, System.nanoTime()), 0L, 100L, TimeUnit.MILLISECONDS);
        }
    }

    synchronized void disconnected() {
        if (this.stopped) {
            return;
        }
        this.desired = new DiscordRpcSession.Request(this.desired.generation() + 1L, "", null, DiscordRpcSession.Status.NO_WORLD);
    }

    DiscordRpcSession.Status status() {
        DiscordRpcSession.Request request = this.desired;
        return request.active() ? this.session.status() : request.inactiveStatus();
    }

    @Override
    public synchronized void close() {
        if (this.stopped) {
            return;
        }
        this.stopped = true;
        this.desired = new DiscordRpcSession.Request(this.desired.generation() + 1L, "", null, DiscordRpcSession.Status.STOPPED);
        if (this.worker != null) {
            this.worker.execute(this.session::close);
            this.worker.shutdown();
            Thread cleanup = new Thread(() -> {
                try {
                    this.worker.awaitTermination(1500L, TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }, "BLOODHELPER Discord cleanup");
            cleanup.setDaemon(false);
            cleanup.start();
        }
    }
}

