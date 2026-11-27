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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import ru.bloodhelper.client.music.MusicSearchCatalog;
import ru.bloodhelper.client.music.MusicSearchResult;
import ru.bloodhelper.client.vl.PrivacyGuard;

public final class MusicSearchService
implements AutoCloseable {
    private static final int MAX_BODY = 524288;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "bloodhelper-music-search");
        t.setDaemon(true);
        return t;
    });
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4L)).followRedirects(HttpClient.Redirect.NEVER).build();
    private final boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
    private volatile MusicSearchResult result = MusicSearchResult.empty(MusicSearchResult.Provider.YANDEX, "", MusicSearchResult.Status.IDLE);
    private volatile byte[] artwork;
    private Future<?> task;
    private Process nativeProcess;
    private long generation;
    private boolean closed;

    public MusicSearchService() {
    }

    public MusicSearchService(MusicSearchResult retained, byte[] cover) {
        if (retained != null) {
            this.result = new MusicSearchResult(retained.provider(), retained.query(), retained.status() == MusicSearchResult.Status.LOADING || retained.status() == MusicSearchResult.Status.OPENING || retained.status() == MusicSearchResult.Status.CONNECTING ? (retained.tracks().isEmpty() ? MusicSearchResult.Status.IDLE : MusicSearchResult.Status.READY) : retained.status(), retained.tracks(), retained.artist(), retained.page(), retained.total());
        }
        this.artwork = cover;
    }

    public MusicSearchResult result() {
        return this.result;
    }

    public byte[] artwork() {
        return this.artwork;
    }

    public synchronized void reset(MusicSearchResult.Provider provider, String query) {
        this.cancel();
        this.artwork = null;
        this.result = MusicSearchResult.empty(provider, MusicSearchResult.cleanQuery(query), MusicSearchResult.Status.IDLE);
    }

    public synchronized void search(MusicSearchResult.Provider provider, String value) {
        if (this.closed) {
            return;
        }
        this.cancel();
        this.artwork = null;
        String query = MusicSearchResult.cleanQuery(value);
        this.result = MusicSearchResult.empty(provider, query, MusicSearchResult.Status.LOADING);
        long token = this.generation;
        this.task = this.worker.submit(() -> {
            try {
                MusicSearchResult next;
                if (provider == MusicSearchResult.Provider.YANDEX || query.isEmpty()) {
                    URI uRI = query.isEmpty() ? MusicSearchCatalog.chart(provider) : MusicSearchCatalog.yandexSearch(query);
                    PrivacyGuard.checkUri(uRI);
                    HttpRequest request = HttpRequest.newBuilder(uRI).timeout(Duration.ofSeconds(10L)).header("Accept", "application/json").header("User-Agent", "BloodHelper/MusicSearch").GET().build();
                    HttpResponse response = this.http.send(request, ignored -> new BoundedBody());
                    if (response.statusCode() != 200) {
                        next = MusicSearchResult.empty(provider, query, response.statusCode() == 429 ? MusicSearchResult.Status.RATE_LIMITED : MusicSearchResult.Status.NETWORK);
                    } else {
                        String body = new String((byte[])response.body(), StandardCharsets.UTF_8);
                        List<MusicSearchResult.Track> tracks = query.isEmpty() ? MusicSearchCatalog.parseChart(provider, body) : MusicSearchCatalog.parseYandex(body);
                        next = new MusicSearchResult(provider, query, tracks.isEmpty() ? MusicSearchResult.Status.EMPTY : MusicSearchResult.Status.READY, tracks);
                    }
                } else {
                    next = !this.windows ? MusicSearchResult.empty(provider, query, MusicSearchResult.Status.UNSUPPORTED) : MusicSearchService.readNative(this.nativeRequest("search", query, "", token), query);
                }
                this.publish(token, next);
            }
            catch (InterruptedException ignored2) {
                Thread.currentThread().interrupt();
            }
            catch (Exception failure) {
                this.publish(token, MusicSearchResult.empty(provider, query, provider == MusicSearchResult.Provider.YANDEX || query.isEmpty() ? MusicSearchResult.Status.NETWORK : MusicSearchResult.Status.UNAVAILABLE));
            }
        });
    }

    public synchronized void searchArtist(MusicSearchResult.Provider provider, MusicSearchResult.Artist artist, int page) {
        if (this.closed || artist == null) {
            return;
        }
        byte[] existingCover = artist.equals(this.result.artist()) ? this.artwork : null;
        this.cancel();
        this.artwork = existingCover;
        long token = this.generation;
        String query = provider == MusicSearchResult.Provider.SPOTIFY ? MusicSearchCatalog.spotifyArtistQuery(artist) : artist.name();
        this.result = new MusicSearchResult(provider, query, MusicSearchResult.Status.LOADING, List.of(), artist, page, 0);
        this.task = this.worker.submit(() -> {
            block15: {
                try {
                    byte[] png;
                    MusicSearchResult next;
                    HttpRequest request;
                    if (provider == MusicSearchResult.Provider.YANDEX) {
                        URI uRI = MusicSearchCatalog.yandexArtistTracks(artist, page);
                        PrivacyGuard.checkUri(uRI);
                        request = HttpRequest.newBuilder(uRI).timeout(Duration.ofSeconds(10L)).header("Accept", "application/json").header("User-Agent", "BloodHelper/MusicSearch").GET().build();
                        HttpResponse response = this.http.send(request, ignored -> new BoundedBody());
                        next = response.statusCode() == 200 ? MusicSearchCatalog.parseArtistTracks(new String((byte[])response.body(), StandardCharsets.UTF_8), artist, page) : new MusicSearchResult(provider, query, response.statusCode() == 429 ? MusicSearchResult.Status.RATE_LIMITED : MusicSearchResult.Status.NETWORK, List.of(), artist, page, 0);
                    } else if (!this.windows) {
                        next = new MusicSearchResult(provider, query, MusicSearchResult.Status.UNSUPPORTED, List.of(), artist, page, 0);
                    } else {
                        MusicSearchResult found = MusicSearchService.readNative(this.nativeRequest("search", query, "", token), query);
                        next = new MusicSearchResult(provider, query, found.status(), found.tracks(), artist, 0, found.tracks().size());
                    }
                    this.publish(token, next);
                    if (existingCover != null || provider != MusicSearchResult.Provider.YANDEX || !artist.cover().matches("https://avatars\\.yandex\\.net/get-music-content/[a-zA-Z0-9._/-]+/200x200")) break block15;
                    URI uRI = URI.create(artist.cover());
                    PrivacyGuard.checkUri(uRI);
                    request = HttpRequest.newBuilder(uRI).timeout(Duration.ofSeconds(8L)).GET().build();
                    HttpResponse cover = this.http.send(request, ignored -> new BoundedBody());
                    byte[] byArray = png = cover.statusCode() == 200 ? MusicSearchService.smallPng((byte[])cover.body()) : null;
                    if (png == null) break block15;
                    MusicSearchService musicSearchService = this;
                    synchronized (musicSearchService) {
                        if (!this.closed && token == this.generation) {
                            this.artwork = png;
                        }
                    }
                }
                catch (InterruptedException ignored2) {
                    Thread.currentThread().interrupt();
                }
                catch (Exception failure) {
                    MusicSearchService musicSearchService = this;
                    synchronized (musicSearchService) {
                        if (token == this.generation && this.result.status() == MusicSearchResult.Status.LOADING) {
                            this.publish(token, new MusicSearchResult(provider, query, provider == MusicSearchResult.Provider.YANDEX ? MusicSearchResult.Status.NETWORK : MusicSearchResult.Status.UNAVAILABLE, List.of(), artist, page, 0));
                        }
                    }
                }
            }
        });
    }

    /*
     * Exception decompiling
     */
    static byte[] smallPng(byte[] bytes) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 7 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public synchronized void open(MusicSearchResult expected, MusicSearchResult.Track track) {
        if (this.closed || expected != this.result || this.result.status() == MusicSearchResult.Status.OPENING || this.result.status() == MusicSearchResult.Status.CONNECTING || !expected.tracks().contains(track) || !track.available()) {
            return;
        }
        this.cancel();
        long token = this.generation;
        this.result = new MusicSearchResult(expected.provider(), expected.query(), MusicSearchResult.Status.OPENING, expected.tracks(), expected.artist(), expected.page(), expected.total());
        this.task = this.worker.submit(() -> {
            try {
                MusicSearchResult.Status status;
                if (!this.windows) {
                    status = MusicSearchResult.Status.UNSUPPORTED;
                } else {
                    JsonObject answer = this.nativeRequest(expected.provider() == MusicSearchResult.Provider.YANDEX ? "play_yandex" : (track.id().startsWith("spotify:track:") ? "play_spotify_uri" : "play"), expected.query(), track.id(), token, track);
                    status = MusicSearchService.status(answer);
                }
                this.publish(token, new MusicSearchResult(expected.provider(), expected.query(), status, expected.tracks(), expected.artist(), expected.page(), expected.total()));
            }
            catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            catch (Exception failure) {
                this.publish(token, new MusicSearchResult(expected.provider(), expected.query(), MusicSearchResult.Status.UNAVAILABLE, expected.tracks(), expected.artist(), expected.page(), expected.total()));
            }
        });
    }

    public synchronized void connectYandex(MusicSearchResult musicSearchResult) {
    }

    private synchronized void publish(long token, MusicSearchResult value) {
        if (!this.closed && token == this.generation) {
            this.result = value;
        }
    }

    private void cancel() {
        ++this.generation;
        if (this.task != null) {
            this.task.cancel(true);
            this.task = null;
        }
        if (this.nativeProcess != null) {
            this.nativeProcess.destroyForcibly();
            this.nativeProcess = null;
        }
    }

    @Override
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.cancel();
        this.worker.shutdownNow();
        this.http.shutdownNow();
    }

    private JsonObject nativeRequest(String mode, String query, String id, long token) throws Exception {
        return this.nativeRequest(mode, query, id, token, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private JsonObject nativeRequest(String mode, String query, String id, long token, MusicSearchResult.Track track) throws Exception {
        Path directory = null;
        Process process = null;
        try {
            byte[] data;
            directory = Files.createTempDirectory("bloodhelper-search-", new FileAttribute[0]);
            Path script = directory.resolve("music-search.ps1");
            try (InputStream resource = MusicSearchService.class.getResourceAsStream("/bloodhelper/" + (mode.equals("play_yandex") || mode.equals("connect_yandex") ? "music-yandex.ps1" : "music-search.ps1"));){
                if (resource == null) {
                    throw new IOException("Missing search worker");
                }
                Files.copy(resource, script, new CopyOption[0]);
            }
            String windowsRoot = System.getenv("SystemRoot");
            if (windowsRoot == null) {
                throw new IOException("Missing Windows");
            }
            MusicSearchService musicSearchService = this;
            synchronized (musicSearchService) {
                if (this.closed || this.generation != token || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                this.nativeProcess = process = new ProcessBuilder(Path.of(windowsRoot, "System32", "WindowsPowerShell", "v1.0", "powershell.exe").toString(), "-NoLogo", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", script.toString()).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            }
            JsonObject request = new JsonObject();
            request.addProperty("mode", mode);
            request.addProperty("query", query);
            request.addProperty("id", id);
            if (track != null) {
                request.addProperty("title", track.title());
                request.addProperty("artist", track.artist());
            }
            request.addProperty("ownerPid", (Number)ProcessHandle.current().pid());
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);){
                writer.write(String.valueOf(request) + "\n");
            }
            Process owned = process;
            CompletableFuture<byte[]> read = CompletableFuture.supplyAsync(() -> {
                try {
                    return owned.getInputStream().readNBytes(65537);
                }
                catch (IOException e) {
                    throw new CompletionException(e);
                }
            });
            try {
                data = read.get(mode.equals("connect_yandex") ? 30L : 15L, TimeUnit.SECONDS);
            }
            finally {
                if (!read.isDone()) {
                    process.destroyForcibly();
                    read.cancel(true);
                }
            }
            if (data.length > 65536 || !process.waitFor(1L, TimeUnit.SECONDS) || process.exitValue() != 0) {
                throw new IOException("Invalid worker response");
            }
            JsonObject jsonObject = JsonParser.parseString((String)new String(data, StandardCharsets.UTF_8).strip()).getAsJsonObject();
            return jsonObject;
        }
        finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            MusicSearchService musicSearchService = this;
            synchronized (musicSearchService) {
                if (this.nativeProcess == process) {
                    this.nativeProcess = null;
                }
            }
            if (directory != null) {
                try {
                    Files.deleteIfExists(directory.resolve("music-search.ps1"));
                    Files.deleteIfExists(directory);
                }
                catch (IOException iOException) {}
            }
        }
    }

    static MusicSearchResult.Status status(JsonObject answer) {
        try {
            return MusicSearchResult.Status.valueOf(MusicSearchCatalog.string(answer, "status").toUpperCase(Locale.ROOT));
        }
        catch (RuntimeException invalid) {
            return MusicSearchResult.Status.UNAVAILABLE;
        }
    }

    static MusicSearchResult readNative(JsonObject answer, String query) {
        MusicSearchResult.Status state = MusicSearchService.status(answer);
        ArrayList<MusicSearchResult.Track> tracks = new ArrayList<MusicSearchResult.Track>();
        HashSet<String> ids = new HashSet<String>();
        if (state == MusicSearchResult.Status.READY && answer.has("tracks") && answer.get("tracks").isJsonArray()) {
            for (JsonElement entry : answer.getAsJsonArray("tracks")) {
                if (tracks.size() >= 20) break;
                try {
                    JsonObject row = entry.getAsJsonObject();
                    String id = MusicSearchCatalog.string(row, "id");
                    String title = MusicSearchCatalog.string(row, "title");
                    String artist = MusicSearchCatalog.string(row, "artist");
                    if (!id.matches("[0-9a-f.:-]{1,220}") || title.isBlank() || artist.isBlank() || !ids.add(id)) continue;
                    String uri = MusicSearchCatalog.string(row, "uri");
                    ArrayList<MusicSearchResult.Artist> people = new ArrayList<MusicSearchResult.Artist>();
                    if (row.has("artists") && row.get("artists").isJsonArray()) {
                        for (JsonElement entryArtist : row.getAsJsonArray("artists")) {
                            JsonObject person = entryArtist.getAsJsonObject();
                            String artistUri = MusicSearchCatalog.string(person, "uri");
                            String name = MusicSearchCatalog.string(person, "name");
                            if (artistUri.matches("spotify:artist:[A-Za-z0-9]{22}") && !name.isBlank()) {
                                people.add(new MusicSearchResult.Artist(artistUri, name, artistUri));
                            }
                            if (people.size() != 12) continue;
                            break;
                        }
                    }
                    if (people.isEmpty()) {
                        people.add(new MusicSearchResult.Artist("", artist, ""));
                    }
                    tracks.add(new MusicSearchResult.Track(id, title, artist, Math.clamp(row.get("durationMs").getAsLong(), 0L, 86400000L), true, people, uri.matches("spotify:track:[A-Za-z0-9]{22}") ? uri : ""));
                }
                catch (RuntimeException runtimeException) {}
            }
            if (tracks.isEmpty()) {
                state = MusicSearchResult.Status.EMPTY;
            }
        }
        return new MusicSearchResult(MusicSearchResult.Provider.SPOTIFY, query, state, tracks);
    }

    private /* synthetic */ void lambda$connectYandex$0(long l, MusicSearchResult musicSearchResult) {
    }

    static final class BoundedBody
    implements HttpResponse.BodySubscriber<byte[]> {
        private final HttpResponse.BodySubscriber<byte[]> delegate = HttpResponse.BodySubscribers.ofByteArray();
        private Flow.Subscription subscription;
        private long bytes;

        BoundedBody() {
        }

        @Override
        public CompletionStage<byte[]> getBody() {
            return this.delegate.getBody();
        }

        @Override
        public void onSubscribe(Flow.Subscription value) {
            this.subscription = value;
            this.delegate.onSubscribe(value);
        }

        @Override
        public void onNext(List<ByteBuffer> buffers) {
            for (ByteBuffer buffer : buffers) {
                this.bytes += (long)buffer.remaining();
            }
            if (this.bytes > 524288L) {
                this.subscription.cancel();
                this.delegate.onError(new IOException("Search response too large"));
            } else {
                this.delegate.onNext((byte[])buffers);
            }
        }

        @Override
        public void onError(Throwable failure) {
            this.delegate.onError(failure);
        }

        @Override
        public void onComplete() {
            this.delegate.onComplete();
        }
    }
}

