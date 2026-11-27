/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.cosmetic;

import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.LongSupplier;
import ru.bloodhelper.client.cosmetic.CosmeticRoster;
import ru.bloodhelper.client.cosmetic.NicknamePalette;

final class CosmeticCache {
    static final long LEASE_NANOS = Duration.ofMinutes(3L).toNanos();
    private final LongSupplier clock;
    private volatile RosterSnapshot roster;
    private volatile Self self;

    CosmeticCache(LongSupplier clock) {
        this.clock = clock;
    }

    synchronized void acceptRoster(CosmeticRoster value) {
        if (value == null) {
            return;
        }
        RosterSnapshot old = this.roster;
        if (old == null || value.revision() >= old.roster.revision()) {
            this.roster = new RosterSnapshot(value, this.clock.getAsLong());
        }
    }

    synchronized void acceptSelf(String uid, String color, String uuid, String name, boolean disabled) {
        this.acceptSelf(uid, color, null, uuid, name, disabled);
    }

    synchronized void acceptSelf(String uid, String color, NicknamePalette palette, String uuid, String name, boolean disabled) {
        String publicUid = CosmeticCache.validUid(uid) ? uid : "";
        UUID boundUuid = CosmeticRoster.canonicalUuid(uuid);
        OptionalInt rgb = CosmeticRoster.parseColor(color);
        boolean bound = !publicUid.isEmpty() && boundUuid != null && CosmeticRoster.validName(name);
        this.self = new Self(publicUid, bound ? boundUuid : null, bound ? name : null, rgb, palette != null ? palette : (rgb.isPresent() ? NicknamePalette.solid(rgb.getAsInt()) : null), disabled, this.clock.getAsLong());
    }

    OptionalInt color(UUID uuid, String name, boolean privacyHidden) {
        if (privacyHidden || !CosmeticRoster.validName(name)) {
            return OptionalInt.empty();
        }
        Self own = this.self;
        if (own != null && own.disabled) {
            return OptionalInt.empty();
        }
        RosterSnapshot publicRoster = this.roster;
        if (own != null && own.matches(uuid, name) && this.fresh(own.received) && (publicRoster == null || own.received >= publicRoster.received)) {
            return own.color;
        }
        return publicRoster != null && this.fresh(publicRoster.received) ? publicRoster.roster.color(uuid, name) : OptionalInt.empty();
    }

    Optional<NicknamePalette> palette(UUID uuid, String name, boolean privacyHidden) {
        if (privacyHidden || !CosmeticRoster.validName(name)) {
            return Optional.empty();
        }
        Self own = this.self;
        if (own != null && own.disabled) {
            return Optional.empty();
        }
        RosterSnapshot publicRoster = this.roster;
        if (own != null && own.matches(uuid, name) && this.fresh(own.received) && (publicRoster == null || own.received >= publicRoster.received)) {
            return Optional.ofNullable(own.palette);
        }
        return publicRoster != null && this.fresh(publicRoster.received) ? publicRoster.roster.palette(uuid, name) : Optional.empty();
    }

    String uidLabel(boolean privacyHidden) {
        Self own = this.self;
        return "UID: " + (privacyHidden || own == null || own.publicUid.isEmpty() ? "\u2014" : own.publicUid);
    }

    synchronized void clear() {
        this.roster = null;
        this.self = null;
    }

    private boolean fresh(long received) {
        long age = this.clock.getAsLong() - received;
        return age >= 0L && age < LEASE_NANOS;
    }

    static boolean validUid(String value) {
        return value != null && value.matches("[1-9][0-9]{0,11}");
    }

    private record RosterSnapshot(CosmeticRoster roster, long received) {
    }

    private record Self(String publicUid, UUID uuid, String name, OptionalInt color, NicknamePalette palette, boolean disabled, long received) {
        boolean matches(UUID candidateUuid, String candidateName) {
            return this.uuid != null && candidateUuid != null && this.uuid.equals(candidateUuid) && this.name != null && this.name.equalsIgnoreCase(candidateName);
        }
    }
}

