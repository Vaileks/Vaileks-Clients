/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package ru.bloodhelper.client.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.lyrics.LrcParser;
import ru.bloodhelper.client.lyrics.LyricLine;
import ru.bloodhelper.client.lyrics.LyricsIdentity;
import ru.bloodhelper.client.lyrics.WordTiming;
import ru.bloodhelper.client.music.LocalMusicLyrics;
import ru.bloodhelper.client.music.MediaSnapshot;
import ru.bloodhelper.client.vl.PrivacyGuard;

public final class LyricsService
implements AutoCloseable {
    private static final int MAX_BODY = 0x100000;
    private static final double MAX_DURATION_DIFFERENCE_SECONDS = 2.0;
    private final HttpClient http;
    private final Map<String, Lyrics> cache = new LinkedHashMap<String, Lyrics>(this, 64, 0.75f, true){
        final /* synthetic */ LyricsService this$0;
        {
            LyricsService lyricsService = this$0;
            Objects.requireNonNull(lyricsService);
            this.this$0 = lyricsService;
            super(arg0, arg1, arg2);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Lyrics> eldest) {
            return this.size() > 64;
        }
    };
    private final Set<CompletableFuture<?>> requests = new HashSet();
    private volatile Lyrics active = Lyrics.empty(Status.DISABLED);
    private volatile List<Found> alternatives = List.of();
    private volatile boolean alternativesLoading;
    private volatile Issue alternativesIssue = Issue.NONE;
    private volatile String trackKey = "";
    private volatile String identity = "";
    private String title = "";
    private String artist = "";
    private volatile boolean closed;
    private volatile long trackDurationMs;
    private long generation;

    public LyricsService() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4L)).followRedirects(HttpClient.Redirect.NEVER).build());
    }

    LyricsService(HttpClient http) {
        this.http = Objects.requireNonNull(http);
    }

    public synchronized void update(MediaSnapshot snapshot, ClientConfig.MusicSettings settings) {
        this.update(snapshot, settings, false);
    }

    public synchronized void update(MediaSnapshot snapshot, ClientConfig.MusicSettings settings, boolean playerOpen) {
        String nextArtist;
        boolean enabled;
        boolean bl = enabled = !this.closed && (playerOpen || settings.enabled && !"OFF".equals(settings.lyricsMode)) && snapshot != null && snapshot.available() && !snapshot.title().isBlank();
        if (!enabled) {
            if (!this.trackKey.isEmpty() || this.status() != Status.DISABLED) {
                this.reset();
            }
            return;
        }
        LocalMusicLyrics.Lyrics local = LocalMusicLyrics.forTrack(snapshot.trackToken());
        if (local != null) {
            String key = "LOCAL\u0000" + local.track();
            if (!key.equals(this.trackKey)) {
                this.reset();
                this.trackKey = key;
                this.identity = key;
                this.title = snapshot.title();
                this.artist = "";
                Found found2 = new Found(local.lines(), "LOCAL", local.track(), "", (double)snapshot.durationMs() / 1000.0, local.lines().isEmpty() ? Issue.NO_MATCH : Issue.NONE);
                this.active = new Lyrics(found2, local.lines().isEmpty() ? Status.NOT_FOUND : Status.READY, 0L);
            }
            this.trackDurationMs = snapshot.durationMs();
            return;
        }
        String nextTitle = LyricsService.metadata(snapshot.title());
        String nextIdentity = LyricsService.identity(nextTitle, nextArtist = LyricsService.metadata(snapshot.artist()), snapshot.durationMs());
        String key = nextIdentity + "\u0000" + settings.lyricsSource + "\u0000" + settings.lyricAdjustments.preferredRecord(nextIdentity);
        if (key.equals(this.trackKey)) {
            this.trackDurationMs = snapshot.durationMs();
            return;
        }
        this.reset();
        this.trackKey = key;
        this.identity = nextIdentity;
        this.title = nextTitle;
        this.artist = nextArtist;
        this.trackDurationMs = snapshot.durationMs();
        Lyrics cached = this.cache.get(key);
        if (cached != null) {
            this.active = cached;
            this.remember(cached.found());
            return;
        }
        this.active = Lyrics.empty(Status.LOADING);
        long token = this.generation;
        long started = System.nanoTime();
        String preferred = settings.lyricAdjustments.preferredRecord(this.identity);
        this.fetchSources(this.title, this.artist, this.trackDurationMs, token, settings.lyricsSource, preferred).whenComplete((found, failure) -> {
            LyricsService lyricsService = this;
            synchronized (lyricsService) {
                if (this.closed || token != this.generation || !key.equals(this.trackKey)) {
                    return;
                }
                if (failure != null) {
                    found = LyricsService.failed(settings.lyricsSource, failure);
                }
                Status state = found.hasText() ? Status.READY : (LyricsService.isError(found.issue()) ? Status.ERROR : Status.NOT_FOUND);
                this.active = new Lyrics((Found)found, state, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
                if (state != Status.ERROR) {
                    this.cache.put(key, this.active);
                }
                this.remember((Found)found);
                this.cancelRequests();
            }
        });
    }

    public boolean hasSyncedLyrics() {
        return !this.active.found().lines().isEmpty();
    }

    public List<LyricLine> displayLines() {
        Found found = this.active.found();
        return found.lines().isEmpty() ? found.plain() : found.lines();
    }

    public String source() {
        return this.active.found().source();
    }

    public String recordId() {
        return this.active.found().id();
    }

    public Issue issue() {
        return this.active.found().issue();
    }

    public long lookupMs() {
        return this.active.lookupMs();
    }

    public long firstCueMs() {
        return LyricsService.firstCue(this.active.found().lines());
    }

    public synchronized void retry() {
        this.cache.remove(this.trackKey);
        this.reset();
    }

    public String preferredRecord(ClientConfig.MusicSettings settings) {
        return settings.lyricAdjustments.preferredRecord(this.identity);
    }

    public synchronized void useAutomaticRecord(ClientConfig.MusicSettings settings) {
        settings.lyricAdjustments.records.remove(this.identity);
        this.cache.remove(this.identity + "\u0000" + settings.lyricsSource + "\u0000");
        this.retry();
    }

    public String trackKey() {
        return this.trackKey;
    }

    public Status status() {
        return this.active.status();
    }

    public boolean alternativesLoading() {
        return this.alternativesLoading;
    }

    public Issue alternativesIssue() {
        return this.alternativesIssue;
    }

    public List<Variant> variants() {
        return this.alternatives.stream().map(f -> new Variant(f.id(), f.album(), f.duration(), LyricsService.firstCue(f.lines()))).toList();
    }

    public int trackOffsetMs(ClientConfig.MusicSettings settings) {
        return settings.lyricAdjustments.offset(this.adjustmentKey());
    }

    public int offsetMs(ClientConfig.MusicSettings settings) {
        return settings.lyricOffsetMs + this.trackOffsetMs(settings);
    }

    public synchronized void setTrackOffsetMs(ClientConfig.MusicSettings settings, int value) {
        if (this.status() == Status.READY && this.hasSyncedLyrics()) {
            settings.lyricAdjustments.setOffset(this.adjustmentKey(), value);
        }
    }

    public synchronized void alignFirstLineNow(ClientConfig.MusicSettings settings, long playbackMs) {
        long first = this.firstCueMs();
        if (this.status() == Status.READY && this.hasSyncedLyrics()) {
            this.setTrackOffsetMs(settings, Math.clamp(first - playbackMs - (long)settings.lyricOffsetMs, -30000, 30000));
        }
    }

    private String adjustmentKey() {
        return this.identity + "\u0000" + this.source() + "\u0000" + this.recordId();
    }

    public synchronized void findAlternatives() {
        if (this.closed || this.trackKey.isEmpty() || this.alternativesLoading || this.status() == Status.LOADING) {
            return;
        }
        if (this.artist.isBlank() || this.trackDurationMs <= 0L) {
            this.alternativesIssue = Issue.MISSING_METADATA;
            return;
        }
        this.alternativesLoading = true;
        this.alternativesIssue = Issue.NONE;
        long token = this.generation;
        String expectedTitle = this.title;
        String expectedArtist = this.artist;
        long duration = this.trackDurationMs;
        this.search(expectedTitle, expectedArtist, duration, token, true).whenComplete((result, failure) -> {
            LyricsService lyricsService = this;
            synchronized (lyricsService) {
                if (token != this.generation || this.closed) {
                    return;
                }
                this.alternativesLoading = false;
                if (failure != null) {
                    this.alternativesIssue = LyricsService.failed("LRCLIB", failure).issue();
                    return;
                }
                for (Found found : result) {
                    this.remember(found);
                }
                if (result.isEmpty()) {
                    this.alternativesIssue = Issue.NO_MATCH;
                }
            }
        });
    }

    public synchronized void selectVariant(String id, ClientConfig.MusicSettings settings) {
        for (Found found : this.alternatives) {
            if (!found.id().equals(id)) continue;
            this.active = new Lyrics(found, Status.READY, this.active.lookupMs());
            settings.lyricAdjustments.preferRecord(this.identity, id);
            this.trackKey = this.identity + "\u0000" + settings.lyricsSource + "\u0000" + id;
            this.cache.put(this.trackKey, this.active);
            return;
        }
    }

    private void remember(Found found) {
        if (!"LRCLIB".equals(found.source()) || found.id().isEmpty() || found.lines().isEmpty()) {
            return;
        }
        ArrayList<Found> values = new ArrayList<Found>(this.alternatives);
        if (values.stream().noneMatch(f -> f.id().equals(found.id())) && values.size() < 40) {
            values.add(found);
        }
        this.alternatives = List.copyOf(values);
    }

    public List<LyricLine> lines(ClientConfig.MusicSettings settings) {
        return this.active.found().lines();
    }

    public ActiveLine current(long playbackMs, ClientConfig.MusicSettings settings) {
        long end;
        long time;
        if (!settings.enabled || "OFF".equals(settings.lyricsMode)) {
            return null;
        }
        List<LyricLine> lines = this.lines(settings);
        int index = LrcParser.indexAt(lines, time = playbackMs + (long)this.offsetMs(settings));
        if (index < 0) {
            return null;
        }
        LyricLine line = lines.get(index);
        long l = index + 1 < lines.size() ? lines.get(index + 1).timestampMs() : (end = this.trackDurationMs > line.timestampMs() ? this.trackDurationMs : line.timestampMs() + 6000L);
        if (time >= end || line.text().isBlank()) {
            return null;
        }
        return new ActiveLine(index, line, end);
    }

    public String currentLine(long playbackMs, ClientConfig.MusicSettings settings) {
        ActiveLine line = this.current(playbackMs, settings);
        return line == null ? "" : line.line().text();
    }

    public int countdownSeconds(long playbackMs, ClientConfig.MusicSettings settings) {
        if (!settings.enabled || "OFF".equals(settings.lyricsMode) || this.status() != Status.READY) {
            return 0;
        }
        for (LyricLine line : this.active.found().lines()) {
            if (line.text().isBlank()) continue;
            long remaining = line.timestampMs() - playbackMs - (long)this.offsetMs(settings);
            return remaining > 0L && remaining <= 3000L ? (int)((remaining + 999L) / 1000L) : 0;
        }
        return 0;
    }

    public double lineProgress(long playbackMs, ClientConfig.MusicSettings settings) {
        ActiveLine line = this.current(playbackMs, settings);
        return line == null ? 0.0 : Math.clamp((double)(playbackMs + (long)this.offsetMs(settings) - line.line().timestampMs()) / (double)Math.max(1L, line.endMs() - line.line().timestampMs()), 0.0, 1.0);
    }

    private static List<LyricLine> synced(String lrc) {
        List<LyricLine> lines = LrcParser.parse(lrc);
        return lines.stream().anyMatch(line -> !line.text().isBlank()) ? lines : List.of();
    }

    private static long firstCue(List<LyricLine> lines) {
        return lines.stream().filter(l -> !l.text().isBlank()).mapToLong(LyricLine::timestampMs).findFirst().orElse(-1L);
    }

    private CompletableFuture<Found> fetchSources(String title, String artist, long duration, long token, String source, String preferred) {
        if ("KUGOU".equals(source)) {
            return this.fetchKugou(title, artist, duration, token).exceptionally(f -> LyricsService.failed("KuGou", f));
        }
        if ("NETEASE".equals(source)) {
            return this.fetchNetease(title, artist, duration, token).exceptionally(f -> LyricsService.failed("NetEase", f));
        }
        if ("LYRICS_OVH".equals(source)) {
            return this.fetchOvh(title, artist, token).exceptionally(f -> LyricsService.failed("lyrics.ovh", f));
        }
        CompletionStage primary = this.fetch(title, artist, duration, token, preferred).exceptionally(f -> LyricsService.failed("LRCLIB", f));
        if ("LRCLIB".equals(source) || !preferred.isEmpty()) {
            return primary;
        }
        if (((CompletableFuture)primary).isDone() && !((Found)((CompletableFuture)primary).join()).lines().isEmpty()) {
            return primary;
        }
        CompletionStage kugou = this.fetchKugou(title, artist, duration, token).exceptionally(f -> LyricsService.failed("KuGou", f));
        if (((CompletableFuture)kugou).isDone() && !((Found)((CompletableFuture)kugou).join()).lines().isEmpty()) {
            return kugou;
        }
        CompletionStage netease = this.fetchNetease(title, artist, duration, token).exceptionally(f -> LyricsService.failed("NetEase", f));
        List<CompletionStage> providers = List.of(primary, kugou, netease);
        CompletableFuture<Found> result = new CompletableFuture<Found>();
        for (CompletableFuture completableFuture : providers) {
            completableFuture.thenAccept(f -> {
                if (!f.lines().isEmpty()) {
                    result.complete((Found)f);
                }
            });
        }
        CompletableFuture.allOf(new CompletableFuture[]{primary, kugou, netease}).thenRun(() -> this.lambda$fetchSources$7(result, providers, title, artist, token, (CompletableFuture)primary));
        return result;
    }

    private CompletableFuture<Found> fetchOvh(String title, String artist, long token) {
        if (artist.isBlank()) {
            return CompletableFuture.completedFuture(Found.empty("lyrics.ovh", Issue.MISSING_METADATA));
        }
        LyricsIdentity.Track identity = LyricsIdentity.track(title, artist);
        String url = "https://api.lyrics.ovh/v1/" + LyricsService.encode(identity.artist()).replace("+", "%20") + "/" + LyricsService.encode(identity.title()).replace("+", "%20");
        return this.request(url, token).thenApply(json -> {
            List<LyricLine> plain = json.isJsonObject() ? LyricsService.plainLines(LyricsService.string(json.getAsJsonObject(), "lyrics")) : List.of();
            return new Found(List.of(), "lyrics.ovh", "", "", 0.0, plain.isEmpty() ? Issue.NO_MATCH : Issue.UNSYNCED, plain);
        });
    }

    private CompletableFuture<Found> fetchNetease(String title, String artist, long duration, long token) {
        if (artist.isBlank() || duration <= 0L) {
            return CompletableFuture.completedFuture(Found.empty("NetEase", Issue.MISSING_METADATA));
        }
        LyricsIdentity.Track identity = LyricsIdentity.track(title, artist);
        String form = "s=" + LyricsService.encode(identity.artist() + " " + identity.title()) + "&type=1&limit=10&offset=0";
        return this.request("https://music.163.com/api/search/get", token, form).thenCompose(json -> this.downloadNetease(LyricsService.selectNeteaseCandidates(json, title, artist, duration), 0, token));
    }

    private CompletableFuture<Found> downloadNetease(List<JsonObject> candidates, int index, long token) {
        if (index >= candidates.size() || index >= 3) {
            return CompletableFuture.completedFuture(Found.empty("NetEase", Issue.NO_MATCH));
        }
        JsonObject item = candidates.get(index);
        String id = LyricsService.numericId(item);
        return this.request("https://music.163.com/api/song/lyric?id=" + id + "&lv=1&kv=-1&tv=-1&yv=-1", token).thenCompose(json -> {
            String lrc = "";
            if (json.isJsonObject() && json.getAsJsonObject().has("lrc") && json.getAsJsonObject().get("lrc").isJsonObject()) {
                lrc = LyricsService.string(json.getAsJsonObject().getAsJsonObject("lrc"), "lyric");
            }
            List<LyricLine> lines = LyricsService.synced(lrc);
            if (json.isJsonObject()) {
                for (String field : List.of("yrc", "klyric")) {
                    List<LyricLine> timed;
                    JsonElement part = json.getAsJsonObject().get(field);
                    if (part == null || !part.isJsonObject() || (timed = WordTiming.provider(LyricsService.string(part.getAsJsonObject(), "lyric"), false)).isEmpty()) continue;
                    lines = timed;
                    break;
                }
            }
            if (lrc.contains("\u7eaf\u97f3\u4e50") || lrc.contains("\u8bf7\u6b23\u8d4f")) {
                lines = List.of();
            }
            if (lines.isEmpty()) {
                return this.downloadNetease(candidates, index + 1, token);
            }
            return CompletableFuture.completedFuture(new Found(lines, "NetEase", id, "", LyricsService.number(item, "duration") / 1000.0, Issue.NONE));
        });
    }

    static List<JsonObject> selectNeteaseCandidates(JsonElement json, String title, String artist, long duration) {
        if (duration <= 0L || artist.isBlank() || !json.isJsonObject()) {
            return List.of();
        }
        JsonObject root = json.getAsJsonObject();
        if (!root.has("result") || !root.get("result").isJsonObject()) {
            return List.of();
        }
        JsonObject result = root.getAsJsonObject("result");
        if (!result.has("songs") || !result.get("songs").isJsonArray()) {
            return List.of();
        }
        ArrayList<JsonObject> candidates = new ArrayList<JsonObject>();
        for (JsonElement value : result.getAsJsonArray("songs")) {
            JsonObject song;
            if (!value.isJsonObject() || !(song = value.getAsJsonObject()).has("artists") || !song.get("artists").isJsonArray()) continue;
            ArrayList<String> names = new ArrayList<String>();
            for (JsonElement a : song.getAsJsonArray("artists")) {
                if (!a.isJsonObject()) continue;
                names.add(LyricsService.string(a.getAsJsonObject(), "name"));
            }
            double length = LyricsService.number(song, "duration");
            if (!LyricsIdentity.matches(title, artist, LyricsService.string(song, "name"), String.join((CharSequence)", ", names)) || !Double.isFinite(length) || !(length > 0.0) || !(Math.abs(length - (double)duration) <= 2000.0) || LyricsService.numericId(song).isEmpty()) continue;
            candidates.add(song);
        }
        candidates.sort(Comparator.comparingDouble(c -> Math.abs(LyricsService.number(c, "duration") - (double)duration)));
        return List.copyOf(candidates.subList(0, Math.min(3, candidates.size())));
    }

    static List<LyricLine> plainLines(String text) {
        if (text == null || text.isBlank() || text.length() > 0x100000) {
            return List.of();
        }
        ArrayList<LyricLine> lines = new ArrayList<LyricLine>();
        for (String raw : text.split("\\R")) {
            String line = raw.replaceAll("\u00a7.", "").replaceAll("[\\p{Cntrl}\\p{Cf}]", " ").strip();
            if (line.isBlank()) continue;
            int end = line.offsetByCodePoints(0, Math.min(512, line.codePointCount(0, line.length())));
            lines.add(new LyricLine(-1L, line.substring(0, end)));
            if (lines.size() >= 8192) break;
        }
        return List.copyOf(lines);
    }

    private CompletableFuture<Found> fetchKugou(String title, String artist, long duration, long token) {
        if (artist.isBlank() || duration <= 0L) {
            return CompletableFuture.completedFuture(Found.empty("KuGou", Issue.MISSING_METADATA));
        }
        LyricsIdentity.Track identity = LyricsIdentity.track(title, artist);
        String url = "https://lyrics.kugou.com/search?ver=1&client=pc&man=yes&keyword=" + LyricsService.encode(identity.artist() + " - " + identity.title()) + "&duration=" + duration;
        return this.request(url, token).thenCompose(json -> this.downloadKugou(LyricsService.selectKugouCandidates(json, title, artist, duration), 0, token));
    }

    private CompletableFuture<Found> downloadKugou(List<JsonObject> candidates, int index, long token) {
        if (index >= candidates.size() || index >= 3) {
            return CompletableFuture.completedFuture(Found.empty("KuGou", Issue.NO_MATCH));
        }
        JsonObject item = candidates.get(index);
        String query = "&charset=utf8&id=" + LyricsService.encode(LyricsService.string(item, "id")) + "&accesskey=" + LyricsService.encode(LyricsService.string(item, "accesskey"));
        return ((CompletableFuture)((CompletableFuture)this.request("https://lyrics.kugou.com/download?ver=1&client=pc&fmt=krc" + query, token).handle((json, error) -> error == null ? LyricsService.parseKugou(json) : List.of())).thenCompose(words -> words.stream().anyMatch(LyricLine::wordTimed) ? CompletableFuture.completedFuture(words) : this.request("https://lyrics.kugou.com/download?ver=1&client=pc&fmt=lrc" + query, token).thenApply(LyricsService::parseKugou))).thenCompose(lines -> lines.isEmpty() ? this.downloadKugou(candidates, index + 1, token) : CompletableFuture.completedFuture(new Found((List<LyricLine>)lines, "KuGou", LyricsService.string(item, "id"), "", LyricsService.number(item, "duration") / 1000.0, Issue.NONE)));
    }

    static List<JsonObject> selectKugouCandidates(JsonElement json, String title, String artist, long duration) {
        if (json == null || !json.isJsonObject() || !json.getAsJsonObject().has("candidates") || !json.getAsJsonObject().get("candidates").isJsonArray() || duration <= 0L || artist.isBlank()) {
            return List.of();
        }
        ArrayList<JsonObject> candidates = new ArrayList<JsonObject>();
        for (JsonElement element : json.getAsJsonObject().getAsJsonArray("candidates")) {
            if (!element.isJsonObject()) continue;
            JsonObject c2 = element.getAsJsonObject();
            double length = LyricsService.number(c2, "duration");
            if (!LyricsIdentity.matches(title, artist, LyricsService.string(c2, "song"), LyricsService.string(c2, "singer")) || !Double.isFinite(length) || !(length > 0.0) || !(Math.abs(length - (double)duration) <= 2000.0) || !LyricsService.string(c2, "id").matches("[0-9]{1,20}") || !LyricsService.string(c2, "accesskey").matches("[A-Za-z0-9]{1,128}")) continue;
            candidates.add(c2);
        }
        candidates.sort(Comparator.comparingDouble(c -> Math.abs(LyricsService.number(c, "duration") - (double)duration)));
        return List.copyOf(candidates.subList(0, Math.min(3, candidates.size())));
    }

    static List<LyricLine> parseKugou(JsonElement json) {
        if (json == null || !json.isJsonObject()) {
            return List.of();
        }
        String encoded = LyricsService.string(json.getAsJsonObject(), "content");
        if (encoded.length() > 0x100000) {
            return List.of();
        }
        try {
            byte[] data = Base64.getDecoder().decode(encoded);
            if (data.length >= 4 && data[0] == 107 && data[1] == 114 && data[2] == 99 && data[3] == 49) {
                return WordTiming.decodeKrc(data);
            }
            return LyricsService.synced(new String(data, StandardCharsets.UTF_8));
        }
        catch (IllegalArgumentException malformed) {
            return List.of();
        }
    }

    private CompletableFuture<Found> fetch(String title, String artist, long duration, long token, String preferred) {
        if (preferred.matches("[0-9]{1,20}")) {
            return this.request("https://lrclib.net/api/get/" + preferred, token).thenCompose(json -> {
                Found found = LyricsService.matchOne(json, title, artist, duration);
                return found.lines().isEmpty() ? this.fetch(title, artist, duration, token, "") : CompletableFuture.completedFuture(found);
            });
        }
        LyricsIdentity.Track identity = LyricsIdentity.track(title, artist);
        String query = "?track_name=" + LyricsService.encode(identity.title()) + "&artist_name=" + LyricsService.encode(identity.artist());
        if (duration > 0L) {
            query = query + "&duration=" + (double)duration / 1000.0;
        }
        CompletableFuture<JsonObject> exact = artist.isBlank() || duration <= 0L || duration > 3600000L ? CompletableFuture.completedFuture(new JsonObject()) : this.request("https://lrclib.net/api/get" + query, token);
        return exact.thenCompose(json -> {
            Found found = LyricsService.matchOne(json, title, artist, duration);
            if (!found.lines().isEmpty()) {
                return CompletableFuture.completedFuture(found);
            }
            return this.search(title, artist, duration, token, false).thenApply(results -> {
                Found best = LyricsService.choose(results, artist, duration);
                return !best.hasText() && found.issue() != Issue.NO_MATCH ? found : best;
            });
        });
    }

    private CompletableFuture<List<Found>> search(String title, String artist, long duration, long token, boolean all) {
        LyricsIdentity.Track identity = LyricsIdentity.track(title, artist);
        String query = "https://lrclib.net/api/search?q=" + LyricsService.encode((identity.title() + " " + identity.artist()).strip());
        return this.request(query, token).thenCompose(json -> {
            List<Found> first = LyricsService.matches(json, title, artist, duration);
            if (artist.isBlank() || !all && first.stream().anyMatch(f -> !f.lines().isEmpty())) {
                return CompletableFuture.completedFuture(first);
            }
            String structured = "https://lrclib.net/api/search?track_name=" + LyricsService.encode(identity.title()) + "&artist_name=" + LyricsService.encode(identity.artist());
            return this.request(structured, token).handle((more, failure) -> {
                if (failure != null && first.isEmpty()) {
                    throw new CompletionException((Throwable)failure);
                }
                ArrayList<Found> result = new ArrayList<Found>(first);
                if (failure == null) {
                    result.addAll(LyricsService.matches(more, title, artist, duration));
                }
                return result;
            });
        });
    }

    private static Found matchOne(JsonElement json, String title, String artist, long duration) {
        JsonArray array = new JsonArray();
        array.add(json);
        return LyricsService.choose(LyricsService.matches((JsonElement)array, title, artist, duration), artist, duration);
    }

    static List<LyricLine> selectSearchResult(JsonElement results, String title, String artist, long duration) {
        return LyricsService.choose(LyricsService.matches(results, title, artist, duration), artist, duration).lines();
    }

    private static List<Found> matches(JsonElement results, String title, String artist, long duration) {
        if (results == null || !results.isJsonArray() || LyricsIdentity.normalize(title).isEmpty()) {
            return List.of();
        }
        ArrayList<Found> candidates = new ArrayList<Found>();
        for (JsonElement element : results.getAsJsonArray()) {
            String id;
            double length;
            JsonObject item;
            if (!element.isJsonObject() || !LyricsIdentity.matches(title, artist, LyricsService.string(item = element.getAsJsonObject(), "trackName"), LyricsService.string(item, "artistName")) || !Double.isFinite(length = LyricsService.number(item, "duration")) || length < 0.0 || duration > 0L && (length <= 0.0 || Math.abs(length - (double)duration / 1000.0) > 2.0)) continue;
            List<LyricLine> lines = LyricsService.synced(LyricsService.string(item, "syncedLyrics"));
            Issue issue = !lines.isEmpty() ? Issue.NONE : (!LyricsService.string(item, "plainLyrics").isBlank() ? Issue.UNSYNCED : (item.has("instrumental") && item.get("instrumental").isJsonPrimitive() && item.get("instrumental").getAsString().equals("true") ? Issue.INSTRUMENTAL : Issue.NO_MATCH));
            String string = id = item.has("id") && item.get("id").isJsonPrimitive() ? item.get("id").getAsString() : "";
            if (!id.matches("[0-9]{1,20}")) {
                id = "";
            }
            candidates.add(new Found(lines, "LRCLIB", id, LyricsService.metadata(LyricsService.string(item, "albumName")), length, issue, lines.isEmpty() ? LyricsService.plainLines(LyricsService.string(item, "plainLyrics")) : List.of()));
        }
        if (artist.isBlank()) {
            HashSet<String> performers = new HashSet<String>();
            for (JsonElement e : results.getAsJsonArray()) {
                if (!e.isJsonObject()) continue;
                JsonObject item = e.getAsJsonObject();
                double length = LyricsService.number(item, "duration");
                if (!LyricsIdentity.normalize(LyricsService.string(item, "trackName")).equals(LyricsIdentity.normalize(title)) || duration > 0L && (!(length > 0.0) || !(Math.abs(length - (double)duration / 1000.0) <= 2.0))) continue;
                performers.add(LyricsIdentity.normalize(LyricsService.string(item, "artistName")));
            }
            if (performers.size() > 1) {
                return List.of();
            }
        }
        return candidates;
    }

    private static Found choose(List<Found> candidates, String artist, long duration) {
        if (candidates.isEmpty()) {
            return Found.empty("LRCLIB", Issue.NO_MATCH);
        }
        if (duration <= 0L) {
            double first = candidates.getFirst().duration();
            if (candidates.stream().anyMatch(f -> Math.abs(f.duration() - first) > 2.0)) {
                return Found.empty("LRCLIB", Issue.NO_MATCH);
            }
        }
        return candidates.stream().min(Comparator.comparingInt(f -> !f.lines().isEmpty() ? 0 : (f.hasText() ? 1 : 2)).thenComparingDouble(f -> duration > 0L ? Math.abs(f.duration() - (double)duration / 1000.0) : 0.0)).orElseThrow();
    }

    private CompletableFuture<JsonElement> request(String url, long token) {
        return this.request(url, token, null);
    }

    private synchronized CompletableFuture<JsonElement> request(String url, long token, String form) {
        if (this.closed || token != this.generation) {
            return CompletableFuture.failedFuture(new CancellationException());
        }
        URI uRI = URI.create(url);
        PrivacyGuard.checkUri(uRI);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uRI).timeout(Duration.ofSeconds(6L)).header("User-Agent", "BloodHelper/1.0 (Fabric visual client)").header("Accept", "application/json");
        HttpRequest request = form == null ? builder.GET().build() : builder.header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8)).build();
        CompletableFuture response = this.http.sendAsync(request, ignored -> new LimitedBody());
        this.requests.add(response);
        response.whenComplete((ignored, failure) -> {
            LyricsService lyricsService = this;
            synchronized (lyricsService) {
                this.requests.remove(response);
            }
        });
        return response.orTimeout(7L, TimeUnit.SECONDS).thenApply(result -> {
            if (result.statusCode() == 404) {
                return new JsonObject();
            }
            if (result.statusCode() != 200) {
                throw new LyricsHttpException(result.statusCode());
            }
            try {
                return JsonParser.parseString((String)new String((byte[])result.body(), StandardCharsets.UTF_8));
            }
            catch (RuntimeException invalid) {
                throw new IllegalArgumentException("Invalid lyrics response", invalid);
            }
        });
    }

    private static Found failed(String source, Throwable failure) {
        Issue issue;
        while (failure.getCause() != null && (failure instanceof CompletionException || failure instanceof ExecutionException)) {
            failure = failure.getCause();
        }
        if (failure instanceof LyricsHttpException) {
            LyricsHttpException http = (LyricsHttpException)failure;
            issue = http.code == 429 ? Issue.RATE_LIMITED : Issue.HTTP_ERROR;
        } else {
            issue = failure instanceof IllegalArgumentException ? Issue.INVALID_RESPONSE : Issue.NETWORK;
        }
        Issue issue2 = issue;
        return Found.empty(source, issue2);
    }

    private static boolean isError(Issue issue) {
        return issue == Issue.RATE_LIMITED || issue == Issue.HTTP_ERROR || issue == Issue.NETWORK || issue == Issue.INVALID_RESPONSE;
    }

    private static String numericId(JsonObject value) {
        JsonElement element = value.get("id");
        String id = element != null && element.isJsonPrimitive() ? element.getAsString() : "";
        return id.matches("[0-9]{1,20}") ? id : "";
    }

    private static String string(JsonObject value, String name) {
        JsonElement element = value.get(name);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() ? element.getAsString() : "";
    }

    private static double number(JsonObject value, String name) {
        try {
            return value.has(name) && !value.get(name).isJsonNull() ? value.get(name).getAsDouble() : 0.0;
        }
        catch (RuntimeException ignored) {
            return 0.0;
        }
    }

    private static String metadata(String value) {
        String text = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFC).replaceAll("[\\p{Cntrl}]", " ").strip();
        return text.substring(0, text.offsetByCodePoints(0, Math.min(text.codePointCount(0, text.length()), 300)));
    }

    private static String identity(String title, String artist, long duration) {
        return title.toLowerCase(Locale.ROOT) + "\u0000" + artist.toLowerCase(Locale.ROOT) + "\u0000" + duration / 1000L;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void cancelRequests() {
        List<CompletableFuture<?>> pending = List.copyOf(this.requests);
        this.requests.clear();
        pending.forEach(f -> f.cancel(true));
    }

    private void reset() {
        ++this.generation;
        this.trackKey = "";
        this.identity = "";
        this.trackDurationMs = 0L;
        this.active = Lyrics.empty(Status.DISABLED);
        this.alternatives = List.of();
        this.alternativesLoading = false;
        this.alternativesIssue = Issue.NONE;
        this.cancelRequests();
    }

    @Override
    public synchronized void close() {
        this.closed = true;
        this.reset();
        this.cache.clear();
        this.http.shutdownNow();
    }

    private /* synthetic */ void lambda$fetchSources$7(CompletableFuture result, List providers, String title, String artist, long token, CompletableFuture primary) {
        if (result.isDone()) {
            return;
        }
        for (CompletableFuture provider : providers) {
            if (!((Found)provider.join()).hasText()) continue;
            result.complete((Found)provider.join());
            return;
        }
        ((CompletableFuture)this.fetchOvh(title, artist, token).exceptionally(f -> LyricsService.failed("lyrics.ovh", f))).thenAccept(plain -> {
            if (plain.hasText()) {
                result.complete(plain);
                return;
            }
            Found first = (Found)primary.join();
            if (first.issue() == Issue.INSTRUMENTAL || LyricsService.isError(first.issue())) {
                result.complete(first);
            } else {
                Found failure = providers.stream().map(CompletableFuture::join).filter(f -> LyricsService.isError(f.issue())).findFirst().orElse((Found)plain);
                result.complete(LyricsService.isError(failure.issue()) ? failure : first);
            }
        });
    }

    public static enum Status {
        DISABLED,
        LOADING,
        READY,
        NOT_FOUND,
        ERROR;

    }

    private record Lyrics(Found found, Status status, long lookupMs) {
        static Lyrics empty(Status status) {
            return new Lyrics(Found.empty("", Issue.NONE), status, 0L);
        }
    }

    public static enum Issue {
        NONE,
        NO_MATCH,
        UNSYNCED,
        INSTRUMENTAL,
        MISSING_METADATA,
        RATE_LIMITED,
        NETWORK,
        HTTP_ERROR,
        INVALID_RESPONSE;

    }

    private record Found(List<LyricLine> lines, String source, String id, String album, double duration, Issue issue, List<LyricLine> plain) {
        Found(List<LyricLine> lines, String source, String id, String album, double duration, Issue issue) {
            this(lines, source, id, album, duration, issue, List.of());
        }

        boolean hasText() {
            return !this.lines.isEmpty() || !this.plain.isEmpty();
        }

        static Found empty(String source, Issue issue) {
            return new Found(List.of(), source, "", "", 0.0, issue);
        }
    }

    public record ActiveLine(int index, LyricLine line, long endMs) {
    }

    private static final class LyricsHttpException
    extends RuntimeException {
        final int code;

        LyricsHttpException(int code) {
            super("Lyrics HTTP " + code);
            this.code = code;
        }
    }

    private static final class LimitedBody
    implements HttpResponse.BodySubscriber<byte[]> {
        private final HttpResponse.BodySubscriber<byte[]> delegate = HttpResponse.BodySubscribers.ofByteArray();
        private Flow.Subscription subscription;
        private long received;
        private boolean ended;

        private LimitedBody() {
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
            if (this.ended) {
                return;
            }
            for (ByteBuffer buffer : buffers) {
                this.received += (long)buffer.remaining();
            }
            if (this.received > 0x100000L) {
                this.ended = true;
                this.subscription.cancel();
                this.delegate.onError(new IOException("Lyrics response exceeds limit"));
            } else {
                this.delegate.onNext((byte[])buffers);
            }
        }

        @Override
        public void onError(Throwable failure) {
            if (!this.ended) {
                this.ended = true;
                this.delegate.onError(failure);
            }
        }

        @Override
        public void onComplete() {
            if (!this.ended) {
                this.ended = true;
                this.delegate.onComplete();
            }
        }
    }

    public record Variant(String id, String album, double durationSeconds, long firstCueMs) {
    }
}

