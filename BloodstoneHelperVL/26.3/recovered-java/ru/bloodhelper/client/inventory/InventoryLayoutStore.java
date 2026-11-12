/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.client.Minecraft
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.inventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import org.slf4j.LoggerFactory;

public final class InventoryLayoutStore {
    public static final int LIMIT = 32;
    private static final int MAX_BYTES = 0x200000;
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "BloodHelper inventory profiles");
        thread.setDaemon(true);
        thread.setPriority(1);
        return thread;
    });
    private static volatile List<Profile> profiles = List.of();
    private static volatile boolean ready;
    private static volatile boolean busy;
    private static volatile String error;
    private static Path file;

    private InventoryLayoutStore() {
    }

    public static void initialize(Path configDir) {
        if (file != null) {
            return;
        }
        file = configDir.resolve("bloodhelper/inventory-layouts.json");
        IO.execute(() -> {
            try {
                if (Files.exists(file, new LinkOption[0])) {
                    if (Files.size(file) > 0x200000L) {
                        throw new IllegalArgumentException("File too large");
                    }
                    profiles = InventoryLayoutStore.decodeProfiles(Files.readString(file));
                }
            }
            catch (Exception e) {
                error = "inventory-layouts.json";
                LoggerFactory.getLogger((String)"BloodHelper inventory").warn("Cannot read inventory profiles; original file retained", (Throwable)e);
            }
            finally {
                ready = true;
            }
        });
    }

    static List<Profile> decodeProfiles(String text) {
        JsonObject root = JsonParser.parseString((String)text).getAsJsonObject();
        int version = root.get("version").getAsInt();
        if (version != 1 && version != 2) {
            throw new IllegalArgumentException("Unknown version");
        }
        ArrayList<Profile> loaded = new ArrayList<Profile>();
        HashSet<String> names = new HashSet<String>();
        HashSet<String> ids = new HashSet<String>();
        for (JsonElement element : root.getAsJsonArray("profiles")) {
            JsonObject p = element.getAsJsonObject();
            String id = p.get("id").getAsString();
            String name = InventoryLayoutStore.validName(p.get("name").getAsString());
            JsonArray slots = p.getAsJsonArray("slots");
            if (loaded.size() >= 32 || slots.size() != 36 && (version != 2 || slots.size() != 41) || id.length() > 64 || !ids.add(id) || !names.add(name.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Invalid profile");
            }
            ArrayList<JsonElement> items = new ArrayList<JsonElement>();
            for (JsonElement slot : slots) {
                if (!slot.isJsonNull() && !slot.isJsonObject()) {
                    throw new IllegalArgumentException("Invalid item");
                }
                items.add(slot.deepCopy());
            }
            loaded.add(new Profile(id, name, List.copyOf(items)));
        }
        return List.copyOf(loaded);
    }

    public static List<Profile> profiles() {
        return profiles;
    }

    public static boolean ready() {
        return ready;
    }

    public static boolean busy() {
        return busy;
    }

    public static String error() {
        return error;
    }

    public static String validName(String name) {
        String clean;
        String string = clean = name == null ? "" : name.strip();
        if (clean.isEmpty() || clean.length() > 32 || clean.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid profile name");
        }
        return clean;
    }

    public static Profile named(String name) {
        return profiles.stream().filter(p -> p.name.equalsIgnoreCase(name.strip())).findFirst().orElse(null);
    }

    public static void save(String name, List<JsonElement> slots, Consumer<Boolean> done) {
        if (!ready || busy || !error.isEmpty() || slots.size() != 41) {
            done.accept(false);
            return;
        }
        Profile previous = InventoryLayoutStore.named(name = InventoryLayoutStore.validName(name));
        if (previous == null && profiles.size() >= 32) {
            done.accept(false);
            return;
        }
        ArrayList<Profile> next = new ArrayList<Profile>(profiles);
        if (previous != null) {
            next.remove(previous);
        }
        next.add(new Profile(previous == null ? UUID.randomUUID().toString() : previous.id, name, slots.stream().map(JsonElement::deepCopy).toList()));
        InventoryLayoutStore.persist(next, done);
    }

    public static void delete(Profile profile, Consumer<Boolean> done) {
        if (!ready || busy || !error.isEmpty()) {
            done.accept(false);
            return;
        }
        ArrayList<Profile> next = new ArrayList<Profile>(profiles);
        if (!next.remove(profile)) {
            done.accept(false);
            return;
        }
        InventoryLayoutStore.persist(next, done);
    }

    public static void close() {
        IO.shutdown();
        try {
            IO.awaitTermination(2L, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void persist(List<Profile> next, Consumer<Boolean> done) {
        busy = true;
        IO.execute(() -> {
            boolean success = false;
            try {
                JsonObject root = new JsonObject();
                root.addProperty("version", (Number)2);
                root.add("profiles", JSON.toJsonTree((Object)next));
                byte[] bytes = JSON.toJson((JsonElement)root).getBytes(StandardCharsets.UTF_8);
                if (bytes.length > 0x200000) {
                    throw new IllegalArgumentException("Profiles too large");
                }
                Files.createDirectories(file.getParent(), new FileAttribute[0]);
                Path legacyBackup = file.resolveSibling("inventory-layouts.v1.backup.json");
                if (Files.exists(file, new LinkOption[0]) && !Files.exists(legacyBackup, new LinkOption[0]) && JsonParser.parseString((String)Files.readString(file)).getAsJsonObject().get("version").getAsInt() == 1) {
                    Files.copy(file, legacyBackup, new CopyOption[0]);
                }
                Path temp = Files.createTempFile(file.getParent(), "inventory-layouts-", ".tmp", new FileAttribute[0]);
                try {
                    Files.write(temp, bytes, new OpenOption[0]);
                    try {
                        Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                    }
                    catch (AtomicMoveNotSupportedException e) {
                        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                finally {
                    Files.deleteIfExists(temp);
                }
                profiles = List.copyOf(next);
                success = true;
            }
            catch (Exception e) {
                LoggerFactory.getLogger((String)"BloodHelper inventory").warn("Cannot save inventory profiles", (Throwable)e);
            }
            finally {
                busy = false;
            }
            boolean result = success;
            Minecraft.getInstance().execute(() -> done.accept(result));
        });
    }

    static {
        error = "";
    }

    public record Profile(String id, String name, List<JsonElement> slots) {
    }
}

