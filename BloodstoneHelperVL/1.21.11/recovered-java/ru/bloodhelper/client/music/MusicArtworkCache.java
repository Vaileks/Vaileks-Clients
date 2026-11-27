/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParser
 */
package ru.bloodhelper.client.music;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ru.bloodhelper.client.music.MusicSearchCatalog;
import ru.bloodhelper.client.music.MusicSearchService;
import ru.bloodhelper.client.vl.PrivacyGuard;

public final class MusicArtworkCache
implements AutoCloseable {
    private final Map<String, byte[]> cache = new LinkedHashMap<String, byte[]>();
    private final Set<String> pending = new HashSet<String>();
    private final Map<String, Long> retryAt = new HashMap<String, Long>();
    private final ExecutorService workers = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r, "bloodhelper-music-artwork");
        thread.setDaemon(true);
        return thread;
    });
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3L)).followRedirects(HttpClient.Redirect.NEVER).build();
    private boolean closed;

    public synchronized byte[] get(String url) {
        boolean spotify;
        boolean bl = spotify = url != null && url.matches("spotify:(?:artist|track):[A-Za-z0-9]{22}");
        if (this.closed || !spotify && MusicSearchCatalog.imageUrl(url).isEmpty()) {
            return null;
        }
        byte[] bytes = this.cache.get(url);
        if (bytes != null) {
            return bytes;
        }
        if (this.pending.size() >= 24 || this.pending.contains(url) || System.nanoTime() < this.retryAt.getOrDefault(url, 0L)) {
            return null;
        }
        this.pending.add(url);
        this.workers.submit(() -> {
            byte[] image = null;
            try {
                String imageUrl = url;
                if (spotify) {
                    String[] parts = url.split(":");
                    URI uRI = URI.create("https://open.spotify.com/oembed?url=https%3A%2F%2Fopen.spotify.com%2F" + parts[1] + "%2F" + parts[2]);
                    PrivacyGuard.checkUri(uRI);
                    HttpRequest metadata = HttpRequest.newBuilder(uRI).timeout(Duration.ofSeconds(5L)).GET().build();
                    HttpResponse response = this.http.send(metadata, ignored -> new MusicSearchService.BoundedBody());
                    if (response.statusCode() != 200) {
                        throw new IOException("Artwork metadata unavailable");
                    }
                    imageUrl = MusicSearchCatalog.imageUrl(MusicSearchCatalog.string(JsonParser.parseString((String)new String((byte[])response.body(), StandardCharsets.UTF_8)).getAsJsonObject(), "thumbnail_url"));
                    if (imageUrl.isEmpty()) {
                        throw new IOException("Invalid artwork URI");
                    }
                }
                URI uRI = URI.create(imageUrl);
                PrivacyGuard.checkUri(uRI);
                HttpRequest request = HttpRequest.newBuilder(uRI).timeout(Duration.ofSeconds(5L)).GET().build();
                HttpResponse response = this.http.send(request, ignored -> new MusicSearchService.BoundedBody());
                if (response.statusCode() == 200) {
                    image = MusicSearchService.smallPng((byte[])response.body());
                }
            }
            catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
            catch (Exception exception) {
                // empty catch block
            }
            MusicArtworkCache musicArtworkCache = this;
            synchronized (musicArtworkCache) {
                this.pending.remove(url);
                if (this.closed) {
                    return;
                }
                if (image != null) {
                    if (this.cache.size() >= 128) {
                        this.cache.remove(this.cache.keySet().iterator().next());
                    }
                    this.cache.put(url, image);
                    this.retryAt.remove(url);
                } else {
                    if (this.retryAt.size() >= 128) {
                        this.retryAt.clear();
                    }
                    this.retryAt.put(url, System.nanoTime() + 30000000000L);
                }
            }
        });
        return null;
    }

    @Override
    public synchronized void close() {
        this.closed = true;
        this.workers.shutdownNow();
        this.http.shutdownNow();
        this.cache.clear();
        this.retryAt.clear();
        this.pending.clear();
    }
}

