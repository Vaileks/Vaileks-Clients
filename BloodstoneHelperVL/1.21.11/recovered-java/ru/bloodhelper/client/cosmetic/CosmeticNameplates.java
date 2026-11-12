/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_310
 *  net.minecraft.class_5250
 *  net.minecraft.class_5481
 *  net.minecraft.class_640
 */
package ru.bloodhelper.client.cosmetic;

import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import net.minecraft.class_5481;
import net.minecraft.class_640;
import ru.bloodhelper.client.cosmetic.CosmeticNameText;
import ru.bloodhelper.client.cosmetic.CosmeticNames;
import ru.bloodhelper.client.cosmetic.NicknamePalette;
import ru.bloodhelper.client.modules.StreamerMode;

public final class CosmeticNameplates {
    private CosmeticNameplates() {
    }

    private static boolean nameChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_';
    }

    public static class_5481 recolor(class_5481 sequence) {
        class_5250 original = class_2561.method_43473();
        sequence.accept((index, style, point) -> {
            original.method_10852((class_2561)class_2561.method_43470((String)new String(Character.toChars(point))).method_10862(style));
            return true;
        });
        class_2561 colored = CosmeticNameplates.recolor((class_2561)original);
        ArrayList runs = new ArrayList();
        colored.method_27658((style, text) -> {
            runs.add(class_5481.method_30747((String)text, (class_2583)style));
            return Optional.empty();
        }, class_2583.field_24360);
        return class_5481.method_30749(runs);
    }

    public static class_2561 recolor(class_2561 text) {
        class_310 mc = class_310.method_1551();
        if (mc.method_1562() == null || StreamerMode.hideNickname() || StreamerMode.hideNametags()) {
            return text;
        }
        String plain = text.getString();
        int index = 0;
        while (index < plain.length()) {
            Optional<NicknamePalette> palette;
            int start = index;
            while (index < plain.length() && CosmeticNameplates.nameChar(plain.charAt(index))) {
                ++index;
            }
            if (index == start) {
                ++index;
                continue;
            }
            if (index - start > 16) continue;
            String name = plain.substring(start, index);
            class_640 info = mc.method_1562().method_2874(name);
            if (info == null || !(palette = CosmeticNames.palette(info.method_2966().id(), name, false)).isPresent()) continue;
            text = CosmeticNameText.color(text, name, palette.get(), System.currentTimeMillis());
        }
        return text;
    }
}

