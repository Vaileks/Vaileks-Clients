/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import java.util.Set;

public record MediaFavoriteState(String provider, boolean available, boolean saved, long requestId, boolean accepted, String status) {
    private static final Set<String> PROVIDERS = Set.of("spotify", "yandex", "soundcloud", "local");
    private static final Set<String> STATUSES = Set.of("unsupported", "unavailable", "ready", "saved", "pending", "not_confirmed");

    public MediaFavoriteState {
        provider = PROVIDERS.contains(provider == null ? "" : provider) ? provider : "";
        status = STATUSES.contains(status == null ? "" : status) ? status : "unavailable";
        available = available && !provider.isEmpty() && !status.equals("unsupported");
        saved = saved && available;
        requestId = Math.max(0L, requestId);
        accepted = accepted && requestId > 0L && saved;
    }

    public static MediaFavoriteState unavailable() {
        return new MediaFavoriteState("", false, false, 0L, false, "unavailable");
    }

    public boolean canAdd() {
        return this.available && !this.saved && (this.status.equals("ready") || this.status.equals("not_confirmed"));
    }
}

