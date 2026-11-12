/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.resources.server.PackLoadFeedback$FinalResult
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
 */
package ru.bloodhelper.client.resourcepack;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.resourcepack.BloodstonePackPreloader;
import ru.bloodhelper.client.resourcepack.PackAcceptanceScope;
import ru.bloodhelper.client.resourcepack.PackOfferPolicy;
import ru.bloodhelper.client.resourcepack.ServerPackSession;
import ru.bloodhelper.client.vl.PrivacyGuard;

public final class BloodstoneResourcePack {
    private static final ServerPackSession session = new ServerPackSession();
    private static boolean initialized;

    private BloodstoneResourcePack() {
    }

    public static void initialize(Path gameDirectory) {
        session.clear();
        BloodstonePackPreloader.initialize(gameDirectory);
        initialized = true;
        BloodstoneResourcePack.settingsChanged();
    }

    public static void settingsChanged() {
        BloodstonePackPreloader.settingsChanged(BloodstoneResourcePack.enabled());
        if (!BloodstoneResourcePack.enabled()) {
            session.clear();
        }
    }

    private static boolean enabled() {
        return initialized && BloodHelperClient.config().resourcePack.enabled;
    }

    public static Snapshot status() {
        if (!BloodstoneResourcePack.enabled()) {
            return new Snapshot(State.DISABLED, 0.0, "", 0, 0, 0);
        }
        ServerPackSession.Counts counts = session.counts();
        int total = counts.totalPacks();
        int completed = counts.completedPacks();
        int failed = counts.failedPacks();
        if (total == 0) {
            BloodstonePackPreloader.Status preload = BloodstonePackPreloader.status();
            State state = switch (preload.state()) {
                default -> throw new MatchException(null, null);
                case BloodstonePackPreloader.State.IDLE -> State.WAITING;
                case BloodstonePackPreloader.State.DOWNLOADING -> State.PRELOADING;
                case BloodstonePackPreloader.State.READY -> State.CACHED;
                case BloodstonePackPreloader.State.ERROR -> State.PRELOAD_ERROR;
            };
            return new Snapshot(state, preload.progress(), "", preload.ready(), 2, 2 - preload.ready());
        }
        State state = failed > 0 ? State.ERROR : (total == 0 ? State.WAITING : (completed == total ? State.ACTIVE : State.LOADING));
        return new Snapshot(state, total == 0 ? 0.0 : (double)completed / (double)total, "", completed, total, failed);
    }

    public static boolean offer(Minecraft minecraft, Connection connection, ServerData server, ClientboundResourcePackPushPacket packet) {
        if (!BloodstoneResourcePack.enabled() || server == null || !PackOfferPolicy.isBloodstoneAddress(server.ip)) {
            return false;
        }
        URI address = PackOfferPolicy.remoteUrl(packet.url());
        if (address == null) {
            return false;
        }
        try {
            URI uRI = address;
            PrivacyGuard.checkUri(uRI);
            URL url = uRI.toURL();
            session.offer(connection, packet.id());
            Path cached = BloodstonePackPreloader.cached(packet.hash());
            PackAcceptanceScope.accepting(packet.id(), () -> {
                if (cached != null) {
                    minecraft.getDownloadedPackSource().pushLocalPack(packet.id(), cached);
                } else {
                    minecraft.getDownloadedPackSource().pushPack(packet.id(), url, packet.hash());
                }
            });
            return true;
        }
        catch (MalformedURLException invalidUrl) {
            return false;
        }
    }

    public static void feedback(Connection connection, UUID id, PackLoadFeedback.FinalResult result) {
        if (!BloodstoneResourcePack.enabled()) {
            return;
        }
        session.result(connection, id, result == PackLoadFeedback.FinalResult.APPLIED);
    }

    public static void pop(Connection connection, Optional<UUID> id) {
        if (BloodstoneResourcePack.enabled()) {
            session.pop(connection, id);
        }
    }

    public static void disconnected() {
        session.clear();
    }

    public static void close() {
        BloodstonePackPreloader.close();
        initialized = false;
        session.clear();
    }

    public record Snapshot(State state, double progress, String message, int completedPacks, int totalPacks, int failedPacks) {
    }

    public static enum State {
        DISABLED,
        WAITING,
        PRELOADING,
        CACHED,
        PRELOAD_ERROR,
        LOADING,
        ACTIVE,
        ERROR;

    }
}

