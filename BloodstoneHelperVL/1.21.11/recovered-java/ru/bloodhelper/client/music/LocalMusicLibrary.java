/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 */
package ru.bloodhelper.client.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

public final class LocalMusicLibrary {
    public static final int MAX_TRACKS = 1024;
    private static final long MAX_FILE = 0x20000000L;
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path storage;
    private volatile List<Track> tracks = List.of();
    private volatile double volume = 0.7;

    public LocalMusicLibrary(Path storage) {
        this.storage = storage;
    }

    public List<Track> tracks() {
        return this.tracks;
    }

    public double volume() {
        return this.volume;
    }

    public Track find(String id) {
        return this.tracks.stream().filter(t -> t.id().equals(id)).findFirst().orElse(null);
    }

    public synchronized void load() throws IOException {
        if (!Files.exists(this.storage, new LinkOption[0])) {
            return;
        }
        if (Files.size(this.storage) > 0x200000L) {
            throw new IOException("Library file is too large");
        }
        try {
            Stored data = (Stored)JSON.fromJson(Files.readString(this.storage, StandardCharsets.UTF_8), Stored.class);
            if (data == null || data.version != 1 || data.tracks == null || data.tracks.size() > 1024) {
                throw new IllegalArgumentException("Invalid library");
            }
            HashSet<String> ids = new HashSet<String>();
            for (Track track : data.tracks) {
                if (track != null && track.id != null && track.id.matches("[a-f0-9]{32}") && ids.add(track.id) && track.fingerprint != null && track.fingerprint.matches("[a-f0-9]{64}") && track.path != null && Path.of(track.path, new String[0]).isAbsolute() && track.size > 0L && track.size <= 0x20000000L && track.title != null && LocalMusicLibrary.cleanTitle(track.title).equals(track.title)) continue;
                throw new IllegalArgumentException("Invalid track");
            }
            this.tracks = List.copyOf(data.tracks);
            this.volume = Double.isFinite(data.volume) ? Math.clamp(data.volume, 0.0, 1.0) : 0.7;
        }
        catch (RuntimeException invalid) {
            throw new IOException("Could not read music library; original file preserved", invalid);
        }
    }

    public synchronized ImportResult add(Collection<Path> files) throws IOException {
        ArrayList<Track> next = new ArrayList<Track>(this.tracks);
        HashSet<Path> known = new HashSet<Path>();
        for (Track track : next) {
            known.add(track.file().toAbsolutePath().normalize());
        }
        int added = 0;
        int skipped = 0;
        for (Path value : files) {
            if (next.size() >= 1024) {
                ++skipped;
                continue;
            }
            try {
                Path file = value.toRealPath(new LinkOption[0]);
                long size = Files.size(file);
                if (!(LocalMusicLibrary.supported(file) && size > 0L && size <= 0x20000000L && Files.isRegularFile(file, new LinkOption[0]) && known.add(file))) {
                    ++skipped;
                    continue;
                }
                String title = file.getFileName().toString();
                title = title.substring(0, title.lastIndexOf(46));
                next.add(new Track(UUID.randomUUID().toString().replace("-", ""), file.toString(), LocalMusicLibrary.cleanTitle(title), LocalMusicLibrary.fingerprint(file), size, false));
                ++added;
            }
            catch (IOException | IllegalArgumentException invalid) {
                ++skipped;
            }
        }
        if (added > 0) {
            this.commit(next, this.volume);
        }
        return new ImportResult(added, skipped);
    }

    public static boolean supported(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg");
    }

    public static List<Path> folderFiles(Path folder) throws IOException {
        if (!Files.isDirectory(folder, new LinkOption[0])) {
            throw new IOException("Choose an existing folder");
        }
        try (Stream<Path> walk = Files.walk(folder, 12, new FileVisitOption[0]);){
            List<Path> list = walk.limit(20000L).filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(LocalMusicLibrary::supported).sorted().limit(4096L).toList();
            return list;
        }
    }

    public synchronized void rename(String id, String title) throws IOException {
        Track track = this.required(id);
        this.replace(track.named(title));
    }

    public synchronized void favorite(String id) throws IOException {
        this.replace(this.required(id).saved());
    }

    public synchronized void remove(String id) throws IOException {
        this.required(id);
        this.commit(this.tracks.stream().filter(t -> !t.id.equals(id)).toList(), this.volume);
    }

    public synchronized void volume(double value) throws IOException {
        if (Double.isFinite(value)) {
            this.commit(this.tracks, Math.clamp(value, 0.0, 1.0));
        }
    }

    public synchronized void relink(String id, Path candidate) throws IOException {
        Track track = this.required(id);
        Path file = candidate.toRealPath(new LinkOption[0]);
        if (!(LocalMusicLibrary.supported(file) && Files.isRegularFile(file, new LinkOption[0]) && Files.size(file) == track.size && LocalMusicLibrary.fingerprint(file).equals(track.fingerprint))) {
            throw new IOException("Selected file does not match the original track");
        }
        this.replace(track.moved(file));
    }

    public synchronized RelinkResult relinkFolder(Path folder) throws IOException {
        HashSet<Long> sizes = new HashSet<Long>();
        for (Track track : this.tracks) {
            if (Files.isRegularFile(track.file(), new LinkOption[0])) continue;
            sizes.add(track.size);
        }
        HashMap<String, Path> candidates = new HashMap<String, Path>();
        for (Path file : LocalMusicLibrary.folderFiles(folder)) {
            try {
                if (!sizes.contains(Files.size(file))) continue;
                candidates.putIfAbsent(LocalMusicLibrary.fingerprint(file), file.toRealPath(new LinkOption[0]));
            }
            catch (IOException iOException) {}
        }
        ArrayList<Track> arrayList = new ArrayList<Track>();
        int restored = 0;
        int missing = 0;
        for (Track track : this.tracks) {
            if (!Files.isRegularFile(track.file(), new LinkOption[0])) {
                Path match = (Path)candidates.get(track.fingerprint);
                if (match != null) {
                    track = track.moved(match);
                    ++restored;
                } else {
                    ++missing;
                }
            }
            arrayList.add(track);
        }
        if (restored > 0) {
            this.commit(arrayList, this.volume);
        }
        return new RelinkResult(restored, missing);
    }

    private Track required(String id) throws IOException {
        Track result = this.find(id);
        if (result == null) {
            throw new IOException("Track no longer exists");
        }
        return result;
    }

    private void replace(Track replacement) throws IOException {
        this.commit(this.tracks.stream().map(t -> t.id.equals(replacement.id) ? replacement : t).toList(), this.volume);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void commit(List<Track> next, double nextVolume) throws IOException {
        Files.createDirectories(this.storage.getParent(), new FileAttribute[0]);
        Path temp = Files.createTempFile(this.storage.getParent(), "local-music-", ".tmp", new FileAttribute[0]);
        try {
            Files.writeString(temp, (CharSequence)JSON.toJson((Object)new Stored(1, nextVolume, next)), StandardCharsets.UTF_8, new OpenOption[0]);
            try {
                Files.move(temp, this.storage, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temp, this.storage, StandardCopyOption.REPLACE_EXISTING);
            }
            this.tracks = List.copyOf(next);
            this.volume = nextVolume;
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }

    static String fingerprint(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream stream = Files.newInputStream(path, new OpenOption[0]);){
                int count;
                byte[] buffer = new byte[65536];
                long read = 0L;
                while ((count = stream.read(buffer)) >= 0) {
                    if ((read += (long)count) > 0x20000000L) {
                        throw new IOException("Track exceeds 512 MiB");
                    }
                    digest.update(buffer, 0, count);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        }
        catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    static String cleanTitle(String value) {
        String clean;
        String string = clean = value == null ? "" : value.replaceAll("[\\p{Cntrl}\\p{Cf}]|\u00a7.", "").strip();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Enter a track name");
        }
        return clean.codePoints().limit(120L).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    public record Track(String id, String path, String title, String fingerprint, long size, boolean favorite) {
        public Path file() {
            return Path.of(this.path, new String[0]);
        }

        Track moved(Path file) {
            return new Track(this.id, file.toString(), this.title, this.fingerprint, this.size, this.favorite);
        }

        Track named(String value) {
            return new Track(this.id, this.path, LocalMusicLibrary.cleanTitle(value), this.fingerprint, this.size, this.favorite);
        }

        Track saved() {
            return new Track(this.id, this.path, this.title, this.fingerprint, this.size, true);
        }
    }

    private record Stored(int version, double volume, List<Track> tracks) {
    }

    public record ImportResult(int added, int skipped) {
    }

    public record RelinkResult(int restored, int missing) {
    }
}

