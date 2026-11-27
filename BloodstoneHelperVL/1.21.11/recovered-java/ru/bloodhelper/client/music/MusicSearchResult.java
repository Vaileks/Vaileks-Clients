/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import java.util.List;
import java.util.Locale;

public record MusicSearchResult(Provider provider, String query, Status status, List<Track> tracks, Artist artist, int page, int total) {
    public MusicSearchResult {
        tracks = List.copyOf(tracks);
    }

    public MusicSearchResult(Provider provider, String query, Status status, List<Track> tracks) {
        this(provider, query, status, tracks, null, 0, tracks.size());
    }

    public boolean chart() {
        return this.artist == null && this.query.isEmpty();
    }

    public static MusicSearchResult empty(Provider provider, String query, Status status) {
        return new MusicSearchResult(provider, query, status, List.of());
    }

    public static String cleanQuery(String value) {
        if (value == null) {
            return "";
        }
        String clean = value.replaceAll("[\\p{Cntrl}]", " ").strip();
        return clean.codePoints().limit(120L).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    public static Provider providerFor(String source) {
        return source != null && source.toLowerCase(Locale.ROOT).contains("spotify") ? Provider.SPOTIFY : Provider.YANDEX;
    }

    public static enum Provider {
        YANDEX,
        SPOTIFY;

    }

    public static enum Status {
        IDLE,
        LOADING,
        READY,
        EMPTY,
        NETWORK,
        RATE_LIMITED,
        UNAVAILABLE,
        OPEN_PLAYER,
        SIGN_IN,
        UNSUPPORTED,
        STALE,
        OPENING,
        OPENED,
        REQUESTED,
        PLAYING,
        BACKGROUND_SETUP,
        CONNECTING,
        BACKGROUND_READY,
        BACKGROUND_UNAVAILABLE;

    }

    public record Artist(String id, String name, String cover) {
    }

    public record Track(String id, String title, String artist, long durationMs, boolean available, List<Artist> artists, String cover) {
        public Track {
            artists = List.copyOf(artists);
        }

        public Track(String id, String title, String artist, long durationMs, boolean available, List<Artist> artists) {
            this(id, title, artist, durationMs, available, artists, "");
        }

        public Track(String id, String title, String artist, long durationMs, boolean available) {
            this(id, title, artist, durationMs, available, List.of(new Artist("", artist, "")));
        }
    }
}

