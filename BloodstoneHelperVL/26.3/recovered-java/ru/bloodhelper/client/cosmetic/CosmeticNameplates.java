/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.FormattedCharSequence
 */
package ru.bloodhelper.client.cosmetic;

import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
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

    public static FormattedCharSequence recolor(FormattedCharSequence sequence) {
        MutableComponent original = Component.empty();
        sequence.accept((index, style, point) -> {
            original.append((Component)Component.literal((String)new String(Character.toChars(point))).setStyle(style));
            return true;
        });
        Component colored = CosmeticNameplates.recolor((Component)original);
        ArrayList runs = new ArrayList();
        colored.visit((style, text) -> {
            runs.add(FormattedCharSequence.forward((String)text, (Style)style));
            return Optional.empty();
        }, Style.EMPTY);
        return FormattedCharSequence.composite(runs);
    }

    public static Component recolor(Component text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || StreamerMode.hideNickname() || StreamerMode.hideNametags()) {
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
            PlayerInfo info = mc.getConnection().getPlayerInfo(name);
            if (info == null || !(palette = CosmeticNames.palette(info.getProfile().id(), name, false)).isPresent()) continue;
            text = CosmeticNameText.color(text, name, palette.get(), System.currentTimeMillis());
        }
        return text;
    }
}

