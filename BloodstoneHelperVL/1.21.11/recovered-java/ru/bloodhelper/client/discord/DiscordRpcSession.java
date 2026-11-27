/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.discord;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import ru.bloodhelper.client.discord.DiscordIpc;
import ru.bloodhelper.client.discord.DiscordPresence;
import ru.bloodhelper.client.discord.DiscordRpcProtocol;

final class DiscordRpcSession
implements AutoCloseable {
    static final long RESPONSE_TIMEOUT = 15000000000L;
    static final long UPDATE_INTERVAL = 5000000000L;
    private final DiscordIpc.Connector connector;
    private final long pid;
    private DiscordIpc ipc;
    private DiscordRpcProtocol.Reader reader;
    private String applicationId = "";
    private String pendingNonce;
    private long generation;
    private long nonce;
    private long responseDeadline;
    private long retryAt;
    private long nextPublish;
    private long nextPing;
    private long pongDeadline;
    private long nextRefresh;
    private int failures;
    private boolean ready;
    private boolean closed;
    private byte[] expectedPong;
    private DiscordPresence sent;
    private volatile Status status = Status.OFF;

    DiscordRpcSession(DiscordIpc.Connector connector, long pid) {
        this.connector = connector;
        this.pid = pid;
    }

    Status status() {
        return this.status;
    }

    void step(Supplier<Request> desired, long now) {
        if (this.closed) {
            return;
        }
        Request request = desired.get();
        try {
            boolean redact;
            DiscordRpcProtocol.Frame frame;
            if (!(this.ipc == null || request.active() && this.sameConnection(request))) {
                this.clearAndClose();
                this.retryAt = 0L;
                this.failures = 0;
            }
            if (!request.active()) {
                this.status = request.inactiveStatus();
                return;
            }
            if (this.ipc == null) {
                if (!this.sameConnection(request)) {
                    this.retryAt = 0L;
                    this.failures = 0;
                    this.applicationId = request.applicationId();
                    this.generation = request.generation();
                }
                if (now < this.retryAt) {
                    return;
                }
                this.status = Status.CONNECTING;
                this.ipc = this.connector.open();
                this.applicationId = request.applicationId();
                this.generation = request.generation();
                if (!desired.get().active() || !this.sameConnection(desired.get())) {
                    this.clearAndClose();
                    return;
                }
                this.reader = new DiscordRpcProtocol.Reader();
                this.ready = false;
                this.ipc.write(DiscordRpcProtocol.encode(0, DiscordRpcProtocol.handshake(this.applicationId)));
                this.responseDeadline = now + 15000000000L;
            }
            for (int i = 0; i < 16 && (frame = this.reader.poll(this.ipc, now)) != null; ++i) {
                this.receive(frame, now);
            }
            if (!(this.ready && this.pendingNonce == null || now < this.responseDeadline)) {
                throw new IOException("Discord response timeout");
            }
            if (this.expectedPong != null && now >= this.pongDeadline) {
                throw new IOException("Discord heartbeat timeout");
            }
            request = desired.get();
            if (!request.active() || !this.sameConnection(request)) {
                this.clearAndClose();
                return;
            }
            if (!this.ready) {
                return;
            }
            boolean changed = !Objects.equals(this.sent, request.presence());
            boolean bl = redact = this.sent != null && (!this.sent.avatarNickname().isEmpty() && request.presence().avatarNickname().isEmpty() || !this.sent.serverIconAddress().isEmpty() && request.presence().serverIconAddress().isEmpty() || this.sent.state().contains("\u266b") && !request.presence().state().contains("\u266b"));
            if (redact || this.pendingNonce == null && now >= this.nextPublish && (changed || now >= this.nextRefresh)) {
                if (!request.equals(desired.get())) {
                    return;
                }
                this.publish(request.presence(), now);
                this.nextPublish = now + 5000000000L;
                this.nextRefresh = now + 60000000000L;
            }
            if (this.expectedPong == null && now >= this.nextPing) {
                this.expectedPong = Long.toString(++this.nonce).getBytes(StandardCharsets.UTF_8);
                this.ipc.write(DiscordRpcProtocol.encode(3, this.expectedPong));
                this.pongDeadline = now + 15000000000L;
                this.nextPing = now + 15000000000L;
            }
        }
        catch (IOException | LinkageError | RuntimeException error) {
            this.clearAndClose();
            this.retryAt = now + DiscordRpcSession.retryDelay(this.failures);
            this.failures = Math.min(this.failures + 1, 6);
            this.status = Status.RETRYING;
        }
    }

    static long retryDelay(int failures) {
        return Math.min(10L, 1L << Math.min(Math.max(0, failures), 4)) * 1000000000L;
    }

    private boolean sameConnection(Request request) {
        return this.generation == request.generation() && this.applicationId.equals(request.applicationId());
    }

    private void receive(DiscordRpcProtocol.Frame frame, long now) throws IOException {
        switch (frame.opcode()) {
            case 3: {
                this.ipc.write(DiscordRpcProtocol.encode(4, frame.payload()));
                break;
            }
            case 4: {
                if (!Arrays.equals(this.expectedPong, frame.payload())) break;
                this.expectedPong = null;
                break;
            }
            case 2: {
                throw new IOException("Discord closed IPC");
            }
            case 1: {
                JsonObject json = frame.json();
                if ("ERROR".equals(DiscordRpcProtocol.string(json, "evt"))) {
                    throw new IOException("Discord rejected RPC");
                }
                if (!this.ready && "READY".equals(DiscordRpcProtocol.string(json, "evt")) && "DISPATCH".equals(DiscordRpcProtocol.string(json, "cmd"))) {
                    this.ready = true;
                    this.nextPing = now + 15000000000L;
                    break;
                }
                if (!this.ready || this.pendingNonce == null || !this.pendingNonce.equals(DiscordRpcProtocol.string(json, "nonce")) || !"SET_ACTIVITY".equals(DiscordRpcProtocol.string(json, "cmd"))) break;
                this.pendingNonce = null;
                this.failures = 0;
                this.status = Status.CONNECTED;
                break;
            }
            default: {
                throw new IOException("Unexpected Discord opcode");
            }
        }
    }

    private void publish(DiscordPresence presence, long now) throws IOException {
        this.pendingNonce = Long.toString(++this.nonce);
        this.ipc.write(DiscordRpcProtocol.encode(1, DiscordRpcProtocol.activity(this.pid, this.pendingNonce, presence)));
        this.sent = presence;
        this.responseDeadline = now + 15000000000L;
        this.status = Status.UPDATING;
    }

    private void clearAndClose() {
        if (this.ipc != null) {
            try {
                if (this.ready) {
                    this.ipc.write(DiscordRpcProtocol.encode(1, DiscordRpcProtocol.activity(this.pid, Long.toString(++this.nonce), null)));
                }
            }
            catch (IOException | LinkageError | RuntimeException throwable) {
                // empty catch block
            }
            try {
                this.ipc.close();
            }
            catch (IOException | LinkageError | RuntimeException throwable) {
                // empty catch block
            }
        }
        this.ipc = null;
        this.reader = null;
        this.sent = null;
        this.ready = false;
        this.pendingNonce = null;
        this.expectedPong = null;
        this.nextPublish = 0L;
    }

    @Override
    public void close() {
        this.closed = true;
        this.clearAndClose();
        this.status = Status.STOPPED;
    }

    static enum Status {
        OFF,
        INVALID_ID,
        STREAMER,
        NO_WORLD,
        CONNECTING,
        UPDATING,
        CONNECTED,
        RETRYING,
        STOPPED;

    }

    record Request(long generation, String applicationId, DiscordPresence presence, Status inactiveStatus) {
        boolean active() {
            return this.presence != null && DiscordRpcProtocol.validApplicationId(this.applicationId);
        }
    }
}

