/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_5223
 *  net.minecraft.class_5250
 *  net.minecraft.class_5348
 */
package ru.bloodhelper.client.circles;

import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5223;
import net.minecraft.class_5250;
import net.minecraft.class_5348;

public final class IconTextRewriter {
    private final ColorResolver colors;

    public IconTextRewriter(ColorResolver colors) {
        this.colors = colors;
    }

    public class_2561 rewrite(class_2561 input) {
        if (input == null || input.method_10858(2049).length() > 2048 || input.method_27657(value -> {
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
        class_5223.method_27476((class_5348)input, (class_2583)class_2583.field_24360, (index, style, codePoint) -> {
            int color;
            int n = color = IconTextRewriter.isIcon(codePoint) ? this.colors.resolve(style, codePoint) : -1;
            if (color != -1) {
                result.append(9679, class_2583.field_24360.method_36139(color).method_10958(style.method_10970()).method_10949(style.method_10969()).method_10975(style.method_10955()));
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
        public int resolve(class_2583 var1, int var2);
    }

    private static final class Result {
        private final class_5250 text = class_2561.method_43473();
        private final StringBuilder buffer = new StringBuilder();
        private class_2583 style;
        private boolean changed;

        private Result() {
        }

        void append(int codePoint, class_2583 nextStyle) {
            if (!nextStyle.equals((Object)this.style)) {
                this.flush();
                this.style = nextStyle;
            }
            this.buffer.appendCodePoint(codePoint);
        }

        void flush() {
            if (!this.buffer.isEmpty()) {
                this.text.method_10852((class_2561)class_2561.method_43470((String)this.buffer.toString()).method_10862(this.style));
                this.buffer.setLength(0);
            }
        }

        class_2561 finish() {
            this.flush();
            return this.text;
        }
    }
}

