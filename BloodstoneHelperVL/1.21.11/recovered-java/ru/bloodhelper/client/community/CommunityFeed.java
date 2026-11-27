/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package ru.bloodhelper.client.community;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import ru.bloodhelper.client.hud.NotificationInbox;

public final class CommunityFeed {
    private static volatile Panel mainMenu;
    private static volatile List<Message> messages;
    private static long lastSeen;
    private static Path cache;

    private CommunityFeed() {
    }

    public static void initialize(Path directory) {
        cache = directory.resolve("community-cache.json");
        try {
            if (Files.isRegularFile(cache, new LinkOption[0]) && Files.size(cache) < 256000L) {
                JsonObject value = JsonParser.parseString((String)Files.readString(cache)).getAsJsonObject();
                lastSeen = Math.max(0L, value.get("lastSeen").getAsLong());
                mainMenu = CommunityFeed.panel(value.get("mainMenu"));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static String text(JsonObject value, String key, int max) {
        JsonElement e = value.get(key);
        if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Invalid message text");
        }
        String s = e.getAsString();
        if (s.length() > max) {
            throw new IllegalArgumentException("Message too long");
        }
        return s;
    }

    private static Panel panel(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return null;
        }
        JsonObject o = value.getAsJsonObject();
        return new Panel(CommunityFeed.text(o, "title", 120), CommunityFeed.text(o, "body", 4000));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void accept(JsonObject value) throws IOException {
        if (value.get("schema").getAsInt() != 1) {
            throw new IllegalArgumentException("Unsupported community schema");
        }
        Panel title = CommunityFeed.panel(value.get("mainMenu"));
        JsonArray entries = value.getAsJsonArray("messages");
        if (entries == null || entries.size() > 40) {
            throw new IllegalArgumentException("Too many messages");
        }
        ArrayList<Message> parsed = new ArrayList<Message>();
        long now = System.currentTimeMillis() / 1000L;
        for (JsonElement e : entries) {
            JsonObject o = e.getAsJsonObject();
            long id = o.get("id").getAsLong();
            if (id <= 0L) {
                throw new IllegalArgumentException("Invalid message id");
            }
            Message m = new Message(id, CommunityFeed.text(o, "title", 120), CommunityFeed.text(o, "body", 4000));
            if (o.get("expires").getAsLong() <= now) continue;
            parsed.add(m);
        }
        long previousSeen = lastSeen;
        boolean titleChanged = !Objects.equals(mainMenu, title);
        parsed.sort(Comparator.comparingLong(Message::id));
        for (Message m : parsed) {
            if (m.id <= lastSeen) continue;
            NotificationInbox.record("BloodHelper \u00b7 \u043e\u0431\u044a\u044f\u0432\u043b\u0435\u043d\u0438\u0435", m.title, m.body);
            lastSeen = m.id;
        }
        mainMenu = title;
        messages = List.copyOf(parsed);
        if (cache != null && (titleChanged || previousSeen != lastSeen)) {
            Files.createDirectories(cache.getParent(), new FileAttribute[0]);
            JsonObject save = new JsonObject();
            save.addProperty("lastSeen", (Number)lastSeen);
            save.add("mainMenu", new Gson().toJsonTree((Object)title));
            Path tmp = Files.createTempFile(cache.getParent(), "community-", ".tmp", new FileAttribute[0]);
            try {
                Files.writeString(tmp, (CharSequence)save.toString(), new OpenOption[0]);
                try {
                    Files.move(tmp, cache, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                }
                catch (AtomicMoveNotSupportedException e) {
                    Files.move(tmp, cache, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            finally {
                Files.deleteIfExists(tmp);
            }
        }
    }

    public static Panel mainMenu() {
        return mainMenu;
    }

    public static List<Message> messages() {
        return messages;
    }

    static {
        messages = List.of();
    }

    public record Panel(String title, String body) {
    }

    public record Message(long id, String title, String body) {
    }
}

