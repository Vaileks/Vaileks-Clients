/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import ru.bloodhelper.client.hud.AlertTracker;

public final class NotificationPolicy {
    public static final int MAX_PENDING = 16;
    private static final long EFFECT_BATCH_MS = 750L;

    private NotificationPolicy() {
    }

    public static int priority(AlertTracker.Notice notice) {
        String key = notice.key();
        if (key.startsWith("armor:")) {
            return 4;
        }
        if (key.startsWith("item:")) {
            return 3;
        }
        if (key.startsWith("feature:cooldownReady:") || key.startsWith("feature:chatMentions:")) {
            return 2;
        }
        if (key.endsWith(":expires") || key.startsWith("effects:expires:")) {
            return 1;
        }
        return 0;
    }

    public static List<AlertTracker.Notice> select(List<AlertTracker.Notice> source, int limit, String language) {
        List<AlertTracker.Notice> ordered = source.stream().sorted(Comparator.comparingLong(AlertTracker.Notice::createdAt).thenComparing(AlertTracker.Notice::key)).toList();
        ArrayList<AlertTracker.Notice> cards = new ArrayList<AlertTracker.Notice>();
        ArrayList<AlertTracker.Notice> batch = new ArrayList<AlertTracker.Notice>();
        for (AlertTracker.Notice notice : ordered) {
            if (!notice.key().startsWith("effect:") || !notice.key().endsWith(":expires")) {
                cards.add(notice);
                continue;
            }
            if (!batch.isEmpty() && notice.createdAt() - batch.getFirst().createdAt() > 750L) {
                cards.add(NotificationPolicy.combine(batch, language));
                batch.clear();
            }
            batch.add(notice);
        }
        if (!batch.isEmpty()) {
            cards.add(NotificationPolicy.combine(batch, language));
        }
        cards.sort(Comparator.comparingInt(NotificationPolicy::priority).reversed().thenComparing(Comparator.comparingLong(AlertTracker.Notice::createdAt).reversed()).thenComparing(AlertTracker.Notice::key));
        return List.copyOf(cards.subList(0, Math.clamp((long)limit, 0, Math.min(4, cards.size()))));
    }

    private static AlertTracker.Notice combine(List<AlertTracker.Notice> batch, String language) {
        if (batch.size() == 1) {
            return batch.getFirst();
        }
        AlertTracker.Notice first = batch.getFirst();
        String names = batch.stream().map(AlertTracker.Notice::title).distinct().collect(Collectors.joining(", "));
        boolean sameTime = batch.stream().allMatch(n -> n.body().equals(first.body()));
        String title = sameTime ? names : ("ru".equals(language) ? "\u042d\u0444\u0444\u0435\u043a\u0442\u044b" : "Effects");
        String body = sameTime ? first.body() : batch.stream().map(n -> n.title() + " \u2014 " + n.body()).collect(Collectors.joining("; "));
        return new AlertTracker.Notice("effects:expires:" + first.key(), title, body, "effects", first.createdAt(), batch.stream().mapToLong(AlertTracker.Notice::expiresAt).min().orElse(first.expiresAt()));
    }
}

