/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaterniond
 *  org.joml.Quaterniondc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3d
 */
package ru.bloodhelper.client.lyrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.lyrics.KineticLyricsLayout;
import ru.bloodhelper.client.lyrics.LyricsService;
import ru.bloodhelper.client.lyrics.WorldLyricWords;
import ru.bloodhelper.client.modules.WaypointProjection;
import ru.bloodhelper.client.music.MediaService;
import ru.bloodhelper.client.music.MediaSnapshot;
import ru.bloodhelper.client.visuals.WorldGeometry;
import ru.bloodhelper.client.visuals.WorldProjection;

public final class KineticLyricsRenderer {
    private final LyricsService lyrics;
    private final MediaService media;
    private final Supplier<ClientConfig> config;
    private final List<Word> words = new ArrayList<Word>();
    private final List<WorldGeometry.Text> meshText = new ArrayList<WorldGeometry.Text>();
    private final List<Fading> fading = new ArrayList<Fading>();
    private String track = "";
    private String content = "";
    private Object level;
    private int index = -1;
    private long previousTime = Long.MIN_VALUE;
    private boolean split;
    private static int renderedWords;
    private static final List<int[]> renderedBounds;

    public static List<int[]> renderedBounds() {
        return List.copyOf(renderedBounds);
    }

    public static int renderedWords() {
        return renderedWords;
    }

    private KineticLyricsRenderer(LyricsService lyrics, MediaService media, Supplier<ClientConfig> config) {
        this.lyrics = lyrics;
        this.media = media;
        this.config = config;
    }

    public static void register(LyricsService lyrics, MediaService media, Supplier<ClientConfig> config) {
        KineticLyricsRenderer renderer = new KineticLyricsRenderer(lyrics, media, config);
        LevelRenderEvents.END_MAIN.register(context -> renderer.render());
    }

    private void clear() {
        this.words.clear();
        this.fading.clear();
        this.index = -1;
        this.previousTime = Long.MIN_VALUE;
        this.content = "";
    }

    private void render() {
        String text;
        this.meshText.clear();
        renderedWords = 0;
        renderedBounds.clear();
        Minecraft mc = Minecraft.getInstance();
        ClientConfig c = this.config.get();
        ClientConfig.MusicSettings p = c.music;
        if (!p.enabled || !"WORLD".equals(p.lyricsMode) && !"BOTH".equals(p.lyricsMode) || mc.level == null || mc.player == null || mc.gui.hud.isHidden()) {
            this.clear();
            return;
        }
        if (mc.gui.screen() != null || !WorldProjection.valid()) {
            return;
        }
        MediaSnapshot snapshot = this.media.snapshot();
        if (!snapshot.available()) {
            this.clear();
            return;
        }
        long playback = snapshot.playbackPosition();
        long time = playback + (long)this.lyrics.offsetMs(p);
        long now = System.nanoTime() / 1000000L;
        if (this.level != mc.level || !this.track.equals(this.lyrics.trackKey()) || this.split != p.splitLyrics || this.previousTime != Long.MIN_VALUE && (time < this.previousTime - 400L || time > this.previousTime + 1800L)) {
            this.clear();
        }
        this.level = mc.level;
        this.track = this.lyrics.trackKey();
        this.split = p.splitLyrics;
        this.previousTime = time;
        LyricsService.ActiveLine active = this.lyrics.current(playback, p);
        int next = active == null ? -1 : active.index();
        String string = text = active == null ? "" : active.line().text();
        if (next != this.index || !text.equals(this.content)) {
            for (Word w : this.words) {
                if (w.reveal > time) continue;
                this.fading.add(new Fading(w, now));
            }
            this.words.clear();
            this.index = next;
            this.content = text;
            if (active != null) {
                this.spawn(active, p, now);
            }
        }
        this.fading.removeIf(f -> now - f.at >= 500L);
        while (this.fading.size() > 96) {
            this.fading.removeFirst();
        }
        for (Word w : this.words) {
            if (time < w.reveal) continue;
            this.draw(w, time, now, 1.0, c, true);
        }
        for (Fading f2 : this.fading) {
            this.draw(f2.word, time, now, 1.0 - (double)(now - f2.at) / 500.0, c, false);
        }
        WorldGeometry.text(this.meshText);
    }

    private void spawn(LyricsService.ActiveLine active, ClientConfig.MusicSettings p, long now) {
        Minecraft mc = Minecraft.getInstance();
        CustomFont font = CustomFont.medium();
        float height = (float)(0.36 * p.lyricSize);
        float gap = height * 0.28f;
        float maxWidth = (float)Math.min(p.lyricDistance * 0.95, 10.0);
        List<WorldLyricWords.Cue> cues = WorldLyricWords.cues(active.line(), active.endMs(), p.splitLyrics);
        ArrayList rows = new ArrayList();
        ArrayList<WorldLyricWords.Cue> row = new ArrayList<WorldLyricWords.Cue>();
        float width = 0.0f;
        for (WorldLyricWords.Cue cue : cues) {
            for (String part : font.wrap(cue.text(), 32.0f, maxWidth / height * 32.0f)) {
                float w = font.width(part, 32.0f) / 32.0f * height;
                if (!row.isEmpty() && width + w + gap > maxWidth) {
                    rows.add(row);
                    row = new ArrayList();
                    width = 0.0f;
                }
                row.add(new WorldLyricWords.Cue(part, cue.reveal()));
                width += w + gap;
            }
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }
        Random random = new Random((long)active.line().text().hashCode() * 31L + (long)active.index());
        int rowCount = Math.min(p.lyricLines, rows.size());
        for (int r = 0; r < rowCount; ++r) {
            row = new ArrayList((Collection)rows.get(r));
            width = 0.0f;
            for (WorldLyricWords.Cue cue : row) {
                width += font.width(cue.text(), 32.0f) / 32.0f * height + gap;
            }
            float x = -(width - gap) / 2.0f;
            for (WorldLyricWords.Cue cue : row) {
                Vec3 displacement;
                float w = font.width(cue.text(), 32.0f) / 32.0f * height;
                double jitter = (random.nextDouble() - 0.5) * (double)height * 0.25 * (p.lyricSpread / 65.0);
                Vector3d local = new Vector3d((double)(x + w / 2.0f), (double)((float)(rowCount - 1) * height) * 0.65 - (double)((float)r * height) * 1.3 + jitter + 0.3, -p.lyricDistance).rotate((Quaterniondc)new Quaterniond((Quaternionfc)WorldProjection.rotation()));
                Vec3 origin = WorldProjection.camera();
                BlockHitResult hit = mc.level.clip(new ClipContext(origin, origin.add(displacement = new Vec3(local.x, local.y, local.z)), ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, (Entity)mc.player));
                double fraction = hit.getType() == HitResult.Type.MISS ? 1.0 : KineticLyricsLayout.visibleFraction(displacement.length(), hit.getLocation().distanceTo(origin));
                this.words.add(new Word(cue.text(), origin.add(displacement.scale(fraction)), cue.reveal(), height * (float)fraction, now));
                x += w + gap;
            }
        }
    }

    private void draw(Word w, long time, long now, double fade, ClientConfig c, boolean current) {
        float slide;
        int color;
        double ease;
        double alpha;
        double appear;
        ClientConfig.MusicSettings p = c.music;
        double d = appear = c.animations ? Math.clamp((double)(time - w.reveal) / 280.0, 0.0, 1.0) : 1.0;
        if (!this.media.snapshot().playing() || time - w.reveal > 280L) {
            appear = 1.0;
        }
        if ((alpha = (ease = 1.0 - Math.pow(1.0 - appear, 3.0)) * fade) < 0.02) {
            return;
        }
        Vec3 anchor = w.anchor.add(0.0, (1.0 - fade) * p.lyricFloat, 0.0);
        WaypointProjection.Point point = WorldProjection.projectUnbounded(anchor);
        if (point == null) {
            return;
        }
        float px = WorldProjection.pixelHeight(anchor, w.height);
        if (!Float.isFinite(px) || px < 3.0f) {
            return;
        }
        float originalPx = px;
        px = Math.min(px, 60.0f);
        if ("POP".equals(p.lyricAnimation)) {
            px *= 0.75f + 0.25f * (float)ease;
        }
        CustomFont font = CustomFont.medium();
        float scale = px / 32.0f;
        String text = w.text;
        if ("TYPEWRITER".equals(p.lyricAnimation) && appear < 1.0) {
            int chars = Math.max(1, (int)Math.ceil((double)text.codePointCount(0, text.length()) * appear));
            text = text.substring(0, text.offsetByCodePoints(0, chars));
        }
        float width = font.width(text, 32.0f);
        float y = -16.0f;
        int n = "WHITE".equals(p.lyricColor) ? -1 : (color = UiDraw.blend(c.accent, -1, current ? 0.55 : 0.8));
        if ("GRADIENT".equals(p.lyricColor)) {
            color = UiDraw.blend(c.accent, -1, 0.3 + 0.65 * appear);
        }
        float height = w.height * (px / originalPx);
        float f = slide = "SLIDE".equals(p.lyricAnimation) ? (float)(-0.375 * (1.0 - ease)) : 0.0f;
        if (p.lyricShadow) {
            this.meshText.add(new WorldGeometry.Text(anchor, text, height, UiDraw.alpha(0, (int)(185.0 * alpha)), slide + 0.044f, 0.05f));
        }
        if (p.lyricGlow && RenderQuality.shadows()) {
            int glow = UiDraw.alpha(c.accent, (int)(32.0 * alpha));
            this.meshText.add(new WorldGeometry.Text(anchor, text, height, glow, slide - 0.031f, 0.0f));
            this.meshText.add(new WorldGeometry.Text(anchor, text, height, glow, slide + 0.031f, 0.0f));
        }
        this.meshText.add(new WorldGeometry.Text(anchor, text, height, UiDraw.alpha(color, (int)(255.0 * alpha)), slide, 0.0f));
        ++renderedWords;
        renderedBounds.add(new int[]{(int)(point.x() - width * scale / 2.0f) - 2, (int)(point.y() - 16.0f * scale) - 2, (int)(width * scale) + 4, (int)(32.0f * scale) + 4});
    }

    static {
        renderedBounds = new ArrayList<int[]>();
    }

    private record Word(String text, Vec3 anchor, long reveal, float height, long born) {
    }

    private record Fading(Word word, long at) {
    }
}

