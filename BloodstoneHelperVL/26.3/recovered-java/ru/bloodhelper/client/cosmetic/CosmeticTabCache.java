/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.network.chat.Component
 */
package ru.bloodhelper.client.cosmetic;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import ru.bloodhelper.client.cosmetic.CosmeticNameText;
import ru.bloodhelper.client.cosmetic.NicknamePalette;

public final class CosmeticTabCache {
    private final Map<PlayerInfo, Painted> entries = new WeakHashMap<PlayerInfo, Painted>();

    public Component color(PlayerInfo player, Component original, String nickname, NicknamePalette palette, long time) {
        long frame = palette.animated() ? time / 33L : 0L;
        int signature = original.hashCode();
        Painted previous = this.entries.get(player);
        if (previous != null && previous.frame == frame && previous.signature == signature && previous.palette.equals(palette) && previous.original.equals((Object)original)) {
            return previous.result;
        }
        Component result = CosmeticNameText.color(original, nickname, palette, time);
        if (this.entries.size() >= 256) {
            this.entries.clear();
        }
        this.entries.put(player, new Painted((Component)original.copy(), signature, palette, frame, result));
        return result;
    }

    private record Painted(Component original, int signature, NicknamePalette palette, long frame, Component result) {
    }
}

