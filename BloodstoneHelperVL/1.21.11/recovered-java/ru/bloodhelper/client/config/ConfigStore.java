/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.config.AppearancePreset;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.MenuState;
import ru.bloodhelper.client.config.PortableConfigProfile;
import ru.bloodhelper.client.hud.NotificationInbox;

public final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path directory;
    private static ClientConfig current;
    private static long revision;
    private static String activeProfile;
    private static MenuState menuState;
    private static final ExecutorService WRITER;
    private static final AtomicReference<PendingWrite> pending;
    private static final AtomicBoolean writing;
    private static volatile PendingWrite lastWritten;

    public static long revision() {
        return revision;
    }

    private ConfigStore() {
    }

    public static void init(Path configDirectory) {
        ConfigStore.flush();
        current = new ClientConfig();
        directory = configDirectory.resolve("bloodhelper");
        NotificationInbox.initialize(directory);
        Path path = directory.resolve("client.json");
        if (Files.isRegularFile(path, new LinkOption[0])) {
            try {
                current = ConfigStore.read(path);
            }
            catch (Exception error) {
                LoggerFactory.getLogger((String)"BloodHelper").warn("Cannot read config; preserving it as a backup", (Throwable)error);
                try {
                    Files.copy(path, directory.resolve("client-invalid-" + System.currentTimeMillis() + ".json"), new CopyOption[0]);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
        current.sanitize();
        menuState = new MenuState();
        try {
            MenuState loaded;
            Path menuFile = directory.resolve("menu-state.json");
            if (Files.isRegularFile(menuFile, new LinkOption[0]) && Files.size(menuFile) <= 32768L && (loaded = (MenuState)GSON.fromJson(Files.readString(menuFile, StandardCharsets.UTF_8), MenuState.class)) != null) {
                menuState = loaded.copy();
            }
        }
        catch (Exception menuFile) {
            // empty catch block
        }
        activeProfile = "";
        try {
            String candidate;
            Path state = directory.resolve("profile-state.json");
            if (Files.isRegularFile(state, new LinkOption[0]) && Files.size(state) <= 1024L && (candidate = (String)GSON.fromJson(Files.readString(state, StandardCharsets.UTF_8), String.class)) != null && !candidate.isBlank() && Files.isRegularFile(ConfigStore.profile(candidate), new LinkOption[0])) {
                activeProfile = candidate;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static ClientConfig get() {
        return current;
    }

    public static MenuState menuState() {
        return menuState.copy();
    }

    public static synchronized void rememberMenuState(MenuState state) {
        menuState = state.copy();
        if (directory == null) {
            return;
        }
        Path path = directory.resolve("menu-state.json");
        String json = GSON.toJson((Object)menuState);
        WRITER.execute(() -> {
            try {
                ConfigStore.writeJson(path, json);
            }
            catch (IOException error) {
                LoggerFactory.getLogger((String)"BloodHelper").warn("Could not remember menu navigation", (Throwable)error);
            }
        });
    }

    public static synchronized void requestSave() {
        ++revision;
        current.sanitize();
        if (directory == null) {
            return;
        }
        pending.set(new PendingWrite(directory.resolve("client.json"), GSON.toJson((Object)current)));
        ConfigStore.scheduleWrite();
    }

    private static void scheduleWrite() {
        if (!writing.compareAndSet(false, true)) {
            return;
        }
        WRITER.execute(() -> {
            try {
                PendingWrite next;
                while ((next = (PendingWrite)pending.getAndSet(null)) != null) {
                    if (next.equals(lastWritten) && Files.isRegularFile(next.path, new LinkOption[0])) continue;
                    try {
                        ConfigStore.writeJson(next.path, next.json);
                        lastWritten = next;
                    }
                    catch (IOException error) {
                        LoggerFactory.getLogger((String)"BloodHelper").error("Could not save settings", (Throwable)error);
                    }
                }
            }
            finally {
                writing.set(false);
                if (pending.get() != null) {
                    ConfigStore.scheduleWrite();
                }
            }
        });
    }

    public static synchronized void save() {
        ConfigStore.requestSave();
        ConfigStore.flush();
    }

    public static void flush() {
        try {
            do {
                WRITER.submit(() -> {}).get();
            } while (pending.get() != null || writing.get());
        }
        catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
        catch (ExecutionException error) {
            throw new IllegalStateException(error);
        }
    }

    public static synchronized void saveProfile(String name) throws IOException {
        current.sanitize();
        ConfigStore.write(ConfigStore.profile(name), current);
    }

    public static synchronized boolean loadProfile(String name) {
        try {
            Path source = ConfigStore.profile(name);
            String text = Files.readString(source, StandardCharsets.UTF_8);
            ClientConfig loaded = text.contains("\"bloodhelper-config\"") ? PortableConfigProfile.readStored(source, name).preview(current) : ConfigStore.read(source);
            loaded.serverSafeMode = ConfigStore.current.serverSafeMode;
            current = loaded;
            activeProfile = PortableConfigProfile.validName(name);
            ConfigStore.save();
            ConfigStore.rememberActive();
            return true;
        }
        catch (Exception error) {
            return false;
        }
    }

    public static synchronized boolean deleteProfile(String name) {
        try {
            boolean deleted = Files.deleteIfExists(ConfigStore.profile(name));
            if (deleted && name.equals(activeProfile)) {
                activeProfile = "";
                ConfigStore.rememberActive();
            }
            return deleted;
        }
        catch (Exception error) {
            return false;
        }
    }

    public static List<String> profiles() {
        List<String> list;
        block10: {
            if (directory == null) {
                return List.of();
            }
            Path path = directory.resolve("profiles");
            if (!Files.isDirectory(path, new LinkOption[0])) {
                return List.of();
            }
            Stream<Path> stream = Files.list(path);
            try {
                list = stream.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".json")).map(n -> n.substring(0, n.length() - 5)).filter(ConfigStore::validProfileName).sorted(String.CASE_INSENSITIVE_ORDER).limit(256L).toList();
                if (stream == null) break block10;
            }
            catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException error) {
                    return List.of();
                }
            }
            stream.close();
        }
        return list;
    }

    public static Path appearanceDirectory() throws IOException {
        if (directory == null) {
            throw new IOException("Settings directory unavailable");
        }
        return Files.createDirectories(directory.resolve("appearance"), new FileAttribute[0]);
    }

    public static List<Path> appearances() throws IOException {
        try (Stream<Path> paths = Files.list(ConfigStore.appearanceDirectory());){
            List<Path> list = paths.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(p -> p.getFileName().toString().endsWith(".json")).sorted().limit(256L).toList();
            return list;
        }
    }

    public static Path exportAppearance() throws IOException {
        String stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").format(LocalDateTime.now());
        Path destination = ConfigStore.appearanceDirectory().resolve("BloodHelper-" + stamp + ".json");
        Files.writeString(destination, (CharSequence)AppearancePreset.capture(current).encode(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        return destination;
    }

    public static synchronized void applyAppearance(AppearancePreset preset) {
        current = preset.preview(current);
        ConfigStore.requestSave();
    }

    public static String activeProfile() {
        return activeProfile;
    }

    public static Path profilesDirectory() throws IOException {
        if (directory == null) {
            throw new IOException("Settings directory unavailable");
        }
        return Files.createDirectories(directory.resolve("profiles"), new FileAttribute[0]);
    }

    public static Path profileImportDirectory() throws IOException {
        if (directory == null) {
            throw new IOException("Settings directory unavailable");
        }
        return Files.createDirectories(directory.resolve("config-imports"), new FileAttribute[0]);
    }

    public static Path profileExportDirectory() throws IOException {
        if (directory == null) {
            throw new IOException("Settings directory unavailable");
        }
        return Files.createDirectories(directory.resolve("config-exports"), new FileAttribute[0]);
    }

    public static List<Path> profileImportFiles() throws IOException {
        try (Stream<Path> files = Files.list(ConfigStore.profileImportDirectory());){
            List<Path> list = files.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")).sorted().limit(64L).toList();
            return list;
        }
    }

    public static synchronized void createProfile(String name) throws IOException {
        name = PortableConfigProfile.validName(name);
        ConfigStore.ensureNewName(name);
        current.sanitize();
        if (!activeProfile.isEmpty()) {
            ConfigStore.writePortable(activeProfile, current);
        }
        ConfigStore.createPortable(ConfigStore.profile(name), PortableConfigProfile.capture(name, current).encode());
        activeProfile = name;
        ConfigStore.rememberActive();
        ConfigStore.requestSave();
    }

    public static synchronized void saveActiveProfile() throws IOException {
        if (activeProfile.isEmpty()) {
            throw new IOException("Create a named config first");
        }
        current.sanitize();
        ConfigStore.writePortable(activeProfile, current);
        ConfigStore.save();
    }

    public static synchronized void switchProfile(String name) throws IOException {
        if ((name = PortableConfigProfile.validName(name)).equals(activeProfile)) {
            return;
        }
        ClientConfig candidate = PortableConfigProfile.readStored(ConfigStore.profile(name), name).preview(current);
        if (activeProfile.isEmpty()) {
            String backup = ConfigStore.uniqueName("My settings");
            ConfigStore.createPortable(ConfigStore.profile(backup), PortableConfigProfile.capture(backup, current).encode());
        } else {
            ConfigStore.writePortable(activeProfile, current);
        }
        ConfigStore.flush();
        Path live = directory.resolve("client.json");
        String json = GSON.toJson((Object)candidate);
        ConfigStore.writeJson(live, json);
        lastWritten = new PendingWrite(live, json);
        current = candidate;
        activeProfile = name;
        ConfigStore.rememberActive();
    }

    public static synchronized String importProfile(Path source) throws IOException {
        PortableConfigProfile imported = PortableConfigProfile.read(source);
        String name = ConfigStore.uniqueName(imported.name());
        ConfigStore.createPortable(ConfigStore.profile(name), imported.named(name).encode());
        return name;
    }

    public static synchronized Path exportProfile(String name) throws IOException {
        PortableConfigProfile portable = (name = PortableConfigProfile.validName(name)).equals(activeProfile) ? PortableConfigProfile.capture(name, current) : PortableConfigProfile.readStored(ConfigStore.profile(name), name);
        Path folder = ConfigStore.profileExportDirectory();
        for (int suffix = 1; suffix <= 1000; ++suffix) {
            Path file = folder.resolve(name + (String)(suffix == 1 ? "" : " " + suffix) + ".json");
            try {
                Files.writeString(file, (CharSequence)portable.encode(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                return file;
            }
            catch (FileAlreadyExistsException fileAlreadyExistsException) {
                continue;
            }
        }
        throw new IOException("Too many exported copies");
    }

    public static synchronized void renameProfile(String previous, String next) throws IOException {
        if ((previous = PortableConfigProfile.validName(previous)).equals(next = PortableConfigProfile.validName(next))) {
            return;
        }
        ConfigStore.ensureNewName(next);
        Path source = ConfigStore.profile(previous);
        PortableConfigProfile portable = previous.equals(activeProfile) ? PortableConfigProfile.capture(next, current) : PortableConfigProfile.readStored(source, previous).named(next);
        Path target = ConfigStore.profile(next);
        ConfigStore.createPortable(target, portable.encode());
        try {
            Files.delete(source);
        }
        catch (IOException failure) {
            Files.deleteIfExists(target);
            throw failure;
        }
        if (previous.equals(activeProfile)) {
            activeProfile = next;
            ConfigStore.rememberActive();
        }
    }

    private static void writePortable(String name, ClientConfig config) throws IOException {
        ConfigStore.writeJson(ConfigStore.profile(name), PortableConfigProfile.capture(name, config).encode());
    }

    private static void createPortable(Path path, String json) throws IOException {
        Files.createDirectories(path.getParent(), new FileAttribute[0]);
        Files.writeString(path, (CharSequence)json, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    private static String uniqueName(String preferred) throws IOException {
        preferred = PortableConfigProfile.validName(preferred);
        if (ConfigStore.profiles().size() >= 256) {
            throw new IOException("Config library is full (256)");
        }
        for (int i = 1; i <= 1000; ++i) {
            Object suffix = i == 1 ? "" : " " + i;
            String name = preferred.substring(0, Math.min(preferred.length(), 40 - ((String)suffix).length())).stripTrailing() + (String)suffix;
            if (!ConfigStore.profiles().stream().noneMatch(existing -> existing.equalsIgnoreCase(name)) || Files.exists(ConfigStore.profile(name), new LinkOption[0])) continue;
            return name;
        }
        throw new IOException("Could not find a free config name");
    }

    private static void ensureNewName(String name) throws IOException {
        if (ConfigStore.profiles().size() >= 256) {
            throw new IOException("Config library is full (256)");
        }
        if (ConfigStore.profiles().stream().anyMatch(existing -> existing.equalsIgnoreCase(name)) || Files.exists(ConfigStore.profile(name), new LinkOption[0])) {
            throw new FileAlreadyExistsException(name);
        }
    }

    private static boolean validProfileName(String name) {
        try {
            PortableConfigProfile.validName(name);
            return true;
        }
        catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    private static void rememberActive() {
        try {
            ConfigStore.writeJson(directory.resolve("profile-state.json"), GSON.toJson((Object)activeProfile));
        }
        catch (IOException error) {
            LoggerFactory.getLogger((String)"BloodHelper").warn("Could not remember active config", (Throwable)error);
        }
    }

    private static Path profile(String name) {
        String valid = PortableConfigProfile.validName(name);
        if (directory == null) {
            throw new IllegalStateException("Settings directory unavailable");
        }
        return directory.resolve("profiles").resolve(valid + ".json");
    }

    private static ClientConfig read(Path path) throws IOException {
        ClientConfig result = (ClientConfig)GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), ClientConfig.class);
        if (result == null) {
            throw new IOException("Empty config");
        }
        result.sanitize();
        return result;
    }

    private static void write(Path path, ClientConfig value) throws IOException {
        ConfigStore.writeJson(path, GSON.toJson((Object)value));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void writeJson(Path path, String json) throws IOException {
        Files.createDirectories(path.getParent(), new FileAttribute[0]);
        Path temporary = Files.createTempFile(path.getParent(), "settings-", ".tmp", new FileAttribute[0]);
        try {
            Files.writeString(temporary, (CharSequence)json, StandardCharsets.UTF_8, new OpenOption[0]);
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally {
            Files.deleteIfExists(temporary);
        }
    }

    static {
        current = new ClientConfig();
        activeProfile = "";
        menuState = new MenuState();
        WRITER = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "BLOODHELPER settings");
            thread.setDaemon(true);
            return thread;
        });
        pending = new AtomicReference();
        writing = new AtomicBoolean();
    }

    private record PendingWrite(Path path, String json) {
    }
}

