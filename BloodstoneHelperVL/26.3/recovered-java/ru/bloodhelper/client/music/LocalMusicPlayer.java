/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import ru.bloodhelper.client.music.LocalMusicLibrary;
import ru.bloodhelper.client.music.LocalMusicLyrics;
import ru.bloodhelper.client.music.MediaFavoriteState;
import ru.bloodhelper.client.music.MediaSnapshot;
import ru.bloodhelper.client.music.Mp3Duration;
import ru.bloodhelper.client.music.VorbisPcmStream;

public final class LocalMusicPlayer
implements AutoCloseable {
    private final Object lock = new Object();
    private volatile boolean closed;
    private volatile boolean playing;
    private volatile boolean ended;
    private volatile LocalMusicLibrary.Track track;
    private volatile MediaSnapshot snapshot = MediaSnapshot.empty();
    private volatile String status = "no_session";
    private volatile String error = "";
    private volatile double volume = 0.7;
    private volatile double ducking = 1.0;
    private long generation;
    private long targetMs;
    private long seekRequest;
    private long revision;
    private Thread worker;

    public MediaSnapshot snapshot() {
        return this.snapshot;
    }

    public String status() {
        return this.status;
    }

    public String error() {
        return this.error;
    }

    public String trackId() {
        LocalMusicLibrary.Track value = this.track;
        return value == null ? "" : value.id();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void play(LocalMusicLibrary.Track next, double initialVolume) {
        Object object = this.lock;
        synchronized (object) {
            if (this.closed) {
                return;
            }
            this.track = next;
            this.volume = Math.clamp(initialVolume, 0.0, 1.0);
            this.playing = true;
            this.ended = false;
            this.targetMs = 0L;
            this.seekRequest = 0L;
            ++this.generation;
            this.error = "";
            this.status = "starting";
            this.snapshot = MediaSnapshot.empty();
            if (this.worker == null) {
                this.worker = new Thread(this::run, "bloodhelper-local-audio");
                this.worker.setDaemon(true);
                this.worker.start();
            }
            this.lock.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void toggle() {
        Object object = this.lock;
        synchronized (object) {
            if (this.track != null && !this.closed) {
                if (this.ended || "unavailable".equals(this.status)) {
                    this.targetMs = 0L;
                    ++this.generation;
                    this.ended = false;
                }
                this.playing = !this.playing;
                this.lock.notifyAll();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void stop() {
        Object object = this.lock;
        synchronized (object) {
            this.track = null;
            this.playing = false;
            ++this.generation;
            this.snapshot = MediaSnapshot.empty();
            this.status = "no_session";
            this.lock.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long seek(long millis, String expected) {
        Object object = this.lock;
        synchronized (object) {
            if (this.closed || this.track == null || !this.track.id().equals(expected) || !this.snapshot.canSeek()) {
                return 0L;
            }
            this.targetMs = Math.clamp(millis, 0L, Math.max(0L, this.snapshot.durationMs() - 1L));
            this.ended = false;
            ++this.generation;
            this.lock.notifyAll();
            return ++this.seekRequest;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean volume(double value, String expected) {
        Object object = this.lock;
        synchronized (object) {
            if (this.closed || this.track == null || !this.track.id().equals(expected) || !Double.isFinite(value)) {
                return false;
            }
            this.volume = Math.clamp(value, 0.0, 1.0);
            this.snapshot = this.snapshot.withVolume(this.volume);
            return true;
        }
    }

    public void ducking(double factor) {
        this.ducking = Double.isFinite(factor) ? Math.clamp(factor, 0.0, 1.0) : 1.0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void refresh(LocalMusicLibrary.Track updated) {
        Object object = this.lock;
        synchronized (object) {
            if (this.track != null && updated != null && updated.id().equals(this.track.id())) {
                this.track = updated;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void run() {
        long observed = -1L;
        while (!this.closed) {
            long request;
            long offset;
            long token;
            LocalMusicLibrary.Track selected;
            Object object = this.lock;
            synchronized (object) {
                block14: {
                    try {
                        while (!(this.closed || this.track != null && this.generation != observed)) {
                            this.lock.wait();
                        }
                    }
                    catch (InterruptedException interrupted) {
                        if (!this.closed) break block14;
                        break;
                    }
                }
                if (this.closed) {
                    break;
                }
                selected = this.track;
                token = this.generation;
                observed = this.generation;
                offset = this.targetMs;
                request = this.seekRequest;
            }
            try {
                this.stream(selected, token, offset, request);
            }
            catch (Exception failure) {
                Object object2 = this.lock;
                synchronized (object2) {
                    if (token != this.generation || this.closed) {
                        continue;
                    }
                    this.playing = false;
                    this.status = "unavailable";
                    this.error = failure instanceof LineUnavailableException ? "Audio output is unavailable" : (failure instanceof UnsupportedAudioFileException ? "Unsupported or damaged audio file" : "Could not read or play this file");
                    this.snapshot = MediaSnapshot.empty();
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean current(long token) {
        Object object = this.lock;
        synchronized (object) {
            return !this.closed && this.generation == token && this.track != null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void stream(LocalMusicLibrary.Track selected, long token, long offset, long request) throws Exception {
        if (!Files.isRegularFile(selected.file(), new LinkOption[0])) {
            throw new IOException("Missing file");
        }
        LocalMusicLyrics.load(selected);
        AudioFileFormat metadata = AudioSystem.getAudioFileFormat(selected.file().toFile());
        try (AudioInputStream encoded = AudioSystem.getAudioInputStream(selected.file().toFile());){
            AudioFormat source = encoded.getFormat();
            if (!Float.isFinite(source.getSampleRate())) throw new UnsupportedAudioFileException();
            if (source.getSampleRate() < 8000.0f) throw new UnsupportedAudioFileException();
            if (source.getSampleRate() > 192000.0f) throw new UnsupportedAudioFileException();
            if (source.getChannels() < 1) throw new UnsupportedAudioFileException();
            if (source.getChannels() > 2) {
                throw new UnsupportedAudioFileException();
            }
            AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, source.getSampleRate(), 16, source.getChannels(), source.getChannels() * 2, source.getSampleRate(), false);
            try (AudioInputStream decoded = LocalMusicPlayer.decode(selected.file().toFile(), metadata, pcm, encoded);){
                long skipped;
                int read;
                long duration = source.getEncoding().toString().startsWith("MPEG") ? Mp3Duration.milliseconds(selected.file().toFile()) : LocalMusicPlayer.durationMs(metadata, decoded);
                long framesToSkip = (long)((float)Math.max(0L, offset) * pcm.getFrameRate() / 1000.0f);
                long skipBytes = framesToSkip * (long)pcm.getFrameSize();
                byte[] bytes = new byte[4096 - 4096 % pcm.getFrameSize()];
                for (skipped = 0L; skipped < skipBytes && this.current(token) && (read = decoded.read(bytes, 0, (int)Math.min((long)bytes.length, skipBytes - skipped))) >= 0; skipped += (long)read) {
                }
                if (!this.current(token)) {
                    return;
                }
                long base = (long)((double)skipped / (double)pcm.getFrameSize() * 1000.0 / (double)pcm.getFrameRate());
                SourceDataLine line = AudioSystem.getSourceDataLine(pcm);
                try {
                    line.open(pcm, Math.max(bytes.length, (int)(pcm.getFrameRate() / 10.0f) * pcm.getFrameSize()));
                    line.start();
                    while (this.current(token)) {
                        int read2;
                        if (!this.playing) {
                            line.stop();
                            this.publish(selected, token, base + line.getMicrosecondPosition() / 1000L, duration, request, false);
                            Object object = this.lock;
                            synchronized (object) {
                                if (!this.playing && this.current(token)) {
                                    this.lock.wait(50L);
                                }
                                continue;
                            }
                        }
                        if (!line.isRunning()) {
                            line.start();
                        }
                        if ((read2 = decoded.read(bytes, 0, bytes.length)) < 0) {
                            while (this.current(token) && this.playing && line.available() < line.getBufferSize()) {
                                Thread.sleep(10L);
                            }
                            if (!this.current(token)) {
                                return;
                            }
                            if (!this.playing) continue;
                            this.playing = false;
                            this.ended = true;
                            this.publish(selected, token, duration > 0L ? duration : base + line.getMicrosecondPosition() / 1000L, duration, request, false);
                            return;
                        }
                        LocalMusicPlayer.applyGain(bytes, read2, this.volume * this.ducking);
                        for (int written = 0; written < read2 && this.current(token); written += line.write(bytes, written, read2 - written)) {
                        }
                        this.publish(selected, token, base + line.getMicrosecondPosition() / 1000L, duration, request, true);
                    }
                    return;
                }
                finally {
                    line.stop();
                    line.flush();
                    line.close();
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void publish(LocalMusicLibrary.Track selected, long token, long position, long duration, long request, boolean active) {
        Object object = this.lock;
        synchronized (object) {
            if (!this.current(token)) {
                return;
            }
            LocalMusicLibrary.Track current = this.track;
            this.snapshot = new MediaSnapshot(current.title(), "Local music", position, duration, active, null, ++this.revision, duration > 0L, current.id(), request, request > 0L, 1.0, System.nanoTime(), true, this.volume, current.id(), 0L, false, new MediaFavoriteState("local", true, current.favorite(), 0L, false, current.favorite() ? "saved" : "ready"));
            this.status = active ? "playing" : "paused";
        }
    }

    static long durationMs(AudioFileFormat metadata, AudioInputStream decoded) {
        Number number;
        Object property = metadata.properties().get("duration");
        if (property instanceof Number && (number = (Number)property).longValue() > 0L) {
            return number.longValue() / 1000L;
        }
        return decoded.getFrameLength() > 0L && decoded.getFormat().getFrameRate() > 0.0f ? (long)((float)(decoded.getFrameLength() * 1000L) / decoded.getFormat().getFrameRate()) : 0L;
    }

    static AudioInputStream decode(File file, AudioFileFormat metadata, AudioFormat pcm, AudioInputStream encoded) throws IOException {
        if ("VORBISENC".equals(encoded.getFormat().getEncoding().toString())) {
            long l;
            Object value = metadata.properties().get("duration");
            if (value instanceof Number) {
                Number n = (Number)value;
                l = n.longValue() / 1000L;
            } else {
                l = 0L;
            }
            return VorbisPcmStream.open(file, l);
        }
        return AudioSystem.getAudioInputStream(pcm, encoded);
    }

    static void applyGain(byte[] bytes, int count, double gain) {
        int i = 0;
        while (i + 1 < count) {
            short value = (short)(bytes[i] & 0xFF | bytes[i + 1] << 8);
            int scaled = (int)Math.round((double)value * Math.clamp(gain, 0.0, 1.0));
            bytes[i] = (byte)scaled;
            bytes[i + 1] = (byte)(scaled >> 8);
            i += 2;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        Object object = this.lock;
        synchronized (object) {
            this.closed = true;
            ++this.generation;
            this.lock.notifyAll();
            if (this.worker != null) {
                this.worker.interrupt();
            }
        }
    }
}

