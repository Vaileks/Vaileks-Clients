/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CombatTimelineRecorder {
    public static final int MAX_EVENTS = 100;
    private static final int MAX_EFFECTS = 256;
    private final ArrayDeque<TimedEvent> events = new ArrayDeque();
    private final Map<String, EffectState> effects = new LinkedHashMap<String, EffectState>();
    private final Map<String, EffectState> nextEffects = new LinkedHashMap<String, EffectState>();
    private long windowMillis;
    private long lastAt;
    private float health;
    private float absorption;
    private boolean active;
    private boolean effectsInitialized;
    private boolean healthEnabled;
    private boolean effectsEnabled;
    private boolean totemsEnabled;
    private Object lastTotemNotification;
    private List<Event> frozen;

    public void begin(long now, float health, float absorption, int seconds, boolean healthEnabled, boolean effectsEnabled, boolean totemsEnabled) {
        this.reset();
        this.windowMillis = (long)Math.clamp((long)seconds, 10, 60) * 1000L;
        this.lastAt = now;
        this.health = CombatTimelineRecorder.safe(health);
        this.absorption = CombatTimelineRecorder.safe(absorption);
        this.healthEnabled = healthEnabled;
        this.effectsEnabled = effectsEnabled;
        this.totemsEnabled = totemsEnabled;
        this.active = true;
    }

    public void health(long now, float currentHealth, float currentAbsorption) {
        if (!this.active) {
            return;
        }
        now = this.advance(now);
        if (Float.isFinite(currentHealth)) {
            currentHealth = CombatTimelineRecorder.safe(currentHealth);
            if (this.healthEnabled && Math.abs(currentHealth - this.health) > 0.001f) {
                this.add(now, currentHealth < this.health ? Kind.HEALTH_LOSS : Kind.HEALTH_GAIN, this.health, currentHealth, "", 0, 0, false);
            }
            this.health = currentHealth;
        }
        if (Float.isFinite(currentAbsorption)) {
            currentAbsorption = CombatTimelineRecorder.safe(currentAbsorption);
            if (this.healthEnabled && Math.abs(currentAbsorption - this.absorption) > 0.001f) {
                this.add(now, currentAbsorption < this.absorption ? Kind.ABSORPTION_LOSS : Kind.ABSORPTION_GAIN, this.absorption, currentAbsorption, "", 0, 0, false);
            }
            this.absorption = currentAbsorption;
        }
    }

    public void effects(long now, Iterable<EffectState> observed, boolean serverRemoval) {
        if (!this.active || !this.effectsEnabled) {
            return;
        }
        now = this.advance(now);
        this.nextEffects.clear();
        for (EffectState effect : observed) {
            if (effect == null || effect.key() == null || effect.key().isBlank() || effect.key().length() > 256 || this.nextEffects.size() >= 256) continue;
            this.nextEffects.put(effect.key(), effect);
        }
        if (this.effectsInitialized) {
            for (EffectState effect : this.nextEffects.values()) {
                EffectState old = this.effects.get(effect.key());
                if (old == null) {
                    this.add(now, Kind.EFFECT_GAIN, 0.0f, 0.0f, effect.key(), 0, effect.level(), false);
                    continue;
                }
                if (old.level() == effect.level()) continue;
                this.add(now, Kind.EFFECT_LEVEL, 0.0f, 0.0f, effect.key(), old.level(), effect.level(), false);
            }
            for (EffectState old : this.effects.values()) {
                if (this.nextEffects.containsKey(old.key())) continue;
                this.add(now, Kind.EFFECT_LOSS, 0.0f, 0.0f, old.key(), old.level(), 0, !serverRemoval && !old.infinite() && old.ticks() <= 1);
            }
        }
        this.effects.clear();
        this.effects.putAll(this.nextEffects);
        this.effectsInitialized = true;
    }

    public void totem(long now, Object notification) {
        if (!this.active || !this.totemsEnabled || notification == null || notification == this.lastTotemNotification) {
            return;
        }
        this.lastTotemNotification = notification;
        this.add(this.advance(now), Kind.TOTEM, 0.0f, 0.0f, "", 0, 0, false);
    }

    public List<Event> freeze(long now) {
        if (this.frozen != null) {
            return this.frozen;
        }
        if (!this.active) {
            return List.of();
        }
        now = this.advance(now);
        this.add(now, Kind.DEATH, 0.0f, 0.0f, "", 0, 0, false);
        ArrayList<Event> result = new ArrayList<Event>(this.events.size());
        for (TimedEvent event : this.events) {
            result.add(new Event(Math.max(0L, now - event.at()), event.kind(), event.before(), event.after(), event.effectKey(), event.beforeLevel(), event.afterLevel(), event.observedExpiry()));
        }
        this.frozen = List.copyOf(result);
        this.active = false;
        this.events.clear();
        this.effects.clear();
        this.nextEffects.clear();
        this.lastTotemNotification = null;
        return this.frozen;
    }

    public void reset() {
        this.events.clear();
        this.effects.clear();
        this.nextEffects.clear();
        this.frozen = null;
        this.active = false;
        this.effectsInitialized = false;
        this.lastTotemNotification = null;
    }

    public boolean active() {
        return this.active;
    }

    public int retainedEvents() {
        return this.events.size();
    }

    private long advance(long now) {
        this.lastAt = Math.max(this.lastAt, now);
        while (!this.events.isEmpty() && this.lastAt - this.events.peekFirst().at() > this.windowMillis) {
            this.events.removeFirst();
        }
        return this.lastAt;
    }

    private void add(long now, Kind kind, float before, float after, String key, int oldLevel, int newLevel, boolean expiry) {
        while (this.events.size() >= 100) {
            this.events.removeFirst();
        }
        this.events.addLast(new TimedEvent(now, kind, before, after, key, oldLevel, newLevel, expiry));
    }

    private static float safe(float value) {
        return Float.isFinite(value) ? Math.max(0.0f, value) : 0.0f;
    }

    public static enum Kind {
        HEALTH_LOSS,
        HEALTH_GAIN,
        ABSORPTION_LOSS,
        ABSORPTION_GAIN,
        EFFECT_GAIN,
        EFFECT_LOSS,
        EFFECT_LEVEL,
        TOTEM,
        DEATH;

    }

    public record EffectState(String key, int level, int ticks, boolean infinite) {
    }

    private record TimedEvent(long at, Kind kind, float before, float after, String effectKey, int beforeLevel, int afterLevel, boolean observedExpiry) {
    }

    public record Event(long beforeDeathMillis, Kind kind, float before, float after, String effectKey, int beforeLevel, int afterLevel, boolean observedExpiry) {
    }
}

