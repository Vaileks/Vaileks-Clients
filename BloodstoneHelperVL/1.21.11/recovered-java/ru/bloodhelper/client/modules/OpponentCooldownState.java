/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

final class OpponentCooldownState<T> {
    static final int MAX_PLAYERS = 64;
    static final int MAX_ABILITIES_PER_PLAYER = 32;
    static final long MAX_DURATION_TICKS = 51840000L;
    private static final long NANOS_PER_TICK = 50000000L;
    private static final long NANOS_PER_SECOND = 1000000000L;
    private final Map<PlayerKey, LinkedHashMap<AbilityKey, Observation<T>>> players = new LinkedHashMap<PlayerKey, LinkedHashMap<AbilityKey, Observation<T>>>();
    private final UnaryOperator<T> copy;
    private Object world;

    OpponentCooldownState(UnaryOperator<T> copy) {
        this.copy = Objects.requireNonNull(copy);
    }

    void world(Object nextWorld) {
        if (this.world != nextWorld) {
            this.players.clear();
            this.world = nextWorld;
        }
    }

    void clear() {
        this.players.clear();
        this.world = null;
    }

    void retainPlayers(Set<PlayerKey> aliveAndPresent) {
        this.players.keySet().removeIf(key -> !aliveAndPresent.contains(key));
    }

    void forgetPlayer(PlayerKey player) {
        this.players.remove(player);
    }

    void observe(PlayerKey player, String itemKey, String abilityKey, long stamp, long epochMillis, long nowNanos, T value) {
        long remainingTicks;
        Observation<T> previous;
        if (this.world == null || player == null || player.uuid() == null || player.entityId() < 0 || value == null || itemKey == null || itemKey.isEmpty() || itemKey.length() > 300 || abilityKey == null || abilityKey.isEmpty() || abilityKey.length() > 160 || stamp <= 0L || epochMillis < 0L) {
            return;
        }
        long nowTicks = epochMillis / 50L;
        if (stamp > nowTicks && stamp - nowTicks > 51840000L) {
            return;
        }
        AbilityKey ability = new AbilityKey(itemKey, abilityKey);
        LinkedHashMap<AbilityKey, Observation<Object>> entries = this.players.get(player);
        Observation<T> observation = previous = entries == null ? null : entries.get(ability);
        if (previous != null && stamp <= previous.stamp()) {
            return;
        }
        long l = remainingTicks = stamp > nowTicks ? stamp - nowTicks : 0L;
        if (remainingTicks == 0L && previous == null) {
            return;
        }
        if (entries == null) {
            this.players.keySet().removeIf(key -> key.uuid().equals(player.uuid()) || key.entityId() == player.entityId());
            if (this.players.size() >= 64) {
                this.players.remove(this.players.keySet().iterator().next());
            }
            entries = new LinkedHashMap();
            this.players.put(player, entries);
        }
        if (!entries.containsKey(ability) && entries.size() >= 32) {
            AbilityKey expired = entries.entrySet().stream().filter(entry -> OpponentCooldownState.remainingNanos((Observation)entry.getValue(), nowNanos) == 0L).map(Map.Entry::getKey).findFirst().orElse(entries.keySet().iterator().next());
            entries.remove(expired);
        }
        entries.put(ability, new Observation(stamp, nowNanos, remainingTicks * 50000000L, this.copy.apply(value)));
    }

    List<Remaining<T>> snapshot(PlayerKey player, long nowNanos) {
        LinkedHashMap<AbilityKey, Observation<T>> entries = this.players.get(player);
        if (entries == null) {
            return List.of();
        }
        ArrayList result = new ArrayList();
        for (Observation<T> observation : entries.values()) {
            long nanos = OpponentCooldownState.remainingNanos(observation, nowNanos);
            if (nanos <= 0L) continue;
            result.add(new Remaining(this.copy.apply(observation.value()), (int)((nanos + 1000000000L - 1L) / 1000000000L)));
        }
        return List.copyOf(result);
    }

    private static long remainingNanos(Observation<?> observation, long nowNanos) {
        long elapsed = nowNanos - observation.startedNanos();
        return observation.durationNanos() - Math.min(observation.durationNanos(), Math.max(0L, elapsed));
    }

    int playerCount() {
        return this.players.size();
    }

    int abilityCount(PlayerKey key) {
        return this.players.containsKey(key) ? this.players.get(key).size() : 0;
    }

    record PlayerKey(UUID uuid, int entityId) {
    }

    private record AbilityKey(String itemKey, String abilityKey) {
    }

    private record Observation<T>(long stamp, long startedNanos, long durationNanos, T value) {
    }

    record Remaining<T>(T value, int seconds) {
    }
}

