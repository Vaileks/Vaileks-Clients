/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import ru.bloodhelper.client.gui.BundledCjkFont;

final class BundledFontFallback {
    static final String RESOURCE = "/assets/bloodhelper/font/cjk.ttf.xz";

    private BundledFontFallback() {
    }

    static Font font() {
        return Holder.FONT;
    }

    private static Font load() {
        Font font;
        block8: {
            InputStream stream = BundledCjkFont.open();
            try {
                font = Font.createFont(0, stream);
                if (stream == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (FontFormatException | IOException exception) {
                    throw new IllegalStateException("Cannot load bundled CJK font /assets/bloodhelper/font/cjk.ttf.xz", exception);
                }
            }
            stream.close();
        }
        return font;
    }

    static List<Run> runs(String text, Font primary) {
        int cp;
        if (text.isEmpty()) {
            return List.of();
        }
        ArrayList<Run> result = new ArrayList<Run>();
        int start = 0;
        int face = -1;
        for (int index = 0; index < text.length(); index += Character.charCount(cp)) {
            int nextFace;
            Font previous;
            cp = text.codePointAt(index);
            Font font = previous = face == 1 ? BundledFontFallback.font() : primary;
            if (face >= 0 && BundledFontFallback.continuation(cp) && (previous.canDisplay(cp) || Character.getType(cp) == 16 || BundledFontFallback.variationSelector(cp))) {
                nextFace = face;
            } else {
                int n = nextFace = primary.canDisplay(cp) || !BundledFontFallback.font().canDisplay(cp) ? 0 : 1;
            }
            if (nextFace == face) continue;
            if (face >= 0) {
                result.add(new Run(start, index, face));
            }
            start = index;
            face = nextFace;
        }
        result.add(new Run(start, text.length(), face));
        return result;
    }

    private static boolean continuation(int cp) {
        int type = Character.getType(cp);
        return type == 6 || type == 8 || type == 7 || type == 16;
    }

    private static boolean variationSelector(int cp) {
        return cp >= 65024 && cp <= 65039 || cp >= 917760 && cp <= 917999;
    }

    static long glyphKey(int face, int glyph) {
        return (long)face << 32 | Integer.toUnsignedLong(glyph);
    }

    static int face(long glyphKey) {
        return (int)(glyphKey >>> 32);
    }

    private static final class Holder {
        private static final Font FONT = BundledFontFallback.load();

        private Holder() {
        }
    }

    record Run(int start, int end, int face) {
    }
}

