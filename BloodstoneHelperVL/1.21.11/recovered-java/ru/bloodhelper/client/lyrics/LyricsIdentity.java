/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.lyrics;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class LyricsIdentity {
    private static final Pattern GUEST = Pattern.compile("(?iu)\\s*(?:\\(|\\[)\\s*(?:feat\\.?|ft\\.?|featuring|\u043f\\.\u0443\\.)\\s+([^\\[\\]()]+)(?:\\)|\\])\\s*$");

    LyricsIdentity() {
    }

    static Track track(String title, String artist) {
        Matcher guest = GUEST.matcher(title);
        if (!guest.find()) {
            return new Track(title, artist);
        }
        String credit = guest.group(1).strip();
        String combined = artist.isBlank() ? credit : (LyricsIdentity.artists(artist).containsAll(LyricsIdentity.artists(credit)) ? artist : artist + "; " + credit);
        return new Track(title.substring(0, guest.start()).strip(), combined);
    }

    static boolean matches(String title, String artist, String candidateTitle, String candidateArtist) {
        Track expected = LyricsIdentity.track(title, artist);
        Track candidate = LyricsIdentity.track(candidateTitle, candidateArtist);
        if (!LyricsIdentity.normalize(expected.title()).equals(LyricsIdentity.normalize(candidate.title())) || candidateArtist.isBlank()) {
            return false;
        }
        if (artist.isBlank()) {
            return expected.artist().isBlank() || LyricsIdentity.artists(candidate.artist()).containsAll(LyricsIdentity.artists(expected.artist()));
        }
        return LyricsIdentity.normalize(expected.artist()).equals(LyricsIdentity.normalize(candidate.artist())) || LyricsIdentity.artists(expected.artist()).equals(LyricsIdentity.artists(candidate.artist()));
    }

    private static Set<String> artists(String value) {
        return Arrays.stream(value.split("(?iu)\\s+(?:feat\\.?|ft\\.?|featuring|x|\u00d7|\u043f\\.\u0443\\.)\\s+|[,;&\u3001/]+")).map(LyricsIdentity::normalize).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFC).toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{M}\\p{N}]+", " ").strip();
    }

    record Track(String title, String artist) {
    }
}

