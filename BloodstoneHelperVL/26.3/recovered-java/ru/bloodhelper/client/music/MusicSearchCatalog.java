/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package ru.bloodhelper.client.music;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.invoke.CallSite;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import ru.bloodhelper.client.music.MusicSearchResult;

public final class MusicSearchCatalog {
    private MusicSearchCatalog() {
    }

    public static URI chart(MusicSearchResult.Provider provider) {
        return URI.create(provider == MusicSearchResult.Provider.YANDEX ? "https://api.music.yandex.net/landing3/chart" : "https://charts-spotify-com-service.spotify.com/public/v0/charts");
    }

    public static URI yandexSearch(String query) {
        return URI.create("https://api.music.yandex.net/search?type=track&page=0&text=" + URLEncoder.encode(MusicSearchResult.cleanQuery(query), StandardCharsets.UTF_8));
    }

    public static URI yandexTrack(String id, boolean application) {
        if (id == null || !id.matches("[0-9]{1,20}:[0-9]{1,20}")) {
            throw new IllegalArgumentException("Invalid track id");
        }
        String[] parts = id.split(":");
        return URI.create((application ? "yandexmusic://" : "https://music.yandex.ru/") + "album/" + parts[1] + "/track/" + parts[0]);
    }

    public static URI yandexArtistTracks(MusicSearchResult.Artist artist, int page) {
        if (artist == null || !artist.id().matches("[0-9]{1,20}") || page < 0 || page > 1000) {
            throw new IllegalArgumentException("Invalid artist/page");
        }
        return URI.create("https://api.music.yandex.net/artists/" + artist.id() + "/tracks?page=" + page + "&page-size=20");
    }

    public static String artistCover(String value) {
        if (value == null || !value.matches("avatars\\.yandex\\.net/get-music-content/[a-zA-Z0-9._/-]+/%%")) {
            return "";
        }
        return "https://" + value.replace("%%", "200x200");
    }

    public static String spotifyArtistQuery(MusicSearchResult.Artist artist) {
        return "artist:\"" + MusicSearchResult.cleanQuery(artist.name()).replace('\"', ' ').replace('\\', ' ').strip() + "\"";
    }

    public static MusicSearchResult parseArtistTracks(String body, MusicSearchResult.Artist artist, int page) {
        JsonObject result = JsonParser.parseString((String)body).getAsJsonObject().getAsJsonObject("result");
        if (result == null || !result.has("tracks") || !result.get("tracks").isJsonArray()) {
            throw new IllegalArgumentException("Missing artist tracks");
        }
        List<MusicSearchResult.Track> tracks = MusicSearchCatalog.parseTracks(result.getAsJsonArray("tracks"));
        int total = result.getAsJsonObject("pager").get("total").getAsInt();
        return new MusicSearchResult(MusicSearchResult.Provider.YANDEX, artist.name(), tracks.isEmpty() ? MusicSearchResult.Status.EMPTY : MusicSearchResult.Status.READY, tracks, artist, page, Math.max(0, total));
    }

    public static List<MusicSearchResult.Track> parseYandex(String body) {
        JsonObject root = JsonParser.parseString((String)body).getAsJsonObject();
        if (!root.has("result") || !root.get("result").isJsonObject()) {
            throw new IllegalArgumentException("Missing search result");
        }
        JsonObject result = root.getAsJsonObject("result");
        if (!result.has("tracks")) {
            return List.of();
        }
        JsonArray tracks = result.getAsJsonObject("tracks").getAsJsonArray("results");
        if (tracks == null) {
            throw new IllegalArgumentException("Missing tracks");
        }
        return MusicSearchCatalog.parseTracks(tracks);
    }

    public static List<MusicSearchResult.Track> parseChart(MusicSearchResult.Provider provider, String body) {
        JsonObject root = JsonParser.parseString((String)body).getAsJsonObject();
        if (provider == MusicSearchResult.Provider.YANDEX) {
            JsonArray rows = root.getAsJsonObject("result").getAsJsonObject("chart").getAsJsonArray("tracks");
            JsonArray tracks = new JsonArray();
            for (JsonElement row : rows) {
                if (!row.isJsonObject() || !row.getAsJsonObject().has("track")) continue;
                tracks.add(row.getAsJsonObject().get("track"));
            }
            return MusicSearchCatalog.parseTracks(tracks, 100);
        }
        for (JsonElement item : root.getAsJsonArray("chartEntryViewResponses")) {
            JsonObject chart = item.getAsJsonObject();
            if (!"REGIONAL_GLOBAL_WEEKLY".equals(MusicSearchCatalog.string(chart.getAsJsonObject("displayChart").getAsJsonObject("chartMetadata"), "alias"))) continue;
            LinkedHashMap<String, MusicSearchResult.Track> tracks = new LinkedHashMap<String, MusicSearchResult.Track>();
            for (JsonElement entry : chart.getAsJsonArray("entries")) {
                try {
                    JsonObject metadata = entry.getAsJsonObject().getAsJsonObject("trackMetadata");
                    String id = MusicSearchCatalog.string(metadata, "trackUri");
                    String title = MusicSearchCatalog.string(metadata, "trackName");
                    if (!id.matches("spotify:track:[A-Za-z0-9]{22}") || title.isBlank()) continue;
                    ArrayList<MusicSearchResult.Artist> artists = new ArrayList<MusicSearchResult.Artist>();
                    for (JsonElement person : metadata.getAsJsonArray("artists")) {
                        JsonObject a = person.getAsJsonObject();
                        String uri = MusicSearchCatalog.string(a, "spotifyUri");
                        String name = MusicSearchCatalog.string(a, "name");
                        if (name.isBlank()) continue;
                        artists.add(new MusicSearchResult.Artist(uri.matches("spotify:artist:[A-Za-z0-9]{22}") ? uri : "", name, uri.matches("spotify:artist:[A-Za-z0-9]{22}") ? uri : ""));
                    }
                    if (artists.isEmpty()) continue;
                    String names = String.join((CharSequence)", ", artists.stream().map(MusicSearchResult.Artist::name).toList());
                    tracks.putIfAbsent(id, new MusicSearchResult.Track(id, title, names, 0L, true, artists, MusicSearchCatalog.imageUrl(MusicSearchCatalog.string(metadata, "displayImageUri"))));
                    if (tracks.size() != 50) continue;
                    break;
                }
                catch (RuntimeException runtimeException) {
                }
            }
            return List.copyOf(tracks.values());
        }
        throw new IllegalArgumentException("Missing Spotify song chart");
    }

    public static String imageUrl(String value) {
        if (value == null) {
            return "";
        }
        return value.matches("https://avatars\\.yandex\\.net/get-music-content/[a-zA-Z0-9._/-]+/200x200") || value.matches("https://i\\.scdn\\.co/image/[a-f0-9]{40}") ? value : "";
    }

    private static List<MusicSearchResult.Track> parseTracks(JsonArray tracks) {
        return MusicSearchCatalog.parseTracks(tracks, 20);
    }

    private static List<MusicSearchResult.Track> parseTracks(JsonArray tracks, int limit) {
        LinkedHashMap<CallSite, MusicSearchResult.Track> found = new LinkedHashMap<CallSite, MusicSearchResult.Track>();
        for (JsonElement element : tracks) {
            if (found.size() >= limit) break;
            try {
                JsonObject track = element.getAsJsonObject();
                String id = MusicSearchCatalog.string(track, "id");
                Object title = MusicSearchCatalog.string(track, "title");
                String version = MusicSearchCatalog.string(track, "version");
                JsonArray albums = track.getAsJsonArray("albums");
                JsonArray artists = track.getAsJsonArray("artists");
                if (((String)title).isBlank() || albums == null || albums.isEmpty() || artists == null) continue;
                String key = id + ":" + MusicSearchCatalog.string(albums.get(0).getAsJsonObject(), "id");
                MusicSearchCatalog.yandexTrack(key, false);
                ArrayList<String> names = new ArrayList<String>();
                ArrayList<MusicSearchResult.Artist> people = new ArrayList<MusicSearchResult.Artist>();
                for (JsonElement artist : artists) {
                    JsonObject person = artist.getAsJsonObject();
                    String name = MusicSearchCatalog.string(person, "name");
                    String artistId = MusicSearchCatalog.string(person, "id");
                    if (!name.isBlank()) {
                        names.add(name);
                        if (artistId.matches("[0-9]{1,20}")) {
                            String cover = person.has("cover") && person.get("cover").isJsonObject() ? MusicSearchCatalog.string(person.getAsJsonObject("cover"), "uri") : "";
                            people.add(new MusicSearchResult.Artist(artistId, name, MusicSearchCatalog.artistCover(cover)));
                        }
                    }
                    if (names.size() < 12) continue;
                    break;
                }
                if (names.isEmpty()) continue;
                if (!version.isBlank()) {
                    title = (String)title + " (" + version + ")";
                }
                long duration = track.has("durationMs") ? Math.clamp(track.get("durationMs").getAsLong(), 0L, 86400000L) : 0L;
                boolean available = track.has("available") && track.get("available").getAsBoolean();
                String cover = MusicSearchCatalog.artistCover(MusicSearchCatalog.string(track, "coverUri"));
                if (cover.isEmpty()) {
                    cover = MusicSearchCatalog.artistCover(MusicSearchCatalog.string(albums.get(0).getAsJsonObject(), "coverUri"));
                }
                found.putIfAbsent((CallSite)((Object)key), new MusicSearchResult.Track(key, MusicSearchCatalog.text((String)title), MusicSearchCatalog.text(String.join((CharSequence)", ", names)), duration, available, people, cover));
            }
            catch (RuntimeException runtimeException) {}
        }
        return List.copyOf(found.values());
    }

    static String string(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? MusicSearchCatalog.text(object.get(key).getAsString()) : "";
    }

    static String text(String value) {
        return value.replaceAll("[\\p{Cntrl}]", " ").strip().codePoints().limit(300L).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}

