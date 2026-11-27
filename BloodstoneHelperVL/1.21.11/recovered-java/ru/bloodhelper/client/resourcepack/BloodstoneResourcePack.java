/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2535
 *  net.minecraft.class_2720
 *  net.minecraft.class_310
 *  net.minecraft.class_642
 *  net.minecraft.class_9039$class_9040
 */
package ru.bloodhelper.client.resourcepack;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_2535;
import net.minecraft.class_2720;
import net.minecraft.class_310;
import net.minecraft.class_642;
import net.minecraft.class_9039;
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

    public static boolean offer(class_310 minecraft, class_2535 connection, class_642 server, class_2720 packet) {
        if (!BloodstoneResourcePack.enabled() || server == null || !PackOfferPolicy.isBloodstoneAddress(server.field_3761)) {
            return false;
        }
        URI address = PackOfferPolicy.remoteUrl(packet.comp_2159());
        if (address == null) {
            return false;
        }
        try {
            URI uRI = address;
            PrivacyGuard.checkUri(uRI);
            URL url = uRI.toURL();
            session.offer(connection, packet.comp_2158());
            Path cached = BloodstonePackPreloader.cached(packet.comp_2160());
            PackAcceptanceScope.accepting(packet.comp_2158(), () -> {
                if (cached != null) {
                    minecraft.method_1516().method_55524(packet.comp_2158(), cached);
                } else {
                    minecraft.method_1516().method_55523(packet.comp_2158(), url, packet.comp_2160());
                }
            });
            return true;
        }
        catch (MalformedURLException invalidUrl) {
            return false;
        }
    }

    public static void feedback(class_2535 connection, UUID id, class_9039.class_9040 result) {
        if (!BloodstoneResourcePack.enabled()) {
            return;
        }
        session.result(connection, id, result == class_9039.class_9040.field_47624);
    }

    public static void pop(class_2535 connection, Optional<UUID> id) {
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

