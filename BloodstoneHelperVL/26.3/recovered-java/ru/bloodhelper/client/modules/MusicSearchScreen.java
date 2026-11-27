/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 */
package ru.bloodhelper.client.modules;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.GuiTextField;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.HudMotion;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.ModuleDetailScreen;
import ru.bloodhelper.client.modules.MusicArtistArtwork;
import ru.bloodhelper.client.music.MusicArtworkCache;
import ru.bloodhelper.client.music.MusicSearchResult;
import ru.bloodhelper.client.music.MusicSearchService;

public final class MusicSearchScreen
extends ModuleDetailScreen {
    private final GuiTextField input = new GuiTextField(0, 0, 100, 26, "");
    private MusicSearchService service = new MusicSearchService();
    private MusicSearchResult retained;
    private MusicSearchResult displayed;
    private byte[] retainedCover;
    private MusicSearchResult.Provider provider;
    private final MusicSearchResult.Artist artist;
    private final MusicArtistArtwork portrait = new MusicArtistArtwork();
    private MusicArtworkCache images = new MusicArtworkCache();
    private final Map<String, MusicArtistArtwork> thumbnails = new LinkedHashMap<String, MusicArtistArtwork>();
    private final HudMotion motion = new HudMotion();
    private long openedAt;
    private long resultsAt;
    private double paintedScroll;
    private float rowOpacity = 1.0f;
    private boolean rowInteractive = true;
    private List<MusicSearchResult.Artist> chooser = List.of();
    private int searchScroll;

    public MusicSearchScreen(Screen parent) {
        this(parent, null, null);
    }

    public MusicSearchScreen(Screen parent, MusicSearchResult.Provider selectedProvider, MusicSearchResult.Artist artist) {
        super(parent, HudStyle.tr("\u041f\u043e\u0438\u0441\u043a \u043c\u0443\u0437\u044b\u043a\u0438", "Music search"));
        this.artist = artist;
        String selected = BloodHelperClient.config().music.preferredSource;
        this.provider = selectedProvider == null ? MusicSearchResult.providerFor(selected.isEmpty() ? BloodHelperClient.media().sources().activeId() : selected) : selectedProvider;
        this.input.setMaxLength(120);
        this.input.setTextSize(12.0f);
        this.input.setFocused(true);
        this.input.setResponder(value -> {
            this.resetScroll();
            this.chooser = List.of();
            this.service.reset(this.provider, (String)value);
            if (MusicSearchResult.cleanQuery(value).isEmpty()) {
                this.service.search(this.provider, "");
            }
        });
        if (artist != null) {
            this.service.searchArtist(this.provider, artist, 0);
        } else {
            this.service.search(this.provider, "");
        }
    }

    protected void init() {
        super.init();
        this.openedAt = System.nanoTime();
        if (this.service == null) {
            this.service = new MusicSearchService(this.retained, this.retainedCover);
            this.images = new MusicArtworkCache();
            if (this.service.result().status() == MusicSearchResult.Status.IDLE) {
                this.submit();
            }
        }
    }

    @Override
    protected double appearance(long now) {
        return BloodHelperClient.config().animations ? 1.0 - Math.pow(1.0 - Math.clamp((double)(now - this.openedAt) / 2.4E8, 0.0, 1.0), 3.0) : 1.0;
    }

    @Override
    protected int preferredPanelWidth() {
        return 700;
    }

    @Override
    protected int preferredPanelHeight() {
        return 470;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void renderPanel(GuiGraphicsExtractor g, int mx, int my, float delta) {
        this.rowOpacity = 1.0f;
        this.rowInteractive = true;
        this.frame(g, this.artist == null ? HudStyle.tr("\u041f\u043e\u0438\u0441\u043a \u043c\u0443\u0437\u044b\u043a\u0438", "Music search") : HudStyle.tr("\u0418\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c", "Artist"), mx, my);
        int x = this.left + 16;
        int available = Math.max(1, this.panelWidth - 32);
        int tab = Math.max(1, (available - 8) / 2);
        MusicSearchResult current = this.service.result();
        if (this.artist != null) {
            this.portrait.draw(g, this.service.artwork() != null ? this.service.artwork() : this.images.get(this.artist.cover()), this.artist.name(), x, this.bodyTop, 78);
            CustomFont.medium().draw(g, CustomFont.medium().ellipsize(this.artist.name(), 22.0f, Math.max(1, available - 108)), x + 94, this.bodyTop + 9, 22.0f, -1381908);
            this.text(g, this.provider == MusicSearchResult.Provider.YANDEX ? HudStyle.tr("\u042f\u043d\u0434\u0435\u043a\u0441 \u041c\u0443\u0437\u044b\u043a\u0430", "Yandex Music") : "Spotify", x + 94, this.bodyTop + 41, -7105633);
            this.text(g, (String)(current.total() > 0 ? (this.provider == MusicSearchResult.Provider.YANDEX ? HudStyle.tr("\u041f\u0435\u0441\u0435\u043d: ", "Tracks: ") : HudStyle.tr("\u041d\u0430\u0439\u0434\u0435\u043d\u043e: ", "Found: ")) + current.total() : HudStyle.tr("\u041f\u0435\u0441\u043d\u0438 \u0438\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044f", "Artist's songs")), x + 94, this.bodyTop + 60, -7105633);
            this.button(g, x + available - 90, this.bodyTop + 51, 90, HudStyle.tr("\u041e\u0431\u043d\u043e\u0432\u0438\u0442\u044c", "Refresh"), mx, my, this::submit);
            this.bodyTop += 92;
        } else {
            this.button(g, x, this.bodyTop, tab, (this.provider == MusicSearchResult.Provider.YANDEX ? "\u2713  " : "") + HudStyle.tr("\u042f\u043d\u0434\u0435\u043a\u0441 \u041c\u0443\u0437\u044b\u043a\u0430", "Yandex Music"), mx, my, () -> this.choose(MusicSearchResult.Provider.YANDEX));
            this.button(g, x + tab + 8, this.bodyTop, tab, (this.provider == MusicSearchResult.Provider.SPOTIFY ? "\u2713  " : "") + "Spotify", mx, my, () -> this.choose(MusicSearchResult.Provider.SPOTIFY));
            int y = this.bodyTop + 32;
            int actionWidth = Math.min(80, available / 3);
            UiDraw.rounded(g, x, y, Math.max(1, available - actionWidth - 8), 29, 7, ThemeColors.surface(-14211282));
            UiIcons.draw(g, "search", x + 8, y + 7, 15, -7105633);
            this.input.setX(x + 30);
            this.input.setY(y + 1);
            this.input.setWidth(Math.max(1, available - actionWidth - 48));
            this.input.setHeight(27);
            this.input.setHint(HudStyle.tr("\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u0442\u0440\u0435\u043a\u0430 \u0438\u043b\u0438 \u0438\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c", "Track title or artist"));
            this.input.render(g, mx, my, delta);
            this.button(g, x + available - actionWidth, y + 4, actionWidth, HudStyle.tr("\u041d\u0430\u0439\u0442\u0438", "Search"), mx, my, this::submit);
            this.bodyTop = y + 42;
            if (current.chart()) {
                this.text(g, this.provider == MusicSearchResult.Provider.YANDEX ? HudStyle.tr("\u0427\u0430\u0440\u0442 \u042f\u043d\u0434\u0435\u043a\u0441 \u041c\u0443\u0437\u044b\u043a\u0438 \u00b7 \u0422\u043e\u043f-100", "Yandex Music chart \u00b7 Top 100") : HudStyle.tr("Spotify \u00b7 \u041c\u0438\u0440\u043e\u0432\u043e\u0439 \u0447\u0430\u0440\u0442 \u043d\u0435\u0434\u0435\u043b\u0438 \u00b7 \u0422\u043e\u043f-50", "Spotify \u00b7 Global weekly chart \u00b7 Top 50"), x + 4, this.bodyTop, -7105633);
                this.bodyTop += 22;
            }
        }
        if (this.provider == MusicSearchResult.Provider.YANDEX && current.status() == MusicSearchResult.Status.BACKGROUND_SETUP) {
            String explanation = HudStyle.tr("\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u043f\u0435\u0440\u0435\u0437\u0430\u043f\u0443\u0441\u0442\u0438\u0442 \u042f\u043d\u0434\u0435\u043a\u0441 \u041c\u0443\u0437\u044b\u043a\u0443. \u041f\u043e\u0441\u043b\u0435 \u044d\u0442\u043e\u0433\u043e \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435 \u043c\u043e\u0436\u043d\u043e \u043e\u0441\u0442\u0430\u0432\u0438\u0442\u044c \u0441\u0432\u0451\u0440\u043d\u0443\u0442\u044b\u043c.", "Connecting restarts Yandex Music once. Afterward you can leave the app minimized.");
            for (String line : CustomFont.regular().wrap(explanation, 11.0f, Math.max(1, available - 8))) {
                this.text(g, line, x + 4, this.bodyTop, -7105633);
                this.bodyTop += 16;
            }
            this.button(g, x + 4, this.bodyTop + 4, Math.min(180, available - 8), HudStyle.tr("\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u042f\u043d\u0434\u0435\u043a\u0441", "Connect Yandex"), mx, my, () -> this.service.connectYandex(current));
            this.bodyTop += 36;
        }
        if (this.artist != null && current.total() > 20 && this.provider == MusicSearchResult.Provider.YANDEX) {
            int py = this.bodyBottom - 23;
            if (current.page() > 0) {
                this.button(g, x, py, 90, HudStyle.tr("\u041f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0438\u0435", "Previous"), mx, my, () -> this.page(current.page() - 1));
            }
            if ((current.page() + 1) * 20 < current.total()) {
                this.button(g, x + available - 90, py, 90, HudStyle.tr("\u0421\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0435", "Next"), mx, my, () -> this.page(current.page() + 1));
            }
            this.text(g, current.page() + 1 + " / " + (current.total() + 19) / 20, x + available / 2 - 20, py + 5, -7105633);
            this.bodyBottom -= 30;
        }
        if (!this.chooser.isEmpty()) {
            this.button(g, x, this.bodyTop, 100, HudStyle.tr("\u041a \u0440\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442\u0430\u043c", "To results"), mx, my, this::closeChooser);
            this.bodyTop += 36;
            this.scrollbar(g, this.chooser.size() * 32);
            this.paintedScroll = this.motion.value("chooser-scroll", this.scroll, System.nanoTime(), BloodHelperClient.config().animations, 210.0);
            int cy = this.bodyTop - (int)Math.round(this.paintedScroll);
            g.enableScissor(x, this.bodyTop, x + available, this.bodyBottom);
            for (MusicSearchResult.Artist person : this.chooser) {
                if (cy + 21 > this.bodyTop && cy < this.bodyBottom) {
                    this.rowInteractive = cy >= this.bodyTop && cy + 21 <= this.bodyBottom;
                    this.thumbnail(g, person.cover(), person.name(), x, cy - 2, 26);
                    this.button(g, x + 34, cy, available - 34, person.name() + "  \u203a", mx, my, () -> this.preview(person));
                }
                cy += 32;
            }
            g.disableScissor();
            this.rowInteractive = true;
            this.footer(g, HudStyle.tr("\u0412\u044b\u0431\u0435\u0440\u0438 \u0438\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044f, \u0447\u0442\u043e\u0431\u044b \u043f\u043e\u0441\u043c\u043e\u0442\u0440\u0435\u0442\u044c \u0435\u0433\u043e \u043f\u0435\u0441\u043d\u0438", "Choose an artist to see their songs"));
            return;
        }
        long now = System.nanoTime();
        if (current != this.displayed) {
            this.displayed = current;
            this.resultsAt = now;
        }
        this.scrollbar(g, current.tracks().size() * 53);
        this.paintedScroll = this.motion.value("scroll", this.scroll, now, BloodHelperClient.config().animations, 210.0);
        g.enableScissor(x, this.bodyTop, x + available, this.bodyBottom);
        try {
            if (current.tracks().isEmpty()) {
                int lineY = this.bodyTop + 20;
                for (String line : CustomFont.regular().wrap(this.message(current.status()), 12.0f, Math.max(1, available - 24))) {
                    CustomFont.regular().draw(g, line, x + 12, lineY, 12.0f, -7105633);
                    lineY += 19;
                }
            }
            int row = 0;
            for (MusicSearchResult.Track track : current.tracks()) {
                int ry;
                double fade = BloodHelperClient.config().animations ? Math.clamp((double)(now - this.resultsAt - (long)Math.min(row, 5) * 20000000L) / 2.0E8, 0.0, 1.0) : 1.0;
                fade = 1.0 - Math.pow(1.0 - fade, 3.0);
                this.rowOpacity = (float)fade;
                if ((ry = this.bodyTop + row++ * 53 - (int)Math.round(this.paintedScroll) + (int)Math.round((1.0 - fade) * 8.0)) + 48 <= this.bodyTop || ry >= this.bodyBottom) continue;
                this.rowInteractive = ry >= this.bodyTop && ry + 48 <= this.bodyBottom;
                UiDraw.rounded(g, x, ry, available, 48, 7, UiDraw.alpha(ThemeColors.surface(-14408661), (int)(255.0 * fade)));
                int artX = x + (current.chart() ? 31 : 7);
                int labelX = artX + 44;
                if (current.chart()) {
                    this.text(g, String.valueOf(row), x + 8, ry + 18, -7105633);
                }
                this.thumbnail(g, track.cover(), track.title(), artX, ry + 5, 38);
                int labelWidth = Math.max(1, available - 145 - (labelX - x));
                CustomFont.medium().draw(g, CustomFont.medium().ellipsize(track.title(), 12.0f, labelWidth), labelX, ry + 8, 12.0f, UiDraw.alpha(-1381908, (int)(255.0 * fade)));
                boolean canPreview = this.artist == null && !track.artists().isEmpty();
                ModuleDetailScreen.Action artistArea = new ModuleDetailScreen.Action(labelX, ry + 25, labelWidth, 20, () -> {
                    if (current != this.service.result()) {
                        return;
                    }
                    if (track.artists().size() == 1) {
                        this.preview(track.artists().getFirst());
                    } else {
                        this.searchScroll = this.scroll;
                        this.chooser = track.artists();
                        this.resetScroll();
                    }
                });
                double hovered = this.motion.value("artist-" + track.id(), canPreview && artistArea.contains(mx, my) ? 1.0 : 0.0, now, BloodHelperClient.config().animations, 150.0);
                this.text(g, CustomFont.regular().ellipsize(track.artist() + (canPreview ? "  \u203a" : ""), 11.0f, labelWidth), labelX, ry + 28, UiDraw.alpha(UiDraw.blend(-7105633, ThemeColors.accent(), hovered), (int)(255.0 * fade)));
                if (canPreview && fade > 0.95 && this.rowInteractive) {
                    this.actions.add(artistArea);
                }
                this.text(g, MusicSearchScreen.time(track.durationMs()), x + available - 134, ry + 18, UiDraw.alpha(-7105633, (int)(255.0 * fade)));
                if (track.available() && current.status() != MusicSearchResult.Status.OPENING && current.status() != MusicSearchResult.Status.CONNECTING) {
                    this.button(g, x + available - 86, ry + 14, 76, HudStyle.tr("\u0418\u0433\u0440\u0430\u0442\u044c", "Play"), mx, my, () -> this.service.open(current, track));
                    continue;
                }
                this.text(g, current.status() == MusicSearchResult.Status.OPENING || current.status() == MusicSearchResult.Status.CONNECTING ? "\u2026" : HudStyle.tr("\u041d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d", "Unavailable"), x + available - 82, ry + 18, -7105633);
            }
        }
        finally {
            g.disableScissor();
        }
        this.rowOpacity = 1.0f;
        this.rowInteractive = true;
        this.bodyBottom = this.top + this.panelHeight - 26;
        String hint = current.status() == MusicSearchResult.Status.READY || current.status() == MusicSearchResult.Status.IDLE || current.status() == MusicSearchResult.Status.EMPTY ? (this.artist != null ? (this.provider == MusicSearchResult.Provider.YANDEX ? HudStyle.tr("\u041f\u0435\u0441\u043d\u0438 \u0438\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044f \u00b7 \u00ab\u0418\u0433\u0440\u0430\u0442\u044c\u00bb \u2014 \u0437\u0430\u043f\u0443\u0441\u0442\u0438\u0442\u044c \u0442\u0440\u0435\u043a", "Artist's songs \u00b7 Play a track") : HudStyle.tr("\u041f\u0435\u0441\u043d\u0438 \u0438\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044f \u00b7 \u00ab\u0418\u0433\u0440\u0430\u0442\u044c\u00bb \u2014 \u0437\u0430\u043f\u0443\u0441\u0442\u0438\u0442\u044c \u0442\u0440\u0435\u043a \u0432 Spotify", "Artist's songs \u00b7 Play a track in Spotify")) : (this.provider == MusicSearchResult.Provider.YANDEX ? HudStyle.tr("Enter \u2014 \u043f\u043e\u0438\u0441\u043a \u00b7 \u041f\u0443\u0441\u0442\u0430\u044f \u0441\u0442\u0440\u043e\u043a\u0430 \u2014 \u0447\u0430\u0440\u0442 \u00b7 \u041d\u0430\u0436\u043c\u0438 \u043d\u0430 \u0430\u0432\u0442\u043e\u0440\u0430, \u0447\u0442\u043e\u0431\u044b \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0435\u0433\u043e \u043f\u0435\u0441\u043d\u0438", "Enter to search \u00b7 Empty search shows charts \u00b7 Click an artist for their songs") : HudStyle.tr("\u041e\u0442\u043a\u0440\u043e\u0439 Spotify \u0438 \u0432\u043e\u0439\u0434\u0438 \u0432 \u0430\u043a\u043a\u0430\u0443\u043d\u0442 \u00b7 Enter \u2014 \u043f\u043e\u0438\u0441\u043a", "Open Spotify and sign in \u00b7 Enter to search"))) : this.message(current.status());
        this.footer(g, hint);
    }

    private void thumbnail(GuiGraphicsExtractor g, String url, String name, int x, int y, int size) {
        Object key;
        Object object = key = url.isEmpty() ? "fallback:" + name : url;
        if (!this.thumbnails.containsKey(key) && this.thumbnails.size() >= 32) {
            String oldest = this.thumbnails.keySet().iterator().next();
            this.thumbnails.remove(oldest).close();
        }
        this.thumbnails.computeIfAbsent((String)key, ignored -> new MusicArtistArtwork()).draw(g, this.images.get(url), name, x, y, size);
    }

    @Override
    protected void button(GuiGraphicsExtractor g, int x, int y, int w, String label, int mx, int my, Runnable callback) {
        ModuleDetailScreen.Action action = new ModuleDetailScreen.Action(x, y, w, 21, callback);
        if (this.rowOpacity > 0.95f && this.rowInteractive) {
            this.actions.add(action);
        }
        double hover = this.motion.value("button-" + label + "-" + x + "-" + y, action.contains(mx, my) ? 1.0 : 0.0, System.nanoTime(), BloodHelperClient.config().animations, 150.0);
        int alpha = (int)(255.0f * this.rowOpacity);
        UiDraw.rounded(g, x, y, w, 21, 6, UiDraw.alpha(UiDraw.blend(ThemeColors.surface(-13421766), ThemeColors.surface(-11451061), hover), alpha));
        UiDraw.outline(g, x, y, w, 21, 6, UiDraw.alpha(UiDraw.blend(ThemeColors.surface(-11184801), ThemeColors.accent(), hover), alpha));
        CustomFont font = CustomFont.medium();
        String text = font.ellipsize(label, 10.0f, Math.max(0, w - 10));
        font.draw(g, text, (float)x + ((float)w - font.width(text, 10.0f)) / 2.0f, y + 5, 10.0f, UiDraw.alpha(-1381908, alpha));
    }

    private void resetScroll() {
        this.scroll = 0;
        this.paintedScroll = 0.0;
        this.motion.reset();
    }

    private void closeChooser() {
        this.chooser = List.of();
        this.scroll = this.searchScroll;
        this.paintedScroll = this.scroll;
        this.motion.reset();
        this.resultsAt = System.nanoTime();
    }

    private void page(int page) {
        this.resetScroll();
        this.service.searchArtist(this.provider, this.artist, page);
    }

    private void preview(MusicSearchResult.Artist person) {
        this.minecraft.gui.setScreen((Screen)new MusicSearchScreen(this, this.provider, person));
    }

    private void choose(MusicSearchResult.Provider next) {
        if (this.provider != next) {
            this.provider = next;
            this.resetScroll();
            this.chooser = List.of();
            this.service.reset(this.provider, this.input.getValue());
            if (MusicSearchResult.cleanQuery(this.input.getValue()).isEmpty()) {
                this.service.search(this.provider, "");
            }
        }
    }

    private void submit() {
        this.resetScroll();
        this.chooser = List.of();
        if (this.artist == null) {
            this.service.search(this.provider, this.input.getValue());
        } else {
            this.service.searchArtist(this.provider, this.artist, this.service.result().page());
        }
    }

    private String message(MusicSearchResult.Status status) {
        return switch (status) {
            case MusicSearchResult.Status.IDLE -> HudStyle.tr("\u0412\u0432\u0435\u0434\u0438 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043f\u0435\u0441\u043d\u0438 \u0438\u043b\u0438 \u0438\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044f \u0438 \u043d\u0430\u0436\u043c\u0438 Enter.", "Enter a song title or artist and press Enter.");
            case MusicSearchResult.Status.LOADING -> HudStyle.tr("\u0418\u0449\u0435\u043c \u0442\u0440\u0435\u043a\u0438\u2026", "Searching for tracks\u2026");
            case MusicSearchResult.Status.EMPTY -> HudStyle.tr("\u041d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e. \u041f\u043e\u043f\u0440\u043e\u0431\u0443\u0439 \u0434\u0440\u0443\u0433\u043e\u0435 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435.", "No tracks found. Try a different search.");
            case MusicSearchResult.Status.NETWORK -> HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0432\u044b\u043f\u043e\u043b\u043d\u0438\u0442\u044c \u043f\u043e\u0438\u0441\u043a. \u041f\u0440\u043e\u0432\u0435\u0440\u044c \u0441\u043e\u0435\u0434\u0438\u043d\u0435\u043d\u0438\u0435 \u0438 \u043f\u043e\u0432\u0442\u043e\u0440\u0438.", "Search failed. Check your connection and retry.");
            case MusicSearchResult.Status.RATE_LIMITED -> HudStyle.tr("\u0421\u0435\u0440\u0432\u0438\u0441 \u043f\u0440\u043e\u0441\u0438\u0442 \u043f\u043e\u0434\u043e\u0436\u0434\u0430\u0442\u044c. \u041f\u043e\u0432\u0442\u043e\u0440\u0438 \u043f\u043e\u0438\u0441\u043a \u043d\u0435\u043c\u043d\u043e\u0433\u043e \u043f\u043e\u0437\u0436\u0435.", "Too many requests. Try again shortly.");
            case MusicSearchResult.Status.OPEN_PLAYER -> HudStyle.tr("\u041e\u0442\u043a\u0440\u043e\u0439 \u043c\u0443\u0437\u044b\u043a\u0430\u043b\u044c\u043d\u043e\u0435 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435 \u0438 \u0432\u043e\u0439\u0434\u0438 \u0432 \u0430\u043a\u043a\u0430\u0443\u043d\u0442.", "Open the music app and sign in.");
            case MusicSearchResult.Status.SIGN_IN -> HudStyle.tr("\u0412\u043e\u0439\u0434\u0438 \u0432 \u0430\u043a\u043a\u0430\u0443\u043d\u0442 \u0432 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0438 Spotify, \u0437\u0430\u0442\u0435\u043c \u043f\u043e\u0432\u0442\u043e\u0440\u0438 \u043f\u043e\u0438\u0441\u043a.", "Sign in to the Spotify app, then search again.");
            case MusicSearchResult.Status.UNSUPPORTED -> HudStyle.tr("\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u043c\u0443\u0437\u044b\u043a\u0430\u043b\u044c\u043d\u044b\u043c\u0438 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044f\u043c\u0438 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e \u0432 Windows.", "Music application controls require Windows.");
            case MusicSearchResult.Status.STALE -> HudStyle.tr("\u041f\u043b\u0435\u0435\u0440 \u0438\u0437\u043c\u0435\u043d\u0438\u043b\u0441\u044f. \u041d\u0430\u0436\u043c\u0438 \u00ab\u0418\u0433\u0440\u0430\u0442\u044c\u00bb \u0435\u0449\u0451 \u0440\u0430\u0437.", "The player changed. Press Play again.");
            case MusicSearchResult.Status.OPENING -> HudStyle.tr("\u0412\u043a\u043b\u044e\u0447\u0430\u0435\u043c \u0442\u0440\u0435\u043a\u2026", "Starting track\u2026");
            case MusicSearchResult.Status.OPENED -> HudStyle.tr("\u041f\u0440\u043e\u0434\u043e\u043b\u0436\u0438 \u0432 \u042f\u043d\u0434\u0435\u043a\u0441 \u041c\u0443\u0437\u044b\u043a\u0435. \u0415\u0441\u043b\u0438 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435 \u043d\u0435 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e, \u043e\u0442\u043a\u0440\u043e\u0435\u0442\u0441\u044f \u0441\u0430\u0439\u0442.", "Continue in Yandex Music. The website opens if the app is not installed.");
            case MusicSearchResult.Status.REQUESTED -> HudStyle.tr("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0430. \u0417\u0430\u043f\u0443\u0441\u043a \u043f\u043e\u043a\u0430 \u043d\u0435 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0451\u043d.", "Command sent. Playback is not confirmed yet.");
            case MusicSearchResult.Status.PLAYING -> HudStyle.tr("\u0418\u0433\u0440\u0430\u0435\u0442 \u0432 \u0444\u043e\u043d\u0435. \u041c\u043e\u0436\u043d\u043e \u0437\u0430\u043a\u0440\u044b\u0442\u044c \u043f\u043e\u0438\u0441\u043a.", "Playing in the background. You can close search.");
            case MusicSearchResult.Status.BACKGROUND_SETUP -> HudStyle.tr("\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0438 \u042f\u043d\u0434\u0435\u043a\u0441 \u0434\u043b\u044f \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u0432 \u0444\u043e\u043d\u0435.", "Connect Yandex for background playback.");
            case MusicSearchResult.Status.CONNECTING -> HudStyle.tr("\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0430\u0435\u043c \u042f\u043d\u0434\u0435\u043a\u0441 \u041c\u0443\u0437\u044b\u043a\u0443\u2026", "Connecting Yandex Music\u2026");
            case MusicSearchResult.Status.BACKGROUND_READY -> HudStyle.tr("\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u043e. \u042f\u043d\u0434\u0435\u043a\u0441 \u043c\u043e\u0436\u043d\u043e \u043e\u0441\u0442\u0430\u0432\u0438\u0442\u044c \u0441\u0432\u0451\u0440\u043d\u0443\u0442\u044b\u043c.", "Connected. Yandex can stay minimized.");
            case MusicSearchResult.Status.BACKGROUND_UNAVAILABLE -> HudStyle.tr("\u042f\u043d\u0434\u0435\u043a\u0441 \u043d\u0435 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u043b \u0437\u0430\u043f\u0443\u0441\u043a. \u041f\u0440\u043e\u0432\u0435\u0440\u044c \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e\u0441\u0442\u044c \u043f\u0435\u0441\u043d\u0438 \u0438 \u043f\u043e\u0432\u0442\u043e\u0440\u0438.", "Yandex did not confirm playback. Check track availability and retry.");
            case MusicSearchResult.Status.UNAVAILABLE -> HudStyle.tr("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u043e\u043b\u0443\u0447\u0438\u0442\u044c \u0434\u043e\u0441\u0442\u0443\u043f \u043a \u043f\u043e\u0438\u0441\u043a\u0443 \u043f\u043b\u0435\u0435\u0440\u0430. \u041e\u0442\u043a\u0440\u043e\u0439 \u0435\u0433\u043e \u043e\u043a\u043d\u043e \u0438 \u043f\u043e\u0432\u0442\u043e\u0440\u0438.", "Player search is unavailable. Open its window and retry.");
            default -> "";
        };
    }

    private static String time(long ms) {
        long seconds = ms / 1000L;
        return ms <= 0L ? "\u2014:\u2014" : seconds / 60L + ":" + String.format(Locale.ROOT, "%02d", seconds % 60L);
    }

    @Override
    protected boolean panelMouseClicked(MouseButtonEvent event, boolean doubled) {
        return this.artist == null && this.chooser.isEmpty() && this.input.mouseClicked(event, doubled) || super.panelMouseClicked(event, doubled);
    }

    @Override
    protected boolean panelMouseDragged(MouseButtonEvent event, double dx, double dy) {
        return this.input.mouseDragged(event, dx, dy) || super.panelMouseDragged(event, dx, dy);
    }

    @Override
    protected boolean panelMouseReleased(MouseButtonEvent event) {
        return this.input.mouseReleased(event) || super.panelMouseReleased(event);
    }

    public boolean keyPressed(KeyEvent event) {
        if (this.artist != null) {
            return super.keyPressed(event);
        }
        if (!this.chooser.isEmpty()) {
            return super.keyPressed(event);
        }
        if (this.input.isFocused() && (event.key() == 40 || event.key() == 88)) {
            this.submit();
            return true;
        }
        return this.input.keyPressed(event) || super.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        return this.artist == null && this.chooser.isEmpty() && this.input.charTyped(event) || super.charTyped(event);
    }

    @Override
    public void onClose() {
        if (!this.chooser.isEmpty()) {
            this.closeChooser();
        } else {
            super.onClose();
        }
    }

    public void removed() {
        if (this.service != null) {
            this.retained = this.service.result();
            this.retainedCover = this.service.artwork();
            this.service.close();
            this.service = null;
        }
        this.portrait.close();
        this.thumbnails.values().forEach(MusicArtistArtwork::close);
        this.thumbnails.clear();
        this.images.close();
        super.removed();
    }
}

