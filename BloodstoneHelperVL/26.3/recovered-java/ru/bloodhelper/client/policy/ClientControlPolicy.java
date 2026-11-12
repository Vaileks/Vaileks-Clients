/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.policy;

import java.util.HashSet;
import java.util.Set;
import ru.bloodhelper.client.policy.ControlSnapshot;
import ru.bloodhelper.client.policy.ModulePolicy;

public final class ClientControlPolicy {
    private static volatile ControlSnapshot snapshot = ControlSnapshot.empty();
    private static volatile ControlSnapshot.Global global = ControlSnapshot.Global.empty();
    private static volatile boolean damagedCache;
    private static volatile long generation;
    private static volatile long restrictionGeneration;
    private static Restrictions lastRestrictions;

    private static void changedRestrictions() {
        HashSet<String> denied = new HashSet<String>(global.disabledModules());
        denied.addAll(snapshot.deviceDisabledModules());
        Restrictions next = new Restrictions(!ClientControlPolicy.enabled(), Set.copyOf(denied));
        if (!lastRestrictions.equals(next)) {
            lastRestrictions = next;
            ++restrictionGeneration;
        }
    }

    public static long restrictionGeneration() {
        return restrictionGeneration;
    }

    private ClientControlPolicy() {
    }

    public static ControlSnapshot snapshot() {
        return snapshot;
    }

    public static ControlSnapshot.Global global() {
        return global;
    }

    public static boolean cacheDamaged() {
        return damagedCache;
    }

    public static long generation() {
        return generation;
    }

    public static String publicUid() {
        return snapshot.publicUid();
    }

    public static boolean enabled() {
        return !damagedCache && !snapshot.clientDisabled();
    }

    public static boolean allows(String id) {
        return ClientControlPolicy.enabled() && ModulePolicy.MODULES.contains(id) && !global.disabledModules().contains(id) && !snapshot.deviceDisabledModules().contains(id);
    }

    public static boolean accept(ControlSnapshot next) {
        if (next == null || next.revision() < snapshot.revision()) {
            return false;
        }
        boolean changed = damagedCache || !snapshot.equals(next);
        snapshot = next;
        damagedCache = false;
        if (changed |= ClientControlPolicy.acceptGlobal(new ControlSnapshot.Global(next.revision(), next.globalDisabledModules(), next.generatedAt(), global.announcement()))) {
            ++generation;
        }
        ClientControlPolicy.changedRestrictions();
        return changed;
    }

    public static boolean acceptGlobal(ControlSnapshot.Global next) {
        if (next == null || next.revision() < global.revision() || next.equals(global)) {
            return false;
        }
        global = next;
        ++generation;
        ClientControlPolicy.changedRestrictions();
        return true;
    }

    public static void restore(ControlSnapshot cached, ControlSnapshot.Global cachedGlobal, boolean damaged) {
        snapshot = cached == null ? ControlSnapshot.empty() : cached;
        ControlSnapshot.Global global = ClientControlPolicy.global = cachedGlobal == null ? ControlSnapshot.Global.empty() : cachedGlobal;
        if (snapshot.revision() > ClientControlPolicy.global.revision()) {
            ClientControlPolicy.global = new ControlSnapshot.Global(snapshot.revision(), snapshot.globalDisabledModules(), snapshot.generatedAt(), ClientControlPolicy.global.announcement());
        }
        damagedCache = damaged;
        ++generation;
        ClientControlPolicy.changedRestrictions();
    }

    public static String status(boolean russian) {
        if (damagedCache) {
            return russian ? "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0432\u0435\u0440\u0438\u0442\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d\u043d\u044b\u0439 \u0441\u0442\u0430\u0442\u0443\u0441 \u00b7 \u043e\u0436\u0438\u0434\u0430\u0435\u043c \u0441\u0435\u0440\u0432\u0438\u0441" : "Saved status could not be verified \u00b7 waiting for service";
        }
        if (!ClientControlPolicy.enabled()) {
            return russian ? "BloodHelper \u043e\u0442\u043a\u043b\u044e\u0447\u0451\u043d \u0432\u043b\u0430\u0434\u0435\u043b\u044c\u0446\u0435\u043c \u0441\u0435\u0440\u0432\u0438\u0441\u0430" : "BloodHelper disabled by the service owner";
        }
        return russian ? "\u041e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u043e \u0432\u043b\u0430\u0434\u0435\u043b\u044c\u0446\u0435\u043c \u0441\u0435\u0440\u0432\u0438\u0441\u0430" : "Disabled by the service owner";
    }

    static {
        lastRestrictions = new Restrictions(false, Set.of());
    }

    private record Restrictions(boolean disabled, Set<String> denied) {
    }
}

