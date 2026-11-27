/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import ru.bloodhelper.client.music.LocalMusicLibrary;
import ru.bloodhelper.client.music.LocalMusicPlayer;

public final class LocalMusicController
implements AutoCloseable {
    public static final String SOURCE = "bloodhelper:local";
    private final LocalMusicLibrary library;
    private final LocalMusicPlayer player = new LocalMusicPlayer();
    private final ExecutorService io = Executors.newSingleThreadExecutor(r -> {
        Thread worker = new Thread(r, "bloodhelper-music-library");
        worker.setDaemon(true);
        return worker;
    });
    private volatile boolean ready;
    private volatile boolean busy = true;
    private volatile boolean closed;
    private volatile boolean suspended;
    private volatile String notice = "Loading library\u2026";
    private volatile Set<String> missing = Set.of();
    private final AtomicReference<Double> pendingVolume = new AtomicReference();
    private final AtomicBoolean savingVolume = new AtomicBoolean();

    public LocalMusicController(Path storage) {
        this.library = new LocalMusicLibrary(storage);
        this.io.execute(() -> {
            try {
                this.library.load();
                this.refreshMissing();
                this.ready = true;
                this.notice = "";
            }
            catch (Exception failure) {
                this.notice = "Music library could not be read; existing file was preserved";
            }
            finally {
                this.busy = false;
            }
        });
    }

    public void setSuspended(boolean value) {
        if (this.suspended == value) {
            return;
        }
        this.suspended = value;
        if (value) {
            this.player.stop();
        }
    }

    public LocalMusicLibrary library() {
        return this.library;
    }

    public LocalMusicPlayer player() {
        return this.player;
    }

    public boolean busy() {
        return this.busy;
    }

    public String notice() {
        return this.notice;
    }

    public boolean missing(String id) {
        return this.missing.contains(id);
    }

    public boolean ready() {
        return this.ready;
    }

    public synchronized void run(Action action) {
        if (this.suspended || this.closed || this.busy || !this.ready) {
            return;
        }
        this.busy = true;
        this.notice = "";
        this.io.execute(() -> {
            try {
                if (this.suspended || this.closed) {
                    return;
                }
                action.run();
                this.refreshMissing();
                this.player.refresh(this.library.find(this.player.trackId()));
            }
            catch (Exception failure) {
                this.notice = LocalMusicController.safeError(failure);
            }
            finally {
                this.busy = false;
            }
        });
    }

    private static String safeError(Exception failure) {
        String message = failure.getMessage();
        return message == null || message.isBlank() ? "Could not complete this action" : message.replaceAll("[\\p{Cntrl}]", " ");
    }

    public void importPaths(Collection<Path> paths) {
        this.run(() -> {
            ArrayList<Path> files = new ArrayList<Path>();
            for (Path path : paths) {
                if (Files.isDirectory(path, new LinkOption[0])) {
                    files.addAll(LocalMusicLibrary.folderFiles(path));
                    continue;
                }
                files.add(path);
            }
            LocalMusicLibrary.ImportResult result = this.library.add(files);
            this.notice = "+" + result.added() + " \u00b7 skipped: " + result.skipped();
        });
    }

    public void rename(String id, String title) {
        this.run(() -> {
            this.library.rename(id, title);
            this.notice = "Saved";
        });
    }

    public void remove(String id) {
        this.run(() -> {
            this.library.remove(id);
            if (this.player.trackId().equals(id)) {
                this.player.stop();
            }
            this.notice = "Removed from library; file kept";
        });
    }

    public void relink(String id, Path file) {
        this.run(() -> {
            this.library.relink(id, file);
            this.notice = "Link restored";
        });
    }

    public void relinkFolder(Path folder) {
        this.run(() -> {
            LocalMusicLibrary.RelinkResult result = this.library.relinkFolder(folder);
            this.notice = "Restored: " + result.restored() + " \u00b7 missing: " + result.missing();
        });
    }

    public void play(String id) {
        if (this.suspended || !this.ready || this.closed) {
            return;
        }
        LocalMusicLibrary.Track track = this.library.find(id);
        if (track != null) {
            this.player.play(track, this.library.volume());
        }
    }

    public void next(int direction) {
        List<LocalMusicLibrary.Track> tracks = this.library.tracks();
        for (int i = 0; i < tracks.size(); ++i) {
            if (!tracks.get(i).id().equals(this.player.trackId())) continue;
            for (int step = 1; step <= tracks.size(); ++step) {
                LocalMusicLibrary.Track next = tracks.get(Math.floorMod(i + direction * step, tracks.size()));
                if (this.missing(next.id())) continue;
                this.play(next.id());
                return;
            }
            return;
        }
    }

    public long favorite(String id) {
        if (!id.equals(this.player.trackId()) || this.busy) {
            return 0L;
        }
        this.run(() -> this.library.favorite(id));
        return 1L;
    }

    public void rememberVolume() {
        if (this.suspended || !this.ready || this.closed) {
            return;
        }
        this.pendingVolume.set(this.player.snapshot().volume());
        this.scheduleVolume();
    }

    private void scheduleVolume() {
        if (!this.savingVolume.compareAndSet(false, true)) {
            return;
        }
        this.io.execute(() -> {
            try {
                Double value;
                while ((value = (Double)this.pendingVolume.getAndSet(null)) != null) {
                    this.library.volume(value);
                }
            }
            catch (Exception failure) {
                this.notice = LocalMusicController.safeError(failure);
            }
            finally {
                this.savingVolume.set(false);
                if (this.pendingVolume.get() != null && !this.closed) {
                    this.scheduleVolume();
                }
            }
        });
    }

    private void refreshMissing() {
        HashSet<String> values = new HashSet<String>();
        for (LocalMusicLibrary.Track track : this.library.tracks()) {
            if (Files.isRegularFile(track.file(), new LinkOption[0])) continue;
            values.add(track.id());
        }
        this.missing = Set.copyOf(values);
    }

    public void choose(boolean directory, boolean multiple, Consumer<List<Path>> selected) {
        if (this.suspended || this.busy || !this.ready || this.closed) {
            return;
        }
        Thread picker = new Thread(() -> {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle(directory ? "BloodHelper \u00b7 Music folder" : "BloodHelper \u00b7 MP3 / WAV / OGG");
                    chooser.setFileSelectionMode(directory ? 1 : 0);
                    chooser.setMultiSelectionEnabled(multiple);
                    if (!directory) {
                        chooser.setFileFilter(new FileNameExtensionFilter("MP3, WAV, OGG", "mp3", "wav", "ogg"));
                    }
                    if (chooser.showOpenDialog(null) == 0) {
                        File[] fileArray;
                        if (multiple) {
                            fileArray = chooser.getSelectedFiles();
                        } else {
                            File[] fileArray2 = new File[1];
                            fileArray = fileArray2;
                            fileArray2[0] = chooser.getSelectedFile();
                        }
                        File[] files = fileArray;
                        List<Path> paths = Arrays.stream(files).filter(Objects::nonNull).map(File::toPath).toList();
                        if (!(paths.isEmpty() || this.closed || this.suspended)) {
                            selected.accept(paths);
                        }
                    }
                });
            }
            catch (Exception | LinkageError unavailable) {
                this.notice = "File chooser unavailable; paste a file or folder path";
            }
        }, "bloodhelper-music-picker");
        picker.setDaemon(true);
        picker.start();
    }

    @Override
    public void close() {
        this.closed = true;
        this.player.close();
        this.io.shutdown();
    }

    @FunctionalInterface
    public static interface Action {
        public void run() throws Exception;
    }
}

