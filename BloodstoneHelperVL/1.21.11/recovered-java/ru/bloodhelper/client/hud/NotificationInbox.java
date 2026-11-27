/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.hud;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

public final class NotificationInbox {
    private static final Gson JSON = new Gson();
    private static final int LIMIT = 200;
    private static final ScheduledExecutorService IO = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "BloodHelper notification history");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicReference<Write> PENDING = new AtomicReference();
    private static final AtomicBoolean SCHEDULED = new AtomicBoolean();
    private static Data data = new Data();
    private static Path file;

    private NotificationInbox() {
    }

    public static synchronized void initialize(Path directory) {
        file = directory.resolve("notification-history.json");
        data = new Data();
        try {
            Data loaded;
            if (Files.isRegularFile(file, new LinkOption[0]) && Files.size(file) <= 512000L && (loaded = (Data)JSON.fromJson(Files.readString(file), Data.class)) != null && loaded.entries != null) {
                NotificationInbox.data.snoozedUntil = Math.min(loaded.snoozedUntil, System.currentTimeMillis() + 86400000L);
                for (Entry e : loaded.entries) {
                    if (e == null || e.time <= 0L || e.source == null || e.title == null || e.body == null) continue;
                    NotificationInbox.data.entries.add(new Entry(e.time, NotificationInbox.clean(e.source, 80), NotificationInbox.clean(e.title, 160), NotificationInbox.clean(e.body, 2048), e.read));
                }
                NotificationInbox.trim();
            }
        }
        catch (Exception e) {
            LoggerFactory.getLogger((String)"BloodstoneHelperVL").warn("Cannot read notification history", (Throwable)e);
        }
    }

    public static synchronized void record(String source, String title, String body) {
        if (title == null && body == null) {
            return;
        }
        NotificationInbox.data.entries.add(new Entry(System.currentTimeMillis(), NotificationInbox.clean(source, 80), NotificationInbox.clean(title, 160), NotificationInbox.clean(body, 2048), false));
        NotificationInbox.trim();
        NotificationInbox.save();
    }

    private static String clean(String value, int limit) {
        if (value == null) {
            return "";
        }
        return value.length() <= limit ? value : value.substring(0, limit);
    }

    private static void trim() {
        if (NotificationInbox.data.entries.size() > 200) {
            NotificationInbox.data.entries = new ArrayList<Entry>(NotificationInbox.data.entries.subList(NotificationInbox.data.entries.size() - 200, NotificationInbox.data.entries.size()));
        }
    }

    public static synchronized List<Entry> entries() {
        ArrayList<Entry> copy = new ArrayList<Entry>(NotificationInbox.data.entries);
        Collections.reverse(copy);
        return List.copyOf(copy);
    }

    public static synchronized long unread() {
        return NotificationInbox.data.entries.stream().filter(e -> !e.read).count();
    }

    public static synchronized boolean snoozed() {
        return NotificationInbox.data.snoozedUntil > System.currentTimeMillis();
    }

    public static synchronized long snoozedUntil() {
        return NotificationInbox.data.snoozedUntil;
    }

    public static synchronized void snooze(int minutes) {
        NotificationInbox.data.snoozedUntil = minutes <= 0 ? 0L : System.currentTimeMillis() + (long)Math.min(1440, minutes) * 60000L;
        NotificationInbox.save();
    }

    public static synchronized void markRead() {
        NotificationInbox.data.entries = NotificationInbox.data.entries.stream().map(e -> new Entry(e.time, e.source, e.title, e.body, true)).collect(Collectors.toCollection(ArrayList::new));
        NotificationInbox.save();
    }

    public static synchronized void clear() {
        NotificationInbox.data.entries.clear();
        NotificationInbox.save();
    }

    private static void save() {
        if (file == null) {
            return;
        }
        PENDING.set(new Write(file, JSON.toJson((Object)data)));
        if (SCHEDULED.compareAndSet(false, true)) {
            IO.schedule(NotificationInbox::flush, 750L, TimeUnit.MILLISECONDS);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void flush() {
        try {
            Write write;
            while ((write = (Write)PENDING.getAndSet(null)) != null) {
                try {
                    Files.createDirectories(write.path.getParent(), new FileAttribute[0]);
                    Path temp = Files.createTempFile(write.path.getParent(), "inbox-", ".tmp", new FileAttribute[0]);
                    try {
                        Files.writeString(temp, (CharSequence)write.json, new OpenOption[0]);
                        try {
                            Files.move(temp, write.path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                        }
                        catch (AtomicMoveNotSupportedException e) {
                            Files.move(temp, write.path, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    finally {
                        Files.deleteIfExists(temp);
                    }
                }
                catch (IOException e) {
                    LoggerFactory.getLogger((String)"BloodstoneHelperVL").warn("Cannot save notification history", (Throwable)e);
                }
            }
        }
        finally {
            SCHEDULED.set(false);
            if (PENDING.get() != null && SCHEDULED.compareAndSet(false, true)) {
                IO.schedule(NotificationInbox::flush, 750L, TimeUnit.MILLISECONDS);
            }
        }
    }

    private static final class Data {
        long snoozedUntil;
        List<Entry> entries = new ArrayList<Entry>();

        private Data() {
        }
    }

    public record Entry(long time, String source, String title, String body, boolean read) {
    }

    private record Write(Path path, String json) {
    }
}

