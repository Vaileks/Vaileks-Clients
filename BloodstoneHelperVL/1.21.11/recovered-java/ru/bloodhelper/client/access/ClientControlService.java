/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.authlib.GameProfile
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 */
package ru.bloodhelper.client.access;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_310;
import net.minecraft.class_746;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.access.ControlCache;
import ru.bloodhelper.client.access.DeviceIdentity;
import ru.bloodhelper.client.access.ServiceJson;
import ru.bloodhelper.client.access.ServiceTransport;
import ru.bloodhelper.client.community.CommunityFeed;
import ru.bloodhelper.client.config.SkinsSettings;
import ru.bloodhelper.client.cosmetic.CosmeticNames;
import ru.bloodhelper.client.cosmetic.CosmeticRoster;
import ru.bloodhelper.client.policy.ClientControlPolicy;
import ru.bloodhelper.client.policy.ControlSnapshot;
import ru.bloodhelper.client.skins.CustomModels;
import ru.bloodhelper.client.skins.SharedSkins;

public final class ClientControlService {
    private static ClientControlService instance;
    private final Path directory;
    private final String label;
    private final ServiceTransport transport = new ServiceTransport();
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "BloodHelper owner controls");
        t.setDaemon(true);
        return t;
    });
    private final AtomicReference<Update> pending = new AtomicReference();
    private final AtomicBoolean retryQueued = new AtomicBoolean();
    private final AtomicBoolean skinQueued = new AtomicBoolean();
    private final AtomicLong skinRevision = new AtomicLong();
    private final SecureRandom random = new SecureRandom();
    private DeviceIdentity identity;
    private String wireUid;
    private volatile boolean closed;
    private volatile long lastRetry;
    private volatile GameIdentity gameIdentity;

    private ClientControlService(Path directory, String label, ControlCache.Cached cache) {
        this.directory = directory;
        this.label = label;
        this.wireUid = cache.wireUid();
    }

    public static void initialize(String string) {
        CommunityFeed.initialize(FabricLoader.getInstance().getConfigDir().resolve("bloodhelper"));
        CustomModels.initialize();
    }

    private void offer(ControlSnapshot device, ControlSnapshot.Global global, CosmeticRoster roster) {
        this.pending.updateAndGet(old -> new Update((ControlSnapshot)(device != null ? device : (old == null ? null : old.device())), (ControlSnapshot.Global)(global != null ? global : (old == null ? null : old.global())), (CosmeticRoster)(roster != null ? roster : (old == null ? null : old.roster()))));
    }

    private void poll() {
    }

    private void pollDevice() {
    }

    private void pollModels() {
        if (this.closed) {
            return;
        }
        try {
            JsonObject models = this.json("GET", "/v1/models?bundled=1", null);
            SharedSkins.accept(models);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void skinChanged() {
        this.skinRevision.incrementAndGet();
        this.queueSkinRefresh();
    }

    private void queueSkinRefresh() {
        if (this.closed || !this.skinQueued.compareAndSet(false, true)) {
            return;
        }
        this.worker.schedule(() -> {
            long revision = this.skinRevision.get();
            try {
                this.pollDevice();
                this.pollModels();
            }
            finally {
                this.skinQueued.set(false);
                if (this.skinRevision.get() != revision) {
                    this.queueSkinRefresh();
                }
            }
        }, 150L, TimeUnit.MILLISECONDS);
    }

    private JsonObject json(String method, String path, JsonObject body) throws IOException {
        return ServiceJson.object(this.transport.request(method, path, null, body, 262144));
    }

    public static boolean tick() {
        boolean changed;
        ControlSnapshot state;
        Update update;
        ClientControlService current = instance;
        if (current == null) {
            return false;
        }
        String previousModel = SharedSkins.selected;
        GameIdentity previousProfile = current.gameIdentity;
        SkinsSettings skins = BloodHelperClient.config().skins;
        SharedSkins.selected = skins.enabled && skins.share && !CustomModels.validId(skins.character) ? skins.character : "NONE";
        class_746 player = class_310.method_1551().field_1724;
        if (player != null) {
            GameProfile profile = player.method_7334();
            if (profile.id() != null && profile.name() != null && profile.name().matches("[A-Za-z0-9_]{3,16}")) {
                current.gameIdentity = new GameIdentity(profile.id().toString(), profile.name());
            }
        } else {
            current.gameIdentity = null;
        }
        if (!previousModel.equals(SharedSkins.selected) || !Objects.equals(previousProfile, current.gameIdentity)) {
            current.skinChanged();
        }
        if ((update = (Update)current.pending.getAndSet(null)) == null) {
            return false;
        }
        long generation = ClientControlPolicy.generation();
        long restrictions = ClientControlPolicy.restrictionGeneration();
        if (update.global() != null) {
            ClientControlPolicy.acceptGlobal(update.global());
        }
        if (update.device() != null) {
            boolean fresh = update.device().revision() >= ClientControlPolicy.snapshot().revision();
            ClientControlPolicy.accept(update.device());
            if (fresh) {
                state = ClientControlPolicy.snapshot();
                CosmeticNames.acceptSelf(state.publicUid(), state.nicknameColor(), state.nicknameGradient(), state.minecraftUuid(), state.minecraftName(), !ClientControlPolicy.enabled());
            }
        }
        if (update.roster() != null) {
            CosmeticNames.acceptRoster(update.roster());
        }
        boolean bl = changed = generation != ClientControlPolicy.generation();
        if (changed && !ClientControlPolicy.cacheDamaged()) {
            state = ClientControlPolicy.snapshot();
            ControlSnapshot.Global global = ClientControlPolicy.global();
            current.worker.execute(() -> {
                try {
                    if (current.identity != null) {
                        ControlCache.save(current.directory, current.identity, current.wireUid, state, global);
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            });
        }
        return restrictions != ClientControlPolicy.restrictionGeneration();
    }

    public static void retry() {
        ClientControlService current = instance;
        if (current == null || current.closed) {
            return;
        }
        long now = System.nanoTime();
        if (now - current.lastRetry < TimeUnit.SECONDS.toNanos(5L) || !current.retryQueued.compareAndSet(false, true)) {
            return;
        }
        current.lastRetry = now;
        current.worker.execute(() -> {
            try {
                current.poll();
            }
            finally {
                current.retryQueued.set(false);
            }
        });
    }

    public static void close() {
        ClientControlService current = instance;
        instance = null;
        if (current == null) {
            return;
        }
        current.closed = true;
        CustomModels.close();
        current.worker.shutdownNow();
        current.transport.close();
        CosmeticNames.clear();
    }

    private static /* synthetic */ void lambda$initialize$0() {
        if (instance != null && ClientControlService.instance.gameIdentity != null) {
            instance.pollModels();
        }
    }

    private record GameIdentity(String uuid, String name) {
    }

    private record Update(ControlSnapshot device, ControlSnapshot.Global global, CosmeticRoster roster) {
    }
}

