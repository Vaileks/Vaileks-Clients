/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.resourcepack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.resourcepack.PackOfferPolicy;
import ru.bloodhelper.client.vl.PrivacyGuard;

public final class BloodstonePackPreloader {
    private static final String HOST = "http://resources2.bloodstone.boo:8880/";
    private static final String RELEASE = "https://api.github.com/repos/Trard/bloodstone3/releases/latest";
    private static final String MODEL_FALLBACK = "BetterModel-hub2-1461e15f0193.zip";
    private static final long MAX_PACK = 0x4000000L;
    private static final Gson JSON = new Gson();
    private static final Map<String, Path> VERIFIED = new ConcurrentHashMap<String, Path>();
    private static final Map<String, Entry> entries = new HashMap<String, Entry>();
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "BloodHelper pack preload");
        t.setDaemon(true);
        t.setPriority(1);
        return t;
    });
    private static final Pattern MODEL = Pattern.compile("href=\"(BetterModel-hub2-([a-f0-9]{12,40})\\.zip)\"[^\\r\\n]*?</a>\\s+(\\d{2}-[A-Za-z]{3}-\\d{4} \\d{2}:\\d{2})");
    private static final DateTimeFormatter INDEX_DATE = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm", Locale.ENGLISH);
    private static volatile boolean enabled;
    private static volatile long generation;
    private static volatile HttpURLConnection active;
    private static volatile Status status;
    private static Path directory;
    private static boolean loaded;

    private BloodstonePackPreloader() {
    }

    public static void initialize(Path gameDirectory) {
        directory = gameDirectory.resolve("config/bloodhelper/pack-cache");
    }

    public static Status status() {
        return status;
    }

    public static synchronized void settingsChanged(boolean value) {
        if (directory == null || enabled == value) {
            return;
        }
        enabled = value;
        long token = ++generation;
        if (!value) {
            HttpURLConnection connection = active;
            if (connection != null) {
                connection.disconnect();
            }
            status = new Status(State.IDLE, 0, 0.0);
        } else {
            status = new Status(State.DOWNLOADING, 0, 0.0);
            WORKER.execute(() -> BloodstonePackPreloader.preload(token));
        }
    }

    public static Path cached(String hash) {
        return hash != null && hash.matches("(?i)[a-f0-9]{40}") ? VERIFIED.get(hash.toLowerCase(Locale.ROOT)) : null;
    }

    private static void preload(long token) {
        block7: {
            int ready = 0;
            try {
                BloodstonePackPreloader.check(token);
                Files.createDirectories(directory, new FileAttribute[0]);
                if (!loaded) {
                    BloodstonePackPreloader.loadIndex(token);
                    loaded = true;
                }
                for (String key : List.of("bloodstone", "bettermodel")) {
                    BloodstonePackPreloader.check(token);
                    try {
                        Source source = key.equals("bloodstone") ? BloodstonePackPreloader.bloodstone(token) : BloodstonePackPreloader.betterModel(token);
                        BloodstonePackPreloader.download(source, token, ready);
                        ++ready;
                    }
                    catch (IOException error) {
                        BloodstonePackPreloader.check(token);
                        Entry old = entries.get(key);
                        if (old != null && VERIFIED.containsKey(old.sha1())) {
                            ++ready;
                        }
                        LoggerFactory.getLogger((String)"BloodHelper/Packs").debug("Could not refresh {} resource pack", (Object)key, (Object)error);
                    }
                    status = new Status(State.DOWNLOADING, ready, (double)ready / 2.0);
                }
                BloodstonePackPreloader.check(token);
                status = new Status(ready == 2 ? State.READY : State.ERROR, ready, (double)ready / 2.0);
            }
            catch (IOException ignored) {
                if (!enabled || token != generation) break block7;
                status = new Status(State.ERROR, ready, (double)ready / 2.0);
            }
        }
    }

    private static Source bloodstone(long token) throws IOException {
        try {
            JsonObject release = JsonParser.parseString((String)BloodstonePackPreloader.text(RELEASE, token)).getAsJsonObject();
            for (JsonElement item : release.getAsJsonArray("assets")) {
                String url;
                JsonObject asset = item.getAsJsonObject();
                if (!"BloodStone.zip".equals(asset.get("name").getAsString()) || !(url = asset.get("browser_download_url").getAsString()).startsWith("https://github.com/Trard/bloodstone3/releases/download/")) continue;
                String digest = asset.has("digest") && !asset.get("digest").isJsonNull() ? asset.get("digest").getAsString() : "";
                return new Source("bloodstone", url, digest.matches("sha256:[a-f0-9]{64}") ? digest.substring(7) : "", "");
            }
        }
        catch (IOException | RuntimeException ignored) {
            BloodstonePackPreloader.check(token);
        }
        return new Source("bloodstone", "http://resources2.bloodstone.boo:8880/BloodStone.zip", "", "");
    }

    private static Source betterModel(long token) throws IOException {
        String prefix;
        String filename;
        block3: {
            filename = MODEL_FALLBACK;
            prefix = "1461e15f0193";
            try {
                Matcher matcher = MODEL.matcher(BloodstonePackPreloader.text(HOST, token));
                LocalDateTime latest = LocalDateTime.MIN;
                while (matcher.find()) {
                    LocalDateTime time = LocalDateTime.parse(matcher.group(3), INDEX_DATE);
                    if (!time.isAfter(latest)) continue;
                    latest = time;
                    filename = matcher.group(1);
                    prefix = matcher.group(2);
                }
            }
            catch (IOException | RuntimeException ignored) {
                BloodstonePackPreloader.check(token);
                Entry previous = entries.get("bettermodel");
                if (previous == null) break block3;
                return new Source("bettermodel", previous.url(), previous.sha256(), previous.sha1());
            }
        }
        return new Source("bettermodel", HOST + filename, "", prefix);
    }

    /*
     * Exception decompiling
     */
    private static void download(Source source, long token, int complete) throws IOException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
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

    private static HttpURLConnection connect(String url, long token, Entry previous) throws IOException {
        for (int redirect = 0; redirect < 6; ++redirect) {
            HttpURLConnection connection;
            BloodstonePackPreloader.check(token);
            URI uri = PackOfferPolicy.remoteUrl(url);
            if (uri == null) {
                throw new IOException("Invalid resource URL");
            }
            URI uRI = uri;
            PrivacyGuard.checkUri(uRI);
            URL uRL = uRI.toURL();
            PrivacyGuard.checkUrlObject(uRL);
            active = connection = (HttpURLConnection)uRL.openConnection();
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(12000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "BloodHelper resource-pack preloader");
            if (previous != null) {
                if (previous.etag() != null) {
                    connection.setRequestProperty("If-None-Match", previous.etag());
                }
                if (previous.modified() > 0L) {
                    connection.setIfModifiedSince(previous.modified());
                }
            }
            try {
                int code = connection.getResponseCode();
                if (code != 301 && code != 302 && code != 303 && code != 307 && code != 308) {
                    return connection;
                }
                String next = connection.getHeaderField("Location");
                if (next == null) {
                    throw new IOException("Missing redirect");
                }
                url = uri.resolve(next).toString();
            }
            catch (IOException error) {
                connection.disconnect();
                throw error;
            }
            connection.disconnect();
        }
        throw new IOException("Too many redirects");
    }

    private static String text(String url, long token) throws IOException {
        HttpURLConnection connection = BloodstonePackPreloader.connect(url, token, null);
        try {
            String string;
            block12: {
                if (connection.getResponseCode() != 200) {
                    throw new IOException("Metadata HTTP " + connection.getResponseCode());
                }
                InputStream in = connection.getInputStream();
                try {
                    byte[] bytes = in.readNBytes(262145);
                    BloodstonePackPreloader.check(token);
                    if (bytes.length > 262144) {
                        throw new IOException("Metadata too large");
                    }
                    string = new String(bytes, StandardCharsets.UTF_8);
                    if (in == null) break block12;
                }
                catch (Throwable throwable) {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                in.close();
            }
            return string;
        }
        finally {
            connection.disconnect();
            if (active == connection) {
                active = null;
            }
        }
    }

    private static void loadIndex(long token) throws IOException {
        Path path = directory.resolve("index.json");
        if (!Files.isRegularFile(path, new LinkOption[0]) || Files.size(path) > 16384L) {
            return;
        }
        try {
            Entry[] stored = (Entry[])JSON.fromJson(Files.readString(path), Entry[].class);
            if (stored == null || stored.length > 2) {
                return;
            }
            for (Entry entry : stored) {
                Path pack;
                BloodstonePackPreloader.check(token);
                if (entry == null || !"bloodstone".equals(entry.key()) && !"bettermodel".equals(entry.key()) || entry.sha1() == null || !entry.sha1().matches("[a-f0-9]{40}") || entry.sha256() == null || !entry.sha256().matches("[a-f0-9]{64}") || !BloodstonePackPreloader.allowedSource(entry.url()) || !Files.isRegularFile(pack = directory.resolve(entry.sha1() + ".zip"), new LinkOption[0]) || Files.size(pack) > 0x4000000L) continue;
                MessageDigest hash = BloodstonePackPreloader.digest("SHA-1");
                try (InputStream in = Files.newInputStream(pack, new OpenOption[0]);){
                    int n;
                    byte[] buffer = new byte[32768];
                    while ((n = in.read(buffer)) != -1) {
                        BloodstonePackPreloader.check(token);
                        hash.update(buffer, 0, n);
                    }
                }
                if (!HexFormat.of().formatHex(hash.digest()).equals(entry.sha1())) continue;
                BloodstonePackPreloader.validateZip(pack);
                entries.put(entry.key(), entry);
                VERIFIED.put(entry.sha1(), pack);
            }
            try (Stream<Path> files = Files.list(directory);){
                for (Path file : files.limit(64L).toList()) {
                    String name = file.getFileName().toString();
                    if (name.matches("[a-f0-9]{40}\\.zip") && !VERIFIED.containsKey(name.substring(0, 40))) {
                        Files.deleteIfExists(file);
                        continue;
                    }
                    if (!name.startsWith("pack-") || !name.endsWith(".part")) continue;
                    Files.deleteIfExists(file);
                }
            }
        }
        catch (IOException | RuntimeException ignored) {
            BloodstonePackPreloader.check(token);
        }
    }

    private static boolean allowedSource(String url) {
        return url != null && (url.startsWith(HOST) || url.startsWith("https://github.com/Trard/bloodstone3/releases/download/"));
    }

    private static void validateZip(Path path) throws IOException {
        try (ZipFile zip = new ZipFile(path.toFile());){
            ZipEntry metadata = zip.getEntry("pack.mcmeta");
            if (metadata == null || metadata.isDirectory() || metadata.getSize() > 65536L || zip.size() > 50000) {
                throw new IOException("Not a resource pack");
            }
            try (InputStream in = zip.getInputStream(metadata);){
                byte[] bytes = in.readNBytes(65537);
                if (bytes.length > 65536 || !JsonParser.parseString((String)new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject().has("pack")) {
                    throw new IOException("Invalid pack metadata");
                }
            }
            catch (RuntimeException error) {
                throw new IOException("Invalid pack metadata", error);
            }
        }
    }

    private static void saveIndex() throws IOException {
        Path temporary = directory.resolve("index.json.tmp");
        Files.writeString(temporary, (CharSequence)JSON.toJson(entries.values()), StandardCharsets.UTF_8, new OpenOption[0]);
        try {
            Files.move(temporary, directory.resolve("index.json"), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (AtomicMoveNotSupportedException error) {
            Files.move(temporary, directory.resolve("index.json"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static MessageDigest digest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static void check(long token) throws IOException {
        if (!enabled || token != generation || Thread.currentThread().isInterrupted()) {
            throw new IOException("Cancelled");
        }
    }

    public static void close() {
        BloodstonePackPreloader.settingsChanged(false);
        WORKER.shutdownNow();
    }

    static {
        status = new Status(State.IDLE, 0, 0.0);
    }

    public record Status(State state, int ready, double progress) {
    }

    public static enum State {
        IDLE,
        DOWNLOADING,
        READY,
        ERROR;

    }

    private record Source(String key, String url, String sha256, String sha1Prefix) {
    }

    private record Entry(String key, String url, String sha1, String sha256, String etag, long modified) {
    }
}

