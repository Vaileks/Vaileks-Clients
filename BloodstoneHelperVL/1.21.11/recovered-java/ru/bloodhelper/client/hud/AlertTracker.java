/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.bloodhelper.client.hud.NotificationPolicy;

public final class AlertTracker {
    private final Map<String, Observation> observations = new HashMap<String, Observation>();
    private final List<Notice> notices = new ArrayList<Notice>();

    public boolean observe(String key, double value, double threshold) {
        if (key == null || !Double.isFinite(value) || !Double.isFinite(threshold)) {
            this.observations.remove(key);
            return false;
        }
        Observation previous = this.observations.put(key, new Observation(value, threshold));
        return previous != null && Double.compare(previous.threshold, threshold) == 0 && previous.value > threshold && value <= threshold && value < previous.value;
    }

    public void retainObservations(Set<String> present) {
        this.observations.keySet().retainAll(present);
    }

    public void dismiss(String key) {
        this.notices.removeIf(notice -> notice.key.equals(key));
    }

    public boolean push(String key, String title, String body, String icon, long now, long durationMs, int limit) {
        this.expire(now, limit);
        if (key == null || durationMs <= 0L || limit <= 0) {
            return false;
        }
        for (int i = 0; i < this.notices.size(); ++i) {
            Notice current = this.notices.get(i);
            if (!current.key.equals(key)) continue;
            this.notices.set(i, new Notice(key, title, body, icon, current.createdAt, current.expiresAt));
            return false;
        }
        long expires = now > Long.MAX_VALUE - durationMs ? Long.MAX_VALUE : now + durationMs;
        this.notices.add(new Notice(key, title, body, icon, now, expires));
        this.trim(limit);
        return true;
    }

    public void expire(long now, int limit) {
        this.notices.removeIf(notice -> now >= notice.expiresAt);
        this.trim(limit);
    }

    public List<Notice> snapshot(long now) {
        this.notices.removeIf(notice -> now >= notice.expiresAt);
        return List.copyOf(this.notices);
    }

    private void trim(int limit) {
        int boundedLimit = Math.max(0, Math.min(16, limit));
        while (this.notices.size() > boundedLimit) {
            int discard = 0;
            for (int i = 1; i < this.notices.size(); ++i) {
                if (NotificationPolicy.priority(this.notices.get(i)) >= NotificationPolicy.priority(this.notices.get(discard))) continue;
                discard = i;
            }
            this.notices.remove(discard);
        }
    }

    public void clear() {
        this.observations.clear();
        this.notices.clear();
    }

    private record Observation(double value, double threshold) {
    }

    public record Notice(String key, String title, String body, String icon, long createdAt, long expiresAt) {
        public float remaining(long now) {
            if (this.expiresAt <= this.createdAt) {
                return 0.0f;
            }
            return (float)Math.max(0.0, Math.min(1.0, (double)(this.expiresAt - now) / (double)(this.expiresAt - this.createdAt)));
        }
    }
}

