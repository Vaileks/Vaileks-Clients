/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_634
 *  net.minecraft.class_642
 */
package ru.bloodhelper.client.policy;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_642;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.policy.ClientControlPolicy;
import ru.bloodhelper.client.policy.ModulePolicy;
import ru.bloodhelper.client.policy.PolicyLease;
import ru.bloodhelper.client.policy.RuntimeConfig;
import ru.bloodhelper.client.policy.ServerModuleRule;
import ru.bloodhelper.client.vl.PrivacyGuard;

public final class RemoteModulePolicy {
    public static final String URL = "https://raw.githubusercontent.com/nomielua/bloodhelper-policy/main/policy-v2.json";
    public static final String RELEASES = "https://modrinth.com/mod/bloodhelper/versions";
    private static final long POLL = TimeUnit.MINUTES.toNanos(5L);
    private static final long RETRY = TimeUnit.SECONDS.toNanos(60L);
    private static final long LEASE = TimeUnit.MINUTES.toNanos(6L);
    private static boolean started;
    private static PolicyLease lease;
    private static long nextPoll;
    private static long generation;
    private static long notifiedGeneration;
    private static String serverAddress;
    private static Map<String, ServerModuleRule> serverRules;
    private static CompletableFuture<ModulePolicy> pending;
    private static final ExecutorService WORKER;
    private static ClientConfig cached;
    private static ClientConfig source;
    private static long cachedGeneration;
    private static long cachedConfig;
    private static long cachedControl;

    private RemoteModulePolicy() {
    }

    public static void initialize() {
        started = true;
        lease = new PolicyLease(LEASE);
        nextPoll = 0L;
        ++generation;
        serverRules = ServerModuleRule.DEFAULTS;
        serverAddress = null;
    }

    private static void refreshServerAddress() {
        class_310 mc = class_310.method_1551();
        class_634 connection = mc.method_1562();
        class_642 server = mc.method_1558();
        String address = ServerModuleRule.contextAddress(mc.field_1687 != null && mc.field_1724 != null && connection != null, mc.method_1576() != null || connection != null && connection.method_48296().method_10756(), server == null ? null : server.field_3761);
        if (!Objects.equals(serverAddress, address)) {
            serverAddress = address;
            ++generation;
        }
    }

    public static boolean tick() {
        if (!started) {
            return false;
        }
        RemoteModulePolicy.refreshServerAddress();
        long now = System.nanoTime();
        if (pending != null && pending.isDone()) {
            ModulePolicy fetched;
            try {
                fetched = pending.join();
            }
            catch (RuntimeException error) {
                fetched = null;
            }
            pending = null;
            if (lease.accept(fetched, now)) {
                ++generation;
            }
            if (lease.current() != null) {
                serverRules = lease.current().serverRules();
            }
            nextPoll = now + (lease.current() == null ? RETRY : POLL);
        }
        if (lease.expire(now)) {
            ++generation;
        }
        if (pending == null && now >= nextPoll) {
            pending = CompletableFuture.supplyAsync(RemoteModulePolicy::fetch, WORKER);
        }
        boolean changed = notifiedGeneration != generation;
        notifiedGeneration = generation;
        return changed;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ModulePolicy fetch() {
        HttpURLConnection connection = null;
        try {
            ModulePolicy modulePolicy;
            block18: {
                byte[] bytes;
                InputStream input;
                block16: {
                    ModulePolicy modulePolicy2;
                    block17: {
                        URI uRI = URI.create(URL);
                        PrivacyGuard.checkUri(uRI);
                        URL uRL = uRI.toURL();
                        PrivacyGuard.checkUrlObject(uRL);
                        connection = (HttpURLConnection)uRL.openConnection();
                        connection.setInstanceFollowRedirects(false);
                        connection.setConnectTimeout(4000);
                        connection.setReadTimeout(4000);
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setRequestProperty("User-Agent", "BloodHelper-Policy/1");
                        connection.setUseCaches(false);
                        if (connection.getResponseCode() != 200 || connection.getContentLengthLong() > 32768L) {
                            ModulePolicy modulePolicy3 = null;
                            return modulePolicy3;
                        }
                        input = connection.getInputStream();
                        try {
                            bytes = input.readNBytes(32769);
                            if (bytes.length <= 32768) break block16;
                            modulePolicy2 = null;
                            if (input == null) break block17;
                        }
                        catch (Throwable throwable) {
                            try {
                                if (input != null) {
                                    try {
                                        input.close();
                                    }
                                    catch (Throwable throwable2) {
                                        throwable.addSuppressed(throwable2);
                                    }
                                }
                                throw throwable;
                            }
                            catch (Exception error) {
                                ModulePolicy modulePolicy4 = null;
                                return modulePolicy4;
                            }
                        }
                        input.close();
                    }
                    return modulePolicy2;
                }
                modulePolicy = ModulePolicy.parse(new String(bytes, StandardCharsets.UTF_8));
                if (input == null) break block18;
                input.close();
            }
            return modulePolicy;
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static boolean fallback() {
        return started && lease.current() == null;
    }

    public static boolean allows(String id, boolean localSafe) {
        if (!ModulePolicy.MODULES.contains(id)) {
            return false;
        }
        if (started) {
            RemoteModulePolicy.refreshServerAddress();
        }
        boolean global = lease.current() == null ? !localSafe && !RemoteModulePolicy.fallback() || !ModulePolicy.SAFE_BLOCKS.contains(id) : lease.current().allows(id, localSafe);
        return global && ClientControlPolicy.allows(id) && (!started || ServerModuleRule.allows(serverRules, id, serverAddress));
    }

    public static ClientConfig runtime() {
        ClientConfig saved = ConfigStore.get();
        if (!started) {
            return saved;
        }
        RemoteModulePolicy.refreshServerAddress();
        if (cached == null || source != saved || cachedGeneration != generation || cachedControl != ClientControlPolicy.restrictionGeneration()) {
            source = saved;
            cachedGeneration = generation;
            cachedControl = ClientControlPolicy.restrictionGeneration();
            cachedConfig = ConfigStore.revision();
            cached = new ClientConfig();
            RuntimeConfig.refresh(cached, saved, lease.current(), RemoteModulePolicy.fallback(), serverAddress, serverRules, ClientControlPolicy::allows, !ClientControlPolicy.enabled());
        } else if (cachedConfig != ConfigStore.revision()) {
            cachedConfig = ConfigStore.revision();
            RuntimeConfig.refresh(cached, saved, lease.current(), RemoteModulePolicy.fallback(), serverAddress, serverRules, ClientControlPolicy::allows, !ClientControlPolicy.enabled());
        }
        return cached;
    }

    public static String status(boolean russian) {
        if (!ClientControlPolicy.enabled()) {
            return ClientControlPolicy.status(russian);
        }
        if (RemoteModulePolicy.fallback()) {
            return russian ? "GitHub \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d / \u043f\u0440\u043e\u0432\u0435\u0440\u044f\u0435\u0442\u0441\u044f \u2014 Safe Mode" : "GitHub unavailable / checking \u2014 Safe Mode";
        }
        if (lease.current() == null) {
            return russian ? "\u041f\u0440\u0430\u0432\u0438\u043b\u0430 \u0435\u0449\u0451 \u043d\u0435 \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u044b" : "Policy has not loaded";
        }
        return (russian ? "\u041f\u0440\u0430\u0432\u0438\u043b\u0430 GitHub: r" : "GitHub policy: r") + lease.current().revision() + (lease.current().safeMode() || ConfigStore.get().serverSafeMode ? " \u00b7 Safe Mode" : "");
    }

    public static String restrictionReason(String id, boolean russian) {
        if (!ClientControlPolicy.allows(id)) {
            return ClientControlPolicy.status(russian);
        }
        if (started) {
            RemoteModulePolicy.refreshServerAddress();
        }
        if (started && !ServerModuleRule.allows(serverRules, id, serverAddress)) {
            return russian ? "\u0417\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e \u043d\u0430 \u044d\u0442\u043e\u043c \u0441\u0435\u0440\u0432\u0435\u0440\u0435" : "Not allowed on this server";
        }
        return russian ? "\u041e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u043e \u043f\u0440\u0430\u0432\u0438\u043b\u0430\u043c\u0438 \u0438\u043b\u0438 Safe Mode" : "Disabled by policy or Safe Mode";
    }

    public static String update(String gameVersion, String installed) {
        String candidate = lease.current() == null ? null : lease.current().updates().get(gameVersion);
        return ModulePolicy.newer(candidate, installed) ? candidate : "";
    }

    static {
        lease = new PolicyLease(LEASE);
        notifiedGeneration = -1L;
        serverRules = ServerModuleRule.DEFAULTS;
        WORKER = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "BloodHelper module policy");
            t.setDaemon(true);
            t.setPriority(1);
            return t;
        });
        cachedGeneration = -1L;
        cachedConfig = -1L;
        cachedControl = -1L;
    }
}

