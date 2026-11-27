/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_1060
 *  net.minecraft.class_10799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  org.joml.Matrix3x2fStack
 */
package ru.bloodhelper.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_1060;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import ru.bloodhelper.client.gui.BundledFontFallback;
import ru.bloodhelper.client.gui.FontBatchAccess;
import ru.bloodhelper.client.gui.FontGlyphRun;
import ru.bloodhelper.client.gui.RenderQuality;
import ru.bloodhelper.client.gui.ThemeColors;

public final class CustomFont {
    static final float METRIC_EM = 128.0f;
    private static final int SUPERSAMPLE = 4;
    private static final int PADDING = 2;
    private static final int MAX_LAYOUTS = 512;
    private static final long MAX_ATLAS_PIXELS = 0x400000L;
    private static final int MAX_ATLAS_GLYPHS = 8192;
    private static final List<CustomFont> INSTANCES = new CopyOnWriteArrayList<CustomFont>();
    private static long frame;
    static final FontRenderContext FRC;
    private static final AtomicLong UPLOAD_GENERATION;
    private static final AtomicLong NEXT_PAGE;
    private final Font source;
    private final Font metricFont;
    private Font fallbackMetricFont;
    private final float ascent;
    private final float height;
    private final String resource;
    private final Map<String, Layout> layouts = new LinkedHashMap<String, Layout>(64, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Layout> eldest) {
            return this.size() > 512;
        }
    };
    private final Map<FitKey, String> fitted = new LinkedHashMap<FitKey, String>(64, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<FitKey, String> eldest) {
            return this.size() > 256;
        }
    };
    private final Map<FitKey, List<String>> wrapped = new LinkedHashMap<FitKey, List<String>>(32, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<FitKey, List<String>> eldest) {
            return this.size() > 128;
        }
    };
    private final Map<Integer, Atlas> atlases = new HashMap<Integer, Atlas>();
    private class_1060 textureOwner;
    private boolean cacheGrew;

    private CustomFont(String file) {
        this.resource = "/assets/bloodhelper/fonts/" + file;
        try (InputStream input = CustomFont.class.getResourceAsStream(this.resource);){
            if (input == null) {
                throw new IOException("Missing bundled font " + this.resource);
            }
            this.source = Font.createFont(0, input);
        }
        catch (FontFormatException | IOException e) {
            throw new IllegalStateException("Cannot load custom font " + this.resource, e);
        }
        this.metricFont = this.source.deriveFont(128.0f).deriveFont(Map.of(TextAttribute.KERNING, TextAttribute.KERNING_ON, TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON));
        LineMetrics metrics = this.metricFont.getLineMetrics("Hg\u0410\u0434\u0443", FRC);
        this.ascent = metrics.getAscent() / 128.0f;
        this.height = metrics.getHeight() / 128.0f;
        INSTANCES.add(this);
    }

    public static CustomFont regular() {
        return RegularHolder.INSTANCE;
    }

    public static CustomFont medium() {
        return MediumHolder.INSTANCE;
    }

    public int sourceGlyphCount() {
        return this.source.getNumGlyphs();
    }

    public String sourceName() {
        return this.source.getFontName(Locale.ROOT);
    }

    public static void invalidate() {
        UPLOAD_GENERATION.incrementAndGet();
    }

    public static void endFrame() {
        for (CustomFont font : INSTANCES) {
            font.trimAtlases();
        }
        ++frame;
    }

    private void trimAtlases() {
        if (!this.cacheGrew) {
            return;
        }
        this.cacheGrew = false;
        long pixels = 0L;
        int glyphCount = 0;
        for (Atlas atlas : this.atlases.values()) {
            glyphCount += atlas.glyphs.size();
            for (Page page2 : atlas.pages) {
                pixels += (long)page2.side * (long)page2.side;
            }
        }
        if (pixels <= 0x400000L && glyphCount <= 8192) {
            return;
        }
        ArrayList<Page> oldest = new ArrayList<Page>();
        for (Atlas atlas : this.atlases.values()) {
            oldest.addAll(atlas.pages);
        }
        oldest.sort(Comparator.comparingLong(page -> page.lastUsedFrame));
        for (Page page3 : oldest) {
            if (pixels <= 0x400000L && glyphCount <= 8192) break;
            for (Atlas atlas : this.atlases.values()) {
                if (!atlas.pages.remove(page3)) continue;
                atlas.prepared.clear();
                int before = atlas.glyphs.size();
                atlas.glyphs.values().removeIf(glyph -> glyph.page == page3);
                glyphCount -= before - atlas.glyphs.size();
                break;
            }
            pixels -= (long)page3.side * (long)page3.side;
            page3.manager.method_4615(page3.id);
        }
        if (glyphCount > 8192) {
            for (Atlas atlas : this.atlases.values()) {
                atlas.glyphs.values().removeIf(glyph -> glyph.page == null);
            }
        }
    }

    public float width(String text, float size) {
        if (text == null || text.isEmpty() || !CustomFont.validSize(size)) {
            return 0.0f;
        }
        String normalized = CustomFont.normalize(text);
        if (normalized.indexOf(10) < 0) {
            return this.layout((String)normalized).advance * size;
        }
        float widest = 0.0f;
        for (String line : normalized.split("\n", -1)) {
            widest = Math.max(widest, this.layout((String)line).advance * size);
        }
        return widest;
    }

    public float lineHeight(float size) {
        return CustomFont.validSize(size) ? this.height * size : 0.0f;
    }

    public float[] caretAdvances(String text, float size) {
        if (text == null) {
            text = "";
        }
        if (text.indexOf(10) >= 0 || text.indexOf(13) >= 0 || text.indexOf(9) >= 0) {
            throw new IllegalArgumentException("Caret positions require a single line without tabs");
        }
        float[] result = new float[text.length() + 1];
        if (!CustomFont.validSize(size) || text.isEmpty()) {
            return result;
        }
        float[] positions = this.layout((String)text).carets;
        for (int i = 0; i < result.length; ++i) {
            result[i] = positions[i] * size;
        }
        return result;
    }

    public synchronized String ellipsize(String text, float size, float width) {
        if (text == null || text.isEmpty() || !CustomFont.validSize(size) || !(width > 0.0f)) {
            return "";
        }
        FitKey key = new FitKey(text, size, width);
        String cached = this.fitted.get(key);
        if (cached != null) {
            return cached;
        }
        String result = this.ellipsizeUncached(text, size, width);
        this.fitted.put(key, result);
        return result;
    }

    private String ellipsizeUncached(String text, float size, float width) {
        String line = CustomFont.normalize(text).replace('\n', ' ');
        if (this.width(line, size) <= width) {
            return line;
        }
        String suffix = "\u2026";
        if (this.width(suffix, size) > width) {
            return "";
        }
        List<Integer> boundaries = CustomFont.boundaries(line, false);
        int low = 0;
        int high = boundaries.size() - 1;
        while (low < high) {
            int mid = low + high + 1 >>> 1;
            if (this.width(line.substring(0, boundaries.get(mid)) + suffix, size) <= width) {
                low = mid;
                continue;
            }
            high = mid - 1;
        }
        String prefix = line.substring(0, boundaries.get(low)).stripTrailing();
        while (!prefix.isEmpty() && this.width(prefix + suffix, size) > width) {
            List<Integer> shorter = CustomFont.boundaries(prefix, false);
            prefix = prefix.substring(0, shorter.get(shorter.size() - 2)).stripTrailing();
        }
        return prefix + suffix;
    }

    public synchronized List<String> wrap(String text, float size, float width) {
        if (text == null || text.isEmpty()) {
            return List.of("");
        }
        if (!CustomFont.validSize(size) || !(width > 0.0f)) {
            return List.of();
        }
        FitKey key = new FitKey(text, size, width);
        List<String> cached = this.wrapped.get(key);
        if (cached != null) {
            return cached;
        }
        List<String> result = this.wrapUncached(text, size, width);
        this.wrapped.put(key, result);
        return result;
    }

    private List<String> wrapUncached(String text, float size, float width) {
        ArrayList<String> result = new ArrayList<String>();
        block0: for (String paragraph : CustomFont.normalize(text).split("\n", -1)) {
            if (paragraph.isEmpty()) {
                result.add("");
                continue;
            }
            List<Integer> characters = CustomFont.boundaries(paragraph, false);
            List<Integer> words = CustomFont.boundaries(paragraph, true);
            int start = 0;
            while (start < paragraph.length()) {
                int cp;
                int end = start;
                for (int boundary : characters) {
                    if (boundary <= start) continue;
                    if (this.width(paragraph.substring(start, boundary), size) > width) break;
                    end = boundary;
                }
                if (end == paragraph.length()) {
                    result.add(paragraph.substring(start).stripTrailing());
                    continue block0;
                }
                if (end == start) {
                    for (int boundary : characters) {
                        if (boundary <= start) continue;
                        end = boundary;
                        break;
                    }
                } else {
                    int boundary;
                    int wordEnd = start;
                    Iterator<Integer> iterator = words.iterator();
                    while (iterator.hasNext() && (boundary = iterator.next().intValue()) <= end) {
                        if (boundary <= start) continue;
                        wordEnd = boundary;
                    }
                    if (wordEnd > start) {
                        end = wordEnd;
                    }
                }
                result.add(paragraph.substring(start, end).stripTrailing());
                for (start = end; start < paragraph.length() && Character.isWhitespace(cp = paragraph.codePointAt(start)); start += Character.charCount(cp)) {
                }
            }
        }
        return List.copyOf(result);
    }

    public void draw(class_332 g, String text, float x, float y, float size, int argb) {
        this.drawRaw(g, text, x, y, size, ThemeColors.text(argb));
    }

    public List<WorldRun> worldRuns(String text) {
        RenderSystem.assertOnRenderThread();
        class_1060 manager = class_310.method_1551().method_1531();
        if (this.textureOwner != manager) {
            this.atlases.clear();
            this.textureOwner = manager;
        }
        Atlas atlas = this.atlases.computeIfAbsent(128, key -> new Atlas((int)key, manager));
        List<PreparedRun> prepared = atlas.prepare(this.layout(CustomFont.normalize(text)));
        atlas.upload();
        return prepared.stream().map(run -> new WorldRun(run.page.id, run.quads)).toList();
    }

    public void drawRaw(class_332 g, String text, float x, float y, float size, int argb) {
        this.drawRaw(g, text, x, y, size, argb, true);
    }

    private void drawRaw(class_332 g, String text, float x, float y, float size, int argb, boolean allowBatch) {
        int line;
        if (text == null || text.isEmpty() || !CustomFont.validSize(size) || argb >>> 24 == 0) {
            return;
        }
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            return;
        }
        RenderSystem.assertOnRenderThread();
        class_310 minecraft = class_310.method_1551();
        class_1060 manager = minecraft.method_1531();
        if (this.textureOwner != manager) {
            this.atlases.clear();
            this.textureOwner = manager;
        }
        Matrix3x2fStack pose = g.method_51448();
        double transformScale = Math.max(Math.hypot(pose.m00(), pose.m01()), Math.hypot(pose.m10(), pose.m11()));
        double physicalScale = transformScale * (double)minecraft.method_22683().method_4495();
        int em = CustomFont.rasterEm(size, physicalScale);
        Atlas atlas = this.atlases.computeIfAbsent(em, key -> new Atlas((int)key, manager));
        String normalized = CustomFont.normalize(text);
        if (allowBatch && g instanceof FontBatchAccess) {
            FontBatchAccess batch = (FontBatchAccess)g;
            if (normalized.indexOf(10) < 0) {
                List<PreparedRun> runs = atlas.prepare(this.layout(normalized));
                atlas.upload();
                CustomFont.drawRuns(batch, runs, x, y, size, argb);
                return;
            }
            String[] lines = normalized.split("\n", -1);
            ArrayList<List<PreparedRun>> preparedLines = new ArrayList<List<PreparedRun>>(lines.length);
            for (String line2 : lines) {
                preparedLines.add(atlas.prepare(this.layout(line2)));
            }
            atlas.upload();
            for (int line3 = 0; line3 < lines.length; ++line3) {
                CustomFont.drawRuns(batch, (List)preparedLines.get(line3), x, y + (float)line3 * this.lineHeight(size), size, argb);
            }
            return;
        }
        float scale = size * 4.0f / (float)em;
        if (normalized.indexOf(10) < 0) {
            Layout shaped = this.layout(normalized);
            for (long code : shaped.codes) {
                atlas.glyph(code);
            }
            atlas.upload();
            this.drawLine(g, atlas, shaped, x, y, size, scale, argb);
            return;
        }
        String[] lines = normalized.split("\n", -1);
        Layout[] shaped = new Layout[lines.length];
        for (line = 0; line < lines.length; ++line) {
            shaped[line] = this.layout(lines[line]);
            for (long code : shaped[line].codes) {
                atlas.glyph(code);
            }
        }
        atlas.upload();
        for (line = 0; line < shaped.length; ++line) {
            this.drawLine(g, atlas, shaped[line], x, y + (float)line * this.lineHeight(size), size, scale, argb);
        }
    }

    private static void drawRuns(FontBatchAccess access, List<PreparedRun> runs, float x, float y, float size, int argb) {
        for (PreparedRun run : runs) {
            access.bloodhelper$submitGlyphRun(run.quads, run.page.id, x, y, size, argb);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawLine(class_332 g, Atlas atlas, Layout layout, float x, float y, float size, float scale, int argb) {
        float baseline = y + this.ascent * size;
        Matrix3x2fStack pose = g.method_51448();
        for (int i = 0; i < layout.codes.length; ++i) {
            Glyph glyph = atlas.glyphs.get(layout.codes[i]);
            if (glyph.page == null) continue;
            pose.pushMatrix();
            try {
                pose.translate(x + layout.x[i] * size + (float)glyph.left * scale, baseline + layout.y[i] * size + (float)glyph.top * scale);
                pose.scale(scale, scale);
                g.method_25293(class_10799.field_56883, glyph.page.id, 0, 0, (float)glyph.u, (float)glyph.v, glyph.width, glyph.height, glyph.width, glyph.height, glyph.page.side, glyph.page.side, argb);
                continue;
            }
            finally {
                pose.popMatrix();
            }
        }
    }

    public void drawShadow(class_332 g, String text, float x, float y, float size, int argb) {
        int shadow = (argb >>> 24) * 3 / 5 << 24;
        if (RenderQuality.shadows()) {
            this.drawRaw(g, text, x + 0.75f, y + 0.75f, size, shadow);
        }
        this.draw(g, text, x, y, size, argb);
    }

    private static boolean validSize(float size) {
        return Float.isFinite(size) && size > 0.0f;
    }

    private static String normalize(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n').replace("\t", "    ");
    }

    private static List<Integer> boundaries(String text, boolean words) {
        BreakIterator iterator = words ? BreakIterator.getLineInstance(Locale.ROOT) : BreakIterator.getCharacterInstance(Locale.ROOT);
        iterator.setText(text);
        ArrayList<Integer> result = new ArrayList<Integer>();
        int end = iterator.first();
        while (end != -1) {
            if (end == 0 || end == text.length() || !Character.isLowSurrogate(text.charAt(end)) || !Character.isHighSurrogate(text.charAt(end - 1))) {
                result.add(end);
            }
            end = iterator.next();
        }
        return result;
    }

    synchronized Layout layout(String text) {
        int i;
        Layout cached = this.layouts.get(text);
        if (cached != null) {
            return cached;
        }
        List<BundledFontFallback.Run> runs = BundledFontFallback.runs(text, this.source);
        ArrayList<RunLayout> shaped = new ArrayList<RunLayout>(runs.size());
        int count = 0;
        float advance = 0.0f;
        float[] carets = new float[text.length() + 1];
        for (BundledFontFallback.Run run : runs) {
            Font face = run.face() == 0 ? this.metricFont : this.fallbackMetricFont();
            String part = text.substring(run.start(), run.end());
            Object chars = part.toCharArray();
            GlyphVector vector = face.layoutGlyphVector(FRC, (char[])chars, 0, ((Object)chars).length, 0);
            float[] positions = this.shapedCarets(part, vector, face);
            for (i = 0; i < positions.length; ++i) {
                carets[run.start() + i] = advance + positions[i];
            }
            shaped.add(new RunLayout(run.face(), vector, advance));
            count += vector.getNumGlyphs();
            advance += (float)vector.getGlyphPosition(vector.getNumGlyphs()).getX() / 128.0f;
        }
        long[] codes = new long[count];
        float[] x = new float[count];
        float[] y = new float[count];
        int index = 0;
        for (RunLayout run : shaped) {
            GlyphVector vector = run.vector;
            i = 0;
            while (i < vector.getNumGlyphs()) {
                codes[index] = BundledFontFallback.glyphKey(run.face, vector.getGlyphCode(i));
                Point2D position = vector.getGlyphPosition(i);
                x[index] = run.advance + (float)position.getX() / 128.0f;
                y[index] = (float)position.getY() / 128.0f;
                ++i;
                ++index;
            }
        }
        Layout result = new Layout(codes, x, y, carets, advance);
        this.layouts.put(text, result);
        return result;
    }

    private Font fallbackMetricFont() {
        if (this.fallbackMetricFont == null) {
            this.fallbackMetricFont = BundledFontFallback.font().deriveFont(128.0f).deriveFont(Map.of(TextAttribute.KERNING, TextAttribute.KERNING_ON, TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON));
        }
        return this.fallbackMetricFont;
    }

    public boolean supportsCodePoint(int codepoint) {
        return Character.isValidCodePoint(codepoint) && (this.source.canDisplay(codepoint) || BundledFontFallback.font().canDisplay(codepoint));
    }

    private float[] shapedCarets(String text, GlyphVector vector, Font runFont) {
        int i;
        float advance;
        int length = text.length();
        float[] carets = new float[length + 1];
        if (length == 0) {
            return carets;
        }
        float[] starts = new float[length];
        float[] ends = new float[length];
        Arrays.fill(starts, Float.NaN);
        Arrays.fill(ends, Float.NEGATIVE_INFINITY);
        for (int i2 = 0; i2 < vector.getNumGlyphs(); ++i2) {
            int character = vector.getGlyphCharIndex(i2);
            if (character < 0 || character >= length) continue;
            if (character > 0 && Character.isLowSurrogate(text.charAt(character)) && Character.isHighSurrogate(text.charAt(character - 1))) {
                --character;
            }
            while (character > 0 && CustomFont.isCaretContinuation(text.codePointAt(character))) {
                character = text.offsetByCodePoints(character, -1);
            }
            float start = (float)vector.getGlyphPosition(i2).getX();
            float end = start + vector.getGlyphMetrics(i2).getAdvanceX();
            starts[character] = Float.isNaN(starts[character]) ? start : Math.min(starts[character], start);
            ends[character] = Math.max(ends[character], end);
        }
        if (Float.isNaN(starts[0])) {
            starts[0] = 0.0f;
            ends[0] = 0.0f;
        }
        ArrayList<Integer> clusters = new ArrayList<Integer>();
        for (int i3 = 0; i3 < length; ++i3) {
            if (Float.isNaN(starts[i3])) continue;
            clusters.add(i3);
        }
        clusters.add(length);
        carets[length] = advance = (float)vector.getGlyphPosition(vector.getNumGlyphs()).getX();
        for (i = 1; i < clusters.size() - 1; ++i) {
            int previous = (Integer)clusters.get(i - 1);
            int current = (Integer)clusters.get(i);
            carets[current] = Math.clamp((ends[previous] + starts[current]) * 0.5f, carets[previous], Math.max(carets[previous], advance));
        }
        for (i = 0; i < clusters.size() - 1; ++i) {
            int cp;
            int end;
            int start = (Integer)clusters.get(i);
            if (text.codePointCount(start, end = ((Integer)clusters.get(i + 1)).intValue()) == 1) {
                for (int p = start + 1; p < end; ++p) {
                    carets[p] = carets[start];
                }
                continue;
            }
            float[] weights = new float[end - start];
            float total = 0.0f;
            for (int p = start; p < end; p += Character.charCount(cp)) {
                float weight;
                cp = text.codePointAt(p);
                if (CustomFont.isCaretContinuation(cp)) continue;
                GlyphVector plain = runFont.createGlyphVector(FRC, new String(Character.toChars(cp)));
                weights[p - start] = weight = Math.max(0.0f, (float)plain.getGlyphPosition(plain.getNumGlyphs()).getX());
                total += weight;
            }
            float accumulated = 0.0f;
            float left = carets[start];
            float span = carets[end] - left;
            int p = start;
            while (p < end) {
                int next = p + Character.charCount(text.codePointAt(p));
                for (int interior = p + 1; interior < next; ++interior) {
                    carets[interior] = carets[p];
                }
                carets[next] = total > 0.0f ? left + span * (accumulated += weights[p - start]) / total : left + span;
                p = next;
            }
            carets[end] = left + span;
        }
        i = 0;
        while (i < carets.length) {
            int n = i++;
            carets[n] = carets[n] / 128.0f;
        }
        return carets;
    }

    private static boolean isCaretContinuation(int codepoint) {
        int type = Character.getType(codepoint);
        return type == 6 || type == 8 || type == 7 || type == 16;
    }

    private static int rasterEm(float size, double physicalScale) {
        int em;
        double target = (double)(size * 4.0f) * Math.max(1.0, physicalScale);
        for (em = 64; (double)em < target && em < 2048; em *= 2) {
        }
        return em;
    }

    static {
        FRC = new FontRenderContext(null, true, true);
        UPLOAD_GENERATION = new AtomicLong();
        NEXT_PAGE = new AtomicLong();
    }

    private static final class RegularHolder {
        private static final CustomFont INSTANCE = new CustomFont("sfpromedium.otf");

        private RegularHolder() {
        }
    }

    private static final class MediumHolder {
        private static final CustomFont INSTANCE = new CustomFont("sfprosemibold.otf");

        private MediumHolder() {
        }
    }

    private final class Atlas {
        private final Font font;
        private Font fallbackFont;
        private final int em;
        private final int pageSide;
        private final class_1060 manager;
        private final Map<Long, Glyph> glyphs = new HashMap<Long, Glyph>();
        private final List<Page> pages = new ArrayList<Page>();
        private final Map<Layout, List<PreparedRun>> prepared = new LinkedHashMap<Layout, List<PreparedRun>>(32, 0.75f, true){

            @Override
            protected boolean removeEldestEntry(Map.Entry<Layout, List<PreparedRun>> eldest) {
                return this.size() > 128;
            }
        };

        private Atlas(int em, class_1060 manager) {
            this.em = em;
            this.font = CustomFont.this.source.deriveFont((float)em);
            this.pageSide = Math.max(512, em * 2 / 4);
            this.manager = manager;
        }

        private List<PreparedRun> prepare(Layout layout) {
            List<PreparedRun> cached = this.prepared.get(layout);
            if (cached != null) {
                for (PreparedRun run : cached) {
                    run.page.lastUsedFrame = frame;
                }
                return cached;
            }
            if (layout.codes.length == 0) {
                return List.of();
            }
            ArrayList<PreparedRun> result = new ArrayList<PreparedRun>();
            float[] quads = new float[layout.codes.length * 8];
            float normalizedScale = 4.0f / (float)this.em;
            Page active = null;
            int used = 0;
            for (int i = 0; i < layout.codes.length; ++i) {
                Glyph glyph = this.glyph(layout.codes[i]);
                if (glyph.page == null) continue;
                if (active != glyph.page) {
                    if (active != null) {
                        result.add(new PreparedRun(active, new FontGlyphRun(Arrays.copyOf(quads, used))));
                    }
                    active = glyph.page;
                    used = 0;
                }
                quads[used++] = layout.x[i] + (float)glyph.left * normalizedScale;
                quads[used++] = CustomFont.this.ascent + layout.y[i] + (float)glyph.top * normalizedScale;
                quads[used++] = layout.x[i] + (float)(glyph.left + glyph.width) * normalizedScale;
                quads[used++] = CustomFont.this.ascent + layout.y[i] + (float)(glyph.top + glyph.height) * normalizedScale;
                quads[used++] = (float)glyph.u / (float)active.side;
                quads[used++] = (float)glyph.v / (float)active.side;
                quads[used++] = (float)(glyph.u + glyph.width) / (float)active.side;
                quads[used++] = (float)(glyph.v + glyph.height) / (float)active.side;
            }
            if (active != null) {
                result.add(new PreparedRun(active, new FontGlyphRun(Arrays.copyOf(quads, used))));
            }
            List<PreparedRun> runs = List.copyOf(result);
            if (layout.codes.length <= 256) {
                this.prepared.put(layout, runs);
            }
            return runs;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private Glyph glyph(long code) {
            GlyphVector vector;
            Rectangle bounds;
            Glyph cached = this.glyphs.get(code);
            if (cached != null) {
                if (cached.page != null) {
                    cached.page.lastUsedFrame = frame;
                }
                return cached;
            }
            CustomFont.this.cacheGrew = true;
            Font glyphFont = this.font;
            if (BundledFontFallback.face(code) != 0) {
                if (this.fallbackFont == null) {
                    this.fallbackFont = BundledFontFallback.font().deriveFont((float)this.em);
                }
                glyphFont = this.fallbackFont;
            }
            if ((bounds = (vector = glyphFont.createGlyphVector(FRC, new int[]{(int)code})).getGlyphPixelBounds(0, FRC, 0.0f, 0.0f)).isEmpty()) {
                Glyph empty = new Glyph(null, 0, 0, 0, 0, 0, 0);
                this.glyphs.put(code, empty);
                return empty;
            }
            int left = Math.floorDiv(bounds.x, 4) - 2;
            int top = Math.floorDiv(bounds.y, 4) - 2;
            int right = Math.ceilDiv(bounds.x + bounds.width, 4) + 2;
            int bottom = Math.ceilDiv(bounds.y + bounds.height, 4) + 2;
            int w = right - left;
            int h = bottom - top;
            int rasterWidth = w * 4;
            int rasterHeight = h * 4;
            BufferedImage bitmap = new BufferedImage(rasterWidth, rasterHeight, 2);
            Graphics2D graphics = bitmap.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                graphics.setColor(Color.WHITE);
                graphics.drawGlyphVector(vector, -left * 4, -top * 4);
            }
            finally {
                graphics.dispose();
            }
            Page page = null;
            for (Page candidate : this.pages) {
                if (!candidate.reserve(w, h)) continue;
                page = candidate;
                break;
            }
            if (page == null) {
                int side;
                for (side = this.pageSide; side < Math.max(w, h); side *= 2) {
                }
                page = new Page(this.manager, side);
                this.pages.add(page);
                page.reserve(w, h);
            }
            int u = page.reservedX;
            int v = page.reservedY;
            int[] pixels = bitmap.getRGB(0, 0, rasterWidth, rasterHeight, null, 0, rasterWidth);
            int samples = 16;
            for (int row = 0; row < h; ++row) {
                for (int col = 0; col < w; ++col) {
                    int coverage = 0;
                    for (int sy = 0; sy < 4; ++sy) {
                        int offset = (row * 4 + sy) * rasterWidth + col * 4;
                        for (int sx = 0; sx < 4; ++sx) {
                            coverage += pixels[offset + sx] >>> 24;
                        }
                    }
                    int alpha = (coverage + samples / 2) / samples;
                    page.pixels[(v + row) * page.side + u + col] = alpha << 24 | 0xFFFFFF;
                }
            }
            page.dirty = true;
            page.lastUsedFrame = frame;
            Glyph glyph = new Glyph(page, u, v, w, h, left, top);
            this.glyphs.put(code, glyph);
            return glyph;
        }

        private void upload() {
            for (Page page : this.pages) {
                page.upload();
            }
        }
    }

    private static final class Page {
        private final class_1060 manager;
        private final class_2960 id;
        private final int side;
        private final int[] pixels;
        private LinearTexture texture;
        private long uploadedGeneration = -1L;
        private boolean dirty = true;
        private long lastUsedFrame;
        private int cursorX;
        private int cursorY;
        private int rowHeight;
        private int reservedX;
        private int reservedY;

        private Page(class_1060 manager, int side) {
            this.manager = manager;
            this.side = side;
            this.id = class_2960.method_60655((String)"bloodhelper", (String)("customfont/page_" + NEXT_PAGE.getAndIncrement()));
            this.pixels = new int[side * side];
            Arrays.fill(this.pixels, 0xFFFFFF);
        }

        private boolean reserve(int w, int h) {
            if (w > this.side || h > this.side) {
                return false;
            }
            int x = this.cursorX;
            int y = this.cursorY;
            int row = this.rowHeight;
            if (x + w > this.side) {
                x = 0;
                y += row;
                row = 0;
            }
            if (y + h > this.side) {
                return false;
            }
            this.reservedX = x;
            this.reservedY = y;
            this.cursorX = x + w;
            this.cursorY = y;
            this.rowHeight = Math.max(row, h);
            return true;
        }

        private void upload() {
            boolean live;
            long generation = UPLOAD_GENERATION.get();
            boolean bl = live = this.texture != null && this.texture.live() && this.manager.method_4619(this.id) == this.texture;
            if (live && !this.dirty && this.uploadedGeneration == generation) {
                return;
            }
            class_1011 image = live ? this.texture.method_4525() : new class_1011(this.side, this.side, false);
            for (int y = 0; y < this.side; ++y) {
                for (int x = 0; x < this.side; ++x) {
                    image.method_61941(x, y, this.pixels[y * this.side + x]);
                }
            }
            if (live) {
                this.texture.method_4524();
            } else {
                LinearTexture replacement;
                try {
                    replacement = new LinearTexture(this.id.toString(), image);
                }
                catch (Error | RuntimeException e) {
                    image.close();
                    throw e;
                }
                try {
                    this.manager.method_4616(this.id, (class_1044)replacement);
                }
                catch (Error | RuntimeException e) {
                    replacement.close();
                    throw e;
                }
                this.texture = replacement;
            }
            this.dirty = false;
            this.uploadedGeneration = generation;
        }
    }

    record Layout(long[] codes, float[] x, float[] y, float[] carets, float advance) {
    }

    private record FitKey(String text, float size, float width) {
    }

    private record Glyph(Page page, int u, int v, int width, int height, int left, int top) {
    }

    private record PreparedRun(Page page, FontGlyphRun quads) {
    }

    private record RunLayout(int face, GlyphVector vector, float advance) {
    }

    public record WorldRun(class_2960 texture, FontGlyphRun glyphs) {
    }

    private static final class LinearTexture
    extends class_1043 {
        private LinearTexture(String label, class_1011 image) {
            super(() -> label, image);
            this.field_63613 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        }

        private boolean live() {
            return this.method_4525() != null && this.field_56974 != null && !this.field_56974.isClosed() && this.field_60597 != null && !this.field_60597.isClosed();
        }
    }
}

