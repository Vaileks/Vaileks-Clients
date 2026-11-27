/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11909
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_11909;
import net.minecraft.class_332;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.MusicWidget;
import ru.bloodhelper.client.hud.VolumeDrag;
import ru.bloodhelper.client.lyrics.LrcParser;
import ru.bloodhelper.client.lyrics.LyricHighlight;
import ru.bloodhelper.client.lyrics.LyricLine;
import ru.bloodhelper.client.lyrics.LyricTransition;
import ru.bloodhelper.client.lyrics.LyricsService;
import ru.bloodhelper.client.modules.LocalMusicScreen;
import ru.bloodhelper.client.modules.MediaSourceScreen;
import ru.bloodhelper.client.modules.ModuleDetailScreen;
import ru.bloodhelper.client.modules.MusicSearchScreen;
import ru.bloodhelper.client.music.MediaFavoriteState;
import ru.bloodhelper.client.music.MediaService;
import ru.bloodhelper.client.music.MediaSnapshot;
import ru.bloodhelper.client.music.MediaSources;
import ru.bloodhelper.client.music.PlayerSeekState;

public final class MusicPlayerScreen
extends ModuleDetailScreen {
    private final PlayerSeekState seek = new PlayerSeekState();
    private final VolumeDrag volume = new VolumeDrag();
    private Rect seekBar;
    private Rect volumeBar;
    private Rect lyricsArea;
    private List<LyricLine> cachedLines = List.of();
    private List<Cue> cues = List.of();
    private int cachedWidth;
    private int lyricsHeight;
    private double lyricScroll;
    private long frameNanos;
    private long manualScrollUntil;
    private String renderedTrack = "";
    private String renderedVolume = "";
    private String previousTrack = "";
    private boolean controlsAvailable;
    private String notice = "";
    private long noticeUntil;
    private long openedAt;
    private final LyricTransition lyricTransition = new LyricTransition();

    public MusicPlayerScreen(class_437 parent) {
        super(parent, HudStyle.tr("\u0421\u0435\u0439\u0447\u0430\u0441 \u0438\u0433\u0440\u0430\u0435\u0442", "Now playing"));
    }

    @Override
    protected double appearance(long now) {
        if (this.openedAt == 0L) {
            this.openedAt = now;
        }
        if (!BloodHelperClient.config().animations) {
            return 1.0;
        }
        double t = Math.clamp((double)(now - this.openedAt) / 2.6E8, 0.0, 1.0);
        return 1.0 - Math.pow(1.0 - t, 3.0);
    }

    @Override
    protected int preferredPanelWidth() {
        return 850;
    }

    @Override
    protected int preferredPanelHeight() {
        return 480;
    }

    public void method_25432() {
        this.seek.cancel();
        this.volume.cancel();
        super.method_25432();
    }

    @Override
    protected void renderPanel(class_332 g, int mx, int my, float delta) {
        String identity;
        this.frame(g, HudStyle.tr("\u0421\u0435\u0439\u0447\u0430\u0441 \u0438\u0433\u0440\u0430\u0435\u0442", "Now playing"), mx, my);
        MediaService service = BloodHelperClient.media();
        MediaSnapshot sample = service.snapshot();
        long now = System.nanoTime();
        long position = this.seek.observe(sample, now);
        this.volume.retain(sample.volumeTargetToken());
        if (this.volume.active()) {
            this.sendVolume(false);
        }
        if (!(identity = sample.trackToken() + "\u0000" + sample.title() + "\u0000" + sample.artist()).equals(this.previousTrack)) {
            this.lyricScroll = 0.0;
            this.manualScrollUntil = 0L;
            this.previousTrack = identity;
            this.noticeUntil = 0L;
        }
        this.renderedTrack = sample.trackToken();
        this.renderedVolume = sample.volumeTargetToken();
        this.controlsAvailable = sample.available();
        int column = Math.max(150, Math.min(280, this.panelWidth * 2 / 5));
        int lx = this.left + 18;
        int lw = column - 32;
        int rx = this.left + column + 10;
        int rw = this.panelWidth - column - 28;
        String selected = BloodHelperClient.config().music.preferredSource;
        String app = MediaSources.labelFor(selected.isEmpty() ? service.sources().activeId() : selected);
        this.button(g, lx, this.bodyTop, lw, app.isEmpty() ? HudStyle.tr("\u0412\u044b\u0431\u0440\u0430\u0442\u044c \u043f\u043b\u0435\u0435\u0440", "Choose player") : app, mx, my, () -> this.field_22787.method_1507((class_437)new MediaSourceScreen(this)));
        UiIcons.draw(g, "chevron", lx + lw - 17, this.bodyTop + 4, 13, -7105633);
        int coverSize = Math.max(36, Math.min(lw, this.bodyBottom - this.bodyTop - 189));
        MusicWidget.renderArtwork(g, lx + (lw - coverSize) / 2, this.bodyTop + 34, coverSize, sample);
        int titleY = this.bodyTop + 44 + coverSize;
        CustomFont.medium().draw(g, CustomFont.medium().ellipsize(sample.available() ? sample.title() : HudStyle.tr("\u041d\u0435\u0442 \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0433\u043e \u0442\u0440\u0435\u043a\u0430", "No active track"), 15.0f, lw), lx, titleY, 15.0f, -1381908);
        CustomFont.regular().draw(g, CustomFont.regular().ellipsize(sample.available() ? sample.artist() : HudStyle.tr("\u0417\u0430\u043f\u0443\u0441\u0442\u0438 \u043c\u0443\u0437\u044b\u043a\u0443 \u0432 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0438", "Start music in your application"), 11.0f, lw), lx, titleY + 23, 11.0f, -7105633);
        int progressY = this.bodyBottom - 96;
        this.seekBar = new Rect(lx, progressY, lw, 14);
        boolean canSeek = sample.available() && sample.canSeek();
        double progress = sample.durationMs() > 0L ? Math.clamp((double)position / (double)sample.durationMs(), 0.0, 1.0) : 0.0;
        MusicPlayerScreen.slider(g, this.seekBar, progress, canSeek);
        this.text(g, sample.available() ? MusicPlayerScreen.time(position) : "\u2014:\u2014", lx, progressY + 19, -7105633);
        String duration = sample.available() ? MusicPlayerScreen.time(sample.durationMs()) : "\u2014:\u2014";
        this.text(g, duration, lx + lw - (int)CustomFont.regular().width(duration, 11.0f), progressY + 19, -7105633);
        int cy = this.bodyBottom - 61;
        int center = lx + lw / 2;
        this.iconButton(g, center - 75, cy, 36, "previous", this.controlsAvailable, false, mx, my, () -> this.transport("previous"));
        this.iconButton(g, center - 28, cy - 3, 48, sample.playing() ? "pause" : "play", this.controlsAvailable, true, mx, my, () -> this.transport("toggle"));
        this.iconButton(g, center + 31, cy, 36, "next", this.controlsAvailable, false, mx, my, () -> this.transport("next"));
        MediaFavoriteState favorite = sample.favorite();
        boolean pending = service.favoritePending();
        this.iconButton(g, rx, this.bodyTop, 28, "heart", favorite.available() && !pending, favorite.saved(), mx, my, () -> {
            if (this.renderedTrack.equals(service.snapshot().trackToken()) && favorite.canAdd()) {
                service.addFavorite(this.renderedTrack);
            }
        });
        String favoriteText = favorite.saved() ? HudStyle.tr("\u0412 \u0438\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u043c", "In favorites") : (pending ? HudStyle.tr("\u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0435\u043c\u2026", "Adding\u2026") : (favorite.status().equals("not_confirmed") ? HudStyle.tr("\u0421\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u0435 \u043d\u0435 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u043e", "Save not confirmed") : (favorite.canAdd() ? HudStyle.tr("\u0412 \u0438\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0435", "Add to favorites") : (favorite.status().equals("unsupported") ? HudStyle.tr("\u041f\u043b\u0435\u0435\u0440 \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442 \u0438\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0435", "Player favorites unsupported") : HudStyle.tr("\u041e\u0442\u043a\u0440\u043e\u0439 \u043e\u043a\u043d\u043e \u043f\u043b\u0435\u0435\u0440\u0430 \u0434\u043b\u044f \u0438\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0433\u043e", "Open the player window for favorites")))));
        this.text(g, CustomFont.regular().ellipsize(favoriteText, 11.0f, Math.max(1, rw - 250)), rx + 36, this.bodyTop + 8, -7105633);
        this.button(g, rx + rw - 206, this.bodyTop + 3, 98, HudStyle.tr("\u041c\u043e\u044f \u043c\u0443\u0437\u044b\u043a\u0430", "My music"), mx, my, () -> this.field_22787.method_1507((class_437)new LocalMusicScreen(this)));
        this.button(g, rx + rw - 102, this.bodyTop + 3, 102, HudStyle.tr("\u041f\u043e\u0438\u0441\u043a \u0442\u0440\u0435\u043a\u043e\u0432", "Search tracks"), mx, my, () -> this.field_22787.method_1507((class_437)new MusicSearchScreen(this)));
        UiIcons.draw(g, "volume", lx, this.bodyBottom - 17, 14, sample.canControlVolume() ? -1381908 : -7105633);
        this.volumeBar = new Rect(lx + 22, this.bodyBottom - 18, Math.max(1, lw - 59), 14);
        double level = this.volume.active() ? this.volume.value() : sample.volume();
        MusicPlayerScreen.slider(g, this.volumeBar, Double.isFinite(level) ? level : 0.0, sample.canControlVolume());
        String percentage = sample.canControlVolume() ? Math.round(level * 100.0) + "%" : "\u2014";
        this.text(g, percentage, lx + lw - 32, this.bodyBottom - 16, -7105633);
        this.lyricsArea = new Rect(rx, this.bodyTop + 44, rw, Math.max(1, this.bodyBottom - this.bodyTop - 77));
        this.drawLyrics(g, sample, position, mx, my, now);
        this.button(g, rx, this.bodyBottom - 22, Math.min(180, rw), BloodHelperClient.lyrics().hasSyncedLyrics() ? HudStyle.tr("\u041a \u0442\u0435\u043a\u0443\u0449\u0435\u0439 \u0441\u0442\u0440\u043e\u043a\u0435", "Follow current line") : HudStyle.tr("\u041a \u043d\u0430\u0447\u0430\u043b\u0443 \u0442\u0435\u043a\u0441\u0442\u0430", "Top of lyrics"), mx, my, () -> {
            this.manualScrollUntil = 0L;
            if (!BloodHelperClient.lyrics().hasSyncedLyrics()) {
                this.lyricScroll = 0.0;
            }
        });
        this.footer(g, now < this.noticeUntil ? this.notice : (BloodHelperClient.lyrics().hasSyncedLyrics() ? HudStyle.tr("\u041d\u0430\u0436\u043c\u0438 \u043d\u0430 \u0441\u0442\u0440\u043e\u043a\u0443 \u0442\u0435\u043a\u0441\u0442\u0430, \u0447\u0442\u043e\u0431\u044b \u043f\u0435\u0440\u0435\u0439\u0442\u0438 \u043a \u043d\u0435\u0439", "Click a lyric line to jump to it") : (BloodHelperClient.lyrics().displayLines().isEmpty() ? HudStyle.tr("\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a \u0442\u0435\u043a\u0441\u0442\u0430 \u043c\u043e\u0436\u043d\u043e \u0432\u044b\u0431\u0440\u0430\u0442\u044c \u0432 \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445 \u043e\u0441\u0442\u0440\u043e\u0432\u043a\u0430", "Choose a lyrics provider in the Island settings") : HudStyle.tr("\u0422\u0435\u043a\u0441\u0442 \u0431\u0435\u0437 \u0442\u0430\u0439\u043c\u043a\u043e\u0434\u043e\u0432 \u00b7 \u043f\u0440\u043e\u043a\u0440\u0443\u0447\u0438\u0432\u0430\u0439 \u043a\u043e\u043b\u0451\u0441\u0438\u043a\u043e\u043c", "Plain lyrics \u00b7 scroll with the mouse wheel"))));
        this.frameNanos = now;
    }

    private void transport(String command) {
        if (this.controlsAvailable && this.renderedTrack.equals(BloodHelperClient.media().snapshot().trackToken())) {
            BloodHelperClient.musicCommand(command);
        }
    }

    private void iconButton(class_332 g, int x, int y, int w, String icon, boolean enabled, boolean accent, int mx, int my, Runnable callback) {
        int h = w == 48 ? 34 : 28;
        ModuleDetailScreen.Action hit = new ModuleDetailScreen.Action(x, y, w, h, () -> {
            if (enabled) {
                callback.run();
            }
        });
        this.actions.add(hit);
        int fill = accent ? UiDraw.alpha(BloodHelperClient.config().accent, 70) : ThemeColors.surface(hit.contains(mx, my) ? -12301738 : -14078408);
        UiDraw.rounded(g, x, y, w, h, 9, fill);
        UiIcons.draw(g, icon, x + (w - 18) / 2, y + (h - 18) / 2, 18, enabled ? -1381908 : -7105633);
    }

    private static void slider(class_332 g, Rect rect, double ratio, boolean enabled) {
        UiDraw.rounded(g, rect.x, rect.y + 5, rect.width, 3, 1, ThemeColors.surface(-12038825));
        int amount = (int)Math.round((double)rect.width * Math.clamp(ratio, 0.0, 1.0));
        if (amount > 0) {
            UiDraw.rounded(g, rect.x, rect.y + 5, amount, 3, 1, enabled ? ThemeColors.accent() : -7105633);
        }
        if (enabled) {
            UiDraw.rounded(g, rect.x + Math.clamp((long)(amount - 3), 0, Math.max(0, rect.width - 6)), rect.y + 3, 7, 7, 3, ThemeColors.text(-1381908));
        }
    }

    private void drawLyrics(class_332 g, MediaSnapshot sample, long position, int mx, int my, long now) {
        boolean timed;
        List<LyricLine> lines;
        LyricsService lyrics = BloodHelperClient.lyrics();
        ClientConfig.MusicSettings settings = BloodHelperClient.config().music;
        List<LyricLine> list = lines = sample.available() ? lyrics.displayLines() : List.of();
        if (lines != this.cachedLines || this.cachedWidth != this.lyricsArea.width) {
            this.cachedLines = lines;
            this.cachedWidth = this.lyricsArea.width;
            ArrayList<Cue> next = new ArrayList<Cue>();
            int y = 0;
            for (int i = 0; i < lines.size(); ++i) {
                if (lines.get(i).text().isBlank()) continue;
                List<String> wrapped = CustomFont.medium().wrap(lines.get(i).text(), 15.0f, Math.max(1, this.lyricsArea.width - 20));
                int h = Math.max(1, wrapped.size()) * 20 + 18;
                next.add(new Cue(i, y, h, wrapped));
                y += h;
            }
            this.cues = List.copyOf(next);
            this.lyricsHeight = y;
        }
        int index = (timed = lyrics.hasSyncedLyrics()) ? LrcParser.indexAt(lines, position + (long)lyrics.offsetMs(settings)) : -1;
        this.lyricTransition.update(lyrics.trackKey(), index, position, now, BloodHelperClient.config().animations);
        double max = Math.max(0, this.lyricsHeight - this.lyricsArea.height);
        if (timed && now >= this.manualScrollUntil && !this.cues.isEmpty()) {
            Cue active = this.cues.stream().filter(c -> c.index >= Math.max(0, index)).findFirst().orElse(this.cues.getLast());
            double target = Math.clamp((double)active.y - (double)this.lyricsArea.height * 0.35, 0.0, max);
            double dt = this.frameNanos == 0L ? 1.0 : Math.clamp((double)(now - this.frameNanos) / 1.0E9, 0.0, 0.1);
            this.lyricScroll += (target - this.lyricScroll) * (BloodHelperClient.config().animations ? 1.0 - Math.exp(-dt * 9.0) : 1.0);
        }
        this.lyricScroll = Math.clamp(this.lyricScroll, 0.0, max);
        g.method_44379(this.lyricsArea.x, this.lyricsArea.y, this.lyricsArea.x + this.lyricsArea.width, this.lyricsArea.y + this.lyricsArea.height);
        if (this.cues.isEmpty()) {
            String status = switch (lyrics.status()) {
                case LyricsService.Status.LOADING -> HudStyle.tr("\u0418\u0449\u0435\u043c \u0442\u0435\u043a\u0441\u0442 \u043f\u0435\u0441\u043d\u0438\u2026", "Looking for lyrics\u2026");
                case LyricsService.Status.ERROR -> HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0442\u0435\u043a\u0441\u0442", "Could not load lyrics");
                default -> HudStyle.tr("\u0422\u0435\u043a\u0441\u0442 \u044d\u0442\u043e\u0439 \u043f\u0435\u0441\u043d\u0438 \u043f\u043e\u043a\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d", "Lyrics for this song have not been found");
            };
            int y = this.lyricsArea.y + 20;
            for (String line : CustomFont.regular().wrap(status, 13.0f, Math.max(1, this.lyricsArea.width - 20))) {
                CustomFont.regular().draw(g, line, this.lyricsArea.x + 8, y, 13.0f, -7105633);
                y += 20;
            }
        }
        for (Cue cue : this.cues) {
            boolean hover;
            int y = this.lyricsArea.y + cue.y - (int)Math.round(this.lyricScroll);
            if (y + cue.height <= this.lyricsArea.y || y >= this.lyricsArea.y + this.lyricsArea.height) continue;
            boolean active = cue.index == index;
            double emphasis = this.lyricTransition.weight(cue.index, now);
            boolean bl = hover = mx >= this.lyricsArea.x && mx < this.lyricsArea.x + this.lyricsArea.width && my >= Math.max(y, this.lyricsArea.y) && my < Math.min(y + cue.height, this.lyricsArea.y + this.lyricsArea.height);
            if (emphasis > 0.01 || hover) {
                UiDraw.rounded(g, this.lyricsArea.x, y, this.lyricsArea.width - 4, cue.height - 4, 7, UiDraw.alpha(BloodHelperClient.config().accent, (int)Math.max(32.0 * emphasis, hover ? 16.0 : 0.0)));
            }
            int textOffset = 0;
            for (String line : cue.lines) {
                LyricLine source = lines.get(cue.index);
                int start = source.text().indexOf(line, textOffset);
                if (start < 0) {
                    start = textOffset;
                }
                CustomFont.medium().draw(g, line, this.lyricsArea.x + 8, y + 6, 15.0f, UiDraw.blend(-7105633, -1381908, emphasis * (source.wordTimed() ? 0.45 : 1.0)));
                if (active && source.wordTimed()) {
                    LyricHighlight.draw(g, CustomFont.medium(), source, line, start, this.lyricsArea.x + 8, y + 6, 15.0f, UiDraw.blend(BloodHelperClient.config().accent, -1381908, 0.65), position + (long)lyrics.offsetMs(settings));
                }
                textOffset = start + line.length();
                y += 20;
            }
        }
        g.method_44380();
    }

    @Override
    protected boolean panelMouseClicked(class_11909 event, boolean doubled) {
        if (event.method_74245() != 0) {
            return super.panelMouseClicked(event, doubled);
        }
        MediaSnapshot sample = BloodHelperClient.media().snapshot();
        if (this.seekBar != null && this.seekBar.contains(event.comp_4798(), event.comp_4799())) {
            if (this.renderedTrack.equals(sample.trackToken())) {
                this.seek.begin(sample, this.seekBar.ratio(event.comp_4798()));
            }
            return true;
        }
        if (this.volumeBar != null && this.volumeBar.contains(event.comp_4798(), event.comp_4799())) {
            if (sample.canControlVolume() && this.renderedVolume.equals(sample.volumeTargetToken())) {
                this.volume.begin(this.renderedVolume, this.volumeBar.ratio(event.comp_4798()));
                this.sendVolume(false);
            }
            return true;
        }
        if (this.lyricsArea != null && this.lyricsArea.contains(event.comp_4798(), event.comp_4799())) {
            if (BloodHelperClient.lyrics().hasSyncedLyrics() && sample.canSeek() && this.renderedTrack.equals(sample.trackToken())) {
                double y = event.comp_4799() - (double)this.lyricsArea.y + this.lyricScroll;
                for (Cue cue : this.cues) {
                    if (!(y >= (double)cue.y) || !(y < (double)(cue.y + cue.height)) || cue.index >= this.cachedLines.size()) continue;
                    long target = Math.clamp(this.cachedLines.get(cue.index).timestampMs() - (long)BloodHelperClient.lyrics().offsetMs(BloodHelperClient.config().music), 0L, sample.durationMs());
                    if (this.seek.begin(sample, (double)target / (double)Math.max(1L, sample.durationMs()))) {
                        this.commitSeek();
                    }
                    this.manualScrollUntil = 0L;
                    break;
                }
            }
            return true;
        }
        return super.panelMouseClicked(event, doubled);
    }

    @Override
    protected boolean panelMouseDragged(class_11909 event, double dx, double dy) {
        if (event.method_74245() == 0 && this.seek.dragging()) {
            this.seek.move(this.seekBar.ratio(event.comp_4798()));
            return true;
        }
        if (event.method_74245() == 0 && this.volume.active()) {
            this.volume.move(this.volumeBar.ratio(event.comp_4798()));
            this.sendVolume(false);
            return true;
        }
        return super.panelMouseDragged(event, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(class_11909 event) {
        if (event.method_74245() == 0 && this.seek.dragging()) {
            this.seek.move(this.seekBar.ratio(event.comp_4798()));
            this.commitSeek();
            return true;
        }
        if (event.method_74245() == 0 && this.volume.active()) {
            this.volume.move(this.volumeBar.ratio(event.comp_4798()));
            this.sendVolume(true);
            this.volume.cancel();
            return true;
        }
        return super.panelMouseReleased(event);
    }

    @Override
    protected boolean panelMouseScrolled(double x, double y, double horizontal, double vertical) {
        if (this.lyricsArea != null && this.lyricsArea.contains(x, y)) {
            this.manualScrollUntil = System.nanoTime() + 8000000000L;
            this.lyricScroll = Math.clamp(this.lyricScroll - vertical * 36.0, 0.0, (double)Math.max(0, this.lyricsHeight - this.lyricsArea.height));
            return true;
        }
        return false;
    }

    private void commitSeek() {
        long request = BloodHelperClient.media().seekTo(this.seek.target(), this.seek.token());
        this.seek.submitted(request, System.nanoTime());
        if (request == 0L) {
            this.notice = HudStyle.tr("\u041f\u043b\u0435\u0435\u0440 \u043d\u0435 \u043f\u0440\u0438\u043d\u044f\u043b \u043f\u0435\u0440\u0435\u043c\u043e\u0442\u043a\u0443", "Player could not seek");
            this.noticeUntil = System.nanoTime() + 2000000000L;
        }
    }

    private void sendVolume(boolean release) {
        if (!this.volume.retain(BloodHelperClient.media().snapshot().volumeTargetToken())) {
            return;
        }
        double amount = this.volume.request(System.nanoTime(), release);
        if (Double.isFinite(amount)) {
            BloodHelperClient.media().setVolume(amount, this.volume.target());
        }
    }

    private static String time(long ms) {
        long seconds = Math.max(0L, ms / 1000L);
        return seconds / 60L + ":" + String.format(Locale.ROOT, "%02d", seconds % 60L);
    }

    private record Rect(int x, int y, int width, int height) {
        boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.width) && my >= (double)this.y && my < (double)(this.y + this.height);
        }

        double ratio(double mx) {
            return Math.clamp((mx - (double)this.x) / (double)Math.max(1, this.width), 0.0, 1.0);
        }
    }

    private record Cue(int index, int y, int height, List<String> lines) {
    }
}

