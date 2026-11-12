/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 */
package ru.bloodhelper.client.music;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.loader.api.FabricLoader;
import ru.bloodhelper.client.music.LocalMusicController;
import ru.bloodhelper.client.music.MediaBridgePlatform;
import ru.bloodhelper.client.music.MediaDuckingIntent;
import ru.bloodhelper.client.music.MediaFavoriteCommand;
import ru.bloodhelper.client.music.MediaProtocol;
import ru.bloodhelper.client.music.MediaSeekCommand;
import ru.bloodhelper.client.music.MediaSnapshot;
import ru.bloodhelper.client.music.MediaSources;
import ru.bloodhelper.client.music.MediaVolumeCommand;
import ru.bloodhelper.client.music.PendingMediaFavorite;
import ru.bloodhelper.client.music.PendingMediaPlayback;
import ru.bloodhelper.client.music.PendingMediaSeek;
import ru.bloodhelper.client.music.PendingMediaVolume;

public final class MediaService
implements AutoCloseable {
    private static final long STALE_NANOS = TimeUnit.SECONDS.toNanos(20L);
    private final MediaBridgePlatform platform;
    private final boolean supported;
    private final Object lifecycle = new Object();
    private volatile boolean closed;
    private volatile boolean enabled;
    private volatile boolean controlsAllowed = true;
    private volatile Session session;
    private Session lastSession;
    private volatile MediaSnapshot snapshot = MediaSnapshot.empty();
    private volatile String status;
    private volatile String diagnostic = "";
    private volatile String preferredSource = "";
    private volatile MediaSources sources = MediaSources.empty();
    private volatile LocalMusicController localMusic;
    private long nextSeekRequest;
    private long nextVolumeRequest;
    private long nextFavoriteRequest;
    private final PendingMediaFavorite pendingFavorite = new PendingMediaFavorite();
    private final PendingMediaVolume pendingVolume = new PendingMediaVolume();
    private final PendingMediaSeek pendingSeek = new PendingMediaSeek();
    private final PendingMediaPlayback pendingPlayback = new PendingMediaPlayback();
    private volatile MediaDuckingIntent ducking = MediaDuckingIntent.create(1L, false, 60.0);
    private volatile long duckingUpdatedNanos;
    private Object duckingContext;
    private long duckingEpoch = 1L;

    public MediaService() {
        this(MediaBridgePlatform.current());
    }

    MediaService(MediaBridgePlatform platform) {
        this.platform = Objects.requireNonNull(platform);
        this.supported = platform != MediaBridgePlatform.UNSUPPORTED;
        this.status = this.supported ? "stopped" : "unsupported_os";
    }

    public boolean localSelected() {
        return "bloodhelper:local".equals(this.preferredSource);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public LocalMusicController localMusic() {
        Object object = this.lifecycle;
        synchronized (object) {
            if (this.localMusic == null) {
                this.localMusic = new LocalMusicController(FabricLoader.getInstance().getConfigDir().resolve("bloodhelper/local-music.json"));
            }
            this.localMusic.setSuspended(!this.controlsAllowed);
            return this.localMusic;
        }
    }

    public void start() {
        this.setEnabled(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setControlsAllowed(boolean value) {
        Object object = this.lifecycle;
        synchronized (object) {
            if (this.controlsAllowed == value) {
                return;
            }
            this.controlsAllowed = value;
            if (this.localMusic != null) {
                this.localMusic.setSuspended(!value);
            }
            if (!value) {
                if (this.session != null) {
                    this.session.commands.clear();
                }
                this.pendingSeek.clear();
                this.pendingVolume.clear();
                this.pendingFavorite.clear();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void suspendPlayback() {
        this.setControlsAllowed(false);
        this.setEnabled(false);
        Object object = this.lifecycle;
        synchronized (object) {
            if (this.localMusic != null) {
                this.localMusic.player().stop();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setEnabled(boolean value) {
        Session next;
        Session previous;
        Object object = this.lifecycle;
        synchronized (object) {
            if (this.closed || this.enabled == value) {
                return;
            }
            this.enabled = value;
            previous = this.session;
            Session session = next = value && this.supported && !this.localSelected() ? new Session(this.snapshot.revision(), this.lastSession) : null;
            if (next != null) {
                this.lastSession = next;
            }
            this.session = next;
            if (!value) {
                if (this.localMusic != null) {
                    this.localMusic.player().stop();
                }
                this.sources = MediaSources.empty();
                this.snapshot = MediaService.emptyAfter(this.snapshot);
                this.pendingSeek.clear();
                this.pendingVolume.clear();
                this.pendingFavorite.clear();
                this.ducking = new MediaDuckingIntent(this.ducking.epoch(), false, this.ducking.remainingThousandths());
            }
            this.diagnostic = "";
            this.status = !this.supported ? "unsupported_os" : (value ? "starting" : "stopped");
        }
        if (previous != null) {
            previous.stop();
        }
        if (next != null) {
            next.worker.start();
        }
    }

    public MediaSources sources() {
        ArrayList<MediaSources.Source> apps = new ArrayList<MediaSources.Source>(this.sources.apps());
        apps.add(new MediaSources.Source("bloodhelper:local", this.localMusic != null && this.localMusic.player().snapshot().playing()));
        return new MediaSources(apps, this.localSelected() ? "bloodhelper:local" : this.sources.activeId());
    }

    public String preferredSource() {
        return this.preferredSource;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setPreferredSource(String value) {
        Session next;
        Session previous;
        String clean = MediaSources.cleanId(value);
        Object object = this.lifecycle;
        synchronized (object) {
            if (this.closed || this.preferredSource.equals(clean)) {
                return;
            }
            this.preferredSource = clean;
            if (this.localMusic != null) {
                this.localMusic.player().stop();
            }
            previous = this.session;
            this.snapshot = MediaService.emptyAfter(this.snapshot);
            this.pendingSeek.clear();
            this.pendingVolume.clear();
            this.pendingFavorite.clear();
            Session session = next = this.enabled && this.supported && !this.localSelected() ? new Session(this.snapshot.revision(), this.lastSession) : null;
            if (next != null) {
                this.lastSession = next;
            }
            this.session = next;
            this.status = !this.supported ? "unsupported_os" : (this.enabled ? "starting" : "stopped");
            this.diagnostic = "";
        }
        if (previous != null) {
            previous.stop();
        }
        if (next != null) {
            next.worker.start();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public MediaSnapshot snapshot() {
        if (this.localSelected()) {
            return this.localMusic == null ? MediaSnapshot.empty() : this.localMusic.player().snapshot();
        }
        Object object = this.lifecycle;
        synchronized (object) {
            long now = System.nanoTime();
            MediaSnapshot visible = this.pendingPlayback.view(this.snapshot, now);
            long position = this.pendingSeek.base(this.snapshot, now);
            return position != this.snapshot.playbackPosition(now) ? visible.withPlayback(position, visible.playing(), now) : visible;
        }
    }

    public boolean isSupported() {
        return this.supported;
    }

    public boolean isEnabled() {
        return this.enabled && !this.closed;
    }

    public String status() {
        return this.localSelected() ? (this.localMusic == null ? "no_session" : this.localMusic.player().status()) : this.status;
    }

    public String diagnostic() {
        return this.localSelected() && this.localMusic != null ? this.localMusic.player().error() : this.diagnostic;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void playPause() {
        if (!this.controlsAllowed || this.closed) {
            return;
        }
        if (this.localSelected()) {
            this.localMusic().player().toggle();
            return;
        }
        Object object = this.lifecycle;
        synchronized (object) {
            if (this.offer("toggle")) {
                this.pendingPlayback.toggle(this.snapshot, System.nanoTime());
            }
        }
    }

    public void next() {
        if (!this.controlsAllowed || this.closed) {
            return;
        }
        if (this.localSelected()) {
            this.localMusic().next(1);
        } else {
            this.offer("next");
        }
    }

    public void previous() {
        if (!this.controlsAllowed || this.closed) {
            return;
        }
        if (this.localSelected()) {
            this.localMusic().next(-1);
        } else {
            this.offer("previous");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean favoritePending() {
        if (this.localSelected()) {
            return this.localMusic != null && this.localMusic.busy();
        }
        Object object = this.lifecycle;
        synchronized (object) {
            return this.pendingFavorite.active(this.snapshot, System.nanoTime());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long addFavorite(String expectedTrack) {
        if (!this.controlsAllowed || this.closed) {
            return 0L;
        }
        if (this.localSelected()) {
            return this.localMusic().favorite(expectedTrack);
        }
        Object object = this.lifecycle;
        synchronized (object) {
            MediaFavoriteCommand command;
            Session active = this.session;
            if (active == null || !active.active() || this.pendingFavorite.active(this.snapshot, System.nanoTime())) {
                return 0L;
            }
            Process connection = active.process;
            if (connection == null || !connection.isAlive()) {
                return 0L;
            }
            if ((command = MediaFavoriteCommand.create(this.snapshot, expectedTrack, ++this.nextFavoriteRequest)) == null) {
                return 0L;
            }
            long now = System.nanoTime();
            if (!active.commands.offer(new Command(command.encode(), now, connection, CommandKind.FAVORITE, command.trackToken()))) {
                return 0L;
            }
            this.pendingFavorite.queued(command, now);
            return command.requestId();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDucking(boolean active, double reductionPercent, Object context) {
        if (this.localMusic != null) {
            this.localMusic.player().ducking(active ? 1.0 - Math.clamp(reductionPercent, 0.0, 100.0) / 100.0 : 1.0);
        }
        Object object = this.lifecycle;
        synchronized (object) {
            MediaDuckingIntent next;
            if (this.closed) {
                return;
            }
            if (this.duckingContext != context || active && !this.ducking.active()) {
                this.duckingContext = context;
                long l = this.duckingEpoch = this.duckingEpoch == Long.MAX_VALUE ? 1L : this.duckingEpoch + 1L;
            }
            if (!(next = MediaDuckingIntent.create(this.duckingEpoch, active, reductionPercent)).equals(this.ducking)) {
                this.ducking = next;
            }
            this.duckingUpdatedNanos = System.nanoTime();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long seekTo(long positionMs, String expectedTrack) {
        if (!this.controlsAllowed || this.closed) {
            return 0L;
        }
        if (this.localSelected()) {
            return this.localMusic().player().seek(positionMs, expectedTrack);
        }
        Object object = this.lifecycle;
        synchronized (object) {
            MediaSeekCommand seek;
            Session active = this.session;
            if (active == null || !active.active()) {
                return 0L;
            }
            Process connection = active.process;
            if (connection == null || !connection.isAlive()) {
                return 0L;
            }
            if ((seek = MediaSeekCommand.create(this.snapshot, expectedTrack, ++this.nextSeekRequest, positionMs)) == null) {
                return 0L;
            }
            active.commands.removeIf(command -> command.kind() == CommandKind.SEEK);
            if (!active.commands.offer(new Command(seek.encode(), System.nanoTime(), connection, CommandKind.SEEK, seek.trackToken()))) {
                return 0L;
            }
            this.pendingSeek.queued(seek, System.nanoTime());
            return seek.requestId();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean setVolume(double volume) {
        Object object = this.lifecycle;
        synchronized (object) {
            return this.setVolume(volume, this.snapshot().volumeTargetToken());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean setVolume(double volume, String expectedTargetToken) {
        if (!this.controlsAllowed || this.closed) {
            return false;
        }
        if (this.localSelected()) {
            boolean accepted = this.localMusic().player().volume(volume, expectedTargetToken);
            if (accepted) {
                this.localMusic().rememberVolume();
            }
            return accepted;
        }
        Object object = this.lifecycle;
        synchronized (object) {
            MediaVolumeCommand command;
            Session active = this.session;
            if (active == null || !active.active()) {
                return false;
            }
            Process connection = active.process;
            if (connection == null || !connection.isAlive()) {
                return false;
            }
            if ((command = MediaVolumeCommand.create(this.snapshot, expectedTargetToken, ++this.nextVolumeRequest, volume)) == null) {
                return false;
            }
            active.commands.removeIf(queued -> queued.kind() == CommandKind.VOLUME);
            long now = System.nanoTime();
            if (!active.commands.offer(new Command(command.encode(), now, connection, CommandKind.VOLUME, command.targetToken()))) {
                return false;
            }
            this.pendingVolume.queued(command, now);
            this.snapshot = this.snapshot.withVolume(command.volume());
            return true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long seekBy(long deltaMs) {
        if (this.localSelected()) {
            MediaSnapshot sample = this.snapshot();
            return this.seekTo(Math.max(0L, sample.playbackPosition() + Math.clamp(deltaMs, -31536000000L, 31536000000L)), sample.trackToken());
        }
        Object object = this.lifecycle;
        synchronized (object) {
            MediaSnapshot sample = this.snapshot;
            long base = this.pendingSeek.base(sample, System.nanoTime());
            long target = Math.clamp(base, 0L, 31536000000L) + Math.clamp(deltaMs, -31536000000L, 31536000000L);
            return this.seekTo(target, sample.trackToken());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean offer(String command) {
        if (!this.controlsAllowed || this.closed) {
            return false;
        }
        Object object = this.lifecycle;
        synchronized (object) {
            Process process;
            Session active = this.session;
            if (active != null && active.active() && this.snapshot.available() && (process = active.process) != null && process.isAlive()) {
                String token = this.platform == MediaBridgePlatform.LINUX ? this.snapshot.volumeTargetToken() : "";
                String wire = this.platform == MediaBridgePlatform.LINUX ? "transport:" + token + ":" + command : command;
                return active.commands.offer(new Command(wire, System.nanoTime(), process, CommandKind.TRANSPORT, token));
            }
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void rejectUnsentFavorite(Command command) {
        if (command == null || command.kind() != CommandKind.FAVORITE) {
            return;
        }
        Object object = this.lifecycle;
        synchronized (object) {
            this.pendingFavorite.rejected(command.name());
        }
    }

    private void finishConnection(Process bridge) {
        try {
            bridge.getOutputStream().close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        try {
            if (!bridge.waitFor(this.platform == MediaBridgePlatform.LINUX ? 8L : 3L, TimeUnit.SECONDS)) {
                bridge.destroyForcibly();
            }
        }
        catch (InterruptedException interrupted) {
            bridge.destroyForcibly();
            Thread.currentThread().interrupt();
        }
    }

    private static MediaSnapshot emptyAfter(MediaSnapshot previous) {
        return new MediaSnapshot("", "", 0L, 0L, false, null, previous.revision() + 1L);
    }

    private static String safeMessage(String message) {
        String clean = message.replaceAll("[\\p{Cntrl}]", " ");
        return clean.substring(0, Math.min(512, clean.length()));
    }

    private static Thread daemon(String name, Runnable action) {
        Thread thread = new Thread(action, name);
        thread.setDaemon(true);
        return thread;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        Session previous;
        if (this.localMusic != null) {
            this.localMusic.close();
        }
        Object object = this.lifecycle;
        synchronized (object) {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.enabled = false;
            previous = this.session;
            this.session = null;
            this.sources = MediaSources.empty();
            this.snapshot = MediaService.emptyAfter(this.snapshot);
            this.pendingSeek.clear();
            this.pendingVolume.clear();
            this.pendingFavorite.clear();
            this.ducking = new MediaDuckingIntent(this.ducking.epoch(), false, this.ducking.remainingThousandths());
            this.duckingContext = null;
            this.status = this.supported ? "stopped" : "unsupported_os";
        }
        if (previous != null) {
            previous.stop();
        }
    }

    private final class Session {
        private final AtomicBoolean stopped = new AtomicBoolean();
        private final ArrayBlockingQueue<Command> commands = new ArrayBlockingQueue(16);
        private final MediaProtocol protocol;
        private final String selectedSource;
        private final Thread worker;
        private final CountDownLatch finished;
        private Session predecessor;
        private volatile Process process;
        private volatile long lastPacketNanos;

        Session(long initialRevision, Session previous) {
            this.selectedSource = MediaService.this.preferredSource;
            this.finished = new CountDownLatch(1);
            this.protocol = new MediaProtocol(initialRevision);
            this.predecessor = previous;
            this.worker = MediaService.daemon("bloodhelper-media", this::run);
        }

        boolean active() {
            return !this.stopped.get() && !MediaService.this.closed && MediaService.this.enabled && MediaService.this.session == this;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void publish(MediaSnapshot observation) {
            Object object = MediaService.this.lifecycle;
            synchronized (object) {
                if (!this.active()) {
                    return;
                }
                MediaService.this.sources = this.protocol.sources();
                MediaService.this.snapshot = MediaService.this.pendingVolume.reconcile(observation, System.nanoTime());
                MediaService.this.pendingSeek.base(observation, System.nanoTime());
                MediaService.this.pendingFavorite.active(observation, System.nanoTime());
                MediaService.this.status = observation.available() ? (observation.playing() ? "playing" : "paused") : (this.protocol.diagnostic().isEmpty() ? "no_session" : "unavailable");
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void setStatus(String value) {
            Object object = MediaService.this.lifecycle;
            synchronized (object) {
                if (this.active()) {
                    MediaService.this.status = value;
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void diagnose(String value) {
            Object object = MediaService.this.lifecycle;
            synchronized (object) {
                if (this.active()) {
                    MediaService.this.diagnostic = MediaService.safeMessage(value);
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Loose catch block
         */
        void run() {
            block57: {
                Path directory;
                block54: {
                    block48: {
                        block49: {
                            directory = null;
                            if (this.predecessor != null) {
                                while (this.active() && !this.predecessor.finished.await(100L, TimeUnit.MILLISECONDS)) {
                                }
                                this.predecessor = null;
                            }
                            if (this.active()) break block48;
                            if (directory == null) break block49;
                            try {
                                for (String resource : MediaService.this.platform.resources()) {
                                    Files.deleteIfExists(directory.resolve(resource));
                                }
                                Files.deleteIfExists(directory);
                            }
                            catch (IOException iOException) {
                                // empty catch block
                            }
                        }
                        this.predecessor = null;
                        this.finished.countDown();
                        return;
                    }
                    String[] parent23332 = Path.of(System.getProperty("java.io.tmpdir"), "bloodhelper");
                    Files.createDirectories((Path)parent23332, new FileAttribute[0]);
                    directory = Files.createTempDirectory((Path)parent23332, "media-", new FileAttribute[0]);
                    String[] stringArray = MediaService.this.platform.resources();
                    int n = stringArray.length;
                    for (int resource = 0; resource < n; ++resource) {
                        String resource2 = stringArray[resource];
                        try (InputStream embedded = MediaService.class.getResourceAsStream("/bloodhelper/" + resource2);){
                            if (embedded == null) {
                                throw new IOException("Bundled media resource is missing");
                            }
                            Files.copy(embedded, directory.resolve(resource2), new CopyOption[0]);
                            continue;
                        }
                    }
                    List<String> arguments = MediaService.this.platform.arguments(directory, this.selectedSource);
                    int failures = 0;
                    while (this.active()) {
                        long connectedAt;
                        block53: {
                            Process connection;
                            block50: {
                                block51: {
                                    connectedAt = System.nanoTime();
                                    connection = null;
                                    this.setStatus("starting");
                                    this.process = connection = new ProcessBuilder(arguments).start();
                                    if (this.active()) break block50;
                                    if (connection == null) break block51;
                                    MediaService.this.finishConnection(connection);
                                }
                                this.process = null;
                                this.commands.clear();
                                this.publish(this.protocol.clear());
                                break;
                            }
                            try {
                                this.lastPacketNanos = System.nanoTime();
                                Process bridge = connection;
                                MediaService.daemon("bloodhelper-media-controls", () -> this.writeCommands(bridge)).start();
                                MediaService.daemon("bloodhelper-media-diagnostics", () -> this.drainErrors(bridge)).start();
                                MediaService.daemon("bloodhelper-media-watchdog", () -> this.watch(bridge)).start();
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));){
                                    String line;
                                    while (this.active() && (line = MediaProtocol.readLine(reader)) != null) {
                                        if (line.isBlank()) continue;
                                        MediaSnapshot observation = this.protocol.accept(line);
                                        this.lastPacketNanos = System.nanoTime();
                                        this.publish(observation);
                                        this.diagnose(this.protocol.diagnostic());
                                    }
                                }
                                if (connection == null) break block53;
                            }
                            catch (IOException | RuntimeException failure) {
                                try {
                                    this.diagnose(failure.getClass().getSimpleName() + ": " + failure.getMessage());
                                    if (connection != null) {
                                        MediaService.this.finishConnection(connection);
                                    }
                                    this.process = null;
                                    this.commands.clear();
                                    this.publish(this.protocol.clear());
                                }
                                catch (Throwable throwable) {
                                    if (connection != null) {
                                        MediaService.this.finishConnection(connection);
                                    }
                                    this.process = null;
                                    this.commands.clear();
                                    this.publish(this.protocol.clear());
                                    throw throwable;
                                }
                            }
                            MediaService.this.finishConnection(connection);
                        }
                        this.process = null;
                        this.commands.clear();
                        this.publish(this.protocol.clear());
                        if (!this.active()) continue;
                        this.setStatus("unavailable");
                        if (System.nanoTime() - connectedAt > TimeUnit.SECONDS.toNanos(30L)) {
                            failures = 0;
                        }
                        long retryMs = Math.min(30000L, 1000L << Math.min(5, failures++));
                        Thread.sleep(retryMs);
                    }
                    if (directory == null) break block54;
                    try {
                        for (String resource : MediaService.this.platform.resources()) {
                            Files.deleteIfExists(directory.resolve(resource));
                        }
                        Files.deleteIfExists(directory);
                    }
                    catch (IOException parent23332) {
                        // empty catch block
                    }
                }
                this.predecessor = null;
                this.finished.countDown();
                break block57;
                catch (IOException | RuntimeException failure2222) {
                    block55: {
                        this.diagnose(failure2222.getClass().getSimpleName() + ": " + failure2222.getMessage());
                        this.setStatus("unavailable");
                        if (directory == null) break block55;
                        try {
                            for (String resource : MediaService.this.platform.resources()) {
                                Files.deleteIfExists(directory.resolve(resource));
                            }
                            Files.deleteIfExists(directory);
                        }
                        catch (IOException failure2222) {
                            // empty catch block
                        }
                    }
                    this.predecessor = null;
                    this.finished.countDown();
                }
                catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    {
                        catch (Throwable throwable) {
                            throw throwable;
                        }
                    }
                }
                finally {
                    if (directory != null) {
                        try {
                            for (String resource : MediaService.this.platform.resources()) {
                                Files.deleteIfExists(directory.resolve(resource));
                            }
                            Files.deleteIfExists(directory);
                        }
                        catch (IOException iOException) {}
                    }
                    this.predecessor = null;
                    this.finished.countDown();
                }
            }
        }

        void writeCommands(Process connection) {
            try (BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));){
                long lastVolumeNanos = 0L;
                long lastDuckingNanos = 0L;
                MediaDuckingIntent sentDucking = null;
                while (this.active() && connection.isAlive() && this.process == connection) {
                    Command command;
                    MediaDuckingIntent desired = MediaService.this.ducking;
                    long now = System.nanoTime();
                    if (desired.active() && now - MediaService.this.duckingUpdatedNanos > TimeUnit.SECONDS.toNanos(3L)) {
                        desired = new MediaDuckingIntent(desired.epoch(), false, desired.remainingThousandths());
                    }
                    if (!desired.equals(sentDucking) || desired.active() && now - lastDuckingNanos >= TimeUnit.SECONDS.toNanos(1L)) {
                        writer2.write(desired.encode());
                        writer2.newLine();
                        writer2.flush();
                        sentDucking = desired;
                        lastDuckingNanos = now;
                    }
                    if ((command = this.commands.poll(100L, TimeUnit.MILLISECONDS)) == null || command.connection() != connection || !this.active() || this.process != connection || System.nanoTime() - command.createdNanos() > TimeUnit.SECONDS.toNanos(2L)) {
                        MediaService.this.rejectUnsentFavorite(command);
                        continue;
                    }
                    if (command.kind() == CommandKind.VOLUME) {
                        long wait = TimeUnit.MILLISECONDS.toNanos(50L) - (System.nanoTime() - lastVolumeNanos);
                        if (wait > 0L) {
                            TimeUnit.NANOSECONDS.sleep(wait);
                        }
                        for (Command queued : this.commands) {
                            if (queued.kind() != CommandKind.VOLUME || queued.connection() != connection || queued.createdNanos() < command.createdNanos() || !this.commands.remove(queued)) continue;
                            command = queued;
                        }
                    }
                    MediaSnapshot observed = MediaService.this.snapshot;
                    if (!MediaService.this.controlsAllowed || !this.active() || this.process != connection) {
                        MediaService.this.rejectUnsentFavorite(command);
                        continue;
                    }
                    if (command.kind() == CommandKind.TRANSPORT && !command.token().isEmpty() && !command.token().equals(observed.volumeTargetToken()) || command.kind() == CommandKind.SEEK && (!observed.canSeek() || !command.token().equals(observed.trackToken()))) continue;
                    if (!(command.kind() != CommandKind.FAVORITE || observed.favorite().canAdd() && command.token().equals(observed.trackToken()))) {
                        MediaService.this.rejectUnsentFavorite(command);
                        continue;
                    }
                    if (command.kind() == CommandKind.VOLUME && (!observed.canControlVolume() || !command.token().equals(observed.volumeTargetToken()))) continue;
                    writer2.write(command.name());
                    writer2.newLine();
                    writer2.flush();
                    if (command.kind() != CommandKind.VOLUME) continue;
                    lastVolumeNanos = System.nanoTime();
                }
            }
            catch (IOException writer2) {
            }
            catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        void watch(Process connection) {
            try {
                while (this.active() && connection.isAlive() && this.process == connection) {
                    Thread.sleep(1000L);
                    if (System.nanoTime() - this.lastPacketNanos <= STALE_NANOS) continue;
                    this.diagnose("Media bridge timed out");
                    MediaService.this.finishConnection(connection);
                    return;
                }
            }
            catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        void drainErrors(Process connection) {
            try (InputStreamReader reader = new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8);){
                int count;
                char[] buffer = new char[1024];
                while ((count = reader.read(buffer)) >= 0) {
                    if (count <= 0 || this.process != connection) continue;
                    this.diagnose(new String(buffer, 0, count));
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }

        void stop() {
            this.stopped.set(true);
            this.commands.clear();
            Process bridge = this.process;
            if (bridge != null) {
                MediaService.daemon("bloodhelper-media-stop", () -> MediaService.this.finishConnection(bridge)).start();
            } else {
                this.worker.interrupt();
            }
        }
    }

    private record Command(String name, long createdNanos, Process connection, CommandKind kind, String token) {
    }

    private static enum CommandKind {
        TRANSPORT,
        SEEK,
        VOLUME,
        FAVORITE;

    }
}

