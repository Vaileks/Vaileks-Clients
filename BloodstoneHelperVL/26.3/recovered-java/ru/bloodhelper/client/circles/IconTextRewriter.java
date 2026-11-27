/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.StringDecomposer
 */
package ru.bloodhelper.client.circles;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;

public final class IconTextRewriter {
    private final ColorResolver colors;

    public IconTextRewriter(ColorResolver colors) {
        this.colors = colors;
    }

    public Component rewrite(Component input) {
        if (input == null || input.getString(2049).length() > 2048 || input.visit(value -> {
            int codePoint;
            for (int offset = 0; offset < value.length(); offset += Character.charCount(codePoint)) {
                codePoint = value.codePointAt(offset);
                if (!IconTextRewriter.isIcon(codePoint)) continue;
                return Optional.of(Boolean.TRUE);
            }
            return Optional.empty();
        }).isEmpty()) {
            return input;
        }
        Result result = new Result();
        StringDecomposer.iterateFormatted((FormattedText)input, (Style)Style.EMPTY, (index, style, codePoint) -> {
            int color;
            int n = color = IconTextRewriter.isIcon(codePoint) ? this.colors.resolve(style, codePoint) : -1;
            if (color != -1) {
                result.append(9679, Style.EMPTY.withColor(color).withClickEvent(style.getClickEvent()).withHoverEvent(style.getHoverEvent()).withInsertion(style.getInsertion()));
                result.changed = true;
            } else {
                result.append(codePoint, style);
            }
            return true;
        });
        return result.changed ? result.finish() : input;
    }

    public static boolean isIcon(int codePoint) {
        return codePoint >= 57344 && codePoint <= 63743 || codePoint >= 983040 && codePoint <= 1048573 || codePoint >= 0x100000 && codePoint <= 1114109;
    }

    @FunctionalInterface
    public static interface ColorResolver {
        public int resolve(Style var1, int var2);
    }

    private static final class Result {
        private final MutableComponent text = Component.empty();
        private final StringBuilder buffer = new StringBuilder();
        private Style style;
        private boolean changed;

        private Result() {
        }

        void append(int codePoint, Style nextStyle) {
            if (!nextStyle.equals((Object)this.style)) {
                this.flush();
                this.style = nextStyle;
            }
            this.buffer.appendCodePoint(codePoint);
        }

        void flush() {
            if (!this.buffer.isEmpty()) {
                this.text.append((Component)Component.literal((String)this.buffer.toString()).setStyle(this.style));
                this.buffer.setLength(0);
            }
        }

        Component finish() {
            this.flush();
            return this.text;
        }
    }
}

