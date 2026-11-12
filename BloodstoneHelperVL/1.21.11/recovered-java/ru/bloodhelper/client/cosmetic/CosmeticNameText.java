/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_5250
 */
package ru.bloodhelper.client.cosmetic;

import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;
import ru.bloodhelper.client.cosmetic.CosmeticRoster;
import ru.bloodhelper.client.cosmetic.NicknamePalette;

public final class CosmeticNameText {
    private static final int MAX_TEXT = 4096;

    private CosmeticNameText() {
    }

    public static class_2561 color(class_2561 original, String nickname, int rgb) {
        if (rgb < 0 || rgb > 0xFFFFFF) {
            return original;
        }
        return CosmeticNameText.color(original, nickname, NicknamePalette.solid(rgb), 0L);
    }

    public static class_2561 color(class_2561 original, String nickname, NicknamePalette palette, long time) {
        if (original == null || !CosmeticRoster.validName(nickname) || palette == null) {
            return original;
        }
        String expected = original.method_10858(4097);
        if (expected.length() > 4096) {
            return original;
        }
        int start = CosmeticNameText.uniqueNameStart(expected, nickname);
        if (start < 0) {
            return original;
        }
        int end = start + nickname.length();
        ArrayList spans = new ArrayList();
        StringBuilder plain = new StringBuilder(expected.length());
        original.method_27658((style, text) -> {
            spans.add(new Span(style, text));
            plain.append(text);
            return Optional.empty();
        }, class_2583.field_24360);
        if (!plain.toString().equals(expected)) {
            return original;
        }
        class_5250 result = class_2561.method_43473();
        int offset = 0;
        for (Span span : spans) {
            int local = 0;
            while (local < span.text.length()) {
                boolean inside;
                int absolute = offset + local;
                boolean bl = inside = absolute >= start && absolute < end;
                int boundary = absolute < start ? start : (inside ? end : offset + span.text.length());
                int next = Math.min(span.text.length(), boundary - offset);
                if (next <= local) {
                    return original;
                }
                if (inside && palette.colors().size() == 1) {
                    result.method_10852((class_2561)class_2561.method_43470((String)span.text.substring(local, next)).method_10862(span.style.method_36139(palette.colors().getFirst().intValue())));
                } else if (inside) {
                    int count;
                    for (int i = local; i < next; i += count) {
                        int point = span.text.codePointAt(i);
                        count = Character.charCount(point);
                        result.method_10852((class_2561)class_2561.method_43470((String)new String(Character.toChars(point))).method_10862(span.style.method_36139(palette.at(offset + i - start, nickname.length(), time))));
                    }
                } else {
                    result.method_10852((class_2561)class_2561.method_43470((String)span.text.substring(local, next)).method_10862(span.style));
                }
                local = next;
            }
            offset += span.text.length();
        }
        return result;
    }

    private static int uniqueNameStart(String text, String name) {
        int found = -1;
        for (int index = 0; index <= text.length() - name.length(); ++index) {
            if (!text.regionMatches(true, index, name, 0, name.length())) continue;
            int end = index + name.length();
            if (index > 0 && CosmeticNameText.nameCharacter(text.charAt(index - 1)) || end < text.length() && CosmeticNameText.nameCharacter(text.charAt(end))) continue;
            if (found >= 0) {
                return -1;
            }
            found = index;
        }
        return found;
    }

    private static boolean nameCharacter(char character) {
        return character >= 'A' && character <= 'Z' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '_';
    }

    private record Span(class_2583 style, String text) {
    }
}

