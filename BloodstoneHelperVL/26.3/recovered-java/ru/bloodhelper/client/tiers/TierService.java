/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.Entity
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.tiers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.PlayerDisplaySettings;
import ru.bloodhelper.client.modules.StreamerMode;
import ru.bloodhelper.client.tiers.TierProfile;
import ru.bloodhelper.client.vl.PrivacyGuard;

public final class TierService {
    private static final long TTL = TimeUnit.MINUTES.toNanos(15L);
    private static final long RETRY = TimeUnit.MINUTES.toNanos(1L);
    private static final long INTERVAL = TimeUnit.SECONDS.toNanos(1L);
    private static final int MAX_PROFILES = 512;
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "BloodHelper tiers");
        t.setDaemon(true);
        t.setPriority(1);
        return t;
    });
    private static final Map<String, ProviderState> states = new HashMap<String, ProviderState>();
    private static CompletableFuture<Result> pending;
    private static String pendingSource;
    private static long generation;
    private static long pendingGeneration;
    private static Lookup lookup;

    private TierService() {
    }

    public static void tick(Minecraft mc) {
        long now = System.nanoTime();
        if (pending != null && pending.isDone()) {
            Result result;
            try {
                result = pending.join();
            }
            catch (RuntimeException e) {
                result = new Result(pendingSource, "", TierProfile.EMPTY, null, "API", true);
            }
            pending = null;
            pendingSource = "";
            if (pendingGeneration == generation) {
                TierService.apply(result, now);
            }
        }
        if (lookup != null && pending == null && BloodHelperClient.config().serverAllowsModule("otherTiers")) {
            Lookup request = lookup;
            ProviderState state = states.computeIfAbsent(request.source, ignored -> new ProviderState());
            if (now >= state.nextRequest && !TierService.lookupView(request.source, request.player.name).ready()) {
                Cached cached = state.cache.get(request.player.name.toLowerCase(Locale.ROOT));
                if (request.source.equals("CIS")) {
                    TierService.start("CIS", TierService::fetchCis);
                } else if (cached == null || cached.expires <= now) {
                    TierService.start(request.source, () -> TierService.fetch(request.source, request.player));
                }
                state.nextRequest = now + INTERVAL;
            }
        }
        PlayerDisplaySettings.OtherTiers cfg = BloodHelperClient.config().otherTiers;
        if (!cfg.enabled || !cfg.nametags && !cfg.tab || StreamerMode.hideNametags() || mc.player == null || mc.level == null || mc.getConnection() == null || pending != null) {
            return;
        }
        String source = cfg.provider;
        ProviderState state = states.computeIfAbsent(source, ignored -> new ProviderState());
        if (now < state.nextRequest) {
            return;
        }
        state.nextRequest = now + INTERVAL;
        if (source.equals("CIS")) {
            if (now >= state.snapshotExpires) {
                TierService.start(source, TierService::fetchCis);
            }
            return;
        }
        LinkedHashMap<String, Candidate> players = new LinkedHashMap<String, Candidate>();
        GameProfile self = mc.player.getGameProfile();
        TierService.add(players, self.name(), self.id());
        mc.level.players().stream().filter(p -> p != mc.player).sorted(Comparator.comparingDouble(p -> mc.player.distanceToSqr((Entity)p))).limit(64L).forEach(p -> TierService.add(players, p.getGameProfile().name(), p.getGameProfile().id()));
        mc.getConnection().getOnlinePlayers().stream().limit(128L).forEach(p -> TierService.add(players, p.getProfile().name(), p.getProfile().id()));
        for (Map.Entry entry : players.entrySet()) {
            Cached value = state.cache.get(entry.getKey());
            if (value != null && value.expires > now) continue;
            TierService.start(source, () -> TierService.fetch(source, (Candidate)entry.getValue()));
            break;
        }
    }

    private static void add(Map<String, Candidate> players, String name, UUID uuid) {
        if (name != null && name.matches("[A-Za-z0-9_]{1,16}")) {
            players.putIfAbsent(name.toLowerCase(Locale.ROOT), new Candidate(name, uuid));
        }
    }

    public static void openLookup(Object owner, String source, String name) {
        if (!Set.of("CIS", "MC", "PVP", "A", "MY").contains(source) || !name.matches("[A-Za-z0-9_]{1,16}")) {
            return;
        }
        lookup = new Lookup(owner, source, new Candidate(name, null));
    }

    public static void closeLookup(Object owner) {
        if (lookup != null && TierService.lookup.owner == owner) {
            lookup = null;
        }
    }

    public static LookupView lookupView(String source, String name) {
        ProviderState state = states.get(source);
        if (state == null) {
            return new LookupView(TierProfile.EMPTY, false, "");
        }
        long now = System.nanoTime();
        String key = name.toLowerCase(Locale.ROOT);
        if (source.equals("CIS") && state.snapshotExpires > now) {
            return new LookupView(state.snapshot.getOrDefault(key, TierProfile.EMPTY), true, "");
        }
        Cached cached = state.cache.get(key);
        if (!source.equals("CIS") && cached != null && cached.expires > now) {
            return new LookupView(cached.profile, cached.known, cached.known ? "" : cached.error);
        }
        boolean loading = pending != null && pendingSource.equals(source);
        return new LookupView(TierProfile.EMPTY, false, loading || state.status.equals("OK") ? "" : state.status);
    }

    private static void start(String source, Supplier<Result> fetch) {
        pendingSource = source;
        pendingGeneration = generation;
        pending = CompletableFuture.supplyAsync(fetch, WORKER);
    }

    private static void apply(Result result, long now) {
        ProviderState state = states.computeIfAbsent(result.source, ignored -> new ProviderState());
        String string = state.status = result.error.isEmpty() ? "OK" : result.error;
        if (!result.error.isEmpty() && !result.error.equals(state.lastLoggedError)) {
            LoggerFactory.getLogger((String)"BloodHelper tiers").warn("{}: {}", (Object)result.source, (Object)result.error);
            state.lastLoggedError = result.error;
        } else if (result.error.isEmpty()) {
            state.lastLoggedError = "";
        }
        if (result.dump != null) {
            state.snapshot = result.dump;
            state.snapshotExpires = now + TTL;
        } else if (!result.name.isEmpty()) {
            String key = result.name.toLowerCase(Locale.ROOT);
            if (!state.cache.containsKey(key) && state.cache.size() >= 512) {
                state.cache.remove(state.cache.keySet().iterator().next());
            }
            state.cache.put(key, new Cached(result.profile, now + (result.error.isEmpty() ? TTL : RETRY), result.error.isEmpty(), result.error));
        }
        state.nextRequest = now + (result.sourceFailure ? RETRY : INTERVAL);
    }

    public static TierProfile.Badge badge(String name, String mode) {
        Cached value;
        String source = BloodHelperClient.config().otherTiers.provider;
        ProviderState state = states.get(source);
        if (state == null || name == null) {
            return null;
        }
        long now = System.nanoTime();
        String key = name.toLowerCase(Locale.ROOT);
        TierProfile profile = source.equals("CIS") ? (now < state.snapshotExpires ? state.snapshot.get(key) : null) : ((value = state.cache.get(key)) == null || value.expires < now ? null : value.profile);
        return profile == null ? null : profile.select(mode);
    }

    public static String status(boolean ru) {
        String source = BloodHelperClient.config().otherTiers.provider;
        ProviderState state = states.get(source);
        if (pending != null && pendingSource.equals(source)) {
            return ru ? "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0432 \u0444\u043e\u043d\u0435\u2026" : "Loading in background\u2026";
        }
        if (state == null || state.status.isEmpty()) {
            return ru ? "\u041e\u0436\u0438\u0434\u0430\u043d\u0438\u0435 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0438 \u0432 \u043c\u0438\u0440\u0435" : "Waiting for in-world lookup";
        }
        if (state.status.equals("OK")) {
            if (source.equals("CIS")) {
                return (ru ? "\u0417\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u043e \u0438\u0433\u0440\u043e\u043a\u043e\u0432: " : "Players loaded: ") + state.snapshot.size();
            }
            long now = System.nanoTime();
            long checked = state.cache.values().stream().filter(c -> c.known && c.expires > now).count();
            long ranked = state.cache.values().stream().filter(c -> c.known && c.expires > now && !c.profile.ranks().isEmpty()).count();
            return (ru ? "\u041f\u0440\u043e\u0432\u0435\u0440\u0435\u043d\u043e: " : "Checked: ") + checked + (ru ? " \u00b7 \u0441 \u0442\u0438\u0440\u0430\u043c\u0438: " : " \u00b7 ranked: ") + ranked;
        }
        return (ru ? "\u041f\u043e\u0432\u0442\u043e\u0440 \u0437\u0430\u043f\u0440\u043e\u0441\u0430: " : "Lookup will retry: ") + state.status;
    }

    public static String selfStatus(boolean ru) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return "";
        }
        PlayerDisplaySettings.OtherTiers cfg = BloodHelperClient.config().otherTiers;
        String name = mc.player.getGameProfile().name();
        TierProfile.Badge badge = TierService.badge(name, cfg.gamemode);
        if (badge != null) {
            return (ru ? "\u0422\u0432\u043e\u0439 \u0442\u0438\u0440: " : "Your tier: ") + badge.tier() + " \u00b7 " + badge.mode();
        }
        ProviderState state = states.get(cfg.provider);
        long now = System.nanoTime();
        boolean loaded = false;
        if (state != null) {
            Cached cached = state.cache.get(name.toLowerCase(Locale.ROOT));
            boolean bl = cfg.provider.equals("CIS") ? state.snapshotExpires > now : (loaded = cached != null && cached.known && cached.expires > now);
        }
        return loaded ? (ru ? "\u0422\u0432\u043e\u0439 \u0442\u0438\u0440: \u043d\u0435\u0442 \u0434\u0430\u043d\u043d\u044b\u0445 \u0434\u043b\u044f \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u043e\u0433\u043e \u0440\u0435\u0436\u0438\u043c\u0430" : "Your tier: no data for this mode") : (ru ? "\u0422\u0432\u043e\u0439 \u0442\u0438\u0440: \u0435\u0449\u0451 \u043d\u0435 \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d" : "Your tier: not loaded yet");
    }

    private static Result fetch(String source, Candidate player) {
        boolean identityLookup = false;
        try {
            String endpoint;
            if (source.equals("A")) {
                endpoint = "https://api.atiers.net/api/v1/player/" + player.name;
            } else if (source.equals("MY")) {
                endpoint = "https://mytiers.ru/api/players/" + player.name;
            } else {
                Object uuid;
                if (player.uuid != null && player.uuid.version() == 4) {
                    uuid = player.uuid.toString().replace("-", "");
                } else {
                    JsonObject identity;
                    identityLookup = true;
                    try {
                        identity = TierService.get("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + player.name, 32768);
                    }
                    catch (IOException failure) {
                        identity = TierService.get("https://api.mojang.com/users/profiles/minecraft/" + player.name, 32768);
                    }
                    if (identity == null) {
                        return new Result(source, player.name, TierProfile.EMPTY, null, "", false);
                    }
                    JsonElement id = identity.get("id");
                    if (id == null || !id.getAsString().matches("[a-fA-F0-9]{32}")) {
                        throw new IllegalArgumentException("UUID");
                    }
                    uuid = id.getAsString();
                    identityLookup = false;
                }
                if (source.equals("MC")) {
                    uuid = ((String)uuid).substring(0, 8) + "-" + ((String)uuid).substring(8, 12) + "-" + ((String)uuid).substring(12, 16) + "-" + ((String)uuid).substring(16, 20) + "-" + ((String)uuid).substring(20);
                }
                endpoint = (source.equals("MC") ? "https://mctiers.com/api/v2/profile/" : "https://pvptiers.com/api/profile/") + (String)uuid;
            }
            JsonObject data = TierService.get(endpoint, 524288);
            return new Result(source, player.name, data == null ? TierProfile.EMPTY : TierProfile.parse(source, data), null, "", false);
        }
        catch (Exception e) {
            return new Result(source, player.name, TierProfile.EMPTY, null, TierService.error(e), !identityLookup && e instanceof IOException);
        }
    }

    private static Result fetchCis() {
        try {
            JsonObject root = TierService.get("https://cdn.cistiers.com/api/dump.json", 0x800000);
            if (root == null || !root.has("data") || !root.get("data").isJsonObject()) {
                throw new IllegalArgumentException("schema");
            }
            JsonObject data = root.getAsJsonObject("data");
            if (data.size() > 75000) {
                throw new IllegalArgumentException("limit");
            }
            HashMap<String, TierProfile> snapshot = new HashMap<String, TierProfile>();
            for (Map.Entry e : data.entrySet()) {
                TierProfile profile;
                if (!((String)e.getKey()).matches("[A-Za-z0-9_]{1,16}") || !((JsonElement)e.getValue()).isJsonObject() || (profile = TierProfile.parse("CIS", ((JsonElement)e.getValue()).getAsJsonObject())).ranks().isEmpty()) continue;
                snapshot.put(((String)e.getKey()).toLowerCase(Locale.ROOT), profile);
            }
            return new Result("CIS", "", TierProfile.EMPTY, Map.copyOf(snapshot), "", false);
        }
        catch (Exception e) {
            return new Result("CIS", "", TierProfile.EMPTY, null, TierService.error(e), true);
        }
    }

    private static String error(Exception e) {
        String message = e.getMessage();
        if (message != null && message.matches("(?:DNS|timeout|HTTP [0-9]{3}) [a-zA-Z0-9.-]+")) {
            return message;
        }
        return e instanceof UnknownHostException ? "DNS" : (e instanceof SocketTimeoutException ? "timeout" : "API");
    }

    private static JsonObject get(String url, int limit) throws Exception {
        URI uri;
        URI uRI = uri = URI.create(url);
        PrivacyGuard.checkUri(uRI);
        URL uRL = uRI.toURL();
        PrivacyGuard.checkUrlObject(uRL);
        HttpURLConnection c = (HttpURLConnection)uRL.openConnection();
        try {
            JsonObject jsonObject;
            block17: {
                c.setConnectTimeout(4000);
                c.setReadTimeout(5000);
                c.setInstanceFollowRedirects(false);
                c.setUseCaches(false);
                c.setRequestProperty("User-Agent", "BloodHelper/1.3.8 (+https://modrinth.com/mod/bloodhelper; https://github.com/nomielua)");
                c.setRequestProperty("Accept", "application/json");
                int code = c.getResponseCode();
                if (code == 404 || code == 204) {
                    JsonObject jsonObject2 = null;
                    return jsonObject2;
                }
                if (code != 200) {
                    throw new IOException("HTTP " + code + " " + uri.getHost());
                }
                if (c.getContentLengthLong() > (long)limit) {
                    throw new IOException("limit");
                }
                InputStream stream = c.getInputStream();
                try {
                    byte[] bytes = stream.readNBytes(limit + 1);
                    if (bytes.length > limit) {
                        throw new IOException("limit");
                    }
                    jsonObject = JsonParser.parseString((String)new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
                    if (stream == null) break block17;
                }
                catch (Throwable throwable) {
                    try {
                        if (stream != null) {
                            try {
                                stream.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (UnknownHostException e) {
                        throw new IOException("DNS " + uri.getHost(), e);
                    }
                    catch (SocketTimeoutException e) {
                        throw new IOException("timeout " + uri.getHost(), e);
                    }
                }
                stream.close();
            }
            return jsonObject;
        }
        finally {
            c.disconnect();
        }
    }

    public static void reset() {
        ++generation;
        states.clear();
        lookup = null;
    }

    static {
        pendingSource = "";
    }

    private record Result(String source, String name, TierProfile profile, Map<String, TierProfile> dump, String error, boolean sourceFailure) {
    }

    private record Lookup(Object owner, String source, Candidate player) {
    }

    private static final class ProviderState {
        final Map<String, Cached> cache = new LinkedHashMap<String, Cached>();
        Map<String, TierProfile> snapshot = Map.of();
        long snapshotExpires;
        long nextRequest;
        String status = "";
        String lastLoggedError = "";

        private ProviderState() {
        }
    }

    private record Candidate(String name, UUID uuid) {
    }

    public record LookupView(TierProfile profile, boolean ready, String error) {
    }

    private record Cached(TierProfile profile, long expires, boolean known, String error) {
    }
}

