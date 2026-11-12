/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.texture.TextureManager
 *  net.minecraft.resources.Identifier
 */
package ru.bloodhelper.client.hud;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.BackgroundMenuScreen;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.HudEditorScreen;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.HudLayout;
import ru.bloodhelper.client.hud.HudMotion;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.MusicTextMotion;
import ru.bloodhelper.client.hud.VolumeDrag;
import ru.bloodhelper.client.lyrics.LyricHighlight;
import ru.bloodhelper.client.lyrics.LyricLine;
import ru.bloodhelper.client.lyrics.LyricRibbon;
import ru.bloodhelper.client.lyrics.LyricsService;
import ru.bloodhelper.client.modules.CombatHudModule;
import ru.bloodhelper.client.modules.MusicPlayerScreen;
import ru.bloodhelper.client.music.MediaDisplayTransition;
import ru.bloodhelper.client.music.MediaFavoriteState;
import ru.bloodhelper.client.music.MediaSeekConfirmation;
import ru.bloodhelper.client.music.MediaSnapshot;

public final class MusicWidget {
    private static final Identifier COVER = Identifier.fromNamespaceAndPath((String)"bloodhelper", (String)"media/cover");
    private static final Map<String, HudRenderer.Bounds> controls = new LinkedHashMap<String, HudRenderer.Bounds>();
    private static long coverRevision = Long.MIN_VALUE;
    private static int coverWidth;
    private static int coverHeight;
    private static float islandWidth;
    private static float islandHeight;
    private static long frameTime;
    private static final HudMotion motion;
    private static final Map<String, Long> pressedAt;
    private static HudRenderer.Bounds seekTrack;
    private static HudRenderer.Bounds lastIslandBounds;
    private static boolean controlsExpanded;
    private static double seekLeft;
    private static double seekWidth;
    private static double knobX;
    private static double knobY;
    private static double knobHitRadius;
    private static double seekBottom;
    private static String renderedTrack;
    private static String gestureTrack;
    private static String motionTrack;
    private static boolean seeking;
    private static long seekPosition;
    private static long seekDuration;
    private static long pendingRequest;
    private static long pendingPosition;
    private static long pendingSince;
    private static long seekNoticeUntil;
    private static final Map<String, Marquee> marquees;
    private static final LyricRibbon lyricRibbon;
    private static LyricRibbon.Frame ribbonFrame;
    private static double wavePhase;
    private static long waveFrame;
    private static final VolumeDrag volumeDrag;
    private static boolean volumeOpen;
    private static HudRenderer.Bounds volumeButton;
    private static HudRenderer.Bounds volumeTrack;
    private static HudRenderer.Bounds volumePane;
    private static double volumeLeft;
    private static double volumeWidth;
    private static double volumeVisibility;
    private static double volumeHintVisibility;
    private static String renderedVolumeTarget;
    private static String volumeHint;
    private static VolumeGeometry volumeGeometry;
    private static HudRenderer.Bounds favoriteButton;
    private static String renderedFavoriteTrack;
    private static FavoriteGeometry favoriteGeometry;
    private static final MediaDisplayTransition displayTransition;
    private static String retainedLyric;
    private static int retainedCountdown;
    private static SeekHint seekHint;
    private static SeekHint lastSeekHint;
    private static double seekHintVisibility;
    private static InteractionGeometry geometry;

    public static HudRenderer.Bounds islandBounds() {
        return lastIslandBounds;
    }

    public static boolean displayingTrackTransition() {
        return displayTransition.holding();
    }

    public static VolumeGeometry volumeGeometry() {
        return volumeGeometry;
    }

    public static FavoriteGeometry favoriteGeometry() {
        return favoriteGeometry;
    }

    static HudLayout.Placement reservedPlacement(ClientConfig c, int width, int height) {
        if (!c.music.enabled || c.music.hideWhenIdle && !BloodHelperClient.media().snapshot().available() && !displayTransition.holding()) {
            return null;
        }
        ClientConfig.HudPosition p = c.music.position;
        return HudLayout.place(width, height, Math.round(islandWidth), Math.round(islandHeight), p.scale, p.x, p.y, "CENTER", false);
    }

    private MusicWidget() {
    }

    public static void render(GuiGraphicsExtractor g, ClientConfig c, boolean preview) {
        MusicWidget.clearControls();
        MediaSnapshot media = preview ? new MediaSnapshot(HudStyle.tr("\u041d\u043e\u0447\u043d\u043e\u0439 \u0433\u043e\u0440\u043e\u0434", "City at night"), HudStyle.tr("\u0414\u0435\u043c\u043e\u043d\u0441\u0442\u0440\u0430\u0446\u0438\u044f", "Preview"), 84000L, 212000L, true, null, -1L) : displayTransition.observe(BloodHelperClient.media().snapshot(), System.nanoTime());
        boolean holding = !preview && displayTransition.holding();
        boolean interactive = MusicWidget.canInteract(c, preview);
        if (!holding) {
            boolean bl = controlsExpanded = !CombatHudModule.compactMusic(c) || interactive && (lastIslandBounds != null && lastIslandBounds.contains(MusicWidget.mouseX(), MusicWidget.mouseY()) || seeking || volumeDrag.active());
        }
        if (!interactive) {
            MusicWidget.cancelInteraction();
        }
        if (!(media.canSeek() && c.music.showProgress && controlsExpanded && media.trackToken().equals(gestureTrack))) {
            MusicWidget.cancelSeeking();
        }
        if (!(controlsExpanded && c.music.showControls && media.canControlVolume())) {
            MusicWidget.cancelVolume();
        }
        if (volumeDrag.active()) {
            volumeDrag.retain(media.volumeTargetToken());
            MusicWidget.dispatchVolume(System.nanoTime(), false);
        }
        if (!media.trackToken().equals(motionTrack)) {
            motion.reset();
            motionTrack = media.trackToken();
            seekNoticeUntil = 0L;
            marquees.clear();
            waveFrame = 0L;
            wavePhase = 0.0;
        }
        if (!preview && !media.available()) {
            MusicWidget.releaseCover();
        }
        if (!preview && c.music.hideWhenIdle && !media.available()) {
            frameTime = 0L;
            lastIslandBounds = null;
            return;
        }
        long position = preview ? media.positionMs() : BloodHelperClient.playbackPosition(media);
        long now = System.nanoTime();
        if (pendingRequest != 0L) {
            boolean answered;
            boolean bl = answered = media.seekRequestId() == pendingRequest;
            if (answered && !media.seekAccepted()) {
                pendingRequest = 0L;
                seekNoticeUntil = now + 2500000000L;
            } else if (MediaSeekConfirmation.arrived(media, pendingRequest, pendingPosition, now - pendingSince)) {
                pendingRequest = 0L;
            } else if (now - pendingSince > 4000000000L) {
                pendingRequest = 0L;
                seekNoticeUntil = now + 2500000000L;
            }
        }
        if (seeking) {
            position = seekPosition;
        } else if (pendingRequest != 0L) {
            position = pendingPosition;
        }
        String lyric = "";
        int countdown = 0;
        if (c.music.lyricsMode.equals("ISLAND") || c.music.lyricsMode.equals("BOTH")) {
            String string = lyric = preview ? HudStyle.tr("\u041e\u0433\u043d\u0438 \u043e\u0441\u0442\u0430\u043d\u0443\u0442\u0441\u044f \u0441 \u043d\u0430\u043c\u0438", "The lights stay with us") : BloodHelperClient.lyrics().currentLine(position, c.music);
            if (!preview && lyric.isBlank()) {
                countdown = BloodHelperClient.lyrics().countdownSeconds(position, c.music);
            }
        }
        if (!holding) {
            if (lyric.isBlank()) {
                lyricRibbon.clear();
                ribbonFrame = LyricRibbon.Frame.empty(c.music.hudLyricLines);
            } else {
                List<LyricLine> cues = preview ? List.of(new LyricLine(0L, HudStyle.tr("\u0418 \u0433\u043e\u0440\u043e\u0434 \u043c\u0435\u0434\u043b\u0435\u043d\u043d\u043e \u0443\u0441\u043d\u0451\u0442", "The city slowly falls asleep")), new LyricLine(1000L, lyric), new LyricLine(2000L, HudStyle.tr("\u0414\u043e \u043f\u0435\u0440\u0432\u044b\u0445 \u0443\u0442\u0440\u0435\u043d\u043d\u0438\u0445 \u043b\u0443\u0447\u0435\u0439", "Until the first morning light"))) : BloodHelperClient.lyrics().lines(c.music);
                ribbonFrame = lyricRibbon.update(preview ? "preview" : BloodHelperClient.lyrics().trackKey(), cues, preview ? 1000L : position + (long)BloodHelperClient.lyrics().offsetMs(c.music), c.music.hudLyricLines, now, c.animations && !preview);
            }
        }
        if (holding) {
            lyric = retainedLyric;
            countdown = retainedCountdown;
        } else if (!preview) {
            retainedLyric = lyric;
            retainedCountdown = countdown;
        }
        MusicWidget.renderFrame(g, c, media, position, lyric, countdown, preview);
    }

    private static void renderFrame(GuiGraphicsExtractor g, ClientConfig c, MediaSnapshot media, long position, String lyric, int countdown, boolean preview) {
        String artist;
        String title;
        HudRenderer.Bounds box;
        int targetWidth;
        int rowY;
        ClientConfig.MusicSettings settings = c.music;
        boolean expanded = controlsExpanded;
        boolean showLyric = !c.simpleHud && expanded && (!lyric.isBlank() || countdown > 0);
        long now = System.nanoTime();
        volumeVisibility = motion.value("volume-expanded", volumeOpen ? 1.0 : 0.0, now, c.animations && !preview, RenderQuality.animationMillis(190.0));
        int volumeHeight = expanded ? (int)Math.round(24.0 * volumeVisibility) : 0;
        int n = rowY = settings.showProgress ? 44 : 38;
        int bodyBottom = settings.showControls ? rowY + 22 : (settings.showProgress ? rowY + 12 : 38);
        int lyricY = bodyBottom + 7 + volumeHeight;
        int n2 = targetWidth = expanded ? 214 : 190;
        int targetHeight = expanded ? (showLyric ? lyricY + (countdown > 0 ? 20 : ribbonFrame.height()) : bodyBottom + 9 + volumeHeight) : 36;
        double dt = frameTime == 0L ? 1.0 : Math.min(0.1, (double)(now - frameTime) / 1.0E9);
        double blend = c.animations && !preview && frameTime != 0L && now - frameTime < 500000000L ? 1.0 - Math.exp(-dt * 24.0) : 1.0;
        frameTime = now;
        islandWidth = (float)((double)islandWidth + (double)((float)targetWidth - islandWidth) * blend);
        islandHeight = (float)((double)islandHeight + (double)((float)targetHeight - islandHeight) * blend);
        int w = Math.round(islandWidth);
        int h = Math.round(islandHeight);
        lastIslandBounds = box = HudRenderer.begin(g, "music", settings.position, "CENTER", false, w, h);
        double scale = HudRenderer.renderedScale("music");
        int radius = expanded ? 14 : 20;
        HudStyle.surface(g, 0, 0, w, h, radius, c);
        g.enableScissor(1, 1, w - 1, h - 1);
        int coverSize = expanded ? 28 : 22;
        int coverX = 11;
        int coverY = 7;
        int textX = Math.max(settings.showCover && !c.simpleHud ? coverX + coverSize + 8 : 14, 33);
        int textWidth = Math.max(0, w - textX * 2);
        if (!preview) {
            MusicWidget.updateCover(media);
        }
        if (settings.showCover && !c.simpleHud) {
            MusicWidget.drawCover(g, coverX, coverY, coverSize, c, preview);
        }
        String string = title = media.available() ? media.title() : HudStyle.tr(c, "\u041c\u0443\u0437\u044b\u043a\u0430", "Music");
        if (title.isBlank()) {
            title = HudStyle.tr("\u0411\u0435\u0437 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u044f", "Untitled");
        }
        String string2 = artist = media.available() ? media.artist() : MusicWidget.mediaStatus();
        if (media.available() && artist.isBlank()) {
            artist = HudStyle.tr("\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u044b\u0439 \u0438\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c", "Unknown artist");
        }
        if (!preview && now < seekNoticeUntil) {
            artist = HudStyle.tr("\u041f\u043b\u0435\u0435\u0440 \u043d\u0435 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u043b \u043f\u0435\u0440\u0435\u043c\u043e\u0442\u043a\u0443", "Player did not confirm seeking");
        }
        MusicWidget.scrollingText(g, "title", title, CustomFont.medium(), textX, expanded ? 7 : 4, textWidth, expanded ? 11.0f : 10.0f, -921101, media.playing(), c, preview, now);
        if (!c.simpleHud) {
            MusicWidget.scrollingText(g, "artist", artist, CustomFont.regular(), textX, expanded ? 23 : 20, textWidth, 9.0f, -4802625, media.playing(), c, preview, now);
        }
        boolean volumeAffordance = expanded && settings.showControls && MusicWidget.canInteract(c, preview);
        double volumeButtonVisibility = motion.value("volume-affordance", volumeAffordance ? 1.0 : 0.0, now, c.animations && !preview, RenderQuality.animationMillis(140.0));
        if (!c.simpleHud && volumeButtonVisibility < 0.999) {
            MusicWidget.drawActivity(g, w - 32, expanded ? 20 : 18, media.playing(), c, preview, now, 1.0 - volumeButtonVisibility);
        }
        if (volumeButtonVisibility > 0.001) {
            MusicWidget.drawVolumeButton(g, box, w, scale, media, c, preview, now, volumeButtonVisibility);
            MusicWidget.drawFavoriteButton(g, box, w, scale, media, c, preview, now, volumeButtonVisibility);
        } else {
            volumeHintVisibility = 0.0;
        }
        if (expanded) {
            if (settings.showProgress) {
                MusicWidget.drawProgress(g, box, w, scale, media, position, c, preview, now);
            }
            if (settings.showControls || settings.showProgress) {
                int timeY = rowY + (settings.showControls ? 7 : 1);
                String current = media.available() ? MusicWidget.time(position) : "\u2014:\u2014";
                String total = media.available() && media.durationMs() > 0L ? MusicWidget.time(media.durationMs()) : "\u2014:\u2014";
                current = HudStyle.fitLabel(current, 49);
                total = HudStyle.fitLabel(total, 49);
                HudStyle.label(g, current, 14, timeY, -4802625, true);
                HudStyle.label(g, total, w - 14 - HudStyle.labelWidth(total), timeY, -4802625, true);
            }
            if (settings.showControls) {
                boolean active = media.available();
                MusicWidget.control(g, box, "previous", w / 2 - 41, rowY, 22, scale, active, media.playing(), preview, c, now);
                MusicWidget.control(g, box, "toggle", w / 2 - 13, rowY, 26, scale, active, media.playing(), preview, c, now);
                MusicWidget.control(g, box, "next", w / 2 + 19, rowY, 22, scale, active, media.playing(), preview, c, now);
            }
            if (volumeVisibility > 0.001) {
                MusicWidget.drawVolumeSlider(g, box, w, bodyBottom + 5, scale, media, c, preview, now);
            }
            if (showLyric) {
                if (countdown > 0) {
                    MusicWidget.renderCountdown(g, countdown, position, 14, lyricY, w - 28, c);
                } else {
                    MusicWidget.renderIslandRibbon(g, position, 14, lyricY, w - 28, c, preview);
                }
            }
        }
        g.disableScissor();
        if (volumeHintVisibility < 0.01) {
            MusicWidget.renderSeekHint(g, w, h);
        }
        MusicWidget.renderVolumeHint(g, w, h);
        g.pose().popMatrix();
    }

    public static void renderArtwork(GuiGraphicsExtractor g, int x, int y, int size, MediaSnapshot media) {
        MusicWidget.updateCover(media);
        MusicWidget.drawCover(g, x, y, size, BloodHelperClient.config(), false);
    }

    private static void drawCover(GuiGraphicsExtractor g, int x, int y, int size, ClientConfig c, boolean preview) {
        UiDraw.rounded(g, x, y, size, size, 7, ThemeColors.surface(-14407888));
        if (!preview && coverWidth > 0 && coverHeight > 0) {
            double fit = (double)(size - 4) / (double)Math.max(coverWidth, coverHeight);
            int drawWidth = Math.max(1, (int)Math.round((double)coverWidth * fit));
            int drawHeight = Math.max(1, (int)Math.round((double)coverHeight * fit));
            g.blit(RenderPipelines.GUI_TEXTURED, COVER, x + (size - drawWidth) / 2, y + (size - drawHeight) / 2, 0.0f, 0.0f, drawWidth, drawHeight, coverWidth, coverHeight, coverWidth, coverHeight);
        } else {
            UiIcons.draw(g, "music", x + 3, y + 3, size - 6, preview ? c.accent : -4802625);
        }
    }

    private static void drawActivity(GuiGraphicsExtractor g, int x, int centerY, boolean playing, ClientConfig c, boolean preview, long now, double opacity) {
        if (waveFrame != 0L && playing && c.animations && !preview) {
            wavePhase += Math.min(0.1, Math.max(0.0, (double)(now - waveFrame) / 1.0E9)) * 4.2;
        }
        waveFrame = now;
        double energy = motion.value("wave-energy", playing ? 1.0 : 0.12, now, c.animations && !preview, RenderQuality.animationMillis(280.0));
        UiIcons.wave(g, x, centerY - 6, 21.0f, 12.0f, preview || !c.animations ? 0.8 : wavePhase, energy, UiDraw.alpha(playing ? c.accent : -7763050, (int)(255.0 * opacity)));
    }

    private static void drawVolumeButton(GuiGraphicsExtractor g, HudRenderer.Bounds box, int width, double scale, MediaSnapshot media, ClientConfig c, boolean preview, long now, double visibility) {
        boolean interactive = MusicWidget.canInteract(c, preview);
        boolean supported = media.available() && media.canControlVolume();
        HudRenderer.Bounds hit = MusicWidget.clippedBounds(box, width - 34, 20, 24, 15, scale);
        boolean hovered = interactive && hit != null && hit.contains(MusicWidget.mouseX(), MusicWidget.mouseY());
        double hover = motion.value("volume-button-hover", hovered ? 1.0 : 0.0, now, c.animations && !preview, RenderQuality.animationMillis(150.0));
        int opacity = (int)Math.round(255.0 * visibility);
        g.pose().pushMatrix();
        g.pose().translate((float)(width - 22), (float)(27.5 - hover));
        if (volumeOpen || hover > 0.001) {
            UiDraw.rounded(g, -12, -7, 24, 15, 5, UiDraw.alpha(c.accent, (int)(visibility * (volumeOpen ? 28.0 : hover * 22.0))));
        }
        float size = (float)(1.0 + 0.07 * hover);
        g.pose().scale(size, size);
        UiIcons.draw(g, "volume", -7, -7, 14, UiDraw.alpha(supported ? -921101 : -7763050, opacity));
        g.pose().popMatrix();
        if (interactive && hit != null) {
            volumeButton = hit;
            renderedVolumeTarget = media.volumeTargetToken();
            volumeGeometry = new VolumeGeometry(hit, null, 0.0, 0.0, media.volume(), supported, volumeOpen, volumeDrag.active());
        }
        boolean hint = hovered && (!volumeOpen || !supported);
        volumeHintVisibility = motion.value("volume-hint", hint ? 1.0 : 0.0, now, c.animations && !preview, RenderQuality.animationMillis(140.0));
        if (hint) {
            volumeHint = supported ? HudStyle.tr("\u0413\u0440\u043e\u043c\u043a\u043e\u0441\u0442\u044c \u043f\u043b\u0435\u0435\u0440\u0430", "Player volume") + ": " + Math.round(media.volume() * 100.0) + "%" : HudStyle.tr("\u0413\u0440\u043e\u043c\u043a\u043e\u0441\u0442\u044c \u044d\u0442\u043e\u0433\u043e \u043f\u043b\u0435\u0435\u0440\u0430 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u0430", "Volume unavailable for this player");
        }
    }

    private static void drawFavoriteButton(GuiGraphicsExtractor g, HudRenderer.Bounds box, int width, double scale, MediaSnapshot media, ClientConfig c, boolean preview, long now, double visibility) {
        int color;
        boolean interactive = MusicWidget.canInteract(c, preview);
        MediaFavoriteState state = media.favorite();
        boolean pending = !preview && BloodHelperClient.media().favoritePending();
        HudRenderer.Bounds hit = MusicWidget.clippedBounds(box, width - 34, 3, 24, 16, scale);
        boolean hover = interactive && hit != null && hit.contains(MusicWidget.mouseX(), MusicWidget.mouseY());
        double lift = motion.value("favorite-hover", hover ? 1.0 : 0.0, now, c.animations && !preview, RenderQuality.animationMillis(150.0));
        int n = state.saved() ? c.accent : (color = state.available() && !pending ? -921101 : -7763050);
        if (lift > 0.001 || state.saved()) {
            UiDraw.rounded(g, width - 34, 3, 24, 16, 5, UiDraw.alpha(c.accent, (int)((state.saved() ? 22.0 : 22.0 * lift) * visibility)));
        }
        UiIcons.draw(g, "heart", width - 29, 4 - (int)Math.round(lift), 14, UiDraw.alpha(color, (int)(255.0 * visibility)));
        if (interactive && hit != null) {
            favoriteButton = hit;
            renderedFavoriteTrack = media.trackToken();
            favoriteGeometry = new FavoriteGeometry(hit, state.available(), state.saved(), pending);
        }
        if (hover) {
            volumeHintVisibility = 1.0;
            volumeHint = pending ? HudStyle.tr("\u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0435\u043c \u0432 \u0438\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0435\u2026", "Adding to favorites\u2026") : (state.saved() ? HudStyle.tr("\u0412 \u0438\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u043c", "In favorites") : (state.status().equals("not_confirmed") ? HudStyle.tr("\u041f\u043b\u0435\u0435\u0440 \u043d\u0435 \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u043b \u043b\u0430\u0439\u043a", "Player did not confirm the like") : (state.canAdd() ? HudStyle.tr("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0432 \u0438\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0435", "Add to favorites") : (state.status().equals("unsupported") ? HudStyle.tr("\u041f\u043b\u0435\u0435\u0440 \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442 \u0438\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0435", "Player favorites unsupported") : HudStyle.tr("\u041e\u0442\u043a\u0440\u043e\u0439 \u043e\u043a\u043d\u043e \u043f\u043b\u0435\u0435\u0440\u0430 \u0434\u043b\u044f \u0438\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0433\u043e", "Open the player window for favorites")))));
        }
    }

    private static void drawVolumeSlider(GuiGraphicsExtractor g, HudRenderer.Bounds box, int width, int y, double scale, MediaSnapshot media, ClientConfig c, boolean preview, long now) {
        int left = 34;
        int trackWidth = width - 81;
        int centerY = y + 9;
        boolean supported = media.available() && media.canControlVolume();
        boolean interactive = supported && MusicWidget.canInteract(c, preview) && volumeOpen && volumeVisibility > 0.8;
        HudRenderer.Bounds hit = MusicWidget.clippedBounds(box, left - 4, centerY - 8, trackWidth + 8, 16, scale);
        boolean hovered = interactive && hit != null && hit.contains(MusicWidget.mouseX(), MusicWidget.mouseY());
        double hover = motion.value("volume-track-hover", hovered || volumeDrag.active() ? 1.0 : 0.0, now, c.animations && !preview, RenderQuality.animationMillis(130.0));
        double value = volumeDrag.active() ? volumeDrag.value() : media.volume();
        double display = motion.value("volume-position", value, now, c.animations && !preview && !volumeDrag.active(), RenderQuality.animationMillis(120.0));
        int opacity = (int)Math.round(255.0 * volumeVisibility);
        UiIcons.draw(g, "volume", 13, centerY - 6, 12, UiDraw.alpha(-4802625, opacity));
        g.pose().pushMatrix();
        g.pose().translate(0.0f, (float)(-hover));
        HudStyle.progress(g, left, centerY - 1, trackWidth, (float)display, c.accent, volumeVisibility);
        g.pose().translate((float)((double)left + (double)trackWidth * display), (float)centerY);
        float knobScale = (float)(1.0 + 0.35 * hover);
        g.pose().scale(knobScale, knobScale);
        UiDraw.rounded(g, -3, -3, 6, 6, 3, ThemeColors.icon(UiDraw.alpha(-921101, opacity)));
        g.pose().popMatrix();
        String percent = Math.round(value * 100.0) + "%";
        HudStyle.label(g, percent, width - 14 - HudStyle.labelWidth(percent), centerY - 4, UiDraw.alpha(-4802625, opacity), false);
        if (interactive && hit != null) {
            volumeTrack = hit;
            volumePane = MusicWidget.clippedBounds(box, 11, y, width - 22, 20, scale);
            volumeLeft = (double)box.x() + (double)left * scale;
            volumeWidth = (double)trackWidth * scale;
            renderedVolumeTarget = media.volumeTargetToken();
            volumeGeometry = new VolumeGeometry(volumeButton, hit, volumeLeft, volumeWidth, value, true, true, volumeDrag.active());
        }
    }

    private static int hintY(int height) {
        HudRenderer.Bounds box = HudRenderer.bounds().get("music");
        double scale = HudRenderer.renderedScale("music");
        return box != null && (double)box.y() + (double)(height + 20) * scale > (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() ? -19 : height + 5;
    }

    private static void renderVolumeHint(GuiGraphicsExtractor g, int width, int height) {
        if (volumeHintVisibility < 0.01 || volumeHint.isBlank()) {
            return;
        }
        String hint = HudStyle.fitLabel(volumeHint, width - 18);
        int tipWidth = HudStyle.labelWidth(hint) + 10;
        int x = Math.max(4, width - tipWidth - 6);
        int y = MusicWidget.hintY(height);
        g.pose().pushMatrix();
        g.pose().translate(0.0f, (float)((1.0 - volumeHintVisibility) * -2.0));
        UiDraw.rounded(g, x, y, tipWidth, 14, 5, ThemeColors.surface(UiDraw.alpha(2104095, (int)(245.0 * volumeHintVisibility))));
        HudStyle.label(g, hint, x + 5, y + 3, UiDraw.alpha(-921101, (int)(255.0 * volumeHintVisibility)), false);
        g.pose().popMatrix();
    }

    private static void scrollingText(GuiGraphicsExtractor g, String key, String value, CustomFont font, int x, int y, int width, float size, int color, boolean playing, ClientConfig c, boolean preview, long now) {
        boolean animate = c.animations && !preview;
        double elapsed = marquees.computeIfAbsent(key, unused -> new Marquee()).time(value, now, playing && animate);
        String text = animate ? value : font.ellipsize(value, size, width);
        double offset = MusicTextMotion.offset(font.width(text, size), width, elapsed, 0.0, true);
        g.enableScissor(x, y - 1, x + width, y + (int)Math.ceil(font.lineHeight(size)) + 1);
        MusicWidget.drawText(g, font, text, (float)((double)x + offset), y, size, color, c);
        g.disableScissor();
    }

    private static void drawText(GuiGraphicsExtractor g, CustomFont font, String text, float x, float y, float size, int color, ClientConfig c) {
        if (c.textShadow && RenderQuality.shadows()) {
            font.drawRaw(g, text, x + 0.5f, y + 0.5f, size, UiDraw.alpha(0, (color >>> 24) / 2));
        }
        font.draw(g, text, x, y, size, color);
    }

    private static void renderCountdown(GuiGraphicsExtractor g, int count, long position, int x, int y, int width, ClientConfig c) {
        double pulse = 1.0;
        if (c.animations) {
            long start = 0L;
            for (LyricLine line : BloodHelperClient.lyrics().lines(c.music)) {
                if (line.text().isBlank()) continue;
                start = line.timestampMs();
                break;
            }
            long remaining = Math.max(0L, start - position - (long)BloodHelperClient.lyrics().offsetMs(c.music));
            double elapsed = 1.0 - Math.clamp((double)(remaining - (long)((count - 1) * 1000)) / 1000.0, 0.0, 1.0);
            pulse = 1.0 + 0.14 * Math.exp(-elapsed * 7.0);
        }
        String text = Integer.toString(count);
        CustomFont font = CustomFont.medium();
        float size = 11.5f;
        g.pose().pushMatrix();
        g.pose().translate((float)x + (float)width / 2.0f, (float)(y + 7));
        g.pose().scale((float)pulse, (float)pulse);
        MusicWidget.drawText(g, font, text, -font.width(text, size) / 2.0f, -7.0f, size, c.accent, c);
        g.pose().popMatrix();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderIslandRibbon(GuiGraphicsExtractor g, long position, int x, int y, int width, ClientConfig c, boolean preview) {
        CustomFont font = CustomFont.medium();
        float size = 10.5f;
        LyricsService.ActiveLine active = preview ? null : BloodHelperClient.lyrics().current(position, c.music);
        long time = position + (long)BloodHelperClient.lyrics().offsetMs(c.music);
        double elapsed = active == null ? 0.0 : (double)Math.max(0L, time - active.line().timestampMs()) / 1000.0;
        double cueSeconds = active == null ? 1.0 : Math.max(0.001, (double)(active.endMs() - active.line().timestampMs()) / 1000.0);
        g.enableScissor(x, y - 2, x + width, y + ribbonFrame.height() - 2);
        try {
            for (LyricRibbon.Row row : ribbonFrame.rows()) {
                String text = row.active() && c.animations && !preview ? row.line().text() : font.ellipsize(row.line().text(), size, width);
                float lineWidth = font.width(text, size);
                float textX = row.active() && c.animations && !preview ? (float)(MusicTextMotion.offset(lineWidth, width, elapsed, cueSeconds, false) - (double)width / 2.0) : -lineWidth / 2.0f;
                int opacity = (int)Math.round(255.0 * row.opacity());
                g.pose().pushMatrix();
                try {
                    g.pose().translate((float)x + (float)width / 2.0f, (float)((double)(y + ribbonFrame.centerOffset()) + row.y()));
                    g.pose().scale((float)row.scaleX(), (float)row.scaleY());
                    MusicWidget.drawRibbonText(g, font, text, textX, -7.0f, size, opacity, row.focus(), row.upcoming(), c);
                    if (!row.active() || active == null || !active.line().wordTimed() || !text.equals(active.line().text())) continue;
                    LyricHighlight.draw(g, font, active.line(), text, 0, textX, -7.0f, size, UiDraw.alpha(UiDraw.blend(c.accent, -1, 0.7), (int)((double)opacity * row.focus())), time);
                }
                finally {
                    g.pose().popMatrix();
                }
            }
        }
        finally {
            g.disableScissor();
        }
    }

    private static void drawRibbonText(GuiGraphicsExtractor g, CustomFont font, String text, float x, float y, float size, int opacity, double focus, double upcoming, ClientConfig c) {
        if (c.music.lyricColor.equals("GRADIENT")) {
            MusicTextMotion.gradient(text, x, font.width(text, size), (complete, left, right, blend) -> {
                g.enableScissor(left, (int)Math.floor(y) - 2, right, (int)Math.ceil(y) + 18);
                try {
                    int color = MusicWidget.ribbonColor(UiDraw.blend(c.accent, -1, blend), focus, upcoming);
                    MusicWidget.drawText(g, font, complete, x, y, size, UiDraw.alpha(color, opacity), c);
                }
                finally {
                    g.disableScissor();
                }
            });
        } else {
            int base = c.music.lyricColor.equals("WHITE") ? -921101 : c.accent;
            MusicWidget.drawText(g, font, text, x, y, size, UiDraw.alpha(MusicWidget.ribbonColor(base, focus, upcoming), opacity), c);
        }
    }

    private static int ribbonColor(int base, double focus, double upcoming) {
        int faded = UiDraw.blend(-4802625, base, focus);
        return UiDraw.blend(faded, -31345, upcoming * 0.82);
    }

    public static InteractionGeometry interactionGeometry() {
        return geometry;
    }

    private static void drawProgress(GuiGraphicsExtractor g, HudRenderer.Bounds box, int w, double scale, MediaSnapshot media, long position, ClientConfig c, boolean preview, long now) {
        int trackBottom;
        int left = 13;
        int width = w - 26;
        boolean interactive = MusicWidget.canInteract(c, preview) && media.available() && media.canSeek();
        HudRenderer.Bounds track = MusicWidget.clippedBounds(box, left, 36, width, 10, scale);
        int n = trackBottom = c.music.showControls ? box.y() + (int)Math.floor(44.0 * scale) : Integer.MAX_VALUE;
        if (track != null && track.y() + track.height() > trackBottom) {
            track = trackBottom > track.y() ? new HudRenderer.Bounds(track.x(), track.y(), track.width(), trackBottom - track.y()) : null;
        }
        double mouseX = MusicWidget.mouseX();
        double mouseY = MusicWidget.mouseY();
        boolean trackHover = interactive && track != null && track.contains(mouseX, mouseY);
        double lift = motion.value("track-hover", trackHover ? 1.0 : 0.0, now, c.animations && !preview, RenderQuality.animationMillis(160.0));
        double progress = media.durationMs() > 0L ? Math.clamp((double)position / (double)media.durationMs(), 0.0, 1.0) : 0.0;
        double amount = motion.value("progress", progress, now, c.animations && !preview && !seeking, RenderQuality.animationMillis(130.0));
        double centerX = (double)left + (double)width * amount;
        double centerY = 41.0 - lift;
        double screenX = (double)box.x() + centerX * scale;
        double screenY = (double)box.y() + centerY * scale;
        double hitRadius = Math.max(3.0, 3.5 * scale);
        boolean knobHover = interactive && mouseY < (double)trackBottom && Math.hypot(mouseX - screenX, mouseY - screenY) <= hitRadius;
        double grow = motion.value("knob-hover", knobHover || seeking ? 1.0 : 0.0, now, c.animations && !preview, RenderQuality.animationMillis(160.0));
        double radius = 2.5 + 1.5 * grow;
        g.pose().pushMatrix();
        g.pose().translate(0.0f, (float)(-lift));
        HudStyle.progress(g, left, 40, width, (float)amount, -6907228);
        g.pose().popMatrix();
        g.pose().pushMatrix();
        g.pose().translate((float)centerX, (float)centerY);
        g.pose().scale((float)(radius / 3.0), (float)(radius / 3.0));
        UiDraw.rounded(g, -3, -3, 6, 6, 3, ThemeColors.icon(media.canSeek() || preview ? -921101 : -7763050));
        g.pose().popMatrix();
        if (interactive && track != null) {
            seekTrack = track;
            seekLeft = (double)box.x() + (double)left * scale;
            seekWidth = (double)width * scale;
            seekBottom = trackBottom;
            knobX = screenX;
            knobY = screenY;
            knobHitRadius = hitRadius;
            renderedTrack = media.trackToken();
            seekDuration = media.durationMs();
        }
        long hoverPosition = -1L;
        boolean showHint = trackHover || interactive && seeking;
        seekHintVisibility = motion.value("seek-tooltip", showHint ? 1.0 : 0.0, now, c.animations && !preview, RenderQuality.animationMillis(160.0));
        if (showHint) {
            hoverPosition = MusicWidget.positionAt(mouseX, (double)box.x() + (double)left * scale, (double)width * scale, media.durationMs());
            int localX = (int)Math.round((mouseX - (double)box.x()) / scale);
            int localY = (int)Math.round((mouseY - (double)box.y()) / scale);
            lastSeekHint = new SeekHint(localX, localY + 9, MusicWidget.time(hoverPosition));
        }
        if (seekHintVisibility > 0.01) {
            seekHint = lastSeekHint;
        }
        geometry = new InteractionGeometry(track, screenX, screenY, radius * scale, lift, radius / 2.5, hoverPosition, seeking);
    }

    private static void renderSeekHint(GuiGraphicsExtractor g, int width, int height) {
        if (seekHint == null) {
            return;
        }
        int tipWidth = HudStyle.labelWidth(seekHint.text()) + 10;
        int tipHeight = 14;
        int x = Math.clamp((long)(seekHint.x() - tipWidth / 2), 3, Math.max(3, width - tipWidth - 3));
        int y = MusicWidget.hintY(height);
        g.pose().pushMatrix();
        g.pose().translate(0.0f, (float)((1.0 - seekHintVisibility) * 2.0));
        UiDraw.rounded(g, x, y, tipWidth, tipHeight, 4, ThemeColors.surface(UiDraw.alpha(2104095, (int)(245.0 * seekHintVisibility))));
        HudStyle.label(g, seekHint.text(), x + 5, y + 3, UiDraw.alpha(-921101, (int)(255.0 * seekHintVisibility)), false);
        g.pose().popMatrix();
    }

    private static void control(GuiGraphicsExtractor g, HudRenderer.Bounds box, String command, int x, int y, int width, double scale, boolean active, boolean playing, boolean preview, ClientConfig c, long now) {
        int color;
        boolean main = command.equals("toggle");
        HudRenderer.Bounds hit = MusicWidget.clippedBounds(box, x, y, width, 22, scale);
        boolean hovered = active && MusicWidget.canInteract(c, preview) && hit != null && hit.contains(MusicWidget.mouseX(), MusicWidget.mouseY());
        double hover = motion.value("control-" + command, hovered ? 1.0 : 0.0, now, c.animations && !preview, RenderQuality.animationMillis(170.0));
        Long pressed = pressedAt.get(command);
        double press = pressed == null || !c.animations ? 0.0 : Math.max(0.0, 1.0 - (double)(now - pressed) / 1.7E8);
        g.pose().pushMatrix();
        g.pose().translate((float)x + (float)width / 2.0f, (float)y + 11.0f - (float)(hover * 1.6));
        float size = (float)(1.0 + 0.055 * hover - 0.08 * press);
        g.pose().scale(size, size);
        g.pose().translate((float)(-width) / 2.0f, -11.0f);
        if (main) {
            UiDraw.rounded(g, 0, 0, width, 22, 5, ThemeColors.surface(active ? UiDraw.blend(-13622490, -11849672, hover) : -14804710));
        } else if (hover > 0.001) {
            UiDraw.rounded(g, 0, 0, width, 22, 5, UiDraw.alpha(c.accent, (int)(hover * 25.0)));
        }
        int n = active ? (main ? -921101 : UiDraw.blend(-4802625, -921101, hover)) : (color = -7763050);
        UiIcons.draw(g, main ? (playing ? "pause" : "play") : command, width / 2 - 7, 4, 14, color);
        g.pose().popMatrix();
        if (active && !preview && hit != null) {
            controls.put(command, hit);
        }
    }

    private static HudRenderer.Bounds clippedBounds(HudRenderer.Bounds box, int x, int y, int width, int height, double scale) {
        HudLayout.Placement hit = HudLayout.clip(new HudLayout.Placement(box.x(), box.y(), box.width(), box.height(), scale), x, y, width, height);
        return hit == null ? null : new HudRenderer.Bounds(hit.x(), hit.y(), hit.width(), hit.height());
    }

    private static boolean canInteract(ClientConfig c, boolean preview) {
        return !displayTransition.holding() && MusicWidget.cursorScreen(c, preview);
    }

    private static boolean cursorScreen(ClientConfig c, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        return !preview && !HudRenderer.hiddenInPauseMenu() && mc.player != null && !mc.gui.hud.isHidden() && mc.gui.screen() != null && !(mc.gui.screen() instanceof HudEditorScreen) && !(mc.gui.screen() instanceof BackgroundMenuScreen) && !mc.mouseHandler.isMouseGrabbed() && c.music.enabled && (!c.hudMenuEffects || mc.gui.screen() instanceof ChatScreen);
    }

    public static boolean screenOverlay() {
        return HudRenderer.screenOverlay() && MusicWidget.cursorScreen(BloodHelperClient.config(), false) && !(Minecraft.getInstance().gui.screen() instanceof ChatScreen);
    }

    public static void renderScreen(GuiGraphicsExtractor g) {
        if (!MusicWidget.screenOverlay()) {
            return;
        }
        g.nextStratum();
        MusicWidget.render(g, BloodHelperClient.config(), false);
    }

    private static double mouseX() {
        Minecraft mc = Minecraft.getInstance();
        return mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / Math.max(1.0, (double)mc.getWindow().getScreenWidth());
    }

    private static double mouseY() {
        Minecraft mc = Minecraft.getInstance();
        return mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / Math.max(1.0, (double)mc.getWindow().getScreenHeight());
    }

    private static long positionAt(double x, double left, double width, long duration) {
        return Math.round(Math.clamp((x - left) / Math.max(0.001, width), 0.0, 1.0) * (double)duration);
    }

    public static boolean click(double x, double y, int button) {
        ClientConfig c = BloodHelperClient.config();
        if (!MusicWidget.canInteract(c, false)) {
            MusicWidget.clearControls();
            MusicWidget.cancelInteraction();
            return false;
        }
        MediaSnapshot media = BloodHelperClient.media().snapshot();
        if (button != 1) {
            return false;
        }
        if (!controlsExpanded) {
            return MusicWidget.openPlayer(x, y);
        }
        if (c.music.showControls && favoriteButton != null && favoriteButton.contains(x, y)) {
            if (renderedFavoriteTrack.equals(media.trackToken())) {
                BloodHelperClient.media().addFavorite(renderedFavoriteTrack);
            }
            return true;
        }
        if (c.music.showControls && volumeButton != null && volumeButton.contains(x, y)) {
            MusicWidget.cancelSeeking();
            volumeDrag.cancel();
            volumeOpen = media.available() && media.canControlVolume() && renderedVolumeTarget.equals(media.volumeTargetToken()) ? !volumeOpen : false;
            return true;
        }
        if (c.music.showControls && volumeOpen && volumeTrack != null && volumeTrack.contains(x, y)) {
            MusicWidget.cancelSeeking();
            if (media.available() && media.canControlVolume() && renderedVolumeTarget.equals(media.volumeTargetToken())) {
                volumeDrag.begin(media.volumeTargetToken(), VolumeDrag.at(x, volumeLeft, volumeWidth));
                MusicWidget.dispatchVolume(System.nanoTime(), false);
            }
            return true;
        }
        if (!media.available()) {
            return MusicWidget.openPlayer(x, y);
        }
        if (c.music.showProgress && media.canSeek() && seekTrack != null && renderedTrack.equals(media.trackToken()) && (seekTrack.contains(x, y) || y < seekBottom && Math.hypot(x - knobX, y - knobY) <= knobHitRadius)) {
            gestureTrack = media.trackToken();
            seeking = true;
            pendingRequest = 0L;
            seekNoticeUntil = 0L;
            seekPosition = MusicWidget.positionAt(x, seekLeft, seekWidth, media.durationMs());
            volumeDrag.cancel();
            return true;
        }
        if (!c.music.showControls) {
            return MusicWidget.openPlayer(x, y);
        }
        for (Map.Entry<String, HudRenderer.Bounds> entry : controls.entrySet()) {
            if (!entry.getValue().contains(x, y)) continue;
            pressedAt.put(entry.getKey(), System.nanoTime());
            MusicWidget.cancelSeeking();
            volumeDrag.cancel();
            switch (entry.getKey()) {
                case "previous": {
                    BloodHelperClient.media().previous();
                    break;
                }
                case "next": {
                    BloodHelperClient.media().next();
                    break;
                }
                default: {
                    BloodHelperClient.media().playPause();
                }
            }
            return true;
        }
        return MusicWidget.openPlayer(x, y);
    }

    private static boolean openPlayer(double x, double y) {
        if (lastIslandBounds == null || !lastIslandBounds.contains(x, y)) {
            return false;
        }
        MusicWidget.cancelInteraction();
        Minecraft client = Minecraft.getInstance();
        client.gui.setScreen((Screen)new MusicPlayerScreen(client.gui.screen()));
        return true;
    }

    public static boolean drag(double x, double y, int button) {
        if (button == 1 && volumeDrag.active()) {
            if (!MusicWidget.validVolumeGesture()) {
                volumeDrag.cancel();
                return true;
            }
            volumeDrag.move(VolumeDrag.at(x, volumeLeft, volumeWidth));
            MusicWidget.dispatchVolume(System.nanoTime(), false);
            return true;
        }
        if (button != 1 || !seeking) {
            return false;
        }
        if (!MusicWidget.validGesture()) {
            MusicWidget.cancelSeeking();
            return true;
        }
        seekPosition = MusicWidget.positionAt(x, seekLeft, seekWidth, seekDuration);
        return true;
    }

    public static boolean release(int button) {
        if (button == 1 && volumeDrag.active()) {
            if (MusicWidget.validVolumeGesture()) {
                MusicWidget.dispatchVolume(System.nanoTime(), true);
            }
            volumeDrag.cancel();
            return true;
        }
        if (button != 1 || !seeking) {
            return false;
        }
        boolean valid = MusicWidget.validGesture();
        seeking = false;
        if (valid) {
            pendingRequest = BloodHelperClient.media().seekTo(seekPosition, gestureTrack);
            pendingPosition = seekPosition;
            pendingSince = System.nanoTime();
            if (pendingRequest == 0L) {
                seekNoticeUntil = pendingSince + 2500000000L;
            }
        } else {
            MusicWidget.cancelSeeking();
        }
        return true;
    }

    private static boolean validGesture() {
        ClientConfig c = BloodHelperClient.config();
        MediaSnapshot media = BloodHelperClient.media().snapshot();
        return MusicWidget.canInteract(c, false) && controlsExpanded && c.music.showProgress && media.canSeek() && media.trackToken().equals(gestureTrack);
    }

    private static boolean validVolumeGesture() {
        ClientConfig c = BloodHelperClient.config();
        MediaSnapshot media = BloodHelperClient.media().snapshot();
        return volumeDrag.active() && volumeOpen && MusicWidget.canInteract(c, false) && controlsExpanded && c.music.showControls && media.available() && media.canControlVolume() && volumeDrag.target().equals(media.volumeTargetToken());
    }

    private static void dispatchVolume(long now, boolean release) {
        if (!volumeDrag.active()) {
            return;
        }
        if (!MusicWidget.validVolumeGesture()) {
            volumeDrag.cancel();
            return;
        }
        double request = volumeDrag.request(now, release);
        if (Double.isFinite(request) && !BloodHelperClient.media().setVolume(request, volumeDrag.target())) {
            volumeDrag.cancel();
        }
    }

    public static boolean isSeeking() {
        return seeking;
    }

    public static boolean isAdjustingVolume() {
        return volumeDrag.active();
    }

    public static boolean capturesScroll(double x, double y) {
        return seeking || volumeDrag.active() || MusicWidget.canInteract(BloodHelperClient.config(), false) && (volumeButton != null && volumeButton.contains(x, y) || volumePane != null && volumePane.contains(x, y));
    }

    private static void cancelSeeking() {
        seeking = false;
        pendingRequest = 0L;
        gestureTrack = "";
    }

    private static void cancelVolume() {
        volumeDrag.cancel();
        volumeOpen = false;
    }

    public static void cancelInteraction() {
        MusicWidget.cancelSeeking();
        MusicWidget.cancelVolume();
    }

    public static void clearControls() {
        controls.clear();
        seekTrack = null;
        seekHint = null;
        geometry = null;
        renderedTrack = "";
        volumeButton = null;
        volumeTrack = null;
        volumePane = null;
        volumeGeometry = null;
        renderedVolumeTarget = "";
        favoriteButton = null;
        favoriteGeometry = null;
        renderedFavoriteTrack = "";
    }

    public static void resetMotion() {
        displayTransition.clear();
        retainedLyric = "";
        retainedCountdown = 0;
        lyricRibbon.clear();
        ribbonFrame = LyricRibbon.Frame.empty(3);
        frameTime = 0L;
        motion.reset();
        pressedAt.clear();
        seekNoticeUntil = 0L;
        motionTrack = "";
        lastIslandBounds = null;
        controlsExpanded = false;
        lastSeekHint = null;
        seekHintVisibility = 0.0;
        volumeVisibility = 0.0;
        volumeHintVisibility = 0.0;
        volumeHint = "";
        marquees.clear();
        waveFrame = 0L;
        wavePhase = 0.0;
        MusicWidget.cancelInteraction();
        MusicWidget.clearControls();
    }

    private static String time(long ms) {
        long seconds = Math.max(0L, ms / 1000L);
        return seconds / 60L + ":" + (seconds % 60L < 10L ? "0" : "") + seconds % 60L;
    }

    private static String mediaStatus() {
        return switch (BloodHelperClient.media().status()) {
            case "unsupported_os" -> HudStyle.tr("\u041f\u043b\u0435\u0435\u0440: Windows/Linux", "Player: Windows/Linux");
            case "starting" -> HudStyle.tr("\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435\u2026", "Connecting\u2026");
            case "unavailable" -> HudStyle.tr("\u041f\u043b\u0435\u0435\u0440 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d", "Player unavailable");
            default -> HudStyle.tr("\u041d\u0435\u0442 \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0433\u043e \u0442\u0440\u0435\u043a\u0430", "No active track");
        };
    }

    private static void updateCover(MediaSnapshot media) {
        if (coverRevision == media.revision()) {
            return;
        }
        coverRevision = media.revision();
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        if (coverWidth > 0) {
            textures.release(COVER);
        }
        coverHeight = 0;
        coverWidth = 0;
        byte[] artwork = media.artwork();
        if (artwork == null || artwork.length == 0) {
            return;
        }
        try {
            NativeImage image = NativeImage.read((InputStream)new ByteArrayInputStream(artwork));
            if (image.getWidth() > 2048 || image.getHeight() > 2048) {
                image.close();
                return;
            }
            coverWidth = image.getWidth();
            coverHeight = image.getHeight();
            textures.register(COVER, (AbstractTexture)new DynamicTexture(() -> "BloodHelper music cover", image));
        }
        catch (Exception ignored) {
            coverHeight = 0;
            coverWidth = 0;
        }
    }

    public static void releaseCover() {
        displayTransition.clear();
        retainedLyric = "";
        retainedCountdown = 0;
        lyricRibbon.clear();
        ribbonFrame = LyricRibbon.Frame.empty(3);
        if (coverWidth > 0) {
            Minecraft.getInstance().getTextureManager().release(COVER);
        }
        coverHeight = 0;
        coverWidth = 0;
        coverRevision = Long.MIN_VALUE;
    }

    static {
        islandWidth = 214.0f;
        islandHeight = 75.0f;
        motion = new HudMotion();
        pressedAt = new LinkedHashMap<String, Long>();
        renderedTrack = "";
        gestureTrack = "";
        motionTrack = "";
        marquees = new LinkedHashMap<String, Marquee>();
        lyricRibbon = new LyricRibbon();
        ribbonFrame = LyricRibbon.Frame.empty(3);
        volumeDrag = new VolumeDrag();
        renderedVolumeTarget = "";
        volumeHint = "";
        renderedFavoriteTrack = "";
        displayTransition = new MediaDisplayTransition();
        retainedLyric = "";
    }

    public record VolumeGeometry(HudRenderer.Bounds button, HudRenderer.Bounds track, double left, double width, double value, boolean supported, boolean expanded, boolean dragging) {
    }

    public record FavoriteGeometry(HudRenderer.Bounds button, boolean supported, boolean saved, boolean pending) {
    }

    private static final class Marquee {
        String text = "";
        long previous;
        double elapsed;

        private Marquee() {
        }

        double time(String value, long now, boolean advance) {
            if (!value.equals(this.text)) {
                this.text = value;
                this.elapsed = 0.0;
                this.previous = now;
            }
            if (this.previous != 0L && advance) {
                this.elapsed += Math.min(0.1, Math.max(0.0, (double)(now - this.previous) / 1.0E9));
            }
            this.previous = now;
            return this.elapsed;
        }
    }

    public record InteractionGeometry(HudRenderer.Bounds track, double knobX, double knobY, double knobRadius, double trackLift, double knobScale, long hoverPositionMs, boolean seeking) {
    }

    private record SeekHint(int x, int y, String text) {
    }
}

