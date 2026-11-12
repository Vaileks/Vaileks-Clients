/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.regex.Pattern;

public final class DeathScreenshotFiles {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss-SSS");
    private static final Pattern OWNED = Pattern.compile("death-[0-9]{8}-[0-9]{6}-[0-9]{3}-[a-f0-9]{32}\\.png");

    private DeathScreenshotFiles() {
    }

    public static String filename(Instant instant, UUID id) {
        return "death-" + DATE.format(instant.atOffset(ZoneOffset.UTC)) + "-" + id.toString().replace("-", "") + ".png";
    }

    public static boolean owned(String filename) {
        return filename != null && OWNED.matcher(filename).matches();
    }

    public static Path directory(Path gameDirectory) {
        return gameDirectory.toAbsolutePath().normalize().resolve("screenshots/bloodhelper/deaths");
    }

    public static Path resolve(Path gameDirectory, String filename) {
        if (!DeathScreenshotFiles.owned(filename)) {
            throw new IllegalArgumentException("Invalid death screenshot name");
        }
        return DeathScreenshotFiles.directory(gameDirectory).resolve(filename);
    }

    public static Path ensureDirectory(Path gameDirectory) throws IOException {
        Path current = gameDirectory.toAbsolutePath().normalize();
        for (String name : List.of("screenshots", "bloodhelper", "deaths")) {
            if (Files.isSymbolicLink(current = current.resolve(name))) {
                throw new IOException("Screenshot directory is a symbolic link");
            }
            Files.createDirectories(current, new FileAttribute[0]);
        }
        return current;
    }

    private static void verifyParents(Path gameDirectory) throws IOException {
        Path current = gameDirectory.toAbsolutePath().normalize();
        for (String name : List.of("screenshots", "bloodhelper", "deaths")) {
            if (!Files.isSymbolicLink(current = current.resolve(name))) continue;
            throw new IOException("Screenshot directory is a symbolic link");
        }
    }

    public static List<Entry> list(Path gameDirectory, int limit) throws IOException {
        DeathScreenshotFiles.verifyParents(gameDirectory);
        Path dir = DeathScreenshotFiles.directory(gameDirectory);
        if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
            return List.of();
        }
        int maximum = Math.clamp((long)limit, 1, 200);
        PriorityQueue<Entry> latest = new PriorityQueue<Entry>(Comparator.comparing(Entry::filename));
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir);){
            for (Path path : paths) {
                String name = path.getFileName().toString();
                if (!DeathScreenshotFiles.owned(name) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) continue;
                try {
                    Instant date = LocalDateTime.parse(name.substring(6, 25), DATE).toInstant(ZoneOffset.UTC);
                    latest.add(new Entry(name, date));
                    if (latest.size() <= maximum) continue;
                    latest.remove();
                }
                catch (RuntimeException runtimeException) {}
            }
        }
        ArrayList<Entry> result = new ArrayList<Entry>(latest);
        result.sort(Comparator.comparing(Entry::filename).reversed());
        return List.copyOf(result);
    }

    public static Path readable(Path gameDirectory, String filename) throws IOException {
        DeathScreenshotFiles.verifyParents(gameDirectory);
        Path file = DeathScreenshotFiles.resolve(gameDirectory, filename);
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) || Files.size(file) > 0x6000000L) {
            throw new IOException("Screenshot unavailable or too large");
        }
        try (DataInputStream data = new DataInputStream(Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS));){
            if (data.readLong() != -8552249625308161526L || data.readInt() != 13 || data.readInt() != 1229472850) {
                throw new IOException("Invalid PNG screenshot");
            }
            int width = data.readInt();
            int height = data.readInt();
            if (width < 1 || height < 1 || width > 16384 || height > 16384 || (long)width * (long)height > 0x4000000L) {
                throw new IOException("Screenshot dimensions are too large");
            }
        }
        return file;
    }

    public record Entry(String filename, Instant time) {
    }
}

