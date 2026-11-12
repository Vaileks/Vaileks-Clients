/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package ru.bloodhelper.client.music;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Base64;
import ru.bloodhelper.client.music.MediaFavoriteState;
import ru.bloodhelper.client.music.MediaSnapshot;
import ru.bloodhelper.client.music.MediaSources;

final class MediaProtocol {
    static final int MAX_ARTWORK_BYTES = 0x200000;
    static final int MAX_LINE_CHARS = 0x300000;
    private static final int MAX_TEXT_CHARS = 1024;
    private static final long MAX_TIME_MS = 31536000000L;
    private String diagnostic = "";
    private MediaSources sources = MediaSources.empty();
    private MediaSnapshot current;
    private byte[] currentArtwork;

    String diagnostic() {
        return this.diagnostic;
    }

    MediaSources sources() {
        return this.sources;
    }

    MediaProtocol() {
        this(0L);
    }

    MediaProtocol(long initialRevision) {
        this.current = new MediaSnapshot("", "", 0L, 0L, false, null, initialRevision);
    }

    MediaSnapshot current() {
        return this.current;
    }

    MediaSnapshot clear() {
        if (this.current.available() || this.currentArtwork != null || !this.current.trackToken().isEmpty() || this.current.seekRequestId() != 0L) {
            this.current = new MediaSnapshot("", "", 0L, 0L, false, null, this.current.revision() + 1L);
            this.currentArtwork = null;
        }
        return this.current;
    }

    MediaSnapshot accept(String line) {
        return this.accept(line, System.currentTimeMillis(), System.nanoTime());
    }

    MediaSnapshot accept(String line, long receivedEpochMs, long receivedNanos) {
        byte[] artwork;
        if (line.length() > 0x300000) {
            throw new IllegalArgumentException("Media packet too large");
        }
        JsonObject packet = JsonParser.parseString((String)line).getAsJsonObject();
        if ("diagnostic".equals(MediaProtocol.string(packet, "kind", 32))) {
            this.diagnostic = MediaProtocol.string(packet, "message", 160);
            return this.current;
        }
        if ("sources".equals(MediaProtocol.string(packet, "kind", 32))) {
            this.sources = MediaSources.read(packet);
            return this.current;
        }
        if (!"snapshot".equals(MediaProtocol.string(packet, "kind", 32))) {
            throw new IllegalArgumentException("Unknown media packet");
        }
        if (!MediaProtocol.bool(packet, "available")) {
            return this.clear();
        }
        String title = MediaProtocol.string(packet, "title", 1024);
        String artist = MediaProtocol.string(packet, "artist", 1024);
        String trackToken = MediaProtocol.string(packet, "trackToken", 64);
        boolean metadataChanged = !title.equals(this.current.title()) || !artist.equals(this.current.artist()) || !trackToken.equals(this.current.trackToken());
        byte[] byArray = artwork = metadataChanged ? null : this.currentArtwork;
        if (packet.has("artworkBase64")) {
            JsonElement element = packet.get("artworkBase64");
            if (element.isJsonNull()) {
                if (metadataChanged) {
                    artwork = null;
                }
            } else {
                String encoded = element.getAsString();
                if (encoded.length() > 0x2AAAAC) {
                    throw new IllegalArgumentException("Media artwork too large");
                }
                artwork = Base64.getDecoder().decode(encoded);
                if (artwork.length > 0x200000) {
                    throw new IllegalArgumentException("Media artwork too large");
                }
                if (artwork.length == 0) {
                    artwork = null;
                }
            }
        }
        long revision = this.current.revision();
        if (metadataChanged || !Arrays.equals(artwork, this.currentArtwork)) {
            ++revision;
        }
        this.currentArtwork = artwork;
        boolean playing = MediaProtocol.bool(packet, "playing");
        double rate = MediaProtocol.playbackRate(packet);
        long position = MediaProtocol.time(packet, "positionMs");
        long observedEpochMs = MediaProtocol.observationEpoch(packet);
        if (playing && observedEpochMs > 0L) {
            long transit = Math.clamp(receivedEpochMs - observedEpochMs, 0, 1500);
            position = Math.min(31536000000L, position + (long)((double)transit * rate));
        }
        this.current = new MediaSnapshot(title, artist, position, MediaProtocol.time(packet, "durationMs"), playing, artwork, revision, MediaProtocol.bool(packet, "canSeek"), trackToken, MediaProtocol.requestId(packet, "seekRequestId"), MediaProtocol.bool(packet, "seekAccepted"), rate, receivedNanos, MediaProtocol.bool(packet, "canControlVolume"), MediaProtocol.volume(packet), MediaProtocol.string(packet, "volumeTargetToken", 64), MediaProtocol.requestId(packet, "volumeRequestId"), MediaProtocol.bool(packet, "volumeAccepted"), new MediaFavoriteState(MediaProtocol.string(packet, "favoriteProvider", 32), MediaProtocol.bool(packet, "canFavorite"), MediaProtocol.bool(packet, "favorite"), MediaProtocol.requestId(packet, "favoriteRequestId"), MediaProtocol.bool(packet, "favoriteAccepted"), MediaProtocol.string(packet, "favoriteStatus", 32)));
        return this.current;
    }

    static String readLine(Reader reader) throws IOException {
        int character;
        StringBuilder line = new StringBuilder(1024);
        while ((character = reader.read()) != -1) {
            if (character == 10) {
                return line.toString();
            }
            if (character == 13) continue;
            if (line.length() >= 0x300000) {
                throw new IOException("Media packet too large");
            }
            line.append((char)character);
        }
        return line.isEmpty() ? null : line.toString();
    }

    private static String string(JsonObject object, String key, int limit) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) {
            return "";
        }
        String result = value.getAsString().replaceAll("[\\p{Cntrl}&&[^\\t]]", " ");
        return result.length() > limit ? result.substring(0, limit) : result;
    }

    private static boolean bool(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && !value.isJsonNull() && value.getAsBoolean();
    }

    private static long time(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) {
            return 0L;
        }
        double numeric = value.getAsDouble();
        if (!Double.isFinite(numeric)) {
            return 0L;
        }
        return (long)Math.max(0.0, Math.min(numeric, 3.1536E10));
    }

    private static double playbackRate(JsonObject packet) {
        JsonElement value = packet.get("playbackRate");
        if (value == null || value.isJsonNull()) {
            return 1.0;
        }
        double rate = value.getAsDouble();
        return Double.isFinite(rate) ? Math.clamp(rate, 0.0, 16.0) : 1.0;
    }

    private static long observationEpoch(JsonObject packet) {
        JsonElement value = packet.get("observedAtEpochMs");
        if (value == null || value.isJsonNull()) {
            return 0L;
        }
        try {
            return Math.max(0L, value.getAsBigDecimal().longValueExact());
        }
        catch (ArithmeticException | NumberFormatException ignored) {
            return 0L;
        }
    }

    private static double volume(JsonObject packet) {
        JsonElement value = packet.get("volume");
        if (value == null || value.isJsonNull()) {
            return Double.NaN;
        }
        return value.getAsDouble();
    }

    private static long requestId(JsonObject packet, String key) {
        JsonElement value = packet.get(key);
        if (value == null || value.isJsonNull()) {
            return 0L;
        }
        try {
            return Math.max(0L, value.getAsBigDecimal().longValueExact());
        }
        catch (ArithmeticException | NumberFormatException ignored) {
            return 0L;
        }
    }
}

