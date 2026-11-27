/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.compress.archivers.ArchiveEntry
 *  org.apache.commons.compress.archivers.tar.TarArchiveEntry
 *  org.apache.commons.compress.archivers.tar.TarArchiveInputStream
 */
package ru.bloodhelper.client.assistant;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import ru.bloodhelper.client.vl.PrivacyGuard;

public final class AssistantAssets {
    public static final String MODEL_NAME = "Qwen2.5 0.5B Instruct \u00b7 Q4_0";
    public static final String MODEL_PAGE = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF";
    public static final String ENGINE_PAGE = "https://github.com/ggml-org/llama.cpp/releases/tag/b11719";
    public static final long MODEL_BYTES = 428730208L;
    public static final long ENGINE_BYTES = 19662517L;
    private static final String MODEL_SHA = "7671c0c304e6ce5a7fc577bcb12aba01e2c155cc2efd29b2213c95b18edaf6ed";
    private static final String ENGINE_SHA = "1b1234d208a8b851cc27cd3d96b850d1547a4e5cfc5cd75f0a9fdd1d4c5bea20";
    private static final String MODEL_URL = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/9217f5db79a29953eb74d5343926648285ec7e67/qwen2.5-0.5b-instruct-q4_0.gguf";
    private static final String ENGINE_URL = "https://github.com/ggml-org/llama.cpp/releases/download/b11719/llama-b11719-bin-win-cpu-x64.zip";
    private final Path root;
    private volatile InputStream downloading;

    public AssistantAssets(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    static Engine engine(String os, String arch) {
        os = os.toLowerCase(Locale.ROOT);
        if (!(arch = arch.toLowerCase(Locale.ROOT)).equals("amd64") && !arch.equals("x86_64")) {
            return null;
        }
        if (os.startsWith("windows")) {
            return new Engine("llama-b11719-win-cpu-x64.zip", ENGINE_URL, 19662517L, ENGINE_SHA, false);
        }
        if (os.equals("linux")) {
            return new Engine("llama-b11719-bin-ubuntu-x64.tar.gz", "https://github.com/ggml-org/llama.cpp/releases/download/b11719/llama-b11719-bin-ubuntu-x64.tar.gz", 17968546L, "2c50edd1422cdc686a1620923a7dda9a33634509566d2821155fb3d4879510a3", true);
        }
        return null;
    }

    private static Engine engine() {
        return AssistantAssets.engine(System.getProperty("os.name", ""), System.getProperty("os.arch", ""));
    }

    public static boolean supported() {
        return AssistantAssets.engine() != null;
    }

    public static boolean linux() {
        return AssistantAssets.engine() != null && AssistantAssets.engine().linux;
    }

    public static String requirements(boolean ru) {
        String text;
        String string = text = ru ? "Qwen2.5-0.5B-Instruct (0,49 \u043c\u043b\u0440\u0434 \u043f\u0430\u0440\u0430\u043c\u0435\u0442\u0440\u043e\u0432). \u041c\u043e\u0434\u0435\u043b\u044c: 429 \u041c\u0411, \u0432\u043c\u0435\u0441\u0442\u0435 \u0441 \u0434\u0432\u0438\u0436\u043a\u043e\u043c: 449 \u041c\u0411. \u041e\u0417\u0423 \u0434\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u043e: \u043f\u0440\u0438\u043c\u0435\u0440\u043d\u043e 600\u2013900 \u041c\u0438\u0411, \u0437\u0430\u0432\u0438\u0441\u0438\u0442 \u043e\u0442 \u041f\u041a." : "Qwen2.5-0.5B-Instruct (0.49B parameters). Model: 429 MB; with engine: 449 MB. Additional RAM: roughly 600\u2013900 MiB, depending on your PC.";
        return AssistantAssets.linux() ? text.replace("449", "447") + (ru ? " Linux x64." : " Linux x64.") : text;
    }

    public Path model() {
        return this.root.resolve("qwen2.5-0.5b-instruct-q4_0.gguf");
    }

    private Path archive() {
        return this.root.resolve(Objects.requireNonNull(AssistantAssets.engine()).file);
    }

    public boolean installed() {
        try {
            return AssistantAssets.supported() && AssistantAssets.regular(this.model(), 428730208L) && AssistantAssets.regular(this.archive(), AssistantAssets.engine().bytes);
        }
        catch (IOException error) {
            return false;
        }
    }

    public void install(BooleanSupplier cancelled, DoubleConsumer progress) throws IOException {
        if (!AssistantAssets.supported()) {
            throw new IOException("unsupported_platform");
        }
        Files.createDirectories(this.root, new FileAttribute[0]);
        if (Files.isSymbolicLink(this.root)) {
            throw new IOException("invalid_install_directory");
        }
        this.download(MODEL_URL, this.model(), 428730208L, MODEL_SHA, cancelled, v -> progress.accept(v * 0.95));
        this.download(AssistantAssets.engine().url, this.archive(), AssistantAssets.engine().bytes, AssistantAssets.engine().sha, cancelled, v -> progress.accept(0.95 + v * 0.05));
        for (String name : List.of("QWEN_APACHE_2.0.txt", "LLAMA_CPP_MIT.txt")) {
            InputStream input = AssistantAssets.class.getResourceAsStream("/assets/bloodhelper/assistant/" + name);
            try {
                if (input == null) continue;
                Files.copy(input, this.root.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            }
            finally {
                if (input == null) continue;
                input.close();
            }
        }
    }

    public Runnable downloadCancellation() {
        InputStream input = this.downloading;
        return () -> {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        };
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public Path prepare(BooleanSupplier cancelled) throws IOException {
        if (!AssistantAssets.supported()) throw new IOException("install_required");
        if (!AssistantAssets.verified(this.model(), 428730208L, MODEL_SHA, cancelled)) throw new IOException("install_required");
        if (!AssistantAssets.verified(this.archive(), AssistantAssets.engine().bytes, AssistantAssets.engine().sha, cancelled)) {
            throw new IOException("install_required");
        }
        Path run = Files.createTempDirectory(this.root, "runtime-", new FileAttribute[0]);
        if (AssistantAssets.linux()) {
            try (GZIPInputStream input = new GZIPInputStream(Files.newInputStream(this.archive(), new OpenOption[0]));){
                AssistantAssets.extractLinux(input, run, cancelled);
                Path executable = run.resolve("llama-server");
                Files.setPosixFilePermissions(executable, PosixFilePermissions.fromString("rwx------"));
                if (!Files.isExecutable(executable)) {
                    throw new IOException("linux_execute");
                }
                Path path = executable;
                return path;
            }
            catch (IOException | RuntimeException error) {
                AssistantAssets.removeRuntime(run);
                throw error;
            }
        }
        long total = 0L;
        int count = 0;
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(this.archive(), new OpenOption[0]));){
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                AssistantAssets.check(cancelled);
                if (++count > 256) {
                    throw new IOException("invalid_engine_archive");
                }
                if (entry.isDirectory()) continue;
                String path = entry.getName().replace('\\', '/');
                if (path.startsWith("/")) throw new IOException("invalid_engine_path");
                if (path.contains(":")) throw new IOException("invalid_engine_path");
                if (Arrays.asList(path.split("/")).contains("..")) {
                    throw new IOException("invalid_engine_path");
                }
                String name = path.substring(path.lastIndexOf(47) + 1);
                if (!name.equals("llama-server.exe") && !name.endsWith(".dll") && !name.startsWith("LICENSE")) continue;
                Path dest = run.resolve(name);
                OutputStream output = Files.newOutputStream(dest, StandardOpenOption.CREATE_NEW);
                try {
                    int read;
                    byte[] buffer = new byte[65536];
                    while ((read = zip.read(buffer)) != -1) {
                        AssistantAssets.check(cancelled);
                        if ((total += (long)read) > 0x10000000L) {
                            throw new IOException("engine_too_large");
                        }
                        output.write(buffer, 0, read);
                    }
                }
                finally {
                    if (output == null) continue;
                    output.close();
                }
            }
            Path executable = run.resolve("llama-server.exe");
            if (!Files.isRegularFile(executable, new LinkOption[0])) {
                throw new IOException("engine_missing");
            }
            Path path = executable;
            return path;
        }
        catch (IOException error) {
            AssistantAssets.removeRuntime(run);
            throw error;
        }
    }

    static void extractLinux(InputStream input, Path run, BooleanSupplier cancelled) throws IOException {
        Object target;
        LinkedHashMap<String, Object> aliases = new LinkedHashMap<String, Object>();
        long total = 0L;
        int count = 0;
        try (TarArchiveInputStream tar = new TarArchiveInputStream(input);){
            TarArchiveEntry entry = tar.getNextEntry();
            while (entry != null) {
                AssistantAssets.check(cancelled);
                if (++count > 256 || !tar.canReadEntryData((ArchiveEntry)entry)) {
                    throw new IOException("invalid_engine_archive");
                }
                String name = AssistantAssets.archiveName(entry.getName());
                if (!entry.isDirectory() && AssistantAssets.linuxFile(name)) {
                    if (entry.isSymbolicLink() || entry.isLink()) {
                        target = AssistantAssets.archiveName(entry.getLinkName());
                        if (!AssistantAssets.linuxFile((String)target) || name.equals(target) || aliases.putIfAbsent(name, target) != null) {
                            throw new IOException("invalid_engine_link");
                        }
                    } else {
                        if (!entry.isFile() || entry.isSparse()) {
                            throw new IOException("invalid_engine_entry");
                        }
                        try (OutputStream output = Files.newOutputStream(run.resolve(name), StandardOpenOption.CREATE_NEW);){
                            int read;
                            byte[] buffer = new byte[65536];
                            while ((read = tar.read(buffer)) != -1) {
                                AssistantAssets.check(cancelled);
                                if ((total += (long)read) > 0x10000000L) {
                                    throw new IOException("engine_too_large");
                                }
                                output.write(buffer, 0, read);
                            }
                        }
                    }
                }
                entry = tar.getNextEntry();
            }
        }
        for (int pass = 0; !aliases.isEmpty() && pass < 16; ++pass) {
            Iterator it = aliases.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry alias = it.next();
                target = run.resolve((String)alias.getValue());
                if (!Files.isRegularFile((Path)target, LinkOption.NOFOLLOW_LINKS)) continue;
                if ((total += Files.size((Path)target)) > 0x10000000L) {
                    throw new IOException("engine_too_large");
                }
                Files.copy((Path)target, run.resolve((String)alias.getKey()), new CopyOption[0]);
                it.remove();
            }
        }
        if (!aliases.isEmpty()) {
            throw new IOException("invalid_engine_link");
        }
        if (!Files.isRegularFile(run.resolve("llama-server"), LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("engine_missing");
        }
    }

    private static boolean linuxFile(String name) {
        return name.equals("llama-server") || name.matches("lib[a-zA-Z0-9_-]+\\.so(?:\\.[0-9]+)*") || name.startsWith("LICENSE");
    }

    private static String archiveName(String raw) throws IOException {
        String path = raw.replace('\\', '/');
        if (path.startsWith("/") || path.contains(":") || Arrays.asList(path.split("/")).contains("..")) {
            throw new IOException("invalid_engine_path");
        }
        return path.substring(path.lastIndexOf(47) + 1);
    }

    public static void removeRuntime(Path directory) {
        if (directory == null) {
            return;
        }
        try (Stream<Path> files = Files.walk(directory, new FileVisitOption[0]);){
            for (Path path : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void download(String url, Path dest, long bytes, String sha, BooleanSupplier cancelled, DoubleConsumer progress) throws IOException {
        if (AssistantAssets.verified(dest, bytes, sha, cancelled)) {
            progress.accept(1.0);
            return;
        }
        Path part = dest.resolveSibling(String.valueOf(dest.getFileName()) + ".part");
        Files.deleteIfExists(part);
        try {
            URLConnection connection = AssistantAssets.connection(URI.create(url));
            long advertised = connection.getContentLengthLong();
            if (advertised >= 0L && advertised != bytes) {
                throw new IOException("download_size_changed");
            }
            MessageDigest digest = AssistantAssets.digest();
            long size = 0L;
            try (InputStream input = connection.getInputStream();
                 OutputStream output = Files.newOutputStream(part, StandardOpenOption.CREATE_NEW);){
                int read;
                this.downloading = input;
                byte[] buffer = new byte[65536];
                while ((read = input.read(buffer)) != -1) {
                    AssistantAssets.check(cancelled);
                    if ((size += (long)read) > bytes) {
                        throw new IOException("download_too_large");
                    }
                    digest.update(buffer, 0, read);
                    output.write(buffer, 0, read);
                    progress.accept((double)size / (double)bytes);
                }
            }
            finally {
                this.downloading = null;
            }
            AssistantAssets.check(cancelled);
            if (size != bytes || !HexFormat.of().formatHex(digest.digest()).equals(sha)) {
                throw new IOException("download_checksum");
            }
            Files.move(part, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        finally {
            Files.deleteIfExists(part);
        }
    }

    private static URLConnection connection(URI uri) throws IOException {
        for (int redirects = 0; redirects < 8; ++redirects) {
            String location;
            boolean approved;
            String host = Objects.toString(uri.getHost(), "").toLowerCase(Locale.ROOT);
            boolean bl = approved = host.equals("github.com") || host.equals("release-assets.githubusercontent.com") || host.equals("objects.githubusercontent.com") || host.equals("huggingface.co") || host.endsWith(".huggingface.co") || host.endsWith(".hf.co");
            if (!approved || !"https".equals(uri.getScheme()) || uri.getUserInfo() != null || uri.getPort() != -1 && uri.getPort() != 443) {
                throw new IOException("download_host");
            }
            URI uRI = uri;
            PrivacyGuard.checkUri(uRI);
            HttpURLConnection c = (HttpURLConnection)uRI.toURL().openConnection(Proxy.NO_PROXY);
            c.setInstanceFollowRedirects(false);
            c.setConnectTimeout(10000);
            c.setReadTimeout(15000);
            c.setRequestProperty("User-Agent", "BloodHelper-local-assistant/1.0");
            int status = c.getResponseCode();
            if (status >= 300 && status < 400) {
                location = c.getHeaderField("Location");
                c.disconnect();
                if (location == null) {
                    throw new IOException("download_redirect");
                }
            } else {
                if (status != 200) {
                    c.disconnect();
                    throw new IOException("download_http_" + status);
                }
                return c;
            }
            uri = uri.resolve(location);
        }
        throw new IOException("download_redirect_limit");
    }

    private static boolean regular(Path path, long size) throws IOException {
        return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && Files.size(path) == size;
    }

    private static boolean verified(Path file, long bytes, String sha, BooleanSupplier cancelled) throws IOException {
        if (!AssistantAssets.regular(file, bytes)) {
            return false;
        }
        MessageDigest digest = AssistantAssets.digest();
        try (InputStream input = Files.newInputStream(file, new OpenOption[0]);){
            int read;
            byte[] buffer = new byte[65536];
            while ((read = input.read(buffer)) != -1) {
                AssistantAssets.check(cancelled);
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest()).equals(sha);
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    static void check(BooleanSupplier cancelled) throws InterruptedIOException {
        if (cancelled.getAsBoolean() || Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException("cancelled");
        }
    }

    record Engine(String file, String url, long bytes, String sha, boolean linux) {
    }
}

