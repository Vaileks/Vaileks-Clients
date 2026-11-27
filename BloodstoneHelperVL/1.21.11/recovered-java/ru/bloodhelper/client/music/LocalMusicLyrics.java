/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.music;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import ru.bloodhelper.client.lyrics.LrcParser;
import ru.bloodhelper.client.lyrics.LyricLine;
import ru.bloodhelper.client.music.LocalMusicLibrary;

public final class LocalMusicLyrics {
    private static volatile Lyrics current;

    private LocalMusicLyrics() {
    }

    static void load(LocalMusicLibrary.Track track) {
        Path audio = track.file();
        String name = audio.getFileName().toString();
        Path lrc = audio.resolveSibling(name.substring(0, name.lastIndexOf(46)) + ".lrc");
        List<LyricLine> lines = List.of();
        try {
            if (Files.isRegularFile(lrc, new LinkOption[0]) && Files.size(lrc) <= 0x100000L) {
                lines = LrcParser.parse(Files.readString(lrc, StandardCharsets.UTF_8));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        current = new Lyrics(track.id(), lines);
    }

    public static Lyrics forTrack(String token) {
        Lyrics value = current;
        return value != null && value.track.equals(token) ? value : null;
    }

    public record Lyrics(String track, List<LyricLine> lines) {
    }
}

