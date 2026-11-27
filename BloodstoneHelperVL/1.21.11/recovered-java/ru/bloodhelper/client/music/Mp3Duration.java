/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javazoom.jl.decoder.Bitstream
 *  javazoom.jl.decoder.BitstreamException
 *  javazoom.jl.decoder.Header
 */
package ru.bloodhelper.client.music;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;

final class Mp3Duration {
    private static final LinkedHashMap<FileKey, Long> CACHE = new LinkedHashMap(32, 0.75f, true);

    private Mp3Duration() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static long milliseconds(File file) throws IOException {
        Path path = file.toPath().toRealPath(new LinkOption[0]);
        FileKey key = new FileKey(path, Files.size(path), Files.getLastModifiedTime(path, new LinkOption[0]).toMillis());
        LinkedHashMap<FileKey, Long> linkedHashMap = CACHE;
        synchronized (linkedHashMap) {
            Long cached = CACHE.get(key);
            if (cached != null) {
                return cached;
            }
        }
        double duration = 0.0;
        try (BufferedInputStream input = new BufferedInputStream(Files.newInputStream(path, new OpenOption[0]), 65536);){
            Bitstream stream = new Bitstream((InputStream)input);
            try {
                Header header = stream.readFrame();
                while (header != null) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedIOException("MP3 scan interrupted");
                    }
                    duration += (double)header.ms_per_frame();
                    stream.closeFrame();
                    header = stream.readFrame();
                }
            }
            catch (BitstreamException invalid) {
                throw new IOException("Invalid MP3 frame", invalid);
            }
            finally {
                try {
                    stream.close();
                }
                catch (BitstreamException bitstreamException) {}
            }
        }
        long result = Math.round(duration);
        if (result <= 0L) {
            throw new IOException("MP3 contains no audio frames");
        }
        LinkedHashMap<FileKey, Long> linkedHashMap2 = CACHE;
        synchronized (linkedHashMap2) {
            CACHE.put(key, result);
            while (CACHE.size() > 32) {
                CACHE.remove(CACHE.keySet().iterator().next());
            }
        }
        return result;
    }

    private record FileKey(Path path, long size, long modified) {
    }
}

