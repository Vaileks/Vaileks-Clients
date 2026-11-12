/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import ru.bloodhelper.client.music.MediaFavoriteState;

public record MediaSnapshot(String title, String artist, long positionMs, long durationMs, boolean playing, byte[] artwork, long revision, boolean canSeek, String trackToken, long seekRequestId, boolean seekAccepted, double playbackRate, long observedAtNanos, boolean canControlVolume, double volume, String volumeTargetToken, long volumeRequestId, boolean volumeAccepted, MediaFavoriteState favorite) {
    private final byte[] artwork;

    public MediaSnapshot {
        title = title == null ? "" : title;
        artist = artist == null ? "" : artist;
        durationMs = Math.max(0L, durationMs);
        positionMs = Math.max(0L, positionMs);
        if (durationMs > 0L) {
            positionMs = Math.min(positionMs, durationMs);
        }
        artwork = artwork == null ? null : (byte[])artwork.clone();
        trackToken = trackToken == null ? "" : trackToken;
        canSeek = canSeek && (!title.isBlank() || !artist.isBlank()) && durationMs > 0L && trackToken.matches("[a-f0-9]{32}");
        seekRequestId = Math.max(0L, seekRequestId);
        seekAccepted = seekAccepted && seekRequestId > 0L;
        playbackRate = Double.isFinite(playbackRate) ? Math.clamp(playbackRate, 0.0, 16.0) : 1.0;
        volumeTargetToken = volumeTargetToken == null ? "" : volumeTargetToken;
        canControlVolume = canControlVolume && (!title.isBlank() || !artist.isBlank()) && volumeTargetToken.matches("[a-f0-9]{32}") && Double.isFinite(volume);
        volume = Double.isFinite(volume) ? Math.clamp(volume, 0.0, 1.0) : 0.0;
        volumeRequestId = Math.max(0L, volumeRequestId);
        volumeAccepted = volumeAccepted && volumeRequestId > 0L;
        favorite = favorite == null || !trackToken.matches("[a-f0-9]{32}") || title.isBlank() ? MediaFavoriteState.unavailable() : favorite;
    }

    public MediaSnapshot(String title, String artist, long positionMs, long durationMs, boolean playing, byte[] artwork, long revision, boolean canSeek, String trackToken, long seekRequestId, boolean seekAccepted, double playbackRate, long observedAtNanos, boolean canControlVolume, double volume, String volumeTargetToken, long volumeRequestId, boolean volumeAccepted) {
        this(title, artist, positionMs, durationMs, playing, artwork, revision, canSeek, trackToken, seekRequestId, seekAccepted, playbackRate, observedAtNanos, canControlVolume, volume, volumeTargetToken, volumeRequestId, volumeAccepted, MediaFavoriteState.unavailable());
    }

    public MediaSnapshot(String title, String artist, long positionMs, long durationMs, boolean playing, byte[] artwork, long revision, boolean canSeek, String trackToken, long seekRequestId, boolean seekAccepted, double playbackRate, long observedAtNanos) {
        this(title, artist, positionMs, durationMs, playing, artwork, revision, canSeek, trackToken, seekRequestId, seekAccepted, playbackRate, observedAtNanos, false, 0.0, "", 0L, false);
    }

    public MediaSnapshot(String title, String artist, long positionMs, long durationMs, boolean playing, byte[] artwork, long revision, boolean canSeek, String trackToken, long seekRequestId, boolean seekAccepted) {
        this(title, artist, positionMs, durationMs, playing, artwork, revision, canSeek, trackToken, seekRequestId, seekAccepted, 1.0, System.nanoTime());
    }

    public MediaSnapshot(String title, String artist, long positionMs, long durationMs, boolean playing, byte[] artwork, long revision) {
        this(title, artist, positionMs, durationMs, playing, artwork, revision, false, "", 0L, false);
    }

    MediaSnapshot withVolume(double target) {
        if (this.volume == target) {
            return this;
        }
        return new MediaSnapshot(this.title, this.artist, this.positionMs, this.durationMs, this.playing, this.artwork, this.revision, this.canSeek, this.trackToken, this.seekRequestId, this.seekAccepted, this.playbackRate, this.observedAtNanos, this.canControlVolume, target, this.volumeTargetToken, this.volumeRequestId, this.volumeAccepted, this.favorite);
    }

    MediaSnapshot withPlayback(long position, boolean isPlaying, long now) {
        return new MediaSnapshot(this.title, this.artist, position, this.durationMs, isPlaying, this.artwork, this.revision, this.canSeek, this.trackToken, this.seekRequestId, this.seekAccepted, this.playbackRate, now, this.canControlVolume, this.volume, this.volumeTargetToken, this.volumeRequestId, this.volumeAccepted, this.favorite);
    }

    public long playbackPosition() {
        return this.playbackPosition(System.nanoTime());
    }

    long playbackPosition(long nowNanos) {
        long elapsedMs = Math.clamp((nowNanos - this.observedAtNanos) / 1000000L, 0, 1500);
        long advance = this.playing ? (long)((double)elapsedMs * this.playbackRate) : 0L;
        long position = this.positionMs > Long.MAX_VALUE - advance ? Long.MAX_VALUE : this.positionMs + advance;
        return this.durationMs > 0L ? Math.min(position, this.durationMs) : position;
    }

    public byte[] artwork() {
        return this.artwork == null ? null : (byte[])this.artwork.clone();
    }

    public static MediaSnapshot empty() {
        return new MediaSnapshot("", "", 0L, 0L, false, null, 0L);
    }

    public boolean available() {
        return !this.title.isBlank() || !this.artist.isBlank();
    }
}

